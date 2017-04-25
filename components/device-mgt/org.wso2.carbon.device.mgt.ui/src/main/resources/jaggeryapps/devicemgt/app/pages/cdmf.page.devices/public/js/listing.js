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
    if ($(".select-enable-btn").text() == "Select" && !$(this).hasClass("btn")) {
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
var deviceCheckbox = "#ast-container .ctrl-wr-asset .itm-select input[type='checkbox']";
var assetContainer = "#ast-container";

var deviceListing, currentUser, groupId;

/*
 * DOM ready functions.
 */
$(document).ready(function () {

    var permissionSet = {};
    $.setPermission = function (permission) {
        permissionSet[permission] = true;
    };

    $.hasPermission = function (permission) {
        return permissionSet[permission];
    };

    deviceListing = $("#device-listing");
    currentUser = deviceListing.data("current-user");

    groupId = getParameterByName("groupId");

    /* Adding selected class for selected devices */
    $(deviceCheckbox).each(function () {
        addDeviceSelectedClass(this);
    });

    /* for device list sorting drop down */
    $(".ctrl-filter-type-switcher").popover({
        html: true,
        content: function () {
            return $("#content-filter-types").html();
        }
    });
});

/*
 * On Select All Device button click function.
 *
 * @param button: Select All Device button
 */
function selectAllDevices(button) {
    if (!$(button).data('select')) {
        $(deviceCheckbox).each(function (index) {
            $(this).prop('checked', true);
            addDeviceSelectedClass(this);
        });
        $(button).data('select', true);
        $(button).html('Deselect All Devices');
    } else {
        $(deviceCheckbox).each(function (index) {
            $(this).prop('checked', false);
            addDeviceSelectedClass(this);
        });
        $(button).data('select', false);
        $(button).html('Select All Devices');
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
function addDeviceSelectedClass(checkbox) {
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

function loadDevices(searchType, searchParam) {
    var serviceURL;
    if (groupId && $.hasPermission("LIST_OWN_DEVICES")) {
        serviceURL = "/api/device-mgt/v1.0/groups/id/" + groupId + "/devices";
    } else if ($.hasPermission("LIST_DEVICES")) {
        serviceURL = "/api/device-mgt/v1.0/devices";
    } else if (permissionsUtil.hasPermission("LIST_OWN_DEVICES")) {
        //Get authenticated users devices
        serviceURL = "/api/device-mgt/v1.0/devices?username=" + currentUser;
    } else {
        $("#loading-content").remove();
        $('#device-table').addClass('hidden');
        $('#device-listing-status-msg').text('Permission denied.');
        $("#device-listing-status").removeClass(' hidden');
        return;
    }

    function getPropertyValue(deviceProperties, propertyName) {
        if (!deviceProperties) {
            return;
        }
        var property;
        for (var i = 0; i < deviceProperties.length; i++) {
            property = deviceProperties[i];
            if (property.name == propertyName) {
                return property.value;
            }
        }
        return {};
    }

    function getDeviceTypeLabel(type) {
        var deviceTypes = deviceListing.data("deviceTypes");
        for (var i = 0; i < deviceTypes.length; i++) {
            if (deviceTypes[i].type == type) {
                return deviceTypes[i].label;
            }
        }
        return type;
    }

    function getDeviceTypeCategory(type) {
        var deviceTypes = deviceListing.data("deviceTypes");
        for (var i = 0; i < deviceTypes.length; i++) {
            if (deviceTypes[i].type == type) {
                return deviceTypes[i].category;
            }
        }
        return type;
    }

    function getDeviceTypeThumb(type) {
        var deviceTypes = deviceListing.data("deviceTypes");
        for (var i = 0; i < deviceTypes.length; i++) {
            if (deviceTypes[i].type == type) {
                return deviceTypes[i].thumb;
            }
        }
        return type;
    }

    function analyticsEnabled(type) {
        var deviceTypes = deviceListing.data("deviceTypes");
        for (var i = 0; i < deviceTypes.length; i++) {
            if (deviceTypes[i].type == type) {
                var analyticsEnabled = deviceTypes[i].analyticsEnabled;
                if (analyticsEnabled == undefined) {
                    // By default it should be enabled
                    return true;
                }
                // In JS Boolean("false") returns TRUE => http://stackoverflow.com/a/264037/1560536
                return (analyticsEnabled == "true");
            }
        }
        return true;
    }

    function groupingEnabled(type) {
        var deviceTypes = deviceListing.data("deviceTypes");
        for (var i = 0; i < deviceTypes.length; i++) {
            if (deviceTypes[i].type == type) {
                var groupingEnabled = deviceTypes[i].groupingEnabled;
                if (groupingEnabled == undefined) {
                    // By default it should be enabled
                    return true;
                }
                // In JS Boolean("false") returns TRUE => http://stackoverflow.com/a/264037/1560536
                return (groupingEnabled == "true");
            }
        }
        return true;
    }

    var columns = [
        {
            targets: 0,
            data: 'name',
            class: 'remove-padding icon-only content-fill viewEnabledIcon',
            render: function (data, type, row, meta) {
                return '<div class="thumbnail icon"><img class="square-element text fw " src="'
                    + getDeviceTypeThumb(row.deviceType) + '"/></div>';
            }
        },
        {
            targets: 1,
            data: 'name',
            class: 'viewEnabledIcon',
            render: function (name, type, row, meta) {
                var model = getPropertyValue(row.properties, 'DEVICE_MODEL');
                var vendor = getPropertyValue(row.properties, 'VENDOR');
                var html = '<h4>' + name + '</h4>';
                if (model) {
                    html += '<div>(' + vendor + '-' + model + ')</div>';
                }
                return html;
            }
        },
        {
            targets: 2,
            data: 'user',
            class: 'remove-padding-top viewEnabledIcon'
        },
        {
            targets: 3,
            data: 'status',
            class: 'remove-padding-top viewEnabledIcon',
            render: function (status, type, row, meta) {
                var html;
                switch (status) {
                    case 'ACTIVE' :
                        html = '<span><i class="fw fw-success icon-success"></i> Active</span>';
                        break;
                    case 'INACTIVE' :
                        html = '<span><i class="fw fw-warning icon-warning"></i> Inactive</span>';
                        break;
                    case 'BLOCKED' :
                        html = '<span><i class="fw fw-remove icon-danger"></i> Blocked</span>';
                        break;
                    case 'REMOVED' :
                        html = '<span><i class="fw fw-delete icon-danger"></i> Removed</span>';
                        break;
                    case 'UNREACHABLE' :
                        html = '<span><i class="fw fw-warning icon-warning"></i> Unreachable</span>';
                        break;
                }
                return html;
            }
        },
        {
            targets: 4,
            data: 'deviceType',
            class: 'remove-padding-top viewEnabledIcon',
            render: function (status, type, row, meta) {
                return getDeviceTypeLabel(row.deviceType);
            }
        },
        {
            targets: 5,
            data: 'ownership',
            class: 'remove-padding-top viewEnabledIcon',
            render: function (status, type, row, meta) {
                if (getDeviceTypeCategory(row.deviceType) == 'mobile') {
                    return row.ownership;
                } else {
                    return null;
                }
            }
        },
        {
            targets: 6,
            data: 'status',
            class: 'text-right content-fill text-left-on-grid-view no-wrap tooltip-overflow-fix',
            render: function (status, type, row, meta) {
                var deviceType = row.deviceType;
                var deviceIdentifier = row.deviceIdentifier;
                var html = '<span></span>';
                var statURL = $("#device-listing").data("analitics-url");
                if (status != 'REMOVED') {
                    html = '';

                    if (analyticsEnabled(row.deviceType)) {


                        html += '<a href="' + statURL  +
                            deviceIdentifier + '&deviceName=' + row.name + '" ' + 'data-click-event="remove-form"' +
                            ' class="btn padding-reduce-on-grid-view" data-placement="top" data-toggle="tooltip" data-original-title="Analytics"><span class="fw-stack">' +
                            '<i class="fw fw-circle-outline fw-stack-2x"></i><i class="fw fw-statistics fw-stack-1x"></i></span>' +
                            '<span class="hidden-xs hidden-on-grid-view">Analytics</span>';
                    }

                    if (!groupId && groupingEnabled(row.deviceType)) {
                        html +=
                            '<a href="#" data-click-event="remove-form" class="btn padding-reduce-on-grid-view group-device-link" '
                            +
                            'data-deviceid="' + deviceIdentifier + '" data-devicetype="' + deviceType
                            + '" data-devicename="' +
                            row.name + '" data-placement="top" data-toggle="tooltip" data-original-title="Group"><span class="fw-stack"><i class="fw fw-circle-outline fw-stack-2x"></i>' +
                            '<i class="fw fw-group fw-stack-1x"></i></span>' +
                            '<span class="hidden-xs hidden-on-grid-view">Group</span></a>';
                    }

                    html +=
                        '<a href="#" data-click-event="remove-form" class="btn padding-reduce-on-grid-view edit-device-link" '
                        + 'data-deviceid="' + deviceIdentifier + '" data-devicetype="' + deviceType
                        + '" data-devicename="' + row.name + '" data-placement="top" data-toggle="tooltip" data-original-title="Edit">'
                        + '<span class="fw-stack"><i class="fw fw-circle-outline fw-stack-2x"></i>'
                        + '<i class="fw fw-edit fw-stack-1x"></i></span>'
                        + '<span class="hidden-xs hidden-on-grid-view">Edit</span></a>';
                    var groupOwner = $('#group_owner').text();
                    if (groupId && groupOwner != "wso2.system.user") {
                        html +=
                            '<a href="#" data-click-event="remove-form" class="btn padding-reduce-on-grid-view remove-device-link" '
                            + 'data-deviceid="' + deviceIdentifier + '" data-devicetype="' + deviceType
                            + '" data-devicename="' + row.name + '" data-placement="top" data-toggle="tooltip" data-original-title="Remove from group">'
                            + '<span class="fw-stack"><i class="fw fw-circle-outline fw-stack-2x"></i>'
                            + '<i class="fw fw-delete fw-stack-1x"></i></span>'
                            + '<span class="hidden-xs hidden-on-grid-view">Remove from group</span>';
                    } else {
                        html +=
                            '<a href="#" data-click-event="remove-form" class="btn padding-reduce-on-grid-view remove-device-link" '
                            + 'data-deviceid="' + deviceIdentifier + '" data-devicetype="' + deviceType
                            + '" data-devicename="' + row.name + '" data-placement="top" data-toggle="tooltip" data-original-title="Delete">'
                            + '<span class="fw-stack"><i class="fw fw-circle-outline fw-stack-2x"></i>'
                            + '<i class="fw fw-delete fw-stack-1x"></i></span>'
                            + '<span class="hidden-xs hidden-on-grid-view">Delete</span>';
                    }
                }
                return html;
            }
        }
    ];

    var fnCreatedRow = function (row, data, dataIndex) {
        $(row).attr('data-type', 'selectable');
        $(row).attr('data-deviceid', htmlspecialchars(data.deviceIdentifier));
        $(row).attr('data-devicetype', htmlspecialchars(data.deviceType));
        $(row).attr('data-url', context + '/device/' + htmlspecialchars(data.deviceType) + '?id=' + htmlspecialchars(data.deviceIdentifier));
        var model = htmlspecialchars(getPropertyValue(data.properties, 'DEVICE_MODEL'));
        var vendor = htmlspecialchars(getPropertyValue(data.properties, 'VENDOR'));
        var owner = htmlspecialchars(data.user);
        var status = htmlspecialchars(data.status);
        var ownership = htmlspecialchars(data.ownership);
        var deviceType = htmlspecialchars(data.deviceType);
        var category = getDeviceTypeCategory(deviceType);
        $.each($('td', row), function (colIndex) {
            switch (colIndex) {
                case 1:
                    $(this).attr('data-search', model + ',' + vendor);
                    $(this).attr('data-display', model);
                    break;
                case 2:
                    $(this).attr('data-grid-label', "Owner");
                    $(this).attr('data-search', owner);
                    $(this).attr('data-display', owner);
                    break;
                case 3:
                    $(this).attr('data-grid-label', "Status");
                    $(this).attr('data-search', status);
                    $(this).attr('data-display', status);
                    break;
                case 4:
                    $(this).attr('data-grid-label', "Type");
                    $(this).attr('data-search', deviceType);
                    $(this).attr('data-display', getDeviceTypeLabel(deviceType));
                    break;
                case 5:
                    if (category == 'mobile') {
                        $(this).attr('data-grid-label', "Ownership");
                        $(this).attr('data-search', ownership);
                        $(this).attr('data-display', ownership);
                    }
                    break;
            }
        });
    };

    function htmlspecialchars(text) {
        return jQuery('<div/>').text(text).html();
    }

    var dataFilter = function (data) {
        data = JSON.parse(data);
        var objects = [];

        $(data.devices).each(function (index) {
            objects.push(
                {
                    model: getPropertyValue(data.devices[index].properties, "DEVICE_MODEL"),
                    vendor: getPropertyValue(data.devices[index].properties, "VENDOR"),
                    user: data.devices[index].enrolmentInfo.owner,
                    status: data.devices[index].enrolmentInfo.status,
                    ownership: data.devices[index].enrolmentInfo.ownership,
                    deviceType: data.devices[index].type,
                    deviceIdentifier: data.devices[index].deviceIdentifier,
                    name: data.devices[index].name
                }
            );
        });

        var json = {
            "recordsTotal": data.count,
            "recordsFiltered": data.count,
            "data": objects
        };
        return JSON.stringify(json);
    };

    $('#device-grid').datatables_extended_serverside_paging(
        null,
        serviceURL,
        dataFilter,
        columns,
        fnCreatedRow,
        function () {
            $(".icon .text").res_text(0.2);
            $('#device-grid').removeClass('hidden');
            $("#loading-content").remove();
            attachDeviceEvents();


            // if ($('.advance-search').length < 1) {
            //     $(this).closest('.dataTables_wrapper').find('div[id$=_filter] input')
            //         .after('<a href="' + context + '/devices/search"' +
            //             ' class="advance-search add-padding-3x">Advance Search</a>');
            // }

        }, {
            "placeholder": "Search By Device Name",
            "searchKey": "name"
        }
    );

    $(deviceCheckbox).click(function () {
        addDeviceSelectedClass(this);
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
    $(deviceCheckbox).each(function () {
        addDeviceSelectedClass(this);
    });

    var permissionList = $("#permission").data("permission");
    for (var key in permissionList) {
        if (permissionList.hasOwnProperty(key)) {
            $.setPermission(key);
        }
    }

    loadDevices();

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

function markAlreadyAssignedGroups(deviceId, deviceType) {
    var successCallback = function (data, textStatus, xhr) {
        data = JSON.parse(data);
        if (xhr.status == 200) {
            if (data.length > 0) {
                var selectedValues = [];
                for (var i = 0; i < data.length; i++) {
                    if (data[i].owner != "wso2.system.user") {
                        selectedValues.push(data[i].id);
                    }
                }
                $("#groups").val(selectedValues).trigger("change");
            }
        } else {
            displayErrors(xhr);
        }
    };

    invokerUtil.get("/api/device-mgt/v1.0/groups/device?deviceId=" + deviceId + "&deviceType=" + deviceType,
        successCallback, function (message) {
            displayErrors(message);
        });
}

/**
 * Following functions should be triggered after AJAX request is made.
 */
function attachDeviceEvents() {

    /**
     * Following click function would execute
     * when a user clicks on "Group" link
     * on Device Management page in WSO2 DeviceMgt Console.
     */
    if ($("a.group-device-link").length > 0) {
        $("a.group-device-link").click(function () {
            var deviceId = $(this).data("deviceid");
            var deviceType = $(this).data("devicetype");
            $(modalPopupContent).html($('#group-device-modal-content').html());
            $('#user-groups').html(
                '<div style="height:100px" data-state="loading" data-loading-text="Loading..." data-loading-style="icon-only" data-loading-inverse="true"></div>');
            $("a#group-device-update-link").hide();
            showPopup();

            var serviceURL;
            if ($.hasPermission("LIST_ALL_GROUPS")) {
                serviceURL = "/api/device-mgt/v1.0/admin/groups?limit=100";
            } else if ($.hasPermission("LIST_GROUPS")) {
                //Get authenticated users groups
                serviceURL = "/api/device-mgt/v1.0/groups?limit=100";
            }

            invokerUtil.get(serviceURL, function (data) {
                $("a#group-device-add-link").hide();
                var groups = JSON.parse(data);
                var html = '';
                var hasGroups = false;
                for (var i = 0; i < groups.deviceGroups.length; i++) {
                    if (groups.deviceGroups[i].owner != "wso2.system.user") {
                        html += '<option value="' + groups.deviceGroups[i].id + '">' + groups.deviceGroups[i].name + '</option>';
                        hasGroups = true;
                    }
                }
                if (hasGroups) {
                    html = '<br/><h4>Please select device group(s)</h4><br/>' +
                        '<div class="wr-input-control">' +
                        '<select id="groups" class="form-control select2" multiple="multiple">' +
                        html + '</select></div>';
                    markAlreadyAssignedGroups(deviceId, deviceType);
                    $("a#group-device-update-link").show();
                    $("a#group-add-link").hide();
                } else {
                    $("a#group-device-update-link").hide();
                    $("a#group-add-link").show();
                    html += '<br/><h4>You don\'t have any existing device groups. Please add new device group first.</h4>'
                }
                $('#user-groups').html(html);
                $("select.select2[multiple=multiple]").select2({
                    tags: false
                });
                $("a#group-device-update-link").click(function () {
                    var deviceIdentifier = {"id": deviceId, "type": deviceType};
                    var deviceGroupIds = $("#groups").val();
                    if (!deviceGroupIds) {
                        deviceGroupIds = [];
                    }
                    var deviceToGroupsAssignment = {
                        deviceIdentifier: deviceIdentifier,
                        deviceGroupIds: deviceGroupIds
                    };
                    serviceURL = "/api/device-mgt/v1.0/groups/device/assign";
                    invokerUtil.post(serviceURL, deviceToGroupsAssignment, function (data) {
                        $(modalPopupContent).html($('#group-associate-device-200-content').html());
                        setTimeout(function () {
                            hidePopup();
                            location.reload(false);
                        }, 2000);
                    }, function (jqXHR) {
                        displayDeviceErrors(jqXHR);
                    });
                });
            }, function (jqXHR) {
                if (jqXHR.status == 404) {
                    $(modalPopupContent).html($('#group-404-content').html());
                    $("a#cancel-link").click(function () {
                        hidePopup();
                    });
                } else {
                    displayDeviceErrors(jqXHR);
                }
            });

            $("a#group-device-cancel-link").click(function () {
                hidePopup();
            });
        });

    }

    /**
     * Following click function would execute
     * when a user clicks on "Remove" link
     * on Device Management page in WSO2 MDM Console.
     */
    $("a.remove-device-link").click(function () {
        var deviceIdentifiers = [];
        var deviceId = $(this).data("deviceid");
        var deviceType = $(this).data("devicetype");

        if (deviceId && deviceType) {
            deviceIdentifiers = [{"id": deviceId, "type": deviceType}];
        } else {
            deviceIdentifiers = getSelectedDevices();
        }

        if (deviceIdentifiers.length == 0) {
            $(modalPopupContent).html($('#no-device-selected').html());
            $("a#no-device-selected-link").click(function () {
                hidePopup();
            });
            showPopup();
            return;
        }

        $(modalPopupContent).html($('#remove-device-modal-content').html());
        showPopup();

        $("a#remove-device-yes-link").click(function () {
            if (groupId) {
                var serviceURL = "/api/device-mgt/v1.0/groups/id/" + groupId + "/devices/remove";
                invokerUtil.post(serviceURL, deviceIdentifiers, function (message) {
                    $(modalPopupContent).html($('#remove-device-200-content').html());
                    setTimeout(function () {
                        hidePopup();
                        location.reload(false);
                    }, 2000);
                }, function (jqXHR) {
                    displayDeviceErrors(jqXHR);
                });
            } else {
                removeDevices(deviceIdentifiers);
            }
        });

        $("a#remove-device-cancel-link").click(function () {
            hidePopup();
        });
    });

    /**
     * Following click function would execute
     * when a user clicks on "Edit" link
     * on Device Management page in WSO2 MDM Console.
     */
    $("a.edit-device-link").click(function () {
        var deviceId = $(this).data("deviceid");
        var deviceType = $(this).data("devicetype");
        var deviceName = $(this).data("devicename");
        var serviceURL = "/api/device-mgt/v1.0/devices/type/" + deviceType + "/id/" + deviceId + "/rename";

        $(modalPopupContent).html($('#edit-device-modal-content').html());
        $('#edit-device-name').val(deviceName);
        showPopup();

        $("a#edit-device-yes-link").click(function () {
            var newDeviceName = $('#edit-device-name').val();
            var request = {};
            request['name'] = newDeviceName;
            invokerUtil.post(serviceURL, request, function (message) {
                $(modalPopupContent).html($('#edit-device-200-content').html());
                setTimeout(function () {
                    hidePopup();
                    location.reload(false);
                }, 2000);
            }, function (jqXHR) {
                displayDeviceErrors(jqXHR);
            });
        });

        $("a#edit-device-cancel-link").click(function () {
            hidePopup();
        });
    });

    /**
     * Following click function would execute
     * when a user clicks on "Add to Group" link
     * on Device Management page in WSO2 devicemgt Console.
     */
    $("a.add-devices-to-group-link").click(function () {
        $("a#group-device-update-link").hide();
        var deviceIdentifiers = getSelectedDevices();
        if (deviceIdentifiers.length == 0) {
            $(modalPopupContent).html($('#no-device-selected').html());
            $("a#no-device-selected-link").click(function () {
                hidePopup();
            });
            showPopup();
            return;
        }

        $(modalPopupContent).html($('#group-device-modal-content').html());
        $('#user-groups').html(
            '<div style="height:100px" data-state="loading" data-loading-text="Loading..." data-loading-style="icon-only" data-loading-inverse="true"></div>');
        $("a#group-device-add-link").hide();
        showPopup();

        var serviceURL;
        if ($.hasPermission("LIST_ALL_GROUPS")) {
            serviceURL = "/api/device-mgt/v1.0/admin/groups?limit=100";
        } else if ($.hasPermission("LIST_GROUPS")) {
            //Get authenticated users groups
            serviceURL = "/api/device-mgt/v1.0/groups?limit=100";
        }

        invokerUtil.get(serviceURL, function (data) {
            var groups = JSON.parse(data);
            var html = '';
            var hasGroups = false;
            for (var i = 0; i < groups.deviceGroups.length; i++) {
                if (groups.deviceGroups[i].owner != "wso2.system.user") {
                    html += '<option value="' + groups.deviceGroups[i].id + '">' +
                        groups.deviceGroups[i].name + '</option>';
                    hasGroups = true;
                }
            }
            if (hasGroups) {
                html = '<br /><select id="assign-group-selector" style="color:#3f3f3f;padding:5px;width:250px;">' +
                    html + '</select>';
                $("a#group-add-link").hide();
                $("a#group-device-add-link").show();
            } else {
                html += '<br/><h4>You don\'t have any existing device groups. Please add new device group first.</h4>';
                $("a#group-add-link").show();
                $("a#group-device-add-link").hide();
            }
            $('#user-groups').html(html);
            $("a#group-device-add-link").click(function () {
                var selectedGroup = $('#assign-group-selector').val();
                serviceURL = "/api/device-mgt/v1.0/groups/id/" + selectedGroup + "/devices/add";
                invokerUtil.post(serviceURL, deviceIdentifiers, function (data) {
                    $(modalPopupContent).html($('#group-associate-device-200-content').html());
                    setTimeout(function () {
                        hidePopup();
                        location.reload(false);
                    }, 2000);
                }, function (jqXHR) {
                    displayDeviceErrors(jqXHR);
                });
            });
        }, function (jqXHR) {
            if (jqXHR.status == 404) {
                $(modalPopupContent).html($('#group-404-content').html());
                $("a#cancel-link").click(function () {
                    hidePopup();
                });
            } else {
                displayDeviceErrors(jqXHR);
            }
        });

        $("a#group-device-cancel-link").click(function () {
            hidePopup();
        });
    });
}

function removeDevices(deviceIdentifiers) {
    var serviceURL = "/api/device-mgt/v1.0/devices/type/" + deviceIdentifiers[0].type + "/id/" + deviceIdentifiers[0].id;
    invokerUtil.delete(serviceURL, function (message) {
        if (deviceIdentifiers.length > 1) {
            deviceIdentifiers.slice(1, deviceIdentifiers.length);
            removeDevices(deviceIdentifiers);
        } else {
            $(modalPopupContent).html($('#remove-device-200-content').html());
            setTimeout(function () {
                hidePopup();
                location.reload(false);
            }, 2000);
        }
    }, function (jqXHR) {
        displayDeviceErrors(jqXHR);
    });
}

function displayDeviceErrors(jqXHR) {
    showPopup();
    if (jqXHR.status == 400) {
        $(modalPopupContent).html($('#device-400-content').html());
        $("a#device-400-link").click(function () {
            hidePopup();
        });
    } else if (jqXHR.status == 403) {
        $(modalPopupContent).html($('#device-403-content').html());
        $("a#device-403-link").click(function () {
            hidePopup();
        });
    } else if (jqXHR.status == 409) {
        $(modalPopupContent).html($('#device-409-content').html());
        $("a#device-409-link").click(function () {
            hidePopup();
        });
    } else {
        $(modalPopupContent).html($('#device-unexpected-error-content').html());
        $("a#device-unexpected-error-link").click(function () {
            hidePopup();
        });
        console.log("Error code: " + jqXHR.status);
    }
}

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

/*
 * Function to get selected devices.
 */
function getSelectedDevices() {
    var deviceList = [];
    var thisTable = $(".DTTT_selected").closest('.dataTables_wrapper').find('.dataTable').dataTable();
    thisTable.api().rows().every(function () {
        if ($(this.node()).hasClass('DTTT_selected')) {
            deviceList.push(
                {
                    "id": $(thisTable.api().row(this).node()).data('deviceid'),
                    "type": $(thisTable.api().row(this).node()).data('devicetype')
                }
            );
        }
    });

    return deviceList;
}
