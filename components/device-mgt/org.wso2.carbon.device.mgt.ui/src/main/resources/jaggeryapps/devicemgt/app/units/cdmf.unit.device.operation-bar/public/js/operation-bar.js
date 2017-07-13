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

/*
 * On operation click function.
 * @param selection: Selected operation
 */
function operationSelect(selection) {
    $(modalPopupContent).addClass("operation-data");
    $(modalPopupContent).html($(" .operation[data-operation-code=" + selection + "]").html());
    $(modalPopupContent).data("operation-code", selection);
    showPopup();
}

function submitForm(formId) {
    var form = $("#" + formId);
    var uri = form.attr("action");
    var uriencodedQueryStr = "";
    var uriencodedFormStr = "";
    var payload = {};
    var isItemSelected;

    //setting responses callbacks
    var content = $("#operation-response-template").find(".content");
    var defaultStatusClasses = "fw fw-stack-1x";
    var title = content.find("#title");
    title.attr("class","center-block text-center");
    var statusIcon = content.find("#status-icon");
    var description = content.find("#description");

    form.find("input").each(function () {
        var input = $(this);
        if (input.data("param-type") == "path") {
            uri = uri.replace("{" + input.attr("id") + "}", input.val());
        } else if (input.data("param-type") == "query") {
            var prefix = (uriencodedQueryStr == "") ? "?" : "&";
            uriencodedQueryStr += prefix + input.attr("id") + "=" + input.val();
        } else if (input.data("param-type") == "form") {
            var prefix = (uriencodedFormStr == "") ? "" : "&";
            if (input.attr("type") == "checkbox" || input.attr("type") == "radio"){

                if (isItemSelected == undefined){
                    isItemSelected = false;
                }
                if (input.is(':checked')){
                    isItemSelected = true;
                    uriencodedFormStr += prefix + input.attr("name") + "=" + input.val();
                }
            }else{
                uriencodedFormStr += prefix + input.attr("id") + "=" + input.val();
            }
        }
    });

    if (isItemSelected === false){
        title.html("Please Select One Option");
        statusIcon.attr("class", defaultStatusClasses + " fw-error");
        $(modalPopupContent).html(content.html());
        return false;
    }

    uri += uriencodedQueryStr;
    var httpMethod = form.attr("method").toUpperCase();
    var contentType = form.attr("enctype");
    var featurePayload = form.attr("data-payload");
    if (featurePayload) {
        contentType = "application/json";
        payload = JSON.parse(atob(featurePayload));

    } else if (contentType == undefined || contentType.isEmpty()) {
        contentType = "application/x-www-form-urlencoded";
        payload = uriencodedFormStr;
    }

    var successCallBack = function (response) {
        var res = response;
        try {
            res = JSON.parse(response).messageFromServer;
        } catch (err) {
            //do nothing
        }
        title.html("Operation Triggered!");
        statusIcon.attr("class", defaultStatusClasses + " fw-check");
        description.html(res);
        $(modalPopupContent).html(content.html());
    };
    var errorCallBack = function (response) {
        title.html("An Error Occurred!");
        statusIcon.attr("class", defaultStatusClasses + " fw-error");
        var reason = (response.responseText == "null")?response.statusText:response.responseText;
        description.html(reason);
        $(modalPopupContent).html(content.html());
    };
    //executing http request
    if (httpMethod == "GET") {
        invokerUtil.get(uri, successCallBack, errorCallBack, contentType);
    } else if (httpMethod == "POST") {
        invokerUtil.post(uri, payload, successCallBack, errorCallBack, contentType);
    } else if (httpMethod == "PUT") {
        invokerUtil.put(uri, payload, successCallBack, errorCallBack, contentType);
    } else if (httpMethod == "DELETE") {
        invokerUtil.delete(uri, successCallBack, errorCallBack, contentType);
    } else {
        title.html("An Error Occurred!");
        statusIcon.attr("class", defaultStatusClasses + " fw-error");
        description.html("This operation requires http method: " + httpMethod + " which is not supported yet!");
        $(modalPopupContent).html(content.html());
    }
}

$(document).on('submit', 'form', function (e) {
    e.preventDefault();
    var postOperationRequest = $.ajax({
        url: $(this).attr("action") + '&' + $(this).serialize(),
        method: "post"
    });

    var btnSubmit = $('#btnSend', this);
    btnSubmit.addClass('hidden');

    var lblSending = $('#lblSending', this);
    lblSending.removeClass('hidden');

    var lblSent = $('#lblSent', this);
    postOperationRequest.done(function (data) {
        lblSending.addClass('hidden');
        lblSent.removeClass('hidden');
        setTimeout(function () {
            hidePopup();
        }, 3000);
    });

    postOperationRequest.fail(function (jqXHR, textStatus) {
        lblSending.addClass('hidden');
        lblSent.addClass('hidden');
    });
});