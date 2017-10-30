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

var deviceMgtAPIBaseURI = "/api/device-mgt/v1.0";

/**
 * Following function would execute
 * when a user clicks on the list item
 * initial mode and with out select mode.
 */
function InitiateViewOption() {
    $(location).attr('href', $(this).data("url"));
}

function loadNotifications() {
    var deviceListing = $("#notification-listing");
    var deviceListingSrc = deviceListing.attr("src");
    var currentUser = deviceListing.data("currentUser");
    $.template(
        "notification-listing",
        deviceListingSrc,
        function (template) {
            invokerUtil.get(
                deviceMgtAPIBaseURI + "/notifications",
                // on success
                function (data, textStatus, jqXHR) {
                    if (jqXHR.status == 200 && data) {
                        data = JSON.parse(data);
                        if (data["notifications"] && data["notifications"].length > 0) {
                            var viewModel = {};
                            viewModel["notifications"] = data["notifications"];
                            viewModel["appContext"] = context;
                            var content = template(viewModel);
                            $("#ast-container").html(content);
                            var settings = {
                                "sorting" : false
                            };
                            $("#unread-notifications").datatables_extended(settings);
                            $("#all-notifications").datatables_extended(settings);
                        }
                    }
                },
                // on error
                function (jqXHR) {
                    console.log(jqXHR.status);
                }
            );
        }
    );
}

// Start of HTML embedded invoke methods
function showAdvanceOperation(operation, button) {
    $(button).addClass('selected');
    $(button).siblings().removeClass('selected');
    if ($(button).attr("id") == 'allNotifications') {
        $("#noNotificationtxt").html('You do not have any unread notifications ');
    } else if ($(button).attr("id") == 'unReadNotifications') {
        $("#noNotificationtxt").html('You do not have any notifications ');
    } else {
        $("#noNotificationtxt").html('You do not have any notifications ');
    }
    var hiddenOperation = ".wr-hidden-operations-content > div";
    $(hiddenOperation + '[data-operation="' + operation + '"]').show();
    $(hiddenOperation + '[data-operation="' + operation + '"]').siblings().hide();
}

$(document).ready(function () {
    var permissionSet = {};
    $.setPermission = function (permission) {
        permissionSet[permission] = true;
    };

    $.hasPermission = function (permission) {
        return permissionSet[permission];
    };

    loadNotifications();

    $("#ast-container").on("click", ".new-notification", function(e) {
        var notificationId = $(this).data("id");
        // var redirectUrl = $(this).data("url");
        var query = deviceMgtAPIBaseURI + "/notifications" + "/" + notificationId + "/mark-checked";
        var errorMsgWrapper = "#error-msg";
        var errorMsg = "#error-msg span";
        invokerUtil.put(
            query,
            null,
            // on success
            function (data, textStatus, jqXHR) {
                if (jqXHR.status == 200) {
                    $("#config-save-form").addClass("hidden");
                    // location.href = redirectUrl;
                }
            },
            // on error
            function (jqXHR) {
                if (jqXHR.status == 403) {
                    $(errorMsg).text("Action was not permitted.");
                } else if (jqXHR.status == 500) {
                    $(errorMsg).text("An unexpected error occurred. Please try refreshing the page in a while.");
                }
                $(errorMsgWrapper).removeClass("hidden");
            }
        );
    });
});
