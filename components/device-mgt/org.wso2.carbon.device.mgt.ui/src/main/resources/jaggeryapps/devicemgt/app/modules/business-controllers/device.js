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
        var utility = require('/app/modules/utility.js')["utility"];
        try {
            utility.startTenantFlow(carbonUser);
            var url = devicemgtProps["httpsURL"] + "/api/device-mgt/v1.0/devices/" + deviceType + "/" + deviceId;
            return serviceInvokers.XMLHttp.get(
                url,
                function (backendResponse) {
                    var response = {};
                    if (backendResponse.status == 200 && backendResponse.responseText) {
                        response["status"] = "success";
                        var device = parse(backendResponse.responseText);
                        var propertiesList = device["properties"];
                        var properties = {};
                        for (var i = 0; i < propertiesList.length; i++) {
                            properties[propertiesList[i]["name"]] =
                                propertiesList[i]["value"];
                        }
                        var deviceObject = {};
                        deviceObject[constants["DEVICE_IDENTIFIER"]] = device["deviceIdentifier"];
                        deviceObject[constants["DEVICE_NAME"]] = device["name"];
                        deviceObject[constants["DEVICE_OWNERSHIP"]] = device["enrolmentInfo"]["ownership"];
                        deviceObject[constants["DEVICE_OWNER"]] = device["enrolmentInfo"]["owner"];
                        deviceObject[constants["DEVICE_STATUS"]] = device["enrolmentInfo"]["status"];
                        deviceObject[constants["DEVICE_TYPE"]] = device["type"];
                        if (device["type"] == constants["PLATFORM_IOS"]) {
                            properties[constants["DEVICE_MODEL"]] = properties[constants["DEVICE_PRODUCT"]];
                            delete properties[constants["DEVICE_PRODUCT"]];
                            properties[constants["DEVICE_VENDOR"]] = constants["VENDOR_APPLE"];
                        }
                        deviceObject[constants["DEVICE_PROPERTIES"]] = properties;
                        deviceObject[constants["DEVICE_INFO"]] = device["deviceInfo"];
                        response["content"] = deviceObject;
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
            devicemgtProps["backendRestEndpoints"]["deviceMgt"] + "/devices/user/" + userName;
        return serviceInvokers.XMLHttp.get(
            url, function (responsePayload) {
                for (var i = 0; i < responsePayload.length; i++) {
                    responsePayload[i].thumb = utility.getDeviceThumb(responsePayload[i].type);
                }
                return responsePayload;
            },
            function (responsePayload) {
                log.error(responsePayload);
                return -1;
            }
        );
    };
    return publicMethods;
}();
