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

    publicMethods.getControlOperations = function (deviceType) {
        switch (deviceType) {
            case "virtual_firealarm":
                return [{name: "Alarm Status", description: "0:off 1:on", operation: "bulb"}];
            case "digital_display":
                return [
                    {name: "Restart Browser",operation: "restart-browser",
                                params : ["deviceId","owner","sessionId"]},
                    {name: "Close Browser", operation: "close-browser",
                                params : ["deviceId","owner","sessionId"]},
                    {name: "Terminate Display", operation: "terminate-display",
                                params : ["deviceId","owner","sessionId"]},
                    {name: "Restart Display", operation: "restart-display",
                                params : ["deviceId","owner","sessionId"]},
                    {name: "Shutdown Display", operation: "shutdown-display",
                                params : ["deviceId","owner","sessionId"]},
                    {name: "Edit Content", operation: "edit-content",
                                params : ["deviceId","owner","sessionId","path","attribute","new-value"], editcontent : "true"},
                    {name: "Add New Resource", operation: "add-resource",
                                params : ["deviceId","owner","sessionId","type","time","path"], add : "true"},
                    {name: "Add New Resource Before", operation: "add-resource-before",
                                params : ["deviceId","owner","sessionId","type","time","path","next-page"], add : "true" , before : "true"},
                    {name: "Add New Resource After", operation: "add-resource-next",
                                params : ["deviceId","owner","sessionId","type","time","path","before-page"], add : "true" , after : "true"},
                    {name: "Remove Resource", operation: "remove-resource",
                                params : ["deviceId","owner","sessionId","path"], removeresource : "true"},
                    {name: "Remove Directory", operation: "remove-directory",
                                params : ["deviceId","owner","sessionId","directory-name"], remove : "true"},
                    {name: "Remove Content", operation: "remove-content",
                                params : ["deviceId","owner","sessionId","directory-name","content"], remove : "true" , content : "true"},

                 ];

            default:
                return [];
        }
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
        var header = '{"owner":"' + user + '","deviceId":"' + deviceId + '","protocol":"mqtt"}';
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