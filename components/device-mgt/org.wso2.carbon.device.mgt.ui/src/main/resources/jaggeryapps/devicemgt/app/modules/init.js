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
    "/permission/admin/device-mgt/devices/enroll": ["ui.execute"],
    "/permission/admin/device-mgt/devices/disenroll": ["ui.execute"],
    "/permission/admin/device-mgt/devices/owning-device": ["ui.execute"],
    "/permission/admin/device-mgt/groups": ["ui.execute"],
    "/permission/admin/device-mgt/notifications": ["ui.execute"],
    "/permission/admin/device-mgt/policies": ["ui.execute"],
    "/permission/admin/manage/api/subscribe": ["ui.execute"]
};

userModule.addRole("internal/devicemgt-user", ["admin"], permissions);
