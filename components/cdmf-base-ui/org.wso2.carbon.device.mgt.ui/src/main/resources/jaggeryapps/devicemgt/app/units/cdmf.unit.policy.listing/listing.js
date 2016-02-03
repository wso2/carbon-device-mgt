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
    var log = new Log("policy-listing.js");
    var policyModule = require("/app/modules/policy.js")["policyModule"];
    var response = policyModule.getAllPolicies();
    var pageData = {};
    if (response["status"] == "success") {
        var policyListToView = response["content"];
        pageData["policyListToView"] = policyListToView;
        var policyCount = policyListToView.length;
        if (policyCount == 0) {
            pageData["saveNewPrioritiesButtonEnabled"] = false;
            pageData["noPolicy"] = true;
        } else if (policyCount == 1) {
            pageData["saveNewPrioritiesButtonEnabled"] = false;
            pageData["isUpdated"] = response["updated"];
        } else {
            pageData["saveNewPrioritiesButtonEnabled"] = true;
            pageData["isUpdated"] = response["updated"];
        }
    } else {
        // here, response["status"] == "error"
        pageData["policyListToView"] = [];
        pageData["saveNewPrioritiesButtonEnabled"] = false;
        pageData["noPolicy"] = true;
    }
    log.info(pageData);
    return pageData;
}
