/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

define(
    function () {
        return {
            componentWillMount: function () {
                this.intervalsIds = [];
            },
            addInterval: function () {
                this.intervalsIds.push(window.setInterval.apply(null, arguments));
            },
            componentWillUnmount: function () {
                this.intervalsIds.forEach(window.clearInterval);
            }
        };
    });
