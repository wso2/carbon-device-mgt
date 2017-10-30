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

var carbonModule = require("carbon");
var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
var userModule = require("/app/modules/business-controllers/user.js")["userModule"];

//noinspection JSUnresolvedFunction Server
var carbonServer = new carbonModule.server.Server({
    tenanted: true,
    url: devicemgtProps["httpsURL"] + "/admin"
});

application.put("carbonServer", carbonServer);

var permissions = {
    "/permission/admin/Login": ["ui.execute"],
    "/permission/admin/device-mgt/device/api/subscribe": ["ui.execute"],
	"/permission/admin/device-mgt/devices/enroll": ["ui.execute"],
	"/permission/admin/device-mgt/devices/disenroll": ["ui.execute"],
	"/permission/admin/device-mgt/devices/owning-device/view": ["ui.execute"],
	"/permission/admin/manage/portal": ["ui.execute"]
};

var adminPermissions = {
    "/permission/admin/device-mgt": ["ui.execute"],
	"/permission/admin/manage/api": ["ui.execute"],
	"/permission/admin/manage/portal": ["ui.execute"]
};

//On Startup, admin user will get both roles: devicemgt-admin and devicemgt-user
//Average user through sign-up will only receive the role: devicemgt-user.
//Admin can setup necessary permissions for the role: devicemgt-user
userModule.addRole("internal/devicemgt-user", ["admin"], permissions);
userModule.addRole("internal/devicemgt-admin", ["admin"], adminPermissions);
