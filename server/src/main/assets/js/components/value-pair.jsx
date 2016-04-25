define(
[
    "react"
],
function(
    React
) {
    return React.createClass({
        render: function () {
            return (
                <li>{this.props.name}:{' '}
                    <span className="value">{this.props.value}</span>
                </li>
            );
        }
    });
});
