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
var loadRoleBasedActionURL = function (action, rolename) {
    var href = $("#ast-container").data("app-context") + "role/" + action + "/" + rolename;
    $(location).attr('href', href);
};

/**
 * Following function would execute
 * when a user clicks on the list item
 * initial mode and with out select mode.
 */
function InitiateViewOption() {
    $(location).attr('href', $(this).data("url"));
}

/**
 * Sorting function of roles
 * listed on Role Management page in WSO2 Devicemgt Console.
 */

var loadPaginatedObjects = function (objectGridId, objectGridContainer, objectGridTemplateSrc, serviceURL, callback) {
    var templateSrc = $(objectGridTemplateSrc).attr("src");
    $.template(objectGridId, templateSrc, function (template) {
        invokerUtil.get(serviceURL,
                        function (data) {
                            data = callback(data);
                            if (data.length > 0 && data != null) {
                                $('#ast-container').removeClass('hidden');
                                $('#role-listing-status-msg').text('');
                                for (var i = 0; i < data.viewModel.roles.length; i++) {
                                   data.viewModel.roles[i].adminRole = $("#role-table").data("role");
                                }
                                var content = template(data.viewModel);
                                $(objectGridContainer).html(content);
                                if (isInit) {
                                    $('#role-grid').datatables_extended();
                                    isInit = false;
                                }
                                $("#dt-select-all").addClass("hidden");
                                $(".icon .text").res_text(0.2);
                            } else {
                                $('#ast-container').addClass('hidden');
                                $('#role-listing-status-msg').text('No roles are available to be displayed.');
                            }

                            //$(objectGridId).datatables_extended();
                        }, function (message) {
                $('#ast-container').addClass('hidden');
                $('#role-listing-status-msg').text('Invalid search query. Try again with a valid search query');
            });
    });
};

function loadRoles(searchQuery) {
    var loadingContent = $("#loading-content");
    loadingContent.show();
    var serviceURL = "/devicemgt_admin/roles";
    if (searchQuery) {
        serviceURL = serviceURL + "/search?filter=" + searchQuery;
    }
    var callback = function (data) {
        if (data != null || data == "null") {
            data = JSON.parse(data);
            var canRemove = $("#can-remove").val();
            var canEdit = $("#can-edit").val();
            var roles = [];
            for(var i=0; i<data.responseContent.length; i++) {
                roles.push({"roleName":data.responseContent[i]});
                if(canRemove != null && canRemove != undefined) {
                    roles[i].canRemove = true;
                }

                if(canEdit != null && canEdit != undefined) {
                    roles[i].canEdit = true;
                }
            }

            data = {
                "viewModel": {
                    "roles": roles
                },
                "length": roles.length
            }
        }
        return data;
    };

    loadPaginatedObjects("#role-grid", "#ast-container", "#role-listing", serviceURL, callback);
    loadingContent.hide();
    var sortableElem = '.wr-sortable';
    $(sortableElem).sortable({
        beforeStop: function () {
            var sortedIDs = $(this).sortable('toArray');
        }
    });
    $(sortableElem).disableSelection();
}

var modalPopup = ".wr-modalpopup";
var modalPopupContainer = modalPopup + " .modalpopup-container";
var modalPopupContent = modalPopup + " .modalpopup-content";
var body = "body";
var isInit = true;


/*
 * set popup maximum height function.
 */
function setPopupMaxHeight() {
    $(modalPopupContent).css('max-height', ($(body).height() - ($(body).height() / 100 * 30)));
    $(modalPopupContainer).css('margin-top', (-($(modalPopupContainer).height() / 2)));
}

/*
 * show popup function.
 */
function showPopup() {
    $(modalPopup).show();
    setPopupMaxHeight();
}

/*
 * hide popup function.
 */
function hidePopup() {
    $(modalPopupContent).html('');
    $(modalPopup).hide();
}

/**
 * Following click function would execute
 * when a user clicks on "Remove" link
 * on Role Listing page in WSO2 Devicemgt Console.
 */
$("#role-grid").on("click", ".remove-role-link", function () {
    var role = $(this).data("role");
    var removeRoleAPI = "/devicemgt_admin/roles?rolename=" + role;

    $(modalPopupContent).html($('#remove-role-modal-content').html());
    showPopup();

    $("a#remove-role-yes-link").click(function () {
        invokerUtil.delete(
            removeRoleAPI,
            function () {
                $("#role-" + role).remove();
                $(modalPopupContent).html($('#remove-role-success-content').html());
                $("a#remove-role-success-link").click(function () {
                    hidePopup();
                });
            },
            function () {
                $(modalPopupContent).html($('#remove-role-error-content').html());
                $("a#remove-role-error-link").click(function () {
                    hidePopup();
                });
            }
        );
    });

    $("a#remove-role-cancel-link").click(function () {
        hidePopup();
    });
});

$("#search-btn").click(function () {
    var searchQuery = $("#search-by-name").val();
    if (searchQuery.trim() != "") {
        loadRoles(searchQuery);
    } else {
        loadRoles();
    }
});

$(document).ready(function () {
    loadRoles();
    isInit = true;
});