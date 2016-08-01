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
    // var log = new Log("policy-listing.js");
    context.handlebars.registerHelper('equal', function (lvalue, rvalue, options) {
        if (arguments.length < 3)
            throw new Error("Handlebars Helper equal needs 2 parameters");
        if (lvalue != rvalue) {
            return options.inverse(this);
        } else {
            return options.fn(this);
        }
    });
    var policyModule = require("/app/modules/business-controllers/group.js")["groupModule"];
    var response = policyModule.getAllPolicies();
    if (response["status"] == "success") {
        var policyListToView = response["content"];
        context["policyListToView"] = policyListToView;
        var policyCount = policyListToView.length;
        if (policyCount == 0) {
            context["policyListingStatusMsg"] = "No policy is available to be displayed.";
            context["saveNewPrioritiesButtonEnabled"] = false;
            context["noPolicy"] = true;
        } else if (policyCount == 1) {
            context["policyListingStatusMsg"] = "Add more policies to set up a priority order.";
            context["saveNewPrioritiesButtonEnabled"] = false;
            context["noPolicy"] = false;
        } else {
            context["policyListingStatusMsg"] = "Drag and move to re-order policy priority.";
            context["saveNewPrioritiesButtonEnabled"] = true;
            context["noPolicy"] = false;
        }
    } else {
        context["policyListToView"] = [];
        context["policyListingStatusMsg"] = response["content"];
        context["saveNewPrioritiesButtonEnabled"] = false;
    }
    return context;
}
