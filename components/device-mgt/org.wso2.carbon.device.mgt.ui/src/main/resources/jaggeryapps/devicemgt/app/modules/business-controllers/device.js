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

var deviceModule;
deviceModule = function () {
    var log = new Log("/app/modules/business-controllers/device.js");

    var utility = require('/app/modules/utility.js')["utility"];
    var constants = require('/app/modules/constants.js');
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var serviceInvokers = require("/app/modules/oauth/token-protected-service-invokers.js")["invokers"];
    var batchProvider = require("/app/modules/batch-provider-api.js")["batchProviders"];

    var publicMethods = {};
    var privateMethods = {};

    /**
     * Only GET method is implemented for now since there are no other type of methods used this method.
     * @param url - URL to call the backend without the host
     * @param method - HTTP Method (GET, POST)
     * @returns An object with 'status': 'success'|'error', 'content': {}
     */
    privateMethods.callBackend = function (url, method) {
        if (constants["HTTP_GET"] == method) {
            return serviceInvokers.XMLHttp.get(url,
                function (backendResponse) {
                    var response = {};
                    response.content = backendResponse.responseText;
                    if (backendResponse.status == 200) {
                        response.status = "success";
                    } else if (backendResponse.status == 400 || backendResponse.status == 401 ||
                        backendResponse.status == 404 || backendResponse.status == 500) {
                        response.status = "error";
                    }
                    return response;
                }
            );
        } else {
            log.error("Runtime error : This method only support HTTP GET requests.");
        }
    };

    privateMethods.validateAndReturn = function (value) {
        return (value == undefined || value == null) ? constants["UNSPECIFIED"] : value;
    };

    /*
     @Updated
     */
    publicMethods.viewDevice = function (deviceType, deviceId) {
        var carbonUser = session.get(constants["USER_SESSION_KEY"]);
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants["ERRORS"]["USER_NOT_FOUND"];
        }
        var userName = carbonUser.username + "@" + carbonUser.domain;

        var locationDataSet = [];
        switch (deviceType) {
            case 'android':
                locationDataSet = batchProvider.getData(userName, deviceId, deviceType);
                break;
            case 'android_sense':
                locationDataSet = batchProvider.getData(userName, deviceId, deviceType);
                break;

        }
        var locationData = [];
        var locationTimeData = [];
        if (locationDataSet != null) {

            for (var i = 0; i < locationDataSet.length; i++) {
                var gpsReading = {};
                var gpsReadingTimes = {};
                gpsReading.lat = locationDataSet[i].latitude;
                gpsReading.lng = locationDataSet[i].longitude;
                if (deviceType == "android") {
                    gpsReadingTimes.time = locationDataSet[i].timeStamp;
                } else {
                    gpsReadingTimes.time = locationDataSet[i].meta_timestamp;
                }
                locationData.push(gpsReading);
                locationTimeData.push(gpsReadingTimes);
            }
        }
        var locationInfo = {};
        try {
            var url = devicemgtProps["httpsURL"] + "/api/device-mgt/v1.0/devices/" + deviceType + "/" + deviceId + "/location";
            serviceInvokers.XMLHttp.get(
                url,
                function (backendResponse) {

                    if (backendResponse.status == 200 && backendResponse.responseText) {
                        var device = parse(backendResponse.responseText);
                        locationInfo.latitude = device.latitude;
                        locationInfo.longitude = device.longitude;
                        locationInfo.updatedOn = device.updatedTime;

                    }
                });
        } catch (e) {
            log.error(e.message, e);
        }


        var utility = require('/app/modules/utility.js')["utility"];
        try {
            utility.startTenantFlow(carbonUser);
            var url = devicemgtProps["httpsURL"] + "/api/device-mgt/v1.0/devices/" + deviceType + "/" + deviceId;
            return serviceInvokers.XMLHttp.get(
                url,
                function (backendResponse) {
                    var response = {};
                    if (backendResponse.status == 200 && backendResponse.responseText) {
                        var device = parse(backendResponse.responseText);

                        var filteredDeviceData = {};
                        if (device["deviceIdentifier"]) {
                            filteredDeviceData["deviceIdentifier"] = device["deviceIdentifier"];
                        }
                        if (device["type"]) {
                            filteredDeviceData["type"] = device["type"];
                        }
                        if (device["name"]) {
                            filteredDeviceData["name"] = device["name"];
                        }
                        if (device["enrolmentInfo"]) {
                            var enrolmentInfo = {};
                            if (device["enrolmentInfo"]["status"]) {
                                enrolmentInfo["status"] = device["enrolmentInfo"]["status"];
                            }
                            if (device["enrolmentInfo"]["owner"]) {
                                enrolmentInfo["owner"] = device["enrolmentInfo"]["owner"];
                            }
                            if (device["enrolmentInfo"]["ownership"]) {
                                enrolmentInfo["ownership"] = device["enrolmentInfo"]["ownership"];
                            }
                            filteredDeviceData["enrolmentInfo"] = enrolmentInfo;
                        }
                        if (device["properties"] && device["properties"].length > 0) {
                            var propertiesList = device["properties"];
                            var properties = {};
                            if (propertiesList) {
                                for (var i = 0; i < propertiesList.length; i++) {
                                    if (propertiesList[i]["value"]) {
                                        properties[propertiesList[i]["name"]] =
                                            propertiesList[i]["value"];
                                    }
                                }
                            }

                            filteredDeviceData["initialDeviceInfo"] = properties;

                            if (properties["DEVICE_INFO"]) {
                                var initialDeviceInfoList = parse(properties["DEVICE_INFO"]);
                                var initialDeviceInfo = {};
                                if (Array.isArray(initialDeviceInfoList)) {
                                    for (var j = 0; j < initialDeviceInfoList.length; j++) {
                                        if (initialDeviceInfoList[j]["value"]) {
                                            initialDeviceInfo[initialDeviceInfoList[j]["name"]] =
                                                initialDeviceInfoList[j]["value"];
                                        }
                                    }
                                } else {
                                    initialDeviceInfo = initialDeviceInfoList;
                                }


                                filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"] = initialDeviceInfo;
                            }
                        }

                        if (filteredDeviceData["type"]) {
                            if (filteredDeviceData["type"] == constants["PLATFORM_IOS"]) {
                                if (filteredDeviceData["properties"]) {
                                    filteredDeviceData["properties"]["VENDOR"] = "Apple";
                                }
                            }
                        }
                        if (device["deviceInfo"]) {
                            filteredDeviceData["latestDeviceInfo"] = device["deviceInfo"];

                            //location related verification and modifications
                            // adding the location histry for the movement path.
                            var locationHistory = {};
                            locationHistory.locations = locationData;
                            locationHistory.times = locationTimeData;
                            filteredDeviceData["locationHistory"] = locationHistory;

                            //checking for the latest location information.
                            if (filteredDeviceData.latestDeviceInfo.location && locationInfo) {
                                var infoDate = new Date(filteredDeviceData.latestDeviceInfo.location.updatedTime);
                                var locationDate = new Date(locationInfo.updatedOn);
                                if (infoDate < locationDate) {
                                    filteredDeviceData.latestDeviceInfo.location.longitude = locationInfo.longitude;
                                    filteredDeviceData.latestDeviceInfo.location.latitude = locationInfo.latitude;
                                }
                            }
                        }


                        response["content"] = filteredDeviceData;
                        response["status"] = "success";
                        return response;
                    } else if (backendResponse.status == 401) {
                        response["status"] = "unauthorized";
                        return response;
                    } else if (backendResponse.status == 404) {
                        response["status"] = "notFound";
                        return response;
                    } else {
                        response["status"] = "error";
                        return response;
                    }
                }
            );
        } catch (e) {
            throw e;
        } finally {
            utility.endTenantFlow();
        }
    };

    // Refactored methods
    publicMethods.getDevicesCount = function () {
        var carbonUser = session.get(constants.USER_SESSION_KEY);
        if (carbonUser) {
            var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
            var uiPermissions = userModule.getUIPermissions();
            var url;
            if (uiPermissions.LIST_DEVICES) {
                url = devicemgtProps["httpsURL"] +
                    devicemgtProps["backendRestEndpoints"]["deviceMgt"] + "/devices?offset=0&limit=1";
            } else if (uiPermissions.LIST_OWN_DEVICES) {
                url = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"] +
                    "/devices?offset=0&limit=1&user=" + carbonUser.username;
            } else {
                log.error("Access denied for user: " + carbonUser.username);
                return -1;
            }
            return serviceInvokers.XMLHttp.get(
                url, function (responsePayload) {
                    return parse(responsePayload["responseText"])["count"];
                },
                function (responsePayload) {
                    log.error(responsePayload["responseText"]);
                    return -1;
                }
            );
        } else {
            log.error("User object was not found in the session");
            throw constants["ERRORS"]["USER_NOT_FOUND"];
        }
    };

    publicMethods.getDeviceTypes = function () {
        var url = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"] + "/device-types";
        var response = privateMethods.callBackend(url, constants["HTTP_GET"]);
        if (response.status == "success") {
            response.content = parse(response.content);
        }
        return response;
    };

    /*
     @Updated
     */
    // publicMethods.getLicense = function (deviceType) {
    //     var url;
    //     var license;
    //     if (deviceType == "windows") {
    //         url = mdmProps["httpURL"] + "/mdm-windows-agent/services/device/license";
    //     } else if (deviceType == "ios") {
    //         url = mdmProps["httpsURL"] + "/ios-enrollment/license/";
    //     }

    //     if (url != null && url != undefined) {
    //         serviceInvokers.XMLHttp.get(url, function (responsePayload) {
    //             license = responsePayload.text;
    //         }, function (responsePayload) {
    //             return null;
    //         });
    //     }
    //     return license;
    // };

    publicMethods.getDevices = function (userName) {
        var url = devicemgtProps["httpsURL"] +
            devicemgtProps["backendRestEndpoints"]["deviceMgt"] + "/devices";
        return serviceInvokers.XMLHttp.get(
            url, function (responsePayload) {
                var devices = JSON.parse(responsePayload.responseText).devices;
                for (var i = 0; i < devices.length; i++) {
                    devices[i].thumb = utility.getDeviceThumb(devices[i].type);
                }
                return devices;
            },
            function (responsePayload) {
                log.error(responsePayload);
                return -1;
            }
        );
    };
    return publicMethods;
}();
