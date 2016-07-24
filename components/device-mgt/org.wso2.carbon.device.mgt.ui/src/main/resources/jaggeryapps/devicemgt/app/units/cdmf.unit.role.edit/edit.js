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
 * 
 * @param context Object that gets updated with the dynamic state of this page to be presented
 * @returns {*} A context object that returns the dynamic state of this page to be presented
 */
function onRequest(context) {
    var userModule = require("/app/modules/user.js")["userModule"];
    var deviceMgtProps = require("/app/conf/reader/main.js").config();
    var uri = request.getRequestURI();
    var uriMatcher = new URIMatcher(String(uri));
    var isMatched = uriMatcher.match("/{context}/role/edit/{rolename}");

    if (isMatched) {
        var matchedElements = uriMatcher.elements();
        var roleName = matchedElements.rolename;
        var response = userModule.getRole(roleName);
        if (response["status"] == "success") {
            context["role"] = response["content"];
        }
        var userStores = userModule.getSecondaryUserStores();
        context["userStores"] = userStores;
    }
    //TODO: error scenario
    context["roleNameJSRegEx"] = deviceMgtProps.roleValidationConfig.rolenameJSRegEx;
    context["roleNameHelpText"] = deviceMgtProps.roleValidationConfig.rolenameHelpMsg;
    context["roleNameRegExViolationErrorMsg"] = deviceMgtProps.roleValidationConfig.rolenameRegExViolationErrorMsg;
    return context;
}