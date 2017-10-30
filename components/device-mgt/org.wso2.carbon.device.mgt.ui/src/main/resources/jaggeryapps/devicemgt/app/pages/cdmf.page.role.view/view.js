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
    var log = new Log('roles_view.js')
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var rolename = request.getParameter("rolename");
    if (rolename === 'admin') {
        var canEdit = false;
    } else {
        var canEdit = true;
    }
    var user = userModule.getRole(rolename)["content"];
    var deviceMgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var isExsistingRole = false;
    var roleName = request.getParameter("rolename");

    var role, userRoles, devices;

    if (rolename) {
        var response = userModule.getRole(rolename);

        if (response["status"] == "success") {
            role = response["content"];
            role.domain = response["userDomain"];
            isExsistingUser = true;
        }

        // response = userModule.getRolesByUsername(userName);
        // if (response["status"] == "success") {
        //     userRoles = response["content"];
        // }
        // var deviceModule = require("/app/modules/business-controllers/device.js")["deviceModule"];
        // devices = deviceModule.getDevices(userName);
    }

    var canView = false;
    if (userModule.isAuthorized("/permission/admin/device-mgt/users/view")) {
        canView = true;
    }

    var isCloud = deviceMgtProps.isCloud;
    log.info("role");
    log.info(role);
    return { "exists": isExsistingUser, "role": role, "canView": canView, "isCloud": isCloud, "canEdit": canEdit };
}