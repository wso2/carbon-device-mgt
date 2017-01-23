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

/**
 * Checks if an email address has the valid format or not.
 *
 * @param email Email address
 * @returns {boolean} true if email has the valid format, otherwise false.
 */
function emailIsValid(email) {
    var regExp = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
    return regExp.test(email);
}

$(document).ready(function(){

    /**
     * Following click function would execute
     * when a user clicks on "Add User" button
     * on Add User page in WSO2 Devicemgt Console.
     */
    $("button#add-user-btn").click(function () {

        var usernameInput = $("input#user_name");
        var firstnameInput = $("input#first_name");
        var lastnameInput = $("input#last_name");
        var emailInput = $("input#email");
        var passwordInput = $("input#password");
        var passwordConfirmationInput = $("input#password_confirmation");

        var username = usernameInput.val().trim();
        var firstname = firstnameInput.val();
        var lastname = lastnameInput.val();
        var emailAddress = emailInput.val();
        var password = passwordInput.val();
        var passwordConfirmation = passwordConfirmationInput.val();
        var errorMsgWrapper = "#user-create-error-msg";
        var errorMsg = "#user-create-error-msg span";

        if (!firstname) {
            $(errorMsg).text("Firstname is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!inputIsValid(firstnameInput.data("regex"), firstname)) {
            $(errorMsg).text(firstnameInput.data("errormsg"));
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!lastname) {
            $(errorMsg).text("Lastname is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!inputIsValid(lastnameInput.data("regex"), lastname)) {
            $(errorMsg).text(lastnameInput.data("errormsg"));
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!username) {
            $(errorMsg).text("Username is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!inputIsValid(usernameInput.data("regex"), username)) {
            $(errorMsg).text(usernameInput.data("errormsg"));
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!emailAddress) {
            $(errorMsg).text("Email is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!emailIsValid(emailAddress)) {
            $(errorMsg).text(emailInput.data("errormsg"));
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!password) {
            $(errorMsg).text("Password is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (password.length < 6) {
            $(errorMsg).text("Password is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (password != passwordConfirmation) {
            $(errorMsg).text("Please enter the same password for confirmation.");
            $(errorMsgWrapper).removeClass("hidden");
        } else {
            $(errorMsgWrapper).addClass("hidden");
            $("#add-user-btn").prop('disabled', true);

            var addUserFormData = {};
            addUserFormData.username = username;
            addUserFormData.firstname = firstname;
            addUserFormData.lastname = lastname;
            addUserFormData.emailAddress = emailAddress;
            addUserFormData.password = $("input#password").val();
            addUserFormData.userRoles = null;

            var context = $(".form-login-box").data("context");
            var addUserAPI = context + "/api/user/register";

            $.ajax({
                       type: 'POST',
                       url: addUserAPI,
                       contentType: 'application/json',
                       data: JSON.stringify(addUserFormData),
                       success: function (data) {
                           $("#add-user-btn").prop('disabled', false);
                           if (data == 200) {
                               $('.wr-validation-summary strong').html(
                                       "<i class=\"icon fw fw-success\"></i> Successfully Submitted.");
                               $('.wr-validation-summary').removeClass("alert-danger");
                               $('.wr-validation-summary').addClass("alert-success");
                           } else if (data == 201) {
                               $('.wr-validation-summary strong').html(
                                       "<i class=\"icon fw fw-success\"></i> User created succssfully. You will be " +
                                       "redirected to login page.");
                               $('.wr-validation-summary').removeClass("alert-danger");
                               $('.wr-validation-summary').addClass("alert-success");
                               $("#add-user-btn").prop('disabled', true);
                               setTimeout(function () {
                                   window.location = context + "/login";
                               }, 2000);
                           } else if (data == 400) {
                               $('.wr-validation-summary strong').html(
                                       "<i class=\"icon fw fw-error\"></i> Exception at backend.");
                               $('.wr-validation-summary').removeClass("alert-danger");
                               $('.wr-validation-summary').addClass("alert-warning");
                           } else if (data == 403) {
                               $('.wr-validation-summary strong').html("Action not permitted.");
                           } else if (data == 409) {
                               $('.wr-validation-summary strong').html(
                                       "<i class=\"icon fw fw-info\"></i> User name already exists.");
                               $('.wr-validation-summary').removeClass("alert-default");
                               $('.wr-validation-summary').addClass("alert-success");
                           }
                           $('.wr-validation-summary').removeClass("hidden");
                           $('#password').val('');
                           $('#password_confirmation').val('');
                       },
                       error: function (err) {
                           $("#add-user-btn").prop('disabled', false);
                           $('.wr-validation-summary strong').html(
                                   "<i class=\"icon fw fw-error\"></i> An unexpected error occurred.");
                           $('.wr-validation-summary').removeClass("hidden");
                       }
                   });
        }
    });
});
