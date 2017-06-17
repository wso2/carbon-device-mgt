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
var serviceInvokers = require("/app/modules/oauth/token-protected-service-invokers.js")["invokers"];
var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];

function onRequest(context) {
    var log = new Log("stats.js");
    var carbonServer = require("carbon").server;
    var device = context.unit.params.device;
	var attributes = context.unit.params.attributes;
	var events = [];
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
	var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var constants = require("/app/modules/constants.js");
    var websocketEndpoint = devicemgtProps["wssURL"].replace("https", "wss");
    var jwtService = carbonServer.osgiService(
        'org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService');
    var jwtClient = jwtService.getJWTClient();
    var encodedClientKeys = session.get(constants["ENCODED_TENANT_BASED_WEB_SOCKET_CLIENT_CREDENTIALS"]);
    var token = "";
	var user = userModule.getCarbonUser();
	var tenantDomain = user.domain;
    if (encodedClientKeys) {
        var tokenUtil = require("/app/modules/oauth/token-handler-utils.js")["utils"];
        var resp = tokenUtil.decode(encodedClientKeys).split(":");
		if (tenantDomain == "carbon.super") {
			var tokenPair = jwtClient.getAccessToken(resp[0], resp[1], context.user.username,"default", {});
			if (tokenPair) {
				token = tokenPair.accessToken;
			}
			websocketEndpoint = websocketEndpoint + "/secured-websocket/iot.per.device.stream." + tenantDomain + "." + device.type + "/1.0.0?"
				+ "deviceId=" + device.deviceIdentifier + "&deviceType=" + device.type + "&websocketToken=" + token;
		} else {
			var tokenPair = jwtClient.getAccessToken(resp[0], resp[1], context.user.username + "@" + tenantDomain,"default", {});
			if (tokenPair) {
				token = tokenPair.accessToken;
			}
			websocketEndpoint = websocketEndpoint + "/secured-websocket" + "/t/" + tenantDomain + "/iot.per.device.stream." + tenantDomain
				+ "." + device.type + "/1.0.0?" + "deviceId=" + device.deviceIdentifier + "&deviceType="
				+ device.type + "&websocketToken=" + token;
		}

    }
	var events = [];
	var viewModel = {};
	viewModel.device = device;
	viewModel.websocketEndpoint = websocketEndpoint;

	var restAPIEndpoint = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"]
		+ "/events/last-known/" + device.type + "/" + device.deviceIdentifier;
	serviceInvokers.XMLHttp.get(
		restAPIEndpoint,
		function (restAPIResponse) {
			if (restAPIResponse["status"] == 200 && restAPIResponse["responseText"]) {
				var responsePayload = parse(restAPIResponse["responseText"]);
				var records = responsePayload["records"];
				if (records && records[0] && records[0].values) {
					var record = records[0].values;
					viewModel.timestamp = new Date(records[0].timestamp);
					for (var eventAttribute in attributes){
						var event = {};
						event.key = attributes[eventAttribute];
						event.value = record["" + attributes[eventAttribute]];
						events.push(event);
					}
				} else {
					for (var eventAttribute in attributes){
						var event = {};
						event.key = attributes[eventAttribute];
						event.value = "-";
						events.push(event);
					}
				}

			}
		}
	);
	viewModel.attributes = attributes;
	viewModel.events = events;
    return viewModel;
}
