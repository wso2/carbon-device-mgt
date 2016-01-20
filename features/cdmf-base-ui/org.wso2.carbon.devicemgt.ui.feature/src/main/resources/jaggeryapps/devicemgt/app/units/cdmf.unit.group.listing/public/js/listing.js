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
 * Setting-up global variables.
 */
var groupCheckbox = "#ast-container .ctrl-wr-asset .itm-select input[type='checkbox']";
var assetContainer = "#ast-container";

/*
 * On Select All Groups button click function.
 *
 * @param button: Select All Groups button
 */
function selectAllDevices(button) {
    if (!$(button).data('select')) {
        $(groupCheckbox).each(function (index) {
            $(this).prop('checked', true);
            addGroupSelectedClass(this);
        });
        $(button).data('select', true);
        $(button).html('Deselect All Groups');
    } else {
        $(groupCheckbox).each(function (index) {
            $(this).prop('checked', false);
            addGroupSelectedClass(this);
        });
        $(button).data('select', false);
        $(button).html('Select All Groups');
    }
}

/*
 * On listing layout toggle buttons click function.
 *
 * @param view: Selected view type
 * @param selection: Selection button
 */
function changeDeviceView(view, selection) {
    $(".view-toggle").each(function () {
        $(this).removeClass("selected");
    });
    $(selection).addClass("selected");
    if (view == "list") {
        $(assetContainer).addClass("list-view");
    } else {
        $(assetContainer).removeClass("list-view");
    }
}

/*
 * Add selected style class to the parent element function.
 *
 * @param checkbox: Selected checkbox
 */
function addGroupSelectedClass(checkbox) {
    if ($(checkbox).is(":checked")) {
        $(checkbox).closest(".ctrl-wr-asset").addClass("selected device-select");
    } else {
        $(checkbox).closest(".ctrl-wr-asset").removeClass("selected device-select");
    }
}

function toTitleCase(str) {
    return str.replace(/\w\S*/g, function (txt) {
        return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
    });
}

function loadGroups() {
    var groupListing = $("#group-listing");
    var groupListingSrc = groupListing.attr("src");
    var currentUser = groupListing.data("currentUser");
    $.template("group-listing", groupListingSrc, function (template) {
        var serviceURL = "api/group/all";

        var getGroupsRequest = $.ajax({
                                          url: serviceURL,
                                          method: "GET"
                                      });

        getGroupsRequest.done(function (data) {
            data = JSON.parse(data);
            var viewModel = {};
            viewModel.groups = data.data;
            $('#group-grid').removeClass('hidden');
            var content = template(viewModel);
            $("#ast-container").html(content);

            /*
             * On group checkbox select add parent selected style class
             */
            $(groupCheckbox).click(function () {
                addGroupSelectedClass(this);
            });
            attachEvents();

            $('#group-grid').datatables_extended();
            $(".icon .text").res_text(0.2);
        });

    });
}

function openCollapsedNav() {
    $('.wr-hidden-nav-toggle-btn').addClass('active');
    $('#hiddenNav').slideToggle('slideDown', function () {
        if ($(this).css('display') == 'none') {
            $('.wr-hidden-nav-toggle-btn').removeClass('active');
        }
    });
}

/*
 * DOM ready functions.
 */
$(document).ready(function () {
    loadGroups();
    //$('#device-grid').datatables_extended();

    /* Adding selected class for selected devices */
    $(groupCheckbox).each(function () {
        addGroupSelectedClass(this);
    });

    /* for device list sorting drop down */
    $(".ctrl-filter-type-switcher").popover({
                                                html: true,
                                                content: function () {
                                                    return $("#content-filter-types").html();
                                                }
                                            });

    /* for data tables*/
    $('[data-toggle="tooltip"]').tooltip();

    $("[data-toggle=popover]").popover();

    $(".ctrl-filter-type-switcher").popover({
                                                html: true,
                                                content: function () {
                                                    return $('#content-filter-types').html();
                                                }
                                            });

    $('#nav').affix({
                        offset: {
                            top: $('header').height()
                        }
                    });

});

var modalPopup = ".wr-modalpopup";
var modalPopupContainer = modalPopup + " .modalpopup-container";
var modalPopupContent = modalPopup + " .modalpopup-content";
var body = "body";

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
 * Following functions should be triggered after AJAX request is made.
 */
