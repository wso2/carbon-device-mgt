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

var validateStep = {};
var skipStep = {};
var stepForwardFrom = {};
var stepBackFrom = {};
var policy = {};
var currentlyEffected = {};
var validateInline = {};
var clearInline = {};
var hasPolicyProfileScript = false;

var enableInlineError = function (inputField, errorMsg, errorSign) {
    var fieldIdentifier = "#" + inputField;
    var errorMsgIdentifier = "#" + inputField + " ." + errorMsg;
    var errorSignIdentifier = "#" + inputField + " ." + errorSign;

    if (inputField) {
        $(fieldIdentifier).addClass(" has-error has-feedback");
    }

    if (errorMsg) {
        $(errorMsgIdentifier).removeClass(" hidden");
    }

    if (errorSign) {
        $(errorSignIdentifier).removeClass(" hidden");
    }
};

var disableInlineError = function (inputField, errorMsg, errorSign) {
    var fieldIdentifier = "#" + inputField;
    var errorMsgIdentifier = "#" + inputField + " ." + errorMsg;
    var errorSignIdentifier = "#" + inputField + " ." + errorSign;

    if (inputField) {
        $(fieldIdentifier).removeClass(" has-error has-feedback");
    }

    if (errorMsg) {
        $(errorMsgIdentifier).addClass(" hidden");
    }

    if (errorSign) {
        $(errorSignIdentifier).addClass(" hidden");
    }
};

/**
 * Load all device groups.
 *
 * @param callback  function to call on loading completion.
 */
function loadGroups(callback) {
    invokerUtil.get(
        "/api/device-mgt/v1.0/groups",
        function (data) {
            data = JSON.parse(data);
            callback(data.deviceGroups);
        });
}

/**
 * Creates DeviceGroupWrapper object from selected groups.
 *
 * @param selectedGroups
 * @returns {Array} DeviceGroupWrapper list.
 */
var createDeviceGroupWrapper = function (selectedGroups) {
    var groupObjects = [];
    loadGroups(function (deviceGroups) {
        var tenantId = $("#logged-in-user").data("tenant-id");
        for (var index in deviceGroups) {
            if (deviceGroups.hasOwnProperty(index)) {
                var deviceGroupWrapper = {};
                if (selectedGroups.indexOf(deviceGroups[index].name) > -1) {
                    deviceGroupWrapper.id = deviceGroups[index].id;
                    deviceGroupWrapper.name = deviceGroups[index].name;
                    deviceGroupWrapper.owner = deviceGroups[index].owner;
                    deviceGroupWrapper.tenantId = tenantId;
                    groupObjects.push(deviceGroupWrapper);
                }
            }
        }
    });
    return groupObjects;
};

/**
 *clear inline validation messages.
 */
clearInline["policy-name"] = function () {
    disableInlineError("policy-name-field", "nameEmpty", "nameError");
};


/**
 * Validate if provided policy name is valid against RegEx configures.
 */
validateInline["policy-name"] = function () {
    var policyName = $("input#policy-name-input").val();
    if (policyName && inputIsValidAgainstLength(policyName, 1, 30)) {
        disableInlineError("policy-name-field", "nameEmpty", "nameError");
    } else {
        enableInlineError("policy-name-field", "nameEmpty", "nameError");
    }
};

$("#policy-name-input").focus(function () {
    clearInline["policy-name"]();
}).blur(function () {
    validateInline["policy-name"]();
});

