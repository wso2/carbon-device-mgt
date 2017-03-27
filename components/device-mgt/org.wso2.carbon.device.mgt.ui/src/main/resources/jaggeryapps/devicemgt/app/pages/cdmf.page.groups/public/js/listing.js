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
 * Following function would execute
 * when a user clicks on the list item
 * initial mode and with out select mode.
 */
function InitiateViewOption(url) {
    if ($(".select-enable-btn").text() == "Select") {
        url = $(this).parent().data("url");
        $(location).attr('href', url);
    }
}

(function () {
    var cache = {};
    var validateAndReturn = function (value) {
        return (value == undefined || value == null) ? "Unspecified" : value;
    };
    Handlebars.registerHelper("deviceMap", function (device) {
        device.owner = validateAndReturn(device.owner);
        device.ownership = validateAndReturn(device.ownership);
        var arr = device.properties;
        if (arr) {
            device.properties = arr.reduce(function (total, current) {
                total[current.name] = validateAndReturn(current.value);
                return total;
            }, {});
        }
    });
})();

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

function htmlspecialchars(text){
    return jQuery('<div/>').text(text).html();
}

function loadGroups() {
    var groupListing = $("#group-listing");
    var currentUser = groupListing.data("currentUser");
    var serviceURL;
    if ($.hasPermission("LIST_ALL_GROUPS")) {
        serviceURL = "/api/device-mgt/v1.0/admin/groups";
    } else if ($.hasPermission("LIST_GROUPS")) {
        //Get authenticated users groups
        serviceURL = "/api/device-mgt/v1.0/groups";
    } else {
        $("#loading-content").remove();
        $('#device-table').addClass('hidden');
        $('#device-listing-status-msg').text('Permission denied.');
        $("#device-listing-status").removeClass(' hidden');
        return;
    }

    var dataFilter = function (data) {
        data = JSON.parse(data);
        var objects = [];
        $(data.deviceGroups).each(function (index) {
            objects.push({
                             groupId: htmlspecialchars(data.deviceGroups[index].id),
                             name: htmlspecialchars(data.deviceGroups[index].name),
                             description: htmlspecialchars(data.deviceGroups[index].description),
                             owner: htmlspecialchars(data.deviceGroups[index].owner)
                         })
        });
        var json = {
            "recordsTotal": data.count,
            "recordsFiltered": data.count,
            "data": objects
        };
        return JSON.stringify(json);
    };

    var columns = [
        {
            targets: 0,
            data: 'id',
            class: 'remove-padding icon-only content-fill viewEnabledIcon',
            render: function (data, type, row, meta) {
                return '<div class="thumbnail icon"><img class="square-element text fw " ' +
                       'src="public/cdmf.page.groups/images/group-icon.png"/></div>';
            }
        },
        {
            targets: 1,
            data: 'name',
            class: 'viewEnabledIcon'
        },
        {
            targets: 2,
            data: 'owner',
            class: 'remove-padding-top viewEnabledIcon'
        },
        {
            targets: 3,
            data: 'description',
            class: 'remove-padding-top viewEnabledIcon'
        },
        {
            targets: 4,
            data: 'id',
            class: 'text-right content-fill text-left-on-grid-view no-wrap tooltip-overflow-fix',
            render: function (id, type, row, meta) {
                var html = '';
                if ($.hasPermission("VIEW_GROUP_DEVICES")) {
                    /*html += '<a href="group/' + row.groupId
                            + '/analytics" data-click-event="remove-form" class="btn padding-reduce-on-grid-view">' +
                            '<span class="fw-stack"><i class="fw fw-circle-outline fw-stack-2x"></i><i class="fw fw-statistics fw-stack-1x"></i></span>'
                            +
                            '<span class="hidden-xs hidden-on-grid-view">Analytics</span></a>';*/
                }
                if (row.owner != "wso2.system.user") {
                    // if ($.hasPermission("SHARE_GROUP")) {
                    //     html +=
                    //             '<a href="#" data-click-event="remove-form" class="btn padding-reduce-on-grid-view share-group-link" data-group-id="'
                    //             + row.groupId + '" ' +
                    //             'data-group-owner="' + row.owner
                    //             + '" data-placement="top" data-toggle="tooltip" data-original-title="Share"><span class="fw-stack"><i class="fw fw-circle-outline fw-stack-2x"></i><i class="fw fw-share fw-stack-1x"></i></span>'
                    //             +
                    //             '<span class="hidden-xs hidden-on-grid-view">Share</span></a>';
                    // }
                    if ($.hasPermission("UPDATE_GROUP")) {
                        html +=
                                '<a href="#" data-click-event="remove-form" class="btn padding-reduce-on-grid-view edit-group-link" data-group-name="'
                                + row.name + '" ' +
                                'data-group-owner="' + row.owner + '" data-group-description="' + row.description
                                + '" data-group-id="' + row.groupId
                                + '" data-placement="top" data-toggle="tooltip" data-original-title="Edit"><span class="fw-stack"><i class="fw fw-circle-outline fw-stack-2x"></i>' +
                                '<i class="fw fw-edit fw-stack-1x"></i></span><span class="hidden-xs hidden-on-grid-view">Edit</span></a>';
                    }
                    if ($.hasPermission("REMOVE_GROUP")) {
                        html +=
                                '<a href="#" data-click-event="remove-form" class="btn padding-reduce-on-grid-view remove-group-link" data-group-id="'
                                + row.groupId + '" ' +
                                'data-group-owner="' + row.owner
                                + '" data-placement="top" data-toggle="tooltip" data-original-title="Delete"><span class="fw-stack"><i class="fw fw-circle-outline fw-stack-2x"></i><i class="fw fw-delete fw-stack-1x"></i>'
                                +
                                '</span><span class="hidden-xs hidden-on-grid-view">Delete</span></a>';
                    }
                }
                return html;
            }
        }
    ];

    var fnCreatedRow = function (row, data) {
        $(row).attr('data-type', 'selectable');
        if ($.hasPermission("VIEW_GROUP_DEVICES")) {
            $(row).attr('data-url', 'devices?groupId=' + data.groupId + '&groupName=' + data.name);
        }
        $.each($('td', row), function (colIndex) {
            switch (colIndex) {
                case 1:
                    $(this).attr('data-grid-label', "Name");
                    $(this).attr('data-search', data.name);
                    $(this).attr('data-display', data.name);
                    break;
                case 2:
                    $(this).attr('data-grid-label', "Owner");
                    $(this).attr('data-search', data.owner);
                    $(this).attr('data-display', data.owner);
                    break;
                case 3:
                    $(this).attr('data-grid-label', "Description");
                    $(this).attr('data-search', data.description);
                    $(this).attr('data-display', data.description);
                    break;
            }
        });
    };

    $('#group-grid').datatables_extended_serverside_paging(
            null,
            serviceURL,
            dataFilter,
            columns,
            fnCreatedRow,
            function (oSettings) {
                $(".icon .text").res_text(0.2);
                attachEvents();
                var thisTable = $(this).closest('.dataTables_wrapper').find('.dataTable').dataTable();
                thisTable.removeClass("table-selectable");
            },
            {
                "placeholder": "Search By Group Name",
                "searchKey": "name"
            }
    );
    $(groupCheckbox).click(function () {
        addGroupSelectedClass(this);
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

    /* Adding selected class for selected devices */
    $(groupCheckbox).each(function () {
        addGroupSelectedClass(this);
    });

    var permissionSet = {};

    //This method is used to setup permission for device listing
    $.setPermission = function (permission) {
        permissionSet[permission] = true;
    };

    $.hasPermission = function (permission) {
        return permissionSet[permission];
    };

    var permissionList = $("#permission").data("permission");
    for (var key in permissionList) {
        if (permissionList.hasOwnProperty(key)) {
            $.setPermission(key);
        }
    }

    loadGroups();
    //$('#device-grid').datatables_extended();

    /* for device list sorting drop down */
    $(".ctrl-filter-type-switcher").popover(
        {
            html: true,
            content: function () {
                return $("#content-filter-types").html();
            }
        }
    );

    /* for data tables*/
    $('[data-toggle="tooltip"]').tooltip();

    $("[data-toggle=popover]").popover();

    $(".ctrl-filter-type-switcher").popover(
        {
            html: true,
            content: function () {
                return $('#content-filter-types').html();
            }
        }
    );

    $('#nav').affix(
        {
            offset: {
                top: $('header').height()
            }
        }
    );

});

var modalPopup = ".modal";
var modalPopupContainer = modalPopup + " .modal-content";
var modalPopupContent = modalPopup + " .modal-content";
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
    $(modalPopup).modal('show');
}

