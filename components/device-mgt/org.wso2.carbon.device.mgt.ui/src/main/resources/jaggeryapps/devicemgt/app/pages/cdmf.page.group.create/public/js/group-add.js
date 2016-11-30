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
    $("button#add-group-btn").click(function () {

        var name = $("input#name").val();
        var description = $("input#description").val();

        if (!name) {
            triggerError($("input#name"),"Group Name is a required field. It cannot be empty.");
            return false;
        } else if (!inputIsValid($("input#name").data("regex"), name)) {
            triggerError($("input#name"),$("input#name").data("errormsg"));
            return false;
        } else {
            var group = {"name": name, "description": description};

            var successCallback = function (jqXHR, status, resp) {
                if (resp.status == 201) {
                    $('.wr-validation-summary strong').text("Group created. You will be redirected to groups");
                    $('.wr-validation-summary').removeClass("hidden");
                    $('.wr-validation-summary strong').removeClass("label-danger");
                    $('.wr-validation-summary strong').addClass("label-success");
                    setTimeout(function () {
                        window.location = "../groups";
                    }, 1500);
                } else {
                    displayErrors(resp.status);
                }
            };

            invokerUtil.post("/api/device-mgt/v1.0/groups", group,
                             successCallback, function (message) {
                        displayErrors(message);
                    });

            return false;
        }
    });
});

/**
 * @param el
 * @param errorMsg
 *
 * Triggers validation error for provided element.
 * Note : the basic jQuery validation elements should be present in the markup
 *
 */
function triggerError(el,errorMsg){
    var parent = el.parents('.form-group'),
        errorSpan = parent.find('span'),
        errorMsgContainer = parent.find('label');

    errorSpan.on('click',function(event){
        event.stopPropagation();
        removeErrorStyling($(this));
        el.unbind('.errorspace');
    });

    el.bind('focusin.errorspace',function(){
        removeErrorStyling($(this))
    }).bind('focusout.errorspace',function(){
        addErrorStyling($(this));
    }).bind('keypress.errorspace',function(){
        $(this).unbind('.errorspace');
        removeErrorStyling($(this));
    });

    errorMsgContainer.text(errorMsg);

    parent.addClass('has-error has-feedback');
    errorSpan.removeClass('hidden');
    errorMsgContainer.removeClass('hidden');

    function removeErrorStyling(el){
        var parent = el.parents('.form-group'),
            errorSpan = parent.find('span'),
            errorMsgContainer = parent.find('label');

        parent.removeClass('has-error has-feedback');
        errorSpan.addClass('hidden');
        errorMsgContainer.addClass('hidden');
    }

    function addErrorStyling(el){
        var parent = el.parents('.form-group'),
            errorSpan = parent.find('span'),
            errorMsgContainer = parent.find('label');

        parent.addClass('has-error has-feedback');
        errorSpan.removeClass('hidden');
        errorMsgContainer.removeClass('hidden');
    }
}

function displayErrors(message) {
    $('#error-msg').html(message.responseText);
    modalDialog.header('Unexpected error occurred!');
    modalDialog.content('<h4 id="error-msg"></h4>');
    modalDialog.footer('<div class="buttons"><a href="#" id="group-unexpected-error-link" class="btn-operations">Ok' +
        '</a></div>');
    modalDialog.showAsError();
    $("a#group-unexpected-error-link").click(function () {
        modalDialog.hide();
    });
}