skipStep["policy-platform"] = function (policyPayloadObj) {
    policy["name"] = policyPayloadObj["policyName"];
    policy["platform"] = policyPayloadObj["profile"]["deviceType"];

    var userRoleInput = $("#user-roles-input");
    var ownershipInput = $("#ownership-input");
    var userInput = $("#users-input");
    var groupsInput = $("#groups-input");
    var actionInput = $("#action-input");
    var policyNameInput = $("#policy-name-input");
    var policyDescriptionInput = $("#policy-description-input");

    currentlyEffected["roles"] = policyPayloadObj.roles;
    currentlyEffected["users"] = policyPayloadObj.users;
    currentlyEffected["groups"] = [];

    if (policyPayloadObj.deviceGroups) {
        var deviceGroups = policyPayloadObj.deviceGroups;
        for (var index in deviceGroups) {
            if (deviceGroups.hasOwnProperty(index)) {
                currentlyEffected["groups"].push(deviceGroups[index].name);
            }
        }
    } else {
        currentlyEffected["groups"].push("NONE");
    }

    if (currentlyEffected["roles"].length > 0) {
        $("#user-roles-radio-btn").prop("checked", true);
        $("#user-roles-select-field").show();
        $("#users-select-field").hide();
        userRoleInput.val(currentlyEffected["roles"]).trigger("change");
    } else if (currentlyEffected["users"].length > 0) {
        $("#users-radio-btn").prop("checked", true);
        $("#users-select-field").show();
        $("#user-roles-select-field").hide();
        userInput.val(currentlyEffected["users"]).trigger("change");
    }

    if (currentlyEffected["groups"].length > 0) {
        groupsInput.val(currentlyEffected["groups"]).trigger("change");
    }

    ownershipInput.val(policyPayloadObj.ownershipType);
    actionInput.val(policyPayloadObj.compliance);
    policyNameInput.val(policyPayloadObj["policyName"]);
    policyDescriptionInput.val(policyPayloadObj["description"]);
    // updating next-page wizard title with selected platform
    $("#policy-profile-page-wizard-title").text("EDIT " + policy["platform"] + " POLICY - " + policy["name"]);

    var deviceType = policy["platform"];
    var policyOperations = $("#policy-operations");
    var policyEditTemplateSrc = $(policyOperations).data("template");
    var policyEditScriptSrc = $(policyOperations).data("script");
    var policyEditStylesSrc = $(policyOperations).data("style");
    var policyEditTemplateCacheKey = deviceType + '-policy-edit';

    if (policyEditTemplateSrc) {
        if (policyEditScriptSrc) {
            var script = document.createElement('script');
            script.type = 'text/javascript';
            script.src = context + policyEditScriptSrc;
            $(".wr-advance-operations").prepend(script);
            hasPolicyProfileScript = true;
        } else {
            hasPolicyProfileScript = false;
        }
        $.template(policyEditTemplateCacheKey, context + policyEditTemplateSrc, function (template) {
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
    if (policyEditStylesSrc) {
        var style = document.createElement('link');
        style.type = 'text/css';
        style.rel = 'stylesheet';
        style.href = context + policyEditStylesSrc;
        $(".wr-advance-operations").prepend(style);
    }
    $(".wr-advance-operations-init").addClass("hidden");

    if (!hasPolicyProfileScript) {
        populateGenericProfileOperations(policyPayloadObj["profile"]["profileFeaturesList"]);
    }
};

/**
 * Forward action of policy profile page. Generates policy profile payload.
 */
stepForwardFrom["policy-profile"] = function () {
    if (hasPolicyProfileScript) {
        /*
         generatePolicyProfile() function should be implemented in plugin side and should include the logic to build the
         policy profile object.
         */
        policy["profile"] = generatePolicyProfile();
    }
    // updating next-page wizard title with selected platform
    $("#policy-criteria-page-wizard-title").text("EDIT " + policy["platform"] + " POLICY - " + policy["name"]);
};

/**
 * Forward action of policy criteria page.
 */
stepForwardFrom["policy-criteria"] = function () {
    $("input[type='radio'].select-users-radio").each(function () {
        if ($(this).is(':radio')) {
            if ($(this).is(":checked")) {
                if ($(this).attr("id") == "users-radio-btn") {
                    policy["selectedUsers"] = $("#users-input").val();
                    policy["selectedUserRoles"] = null;
                } else if ($(this).attr("id") == "user-roles-radio-btn") {
                    policy["selectedUsers"] = null;
                    policy["selectedUserRoles"] = $("#user-roles-input").val();
                }
            }
        }
    });
    policy["selectedGroups"] = $("#groups-input").val();
    if (policy["selectedGroups"] && (policy["selectedGroups"].length > 1 || policy["selectedGroups"][0] !== "NONE")) {
        policy["selectedGroups"] = createDeviceGroupWrapper(policy["selectedGroups"]);
    }

    policy["selectedNonCompliantAction"] = $("#action-input").find(":selected").data("action");
    policy["selectedOwnership"] = $("#ownership-input").val();
    // updating next-page wizard title with selected platform
    $("#policy-naming-page-wizard-title").text("EDIT " + policy["platform"] + " POLICY - " + policy["name"]);
};

/**
 * Checks if provided input is valid against provided length range.
 *
 * @param input Alphanumeric or non-alphanumeric input
 * @param minLength Minimum Required Length
 * @param maxLength Maximum Required Length
 * @returns {boolean} Returns true if input matches the provided minimum length and maximum length
 */
var inputIsValidAgainstLength = function (input, minLength, maxLength) {
    var length = input.length;
    return (length == minLength || (length > minLength && length < maxLength) || length == maxLength);
};

/**
 * Validates policy criteria inputs.
 *
 * @returns {boolean} whether the validation is successful.
 */
validateStep["policy-criteria"] = function () {
    var validationStatus = {};
    var selectedAssignees;
    var selectedField = "Role(s)";

    $("input[type='radio'].select-users-radio").each(function () {
        if ($(this).is(":checked")) {
            if ($(this).attr("id") == "users-radio-btn") {
                selectedAssignees = $("#users-input").val();
                selectedField = "User(s)";
            } else if ($(this).attr("id") == "user-roles-radio-btn") {
                selectedAssignees = $("#user-roles-input").val();
            }
            return false;
        }
    });

    if (selectedAssignees) {
        validationStatus["error"] = false;
    } else {
        validationStatus["error"] = true;
        validationStatus["mainErrorMsg"] = selectedField + " is a required field. It cannot be empty";
    }

    var wizardIsToBeContinued;
    if (validationStatus["error"]) {
        wizardIsToBeContinued = false;
        var mainErrorMsgWrapper = "#policy-criteria-main-error-msg";
        var mainErrorMsg = mainErrorMsgWrapper + " span";
        $(mainErrorMsg).text(validationStatus["mainErrorMsg"]);
        $(mainErrorMsgWrapper).removeClass("hidden");
    } else {
        wizardIsToBeContinued = true;
    }

    return wizardIsToBeContinued;
};

/**
 * Validating policy naming.
 *
 * @returns {boolean} whether the validation is successful.
 */
validateStep["policy-naming"] = function () {
    var validationStatus = {};

    // taking values of inputs to be validated
    var policyName = $("input#policy-name-input").val();
    // starting validation process and updating validationStatus
    if (!policyName) {
        validationStatus["error"] = true;
        validationStatus["mainErrorMsg"] = "Policy name is empty. You cannot proceed.";
    } else if (!inputIsValidAgainstLength(policyName, 1, 30)) {
        validationStatus["error"] = true;
        validationStatus["mainErrorMsg"] =
            "Policy name exceeds maximum allowed length.";
    } else {
        validationStatus["error"] = false;
    }
    // ending validation process

    // start taking specific actions upon validation
    var wizardIsToBeContinued;
    if (validationStatus["error"]) {
        wizardIsToBeContinued = false;
        var mainErrorMsgWrapper = "#policy-naming-main-error-msg";
        var mainErrorMsg = mainErrorMsgWrapper + " span";
        $(mainErrorMsg).text(validationStatus["mainErrorMsg"]);
        $(mainErrorMsgWrapper).removeClass("hidden");
    } else {
        wizardIsToBeContinued = true;
    }

    return wizardIsToBeContinued;
};

validateStep["policy-naming-publish"] = function () {
    var validationStatus = {};

    // taking values of inputs to be validated
    var policyName = $("input#policy-name-input").val();
    // starting validation process and updating validationStatus
    if (!policyName) {
        validationStatus["error"] = true;
        validationStatus["mainErrorMsg"] = "Policy name is empty. You cannot proceed.";
    } else if (!inputIsValidAgainstLength(policyName, 1, 30)) {
        validationStatus["error"] = true;
        validationStatus["mainErrorMsg"] =
            "Policy name exceeds maximum allowed length.";
    } else {
        validationStatus["error"] = false;
    }
    // ending validation process

    // start taking specific actions upon validation
    var wizardIsToBeContinued;
    if (validationStatus["error"]) {
        wizardIsToBeContinued = false;
        var mainErrorMsgWrapper = "#policy-naming-main-error-msg";
        var mainErrorMsg = mainErrorMsgWrapper + " span";
        $(mainErrorMsg).text(validationStatus["mainErrorMsg"]);
        $(mainErrorMsgWrapper).removeClass("hidden");
    } else {
        wizardIsToBeContinued = true;
    }

    return wizardIsToBeContinued;
};

stepForwardFrom["policy-naming-publish"] = function () {
    policy["policyName"] = $("#policy-name-input").val();
    policy["description"] = $("#policy-description-input").val();
    //All data is collected. Policy can now be updated.
    updatePolicy(policy, "publish");
};
stepForwardFrom["policy-naming"] = function () {
    policy["policyName"] = $("#policy-name-input").val();
    policy["description"] = $("#policy-description-input").val();
    //All data is collected. Policy can now be updated.
    updatePolicy(policy, "save");
};

// End of functions related to grid-input-view

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

var updatePolicy = function (policy, state) {
    var profilePayloads;
    if (hasPolicyProfileScript) {
        /*
         generateProfileFeaturesList() should be implemented in the plugin side and should include logic to build the
         profilePayloads array which contains objects, {featureCode:"value", deviceType:"value", content:"value"}.
         policy["profile"] object will be available for the method which returns from the generatePolicyProfile() function.
         */
        profilePayloads = generateProfileFeaturesList();

        $.each(profilePayloads, function (i, item) {
            $.each(item.content, function (key, value) {
                //cannot add a true check since it will catch value = false as well
                if (value === null || value === undefined || value === "") {
                    item.content[key] = null;
                }
            });
        });
    } else {
        profilePayloads = generateGenericPayload();
    }

    var payload = {
        "policyName": policy["policyName"],
        "description": policy["description"],
        "compliance": policy["selectedNonCompliantAction"],
        "ownershipType": null,
        "profile": {
            "profileName": policy["policyName"],
            "deviceType": policy["platform"],
            "profileFeaturesList": profilePayloads
        }
    };

    if (policy["selectedUsers"]) {
        payload["users"] = policy["selectedUsers"];
        payload["roles"] = [];
    } else if (policy["selectedUserRoles"]) {
        payload["users"] = [];
        payload["roles"] = policy["selectedUserRoles"];
    } else {
        payload["users"] = [];
        payload["roles"] = [];
    }

    if (policy["selectedGroups"] && policy["selectedGroups"][0] !== "NONE") {
        payload["deviceGroups"] = policy["selectedGroups"];
    }

    var serviceURL = "/api/device-mgt/v1.0/policies/" + getParameterByName("id");
    invokerUtil.put(
        serviceURL,
        payload,
        // on success
        function (data, textStatus, jqXHR) {
            if (jqXHR.status == 200) {
                var policyList = [];
                policyList.push(getParameterByName("id"));
                if (state == "save") {
                    serviceURL = "/api/device-mgt/v1.0/policies/deactivate-policy";
                    invokerUtil.post(
                        serviceURL,
                        policyList,
                        // on success
                        function (data, textStatus, jqXHR) {
                            if (jqXHR.status == 200) {
                                $(".add-policy").addClass("hidden");
                                $(".policy-message").removeClass("hidden");
                            }
                        },
                        // on error
                        function (jqXHR) {
                            console.log("error in saving policy. Received error code : " + jqXHR.status);
                        }
                    );
                } else if (state == "publish") {
                    serviceURL = "/api/device-mgt/v1.0/policies/activate-policy";
                    invokerUtil.post(
                        serviceURL,
                        policyList,
                        // on success
                        function (data, textStatus, jqXHR) {
                            if (jqXHR.status == 200) {
                                $(".add-policy").addClass("hidden");
                                $(".policy-naming").addClass("hidden");
                                $(".policy-message").removeClass("hidden");
                            }
                        },
                        // on error
                        function (jqXHR) {
                            console.log("error in publishing policy. Received error code : " + jqXHR.status);
                        }
                    );
                }
            }
        },
        // on error
        function (jqXHR) {
            console.log("error in updating policy. Received error code : " + jqXHR.status);
        }
    );
};

$(document).ready(function () {
    // Adding initial state of wizard-steps.
    invokerUtil.get(
        "/api/device-mgt/v1.0/policies/" + getParameterByName("id"),
        // on success
        function (data, textStatus, jqXHR) {
            if (jqXHR.status == 200 && data) {
                var policy = JSON.parse(data);
                skipStep["policy-platform"](policy);
            }
        },
        // on error
        function (jqXHR) {
            console.log(jqXHR);
            // should be redirected to an error page
        }
    );

    $("input[type='radio'].select-users-radio").change(function () {
        if ($("#users-radio-btn").is(":checked")) {
            $("#user-roles-select-field").hide();
            $("#users-select-field").show();
        }
        if ($("#user-roles-radio-btn").is(":checked")) {
            $("#users-select-field").hide();
            $("#user-roles-select-field").show();
        }
    });

    // Support for special input type "ANY" on user(s) & user-role(s) selection
    $("#user-roles-input").select2({
        "tags": false
    }).on("select2:select", function (e) {
        if (e.params.data.id == "ANY") {
            $(this).val("ANY").trigger("change");
        } else {
            $("option[value=ANY]", this).prop("selected", false).parent().trigger("change");
        }
    });

    $("#groups-input").select2({
        "tags": false
    }).on("select2:select", function (e) {
        if (e.params.data.id == "NONE") {
            $(this).val("NONE").trigger("change");
        } else {
            $("option[value=NONE]", this).prop("selected", false).parent().trigger("change");
        }
    });

    $("#users-input").select2({
        "tags": false
    }).on("select2:select", function (e) {
        if (e.params.data.id == "ANY") {
            $(this).val("ANY").trigger("change");
        } else {
            $("option[value=ANY]", this).prop("selected", false).parent().trigger("change");
        }
    });

    $("#policy-profile-wizard-steps").html($(".wr-steps").html());

    $(".wizard-stepper").click(function () {
        // button clicked here can be either a continue button or a back button.
        var currentStep = $(this).data("current");
        var validationIsRequired = $(this).data("validate");
        var wizardIsToBeContinued;

        if (validationIsRequired) {
            if (currentStep == "policy-profile") {
                wizardIsToBeContinued = validatePolicyProfile();
            } else {
                wizardIsToBeContinued = validateStep[currentStep]();
            }
        } else {
            wizardIsToBeContinued = true;
        }

        if (wizardIsToBeContinued) {
            // When moving back and forth, following code segment will
            // remove if there are any visible error-messages.
            var errorMsgWrappers = ".alert.alert-danger";
            $(errorMsgWrappers).each(
                function () {
                    if (!$(this).hasClass("hidden")) {
                        $(this).addClass("hidden");
                    }
                }
            );

            var nextStep = $(this).data("next");
            var isBackBtn = $(this).data("is-back-btn");

            // if current button is a continuation...
            if (!isBackBtn) {
                // initiate stepForwardFrom[*] functions to gather form data.
                if (stepForwardFrom[currentStep]) {
                    stepForwardFrom[currentStep](this);
                }
            } else {
                // initiate stepBackFrom[*] functions to rollback.
                if (stepBackFrom[currentStep]) {
                    stepBackFrom[currentStep]();
                }
            }

            // following step occurs only at the last stage of the wizard.
            if (!nextStep) {
                window.location.href = $(this).data("direct");
            }

            // updating next wizard step as current.
            $(".itm-wiz").each(function () {
                var step = $(this).data("step");
                if (step == nextStep) {
                    $(this).addClass("itm-wiz-current");
                } else {
                    $(this).removeClass("itm-wiz-current");
                }
            });

            // adding next update of wizard-steps.
            $("#" + nextStep + "-wizard-steps").html($(".wr-steps").html());

            // hiding current section of the wizard and showing next section.
            $("." + currentStep).addClass("hidden");
            $("." + nextStep).removeClass("hidden");
        }
    });

});