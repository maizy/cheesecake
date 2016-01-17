(function(global) {

    // Currently I don't need any js fw, KISS

    var HttpUtil = global.cheesecake.HttpUtils;

    var STATES = {
        inited: "inited",
        ready: "ready",
        initError: "initError",
        connected: "connected",
        connectError: "connectError"
    };

    var ResultsComponent = function (opts) {
        this._opts = opts;
        this._state = STATES.inited;
        this._config = null;

        this._stateWs = undefined; // For debuging purposes only

        this._loadConfigs();
    };

    ResultsComponent.prototype._loadConfigs = function() {
        this.displayText("Load opts");
        HttpUtil.getJson(this._opts.apiBase, "/configs",
            function(res) {
                if (res.success) {
                    this._config = res.data;
                    this._state = STATES.ready;
                    this._initWs();
                } else {
                    this.displayText("Config load error " + res.message);
                    this._state = STATES.initError;
                }
            }.bind(this),
            this.displayText.bind(this)
        );
    };

    ResultsComponent.prototype._initWs = function() {
        var ws = new global.WebSocket(this._config.wsStateUrl);

        this._stateWs = ws;

        ws.onopen = function() {
            this.displayText("WebSocket connection established");
            this._state = STATES.connected;
        }.bind(this);

        ws.onerror = function(error) {
          this.displayText("WebSocket error: " + error.message);
            this._state = STATES.connectError;
        }.bind(this);

        ws.onclose = function(event) {
            if (event.wasClean) {
                this.displayText("WebSocket connection closed")
            } else {
                this.displayText("WebSocket connection broken")
            }
            this.displayText("Close code: " + event.code + ", reason: " + event.reason);
            this._state = STATES.ready;
        }.bind(this);

        ws.onmessage = function(event) {
          this.displayText("=(ws)=> " + event.data);
        }.bind(this);

    };

    ResultsComponent.prototype.displayText = function(text) {
        var container = this._opts.container;
        var p = global.document.createElement("p");
        p.textContent = text;
        container.insertBefore(p, container.firstChild)
    };

    global.cheesecake = global.cheesecake || {};
    cheesecake.ResultsComponent = ResultsComponent;
    global.cheesecake = cheesecake;

})(window);
