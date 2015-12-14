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
    var log = new Log("/modules/operation.js");
    var utility = require('/modules/utility.js').utility;
    var constants = require('/modules/constants.js');
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
                    {name: "Restart Browser", description: "0:faild 1:sucess", operation: "bulb"},
                    {name: "Close Browser", description: "0:faild 1:sucess"},
                    {name: "Terminate Display", description: "0:faild 1:sucess"},
                    {name: "Restart Display", description: "0:faild 1:sucess"},
                    {name: "Edit Content", description: "0:faild 1:sucess"},
                    {name: "Add New Resource", description: "0:faild 1:sucess"},
                    {name: "Remove Resource", description: "0:faild 1:sucess"},
                    {name: "Remove Directory", description: "0:faild 1:sucess"},
                    {name: "Remove Content", description: "0:faild 1:sucess"},
                    {name: "Shutdown Display", description: "0:faild 1:sucess"}
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
                    {name: "Light", operation: "readlight"},
                    {name: "Magnetic", operation: "readmagnetic"}
                ];
            default:
                return [];
        }
    };

    publicMethods.handlePOSTOperation = function (deviceType, operation, deviceId, value) {
        var endPoint = devicemgtProps["httpsURL"] + '/' + deviceType + "/controller/" + operation + "/" + ((value == 1) ? "ON" : "OFF");
        var header = '{"owner":"' + user + '","deviceId":"' + deviceId + '","protocol":"mqtt"}';
        return post(endPoint, {}, JSON.parse(header), "json");
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
