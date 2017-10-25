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

var validateInline = {};
var clearInline = {};
var domain = $("#domain").val();

var apiBasePath = "/api/device-mgt/v1.0";

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
 *clear inline validation messages.
 */
clearInline["role-name"] = function () {
    disableInlineError("roleNameField", "rolenameEmpty", "rolenameError");
};


/**
 * Validate if provided rolename is valid against RegEx configures.
 */
validateInline["role-name"] = function () {
    var rolenameinput = $("input#rolename");
    if (inputIsValid(rolenameinput.data("regex"), rolenameinput.val())) {
        disableInlineError("roleNameField", "rolenameEmpty", "rolenameError");
    } else {
        enableInlineError("roleNameField", "rolenameEmpty", "rolenameError");
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

$(document).ready(function () {
    var appContext = $("#app-context").data("app-context");
    $("#users").select2({
        multiple: true,
        tags: false,
        ajax: {
            url: appContext + "/api/invoker/execute/",
            method: "POST",
            dataType: 'json',
            delay: 250,
            id: function (user) {
                return user.username;
            },
            data: function (params) {
                var postData = {};
                postData.actionMethod = "GET";
                postData.actionUrl = apiBasePath + "/users/search/usernames?filter=" + params.term + "&domain=" +
                    encodeURIComponent(domain);
                postData.actionPayload = null;
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


    /**
     * Following click function would execute
     * when a user clicks on "Add Role" button
     * on Add Role page in WSO2 MDM Console.
     */
    $("button#add-role-btn").click(function () {
        var isCloud = $("#role-create-form").data("cloud");
        var rolenameInput = $("input#rolename");
        var roleName = rolenameInput.val();
        var currentRoleName = $("input#rolename").data("currentrole");
        if (isCloud) {

            currentRoleName = "devicemgt" + currentRoleName;
        }
        var domain = $("#domain").val();
        var errorMsgWrapper = "#role-create-error-msg";
        var errorMsg = "#role-create-error-msg span";
        if (!roleName) {
            $(errorMsg).text("Role name is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!inputIsValid(rolenameInput.data("regex"), roleName)) {
            $(errorMsg).text(rolenameInput.data("errormsg"));
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!domain) {
            $(errorMsg).text("Domain is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!inputIsValid(/^[^~?!#$:;%^*`+={}\[\]\\()|<>,'"]/, domain)) {
            $(errorMsg).text("Provided domain is invalid.");
            $(errorMsgWrapper).removeClass("hidden");
        } else {
            var addRoleFormData = {};
            if (isCloud) {
                addRoleFormData.roleName = "devicemgt" + roleName;
            } else {
                addRoleFormData.roleName = roleName;
            }
            var addRoleAPI = apiBasePath + "/roles/" + encodeURIComponent(currentRoleName);
            if (domain != "PRIMARY") {
                addRoleFormData.roleName = domain + "/" + roleName;
                addRoleAPI = addRoleAPI + "?user-store=" + encodeURIComponent(domain);
            }

            invokerUtil.put(
                addRoleAPI,
                addRoleFormData,
                function (data, textStatus, jqXHR) {
                    if (jqXHR.status == 200) {
                        // Clearing user input fields.
                        $("input#rolename").val("");
                        $("#domain").val("");
                        // Refreshing with success message
                        $("#role-create-form").addClass("hidden");
                        $("#role-created-msg").removeClass("hidden");
                    }
                }, function (data) {

                    var payload = JSON.parse(data.responseText);
                    $(errorMsg).text(payload.message);
                    $(errorMsgWrapper).removeClass("hidden");
                }
            );
        }
    });

    $("#rolename").focus(function () {
        clearInline["role-name"]();
    });

    $("#rolename").blur(function () {
        validateInline["role-name"]();
    });

    /* When the user store domain value is changed, the users who are assigned to that role should be removed, as
     user and role can be mapped only if both are in same user store
     */
    $("#domain").change(function () {
        $("#users").select2("val", "");
        domain = $("#domain").val();
    });
});