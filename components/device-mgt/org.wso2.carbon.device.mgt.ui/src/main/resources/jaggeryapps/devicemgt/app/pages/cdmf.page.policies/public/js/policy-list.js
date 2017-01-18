/*
 * Copyright (c) 2015-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
// var sortedIDs;
// var dataTableSelection = ".DTTT_selected";
var settings = {
    "sorting": false
};
$('#policy-grid').datatables_extended(settings);
// $(".icon .text").res_text(0.2);

var saveNewPrioritiesButton = "#save-new-priorities-button";
var saveNewPrioritiesButtonEnabled = Boolean($(saveNewPrioritiesButton).data("enabled"));
if (saveNewPrioritiesButtonEnabled) {
    $(saveNewPrioritiesButton).removeClass("hide");
}

/**
 * Following function would execute
 * when a user clicks on the list item
 * initial mode and with out select mode.
 */
function InitiateViewOption() {
    $(location).attr('href', $(this).data("url"));
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
}

/*
 * hide popup function.
 */
function hidePopup() {
    $(modalPopupContent).html("");
    $(modalPopupContent).removeClass("operation-data");
    $(modalPopup).modal('hide');
    $('body').removeClass('modal-open').css('padding-right','0px');
    $('.modal-backdrop').remove();
}

/*
 * Function to get selected policies.
 */
function getSelectedPolicyStates() {
    var policyList = [];
    var thisTable = $(".DTTT_selected").closest('.dataTables_wrapper').find('.dataTable').dataTable();
    thisTable.api().rows().every(function () {
        if ($(this.node()).hasClass('DTTT_selected')) {
            policyList.push($(thisTable.api().row(this).node()).data('status'));
        }
    });

    return policyList;
}

/*
 * Function to get selected policies.
 */
function getSelectedPolicies() {
    var policyList = [];
    var thisTable = $(".DTTT_selected").closest('.dataTables_wrapper').find('.dataTable').dataTable();
    thisTable.api().rows().every(function () {
        if ($(this.node()).hasClass('DTTT_selected')) {
            policyList.push($(thisTable.api().row(this).node()).data('id'));
        }
    });

    return policyList;
}


