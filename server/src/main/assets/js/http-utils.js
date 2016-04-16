/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

(function(global) {
    HttpUtils = {};

    var Error = function (message, additional) {
        this.success = false;
        this.message = message;
        this.additional = additional || {};
    };
    HttpUtils.Error = Error;

    var Success = function (data, additional) {
        this.success = true;
        this.data = data;
        this.additional = additional || {};
    };
    HttpUtils.Success = Success;

    HttpUtils.getJson = function (basePath, relPath, cb, debug_cb) {
        debug_cb = debug_cb || function() {};
        var xhr = new global.XMLHttpRequest();
        var url = basePath + relPath;
        debug_cb("> GET " + url);
        xhr.open("GET", url, true);
        xhr.send();
        xhr.onreadystatechange = function () {
            var res;

            if (xhr.readyState != global.XMLHttpRequest.DONE) return;

            if (xhr.status != 200) {
                cb(new Error(
                    "Bad response " + xhr.status,
                    {status: xhr.status, body: xhr.statusText}
                ));
            } else {

                try {
                    res = new Success(global.JSON.parse(xhr.responseText));
                } catch (e) {
                    res = new Error("Unable to parse json from " + url, {error: e});
                }
                cb(res)
            }
        };
        return cb;
    };
    global.cheesecake = global.cheesecake || {};
    global.cheesecake.HttpUtils = HttpUtils;
})(window);
