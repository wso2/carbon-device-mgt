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

var operationModule = function () {
    var log = new Log("/app/modules/business-controllers/operation.js");
    var utility = require('/app/modules/utility.js').utility;
    var constants = require('/app/modules/constants.js');
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var serviceInvokers = require("/app/modules/oauth/token-protected-service-invokers.js")["invokers"];

    var publicMethods = {};
    var privateMethods = {};

    /**
     * This method reads the token from the Token client and return the access token.
     * If the token pair s not set in the session this will send a redirect to the login page.
     */
    function getAccessToken(deviceType, owner, deviceId) {
        var TokenClient = Packages.org.wso2.carbon.device.mgt.iot.apimgt.TokenClient;
        var accessTokenClient = new TokenClient(deviceType);
        var accessTokenInfo = accessTokenClient.getAccessToken(owner, deviceId);
        return accessTokenInfo.getAccess_token();
    }

    privateMethods.getOperationsFromFeatures = function (deviceType, operationType) {
        var url = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"] + "/device-types/" + deviceType + "/features";
        var featuresList = serviceInvokers.XMLHttp.get(url, function (responsePayload) {
               var features = JSON.parse(responsePayload.responseText);
               var featureList = [];
               var feature;
               for (var i = 0; i < features.length; i++) {
                   feature = {};
                   feature["operation"] = features[i].code;
                   feature["name"] = features[i].name;
                   feature["description"] = features[i].description;
                   feature["contentType"] = features[i].contentType;
                   feature["deviceType"] = deviceType;
                   feature["params"] = [];
				   var featuresEntry = utility.getDeviceTypeConfig(deviceType)["deviceType"]["features"];
				   if (featuresEntry) {
					   var featureEntry = featuresEntry[features[i].code];
					   if (featureEntry) {
						   var permissionEntry = featureEntry["permission"];
						   if (permissionEntry) {
							   feature["permission"] = permissionEntry
						   }
					   }
				   }
                   var metaData = features[i].metadataEntries;
                   if (metaData) {
                       for (var j = 0; j < metaData.length; j++) {
                           feature["params"].push(metaData[j].value);
                       }
                       featureList.push(feature);
                   }
               }
               return featureList;
           }, function (responsePayload) {
               var response = {};
               response["status"] = "error";
               return response;
           }
        );
        return featuresList;
    };

    publicMethods.getControlOperations = function (deviceType) {
        var operations = privateMethods.getOperationsFromFeatures(deviceType, "operation");
        for (var op in operations) {
            var iconIdentifier = operations[op].operation;
            var icon = utility.getOperationIcon(deviceType, iconIdentifier);
            if (icon) {
                operations[op]["icon"] = icon;
            }
        }
        return operations;
    };

    publicMethods.getMonitorOperations = function (deviceType) {
        return privateMethods.getOperationsFromFeatures(deviceType, "monitor");
    };

    publicMethods.handlePOSTOperation = function (deviceType, operation, deviceId, params) {
        var user = session.get(constants.USER_SESSION_KEY);
        var endPoint = devicemgtProps["httpsURL"] + '/' + deviceType + "/controller/" + operation;
        var header = '{"owner":"' + user.username + '","deviceId":"' + deviceId +
                     '","protocol":"mqtt", "sessionId":"' + session.getId() + '", "' +
                     constants.AUTHORIZATION_HEADER + '":"' + constants.BEARER_PREFIX +
                     getAccessToken(deviceType, user.username, deviceId) + '"}';
        return post(endPoint, params, JSON.parse(header), "json");
    };

    publicMethods.handleGETOperation = function (deviceType, operation, operationName, deviceId) {
        var user = session.get(constants.USER_SESSION_KEY);
        var endPoint = devicemgtProps["httpsURL"] + '/' + deviceType + "/controller/" + operation;
        var header = '{"owner":"' + user.username + '","deviceId":"' + deviceId +
                     '","protocol":"mqtt", "' + constants.AUTHORIZATION_HEADER + '":"' +
                     constants.BEARER_PREFIX + getAccessToken(deviceType, user.username, deviceId) +
                     '"}';
        var result = get(endPoint, {}, JSON.parse(header), "json");
        if (result.data) {
            var values = result.data.sensorValue.split(',');
            if (operationName == 'gps') {
                result.data.map = {
                    lat: parseFloat(values[0]),
                    lng: parseFloat(values[1])
                }
            } else {
                var sqSum = 0;
                for (var v in values) {
                    sqSum += Math.pow(values[v], 2);
                }
                result.data[operationName] = Math.sqrt(sqSum);
            }
            delete result.data['sensorValue'];
        }
        return result;
    };

    return publicMethods;
}();