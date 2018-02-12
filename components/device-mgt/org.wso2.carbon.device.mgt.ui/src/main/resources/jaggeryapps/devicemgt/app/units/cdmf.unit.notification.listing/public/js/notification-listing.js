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
var notificationsAvailable = false;

/**
 * Following function would execute
 * when a user clicks on the list item
 * initial mode and with out select mode.
 */
function InitiateViewOption() {
    $(location).attr('href', $(this).data("url"));
}

function loadNotifications() {
    var deviceListingNew = $("#notification-listing-new");
    var deviceListingNewSrc = deviceListingNew.attr("src");

    var deviceListingAll = $("#notification-listing-all");
    var deviceListingAllSrc = deviceListingAll.attr("src");

    $.template(
        "notification-listing-new",
        deviceListingNewSrc,
        function (template) {
            invokerUtil.get(
                deviceMgtAPIBaseURI + "/notifications?status=NEW",
                // on success
                function (data, textStatus, jqXHR) {
                    if (jqXHR.status == 200 && data) {
                        data = JSON.parse(data);
                        if (data["notifications"]) {
                            notificationsAvailable = true;
                            var viewModel = {};
                            viewModel["notifications"] = data["notifications"];
                            viewModel["appContext"] = context;
                            var content = template(viewModel);
                            $("#ast-container").append(content);
                            var settings = {
                                "sorting" : false
                            };
                            $("#unread-notifications").datatables_extended(settings);

                            /**
                             *  append advance operations to list table toolbar
                             */
                            $('#unread-notifications_wrapper').find('.dataTablesTop' +
                                ' .dataTables_toolbar').html(
                                "<a\ class=\"btn btn-primary\"" +
                                " data-click-event=\"clear-notification\">Clear All" +
                                " Notifications</a>"
                            );
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

    $.template(
        "notification-listing-all",
        deviceListingAllSrc,
        function (template) {
            invokerUtil.get(
                deviceMgtAPIBaseURI + "/notifications",
                // on success
                function (data, textStatus, jqXHR) {
                    if (jqXHR.status == 200 && data) {
                        data = JSON.parse(data);
                        if (data["notifications"]) {
                            notificationsAvailable = true;
                            var viewModel = {};
                            viewModel["notifications"] = data["notifications"];
                            viewModel["appContext"] = context;
                            var content = template(viewModel);
                            $("#ast-container").append(content);
                            var settings = {
                                "sorting" : false
                            };
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
    var hiddenOperation = ".wr-hidden-operations-content > div";
    $(hiddenOperation + '[data-operation="' + operation + '"]').siblings().hide();
    $(hiddenOperation + '[data-operation="' + operation + '"]').show();
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

    if(notificationsAvailable) {
        $("#notification-clear-button").removeClass("hidden");
    }

    $("#ast-container").on("click", ".btn", function (e) {

        var clickEvent = $(this).data('click-event');

        if(clickEvent == "clear-notification"){
            e.preventDefault();
            var markAsReadNotificationsAPI = "/api/device-mgt/v1.0/notifications/clear-all";
            var messageSideBar = ".sidebar-messages";
            var clickEvent = $(this).data('click-event');
            var eventHandler = $(this);

            invokerUtil.put(
                markAsReadNotificationsAPI,
                null,
                function (data) {
                    $('.message').remove();
                    $("#notification-bubble").html(0);
                    var undreadNotifications = $("#unread-notifications");
                    undreadNotifications.find("tbody").empty();
                    undreadNotifications.find("tbody").append("<tr><td colspan=''>No data" +
                        " available in table</td></tr>");
                }, function () {
                    var content = "<li class='message message-danger'><h4><i class='icon fw fw-error'></i>Warning</h4>" +
                        "<p>Unexpected error occurred while loading notification. Please refresh the page and" +
                        " try again</p></li>";
                    $(messageSideBar).html(content);
                }
            );
        }

    });
});
