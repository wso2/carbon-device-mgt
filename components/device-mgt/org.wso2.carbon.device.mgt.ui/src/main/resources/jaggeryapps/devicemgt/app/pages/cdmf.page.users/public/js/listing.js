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
var body = "body";

/**
 *
 * Fires the res_text when ever a data table redraw occurs making
 * the font icons change the size to respective screen resolution.
 *
 */
$(document).on('draw.dt', function () {
    $(".icon .text").res_text(0.2);
});

/**
 * Following click function would execute
 * when a user clicks on "Invite" link
 * on User Management page in WSO2 MDM Console.
 */
$("a#invite-user-link").click(function () {
    var usernameList = getSelectedUsernames();
    var inviteUserAPI = apiBasePath + "/users/send-invitation";

    if (usernameList.length == 0) {
        modalDialog.header("Operation cannot be performed !");
        modalDialog.content("Please select a user or a list of users to send invitation emails.");
        modalDialog.footer('<div class="buttons"> <a href="javascript:modalDialog.hide()" class="btn-operations">Ok' +
            '</a> </div>');
        modalDialog.showAsError();
    } else {
        modalDialog.header("");
        modalDialog.content("An invitation mail will be sent to the selected user(s) to initiate an enrolment " +
            "process. Do you wish to continue ?");
        modalDialog.footer('<div class="buttons"><a href="#" id="invite-user-yes-link" class="btn-operations">yes</a>' +
            '<a href="#" id="invite-user-cancel-link" class="btn-operations btn-default">No</a></div>');
        modalDialog.show();

    }

    $("a#invite-user-yes-link").click(function () {
        invokerUtil.post(
            inviteUserAPI,
            usernameList,
            function () {
                modalDialog.header("User invitation email for enrollment was successfully sent.");
                modalDialog.footer('<div class="buttons"><a href="#" id="invite-user-success-link" ' +
                    'class="btn-operations">Ok </a></div>');
                $("a#invite-user-success-link").click(function () {
                    modalDialog.hide();
                });
            },
            function () {
                modalDialog.header('<span class="fw-stack"> <i class="fw fw-circle-outline fw-stack-2x"></i> <i class="fw ' +
                    'fw-error fw-stack-1x"></i> </span> Unexpected Error !');
                modalDialog.content('An unexpected error occurred. Try again later.');
                modalDialog.footer('<div class="buttons"><a href="#" id="invite-user-error-link" ' +
                    'class="btn-operations">Ok </a></div>');
                $("a#invite-user-error-link").click(function () {
                    modalDialog.hide();
                });
            }
        );
    });

    $("a#invite-user-cancel-link").click(function () {
        modalDialog.hide();
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
    modalDialog.header('<span class="fw-stack"> <i class="fw fw-circle-outline fw-stack-2x"></i> <i class="fw fw-key ' +
        'fw-stack-1x"></i> </span> Reset Password');
    modalDialog.content($("#modal-content-reset-password").html());
    modalDialog.footer('<div class="buttons"> <a href="#" id="reset-password-yes-link" class="btn-operations"> Save ' +
        '</a> <a href="#" id="reset-password-cancel-link" class="btn-operations btn-default"> Cancel </a> </div>');
    modalDialog.show();

    $("a#reset-password-yes-link").click(function () {
        var newPassword = $("#basic-modal-view .new-password").val();
        var confirmedPassword = $("#basic-modal-view .confirmed-password").val();
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
            var domain;
            if (username.indexOf('/') > 0) {
                domain = username.substr(0, username.indexOf('/'));
                username = username.substr(username.indexOf('/') + 1);
            }
            var resetPasswordServiceURL = apiBasePath + "/admin/users/" + username + "/credentials";
            if (domain) {
                resetPasswordServiceURL += '?domain=' + encodeURIComponent(domain);
            }
            invokerUtil.post(
                resetPasswordServiceURL,
                resetPasswordFormData,
                // The success callback
                function (data, textStatus, jqXHR) {
                    if (jqXHR.status == 200) {
                        modalDialog.header("Password reset is successful.");
                        modalDialog.content("");
                        modalDialog.footer('<div class="buttons"> <a href="javascript:modalDialog.hide()" ' +
                            'class="btn-operations">Ok</a> </div>');
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
        modalDialog.hide();
    });
}

/**
 * Following click function would execute
 * when a user clicks on "Remove" link
 * on User Listing page in WSO2 MDM Console.
 */
function removeUser(username) {
    var domain;
    if (username.indexOf('/') > 0) {
        domain = username.substr(0, username.indexOf('/'));
        username = username.substr(username.indexOf('/') + 1);
    }
    var removeUserAPI = apiBasePath + "/users/" + encodeURIComponent(username);
    if (domain) {
        removeUserAPI += '?domain=' + encodeURIComponent(domain);
    }
    modalDialog.header("Remove User");
    modalDialog.content("Do you really want to remove this user ?");
    modalDialog.footer('<div class="buttons"> <a href="#" id="remove-user-yes-link" class="btn-operations">Remove</a> ' +
        '<a href="#" id="remove-user-cancel-link" class="btn-operations btn-default">Cancel</a> </div>');
    modalDialog.showAsAWarning();

    $("a#remove-user-cancel-link").click(function () {
        modalDialog.hide();
    });

    $("a#remove-user-yes-link").click(function () {
        invokerUtil.delete(
            removeUserAPI,
            function (data, textStatus, jqXHR) {
                if (jqXHR.status == 200) {
                    if (domain) {
                        username = domain + '/' + username;
                    }
                    $('[id="user-' + username + '"]').remove();
                    // update modal-content with success message
                    modalDialog.header("User Removed.");
                    modalDialog.content("Done. User was successfully removed.");
                    modalDialog.footer('<div class="buttons"> <a href="javascript:modalDialog.hide()" ' +
                        'class="btn-operations">Ok</a> </div>');

                }
            },
            function () {
                modalDialog.hide();
                modalDialog.header("Operation cannot be performed !");
                modalDialog.content("An unexpected error occurred. Please try again later.");
                modalDialog.footer('<div class="buttons"> <a href="javascript:modalDialog.hide()" ' +
                    'class="btn-operations">Ok</a> </div>');
                modalDialog.showAsError();
            }
        );
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
        modalDialog.header("Unauthorized action!");
        modalDialog.content("You don't have permissions to view users");
        modalDialog.footer('<div class="buttons"> <a href="javascript:modalDialog.hide()" class="btn-operations">Ok</a> </div>');
        modalDialog.showAsError();
    }
}

function htmlspecialchars(text){
    return jQuery('<div/>').text(text).html();
}

function loadUsers() {
    var loadingContentView = "#loading-content";
    $(loadingContentView).show();

    var dataFilter = function (data) {
        data = JSON.parse(data);

        var objects = [];

        $(data.users).each(function (index) {
            objects.push({
                filter: htmlspecialchars(data.users[index].username),
                firstname: htmlspecialchars(data.users[index].firstname) ? htmlspecialchars(data.users[index].firstname) : "",
                lastname: htmlspecialchars(data.users[index].lastname) ? htmlspecialchars(data.users[index].lastname) : "",
                emailAddress: htmlspecialchars(data.users[index].emailAddress) ? htmlspecialchars(data.users[index].emailAddress) : "",
                DT_RowId: "user-" + htmlspecialchars(data.users[index].username)
            })
        });

        var json = {
            "recordsTotal": data.count,
            "recordsFiltered": data.count,
            "data": objects
        };

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
            render: function (data, type, row, meta) {
                return '<div class="thumbnail icon viewEnabledIcon" data-url="' + context + '/user/view?username=' + data.filter + '">' +
                    '<i class="square-element text fw fw-user" style="font-size: 74px;"></i>' +
                    '</div>';
            }
        },
        {
            class: "",
            data: null,
            render: function (data, type, row, meta) {
                if (!data.firstname && !data.lastname) {
                    return "";
                } else if (data.firstname && data.lastname) {
                    return "<h4>" + data.firstname + " " + data.lastname + "</h4>";
                }
            }
        },
        {
            class: "remove-padding-top",
            data: 'filter',
            render: function (filter, type, row, meta) {
                return '<i class="fw-user"></i>' + filter;
            }
        },
        {
            class: "remove-padding-top",
            data: null,
            render: function (data, type, row, meta) {
                if (!data.emailAddress) {
                    return "";
                } else {
                    return "<a href='mailto:" + data.emailAddress + "' ><i class='fw-mail'></i>" + data.emailAddress + "</a>";
                }
            }
        },
        {
            class: "text-right content-fill text-left-on-grid-view no-wrap",
            data: null,
            render: function (data, type, row, meta) {
                var editbtn = '<a data-toggle="tooltip" data-placement="bottom" title="Edit User"href="' + context +
                    '/user/edit?username=' + encodeURIComponent(data.filter) + '" data-username="' + data.filter + '" ' +
                    'data-click-event="edit-form" ' +
                    'class="btn padding-reduce-on-grid-view edit-user-link"> ' +
                    '<span class="fw-stack"> ' +
                    '<i class="fw fw-circle-outline fw-stack-2x"></i>' +
                    '<i class="fw fw-edit fw-stack-1x"></i>' +
                    '</span><span class="hidden-xs hidden-on-grid-view">Edit</span></a>';

                var resetPasswordbtn = '<a data-toggle="tooltip" data-placement="bottom" title="Reset Password" href="#" data-username="' + data.filter + '" data-userid="' + data.filter + '" ' +
                    'data-click-event="edit-form" ' +
                    'onclick="javascript:resetPassword(\'' + data.filter + '\')" ' +
                    'class="btn padding-reduce-on-grid-view remove-user-link">' +
                    '<span class="fw-stack">' +
                    '<i class="fw fw-circle-outline fw-stack-2x"></i>' +
                    '<i class="fw fw-key fw-stack-1x"></i>' +
                    '</span><span class="hidden-xs hidden-on-grid-view">Reset Password</span></a>';

                var removebtn = '<a data-toggle="tooltip" data-placement="bottom" title="Remove User" href="#" data-username="' + data.filter + '" data-userid="' + data.filter + '" ' +
                    'data-click-event="remove-form" ' +
                    'onclick="javascript:removeUser(\'' + data.filter + '\')" ' +
                    'class="btn padding-reduce-on-grid-view remove-user-link">' +
                    '<span class="fw-stack">' +
                    '<i class="fw fw-circle-outline fw-stack-2x"></i>' +
                    '<i class="fw fw-delete fw-stack-1x"></i>' +
                    '</span><span class="hidden-xs hidden-on-grid-view">Remove</span></a>';

                var returnbtnSet = '';
                var adminUser = $("#user-table").data("user");
                var currentUser = $("#user-table").data("logged-user");
                if ($("#can-edit").length > 0 && adminUser !== data.filter) {
                    returnbtnSet = returnbtnSet + editbtn;
                }
                if ($("#can-reset-password").length > 0 && adminUser !== data.filter) {
                    returnbtnSet = returnbtnSet + resetPasswordbtn;
                }
                if ($("#can-remove").length > 0 && adminUser !== data.filter && currentUser !== data.filter) {
                    returnbtnSet = returnbtnSet + removebtn;
                }

                return returnbtnSet;
            }
        }

    ];

    var options = {
        "placeholder": "Search By Username",
        "searchKey": "filter"
    };

    var settings = {
        "sorting": false
    };

    $('#user-grid').datatables_extended_serverside_paging(settings, '/api/device-mgt/v1.0/users', dataFilter, columns, fnCreatedRow, null, options);
    $(loadingContentView).hide();

}

$(document).ready(function () {
    loadUsers();
    $(function () {
        $('[data-toggle="tooltip"]').tooltip()
    });
    if (!$("#can-invite").val()) {
        $("#invite-user-button").remove();
    }

});
