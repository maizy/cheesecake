/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

define(
[
    "react",
    "immutable",
    "components/value-pair"
],
function(
    React,
    Immutable,
    ValuePair
) {
    return React.createClass({
        render: function () {

            const values = this.props.values != null
                ? this.props.values.map((value, name) => <ValuePair name={name} value={value}/>)
                : null;
            return values != null
                ? (
                    <ul>
                        {values}
                    </ul>
                )
                : null
        }
    });
});