$(document).ready(function () {

    /**
     * ********************************************
     * Click functions related to Policy Listing
     * ********************************************
     */

    // [1] logic for running apply-changes-for-devices use-case

    var applyChangesButtonId = "#appbar-btn-apply-changes";

    var isUpdated = $("#is-updated").val();
    if (!isUpdated) {
        // if no updated policies found, hide button from app bar
        $(applyChangesButtonId).addClass("hidden");
    } else {
        // if updated policies found, show button from app bar
        $(applyChangesButtonId).removeClass("hidden");
    }

    // click-event function for applyChangesButton
    $(applyChangesButtonId).click(function () {
        var serviceURL = "/api/device-mgt/v1.0/policies/apply-changes";
        modalDialog.header('Do you really want to apply changes to all policies?');
        modalDialog.footer('<div class="buttons"><a href="#" id="change-policy-yes-link" class="btn-operations">Yes' +
            '</a><a href="#" id="change-policy-cancel-link" class="btn-operations btn-default">No</a></div>');
        modalDialog.show();

        $("a#change-policy-yes-link").click(function () {
            invokerUtil.put(
                serviceURL,
                null,
                // on success
                function (data, textStatus, jqXHR) {
                    if (jqXHR.status == 200) {
                        modalDialog.header('Done. Changes applied successfully.');
                        modalDialog.footer('<div class="buttons"><a href="#" id="change-policy-success-link" ' +
                            'class="btn-operations">Ok</a></div>');
                        $("a#change-policy-success-link").click(function () {
                            modalDialog.hide();
                            location.reload();
                        });
                    }
                },
                // on error
                function (jqXHR) {
                    console.log(stringify(jqXHR.data));
                    modalDialog.header('An unexpected error occurred. Please try again later.');
                    modalDialog.footer('<div class="buttons"><a href="#" id="change-policy-error-link" ' +
                        'class="btn-operations">Ok</a></div>');
                    modalDialog.showAsError();
                    $("a#change-policy-error-link").click(function () {
                        modalDialog.hide();
                    });
                }
            );
        });

        $("a#change-policy-cancel-link").click(function () {
            modalDialog.hide();
        });
    });

    // [2] logic for un-publishing a selected set of Active, Active/Updated policies

    $(".policy-unpublish-link").click(function () {
        var policyList = getSelectedPolicies();
        var statusList = getSelectedPolicyStates();
        if (($.inArray("Inactive/Updated", statusList) > -1) || ($.inArray("Inactive", statusList) > -1)) {
            // if policies found in Inactive or Inactive/Updated states with in the selection,
            // pop-up an error saying
            // "You cannot select already inactive policies. Please deselect inactive policies and try again."
            modalDialog.header('Action cannot be performed !');
            modalDialog.content('You cannot select already inactive policies to be unpublished. Please deselect ' +
                'inactive policies and try again.');
            modalDialog.footer('<div class="buttons"><a href="javascript:modalDialog.hide()" class="btn-operations">Ok</a>' +
                '</div>');
            modalDialog.showAsAWarning();
        } else {
            var serviceURL = "/api/device-mgt/v1.0/policies/deactivate-policy";
            if (policyList.length == 0) {
                modalDialog.header('Action cannot be performed !');
                modalDialog.content('Please select a policy or a list of policies to un-publish.');
                modalDialog.footer('<div class="buttons"><a href="javascript:modalDialog.hide()" ' +
                    'class="btn-operations">Ok</a></div>');
                modalDialog.showAsAWarning();
            } else {
                modalDialog.header('Do you really want to unpublish the selected policy(s)?');
                modalDialog.footer('<div class="buttons"><a href="#" id="unpublish-policy-yes-link" ' +
                    'class="btn-operations">Yes</a><a href="#" id="unpublish-policy-cancel-link" ' +
                    'class="btn-operations btn-default">No</a></div>');
                modalDialog.show();
            }

            // on-click function for policy un-publishing "yes" button
            $("a#unpublish-policy-yes-link").click(function () {
                invokerUtil.post(
                    serviceURL,
                    policyList,
                    // on success
                    function (data, textStatus, jqXHR) {
                        if (jqXHR.status == 200 && data) {
                            modalDialog.header('Done. Selected policy was successfully unpublished.');
                            modalDialog.footer('<div class="buttons"><a href="#" id="unpublish-policy-success-link" ' +
                                'class="btn-operations">Ok</a></div>');
                            $("a#unpublish-policy-success-link").click(function () {
                                modalDialog.hide();
                                location.reload();
                            });
                        }
                    },
                    // on error
                    function (jqXHR) {
                        console.log(stringify(jqXHR.data));
                        modalDialog.header('An unexpected error occurred. Please try again later.');
                        modalDialog.footer('<div class="buttons"><a href="#" id="unpublish-policy-error-link" ' +
                            'class="btn-operations">Ok</a></div>');
                        modalDialog.showAsError();
                        $("a#unpublish-policy-error-link").click(function () {
                            modalDialog.hide();
                        });
                    }
                );
            });

            // on-click function for policy un-publishing "cancel" button
            $("a#unpublish-policy-cancel-link").click(function () {
                modalDialog.hide();
            });
        }
    });

    // [3] logic for publishing a selected set of Inactive, Inactive/Updated policies

    $(".policy-publish-link").click(function () {
        var policyList = getSelectedPolicies();
        var statusList = getSelectedPolicyStates();
        if (($.inArray("Active/Updated", statusList) > -1) || ($.inArray("Active", statusList) > -1)) {
            // if policies found in Active or Active/Updated states with in the selection,
            // pop-up an error saying
            // "You cannot select already active policies. Please deselect active policies and try again."
            modalDialog.header('Action cannot be performed !');
            modalDialog.content('You cannot select already active policies. Please deselect active policies and try ' +
                'again.');
            modalDialog.footer('<div class="buttons"><a href="javascript:modalDialog.hide()" class="btn-operations">' +
                'Ok</a></div>');
            modalDialog.showAsAWarning();
        } else {
            var serviceURL = "/api/device-mgt/v1.0/policies/activate-policy";
            if (policyList.length == 0) {
                modalDialog.header('Action cannot be performed !');
                modalDialog.content('Please select a policy or a list of policies to publish.');
                modalDialog.footer('<div class="buttons"><a href="javascript:modalDialog.hide()" ' +
                    'class="btn-operations">Ok</a></div>');
                modalDialog.showAsAWarning();
            } else {
                modalDialog.header('Do you really want to publish the selected policy(s)?');
                modalDialog.footer('<div class="buttons"><a href="#" id="publish-policy-yes-link" ' +
                    'class="btn-operations">Yes</a><a href="#" id="publish-policy-cancel-link" ' +
                    'class="btn-operations btn-default">No</a></div>');
                modalDialog.show();
            }
            // on-click function for policy removing "yes" button
            $("a#publish-policy-yes-link").click(function () {
                invokerUtil.post(
                    serviceURL,
                    policyList,
                    // on success
                    function (data, textStatus, jqXHR) {
                        if (jqXHR.status == 200 && data) {
                            modalDialog.header('Done. Selected policy was successfully published.');
                            modalDialog.footer('<div class="buttons"><a href="#" id="publish-policy-success-link" ' +
                                'class="btn-operations">Ok</a></div>');
                            $("a#publish-policy-success-link").click(function () {
                                modalDialog.hide();
                                location.reload();
                            });
                        }
                    },
                    // on error
                    function (jqXHR) {
                        console.log(stringify(jqXHR.data));
                        modalDialog.header('An unexpected error occurred. Please try again later.');
                        modalDialog.footer('<div class="buttons"><a href="#" id="publish-policy-error-link" ' +
                            'class="btn-operations">Ok</a></div>');
                        modalDialog.showAsError();
                        $("a#publish-policy-error-link").click(function () {
                            modalDialog.hide();
                        });
                    }
                );
            });

            // on-click function for policy removing "cancel" button
            $("a#publish-policy-cancel-link").click(function () {
                modalDialog.hide();
            });
        }
    });

    // [4] logic for removing a selected set of policies

    $(".policy-remove-link").click(function () {
        var policyList = getSelectedPolicies();
        var statusList = getSelectedPolicyStates();
        if (($.inArray("Active/Updated", statusList) > -1) || ($.inArray("Active", statusList) > -1)) {
            // if policies found in Active or Active/Updated states with in the selection,
            // pop-up an error saying
            // "You cannot remove already active policies. Please deselect active policies and try again."
            modalDialog.header('Action cannot be performed !');
            modalDialog.content('You cannot select already active policies. Please deselect active policies and try ' +
                'again.');
            modalDialog.footer('<div class="buttons"><a href="javascript:modalDialog.hide()" class="btn-operations">' +
                'Ok</a></div>');
            modalDialog.showAsAWarning();
        } else {
            var serviceURL = "/api/device-mgt/v1.0/policies/remove-policy";
            if (policyList.length == 0) {
                modalDialog.header('Action cannot be performed !');
                modalDialog.content('Please select a policy or a list of policies to remove.');
                modalDialog.footer('<div class="buttons"><a href="javascript:modalDialog.hide()" ' +
                    'class="btn-operations">Ok</a></div>');
                modalDialog.showAsAWarning();
            } else {
                modalDialog.header('Do you really want to remove the selected policy(s)?');
                modalDialog.footer('<div class="buttons"><a href="#" id="remove-policy-yes-link" class=' +
                    '"btn-operations">Remove</a> <a href="#" id="remove-policy-cancel-link" ' +
                    'class="btn-operations btn-default">Cancel</a></div>');
                modalDialog.show();
            }

            // on-click function for policy removing "yes" button
            $("a#remove-policy-yes-link").click(function () {
                invokerUtil.post(
                    serviceURL,
                    policyList,
                    // on success
                    function (data, textStatus, jqXHR) {
                        if (jqXHR.status == 200 && data) {
                            modalDialog.header('Done. Selected policy was successfully removed.');
                            modalDialog.footer('<div class="buttons"><a href="#" id="remove-policy-success-link" ' +
                                'class="btn-operations">Ok</a></div>');
                            $("a#remove-policy-success-link").click(function () {
                                modalDialog.hide();
                                location.reload();
                            });
                        }
                    },
                    // on error
                    function (jqXHR) {
                        console.log(stringify(jqXHR.data));
                        modalDialog.header('An unexpected error occurred. Please try again later.');
                        modalDialog.footer('<div class="buttons"><a href="#" id="remove-policy-error-link" ' +
                            'class="btn-operations">Ok</a></div>');
                        modalDialog.showAsError();
                        $("a#remove-policy-error-link").click(function () {
                            modalDialog.hide();
                        });
                    }
                );
            });

            // on-click function for policy removing "cancel" button
            $("a#remove-policy-cancel-link").click(function () {
                modalDialog.hide();
            });
        }
    });

    $("#loading-content").remove();
    if ($("#policy-listing-status-msg").text()) {
        $("#policy-listing-status").removeClass("hidden");
    }
    $("#policy-grid").removeClass("hidden");
});