function attachEvents() {
    /**
     * Following click function would execute
     * when a user clicks on "Share" link
     * on Group Management page in WSO2 IoT Server Console.
     */
    $(".view-group-link").click(function () {
        var groupId = $(this).data("groupid");
        window.location = "devices?groupId=" + groupId;
    });

    /**
     * Following click function would execute
     * when a user clicks on "Share" link
     * on Group Management page in WSO2 IoT Server Console.
     */
    $("a.share-group-link").click(function () {
        var groupId = $(this).data("groupid");
        var username = $("#group-listing").data("current-user");
        $(modalPopupContent).html($('#share-group-w1-modal-content').html());
        $('#user-names').html('<div style="height:100px" data-state="loading" data-loading-text="Loading..." data-loading-style="icon-only" data-loading-inverse="true"></div>');
        showPopup();
        $("a#share-group-next-link").hide();
        var userRequest = $.ajax({
                                     url: "api/user/all",
                                     method: "GET",
                                     contentType: "application/json",
                                     accept: "application/json"
                                 });
        userRequest.done(function (data, txtStatus, jqxhr) {
            var users = JSON.parse(data);
            var status = jqxhr.status;
            if (status == 200) {
                var str = '<br /><select id="share-user-selector" style="color:#3f3f3f;padding:5px;width:250px;">';
                var hasUsers = false;
                users = users.content;
                for (var user in users) {
                    if (users[user].username != username) {
                        str += '<option value="' + users[user].username + '">' + users[user].username + '</option>';
                        hasUsers = true;
                    }
                }
                str += '</select>';
                if (!hasUsers) {
                    str = "There is no any other users registered";
                    $('#user-names').html(str);
                    return;
                }
                $('#user-names').html(str);
                $("a#share-group-next-link").show();
                $("a#share-group-next-link").click(function () {
                    var selectedUser = $('#share-user-selector').val();
                    $(modalPopupContent).html($('#share-group-w2-modal-content').html());
                    $('#user-roles').html('<div style="height:100px" data-state="loading" data-loading-text="Loading..." data-loading-style="icon-only" data-loading-inverse="true"></div>');
                    $("a#share-group-yes-link").hide();

                    var roleMappingRequest = $.ajax({
                                                        url: "api/group/id/" + groupId + "/" + selectedUser + "/rolemapping",
                                                        method: "GET",
                                                        contentType: "application/json",
                                                        accept: "application/json"
                                                    });

                    roleMappingRequest.done(function (data, txtStatus, jqxhr) {
                        var roleMap = JSON.parse(data);
                        var status = jqxhr.status;
                        if (status == 200) {
                            roleMap = roleMap.data;
                            var str = '';
                            var isChecked = '';
                            var hasRoles;
                            for (var role in roleMap) {
                                if (roleMap[role].assigned == true) {
                                    isChecked = 'checked';
                                }
                                str += '<label class="checkbox-text"><input type="checkbox" id="user-role-' + roleMap[role].role + '" value="' + roleMap[role].role
                                       + '" ' + isChecked + '/>' + roleMap[role].role + '</label>&nbsp;&nbsp;&nbsp;&nbsp;';
                                hasRoles = true;
                            }
                            if (!hasRoles) {
                                str = "There is no any roles for this group";
                                return;
                            }
                            $('#user-roles').html(str);
                            $("a#share-group-yes-link").show();
                            $("a#share-group-yes-link").click(function () {
                                var updatedRoleMap = [];
                                for (var role in roleMap) {
                                    if ($('#user-role-' + roleMap[role].role).is(':checked') != roleMap[role].assigned) {
                                        roleMap[role].assigned = $('#user-role-' + roleMap[role].role).is(':checked');
                                        updatedRoleMap.push(roleMap[role]);
                                    }
                                }

                                var roleUpdateRequest = $.ajax({
                                                                   url: "api/group/id/" + groupId + "/" + selectedUser + "/roleupdate",
                                                                   method: "POST",
                                                                   contentType: "application/json",
                                                                   accept: "application/json",
                                                                   data: JSON.stringify(updatedRoleMap)
                                                               });

                                roleUpdateRequest.done(function (data, txtStatus,
                                                                 jqxhr) {
                                    var status = jqxhr.status;
                                    if (status == 200) {
                                        $(modalPopupContent).html($('#share-group-200-content').html());
                                        setTimeout(function () {
                                            hidePopup();
                                            location.reload(false);
                                        }, 2000);
                                    } else {
                                        displayErrors(status);
                                    }
                                });

                                roleUpdateRequest.fail(function (jqXHR) {
                                    displayErrors(jqXHR);
                                });
                            });
                        } else {
                            displayErrors(status);
                        }
                    });

                    roleMappingRequest.fail(function (jqXHR) {
                        displayErrors(jqXHR);
                    });

                    $("a#share-group-w2-cancel-link").click(function () {
                        hidePopup();
                    });
                });
            } else {
                displayErrors(status);
            }
        });
        userRequest.fail(function (jqXHR) {
            displayErrors(jqXHR);
        });

        $("a#share-group-w1-cancel-link").click(function () {
            hidePopup();
        });

    });

    /**
     * Following click function would execute
     * when a user clicks on "Remove" link
     * on Group Management page in WSO2 IoT Server Console.
     */
    $("a.remove-group-link").click(function () {
        var groupId = $(this).data("groupid");
        var removeGroupApi = "api/group/id/" + groupId + "/remove";

        $(modalPopupContent).html($('#remove-group-modal-content').html());
        showPopup();

        $("a#remove-group-yes-link").click(function () {
            var deleteRequest = $.ajax({
                                           url: removeGroupApi,
                                           method: "DELETE",
                                           contentType: "application/json",
                                           accept: "application/json"
                                       });

            deleteRequest.done(function (data, txtStatus, jqxhr) {
                var status = jqxhr.status;
                if (status == 200) {
                    $(modalPopupContent).html($('#remove-group-200-content').html());
                    setTimeout(function () {
                        hidePopup();
                        location.reload(false);
                    }, 2000);
                } else {
                    displayErrors(status);
                }
            });

            deleteRequest.fail(function (jqXHR) {
                displayErrors(jqXHR);
            });
        });

        $("a#remove-group-cancel-link").click(function () {
            hidePopup();
        });

    });

    /**
     * Following click function would execute
     * when a user clicks on "Edit" link
     * on Device Management page in WSO2 MDM Console.
     */
    $("a.edit-group-link").click(function () {
        var groupId = $(this).data("groupid");
        var groupName = $(this).data("groupname");
        var groupDescription = $(this).data("groupdescription");
        var editGroupApi = "api/group/id/" + groupId + "/update";

        $(modalPopupContent).html($('#edit-group-modal-content').html());
        $('#edit-group-name').val(groupName);
        $('#edit-group-description').val(groupDescription);
        showPopup();

        $("a#edit-group-yes-link").click(function () {
            var newGroupName = $('#edit-group-name').val();
            var newGroupDescription = $('#edit-group-description').val();
            var group = {"name": newGroupName, "description": newGroupDescription};

            var groupUpdateRequest = $.ajax({
                                                url: editGroupApi,
                                                method: "POST",
                                                contentType: "application/json",
                                                accept: "application/json",
                                                data: JSON.stringify(group)
                                            });

            groupUpdateRequest.done(function (data, txtStatus, jqxhr) {
                var status = jqxhr.status;
                if (status == 200) {
                    $(modalPopupContent).html($('#edit-group-200-content').html());
                    $("h4[data-groupid='" + groupId + "']").html(newGroupName);
                    setTimeout(function () {
                        hidePopup();
                    }, 2000);
                } else {
                    displayErrors(status);
                }
            });

            groupUpdateRequest.fail(function (jqXHR) {
                displayErrors(jqXHR);
            });
        });

        $("a#edit-group-cancel-link").click(function () {
            hidePopup();
        });
    });
}

function displayErrors(jqXHR) {
    showPopup();
    if (jqXHR.status == 400) {
        $(modalPopupContent).html($('#group-400-content').html());
        $("a#group-400-link").click(function () {
            hidePopup();
        });
    } else if (jqXHR.status == 403) {
        $(modalPopupContent).html($('#group-403-content').html());
        $("a#group-403-link").click(function () {
            hidePopup();
        });
    } else if (jqXHR.status == 409) {
        $(modalPopupContent).html($('#group-409-content').html());
        $("a#group-409-link").click(function () {
            hidePopup();
        });
    } else {
        $(modalPopupContent).html($('#group-unexpected-error-content').html());
        $("a#group-unexpected-error-link").click(function () {
            hidePopup();
        });
        console.log("Error code: " + jqXHR.status);
    }
}