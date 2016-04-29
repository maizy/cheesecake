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
