/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function onRequest(context) {
    var log = new Log("policy-view-edit-unit backend js");

    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var groupModule = require("/app/modules/business-controllers/group.js")["groupModule"];

    var rolesResult = userModule.getRoles();
    if (rolesResult.status == "success") {
        context.roles = rolesResult.content;
    }

    var usersResult = userModule.getUsers();
    if (usersResult.status == "success") {
        context.users = usersResult.content;
    }

    context["groups"] = groupModule.getGroups();

    var user = userModule.getCarbonUser();
    context["user"] = {username: user.username, domain: user.domain, tenantId: user.tenantId};

    context.isAuthorized = userModule.isAuthorized("/permission/admin/device-mgt/policies/manage");
    context.isAuthorizedViewUsers = userModule.isAuthorized("/permission/admin/device-mgt/roles/view");
    context.isAuthorizedViewRoles = userModule.isAuthorized("/permission/admin/device-mgt/users/view");
    context.isAuthorizedViewGroups = userModule.isAuthorized("/permission/admin/device-mgt/groups/view");

    return context;
}