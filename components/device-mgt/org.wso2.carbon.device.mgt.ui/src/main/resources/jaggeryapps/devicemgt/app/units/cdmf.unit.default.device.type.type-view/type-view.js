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
	var displayData = {};
	var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
	var user = userModule.getCarbonUser();
	var tenantDomain = user.domain;
	var deviceMgtProps = require("/app/modules/conf-reader/main.js")["conf"];
	var serviceInvokers = require("/app/modules/oauth/token-protected-service-invokers.js")["invokers"];
	var process = require("process");
	context.handlebars.registerHelper('if_eq', function(a, b, opts) {
		if(a == b) // Or === depending on your needs
			return opts.fn(this);
		else
			return opts.inverse(this);
	});
	var restAPIEndpoint = deviceMgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"]
		+ "/device-types/config/" + deviceType;
	displayData.deviceType = deviceType;
	displayData.tenantDomain = tenantDomain;
	serviceInvokers.XMLHttp.get(
		restAPIEndpoint,
		function (restAPIResponse) {
			if (restAPIResponse["status"] == 200 && restAPIResponse["responseText"]) {
				var typeData = parse(restAPIResponse["responseText"]);
				displayData.type = typeData;

			}
		}
	);

	var eventRestAPIEndpoint = deviceMgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"]
		+ "/events/" + deviceType;
	serviceInvokers.XMLHttp.get(
		eventRestAPIEndpoint,
		function (restAPIResponse) {
			if (restAPIResponse["status"] == 200 && restAPIResponse["responseText"]) {
				var typeData = parse(restAPIResponse["responseText"]);
				displayData.event = typeData;
				var sampleValue = "";
				if (typeData.eventAttributes && typeData.eventAttributes.attributes) {
					var eventExample = {};
					for (var i = 0; i < typeData.eventAttributes.attributes.length; i++) {
						var attribute = typeData.eventAttributes.attributes[i];
						switch (attribute.type) {
							case "STRING":
								eventExample[attribute.name] = "string";
								sampleValue = sampleValue + "\"string\", ";
								break;
							case "LONG":
								eventExample[attribute.name] = 0;
								sampleValue = sampleValue + 0 +", ";
								break;
							case "INT":
								eventExample[attribute.name] = 0;
								sampleValue = sampleValue + 0 +", ";
								break;
							case "FLOAT":
								eventExample[attribute.name] = 0.0;
								sampleValue = sampleValue + 0.0 +", ";
								break;
							case "DOUBLE":
								eventExample[attribute.name] = 0.0;
								sampleValue = sampleValue + 0.0 +", ";
								break;
							case "BOOL":
								eventExample[attribute.name] = false;
								sampleValue = sampleValue + false + ", ";
								break;

						}

					}
					var sample = eventExample;
					if (sampleValue && sampleValue.length > 2) {
						displayData.sampleValue = sampleValue.substring(0, sampleValue.length - 2);
					}
					displayData.eventSample = JSON.stringify(sample);
					displayData.mqttGateway = "tcp://" + process.getProperty("mqtt.broker.host") + ":" + process.getProperty("mqtt.broker.port");
					displayData.httpsGateway = "https://" + process.getProperty("iot.gateway.host") + ":" + process.getProperty("iot.gateway.https.port");

				}
			}
		}
	);

    return displayData;
}
