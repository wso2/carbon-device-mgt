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
var policyCurrentStatus = "Active";

var displayPolicy = function(policyPayloadObj) {
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
        policyCurrentStatus = "Active";
    } else if (policyPayloadObj["active"] == true && policyPayloadObj["updated"] == false) {
        policyStatus = '<i class="fw fw-success icon-success"></i> Active</span>';
        policyCurrentStatus = "Active";
    } else if (policyPayloadObj["active"] == false && policyPayloadObj["updated"] == true) {
        policyStatus = '<i class="fw fw-warning icon-warning"></i> Inactive/Updated</span>';
        policyCurrentStatus = "Inactive";
    } else if (policyPayloadObj["active"] == false && policyPayloadObj["updated"] == false) {
        policyStatus = '<i class="fw fw-error icon-danger"></i> Inactive</span>';
        policyCurrentStatus = "Inactive";
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
        $.template(policyViewTemplateCacheKey, context + policyViewTemplateSrc, function(template) {
            var content = template({ "iscloud": $("#logged-in-user").data("iscloud") });
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
var getParameterByName = function(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
};

$(document).ready(function() {
    $('#loading-content').show();
    var path = window.location.href;
    $('ul.nav a').each(function() {
        var url = this.href;
        if (url.indexOf("/devicemgt/policies") !== -1) {
            $(this).addClass('active');
        }
    });

    $("#actbar").hide();
    $("#actnbtn").hide();
    if ($('#actbar ul').children().length == 0) {
        $("#actbtn").hide();
    } else {
        $("#actbtn").show();
    }

    $('#actbar').click(function() {
        event.stopPropagation();
    })

    $('#actToggleBtn').click(function() {
        event.stopPropagation();
        $("#actbar").slideToggle();
    });

    var policyPayloadObj;
    // Adding initial state of wizard-steps.
    invokerUtil.get(
        "/api/device-mgt/v1.0/policies/" + getParameterByName("id"),
        // on success
        function(data, textStatus, jqXHR) {
            if (jqXHR.status == 200 && data) {
                policyPayloadObj = JSON.parse(data);
                displayPolicy(policyPayloadObj);
            }
        },
        // on error
        function(jqXHR) {
            console.log(jqXHR);
            // should be redirected to an error page
        }
    );

    // [4] logic for removing a selected set of policies

    $(".policy-remove-link").click(function() {
        var policyList = [];

        policyList[0] = parseInt(getParameterByName('id'));
        var statusList = policyCurrentStatus;
        console.log("deleting with id " + policyList + "änd status " + statusList);
        if (statusList === 'Active') {
            // if policies found in Active or Active/Updated states with in the selection,
            // pop-up an error saying
            // "You cannot remove already active policies. Please deselect active policies and try again."
            modalDialog.header('Action cannot be performed !');
            modalDialog.content('You cannot delete already active policies. ');
            modalDialog.footer('<div class="buttons"><a href="javascript:modalDialog.hide()" class="btn-operations">' +
                'Ok</a></div>');
            modalDialog.showAsAWarning();
        } else {
            var serviceURL = "/api/device-mgt/v1.0/policies/remove-policy";
            if (policyList.length == 0) {
                modalDialog.header('Action cannot be performed !');
                modalDialog.content('Please select a policy or a list of policies to remove.');
                modalDialog.footer('<div class="buttons"><a href="javascript:modalDialog.hide()" ' +
                    'class="btn-operations">Ok</a></div>');
                modalDialog.showAsAWarning();
            } else {
                modalDialog.header('Do you really want to remove the policy?');
                modalDialog.footer('<div class="buttons"><a href="#" id="remove-policy-yes-link" class=' +
                    '"btn-operations">Remove</a> <a href="#" id="remove-policy-cancel-link" ' +
                    'class="btn-operations btn-default">Cancel</a></div>');
                modalDialog.show();
            }

            // on-click function for policy removing "yes" button
            $("a#remove-policy-yes-link").click(function() {
                invokerUtil.post(
                    serviceURL,
                    policyList,
                    // on success
                    function(data, textStatus, jqXHR) {
                        if (jqXHR.status == 200 && data) {
                            modalDialog.header('Done. Selected policy was successfully removed.');
                            modalDialog.footer('<div class="buttons"><a href="#" id="remove-policy-success-link" ' +
                                'class="btn-operations">Ok</a></div>');
                            $("a#remove-policy-success-link").click(function() {
                                modalDialog.hide();
                                location.href = "../policies";
                            });
                        }
                    },
                    // on error
                    function(jqXHR) {
                        console.log(stringify(jqXHR.data));
                        modalDialog.header('An unexpected error occurred. Please try again later.');
                        modalDialog.footer('<div class="buttons"><a href="#" id="remove-policy-error-link" ' +
                            'class="btn-operations">Ok</a></div>');
                        modalDialog.showAsError();
                        $("a#remove-policy-error-link").click(function() {
                            modalDialog.hide();
                        });
                    }
                );
            });

            // on-click function for policy removing "cancel" button
            $("a#remove-policy-cancel-link").click(function() {
                modalDialog.hide();
            });
        }
    });

    $('#loading-content').remove();

});

$(document).click(function() {
    $("#actbar").hide();
})