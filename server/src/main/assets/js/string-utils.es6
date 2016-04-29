/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

define(
function()
{
    const dashToHumanReadableRe = new window.RegExp("(-[a-z0-9A-Z])", "g");

    const dashToHumanReadable = function (string) {
        const replaced = string.replace(
            dashToHumanReadableRe,
            (match, p1) => {
                return ` ${p1.substr(1, 1).toLocaleUpperCase()}`;
            }
        );
        return replaced.substr(0, 1).toLocaleUpperCase() + replaced.substring(1);
    };

    const numeralFormEn = function(number, one, many, includeNumber = true) {
        const form = (number.abs == 0 || number.abs == 1) ? one : many;
        return includeNumber ? (number + " " + form) : form;
    };

    return {
        dashToHumanReadable: dashToHumanReadable,
        numeralFormEn: numeralFormEn
    };
});
