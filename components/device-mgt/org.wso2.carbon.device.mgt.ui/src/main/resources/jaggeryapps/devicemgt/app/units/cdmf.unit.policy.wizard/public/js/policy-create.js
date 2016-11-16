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

var validateStep = {};
var stepForwardFrom = {};
var stepBackFrom = {};
var policy = {};
var configuredOperations = [];
var deviceTypeLabel;

/**
 * Method to update the visibility of grouped input.
 * @param domElement HTML grouped-input element with class name "grouped-input"
 */
var updateGroupedInputVisibility = function (domElement) {
    if ($(".parent-input:first", domElement).is(":checked")) {
        if ($(".grouped-child-input:first", domElement).hasClass("disabled")) {
            $(".grouped-child-input:first", domElement).removeClass("disabled");
        }
        $(".child-input", domElement).each(function () {
            $(this).prop('disabled', false);
        });
    } else {
        if (!$(".grouped-child-input:first", domElement).hasClass("disabled")) {
            $(".grouped-child-input:first", domElement).addClass("disabled");
        }
        $(".child-input", domElement).each(function () {
            $(this).prop('disabled', true);
        });
    }
};

/**
 * Checks if provided number is valid against a range.
 *
 * @param numberInput Number Input
 * @param min Minimum Limit
 * @param max Maximum Limit
 * @returns {boolean} Returns true if input is within the specified range
 */
var inputIsValidAgainstRange = function (numberInput, min, max) {
    return (numberInput == min || (numberInput > min && numberInput < max) || numberInput == max);
};

/**
 * Checks if provided input is valid against RegEx input.
 *
 * @param regExp Regular expression
 * @param input Input string to check
 * @returns {boolean} Returns true if input matches RegEx
 */
var inputIsValidAgainstRegExp = function (regExp, input) {
    return regExp.test(input);
};

validateStep["policy-profile"] = function () {
    return true;
};

stepForwardFrom["policy-profile"] = function () {
    // updating next-page wizard title with selected platform
    $("#policy-criteria-page-wizard-title").text("ADD " + deviceTypeLabel + " POLICY");
};

stepBackFrom["policy-profile"] = function () {
    // reinitialize configuredOperations
    configuredOperations = [];
    // clearing already-loaded platform specific hidden-operations html content from the relevant div
    // so that, the wrong content would not be shown at the first glance, in case
    // the user selects a different platform
    $(".wr-advance-operations").html(
            "<div class='wr-advance-operations-init'>" +
            "<br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
            "<i class='fw fw-settings fw-spin fw-2x'></i>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;" +
            "Loading Platform Features . . ." +
            "<br>" +
            "<br>" +
            "</div>"
    );
};

stepForwardFrom["policy-criteria"] = function () {
    $("input[type='radio'].select-users-radio").each(function () {
        if ($(this).is(':radio')) {
            if ($(this).is(":checked")) {
                if ($(this).attr("id") == "users-radio-btn") {
                    policy["selectedUsers"] = $("#users-input").val();
                } else if ($(this).attr("id") == "user-roles-radio-btn") {
                    policy["selectedUserRoles"] = $("#user-roles-input").val();
                } else if ($(this).attr("id") == "groups-radio-btn") {
                    policy["selectedUserGroups"] = $("#groups-input").val();
                }
            }
        }
    });
    policy["selectedNonCompliantAction"] = $("#action-input").find(":selected").data("action");
    policy["selectedOwnership"] = $("#ownership-input").val();
    // updating next-page wizard title with selected platform
    $("#policy-naming-page-wizard-title").text("ADD " + deviceTypeLabel + " POLICY");
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
                "Policy name exceeds maximum allowed length. Please check.";
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
                "Policy name exceeds maximum allowed length. Please check.";
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
    savePolicy(policy, "publish");
};
stepForwardFrom["policy-naming"] = function () {
    policy["policyName"] = $("#policy-name-input").val();
    policy["description"] = $("#policy-description-input").val();
    //All data is collected. Policy can now be updated.
    savePolicy(policy, "save");
};

