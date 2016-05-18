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
    context.handlebars.registerHelper('equal', function (lvalue, rvalue, options) {
        if (arguments.length < 3) {
            throw new Error("Handlebars Helper equal needs 2 parameters");
        }
        if (lvalue != rvalue) {
            return options.inverse(this);
        } else {
            return options.fn(this);
        }
    });
    var page = {};
    var policyModule = require("/app/modules/policy.js")["policyModule"];
    var userModule = require("/app/modules/user.js")["userModule"];
    var response = policyModule.getAllPolicies();
    if (response["status"] == "success") {
        var policyListToView = response["content"];
        page["policyListToView"] = policyListToView;
        var policyCount = policyListToView.length;
        if (policyCount == 0) {
            page["policyListingStatusMsg"] = "No policy is available to be displayed.";
            page["saveNewPrioritiesButtonEnabled"] = false;
            page["noPolicy"] = true;
        } else if (policyCount == 1) {
            page["saveNewPrioritiesButtonEnabled"] = false;
            page["noPolicy"] = false;
            page["isUpdated"] = response["updated"];
        } else {
            page["saveNewPrioritiesButtonEnabled"] = true;
            page["noPolicy"] = false;
            page["isUpdated"] = response["updated"];
        }
    } else {
        // here, response["status"] == "error"
        page["policyListToView"] = [];
        page["policyListingStatusMsg"] = "An unexpected error occurred @ backend. Please try again later.";
        page["saveNewPrioritiesButtonEnabled"] = false;
        page["noPolicy"] = true;
    }

    if (userModule.isAuthorized("/permission/admin/device-mgt/policies/delete")) {
        page["removePermitted"] = true;
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/policies/remove")) {
        page["removePermitted"] = true;
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/policies/update")) {
        page["editPermitted"] = true;
    }
    page.permissions = userModule.getUIPermissions();
    return page;
}
