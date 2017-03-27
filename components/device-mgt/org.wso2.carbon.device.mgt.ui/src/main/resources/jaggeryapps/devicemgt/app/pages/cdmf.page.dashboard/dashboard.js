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

function onRequest() {
    var constants = require("/app/modules/constants.js");
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var deviceModule = require("/app/modules/business-controllers/device.js")["deviceModule"];
    var groupModule = require("/app/modules/business-controllers/group.js")["groupModule"];
    var policyModule = require("/app/modules/business-controllers/policy.js")["policyModule"];

    var user = session.get(constants["USER_SESSION_KEY"]);
    var permissions = userModule.getUIPermissions();

    if (!permissions.VIEW_DASHBOARD) {
        response.sendRedirect(devicemgtProps["appContext"] + "devices");
        return;
    }

    var viewModel = {};
    viewModel.permissions = permissions;
    viewModel.enrollmentURL = devicemgtProps.enrollmentURL;
    viewModel.deviceCount = deviceModule.getDevicesCount();
    viewModel.groupCount = groupModule.getGroupCount();
    viewModel.userCount = userModule.getUsersCount();
    viewModel.policyCount = policyModule.getPoliciesCount();
    viewModel.isCloud = devicemgtProps.isCloud;
    if (devicemgtProps.isCloud) {
        viewModel.roleCount = userModule.getFilteredRoles("devicemgt").content.count;
    } else {
        viewModel.roleCount = userModule.getRolesCount();
    }
    return viewModel;
}