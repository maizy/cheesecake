/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */
define(
[
    "humanize-duration"
],
function(
    humanizeDuration
) {
    const shortEnDurationHumanizer = humanizeDuration.humanizer({
        language: "shortEn",
        languages: {
            shortEn: {
                y: () => "y",
                mo: () => "mo",
                w: () => "w",
                d: () => "d",
                h: () => "h",
                m: () => "m",
                s: () => "s",
                ms: () => "ms"
            }
        }
    });

    const durationHumanizer = (seconds) =>
        shortEnDurationHumanizer(
            seconds * 1000,
            {
                delimiter: "\u202f",
                spacer: ""
            }
        );

    return {
        durationHumanizer: durationHumanizer
    }

});
