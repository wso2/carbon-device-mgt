/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

    var userModule = require("/app/modules/user.js").userModule;
    var constants = require("/app/modules/constants.js");
    var permissions = userModule.getUIPermissions();
    var devicemgtProps = require('/app/conf/devicemgt-props.js').config();
    context.permissions = permissions;
    context["enrollmentURL"] = devicemgtProps.enrollmentURL;
    var deviceModule = require("/app/modules/device.js").deviceModule;
    var policyModule = require("/app/modules/policy.js").policyModule;

    context.device_count = deviceModule.getOwnDevicesCount();
    context.user_count = userModule.getUsers()["content"].length;
    context.policy_count = policyModule.getAllPolicies()["content"].length;

    return context;
}