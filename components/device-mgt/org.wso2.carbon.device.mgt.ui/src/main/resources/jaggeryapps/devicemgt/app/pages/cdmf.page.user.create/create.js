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

/**
 * Returns the dynamic state to be populated by add-user page.
 * @returns {*} A context object that returns the dynamic state of this page to be presented
 */
function onRequest() {
    //var log = new Log("/app/pages/cdmf.page.user.create server-side js");

    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
   
    var page = {};
    var response = userModule.getRolesByUserStore("PRIMARY");
    if (response["status"] == "success") {
        page["roles"] = response["content"];
    }

    if (userModule.isAuthorized("/permission/admin/device-mgt/users/manage")) {
        page.canManage = true;
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/roles/view")) {
        page.canViewRoles = true;
    }
    var userStores = userModule.getSecondaryUserStores();
    page["userStores"] = userStores;
    page["charLimit"] = devicemgtProps["userValidationConfig"]["usernameLength"];
    page["usernameJSRegEx"] = devicemgtProps["userValidationConfig"]["usernameJSRegEx"];
    page["usernameHelpMsg"] = devicemgtProps["userValidationConfig"]["usernameHelpMsg"];
    page["usernameRegExViolationErrorMsg"] = devicemgtProps["userValidationConfig"]["usernameRegExViolationErrorMsg"];
    page["firstnameJSRegEx"] = devicemgtProps["userValidationConfig"]["firstnameJSRegEx"];
    page["firstnameRegExViolationErrorMsg"] = devicemgtProps["userValidationConfig"]["firstnameRegExViolationErrorMsg"];
    page["lastnameJSRegEx"] = devicemgtProps["userValidationConfig"]["lastnameJSRegEx"];
    page["lastnameRegExViolationErrorMsg"] = devicemgtProps["userValidationConfig"]["lastnameRegExViolationErrorMsg"];
    page["enrollmentURL"] = devicemgtProps["generalConfig"]["host"] + devicemgtProps["enrollmentDir"];

    return page;
}