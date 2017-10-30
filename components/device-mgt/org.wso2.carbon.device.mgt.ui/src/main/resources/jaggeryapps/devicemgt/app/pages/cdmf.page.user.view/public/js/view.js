$(document).ready(function() {
    $('#loading-content').show();
    var path = window.location.href;
    $('ul.nav a').each(function() {
        var url = this.href;
        if (url.indexOf("/devicemgt/users") !== -1) {
            $(this).addClass('active');
        }
    });

    var apiBasePath = "/api/device-mgt/v1.0";
    var currentUserName = $('#currentUser').text();
    console.log(currentUserName);

    // reset - password - link
    $(".reset-password-link").click(function() {
        // function resetPassword(username) {
        var username = $('.users-remove-link').data('username');
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
    });

    $(".send-invite-link").click(function() {
        var usernameList = [];
        usernameList[0] = $('.users-remove-link').data('username');
        var inviteUserAPI = apiBasePath + "/users/send-invitation";

        if (usernameList.length == 0) {
            // modalDialog.header("Operation cannot be performed !");
            modalDialog.header("No user selected");
            // modalDialog.content("Please select a user or a list of users to send invitation emails.");
            modalDialog.content("Please select at least one user to perform this action.");
            modalDialog.footer('<div class="buttons"> <a href="javascript:modalDialog.hide()" class="btn-operations">Ok' +
                '</a> </div>');
            modalDialog.showAsError();
        } else {
            modalDialog.header("");
            modalDialog.content("An invitation mail will be sent to the user to initiate an enrolment " +
                "process. Do you wish to continue ?");
            modalDialog.footer('<div class="buttons"><a href="#" id="invite-user-yes-link" class="btn-operations">yes</a>' +
                '<a href="#" id="invite-user-cancel-link" class="btn-operations btn-default">No</a></div>');
            modalDialog.show();

        }

        $("a#invite-user-yes-link").click(function() {
            invokerUtil.post(
                inviteUserAPI,
                usernameList,
                function() {
                    modalDialog.header("User invitation email for enrollment was successfully sent.");
                    modalDialog.content("  ");
                    modalDialog.footer('<div class="buttons"><a href="#" id="invite-user-success-link" ' +
                        'class="btn-operations">Ok </a></div>');
                    $("a#invite-user-success-link").click(function() {
                        modalDialog.hide();
                    });
                },
                function() {
                    modalDialog.header('<span class="fw-stack"> <i class="fw fw-circle-outline fw-stack-2x"></i> <i class="fw ' +
                        'fw-error fw-stack-1x"></i> </span> Unexpected Error !');
                    modalDialog.content('An unexpected error occurred. Try again later.');
                    modalDialog.footer('<div class="buttons"><a href="#" id="invite-user-error-link" ' +
                        'class="btn-operations">Ok </a></div>');
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

    $(".users-remove-link").click(function() {
        var usersList = [];
        usersList[0] = $('.users-remove-link').data('username');
        console.log(usersList);
        var serviceURL = "/api/device-mgt/v1.0/users/deleteUsers";

        modalDialog.header('Do you really want to delete the user?');
        modalDialog.footer('<div class="buttons"><a href="#" id="remove-users-yes-link" class=' +
            '"btn-operations">Remove</a> <a href="#" id="remove-users-cancel-link" ' +
            'class="btn-operations btn-default">Cancel</a></div>');
        modalDialog.show();


        // on-click function for users removing "yes" button
        $("a#remove-users-yes-link").click(function() {
            invokerUtil.post(
                serviceURL,
                usersList,
                // on success
                function(data, textStatus, jqXHR) {
                    if (jqXHR.status == 200) {
                        modalDialog.header('Done. User was successfully removed.');
                        modalDialog.footer('<div class="buttons"><a href="#" id="remove-users-success-link" ' +
                            'class="btn-operations">Ok</a></div>');
                        $("a#remove-users-success-link").click(function() {
                            modalDialog.hide();
                            location.href = "../users";
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
    $('#loading-content').remove();
});