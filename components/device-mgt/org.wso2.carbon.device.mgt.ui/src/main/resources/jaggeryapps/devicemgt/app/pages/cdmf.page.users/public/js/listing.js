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
    var sortableElem = '.wr-sortable';
    $(sortableElem).sortable({
        beforeStop: function () {
            $(this).sortable('toArray');
        }
    });
    $(sortableElem).disableSelection();
});

var apiBasePath = "/api/device-mgt/v1.0";
var modalPopup = ".modal";
var modalPopupContainer = modalPopup + " .modal-content";
var modalPopupContent = modalPopup + " .modal-content";
var body = "body";

/**
 *
 * Fires the res_text when ever a data table redraw occurs making
 * the font icons change the size to respective screen resolution.
 *
 */
$(document).on( 'draw.dt', function () {
    $(".icon .text").res_text(0.2);
} );

/*
 * set popup maximum height function.
 */
function setPopupMaxHeight() {
    $(modalPopupContent).css('max-height', ($(body).height() - ($(body).height() / 100 * 30)));
    $(modalPopupContainer).css('margin-top', (-($(modalPopupContainer).height() / 2)));
}

/*
 * show popup function.
 */
function showPopup() {
    $(modalPopup).modal('show');
}

/*
 * hide popup function.
 */
function hidePopup() {
    $(modalPopupContent).html('');
    $(modalPopup).modal('hide');
    $('body').removeClass('modal-open').css('padding-right','0px');
    $('.modal-backdrop').remove();
}

/**
 * Following click function would execute
 * when a user clicks on "Invite" link
 * on User Management page in WSO2 MDM Console.
 */
$("a#invite-user-link").click(function () {
    var usernameList = getSelectedUsernames();
    var inviteUserAPI = apiBasePath + "/users/send-invitation";

    if (usernameList.length == 0) {
        $(modalPopupContent).html($("#errorUsers").html());
    } else {
        $(modalPopupContent).html($('#invite-user-modal-content').html());
    }

    showPopup();

    $("a#invite-user-yes-link").click(function () {
        invokerUtil.post(
            inviteUserAPI,
            usernameList,
            function () {
                $(modalPopupContent).html($('#invite-user-success-content').html());
                $("a#invite-user-success-link").click(function () {
                    hidePopup();
                });
            },
            function () {
                $(modalPopupContent).html($('#invite-user-error-content').html());
                $("a#invite-user-error-link").click(function () {
                    hidePopup();
                });
            }
        );
    });

    $("a#invite-user-cancel-link").click(function () {
        hidePopup();
    });
});

/*
 * Function to get selected usernames.
 */
function getSelectedUsernames() {
    var usernameList = [];
    var userList = $("#user-grid").find("tr.DTTT_selected");
    userList.each(function () {
        usernameList.push($(this).data('username'));
    });
    return usernameList;
}

/**
 * Following click function would execute
 * when a user clicks on "Reset Password" link
 * on User Listing page in WSO2 MDM Console.
 */
