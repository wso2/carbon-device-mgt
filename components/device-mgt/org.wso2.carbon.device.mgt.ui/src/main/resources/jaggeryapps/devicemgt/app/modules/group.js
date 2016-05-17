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
    var log = new Log("/app/modules/group.js");

    var userModule = require("/app/modules/user.js").userModule;
    var constants = require('/app/modules/constants.js');
    var devicemgtProps = require('/app/conf/devicemgt-props.js').config();
    var utility = require("/app/modules/utility.js").utility;
    var serviceInvokers = require("/app/modules/backend-service-invoker.js").backendServiceInvoker;

    var groupServiceEndpoint = devicemgtProps["httpsURL"] + constants.ADMIN_SERVICE_CONTEXT + "/groups";

    var user = session.get(constants.USER_SESSION_KEY);

    var endPoint;

    groupModule.getGroupCount = function () {
        var permissions = userModule.getUIPermissions();
        if (permissions.LIST_ALL_GROUPS) {
            endPoint = groupServiceEndpoint + "/count";
        } else if (permissions.LIST_GROUPS) {
            endPoint = groupServiceEndpoint + "/user/" + user.username + "/count";
        } else {
            log.error("Access denied for user: " + carbonUser.username);
            return -1;
        }
        return serviceInvokers.XMLHttp.get(
                endPoint, function (responsePayload) {
                    return responsePayload;
                },
                function (responsePayload) {
                    log.error(responsePayload);
                    return -1;
                }
        );
    };

    groupModule.getGroupDeviceCount = function (groupName, owner) {
        endPoint = groupServiceEndpoint + "/owner/" + owner + "/name/" + groupName + "/devices/count";
        return serviceInvokers.XMLHttp.get(
                endPoint, function (responsePayload) {
                    return responsePayload;
                },
                function (responsePayload) {
                    log.error(responsePayload);
                    return -1;
                }
        );
    };

    groupModule.getGroupDevices = function (groupName, owner) {
        endPoint = groupServiceEndpoint + "/owner/" + owner + "/name/" + groupName + "/devices";
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

}(groupModule));
