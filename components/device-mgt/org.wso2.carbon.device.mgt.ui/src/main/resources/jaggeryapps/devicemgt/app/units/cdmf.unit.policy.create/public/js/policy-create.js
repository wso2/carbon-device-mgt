/*
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

var stepForwardFrom = {};
var stepBackFrom = {};
var policy = {};
var validateInline = {};
var clearInline = {};
var validateStep = {};
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
            if(deviceGroups.hasOwnProperty(index)) {
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
    disableInlineError("policyNameField", "nameEmpty", "nameError");
};


/**
 * Validate if provided policy name is valid against RegEx configures.
 */
validateInline["policy-name"] = function () {
    var policyName = $("input#policy-name-input").val();
    if (policyName && inputIsValidAgainstLength(policyName, 1, 30)) {
        disableInlineError("policyNameField", "nameEmpty", "nameError");
    } else {
        enableInlineError("policyNameField", "nameEmpty", "nameError");
    }
};

$("#policy-name-input").focus(function(){
    clearInline["policy-name"]();
}).blur(function(){
    validateInline["policy-name"]();
});

/**
 * Forward action of device type selection step. Loads relevant policy profile configurations.
 *
 * @param actionButton
 */
stepForwardFrom["policy-platform"] = function (actionButton) {
    $("#device-type-policy-operations").html("").addClass("hidden");
    $("#generic-policy-operations").addClass("hidden");
    policy["platform"] = $(actionButton).data("platform");
    policy["platformId"] = $(actionButton).data("platform-type");
    // updating next-page wizard title with selected platform
    $("#policy-profile-page-wizard-title").text("ADD " + policy["platform"] + " POLICY");

    var deviceType = policy["platform"];
    var policyOperationsTemplateSrc = $(actionButton).data("template");
    var policyOperationsScriptSrc = $(actionButton).data("script");
    var policyOperationsStylesSrc = $(actionButton).data("style");
    var policyOperationsTemplateCacheKey = deviceType + '-policy-operations';

    if (policyOperationsTemplateSrc) {
        $.template(policyOperationsTemplateCacheKey, context + policyOperationsTemplateSrc, function (template) {
            var content = template();
            $("#device-type-policy-operations").html(content).removeClass("hidden");
            $(".policy-platform").addClass("hidden");
        });
    } else {
        $("#generic-policy-operations").removeClass("hidden");
    }
    if (policyOperationsScriptSrc) {
        var script = document.createElement('script');
        script.type = 'text/javascript';
        script.src = context + policyOperationsScriptSrc;
        $(".wr-advance-operations").prepend(script);
        hasPolicyProfileScript = true;
    } else {
        hasPolicyProfileScript = false;
    }
    if (policyOperationsStylesSrc) {
        var style = document.createElement('link');
        style.type = 'text/css';
        style.rel = 'stylesheet';
        style.href = context + policyOperationsStylesSrc;
        $(".wr-advance-operations").prepend(style);
    }
    $(".wr-advance-operations-init").addClass("hidden");
};

/**
 * Forward action of policy profile page. Generates policy profile payload.
 */
stepForwardFrom["policy-profile"] = function () {
    policy["profile"] = [];
    if (hasPolicyProfileScript) {
        /*
         generatePolicyProfile() function should be implemented in plugin side and should include the logic to build the
         policy profile object.
         */
        policy["profile"] = generatePolicyProfile();
    }
    // updating next-page wizard title with selected platform
    $("#policy-criteria-page-wizard-title").text("ADD " + policy["platform"] + " POLICY");
};

/**
 * Backward action of policy profile page. Moves back to platform selection step.
 */
stepBackFrom["policy-profile"] = function () {
    if (hasPolicyProfileScript) {
        /*
         resetPolicyProfile() function should be implemented in plugin side and should include the logic to reset the policy
         profile object.
         */
        resetPolicyProfile();
    }
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
    //updating next-page wizard title with selected platform
    $("#policy-naming-page-wizard-title").text("ADD " + policy["platform"] + " POLICY");
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

validateStep["policy-platform"] = function () {
    return false;
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
    savePolicy(policy, true, "/api/device-mgt/v1.0/policies/");
};

stepForwardFrom["policy-naming"] = function () {
    policy["policyName"] = $("#policy-name-input").val();
    policy["description"] = $("#policy-description-input").val();
    //All data is collected. Policy can now be updated.
    savePolicy(policy, false, "/api/device-mgt/v1.0/policies/");
};

var savePolicy = function (policy, isActive, serviceURL) {
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
        "active": isActive,
        "profile": {
            "profileName": policy["policyName"],
            "deviceType": policy["platform"],
            "profileFeaturesList": profilePayloads
        }
    };

    if (policy["selectedUsers"]) {
        payload["users"] = policy["selectedUsers"];
    } else if (policy["selectedUserRoles"]) {
        payload["roles"] = policy["selectedUserRoles"];
    } else {
        payload["users"] = [];
        payload["roles"] = [];
    }

    if(policy["selectedGroups"] && policy["selectedGroups"][0] !== "NONE") {
        payload["deviceGroups"] = policy["selectedGroups"];
    }

    invokerUtil.post(
        serviceURL,
        payload,
        function () {
            $(".add-policy").addClass("hidden");
            $(".policy-naming").addClass("hidden");
            $(".policy-message").removeClass("hidden");
        },
        function (data) {
        }
    );
};

function formatRepo(user) {
    if (user.loading) {
        return user.text;
    }
    if (!user.username) {
        return;
    }
    var markup = '<div class="clearfix">' +
        '<div clas="col-sm-8">' +
        '<div class="clearfix">' +
        '<div class="col-sm-3">' + user.username + '</div>';
    if (user.firstname) {
        markup += '<div class="col-sm-3"><i class="fa fa-code-fork"></i> ' + user.firstname + '</div>';
    }
    if (user.emailAddress) {
        markup += '<div class="col-sm-2"><i class="fa fa-star"></i> ' + user.emailAddress + '</div></div>';
    }
    markup += '</div></div>';
    return markup;
}

function formatRepoSelection(user) {
    return user.username || user.text;
}

// End of functions related to grid-input-view


$(document).ready(function () {
    $("#users-input").select2({
        multiple: true,
        tags: false,
        ajax: {
            url: context + "/api/invoker/execute/",
            method: "POST",
            dataType: 'json',
            delay: 250,
            id: function (user) {
                return user.username;
            },
            data: function (params) {
                var postData = {};
                postData.requestMethod = "GET";
                postData.requestURL = "/api/device-mgt/v1.0/users/search/usernames?filter=" + params.term;
                postData.requestPayload = null;
                return JSON.stringify(postData);
            },
            processResults: function (data) {
                var newData = [];
                $.each(data, function (index, value) {
                    value.id = value.username;
                    newData.push(value);
                });
                return {
                    results: newData
                };
            },
            cache: true
        },
        escapeMarkup: function (markup) {
            return markup;
        }, // let our custom formatter work
        minimumInputLength: 1,
        templateResult: formatRepo, // omitted for brevity, see the source of this page
        templateSelection: formatRepoSelection // omitted for brevity, see the source of this page
    });

    $("#loading-content").remove();
    $(".policy-platform").removeClass("hidden");
    // Adding initial state of wizard-steps.
    $("#policy-platform-wizard-steps").html($(".wr-steps").html());

    $("select.select2[multiple=multiple]").select2({
        "tags": false
    });

    $("#users-select-field").hide();
    $("#user-roles-select-field").show();

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

    //Policy wizard stepper
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