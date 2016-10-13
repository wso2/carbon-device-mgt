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
        $("#modal-title-text").html(headerText);

    };
    publicMethoads.content = function (contentText) {
        $("#modal-content-text").html(contentText);

    };
    publicMethoads.footer = function (footerContent) {
        $("#modal-footer-content").html(footerContent);

    };
    publicMethoads.footerButtons = function (buttonList) {
        var footerContent = "";
        for (var btn in buttonList) {
            footerContent = footerContent + '<div class="buttons"><a href="#" id="' + btn.id +
                '" class="btn-operations">' + btn.text + '</a></div>';
        }
        $("#modal-footer-content").html(footerContent);
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
        $("#modal-title-text").html("");
        $("#modal-content-text").html("");
        $("#modal-footer-content").html("");
        $('body').removeClass('modal-open').css('padding-right', '0px');
        $('.modal-backdrop').remove();
    };
    return publicMethoads;
}(modalDialog));