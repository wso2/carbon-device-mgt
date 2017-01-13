/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var policy = {};
var hasPolicyProfileScript = false;

var displayPolicy = function (policyPayloadObj) {
    policy["name"] = policyPayloadObj["policyName"];
    policy["platform"] = policyPayloadObj["profile"]["deviceType"];
    // updating next-page wizard title with selected platform
    $("#policy-heading").text(policy["platform"].toUpperCase() + " POLICY - " + policy["name"].toUpperCase());
    $("#policy-platform").text(policy["platform"].toUpperCase());
    $("#policy-assignment").text(policyPayloadObj.deviceGroups);
    $("#policy-action").text(policyPayloadObj.compliance.toUpperCase());
    $("#policy-description").text(policyPayloadObj["description"]);
    var policyStatus = "Active";
    if (policyPayloadObj["active"] == true && policyPayloadObj["updated"] == true) {
        policyStatus = '<i class="fw fw-warning icon-success"></i> Active/Updated</span>';
    } else if (policyPayloadObj["active"] == true && policyPayloadObj["updated"] == false) {
        policyStatus = '<i class="fw fw-success icon-success"></i> Active</span>';
    } else if (policyPayloadObj["active"] == false && policyPayloadObj["updated"] == true) {
        policyStatus = '<i class="fw fw-warning icon-warning"></i> Inactive/Updated</span>';
    } else if (policyPayloadObj["active"] == false && policyPayloadObj["updated"] == false) {
        policyStatus = '<i class="fw fw-error icon-danger"></i> Inactive</span>';
    }

    $("#policy-status").html(policyStatus);

    if (policyPayloadObj.users.length > 0) {
        $("#policy-users").text(policyPayloadObj.users.toString().split(",").join(", "));
    } else {
        $("#users-row").addClass("hidden");
    }
    if (policyPayloadObj.deviceGroups.length > 0) {
        debugger;
        var deviceGroups = policyPayloadObj.deviceGroups;
        var assignedGroups = [];
        for (var index in deviceGroups) {
            if (deviceGroups.hasOwnProperty(index)) {
                assignedGroups.push(deviceGroups[index].name);
            }
        }
        $("#policy-groups").text(assignedGroups.toString().split(",").join(", "));
    } else {
        $("#policy-groups").text("NONE");
    }

    if (policyPayloadObj.roles.length > 0) {
        $("#policy-roles").text(policyPayloadObj.roles.toString().split(",").join(", "));
    } else {
        $("#roles-row").addClass("hidden");
    }

    var deviceType = policy["platform"];
    var policyOperations = $("#policy-operations");
    var policyViewTemplateSrc = $(policyOperations).data("template");
    var policyViewScriptSrc = $(policyOperations).data("script");
    var policyViewStylesSrc = $(policyOperations).data("style");
    var policyViewTemplateCacheKey = deviceType + '-policy-view';

    if (policyViewTemplateSrc) {
        if (policyViewScriptSrc) {
            var script = document.createElement('script');
            script.type = 'text/javascript';
            script.src = context + policyViewScriptSrc;
            $(".wr-advance-operations").prepend(script);
            hasPolicyProfileScript = true;
        } else {
            hasPolicyProfileScript = false;
        }
        $.template(policyViewTemplateCacheKey, context + policyViewTemplateSrc, function (template) {
            var content = template();
            $("#device-type-policy-operations").html(content).removeClass("hidden");
            $(".policy-platform").addClass("hidden");
            if (hasPolicyProfileScript) {
                /*
                 This method should be implemented in the relevant plugin side and should include the logic to
                 populate the policy profile in the plugin specific UI.
                 */
                polulateProfileOperations(policyPayloadObj["profile"]["profileFeaturesList"]);
            }
        });
    } else {
        $("#generic-policy-operations").removeClass("hidden");
    }
    if (policyViewStylesSrc) {
        var style = document.createElement('link');
        style.type = 'text/css';
        style.rel = 'stylesheet';
        style.href = context + policyViewStylesSrc;
        $(".wr-advance-operations").prepend(style);
    }
    $(".wr-advance-operations-init").addClass("hidden");

    if (!hasPolicyProfileScript) {
        populateGenericProfileOperations(policyPayloadObj["profile"]["profileFeaturesList"]);
    }
};

/**
 * This method will return query parameter value given its name.
 * @param name Query parameter name
 * @returns {string} Query parameter value
 */
var getParameterByName = function (name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
};

$(document).ready(function () {
    var policyPayloadObj;
    // Adding initial state of wizard-steps.
    invokerUtil.get(
        "/api/device-mgt/v1.0/policies/" + getParameterByName("id"),
        // on success
        function (data, textStatus, jqXHR) {
            if (jqXHR.status == 200 && data) {
                policyPayloadObj = JSON.parse(data);
                displayPolicy(policyPayloadObj);
            }
        },
        // on error
        function (jqXHR) {
            console.log(jqXHR);
            // should be redirected to an error page
        }
    );
});
