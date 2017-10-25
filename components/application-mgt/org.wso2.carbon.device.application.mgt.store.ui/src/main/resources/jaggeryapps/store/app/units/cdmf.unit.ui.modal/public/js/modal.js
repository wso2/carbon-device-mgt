/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


var modalDialog = (function () {
    var publicMethoads = {};
    publicMethoads.header = function (headerText) {
        $("#basic-modal-view #modal-title-text").html(headerText);

    };
    publicMethoads.content = function (contentText) {
        $("#basic-modal-view #modal-content-text").html(contentText);

    };
    publicMethoads.footer = function (footerContent) {
        $("#basic-modal-view #modal-footer-content").html(footerContent);

    };
    publicMethoads.footerButtons = function (buttonList) {
        var footerContent = "";
        for (var btn in buttonList) {
            footerContent = footerContent + '<div class="buttons"><a href="#" id="' + btn.id +
                '" class="btn-operations">' + btn.text + '</a></div>';
        }
        $("#basic-modal-view #modal-footer-content").html(footerContent);
    };
    publicMethoads.show = function () {

        $(".error-msg-icon").addClass("hidden");
        $(".warning-msg-icon").addClass("hidden");
        $("#basic-modal-view").removeClass('hidden');
        $("#basic-modal-view").modal('show');

    };
    publicMethoads.showAsError = function () {
        $(".error-msg-icon").removeClass("hidden");
        $("#basic-modal-view").removeClass('hidden');
        $("#basic-modal-view").modal('show');

    };
    publicMethoads.showAsAWarning = function () {
        $(".warning-msg-icon").removeClass("hidden");
        $("#basic-modal-view").removeClass('hidden');
        $("#basic-modal-view").modal('show');

    };
    publicMethoads.hide = function () {
        $("#basic-modal-view").addClass('hidden');
        $("#basic-modal-view").modal('hide');
        $('.modal-backdrop').remove();
    };

    $("#basic-modal-view").on('hidden.bs.modal', function () {
        $('#basic-modal-view .modal-dialog').html('<div class="modal-content"><div class="modal-header">' +
            '<h3 class="pull-left modal-title"><span class="fw-stack error-msg-icon hidden">' +
            '<i class="fw fw-circle-outline fw-stack-2x"></i><i class="fw fw-error fw-stack-1x"></i></span>' +
            '<span class="fw-stack warning-msg-icon hidden"><i class="fw fw-circle-outline fw-stack-2x"></i>' +
            '<i class="fw fw-warning fw-stack-1x"></i></span><span id="modal-title-text"></span></h3>' +
            '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><i class="fw fw-cancel"></i>' +
            '</button></div><div class="modal-body add-margin-top-2x add-margin-bottom-2x">' +
            '<div id="modal-content-text"></div></div>' +
            '<div class="modal-footer" id="modal-footer-content"></div></div>');
        $('body').removeClass('modal-open').css('padding-right', '0px');
    });

    return publicMethoads;
}(modalDialog));