var savePolicy = function (policy, state) {
    var profilePayloads = [{
        "featureCode": "CONFIG",
        "deviceTypeId": policy["platformId"],
        "content": {"policyDefinition": window.queryEditor.getValue()}
    }];

    var payload = {
        "policyName": policy["policyName"],
        "description": policy["description"],
        "compliance": policy["selectedNonCompliantAction"],
        "ownershipType": "ANY",
        "active": (state != "save"),
        "profile": {
            "profileName": policy["policyName"],
            "deviceType": {
                "id": policy["platformId"],
                "name": policy["platform"]
            },
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

    invokerUtil.post(
            "/api/device-mgt/v1.0/policies",
            payload,
            function (response) {
                response = JSON.parse(response);
                if (response["statusCode"] == 201) {
                    $(".add-policy").addClass("hidden");
                    $(".policy-naming").addClass("hidden");
                    $(".policy-message").removeClass("hidden");
                    if (state == "publish") {
                        publishToDevice();
                    }
                }
            },
            function (err) {
                console.log(err);
            }
    );
};

function publishToDevice() {
    var payload = {
        "policyName": policy["policyName"],
        "description": policy["description"],
        "compliance": policy["selectedNonCompliantAction"],
        "ownershipType": "ANY",
        "deviceId": getParameterByName('deviceId'),
        "profile": {
            "profileName": policy["policyName"],
            "deviceType": {
                "id": policy["platformId"],
                "name": policy["platform"]
            },
            "policyDefinition": window.queryEditor.getValue(),
            "policyDescription": policy["description"]
        }
    };

    var successCallback =  function (data, status) {
        console.log("Data: " + data + "\nStatus: " + status);
    };

    var data = {
        url: "/devicemgt/api/policies/add",
        type: "POST",
        contentType: "application/json",
        accept: "application/json",
        success: successCallback,
        data: JSON.stringify(payload)
    };

    $.ajax(data).fail(function (jqXHR) {
        console.log("Error: " + jqXHR);
    });

}

// Start of functions related to grid-input-view

/**
 * Method to set count id to cloned elements.
 * @param {object} addFormContainer
 */
var setId = function (addFormContainer) {
    $(addFormContainer).find("[data-add-form-clone]").each(function (i) {
        $(this).attr("id", $(this).attr("data-add-form-clone").slice(1) + "-" + (i + 1));
        if ($(this).find(".index").length > 0) {
            $(this).find(".index").html(i + 1);
        }
    });
};

/**
 * Method to set count id to cloned elements.
 * @param {object} addFormContainer
 */
var showHideHelpText = function (addFormContainer) {
    var helpText = "[data-help-text=add-form]";
    if ($(addFormContainer).find("[data-add-form-clone]").length > 0) {
        $(addFormContainer).find(helpText).hide();
    } else {
        $(addFormContainer).find(helpText).show();
    }
};

function formatRepo(user) {
    if (user.loading) {
        return user.text
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

function formatGroupRepo(group) {
    if (group.loading) {
        return group.text
    }
    if (!group.name) {
        return;
    }
    var markup = '<div class="clearfix">' +
                 '<div clas="col-sm-8">' +
                 '<div class="clearfix">' +
                 '<div class="col-sm-3">' + group.name + '</div>';
    if (group.name) {
        markup += '<div class="col-sm-3"><i class="fa fa-code-fork"></i> ' + group.name + '</div>';
    }
    if (group.owner) {
        markup += '<div class="col-sm-2"><i class="fa fa-star"></i> ' + group.owner + '</div></div>';
    }
    markup += '</div></div>';
    return markup;
}

function formatGroupRepoSelection(group) {
    return group.name || group.text;
}

// End of functions related to grid-input-view


$(document).ready(function () {
    window.queryEditor = CodeMirror.fromTextArea(document.getElementById('policy-definition-input'), {
        mode: MIME_TYPE_SIDDHI_QL,
        indentWithTabs: true,
        smartIndent: true,
        lineNumbers: true,
        matchBrackets: true,
        autofocus: true,
        extraKeys: {
            "Shift-2": function (cm) {
                insertStr(cm, cm.getCursor(), '@');
                CodeMirror.showHint(cm, getAnnotationHints);
            },
            "Ctrl-Space": "autocomplete"
        }
    });

    $("#users-input").select2({
                                  multiple: true,
                                  tags: true,
                                  ajax: {
                                      url: window.location.origin + "/devicemgt/api/invoker/execute/",
                                      method: "POST",
                                      dataType: 'json',
                                      delay: 250,
                                      id: function (user) {
                                          return user.username;
                                      },
                                      data: function (params) {
                                          var postData = {};
                                          postData.actionMethod = "GET";
                                          postData.actionUrl = "/devicemgt_admin/users";
                                          postData.actionPayload = JSON.stringify({
                                                                                      q: params.term, // search term
                                                                                      page: params.page
                                                                                  });

                                          return JSON.stringify(postData);
                                      },
                                      processResults: function (data, page) {
                                          var newData = [];
                                          $.each(data.responseContent, function (index, value) {
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

    $("#groups-input").select2({
                                  multiple: true,
                                  tags: true,
                                  ajax: {
                                      url: window.location.origin + "/devicemgt/api/invoker/execute/",
                                      method: "POST",
                                      dataType: 'json',
                                      delay: 250,
                                      id: function (group) {
                                          return group.name;
                                      },
                                      data: function (params) {
                                          var postData = {};
                                          postData.actionMethod = "GET";
                                          var username = $("#platform").data("username");
                                          postData.actionUrl = "/devicemgt_admin/groups/user/" + username +
                                                               "/search?groupName=" + params.term;
                                          return JSON.stringify(postData);
                                      },
                                      processResults: function (data, page) {
                                          var newData = [];
                                          $.each(data, function (index, value) {
                                              value.id = value.name;
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
                                  templateResult: formatGroupRepo, // omitted for brevity, see the source of this page
                                  templateSelection: formatGroupRepoSelection // omitted for brevity, see the source of this page
                              });

    // Adding initial state of wizard-steps.
    $("#policy-profile-wizard-steps").html($(".wr-steps").html());

    policy["platform"] = $("#platform").data("platform");
    policy["platformId"] = $("#platform").data("platform-id");
    deviceTypeLabel = $("#platform").data("platform-label");
    // updating next-page wizard title with selected platform
    $("#policy-profile-page-wizard-title").text("ADD " + deviceTypeLabel + " POLICY");

    $("select.select2[multiple=multiple]").select2({
        "tags": true
    });

    $("#users-select-field").hide();
    $("#groups-select-field").hide();
    $("#user-roles-select-field").show();

    $("input[type='radio'].select-users-radio").change(function () {
        if ($("#user-roles-radio-btn").is(":checked")) {
            $("#user-roles-select-field").show();
            $("#users-select-field").hide();
            $("#groups-select-field").hide();
        }
        if ($("#users-radio-btn").is(":checked")) {
            $("#user-roles-select-field").hide();
            $("#users-select-field").show();
            $("#groups-select-field").hide();
        }
        if ($("#groups-radio-btn").is(":checked")) {
            $("#user-roles-select-field").hide();
            $("#users-select-field").hide();
            $("#groups-select-field").show();
        }
    });

    // Support for special input type "ANY" on user(s) & user-role(s) selection
    $("#user-roles-input").select2({
        "tags": true
    }).on("select2:select", function (e) {
        if (e.params.data.id == "ANY") {
            $(this).val("ANY").trigger("change");
        } else {
            $("option[value=ANY]", this).prop("selected", false).parent().trigger("change");
        }
    });

    // Maintains an array of configured features of the profile
    var advanceOperations = ".wr-advance-operations";
    $(advanceOperations).on("click", ".wr-input-control.switch", function (event) {
        var operationCode = $(this).parents(".operation-data").data("operation-code");
        var operation = $(this).parents(".operation-data").data("operation");
        var operationDataWrapper = $(this).data("target");
        // prevents event bubbling by figuring out what element it's being called from.
        if (event.target.tagName == "INPUT") {
            var featureConfiguredIcon;
            if ($("input[type='checkbox']", this).is(":checked")) {
                configuredOperations.push(operationCode);
                // when a feature is enabled, if "zero-configured-features" msg is available, hide that.
                var zeroConfiguredOperationsErrorMsg = "#policy-profile-main-error-msg";
                if (!$(zeroConfiguredOperationsErrorMsg).hasClass("hidden")) {
                    $(zeroConfiguredOperationsErrorMsg).addClass("hidden");
                }
                // add configured-state-icon to the feature
                featureConfiguredIcon = "#" + operation + "-configured";
                if ($(featureConfiguredIcon).hasClass("hidden")) {
                    $(featureConfiguredIcon).removeClass("hidden");
                }
            } else {
                //splicing the array if operation is present.
                var index = $.inArray(operationCode, configuredOperations);
                if (index != -1) {
                    configuredOperations.splice(index, 1);
                }
                // when a feature is disabled, clearing all its current configured, error or success states
                var subErrorMsgWrapper = "#" + operation + "-feature-error-msg";
                var subErrorIcon = "#" + operation + "-error";
                var subOkIcon = "#" + operation + "-ok";
                featureConfiguredIcon = "#" + operation + "-configured";

                if (!$(subErrorMsgWrapper).hasClass("hidden")) {
                    $(subErrorMsgWrapper).addClass("hidden");
                }
                if (!$(subErrorIcon).hasClass("hidden")) {
                    $(subErrorIcon).addClass("hidden");
                }
                if (!$(subOkIcon).hasClass("hidden")) {
                    $(subOkIcon).addClass("hidden");
                }
                if (!$(featureConfiguredIcon).hasClass("hidden")) {
                    $(featureConfiguredIcon).addClass("hidden");
                }
                // reinitializing input fields into the defaults
                $(operationDataWrapper + " input").each(
                        function () {
                            if ($(this).is("input:text")) {
                                $(this).val($(this).data("default"));
                            } else if ($(this).is("input:password")) {
                                $(this).val("");
                            } else if ($(this).is("input:checkbox")) {
                                $(this).prop("checked", $(this).data("default"));
                                // if this checkbox is the parent input of a grouped-input
                                if ($(this).hasClass("parent-input")) {
                                    var groupedInput = $(this).parent().parent().parent();
                                    updateGroupedInputVisibility(groupedInput);
                                }
                            }
                        }
                );
                // reinitializing select fields into the defaults
                $(operationDataWrapper + " select").each(
                        function () {
                            var defaultOption = $(this).data("default");
                            $("option:eq(" + defaultOption + ")", this).prop("selected", "selected");
                        }
                );
                // collapsing expanded-panes (upon the selection of html-select-options) if any
                $(operationDataWrapper + " .expanded").each(
                        function () {
                            if ($(this).hasClass("expanded")) {
                                $(this).removeClass("expanded");
                            }
                            $(this).slideUp();
                        }
                );
                // removing all entries of grid-input elements if exist
                $(operationDataWrapper + " .grouped-array-input").each(
                        function () {
                            var gridInputs = $(this).find("[data-add-form-clone]");
                            if (gridInputs.length > 0) {
                                gridInputs.remove();
                            }
                            var helpTexts = $(this).find("[data-help-text=add-form]");
                            if (helpTexts.length > 0) {
                                helpTexts.show();
                            }
                        }
                );
            }
        }
    });

    // adding support for cloning multiple profiles per feature with cloneable class definitions
    $(advanceOperations).on("click", ".multi-view.add.enabled", function () {
        // get a copy of .cloneable and create new .cloned div element
        var cloned = "<div class='cloned'><hr>" + $(".cloneable", $(this).parent().parent()).html() + "</div>";
        // append newly created .cloned div element to panel-body
        $(this).parent().parent().append(cloned);
        // enable remove action of newly cloned div element
        $(".cloned", $(this).parent().parent()).each(
                function () {
                    if ($(".multi-view.remove", this).hasClass("disabled")) {
                        $(".multi-view.remove", this).removeClass("disabled");
                    }
                    if (!$(".multi-view.remove", this).hasClass("enabled")) {
                        $(".multi-view.remove", this).addClass("enabled");
                    }
                }
        );
    });

    $(advanceOperations).on("click", ".multi-view.remove.enabled", function () {
        $(this).parent().remove();
    });

    // enabling or disabling grouped-input based on the status of a parent check-box
    $(advanceOperations).on("click", ".grouped-input", function () {
        updateGroupedInputVisibility(this);
    });

    // add form entry click function for grid inputs
    $(advanceOperations).on("click", "[data-click-event=add-form]", function () {
        var addFormContainer = $("[data-add-form-container=" + $(this).attr("href") + "]");
        var clonedForm = $("[data-add-form=" + $(this).attr("href") + "]").clone().
                find("[data-add-form-element=clone]").attr("data-add-form-clone", $(this).attr("href"));

        // adding class .child-input to capture text-input-array-values
        $("input, select", clonedForm).addClass("child-input");

        $(addFormContainer).append(clonedForm);
        setId(addFormContainer);
        showHideHelpText(addFormContainer);
    });

    // remove form entry click function for grid inputs
    $(advanceOperations).on("click", "[data-click-event=remove-form]", function () {
        var addFormContainer = $("[data-add-form-container=" + $(this).attr("href") + "]");

        $(this).closest("[data-add-form-element=clone]").remove();
        setId(addFormContainer);
        showHideHelpText(addFormContainer);
    });

    $(".wizard-stepper").click(function () {
        // button clicked here can be either a continue button or a back button.
        var currentStep = $(this).data("current");
        var validationIsRequired = $(this).data("validate");
        var wizardIsToBeContinued;

        if (validationIsRequired) {
            wizardIsToBeContinued = validateStep[currentStep]();
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
            if (nextStep !== "policy-message") {
                $("." + nextStep).removeClass("hidden");
            }
        }
    });
});

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
            results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}