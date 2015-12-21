/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    var log = new Log("/app/modules/operation.js");
    var utility = require('/app/modules/utility.js').utility;
    var constants = require('/app/modules/constants.js');
    var devicemgtProps = require('/app/conf/devicemgt-props.js').config();

    var user = session.get(constants.USER_SESSION_KEY);

    var publicMethods = {};
    var privateMethods = {};

    privateMethods.getOperationsFromFeatures = function (deviceType) {
        var GenericFeatureManager = Packages.org.wso2.carbon.apimgt.webapp.publisher.feature.management.GenericFeatureManager;
        try {
            var featureManager = GenericFeatureManager.getInstance();
            var features = featureManager.getFeatures(deviceType);
            var featureList = [];
            var feature;
            for (var i = 0; i < features.size(); i++) {
                feature = {};
                feature["id"] = features.get(i).getId();
                feature["code"] = features.get(i).getCode();
                feature["name"] = features.get(i).getName();
                feature["description"] = features.get(i).getDescription();
                feature["deviceType"] = features.get(i).getDeviceType();
                feature["metadataEntries"] = [];
                var metaData = features.get(i).getMetadataEntries();
                if (metaData && metaData != null) {
                    var metaDataEntry;
                    for (var j = 0; j < metaData.size(); j++) {
                        metaDataEntry = {};
                        metaDataEntry["id"] = metaData.get(j).getId();
                        metaDataEntry["value"] = metaData.get(j).getValue();
                        ["metadataEntries"].push(metaDataEntry);
                    }
                }
                featureList.push(feature);
            }
            log.info(featureList);
        } catch (e) {
            log.error(e);
            throw e;
        }
    };

    publicMethods.getControlOperations = function (deviceType) {
        privateMethods.getOperationsFromFeatures(deviceType);
        var operations = [];
        switch (deviceType) {
            case "virtual_firealarm":
                operations = [{
                    name: "Alarm Status", operation: "bulb",
                    params: ["state"]
                }];
                break;
            case "digital_display":
                operations = [
                    {
                        name: "Restart Browser", operation: "restart-browser",
                        params: []
                    },
                    {
                        name: "Close Browser", operation: "close-browser",
                        params: []
                    },
                    {
                        name: "Terminate Display", operation: "terminate-display",
                        params: []
                    },
                    {
                        name: "Restart Display", operation: "restart-display",
                        params: []
                    },
                    {
                        name: "Shutdown Display", operation: "shutdown-display",
                        params: []
                    },
                    {
                        name: "Edit Content",
                        operation: "edit-content",
                        params: ["path", "attribute", "new-value"]
                    },
                    {
                        name: "Add New Resource",
                        operation: "add-resource",
                        params: ["type", "time", "path"]
                    },
                    {
                        name: "Add New Resource Before",
                        operation: "add-resource-before",
                        params: ["type", "time", "path", "next-page"]
                    },
                    {
                        name: "Add New Resource After",
                        operation: "add-resource-next",
                        params: ["type", "time", "path", "before-page"]
                    },
                    {
                        name: "Remove Resource", operation: "remove-resource",
                        params: ["path"], removeresource: "true"
                    },
                    {
                        name: "Remove Directory", operation: "remove-directory",
                        params: ["directory-name"], remove: "true"
                    },
                    {
                        name: "Remove Content",
                        operation: "remove-content",
                        params: ["directory-name", "content"]
                    }

                ];
                break;
            default:
                operations = [];
        }
        for (var op in operations){
            var iconPath = utility.getOperationIcon(deviceType, operations[op].operation);
            if (iconPath){
                operations[op]["icon"] = iconPath;
            }
        }
        return operations;
    };

    publicMethods.getMonitorOperations = function (deviceType) {
        switch (deviceType) {
            case "virtual_firealarm":
                return [{name: "Temperature", operation: "readtemperature"}];
            case "android_sense":
                return [
                    {name: "Battery", operation: "readbattery"},
                    {name: "gps", operation: "readgps"},
                    {name: "Gyroscope", operation: "readgyroscope"},
                    {name: "Accelerometer", operation: "readaccelerometer"},
                    {name: "Gravity", operation: "readgravity"},
                    {name: "Rotation", operation: "readrotation"},
                    {name: "Pressure", operation: "readpressure"},
                    {name: "Proximity", operation: "readproximity"},
                    {name: "Light", operation: "readlight"},
                    {name: "Magnetic", operation: "readmagnetic"}
                ];
            default:
                return [];
        }
    };

    publicMethods.handlePOSTOperation = function (deviceType, operation, deviceId, params) {
        var endPoint = devicemgtProps["httpsURL"] + '/' + deviceType + "/controller/" + operation;
        var header = '{"owner":"' + user + '","deviceId":"' + deviceId + '","protocol":"mqtt", "sessionId":"' + session.getId() + '"}';
        log.info(params);
        return post(endPoint, params, JSON.parse(header), "json");
    };

    publicMethods.handleGETOperation = function (deviceType, operation, operationName, deviceId) {
        var endPoint = devicemgtProps["httpsURL"] + '/' + deviceType + "/controller/" + operation;
        var header = '{"owner":"' + user + '","deviceId":"' + deviceId + '","protocol":"mqtt"}';
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
                var sqRootValue = Math.sqrt(sqSum);
                result.data[operationName] = sqRootValue;
            }
            delete result.data['sensorValue'];
        }
        log.info(result);
        return result;
    };

    return publicMethods;
}();