function resetPassword(username) {
    $(modalPopupContent).html($('#reset-password-window').html());
    showPopup();

    $("a#reset-password-yes-link").click(function () {
        var newPassword = $("#new-password").val();
        var confirmedPassword = $("#confirmed-password").val();

        var errorMsgWrapper = "#notification-error-msg";
        var errorMsg = "#notification-error-msg span";
        if (!newPassword) {
            $(errorMsg).text("New password is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!confirmedPassword) {
            $(errorMsg).text("Retyping the new password is required.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (confirmedPassword != newPassword) {
            $(errorMsg).text("New password doesn't match the confirmation.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!inputIsValid(/^[\S]{5,30}$/, confirmedPassword)) {
            $(errorMsg).text("Password should be minimum 5 characters long, should not include any whitespaces.");
            $(errorMsgWrapper).removeClass("hidden");
        } else {
            var resetPasswordFormData = {};
            resetPasswordFormData.newPassword = unescape(confirmedPassword);

            var resetPasswordServiceURL = apiBasePath + "/admin/users/"+ username +"/credentials";

            invokerUtil.post(
                resetPasswordServiceURL,
                resetPasswordFormData,
                // The success callback
                function (data, textStatus, jqXHR) {
                    if (jqXHR.status == 200) {
                        $(modalPopupContent).html($('#reset-password-success-content').html());
                        $("a#reset-password-success-link").click(function () {
                            hidePopup();
                        });
                    }
                },
                // The error callback
                function (jqXHR) {
                    var payload = JSON.parse(jqXHR.responseText);
                    $(errorMsg).text(payload.message);
                    $(errorMsgWrapper).removeClass("hidden");
                }
            );
        }
    });

    $("a#reset-password-cancel-link").click(function () {
        hidePopup();
    });
}

/**
 * Following click function would execute
 * when a user clicks on "Remove" link
 * on User Listing page in WSO2 MDM Console.
 */
function removeUser(username) {
    var removeUserAPI = apiBasePath + "/users/" + username;
    $(modalPopupContent).html($('#remove-user-modal-content').html());
    showPopup();

    $("a#remove-user-yes-link").click(function () {
        invokerUtil.delete(
            removeUserAPI,
            function (data, textStatus, jqXHR) {
                if (jqXHR.status == 200) {
                    $("#user-" + username).remove();
                    // update modal-content with success message
                    $(modalPopupContent).html($('#remove-user-success-content').html());
                    $("a#remove-user-success-link").click(function () {
                        hidePopup();
                    });
                }
            },
            function () {
                $(modalPopupContent).html($('#remove-user-error-content').html());
                $("a#remove-user-error-link").click(function () {
                    hidePopup();
                });
            }
        );
    });

    $("a#remove-user-cancel-link").click(function () {
        hidePopup();
    });
}

/**
 * Following function would execute
 * when a user clicks on the list item
 * initial mode and with out select mode.
 */
function InitiateViewOption() {
    if ($("#can-view").val()) {
        $(location).attr('href', $(this).data("url"));
    } else {
        $(modalPopupContent).html($('#errorUserView').html());
        showPopup();
    }
}

function loadUsers() {
    var loadingContentView = "#loading-content";
    $(loadingContentView).show();

    var dataFilter = function (data) {
        data = JSON.parse(data);

        var objects = [];

        $(data.users).each( function (index) {
            objects.push({
                filter: data.users[index].username,
                firstname: data.users[index].firstname ? data.users[index].firstname : "" ,
                lastname: data.users[index].lastname ? data.users[index].lastname : "",
                emailAddress : data.users[index].emailAddress ? data.users[index].emailAddress : "",
                DT_RowId : "user-" + data.users[index].username})
        });

        var json = {
            "recordsTotal": data.count,
            "recordsFiltered": data.count,
            "data": objects
        };
        console.log(json);
        return JSON.stringify(json);
    };

    //noinspection JSUnusedLocalSymbols
    var fnCreatedRow = function (nRow, aData, iDataIndex) {
        $(nRow).attr('data-type', 'selectable');
        $(nRow).attr('data-username', aData["filter"]);
    };

    //noinspection JSUnusedLocalSymbols
    var columns = [
        {
            class: "remove-padding icon-only content-fill",
            data: null,
            defaultContent: '<div class="thumbnail icon">' +
            '<i class="square-element text fw fw-user" style="font-size: 74px;"></i>' +
            '</div>'
        },
        {
            class: "fade-edge",
            data: null,
            render: function (data, type, row, meta) {
                if (!data.firstname && !data.lastname) {
                    return "";
                } else if (data.firstname && data.lastname) {
                    return "<h4>&nbsp;&nbsp;" + data.firstname + " " + data.lastname + "</h4>";
                }
            }
        },
        {
            class: "fade-edge remove-padding-top",
            data: 'filter',
            render: function (filter, type, row, meta) {
                return '&nbsp;&nbsp;<i class="fw-user"></i>&nbsp;&nbsp;' + filter;
            }
        },
        {
            class: "fade-edge remove-padding-top",
            data: null,
            render: function (data, type, row, meta) {
                if (!data.emailAddress) {
                    return "";
                } else {
                    return "&nbsp;&nbsp;<a href='mailto:" + data.emailAddress + "' ><i class='fw-mail'></i>&nbsp;&nbsp;" + data.emailAddress + "</a>";
                }
            }
        },
        {
            class: "text-right content-fill text-left-on-grid-view no-wrap",
            data: null,
            render: function (data, type, row, meta) {
                return '&nbsp;<a href="/emm/user/edit?username=' + data.filter + '" data-username="' + data.filter + '" ' +
                    'data-click-event="edit-form" ' +
                    'class="btn padding-reduce-on-grid-view edit-user-link"> ' +
                    '<span class="fw-stack"> ' +
                    '<i class="fw fw-ring fw-stack-2x"></i>' +
                    '<i class="fw fw-edit fw-stack-1x"></i>' +
                    '</span>' +
                    '<span class="hidden-xs hidden-on-grid-view">' +
                    '&nbsp;&nbsp;Edit' +
                    '</span>' +
                    '</a>' +
                    '<a href="#" data-username="' + data.filter + '" data-userid="' + data.filter + '" ' +
                    'data-click-event="edit-form" ' +
                    'onclick="javascript:resetPassword(\'' + data.filter + '\')" ' +
                    'class="btn padding-reduce-on-grid-view remove-user-link">' +
                    '<span class="fw-stack">' +
                    '<i class="fw fw-ring fw-stack-2x"></i>' +
                    '<i class="fw fw-key fw-stack-1x"></i>' +
                    '</span>' +
                    '<span class="hidden-xs hidden-on-grid-view">' +
                    '&nbsp;&nbsp;Reset Password' +
                    '</span>' +
                    '</a>' +
                    '<a href="#" data-username="' + data.filter + '" data-userid="' + data.filter + '" ' +
                    'data-click-event="remove-form" ' +
                    'onclick="javascript:removeUser(\'' + data.filter + '\')" ' +
                    'class="btn padding-reduce-on-grid-view remove-user-link">' +
                    '<span class="fw-stack">' +
                    '<i class="fw fw-ring fw-stack-2x"></i>' +
                    '<i class="fw fw-delete fw-stack-1x"></i>' +
                    '</span>' +
                    '<span class="hidden-xs hidden-on-grid-view">' +
                    '&nbsp;&nbsp;Remove' +
                    '</span>' +
                    '</a>';
            }
        }

    ];

    var options = {
        "placeholder": "Search By Username",
        "searchKey" : "filter"
    };

    $('#user-grid').datatables_extended_serverside_paging(null, '/api/device-mgt/v1.0/users', dataFilter, columns, fnCreatedRow, null, options);
    $(loadingContentView).hide();
}

$(document).ready(function () {
    loadUsers();
    $(".viewEnabledIcon").click(function () {
        InitiateViewOption();
    });
    if (!$("#can-invite").val()) {
        $("#invite-user-button").remove();
    }
});
