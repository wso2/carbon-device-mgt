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

window.queryEditor = CodeMirror.fromTextArea(document.getElementById('policy-definition-input'), {
    mode: "application/json",
    indentWithTabs: true,
    smartIndent: true,
    lineNumbers: true,
    matchBrackets: true,
    autofocus: true
});

var validatePolicyProfile = function () {
    return true;
};

/**
 * Generates policy profile feature list which will be saved with the profile.
 *
 * This function will be invoked from the relevant cdmf unit at the time of policy creation.
 *
 * @returns {Array} profile payloads
 */
var generateGenericPayload = function () {
    return [{
        "featureCode": "CONFIG",
        "deviceType": policy["platform"],
        "content": {"policyDefinition": window.queryEditor.getValue()}
    }];
};

/**
 * Populates policy configuration to the ui elements.
 *
 * This method will be invoked from the relevant cdmf unit when the edit page gets loaded.
 *
 * @param profileFeatureList saved feature list
 */
var populateGenericProfileOperations = function (profileFeatureList) {
    var content = JSON.parse(profileFeatureList[0]["content"]);
    window.queryEditor.setValue(content.policyDefinition);
    setTimeout(function() {
        window.queryEditor.refresh();
    }, 100);
};