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

var apiBasePath = "/api/device-mgt/v1.0";
var domain = $("#domain").val();
var isCloud = $("#role-create-form").data("cloud");


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
    disableInlineError("roleNameField", "roleNameEmpty", "roleNameError");
};


/**
 * Validate if provided role-name is valid against RegEx configures.
 */
validateInline["role-name"] = function () {
    var roleNameInput = $("input#roleName");
    var roleName = roleNameInput.val();
    if (inputIsValid(roleNameInput.data("regex"), roleName) && roleName.indexOf("@") < 0 && roleName.indexOf("/") < 0) {
        disableInlineError("roleNameField", "roleNameEmpty", "roleNameError");
    } else {
        enableInlineError("roleNameField", "roleNameEmpty", "roleNameError");
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
        '<div class="col-sm-8">' +
        '<div class="clearfix">' +
        '<div class="col-sm-4">' + user.username + '</div>';
    if (user.name || user.name != undefined) {
        markup += '<div class="col-sm-8"> ( ' + user.name + ' )</div>';
    }
    markup += '</div></div></div>';
    return markup;
}

function formatRepoSelection(user) {
    return user.username || user.text;
}

$(document).ready(function () {
    isCloud = $("#role-create-form").data("cloud");

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
                postData.requestMethod = "GET";
                postData.requestURL = "/api/device-mgt/v1.0/users/search/usernames?filter=" + params.term +
                    "&domain=" + encodeURIComponent(domain);
                postData.requestPayload = null;
                return JSON.stringify(postData);
            },
            processResults: function (data) {
                var newData = [];
                $.each(data, function (index, value) {
                    var user = {};
                    user.id = value.username;
                    user.username = value.username;
                    if (value.firstname && value.lastname) {
                        user.name = value.firstname + " " + value.lastname;
                    }
                    newData.push(user);
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

        var domain = $("#domain").val();
        var roleNameInput = $("input#roleName");
        var roleName = roleNameInput.val();
        var users = $("#users").val();

        var errorMsgWrapper = "#role-create-error-msg";
        var errorMsg = "#role-create-error-msg span";
        if (!roleName) {
            $(errorMsg).text("Role name is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!inputIsValid(roleNameInput.data("regex"), roleName) || roleName.indexOf("@") >= 0 ||
            roleName.indexOf("/") >= 0) {
            $(errorMsg).text(roleNameInput.data("error-msg"));
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
            if (domain != "PRIMARY") {
                addRoleFormData.roleName = domain + "/" + roleName;
            }
            if (users == null) {
                users = [];
            }
            addRoleFormData.users = users;

            var addRoleAPI = apiBasePath + "/roles";

            invokerUtil.post(
                addRoleAPI,
                addRoleFormData,
                function (data, textStatus, jqXHR) {
                    if (jqXHR.status == 201) {
                        // Clearing user input fields.
                        $("input#roleName").val("");
                        $("#domain").val("PRIMARY");
                        $("#users").val("");
                        window.location.href = appContext + "/role/edit-permission/?rolename=" +
                            encodeURIComponent(addRoleFormData.roleName);
                    }
                },
                function (jqXHR) {
                    if (jqXHR.status == 500) {
                        $(errorMsg).text("Either role already exists or unexpected error.");
                        $(errorMsgWrapper).removeClass("hidden");
                    }
                }
            );
        }
    });

    var roleNameInputElement = "#roleName";
    $(roleNameInputElement).focus(function () {
        clearInline["role-name"]();
    });

    $(roleNameInputElement).blur(function () {
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