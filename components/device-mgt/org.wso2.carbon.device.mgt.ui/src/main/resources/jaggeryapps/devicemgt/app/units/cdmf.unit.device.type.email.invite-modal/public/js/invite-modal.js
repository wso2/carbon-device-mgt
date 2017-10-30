/**
 * Created by imesh on 16/12/16.
 */

/**
 * Opens a modal popup with input to enter multiple email addresses to send invites
 */
function toggleEmailInvite() {
    modalDialog.header('<h2 class="pull-left modal-title"><b>Invite Email</b></h2>');
    modalDialog.content($("#invite-by-email-body").html());
    modalDialog.footer('<div class="buttons">' +
        '<a href="javascript:sendInvites()" class="btn-operations btn-default">Done</a>' +
        '</div>');
    modalDialog.show();
    //$('.modal-content .select2-container').remove();
    $('.modal-content #choose_usr_email').select2({
        tags: true,
        tokenSeparators: [",", " "],
        createTag: function(term, data) {
            var value = term.term;
            if (validateEmail(value)) {
                return {
                    id: value,
                    text: value
                };
            }
            return null;
        }
    });
}

function validateEmail(email) {
    var re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
}

/**
 * Action to get email object from select2 element
 * @returns {boolean}
 */
function sendInvites() {
    var emailObj = $('.modal-content #choose_usr_email').select2('data'),
        emailArr = [];

    if (emailObj.length <= 0) {
        console.log('no values to print');
        // modalDialog.header("");
        modalDialog.content("<h4>No emails were sent</h4>");
        modalDialog.footer('<div class="buttons"> <a href="#" id="invite-user-success-link" ' +
            'class="btn-operations">Ok </a> </div>');
        $("a#invite-user-success-link").click(function() {
            modalDialog.hide();
        });
        return false;
    }

    emailObj.forEach(function(el) {
        emailArr.push(el.text);
    });

    var deviceEmailObj = {
        "deviceType": deviceTypeView,
        "recipients": emailArr
    };
    invokerUtil.post(
        "/api/device-mgt/v1.0/users/enrollment-invite",
        deviceEmailObj,
        function() {
            modalDialog.header("Invitations sent");
            modalDialog.content("<h4>Invitation email for enrollment was successfully sent.</h4>");
            modalDialog.footer('<div class="buttons"> <a href="#" id="invite-user-success-link" ' +
                'class="btn-operations">Ok </a> </div>');
            $("a#invite-user-success-link").click(function() {
                modalDialog.hide();
            });
        },
        function() {
            modalDialog.header('<span class="fw-stack"> <i class="fw fw-circle-outline fw-stack-2x"></i> <i class="fw ' +
                'fw-error fw-stack-1x"></i> </span> Unexpected Error !');
            modalDialog.content('An unexpected error occurred. Try again later.');
            modalDialog.footer('<div class="buttons"> <a href="#" id="invite-user-error-link" ' +
                'class="btn-operations">Ok </a> </div>');
            $("a#invite-user-error-link").click(function() {
                modalDialog.hide();
            });
        }
    );
}