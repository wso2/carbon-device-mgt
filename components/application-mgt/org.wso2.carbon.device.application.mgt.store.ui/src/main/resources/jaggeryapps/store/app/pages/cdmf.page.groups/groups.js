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
    var groupModule = require("/app/modules/business-controllers/group.js")["groupModule"];
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var constants = require("/app/modules/constants.js");
    var deviceMgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var currentUser = session.get(constants.USER_SESSION_KEY);
    var page = {};
    if (currentUser) {
        page.permissions = userModule.getUIPermissions();
        page.permissions.list = stringify(page.permissions);
        page.currentUser = currentUser;
        var groupCount = groupModule.getGroupCount();
        if (groupCount > 0) {
            page.groupCount = groupCount;
        }
    }
    page.isCloud = deviceMgtProps.isCloud;
    return page;
}