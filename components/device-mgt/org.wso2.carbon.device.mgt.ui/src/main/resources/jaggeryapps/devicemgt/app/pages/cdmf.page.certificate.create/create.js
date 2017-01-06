/*
 Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

 WSO2 Inc. licenses this file to you under the Apache License,
 Version 2.0 (the "License"); you may not use this file except
 in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 either express or implied. See the License for the
 specific language governing permissions and limitations
 under the License.
 */

/**
 * Returns the dynamic state to be populated by add-user page.
 *
 * @param viewModel Object that gets updated with the dynamic state of this page to be presented
 * @returns {*} A viewModel object that returns the dynamic state of this page to be presented
 */
function onRequest(context) {
    // var log = new Log("units/user-create/create.js");
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var mdmProps = require("/app/modules/conf-reader/main.js")["conf"];
    var viewModel = {};
    viewModel.isAuthorized = userModule.isAuthorized("/permission/admin/device-mgt/certificates/manage");
    var response = userModule.getRolesByUserStore();
    if (response["status"] == "success") {
        viewModel["roles"] = response["content"];
    }

    viewModel["charLimit"] = mdmProps["userValidationConfig"]["usernameLength"];
    viewModel["usernameJSRegEx"] = mdmProps["userValidationConfig"]["usernameJSRegEx"];
    viewModel["usernameHelpText"] = mdmProps["userValidationConfig"]["usernameHelpMsg"];
    viewModel["usernameRegExViolationErrorMsg"] = mdmProps["userValidationConfig"]["usernameRegExViolationErrorMsg"];
    viewModel["firstnameJSRegEx"] = mdmProps["userValidationConfig"]["firstnameJSRegEx"];
    viewModel["firstnameRegExViolationErrorMsg"] = mdmProps["userValidationConfig"]["firstnameRegExViolationErrorMsg"];
    viewModel["lastnameJSRegEx"] = mdmProps["userValidationConfig"]["lastnameJSRegEx"];
    viewModel["lastnameRegExViolationErrorMsg"] = mdmProps["userValidationConfig"]["lastnameRegExViolationErrorMsg"];

    return viewModel;
}