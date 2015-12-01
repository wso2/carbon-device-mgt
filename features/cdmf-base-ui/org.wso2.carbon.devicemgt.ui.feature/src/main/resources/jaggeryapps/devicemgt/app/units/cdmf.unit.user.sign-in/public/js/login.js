function post(path, method) {
    method = method || "post"; // Set method to post by default if not specified.
    form.submit();
}


$( document ).ready(function() {
    var currentHash = window.location.hash,
        submitPath = $("#signInForm").attr("action");
    if(currentHash=="#auth-failed") {
        $('.wr-validation-summary p').text("Sorry!, Please make sure to enter correct username and password");
        $('.wr-validation-summary').removeClass("hidden");
    }else if(currentHash=="#error"){
        $('.wr-validation-summary p').text("Sorry!, Error occured");
        $('.wr-validation-summary').removeClass("hidden");
    }
    $('#signIn').click(function(){
        var username = $("input#username").val(),
            password = $("input#password").val();

        if (!username) {
            $('.wr-validation-summary p').text("Sorry!, Username cannot be empty.");
            $('.wr-validation-summary').removeClass("hidden");
        } else if (!password){
            $('.wr-validation-summary p').text("Sorry!, Password cannot be empty.");
            $('.wr-validation-summary').removeClass("hidden");
        } else {
            post(submitPath,"POST");
        }
    });
});


function submitLoginForm() {
    var submitPath = $("#signInForm").attr("action");
    $('form input').keypress(function() {
        if(e.which == 10 || e.which == 13) {
            if (!username) {
                alert()
                $('.wr-validation-summary p').text("Sorry!, Username cannot be empty.");
                $('.wr-validation-summary').removeClass("hidden");
            } else if (!password){
                $('.wr-validation-summary p').text("Sorry!, Password cannot be empty.");
                $('.wr-validation-summary').removeClass("hidden");
            } else {
                post(submitPath,"POST");
            }
        }
    });
}
