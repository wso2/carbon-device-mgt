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
    var log = new Log("devices.js");
    var constants = require("/app/modules/constants.js");
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var deviceModule = require("/app/modules/business-controllers/device.js")["deviceModule"];
    var groupModule = require("/app/modules/business-controllers/group.js")["groupModule"];

    var utility = require("/app/modules/utility.js").utility;
    context.handlebars.registerHelper('equal', function(lvalue, rvalue, options) {
        if (arguments.length < 3)
            throw new Error("Handlebars Helper equal needs 2 parameters");
        if (lvalue != rvalue) {
            return options.inverse(this);
        } else {
            return options.fn(this);
        }
    });

    var groupId = request.getParameter("groupId");

    var viewModel = {};
    var title = "Devices";
    if (groupId) {
        var group = groupModule.getGroup(groupId);
        if (group) {
            title = group.name + " " + title;
            viewModel.roles = groupModule.getRolesOfGroup(groupId);
            viewModel.group = group;
        }
    }
    viewModel.title = title;
    var currentUser = session.get(constants.USER_SESSION_KEY);
    if (currentUser) {
        viewModel.permissions = {};
        var uiPermissions = userModule.getUIPermissions();
        viewModel.permissions.list = stringify(uiPermissions);
        if (uiPermissions.ADD_DEVICE) {
            viewModel.permissions.enroll = true;
        }
        viewModel.currentUser = currentUser;
        var deviceCount = 0;
        if (groupId) {
            deviceCount = groupModule.getGroupDeviceCount(groupId);
        } else {
            deviceCount = deviceModule.getDevicesCount();
        }
        if (deviceCount > 0) {
            viewModel.deviceCount = deviceCount;
            var utility = require("/app/modules/utility.js").utility;
            var typesListResponse = deviceModule.getDeviceTypes();
            var deviceTypes = [];
            if (typesListResponse["status"] == "success") {
                var data = typesListResponse.content.deviceTypes;
                if (data) {
                    for (var i = 0; i < data.length; i++) {
                        var config = utility.getDeviceTypeConfig(data[i]);
                        if (!config) {
                            continue;
                        }
                        var deviceType = config.deviceType;
                        deviceTypes.push({
                            "type": data[i],
                            "category": deviceType.category,
                            "label": deviceType.label,
                            "thumb": utility.getDeviceThumb(data[i]),
                            "analyticsEnabled": deviceType.analyticsEnabled,
                            "groupingEnabled": deviceType.groupingEnabled
                        });
                    }
                }
            }
            viewModel.deviceTypes = stringify(deviceTypes);
        }
    }

    var mdmProps = require("/app/modules/conf-reader/main.js")["conf"];
    var analyticsServer = mdmProps["dashboardServerURL"];
    var analyticsURL = analyticsServer + "/portal/t/" + context.user.userDomain + "/dashboards/android-iot/battery?owner=" + context.user.username + "&deviceId=";
    viewModel.analyticsURL = analyticsURL;
    viewModel.controlOps = getOperationList();
    viewModel.controlOperations = viewModel.controlOps.controlOperations;
    viewModel.device = viewModel.controlOps.device;
    return viewModel;

}

function getOperationList() {
    var log = new Log("operation.js");
    var operationModule = require("/app/modules/business-controllers/operation.js")["operationModule"];
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var device = {
        "type": "android",
    };

    // var autoCompleteParams = [{ "name": "deviceId", "value": "76c283c5accafb1f" }];
    var encodedFeaturePayloads = null;
    var allControlOps = operationModule.getControlOperations(device.type);
    var filteredControlOps = [];
    var queryParams = [];
    var formParams = [];
    var pathParams = [];
    for (var i = 0; i < allControlOps.length; i++) {
        var controlOperation = {};
        var uiPermission = allControlOps[i]["permission"];
        if (uiPermission && !userModule.isAuthorized("/permission/admin" + uiPermission)) {
            continue;
        }
        controlOperation = allControlOps[i];
        var currentParamList = allControlOps[i]["params"];
        for (var j = 0; j < currentParamList.length; j++) {
            var currentParam = currentParamList[j];
            currentParamList[j]["formParams"] = processParams(currentParam["formParams"]);
            currentParamList[j]["queryParams"] = processParams(currentParam["queryParams"]);
            currentParamList[j]["pathParams"] = processParams(currentParam["pathParams"]);
        }
        controlOperation["params"] = currentParamList;
        if (encodedFeaturePayloads) {
            allControlOps[i]["payload"] = getPayload(encodedFeaturePayloads, allControlOps[i]["operation"]);
        }
        filteredControlOps.push(controlOperation);
    }

    return { "controlOperations": filteredControlOps, "device": device };
}

function processParams(paramsList) {
    for (var i = 0; i < paramsList.length; i++) {
        var paramName = paramsList[i];
        var paramValue = "";
        var paramType = "text";
        // for (var k = 0; k < autoCompleteParams.length; k++) {
        //     if (paramName == autoCompleteParams[k].name) {
        //         paramValue = autoCompleteParams[k].value;
        //         paramType = "hidden";
        //     }
        // }
        paramsList[i] = { "name": paramName, "value": paramValue, "type": paramType };
    }
    return paramsList;
}

function getPayload(featuresPayload, featureCode) {
    var featuresJSONPayloads = JSON.parse(featuresPayload);
    return featuresJSONPayloads[featureCode];
}