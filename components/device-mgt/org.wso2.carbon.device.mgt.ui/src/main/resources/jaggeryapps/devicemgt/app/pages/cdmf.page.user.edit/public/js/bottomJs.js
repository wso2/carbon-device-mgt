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
 * Checks if provided input is valid against RegEx input.
 *
 * @param regExp Regular expression
 * @param inputString Input string to check
 * @returns {boolean} Returns true if input matches RegEx
 */
function inputIsValid(regExp, inputString) {
    regExp = new RegExp(regExp);
    return regExp.test(inputString);
}

//holds the list of inline validation methods
var validateInline = {};
var clearInline = {};

var deviceMgtBasePath = "/api/device-mgt/v1.0";

var enableInlineError = function(inputField, errorMsg, errorSign) {
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

var disableInlineError = function(inputField, errorMsg, errorSign) {
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
 * Validate if provided username is valid against RegEx configures.
 */
validateInline["user-name"] = function() {
    var usernameinput = $("input#username");
    if (inputIsValid(usernameinput.data("regex"), usernameinput.val())) {
        disableInlineError("usernameInputField", "usernameEmpty", "usernameError");
    } else {
        enableInlineError("usernameInputField", "usernameEmpty", "usernameError");
    }
};

/**
 * Validate if provided first name is valid against RegEx configures.
 */
validateInline["first-name"] = function() {
    var firstnameinput = $("input#firstname");
    if (firstnameinput.val()) {
        disableInlineError("firstNameField", "fnError");
    } else {
        enableInlineError("firstNameField", "fnError");
    }
};

/**
 * Validate if provided last name is valid against RegEx configures.
 */
validateInline["last-name"] = function() {
    var lastnameinput = $("input#lastname");
    if (lastnameinput.val()) {
        disableInlineError("lastNameField", "lnError");
    } else {
        enableInlineError("lastNameField", "lnError");
    }
};

/**
 * Checks if provided email address is valid against
 * the email format.
 */
validateInline["emailAddress"] = function() {
    var email = $("#emailAddress").val();
    if (!email) {
        enableInlineError("emailField", "email-required", "emailError");
    } else if (emailIsValid(email)) {
        disableInlineError("emailField", "email-required", "emailError");
        disableInlineError("emailField", "email-invalid", "emailError");
    } else {
        enableInlineError("emailField", "email-invalid", "emailError");
    }
};

/**
 * clear Validation messages when gain focus to the field.
 */
clearInline["user-name"] = function() {
    disableInlineError("usernameInputField", "usernameEmpty", "usernameError");
};

/**
 * clear Validation messages when gain focus to the field.
 */
clearInline["first-name"] = function() {
    disableInlineError("firstNameField", "fnError");
};

/**
 * clear Validation messages when gain focus to the field.
 */
clearInline["last-name"] = function() {
    disableInlineError("lastNameField", "lnError");
};


/**
 * clear Validation messages when gain focus to the field.
 */
clearInline["emailAddress"] = function() {
    disableInlineError("emailField", "email-required", "emailError");
    disableInlineError("emailField", "email-invalid", "emailError");
};

/**
 * Checks if an email address has the valid format or not.
 *
 * @param email Email address
 * @returns {boolean} true if email has the valid format, otherwise false.
 */
function emailIsValid(email) {
    var regExp = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
    return regExp.test(email);
}

$(document).ready(function() {
    $('#loading-content').show();
    $("#emailValidationText").hide();
    $("select.select2[multiple=multiple]").select2({
        tags: false
    });

      $('ul.nav a').each(function() {
        var url = this.href;
        if (url.indexOf("/devicemgt/users") !== -1) {
            $(this).addClass('active');
        }
    });

    var roleList, roleArr;
    if ($("#roles").length > 0) {
        roleList = $("#roles").attr("selectedVals").trim().replace(/ /g, "");
        roleList = roleList.replace(/(\r\n|\n|\r)/gm, "");
        roleArr = roleList.split(",");
    }

    if ($("#roles").length > 0) {
        $("#roles").val(roleArr).trigger("change");
    }
    /**
     * Following click function would execute
     * when a user clicks on "Add User" button
     * on Add User page in WSO2 Devicemgt Console.
     */
    $("button#add-user-btn").click(function() {
        var usernameInput = $("input#username");
        var firstnameInput = $("input#firstname");
        var lastnameInput = $("input#lastname");
        // var charLimit = parseInt($("input#username").attr("limit"));
        var domain = 'PRIMARY';
        var username = usernameInput.val().trim();
        var firstname = firstnameInput.val();
        var lastname = lastnameInput.val();
        var emailAddress = $("input#emailAddress").val();
        var roles;
        if ($("#roles").length > 0) {
            var roles = $("select#roles").val();
        }
        var errorMsgWrapper = "#user-create-error-msg";
        var errorMsg = "#user-create-error-msg span";
        if (!username) {
            $(errorMsg).text("Username is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!inputIsValid(usernameInput.data("regex"), username)) {
            $(errorMsg).text(usernameInput.data("errormsg"));
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!firstname) {
            $(errorMsg).text("Firstname is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!inputIsValid(firstnameInput.data("regex"), firstname)) {
            $(errorMsg).text(firstnameInput.data("errormsg"));
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!lastname) {
            $(errorMsg).text("Lastname is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!inputIsValid(lastnameInput.data("regex"), lastname)) {
            $(errorMsg).text(lastnameInput.data("errormsg"));
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!emailAddress) {
            $(errorMsg).text("Email is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!emailIsValid(emailAddress)) {
            $(errorMsg).text("Provided email is invalid.");
            $(errorMsgWrapper).removeClass("hidden");
        } else {
            var addUserFormData = {};

            addUserFormData.username = domain + "/" + username;
            addUserFormData.firstname = firstname;
            addUserFormData.lastname = lastname;
            addUserFormData.emailAddress = emailAddress;

            if (!roles) {
                roles = [];
            }
            addUserFormData.roles = roles;
            username = username.substr(username.indexOf('/') + 1);
            var addUserAPI = deviceMgtBasePath + "/users/" + encodeURIComponent(username) + "?domain=" +
                encodeURIComponent(domain);

            invokerUtil.put(
                addUserAPI,
                addUserFormData,
                function(data, textStatus, jqXHR) {
                    if (jqXHR.status == 200) {
                        // Clearing user input fields.
                        // Refreshing with success message
                        $("#user-create-form").addClass("hidden");
                        $("#user-created-msg").removeClass("hidden");
                    }
                },
                function(jqXHR) {
                    var payload = JSON.parse(jqXHR.responseText);
                    if (jqXHR.status == 409) {
                        $(errorMsg).text("User : " + username + " doesn't exists. You cannot proceed.");
                    } else if (jqXHR.status == 500) {
                        $(errorMsg).text("An unexpected error occurred at backend server. Please try again later.");
                    } else {
                        $(errorMsg).text(payload.message);
                    }
                    $(errorMsgWrapper).removeClass("hidden");
                }
            );
        }
    });

    $("#emailAddress").focus(function() {
        clearInline["emailAddress"]();
    });

    $("#emailAddress").blur(function() {
        validateInline["emailAddress"]();
    });

    $("#lastname").focus(function() {
        clearInline["last-name"]();
    });

    $("#lastname").blur(function() {
        validateInline["last-name"]();
    });

    $("#firstname").focus(function() {
        clearInline["first-name"]();
    });

    $("#firstname").blur(function() {
        validateInline["first-name"]();
    });

    $('#loading-content').remove();
});