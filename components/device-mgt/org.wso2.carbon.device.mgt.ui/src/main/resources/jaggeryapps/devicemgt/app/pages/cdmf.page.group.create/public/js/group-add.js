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

$(function () {
    $("button#add-group-btn").click(function () {

        var name = $("input#name").val();
        var description = $("input#description").val();

        if (!name) {
            $('.wr-validation-summary strong').text("Group Name is a required field. It cannot be empty.");
            $('.wr-validation-summary').removeClass("hidden");
            return false;
        } else if (!inputIsValid($("input#name").data("regex"), name)) {
            $('.wr-validation-summary strong').text($("input#name").data("errormsg"));
            $('.wr-validation-summary').removeClass("hidden");
            return false;
        } else {
            var group = {"name": name, "description": description};

            var successCallback = function (jqXHR, status, resp) {
                if (resp.status == 201) {
                    $('.wr-validation-summary strong').text("Group created. You will be redirected to groups");
                    $('.wr-validation-summary').removeClass("hidden");
                    $('.wr-validation-summary strong').removeClass("label-danger");
                    $('.wr-validation-summary strong').addClass("label-success");
                    setTimeout(function () {
                        window.location = "../groups";
                    }, 1500);
                } else {
                    displayErrors(resp.status);
                }
            };

            invokerUtil.post("/devicemgt_admin/groups", group,
                             successCallback, function (message) {
                        displayErrors(message);
                    });

            return false;
        }
    });
});

function displayErrors(message) {
    showPopup();
    $('#error-msg').html(message.responseText);
    $(modalPopupContent).html($('#group-error-content').html());
    $("a#group-unexpected-error-link").click(function () {
        hidePopup();
    });
}
