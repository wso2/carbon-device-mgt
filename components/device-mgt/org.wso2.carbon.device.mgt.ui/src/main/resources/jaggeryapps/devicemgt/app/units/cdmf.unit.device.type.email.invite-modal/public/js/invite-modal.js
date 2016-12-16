/**
 * Created by imesh on 16/12/16.
 */

/**
 * Opens a modal popup with input to enter multiple email addresses to send invites
 */
function toggleEmailInvite(){
    $(".modal-content").html($("#invite-by-email-modal").html());
    //$('.modal-content .select2-container').remove();
    $('.modal-content #choose_usr_email').select2({
        tags: true,
        tokenSeparators: [",", " "],
        createTag: function(term, data) {
            var value = term.term;
            if(validateEmail(value)) {
                return {
                    id: value,
                    text: value
                };
            }
            return null;
        }
    });
    showPopup();
}

function validateEmail(email) {
    var re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
}

/**
 * Action to get email object from select2 element
 * @returns {boolean}
 */
function sendInvites(){
    var emailObj = $('.modal-content #choose_usr_email').select2('data'),
        emailArr = [];

    if(emailObj.length <= 0){
        console.log('no values to print');
        return false;
    }

    emailObj.forEach(function(el){
        emailArr.push(el.text);
    })

    hidePopup();
    console.log(emailArr);
}