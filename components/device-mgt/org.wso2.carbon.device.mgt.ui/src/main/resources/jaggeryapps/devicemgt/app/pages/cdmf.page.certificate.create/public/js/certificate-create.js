/*
 Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

 WSO2 Inc. licenses this file to you under the Apache License,
 Version 2.0 (the "License"); you may not use this file except
 in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 either express or implied. See the License for the
 specific language governing permissions and limitations
 under the License.
 */
var pemContent = "";
var certificateName = "";
var errorMsgWrapper = "#certificate-create-error-msg";
var errorMsg = "#certificate-create-error-msg span";
var validateInline = {};
var clearInline = {};

var base_api_url = "/api/certificate-mgt/v1.0";

var enableInlineError = function(inputField, errorMsg, errorSign) {
    var fieldIdentifier = "#" + inputField;
    var errorMsgIdentifier = "#" + inputField + " ." + errorMsg;
    var errorSignIdentifier = "#" + inputField + " ." + errorSign;

    if (inputField) {
        $(fieldIdentifier).addClass(" has-error has-feedback");
    }

    if (errorMsg) {
        $(errorMsgIdentifier).removeClass(" hidden");
    }

    if (errorSign) {
        $(errorSignIdentifier).removeClass(" hidden");
    }
};

var disableInlineError = function(inputField, errorMsg, errorSign) {
    var fieldIdentifier = "#" + inputField;
    var errorMsgIdentifier = "#" + inputField + " ." + errorMsg;
    var errorSignIdentifier = "#" + inputField + " ." + errorSign;

    if (inputField) {
        $(fieldIdentifier).removeClass(" has-error has-feedback");
    }

    if (errorMsg) {
        $(errorMsgIdentifier).addClass(" hidden");
    }

    if (errorSign) {
        $(errorSignIdentifier).addClass(" hidden");
    }
};

function readSingleFile(evt) {
    var f = evt.target.files[0];
    if (f) {
        var r = new FileReader();
        r.onload = function(e) {
            var contents = e.target.result;
            if (f.type == "application/x-x509-ca-cert" || f.name.split('.').pop() == "pem") {
                pemContent = contents;
                certificateName = f.name;
                console.log(contents);
                console.log(pemContent);
                console.log(certificateName);
                pemContent = pemContent.substring(28, pemContent.length - 27);
                console.log(pemContent);
                $(errorMsgWrapper).addClass("hidden");
            } else {
                $(errorMsg).text("Certificate must be a .pem file containing a valid certificate data.");
                $(errorMsgWrapper).removeClass("hidden");
            }
        }
        r.readAsText(f);
    } else {
        //inline error
    }
}

$(document).ready(function() {
    $("#loading-content").show();
    pemContent = "";
    document.getElementById('certificate').addEventListener('change', readSingleFile, false);

    $('#certificate').change(function() {
        var i = $(this).next('label#placeholder').clone();
        var file = $('#certificate')[0].files[0].name;
        $(this).next('label#placeholder').text(file);
    });

    /**
     * Following click function would execute
     * when a user clicks on "Add Certificate" button.
     */
    $("button#add-certificate-btn").click(function() {
        if (!pemContent) {
            $(errorMsg).text(" .pem file must contains certificate information.");
            $(errorMsgWrapper).removeClass("hidden");
        } else {
            var addCertificateFormData = {};
            addCertificateFormData.pem = pemContent;
            addCertificateFormData.name = certificateName;
            var certificateList = [];
            certificateList.push(addCertificateFormData);

            var serviceUrl = base_api_url + "/admin/certificates";
            invokerUtil.post(
                serviceUrl,
                certificateList,
                function(data) {
                    // Refreshing with success message
                    $("#certificate-create-form").addClass("hidden");
                    $("#certificate-created-msg").removeClass("hidden");
                },
                function(data) {
                    if (data["status"] == 500) {
                        $(errorMsg).text("An unexpected error occurred at backend server. Please try again later.");
                    } else {
                        $(errorMsg).text(data);
                    }
                    $(errorMsgWrapper).removeClass("hidden");
                }
            );
        }
    });
    $("#loading-content").remove();
});