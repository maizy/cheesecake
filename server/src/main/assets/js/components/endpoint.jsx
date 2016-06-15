/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

define(
[
    "react"
],
function(
    React
) {
    const EndpointName = React.createClass({
        render: function () {
            const endpoint = this.props.endpoint;
            const type = endpoint.get("type");
            const port = endpoint.get("port");
            switch (type) {
                case "http":
                    return (<span>
                        http://
                        {endpoint.get("address").get("hostname")}
                        {port == "80" ? null : `:${port}`}
                        {endpoint.get("path")}
                    </span>
                    );

                default:
                    return <span>Unknown type: {type}</span>;

            }


        }
    });
    return {
        EndpointName: EndpointName
    };
});
