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

var groupModule = {};
(function (groupModule) {
    var log = new Log("/app/modules/business-controllers/group.js");

    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var constants = require('/app/modules/constants.js');
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var utility = require("/app/modules/utility.js").utility;
    var serviceInvokers = require("/app/modules/oauth/token-protected-service-invokers.js")["invokers"];

    var deviceServiceEndpoint = devicemgtProps["httpsURL"] + "/api/device-mgt/v1.0";

    var user = session.get(constants.USER_SESSION_KEY);

    var endPoint;

    groupModule.getGroupCount = function () {
        var permissions = userModule.getUIPermissions();
        if (permissions.LIST_ALL_GROUPS) {
            endPoint = deviceServiceEndpoint + "/admin/groups/count";
        } else if (permissions.LIST_GROUPS) {
            endPoint = deviceServiceEndpoint + "/groups/count";
        } else {
            if (!user) {
                log.error("User object was not found in the session");
                throw constants["ERRORS"]["USER_NOT_FOUND"];
            }
            log.error("Access denied for user: " + user.username);
            return -1;
        }
        return serviceInvokers.XMLHttp.get(
                endPoint, function (responsePayload) {
                    return responsePayload["responseText"];
                },
                function (responsePayload) {
                    log.error(responsePayload["responseText"]);
                    return -1;
                }
        );
    };

    groupModule.getGroupDeviceCount = function (groupId) {
        endPoint = deviceServiceEndpoint + "/groups/id/" + groupId + "/devices/count";
        return serviceInvokers.XMLHttp.get(
                endPoint, function (responsePayload) {
                    return responsePayload["responseText"];
                },
                function (responsePayload) {
                    log.error(responsePayload);
                    return -1;
                }
        );
    };

    groupModule.getGroupDevices = function (groupId) {
        endPoint = deviceServiceEndpoint + "/groups/id/" + groupId + "/devices?limit=10";
        return serviceInvokers.XMLHttp.get(
                endPoint, function (responsePayload) {
                    return responsePayload;
                },
                function (responsePayload) {
                    log.error(responsePayload);
                    return responsePayload;
                }
        );
    };

    groupModule.getGroups = function () {
        var permissions = userModule.getUIPermissions();
        if (permissions.LIST_ALL_GROUPS) {
            endPoint = deviceServiceEndpoint + "/admin/groups";
        } else if (permissions.LIST_GROUPS) {
            endPoint = deviceServiceEndpoint + "/groups";
        } else {
            log.error("Access denied for user: " + carbonUser.username);
            return -1;
        }
        return serviceInvokers.XMLHttp.get(
            endPoint, function (responsePayload) {
                var data = JSON.parse(responsePayload.responseText);
                if(data) {
                    return data.deviceGroups;
                } else {
                    return [];
                }
            },
            function (responsePayload) {
                log.error(responsePayload);
                return -1;
            }
        );
    };

    groupModule.getGroup = function (groupId) {
        return serviceInvokers.XMLHttp.get(
                deviceServiceEndpoint + "/groups/id/" + groupId, function (responsePayload) {
                    return JSON.parse(responsePayload.responseText);
                },
                function (responsePayload) {
                    log.error(responsePayload);
                    return -1;
                }
        );
    };

    groupModule.getRolesOfGroup = function (groupId) {
        return serviceInvokers.XMLHttp.get(
                deviceServiceEndpoint + "/groups/id/" + groupId + "/roles", function (responsePayload) {
                    var data = JSON.parse(responsePayload.responseText);
                    if(data) {
                        return data.roles;
                    } else {
                        return [];
                    }
                },
                function (responsePayload) {
                    log.error(responsePayload);
                    return -1;
                }
        );
    };

}(groupModule));
