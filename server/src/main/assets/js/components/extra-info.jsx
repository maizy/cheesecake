/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

define(
[
    "react",
    "immutable",
    "components/value-pair",
],
function(
    React,
    Immutable,
    ValuePair
) {
    return React.createClass({
        render: function () {

            let valuesProp = Immutable.OrderedMap([
                ["version", "1.2.3"],
                ["other", "very long value"]
            ]);

            let values = valuesProp.map((name, value) => <ValuePair name={name} value={value}/>);
            return <ul>
                {values}
            </ul>
        }
    });
});
