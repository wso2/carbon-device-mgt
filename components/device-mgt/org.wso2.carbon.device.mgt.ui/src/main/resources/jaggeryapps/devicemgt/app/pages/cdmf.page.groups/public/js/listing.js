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

(function () {
    var permissionSet = {};

    //This method is used to setup permission for device listing
    $.setPermission = function (permission) {
        permissionSet[permission] = true;
    };

    $.hasPermission = function (permission) {
        return permissionSet[permission];
    };
})();

function loadGroups() {
    var groupListing = $("#group-listing");
    var currentUser = groupListing.data("currentUser");
    var serviceURL;
    if ($.hasPermission("LIST_ALL_GROUPS")) {
        serviceURL = "/devicemgt_admin/groups";
    } else if ($.hasPermission("LIST_GROUPS")) {
        //Get authenticated users groups
        serviceURL = "/devicemgt_admin/groups/user/" + currentUser;
    } else {
        $("#loading-content").remove();
        $('#device-table').addClass('hidden');
        $('#device-listing-status-msg').text('Permission denied.');
        $("#device-listing-status").removeClass(' hidden');
        return;
    }

    $('#group-grid').datatables_extended ({
        serverSide: true,
        processing: false,
        searching: true,
        ordering:  false,
        filter: false,
        pageLength : 16,
        ajax: { url : '/devicemgt/api/groups', data : {url : serviceURL},
            dataSrc: function ( json ) {
                $('#group-grid').removeClass('hidden');
                var $list = $("#group-listing :input[type='search']");
                $list.each(function(){
                    $(this).addClass("hidden");
                });
                return json.data;
            }
        },
        columnDefs: [
            { targets: 0, data: 'id', className: 'remove-padding icon-only content-fill' , render: function ( data, type, row, meta ) {
                return '<div class="thumbnail icon"><img class="square-element text fw " src="public/cdmf.page.groups/images/group-icon.png"/></div>';
            }},
            {targets: 1, data: 'name', className: 'fade-edge'},
            { targets: 2, data: 'owner', className: 'fade-edge remove-padding-top'},
            { targets: 3, data: 'id', className: 'text-right content-fill text-left-on-grid-view no-wrap' ,
                render: function ( id, type, row, meta ) {
                    var html;
                    html = '<a href="devices?groupName=' + row.name + '&groupOwner=' + row.owner + '" data-click-event="remove-form" class="btn padding-reduce-on-grid-view">' +
                           '<span class="fw-stack"><i class="fw fw-ring fw-stack-2x"></i><i class="fw fw-view fw-stack-1x"></i></span>' +
                           '<span class="hidden-xs hidden-on-grid-view">View Devices</span></a>';

                    html += '<a href="group/' + row.owner + '/' + row.name + '/analytics" data-click-event="remove-form" class="btn padding-reduce-on-grid-view">' +
                            '<span class="fw-stack"><i class="fw fw-ring fw-stack-2x"></i><i class="fw fw-statistics fw-stack-1x"></i></span>' +
                            '<span class="hidden-xs hidden-on-grid-view">Analytics</span></a>';

                    html += '<a href="#" data-click-event="remove-form" class="btn padding-reduce-on-grid-view share-group-link" data-group-name="' + row.name + '" ' +
                            'data-group-owner="' + row.owner + '"><span class="fw-stack"><i class="fw fw-ring fw-stack-2x"></i><i class="fw fw-share fw-stack-1x"></i></span>' +
                            '<span class="hidden-xs hidden-on-grid-view">Share</span></a>';

                    html += '<a href="#" data-click-event="remove-form" class="btn padding-reduce-on-grid-view edit-group-link" data-group-name="' + row.name + '" ' +
                            'data-group-owner="' + row.owner + '" data-group-description="' + row.description + '"><span class="fw-stack"><i class="fw fw-ring fw-stack-2x"></i>' +
                            '<i class="fw fw-edit fw-stack-1x"></i></span><span class="hidden-xs hidden-on-grid-view">Edit</span></a>';

                    html += '<a href="#" data-click-event="remove-form" class="btn padding-reduce-on-grid-view remove-group-link" data-group-name="' + row.name + '" ' +
                            'data-group-owner="' + row.owner + '"><span class="fw-stack"><i class="fw fw-ring fw-stack-2x"></i><i class="fw fw-delete fw-stack-1x"></i>' +
                            '</span><span class="hidden-xs hidden-on-grid-view">Delete</span></a>';

                    return html;
                }}
        ],
        "createdRow": function( row, data, dataIndex ) {
            $(row).attr('data-type', 'selectable');
            $(row).attr('data-groupid', data.id);
            $.each($('td', row), function (colIndex) {
                switch(colIndex) {
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
                }
            });
        },
        "fnDrawCallback": function( oSettings ) {
            $(".icon .text").res_text(0.2);
            attachEvents();
        }
    });
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
    var permissionList = $("#permission").data("permission");
    for (var key in permissionList) {
        if (permissionList.hasOwnProperty(key)) {
            $.setPermission(key);
        }
    }

    loadGroups();
    //$('#device-grid').datatables_extended();

    /* Adding selected class for selected devices */
    $(groupCheckbox).each(function () {
        addGroupSelectedClass(this);
    });

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
    $('body').removeClass('modal-open').css('padding-right','0px');
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
        var groupName = $(this).data("group-name");
        var groupOwner = $(this).data("group-owner");
        $(modalPopupContent).html($('#share-group-w1-modal-content').html());
        $("a#share-group-next-link").show();
        showPopup();
        $("a#share-group-next-link").click(function () {
            var selectedUser = $('#share-user-selector').val();
            if (selectedUser == $("#group-listing").data("current-user")) {
                $("#user-names").html("Please specify a user other than current user.");
                $("a#share-group-next-link").hide();
            } else {
                getAllRoles(groupName, groupOwner, selectedUser);
            }
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
        var groupName = $(this).data("group-name");
        var groupOwner = $(this).data("group-owner");

        $(modalPopupContent).html($('#remove-group-modal-content').html());
        showPopup();

        $("a#remove-group-yes-link").click(function () {
            var successCallback = function (data, textStatus, xhr) {
                data = JSON.parse(data);
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

            invokerUtil.delete("/devicemgt_admin/groups/owner/" + groupOwner + "/name/" + groupName,
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
                data = JSON.parse(data);
                if (xhr.status == 200) {
                    setTimeout(function () {
                        hidePopup();
                        location.reload(false);
                    }, 2000);
                } else {
                    displayErrors(xhr);
                }
            };

            invokerUtil.put("/devicemgt_admin/groups/owner/" + groupOwner + "/name/" + groupName, group,
                successCallback, function (message) {
                        displayErrors(message);
                });
        });

        $("a#edit-group-cancel-link").click(function () {
            hidePopup();
        });
    });
}

function getAllRoles(groupName, groupOwner, selectedUser) {
    $(modalPopupContent).html($('#share-group-w2-modal-content').html());
    $('#user-roles').html('<div style="height:100px" data-state="loading" data-loading-text="Loading..." data-loading-style="icon-only" data-loading-inverse="true"></div>');
    $("a#share-group-yes-link").hide();
    var successCallback = function (data, textStatus, xhr) {
        data = JSON.parse(data);
        if (xhr.status == 200) {
            if (data.length > 0) {
                generateRoleMap(groupName, groupOwner, selectedUser, data);
            } else {
                $('#user-roles').html("There is no any roles for this group.");
            }
        } else {
            displayErrors(xhr);
        }
    };

    invokerUtil.get("/devicemgt_admin/groups/owner/" + groupOwner + "/name/" + groupName + "/share/roles",
        successCallback, function (message) {
                displayErrors(message);
        });

    $("a#share-group-w2-cancel-link").click(function () {
        hidePopup();
    });
}

function generateRoleMap(groupName, groupOwner, selectedUser, allRoles) {
    var successCallback = function (data, textStatus, xhr) {
        data = JSON.parse(data);
        if (xhr.status == 200) {
            var userRoles = data;
            var str = '';

            for (var i = 0; i < allRoles.length; i++) {
                var isChecked = '';
                for (var j = 0; j < userRoles.length; j++) {
                    if (allRoles[i] == userRoles[j]) {
                        isChecked = 'checked';
                        break;
                    }
                }
                str += '<label class="checkbox-text"><input type="checkbox" id="user-role-' + allRoles[i] + '" value="' + allRoles[i]
                       + '" ' + isChecked + '/>' + allRoles[i] + '</label>&nbsp;&nbsp;&nbsp;&nbsp;';
            }

            $('#user-roles').html(str);
            $("a#share-group-yes-link").show();
            $("a#share-group-yes-link").show();
            $("a#share-group-yes-link").click(function () {
                var roles = [];
                for (var i = 0; i < allRoles.length; i++) {
                    if ($('#user-role-' + allRoles[i]).is(':checked')) {
                        roles.push(allRoles[i]);
                    }
                }
                updateGroupShare(groupName, groupOwner, selectedUser, roles);
            });
        } else {
            displayErrors(xhr);
        }
    };

    invokerUtil.get("/devicemgt_admin/groups/owner/" + groupOwner + "/name/" + groupName + "/share/roles?userName=" + selectedUser,
        successCallback, function (message) {
                displayErrors(message);
        });

    $("a#share-group-w2-cancel-link").click(function () {
        hidePopup();
    });
}

function updateGroupShare(groupName, groupOwner, selectedUser, roles) {
    var successCallback = function (data) {
        $(modalPopupContent).html($('#share-group-200-content').html());
        setTimeout(function () {
            hidePopup();
            location.reload(false);
        }, 2000);
    };

    invokerUtil.put("/devicemgt_admin/groups/owner/" + groupOwner + "/name/" + groupName + "/user/" + selectedUser + "/share/roles",
                    roles, successCallback, function (message) {
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