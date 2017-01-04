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
    var constants = require("/app/modules/constants.js");
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var deviceModule = require("/app/modules/business-controllers/device.js")["deviceModule"];
    var groupModule = require("/app/modules/business-controllers/group.js")["groupModule"];
    
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
    return viewModel;
}