/*
 * hide popup function.
 */
function hidePopup() {
    $(modalPopupContent).html("");
    $(modalPopupContent).removeClass("operation-data");
    $(modalPopup).modal('hide');
    $('body').removeClass('modal-open').css('padding-right', '0px');
    $('.modal-backdrop').remove();
}

/**
 * Following functions should be triggered after AJAX request is made.
 */
function attachEvents() {
    /**
     * Following click function would execute
     * when a user clicks on "Share" link
     * on Group Management page in WSO2 Device Management Server Console.
     */
    $("a.share-group-link").click(function () {
        var groupId = $(this).data("group-id");
        var groupOwner = $(this).data("group-owner");

        $(modalPopupContent).html($('#share-group-w1-modal-content').html());
        showPopup();

        listAllRoles(groupId);
        var shareGroupNextLink = $("a#share-group-next-link");
        shareGroupNextLink.click(function () {
            var roles = $("#roles").val();
            updateGroupShare(groupId, roles);
        });

        var shareGroupNewRoleFromSelectionLink = $("a#share-group-new-role-from-selection");
        shareGroupNewRoleFromSelectionLink.click(function () {
            var roles = [];
            $('.modal .roleCheckBoxes').each(
                function () {
                    if ($(this).is(':checked')) {
                        roles.push($(this).data('role-name'));
                    }
                }
            );
            addNewRole(roles);
            // $(modalPopupContent).html($('#share-group-w3-modal-content').html());
            // createNewRole(roles);
        });
    });

    /**
     * Following click function would execute
     * when a user clicks on "Remove" link
     * on Group Management page in WSO2 IoT Server Console.
     */
    $("a.remove-group-link").click(function () {
        var groupId = $(this).data("group-id");
        var groupOwner = $(this).data("group-owner");

        $(modalPopupContent).html($('#remove-group-modal-content').html());
        showPopup();

        $("a#remove-group-yes-link").click(function () {
            var successCallback = function (data, textStatus, xhr) {
                if (xhr.status == 200) {
                    $(modalPopupContent).html($('#remove-group-200-content').html());
                    setTimeout(function () {
                        hidePopup();
                        location.reload(false);
                    }, 2000);
                } else {
                    displayErrors(xhr);
                }
            };

            invokerUtil.delete("/api/device-mgt/v1.0/groups/id/" + groupId,
                               successCallback, function (message) {
                    displayErrors(message);
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
        var groupId = $(this).data("group-id");
        var groupName = $(this).data("group-name");
        var groupOwner = $(this).data("group-owner");
        var groupDescription = $(this).data("group-description");

        $(modalPopupContent).html($('#edit-group-modal-content').html());
        $('#edit-group-name').val(groupName);
        $('#edit-group-description').val(groupDescription);
        showPopup();

        $("a#edit-group-yes-link").click(function () {
            var newGroupName = $('#edit-group-name').val();
            var newGroupDescription = $('#edit-group-description').val();
            var group = {"name": newGroupName, "description": newGroupDescription, "owner": groupOwner};

            var successCallback = function (data, textStatus, xhr) {
                if (xhr.status == 200) {
                    $(modalPopupContent).html($('#edit-group-200-content').html());
                    setTimeout(function () {
                        hidePopup();
                        location.reload(false);
                    }, 2000);
                } else {
                    displayErrors(xhr);
                }
            };

            invokerUtil.put("/api/device-mgt/v1.0/groups/id/" + groupId, group,
                            successCallback, function (message) {
                    displayErrors(message);
                });
        });

        $("a#edit-group-cancel-link").click(function () {
            hidePopup();
        });
    });
}

function markAlreadySavedUsersRoles(groupId) {
    var successCallback = function (data, textStatus, xhr) {
        data = JSON.parse(data);
        if (xhr.status == 200) {
            if (data.roles.length > 0) {
                var selectedValues = [];
                for (var i = 0; i < data.roles.length; i++) {
                    selectedValues.push(data.roles[i]);
                }
                $("#roles").val(selectedValues).trigger("change");
            } else {
                return;
            }
        } else {
            displayErrors(xhr);
        }
    };

    invokerUtil.get("/api/device-mgt/v1.0/groups/id/" + groupId + "/roles",
                    successCallback, function (message) {
            displayErrors(message);
        });
}

function listAllRoles(groupId) {
    var successCallback = function (data, textStatus, xhr) {
        data = JSON.parse(data);
        if (xhr.status == 200) {
            if (data.roles.length > 0) {
                var html = '<select id="roles" class="form-control select2" multiple="multiple">';
                for (var i = 0; i < data.roles.length; i++) {
                    html += '<option value="' + data.roles[i] + '">' + data.roles[i] + '</option>';
                }
                html += '</select>';
                $("#rolesListing").html(html);
                markAlreadySavedUsersRoles(groupId);
                $("select.select2[multiple=multiple]").select2({
                                                                   tags: false
                                                               });
            } else {
                $("#rolesListing").html("No roles available");
            }
        } else {
            displayErrors(xhr);
        }
    };

    invokerUtil.get("/api/device-mgt/v1.0/roles?offset=0&limit=100&user-store=all",
                    successCallback, function (message) {
            displayErrors(message);
        });
}

function addNewRole(roles) {
    $(modalPopupContent).html($('#share-group-w3-modal-content').html());
    $("a#share-group-w3-yes-link").click(function () {
        var roleName = $('#group-sharing-role-name').val();
        if (roleName) {
            createNewCombinedRole(roleName, roles);
        } else {
            var errorMsgWrapper = "#notification-error-msg";
            var errorMsg = "#notification-error-msg span";
            $(errorMsg).text("Role name cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        }
    });
    $("a#share-group-w3-cancel-link").click(function () {
        hidePopup();
    });
}

function togglePermissionAction(element) {
    $(element).data('value', 'checked');
    var icon = $(element).find("i")[1];
    if ($(icon).hasClass('fw-minus')) {
        $(icon).removeClass('fw-minus');
        $(icon).addClass('fw-add');
        $(element).data('value', 'unchecked');
    } else {
        $(icon).removeClass('fw-add');
        $(icon).addClass('fw-minus');
        $(element).data('value', 'checked');
    }
}

function updateGroupShare(groupId, roles) {
    var successCallback = function (data) {
        $(modalPopupContent).html($('#share-group-200-content').html());
    };

    invokerUtil.post("/api/device-mgt/v1.0/groups/id/" + groupId + "/share",
                     roles, successCallback, function (message) {
            displayErrors(message);
        });
}

function createNewCombinedRole(roleName, roleList) {
    var successCallback = function (data, status, jqXHR, isLast) {
        $(modalPopupContent).html($('#create-combined-role-200-content').html());
        showPopup();
    };
    invokerUtil.post("/api/device-mgt/v1.0/roles/create-combined-role/" + roleName, roleList,
                     successCallback, function (message) {
            displayErrors(message);
        });
}

function displayErrors(jqXHR) {
    showPopup();
    if (jqXHR.status == 400) {
        $(modalPopupContent).html($('#group-400-content').html());
        if (jqXHR.responseText) {
            $('#error-msg').html(jqXHR.responseText.replace(new RegExp("\"", 'g'), ""));
        }
        $("a#group-400-link").click(function () {
            hidePopup();
        });
    } else if (jqXHR.status == 403) {
        $(modalPopupContent).html($('#group-403-content').html());
        $("a#group-403-link").click(function () {
            hidePopup();
        });
    } else if (jqXHR.status == 404) {
        $(modalPopupContent).html($('#group-404-content').html());
        $("#group-404-message").html(jqXHR.responseText);
        $("a#group-404-link").click(function () {
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