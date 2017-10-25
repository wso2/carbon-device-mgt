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

function onRequest(context) {

    var deviceType = context.uriParams.deviceType;
    var deviceId = request.getParameter("deviceId");
	var keys = [];
	var serviceInvokers = require("/app/modules/oauth/token-protected-service-invokers.js")["invokers"];
	var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];

	if (deviceType != null && deviceType != undefined && deviceId != null && deviceId != undefined) {
        var deviceModule = require("/app/modules/business-controllers/device.js")["deviceModule"];
        var device = deviceModule.viewDevice(deviceType, deviceId);

		var restAPIEndpoint = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"]
			+ "/events/" + deviceType;
		serviceInvokers.XMLHttp.get(
			restAPIEndpoint,
			function (restAPIResponse) {
				if (restAPIResponse["status"] == 200 && restAPIResponse["responseText"]) {
					var data = parse(restAPIResponse["responseText"]);
					if (data.eventAttributes.attributes.length > 0) {
						for (var i = 0; i < data.eventAttributes.attributes.length; i++) {
							var attribute = data.eventAttributes.attributes[i];
							if (attribute['name'] == "deviceId") {
								continue;
							}
							keys.push(attribute['name']);
						}
					}

				}
			}
		);

        if (device && device.status != "error") {
			if (keys.length === 0 || keys.length === undefined) {
				return {
					"device": device.content,
					"backendApiUri": "/api/device-mgt/v1.0/events/" + deviceType
				};
			} else {

				return {
					"device": device.content,
					"backendApiUri": "/api/device-mgt/v1.0/events/" + deviceType,
					"attributes": keys
				};
			}
        } else {
            response.sendError(404, "Device Id " + deviceId + " of type " + deviceType + " cannot be found!");
            exit();
        }
    }
}