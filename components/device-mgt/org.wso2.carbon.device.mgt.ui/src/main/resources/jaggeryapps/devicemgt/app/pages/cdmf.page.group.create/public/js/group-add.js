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

$(function () {
    $("button#add-group-btn").click(function () {

        var name = $("input#name").val();
        var description = $("input#description").val();

        if (!name) {
            $('.wr-validation-summary strong').text("Group Name is a required field. It cannot be empty.");
            $('.wr-validation-summary').removeClass("hidden");
            return false;
        } else {
            var group = {"name": name, "description": description};

            var successCallback = function (data) {
                if (data.status == 201) {
                    $('.wr-validation-summary strong').text("Group created. You will be redirected to groups");
                    $('.wr-validation-summary').removeClass("hidden");
                    $('.wr-validation-summary strong').removeClass("label-danger");
                    $('.wr-validation-summary strong').addClass("label-success");
                    setTimeout(function () {
                        window.location = "../groups";
                    }, 1500);
                } else {
                    displayErrors(status);
                }
            };

            invokerUtil.post("/common/group_manager/groups", group,
                             successCallback, function (message) {
                        displayErrors(message.content);
                    });

            return false;
        }
    });
});

function displayErrors(status) {
    showPopup();
    if (status == 400) {
        $(modalPopupContent).html($('#group-400-content').html());
        $("a#group-400-link").click(function () {
            hidePopup();
        });
    } else if (status == 403) {
        $(modalPopupContent).html($('#group-403-content').html());
        $("a#group-403-link").click(function () {
            hidePopup();
        });
    } else if (status == 409) {
        $(modalPopupContent).html($('#group-409-content').html());
        $("a#group-409-link").click(function () {
            hidePopup();
        });
    } else {
        $(modalPopupContent).html($('#group-unexpected-error-content').html());
        $("a#group-unexpected-error-link").click(function () {
            hidePopup();
        });
        console.log("Error code: " + status);
    }
}
