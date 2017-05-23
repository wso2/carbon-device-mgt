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
	var log = new Log("device-view.js");
	var deviceType = context.uriParams.deviceType;
	var deviceId = request.getParameter("id");
	var attributes = [];
	var featureList = [];
	log.error(featureList);
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
						attributes.push(attribute['name']);
					}
				}

			}
		}
	);

	var featureEndpoint = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"]
		+ "/device-types/" + deviceType + "/features";
	serviceInvokers.XMLHttp.get(featureEndpoint, function (responsePayload) {
			var features = JSON.parse(responsePayload.responseText);
			new Log().error(responsePayload.responseText);
			var feature;
			for (var i = 0; i < features.length; i++) {
				feature = {};
				feature["operation"] = features[i].code;
				feature["name"] = features[i].name;
				feature["description"] = features[i].description;
				featureList.push(feature);
			}

		}, function (responsePayload) {
			featureList = null;
		}
	);
	var autoCompleteParams = [
		{"name" : "deviceId", "value" : deviceId}
	];

	if (deviceType != null && deviceType != undefined && deviceId != null && deviceId != undefined) {
		var deviceModule = require("/app/modules/business-controllers/device.js")["deviceModule"];
		var device = deviceModule.viewDevice(deviceType, deviceId);
		if (device && device.status != "error") {
			if (attributes.length === 0 || attributes.length === undefined) {
				return {"device": device.content, "autoCompleteParams" : autoCompleteParams
					, "encodedFeaturePayloads": "", "features":featureList};
			} else {
				return {"device": device.content, "autoCompleteParams" : autoCompleteParams
					, "encodedFeaturePayloads": "", "attributes": attributes, "features":featureList};
			}
		} else {
			response.sendError(404, "Device Id " + deviceId + " of type " + deviceType + " cannot be found!");
			exit();
		}
	}
}