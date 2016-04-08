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
    var userModule = require("/app/modules/user.js").userModule;
    var deviceModule = require("/app/modules/device.js").deviceModule;

    var groupName = request.getParameter("groupName");
    var groupOwner = request.getParameter("groupOwner");

    var page = {};
    var title = "Devices";
    if (groupName) {
        title = groupName + " " + title;
        page.groupName = groupName;
    }
    page.title = title;
    page.permissions = {};
    var currentUser = session.get(constants.USER_SESSION_KEY);
    var permissions = [];
    if (currentUser) {
        if (userModule.isAuthorized("/permission/admin/device-mgt/admin/devices/list")) {
            permissions.push("LIST_DEVICES");
        } else if (userModule.isAuthorized("/permission/admin/device-mgt/user/devices/list")) {
            permissions.push("LIST_OWN_DEVICES");
        } else if (userModule.isAuthorized("/permission/admin/device-mgt/emm-admin/policies/list")) {
            permissions.push("LIST_POLICIES");
        }
        if (userModule.isAuthorized("/permission/admin/device-mgt/admin/devices/add")) {
            permissions.enroll = true;
        }
        if (userModule.isAuthorized("/permission/admin/device-mgt/admin/devices/remove")) {
            permissions.push("REMOVE_DEVICE");
        }

        page.permissions.list = permissions;
        page.currentUser = currentUser;
        var deviceCount = 0;
        if (groupName && groupOwner) {
            var groupModule = require("/app/modules/group.js").groupModule;
            deviceCount = groupModule.getGroupDeviceCount(groupName, groupOwner);
            page.groupOwner = groupOwner;
        } else {
            deviceCount = deviceModule.getOwnDevicesCount();
        }
        if (deviceCount > 0) {
            page.deviceCount = deviceCount;
            var utility = require("/app/modules/utility.js").utility;
            var data = deviceModule.getDeviceTypes();
            var deviceTypes = [];
            if (data.data) {
                for (var i = 0; i < data.data.length; i++) {
                    deviceTypes.push({
                                         "type": data.data[i].name,
                                         "category": utility.getDeviceTypeConfig(data.data[i].name).deviceType.category
                                     });
                }
            }
            page.deviceTypes = deviceTypes;
        }
    }
    return page;
}