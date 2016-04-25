define(
[
    "react",
    "immutable",
    "http-utils",
    "components/extra-info",
    "components/endpoint"
],
function(
    React,
    Immutable,
    HttpUtils,
    ExtraInfo,
    Endpoint
) {

    const STATUS_TABLE_STATUSES = {
        loading: "loading",
        error: "error",
        hasData: "hasData"
    };

    const COLUMNS = 3;

    const StatusCell = React.createClass({
        render: function () {
            return (
                <td className="status"/>
            );
        }
    });

    const EndpointRow = React.createClass({
        render: function () {
            let serviceNameCell = this.props.isFirst ?
                (
                    <td className="service"
                        rowSpan={this.props.thisServiceEndpointsAmount}>
                        {this.props.service.get("name")}
                    </td>
                )
                : null;

            return (
                <tr>
                    {serviceNameCell}
                    <td className="endpoint"><Endpoint.EndpointName endpoint={this.props.endpoint}/></td>
                    <StatusCell/>
                    {/* <td className="extra"><ExtraInfo /></td> */}
                </tr>
            );
        }
    });

    const StatusTableFullWidthRow = React.createClass({
        render: function() {
            return (
                <tbody>
                <tr>
                    <td colSpan={COLUMNS} className={this.props.className}>
                        {this.props.message}
                    </td>
                </tr>
                </tbody>
            );
        }
    });

    const StatusTableAlert = React.createClass({
        render: function() {
            const alert = (
                <div className={`alert alert-${this.props.type}`}
                     role="alert">
                    {this.props.message}
                </div>);
            return <StatusTableFullWidthRow message={alert}/>;
        }
    });

    const StatusTable = React.createClass({
        getInitialState: function () {
            return {
                status: STATUS_TABLE_STATUSES.loading
            };
        },
        componentDidMount: function () {
            HttpUtils.getJson("/", "assets/status-stub.json", res => {
                if (res.success) {
                    let immutableRes = Immutable.fromJS(res.data["services_results"]);
                    this.setState({
                        status: STATUS_TABLE_STATUSES.hasData,
                        fullView: immutableRes
                    });
                } else {
                    this.setState({
                        status: STATUS_TABLE_STATUSES.error,
                        error: res.message
                    });
                }
            });
        },
        render: function () {
            const status = this.state.status;
            var content = null;
            if (status == STATUS_TABLE_STATUSES.loading) {
                content = <StatusTableAlert type="info" message="Loading ..."/>;
            } else if (status == STATUS_TABLE_STATUSES.hasData) {
                let services = this.state.fullView.get("services").flatMap(service => {
                    let totalServices = service.get("endpoints").size;
                    return service.get("endpoints").map((endpoint, index) => {
                        return (
                            <EndpointRow thisServiceEndpointsAmount={totalServices}
                                         isFirst={index == 0}
                                         endpoint={endpoint.get("endpoint")}
                                         service={service.get("service")}
                            />
                        );
                    });

                });
                content = <tbody>{services}</tbody>;

            } else if (status == STATUS_TABLE_STATUSES.error) {
                content = <StatusTableAlert type="danger" message={`Error: ${this.state.error}`}/>;
            }
            return (
                <table className="table table-bordered status-table">
                    <thead>
                    <tr>
                        <th className="service">Service</th>
                        <th className="endpoint">Endpoint</th>
                        <th className="status">Status</th>
                        {/* <th className="extra">Extra</th> */}
                    </tr>
                    </thead>
                    {content}
                </table>
            );
        }
    });
    return {
        StatusTable: StatusTable
    };
});
