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
    //  var log = new Log("wizard.js");
    var constants = require("/app/modules/constants.js");
    var DTYPE_CONF_DEVICE_TYPE_KEY = "deviceType";
    var DTYPE_CONF_DEVICE_TYPE_LABEL_KEY = "label";

    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var utility = require('/app/modules/utility.js').utility;
    var response = userModule.getRoles();
    var wizardPage = {};
    if (response["status"] == "success") {
        wizardPage["roles"] = response["content"];
    }
    var deviceType = context.uriParams.deviceType;
    var typesListResponse = userModule.getPlatforms();
    if (typesListResponse["status"] == "success") {
        wizardPage["type"] = {};
        for (var type in typesListResponse["content"]["deviceTypes"]) {
            if (deviceType == typesListResponse["content"]["deviceTypes"][type]) {
                wizardPage["type"]["name"] = deviceType;
                wizardPage["type"]["label"] = deviceType;
            }
        }
    }
    var user = session.get(constants.USER_SESSION_KEY);
    wizardPage.username = user.username;
    wizardPage.permissions = userModule.getUIPermissions();
    return wizardPage;
}