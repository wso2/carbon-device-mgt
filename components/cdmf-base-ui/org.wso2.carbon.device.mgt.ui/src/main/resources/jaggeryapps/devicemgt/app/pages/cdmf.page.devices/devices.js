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
    var page = {};
    var groupName = request.getParameter("groupName");
    var title = "Devices";
    if (groupName) {
        title = groupName + " " + title;
        page.groupName = groupName;
    }
    page.title =title;
    page.permissions = {};
    var currentUser = session.get(constants.USER_SESSION_KEY);
    if (currentUser) {
        if (userModule.isAuthorized("/permission/admin/device-mgt/admin/devices/add")) {
            page.permissions.enroll = true;
        }
    }
    return page;
}