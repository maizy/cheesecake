/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

define(
[
    "react",
    "string-utils",
    "seq-utils",
    "immutable",
    "time-utils"
],
function(
    React,
    StringUtils,
    SeqUtil,
    Immutable,
    TimeUtils
) {

    const intersperse = SeqUtil.intersperse;

    return React.createClass({

        render: function () {
            const aggregates = this.props.aggregates;
            const status = aggregates.get("current-status").get("value");
            const uptimeChecks = aggregates.get("uptime-checks").get("value");
            const uptimeDuration = aggregates.get("uptime-duration").get("value").get("seconds");

            let uptimeInfo = Immutable.Seq
                .of(
                    uptimeDuration > 0 ?
                        <span>{TimeUtils.durationHumanizer(uptimeDuration)}</span>
                        : null,
                    uptimeChecks ?
                        <span>{StringUtils.numeralFormEn(uptimeChecks, "check", "checks")}</span>
                        : null
                )
                .filterNot(x => x == null);
            if (uptimeInfo.count() > 0) {
                uptimeInfo = intersperse(uptimeInfo, ", ").splice(0, 0, "uptime: ");
            }
            
            return (
                <div className={`status-info status-info__${status}`}>
                    <div>{StringUtils.dashToHumanReadable(status)}</div>
                    <div className="status-info_more">{uptimeInfo}</div>
                </div>
            );
        }
    });
});
