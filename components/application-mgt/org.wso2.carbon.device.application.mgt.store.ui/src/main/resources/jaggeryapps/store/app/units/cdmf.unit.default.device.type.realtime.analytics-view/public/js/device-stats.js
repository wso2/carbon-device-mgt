/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var ws;
var attributes = null;
$(window).load(function () {
	var div = $("#div-chart");
    var websocketUrl = div.data("websocketurl");
	attributes = div.data("attributes").split(",");
    connect(websocketUrl)
});

$(window).unload(function () {
    disconnect();
});

//websocket connection
function connect(target) {
    if ('WebSocket' in window) {
        ws = new WebSocket(target);
    } else if ('MozWebSocket' in window) {
        ws = new MozWebSocket(target);
    } else {
        console.log('WebSocket is not supported by this browser.');
    }
    if (ws) {
        ws.onmessage = function (webSocketData) {
            var data = JSON.parse(webSocketData.data);
			console.log(data);
			var payloadData = data["event"]["payloadData"];
			for (var i = 0; i < attributes.length; i++){
				$("#" + attributes[i] +"-value").text(payloadData[attributes[i]]);
			}
			$("#time-mode").text("Real Time Mode");
        };
    }
}

function disconnect() {
    if (ws != null) {
        ws.close();
        ws = null;
    }
}
