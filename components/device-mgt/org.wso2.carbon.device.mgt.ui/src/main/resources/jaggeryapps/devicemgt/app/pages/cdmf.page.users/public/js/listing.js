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

$(function() {
    var sortableElem = '.wr-sortable';
    $(sortableElem).sortable({
        beforeStop: function() {
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
$(document).on('draw.dt', function() {
    $(".icon .text").res_text(0.2);
});

/**
 * Following click function would execute
 * when a user clicks on "Invite" link
 * on User Management page in WSO2 MDM Console.
 */
$("a#invite-user-link").click(function() {
    var usernameList = getSelectedUsernames();
    var inviteUserAPI = apiBasePath + "/users/send-invitation";

    if (usernameList.length == 0) {
        // modalDialog.header("Operation cannot be performed !");
        modalDialog.header("No user selected");
        // modalDialog.content("Please select a user or a list of users to send invitation emails.");
        modalDialog.content("Please select at least one user to perform this action.");
        modalDialog.footer('<div class="buttons"> <a href="javascript:modalDialog.hide()" class="btn-operations">OK' +
            '</a> </div>');
        modalDialog.showAsError();
    } else {
        modalDialog.header("Send Email Invite");
        modalDialog.content("An invitation mail will be sent to the selected user(s) to initiate an enrolment " +
            "process. Do you wish to continue ?");
        modalDialog.footer('<div class="buttons"><a href="#" id="invite-user-yes-link" class="btn-operations">Yes</a>' +
            '<a href="#" id="invite-user-cancel-link" class="btn-operations btn-default">No</a></div>');
        modalDialog.show();

    }

    $("a#invite-user-yes-link").click(function() {
        invokerUtil.post(
            inviteUserAPI,
            usernameList,
            function() {
                modalDialog.header("User invitation email for enrollment was successfully sent.");
                modalDialog.footer('<div class="buttons"><a href="#" id="invite-user-success-link" ' +
                    'class="btn-operations">OK </a></div>');
                $("a#invite-user-success-link").click(function() {
                    modalDialog.hide();
                });
            },
            function() {
                modalDialog.header('<span class="fw-stack"> <i class="fw fw-circle-outline fw-stack-2x"></i> <i class="fw ' +
                    'fw-error fw-stack-1x"></i> </span> Email Could Not be sent !');
                modalDialog.content('Please check the email address and try again later.');
                modalDialog.footer('<div class="buttons"><a href="#" id="invite-user-error-link" ' +
                    'class="btn-operations">OK </a></div>');
                $("a#invite-user-error-link").click(function() {
                    modalDialog.hide();
                });
            }
        );
    });

    $("a#invite-user-cancel-link").click(function() {
        modalDialog.hide();
    });
});

/*
 * Function to get selected usernames.
 */
function getSelectedUsernames() {
    var usernameList = [];
    var userList = $("#user-grid").find("tr.DTTT_selected");
    userList.each(function() {
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
    var errorMsgWrapper = ".notification-error-msg";
    var errorMsg = ".notification-error-msg span";
    $(errorMsgWrapper).addClass("hidden");
    modalDialog.header('<span class="fw-stack"> <i class="fw fw-circle-outline fw-stack-2x"></i> <i class="fw fw-key ' +
        'fw-stack-1x"></i> </span> Reset Password');
    modalDialog.content($("#modal-content-reset-password").html());
    modalDialog.footer('<div class="buttons"> <a href="#" id="reset-password-yes-link" class="btn-operations"> Save ' +
        '</a> <a href="#" id="reset-password-cancel-link" class="btn-operations btn-default"> Cancel </a> </div>');
    modalDialog.show();

    $("a#reset-password-yes-link").click(function() {
        var newPassword = $("#basic-modal-view .new-password").val();
        var confirmedPassword = $("#basic-modal-view .confirmed-password").val();

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
                function(data, textStatus, jqXHR) {
                    if (jqXHR.status == 200) {
                        modalDialog.header("Password reset is successful.");
                        modalDialog.content("");
                        modalDialog.footer('<div class="buttons"> <a href="javascript:modalDialog.hide()" ' +
                            'class="btn-operations">Ok</a> </div>');
                    }
                },
                // The error callback
                function(jqXHR) {
                    var payload = JSON.parse(jqXHR.responseText);
                    $(errorMsg).text(payload.message);
                    $(errorMsgWrapper).removeClass("hidden");
                }
            );
        }
    });

    $("a#reset-password-cancel-link").click(function() {
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

    $("a#remove-user-cancel-link").click(function() {
        modalDialog.hide();
    });

    $("a#remove-user-yes-link").click(function() {
        invokerUtil.delete(
            removeUserAPI,
            function(data, textStatus, jqXHR) {
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
            function() {
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

function htmlspecialchars(text) {
    return jQuery('<div/>').text(text).html();
}

function loadUsers() {
    var loadingContentView = "#loading-content";
    $(loadingContentView).show();

    var dataFilter = function(data) {
        data = JSON.parse(data);

        var objects = [];

        $(data.users).each(function(index) {
            objects.push({
                displayName : htmlspecialchars(data.users[index].firstname)+' '+htmlspecialchars(data.users[index].lastname),
                filter: htmlspecialchars(data.users[index].username),
                firstname: htmlspecialchars(data.users[index].firstname) ? htmlspecialchars(data.users[index].firstname) : "",
                lastname: htmlspecialchars(data.users[index].lastname) ? htmlspecialchars(data.users[index].lastname) : "",
                emailAddress: htmlspecialchars(data.users[index].emailAddress) ? htmlspecialchars(data.users[index].emailAddress) : "",
                userDeviceCount: htmlspecialchars(data.users[index].userDeviceCount) ? htmlspecialchars(data.users[index].userDeviceCount) : "",
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
    var fnCreatedRow = function(nRow, aData, iDataIndex) {
        $(nRow).attr('data-type', 'selectable');
        $(nRow).attr('data-username', aData["filter"]);
    };

    //noinspection JSUnusedLocalSymbols
    var columns = [{
            class: "",
            data: null,
            render: function(data, type, row, meta) {
                if (!data.firstname && !data.lastname) {
                    return "";
                } else if (data.firstname && data.lastname) {
                    return "<a>" + data.firstname + " " + data.lastname + "</a>";
                }
            }
        },
        {
            class: "icon-only content-fill",
            data: 'displayName',
            render: function(displayName, type, row, meta) {
                var html = '<div class="viewEnabledIcon" data-url="' + context + '/user/view?username=' + row.filter + '">' +
                    '<!--<i class="square-element text fa fa-user" style="font-size: 74px;"></i> -->';

                if (displayName == " ") {
                    if(row.filter == 'admin'){
                         html += "<a> admin </a></div>";
                    }
                    else{
                    html += "<h4 style='height:25px' </h4></div>";
                    }
                } else if (displayName) {
                    html += "<a>" + displayName + "</a></div>";
                }
                return html;
            }
        },
        {
            class: "remove-padding-top",
            data: 'filter',
            render: function(filter, type, row, meta) {
                return '<span><i class="fw-user"></i>' + filter + '</span>';
            }
        },
        {
            class: "remove-padding-top email_width",
            data: null,
            render: function(data, type, row, meta) {
                if (!data.emailAddress) {
                    return "";
                } else {
                    return "<span><a href='mailto:" + data.emailAddress + "' >" + data.emailAddress + "</a></span>";
                }
            }
        },
        {
            class: "remove-padding-top",
            data: null,
            render: function(data, type, row, meta) {
                if (!data.userDeviceCount) {
                    return "";
                } else {
                    return "<span>" + data.userDeviceCount + "</span>";
                }
            }
        },
        {
            class: "text-right content-fill text-left-on-grid-view no-wrap tooltip-overflow-fix",
            data: null,
            render: function(data, type, row, meta) {
                var editbtn = '<a data-toggle="tooltip" data-placement="top" title="Edit User"href="' + context +
                    '/user/edit?username=' + encodeURIComponent(data.filter) + '" data-username="' + data.filter + '" ' +
                    'data-click-event="edit-form" ' +
                    'class="btn padding-reduce-on-grid-view edit-user-link" data-placement="top" data-toggle="tooltip" data-original-title="Edit"> ' +
                    '<span class="fw-stack"> ' +
                    '<i class="fw fw-circle-outline fw-stack-2x"></i>' +
                    '<i class="fw fw-edit fw-stack-1x"></i>' +
                    '</span><span class="hidden-xs hidden-on-grid-view">Edit</span></a>';

                var resetPasswordbtn = '<a data-toggle="tooltip" data-placement="top" title="Reset Password" href="#" data-username="' + data.filter + '" data-userid="' + data.filter + '" ' +
                    'data-click-event="edit-form" ' +
                    'onclick="javascript:resetPassword(\'' + data.filter + '\')" ' +
                    'class="btn padding-reduce-on-grid-view remove-user-link" data-placement="top" data-toggle="tooltip" data-original-title="Reset Password">' +
                    '<span class="fw-stack">' +
                    '<i class="fw fw-circle-outline fw-stack-2x"></i>' +
                    '<i class="fw fw-key fw-stack-1x"></i>' +
                    '</span><span class="hidden-xs hidden-on-grid-view">Reset Password</span></a>';

                var removebtn = '<a data-toggle="tooltip" data-placement="top" title="Remove User" href="#" data-username="' + data.filter + '" data-userid="' + data.filter + '" ' +
                    'data-click-event="remove-form" ' +
                    'onclick="javascript:removeUser(\'' + data.filter + '\')" ' +
                    'class="btn padding-reduce-on-grid-view remove-user-link" data-placement="top" data-toggle="tooltip" data-original-title="Remove">' +
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
        "placeholder": "Search Users",
        "searchKey": "filters"
    };

    var settings = {
        "sorting": false
    };

    $('#user-grid').datatables_extended_serverside_paging(settings, '/api/device-mgt/v1.0/users', dataFilter, columns, fnCreatedRow, null, options);
    $(loadingContentView).hide();

}

/*
 * Function to get selected Users.
 */
function getSelectedUsers() {
    var usersList = [];
    var thisTable = $(".DTTT_selected").closest('.dataTables_wrapper').find('.dataTable').dataTable();
    thisTable.api().rows().every(function() {
        if ($(this.node()).hasClass('DTTT_selected')) {
            usersList.push($(thisTable.api().row(this).node()).data('username'));
        }
    });
    return usersList;
}

$(document).ready(function() {
    $('ul.nav a').each(function() {
        var url = this.href;
        if (url.indexOf("/devicemgt/users") !== -1) {
            $(this).addClass('active');
        }
    });

      $('a.ops_icon').hover(function() {
    var off_img = $(this).find("img")[0];
    var on_img = $(this).find("img")[1];
    if ($(off_img).hasClass("icon_active")) {
        $(off_img).removeClass('icon_active');
        $(on_img).addClass('icon_active');
    } else {
        $(on_img).removeClass('icon_active');
        $(off_img).addClass('icon_active');
    }
});

    loadUsers();
    $(function() {
        $('[data-toggle="tooltip"]').tooltip()
    });
    if (!$("#can-invite").val()) {
        $("#invite-user-button").remove();
    }

    /* Remove multiple user Function*/
    $(".users-remove-link").click(function() {
        var usersList = getSelectedUsers();
        console.log(usersList);
        var serviceURL = "/api/device-mgt/v1.0/users/deleteUsers";
        if (usersList.length == 0) {
            modalDialog.header('Action cannot be performed !');
            modalDialog.content('Please select a users or a list of policies to remove.');
            modalDialog.footer('<div class="buttons"><a href="javascript:modalDialog.hide()" ' +
                'class="btn-operations">Ok</a></div>');
            modalDialog.showAsAWarning();
        } else {
            modalDialog.header('Do you really want to remove the selected users(s)?');
            modalDialog.footer('<div class="buttons"><a href="#" id="remove-users-yes-link" class=' +
                '"btn-operations">Remove</a> <a href="#" id="remove-users-cancel-link" ' +
                'class="btn-operations btn-default">Cancel</a></div>');
            modalDialog.show();
        }

        // on-click function for users removing "yes" button
        $("a#remove-users-yes-link").click(function() {
            invokerUtil.post(
                serviceURL,
                usersList,
                // on success
                function(data, textStatus, jqXHR) {
                    if (jqXHR.status == 200) {
                        modalDialog.header('Done. Selected users was successfully removed.');
                        modalDialog.footer('<div class="buttons"><a href="#" id="remove-users-success-link" ' +
                            'class="btn-operations">Ok</a></div>');
                        $("a#remove-users-success-link").click(function() {
                            modalDialog.hide();
                            location.reload();
                        });
                    }
                },
                // on error
                function(jqXHR) {
                    console.log(stringify(jqXHR.data));
                    modalDialog.header('An unexpected error occurred. Please try again later.');
                    modalDialog.footer('<div class="buttons"><a href="#" id="remove-users-error-link" ' +
                        'class="btn-operations">Ok</a></div>');
                    modalDialog.showAsError();
                    $("a#remove-users-error-link").click(function() {
                        modalDialog.hide();
                    });
                }
            );
        });

        // on-click function for users removing "cancel" button
        $("a#remove-users-cancel-link").click(function() {
            modalDialog.hide();
        });
    });
});