define(
[
    "react"
],
function(
    React
) {
    const EndpointName = React.createClass({
        render: function () {
            // FIXME: all types support

            const endpoint = this.props.endpoint;
            return (
                <span>
                http://
                {endpoint.get("address").get("hostname")}
                :
                {endpoint.get("port")}
                {endpoint.get("path")}
            </span>
            );
        }
    });
    return {
        EndpointName: EndpointName
    };
});
