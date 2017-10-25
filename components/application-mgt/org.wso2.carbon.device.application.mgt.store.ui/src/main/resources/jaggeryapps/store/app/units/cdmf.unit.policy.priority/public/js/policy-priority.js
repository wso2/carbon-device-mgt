/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/* sorting function */
var sortUpdateBtn = "#sortUpdateBtn";
var applyChangesBtn = "#applyChangesBtn";
var sortedIDs;

var saveNewPrioritiesButtonEnabled = Boolean($(sortUpdateBtn).data("enabled"));
if (saveNewPrioritiesButtonEnabled) {
    $(sortUpdateBtn).removeClass("hide");
}

/**
 * Modal related stuff are as follows.
 */

var modalPopup = ".modal";
var modalPopupContainer = modalPopup + " .modal-content";
var modalPopupContent = modalPopup + " .modal-content";
var body = "body";

/*
 * set popup maximum height function.
 */
function setPopupMaxHeight() {
    var maxHeight = "max-height";
    var marginTop = "margin-top";
    var body = "body";
    $(modalPopupContent).css(maxHeight, ($(body).height() - ($(body).height() / 100 * 30)));
    $(modalPopupContainer).css(marginTop, (-($(modalPopupContainer).height() / 2)));
}

/*
 * show popup function.
 */
function showPopup() {
    $(modalPopup).modal('show');
    //setPopupMaxHeight();
}

/*
 * hide popup function.
 */
function hidePopup() {
    $(modalPopupContent).html('');
    $(modalPopup).modal('hide');
    $('body').removeClass('modal-open').css('padding-right','0px');
    $('.modal-backdrop').remove();
}

$(document).ready(function () {
    // Click functions related to Policy Listing
    $(sortUpdateBtn).click(function () {
        $(sortUpdateBtn).prop("disabled", true);

        var newPolicyPriorityList = [];
        var policy;
        var i;
        var sortedItems = sortableListFunction.getSortedItems();
        for (i = 0; i < sortedItems.length; i++) {
            policy = {};
            policy.id = parseInt(sortedItems[i]);
            policy.priority = i + 1;
            newPolicyPriorityList.push(policy);
        }

        var updatePolicyAPI = "/api/device-mgt/v1.0/policies/priorities";
        invokerUtil.put(
            updatePolicyAPI,
            newPolicyPriorityList,
            // on success
            function (data, textStatus, jqXHR) {
                if (jqXHR.status == 200) {
                    modalDialog.header('Done. New Policy priorities were successfully updated.');
                    modalDialog.footer('<div class="buttons"><a href="javascript:void(0)" ' +
                        'id="save-policy-priorities-success-link" class="btn-operations">Ok</a></div>');
                    modalDialog.show();
                    $("a#save-policy-priorities-success-link").click(function () {
                        modalDialog.hide();
                    });
                    $(applyChangesBtn).prop("disabled", false);
                }
            },
            // on error
            function (jqXHR) {
                if (jqXHR.status == 400 || jqXHR.status == 500) {
                    modalDialog.header('An unexpected error occurred. Please try again later.');
                    modalDialog.footer('<div class="buttons"><a href="javascript:void(0)" ' +
                        'id="save-policy-priorities-error-link" class="btn-operations">Ok</a></div>');
                    modalDialog.showAsError();                    
                    $("a#save-policy-priorities-error-link").click(function () {
                        modalDialog.hide();
                    });
                }
            }
        );
    });

    $(applyChangesBtn).click(function () {
        var applyPolicyChangesAPI = "/api/device-mgt/v1.0/policies/apply-changes";
        modalDialog.header('Do you really want to apply changes to all policies?');
        modalDialog.footer('<div class="buttons"><a href="javascript:void(0)" id="apply-changes-yes-link" ' +
            'class="btn-operations">Yes</a><a href="javascript:void(0)" id="apply-changes-cancel-link" ' +
            'class="btn-operations">No</a></div>');
        modalDialog.show();

        $("a#apply-changes-yes-link").click(function () {
            invokerUtil.put(
                applyPolicyChangesAPI,
                null,
                // on success
                function (data, textStatus, jqXHR) {
                    if (jqXHR.status == 200) {
                        modalDialog.header('Done. Changes applied successfully.');
                        modalDialog.footer('<div class="buttons"><a href="javascript:void(0)" ' +
                            'id="apply-changes-success-link" class="btn-operations">Ok</a></div>');
                        modalDialog.show();
                        $("a#apply-changes-success-link").click(function () {
                            modalDialog.hide();
                        });
                    }
                },
                // on error
                function (jqXHR) {
                    if (jqXHR.status == 500) {
                        modalDialog.header('An unexpected error occurred. Please try again later.');
                        modalDialog.footer('<div class="buttons"><a href="javascript:void(0)" ' +
                            'id="apply-changes-error-link" class="btn-operations">Ok</a></div>');
                        modalDialog.showAsError();
                        $("a#apply-changes-error-link").click(function () {
                            modalDialog.hide();
                        });
                    }
                }
            );
        });

        $("a#apply-changes-cancel-link").click(function () {
            modalDialog.hide();
        });
    });

});
