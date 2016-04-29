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
            switch (type) {
                case "http":
                    return (<span>
                        http://
                        {endpoint.get("address").get("hostname")}
                        :
                        {endpoint.get("port")}
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
