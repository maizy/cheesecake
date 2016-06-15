/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

define(
[
    "react",
    "immutable",
    "http-utils",
    "components/extra-info",
    "components/endpoint",
    "react-mixins/set-interval",
    "components/status"
],
function(
    React,
    Immutable,
    HttpUtils,
    ExtraInfo,
    Endpoint,
    SetIntervalMixin,
    Status
) {

    const STATUS_TABLE_STATUSES = {
        loading: "loading",
        error: "error",
        hasData: "hasData"
    };

    const COLUMNS = 4;

    const StatusCell = React.createClass({
        render: function () {
            const status = this.props.aggregates.get("current-status").get("value");
            return (
                <td className={`status status__${status}`}>
                    <Status aggregates={this.props.aggregates}/>
                </td>
            );
        }
    });

    const EndpointRow = React.createClass({
        render: function () {
            const service = this.props.service;
            const endpoint = this.props.endpoint;
            const aggregates = this.props.aggregates;
            const serviceNameCell = this.props.isFirst ?
                (
                    <td className="service"
                        rowSpan={this.props.thisServiceEndpointsAmount}>
                        {service.get("name")}
                    </td>
                )
                : null;
            const extraInfo = aggregates.get("current-extra-info").get("value");
            return (
                <tr>
                    {serviceNameCell}
                    <td className="endpoint"><Endpoint.EndpointName endpoint={endpoint}/></td>
                    <StatusCell aggregates={aggregates}/>
                    <td className="extra"><ExtraInfo values={extraInfo}/></td>
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

    const REFRESH_INTERVAL = 5000;  // ms

    const StatusTable = React.createClass({
        mixins: [SetIntervalMixin],
        getInitialState: function () {
            return {
                status: STATUS_TABLE_STATUSES.loading
            };
        },
        componentDidMount: function () {
            this.refreshStatusTable(() => {
                this.addInterval(this.refreshStatusTable, REFRESH_INTERVAL);
            });
        },
        refreshStatusTable: function(onReady) {
            HttpUtils.getJson("/services/state/full_view", "", res => {
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
                onReady ? (onReady.bind(this))() : null;
            });
        },
        render: function () {
            const status = this.state.status;
            let content = null;
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
                                         aggregates={endpoint.get("aggregates")}
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
                        <th className="extra">Extra</th>
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
