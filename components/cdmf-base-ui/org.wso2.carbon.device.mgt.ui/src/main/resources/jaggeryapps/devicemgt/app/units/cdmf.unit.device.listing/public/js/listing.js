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

(function () {
    var cache = {};
    var permissionSet = {};
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

    //This method is used to setup permission for device listing
    $.setPermission = function (permission) {
        permissionSet[permission] = true;
    };

    $.hasPermission = function (permission) {
        return permissionSet[permission];
    };
})();

/*
 * Setting-up global variables.
 */
var deviceCheckbox = "#ast-container .ctrl-wr-asset .itm-select input[type='checkbox']";
var assetContainer = "#ast-container";

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

function loadDevices() {
    var deviceListing = $("#device-listing");
    var deviceListingSrc = deviceListing.attr("src");
    var imageResource = deviceListing.data("image-resource");
    var currentUser = deviceListing.data("currentUser");
    $.template("device-listing", deviceListingSrc, function (template) {
        var serviceURL;
        if ($.hasPermission("LIST_OWN_DEVICES") || $.hasPermission("LIST_DEVICES")) {
            //Get authenticated users devices
            serviceURL = "/mdm-admin/users/devices?username=" + currentUser;
        } else {
            $("#loading-content").remove();
            $('#device-table').addClass('hidden');
            $('#device-listing-status-msg').text('Permission denied.');
            return;
        }

        var successCallback = function (data) {
            data = JSON.parse(data);
            var viewModel = {};
            viewModel.devices = data;
            viewModel.imageLocation = imageResource;
            if (data.length > 0) {
                $('#device-grid').removeClass('hidden');
                var content = template(viewModel);
                $("#ast-container").html(content);
                /*
                 * On device checkbox select add parent selected style class
                 */
                $(deviceCheckbox).click(function () {
                    addDeviceSelectedClass(this);
                });
                attachDeviceEvents();
            } else {
                $('#device-table').addClass('hidden');
                $('#device-listing-status-msg').text('No device is available to be displayed.');
            }
            $("#loading-content").remove();
            $('#device-grid').datatables_extended();
            $(".icon .text").res_text(0.2);
        };
        invokerUtil.get(serviceURL,
                        successCallback, function (message) {
                    console.log(message.content);
                });
    });
}

/*
 * Setting-up global variables.
 */
var deviceCheckbox = "#ast-container .ctrl-wr-asset .itm-select input[type='checkbox']";
var assetContainer = "#ast-container";

function openCollapsedNav() {
    $('.wr-hidden-nav-toggle-btn').addClass('active');
    $('#hiddenNav').slideToggle('slideDown', function () {
        if ($(this).css('display') == 'none') {
            $('.wr-hidden-nav-toggle-btn').removeClass('active');
        }
    });
}

function loadGroupedDevices(groupId) {
    var serviceURL = "api/group/id/" + groupId + "/device/all";
    var deviceListing = $("#device-listing");
    var deviceListingSrc = deviceListing.attr("src");
    var imageResource = deviceListing.data("image-resource");
    var currentUser = deviceListing.data("currentUser");
    $.template("device-listing", deviceListingSrc, function (template) {

        var loadGroupRequest = $.ajax({
                                          url: serviceURL,
                                          method: "GET",
                                          contentType: "application/json",
                                          accept: "application/json"
                                      });

        loadGroupRequest.done(function (data) {
            data = JSON.parse(data);
            var viewModel = {};
            viewModel.devices = data.data;
            viewModel.imageLocation = imageResource;
            viewModel.isGroupView = "true";
            if (viewModel.devices.length > 0) {
                $('#device-grid').removeClass('hidden');
                var content = template(viewModel);
                $("#ast-container").html(content);
                /*
                 * On device checkbox select add parent selected style class
                 */
                $(deviceCheckbox).click(function () {
                    addDeviceSelectedClass(this);
                });
                attachDeviceEvents();
            } else {
                $('#device-table').addClass('hidden');
                $('#device-listing-status-msg').text('No device is available to be displayed.');
            }
            $("#loading-content").remove();
            $('#device-grid').datatables_extended();
            $(".icon .text").res_text(0.2);
        });
    });

}

/*
 * DOM ready functions.
 */
$(document).ready(function () {

    var groupId = getParameterByName('groupId');

    if (groupId) {
        loadGroupedDevices(groupId);
    } else {
        loadDevices();
    }

    //$('#device-grid').datatables_extended();

    /* Adding selected class for selected devices */
    $(deviceCheckbox).each(function () {
        addDeviceSelectedClass(this);
    });

    var i;
    var permissionList = $("#permission").data("permission");
    for (i = 0; i < permissionList.length; i++) {
        $.setPermission(permissionList[i]);
    }

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
            var endPoint = "api/group/all";

            $(modalPopupContent).html($('#group-device-modal-content').html());
            $('#user-groups').html('<div style="height:100px" data-state="loading" data-loading-text="Loading..." data-loading-style="icon-only" data-loading-inverse="true"></div>');
            $("a#group-device-yes-link").hide();
            showPopup();

            var getGroupsRequest = $.ajax({
                                              url: endPoint,
                                              method: "GET",
                                              contentType: "application/json",
                                              accept: "application/json"
                                          });

            getGroupsRequest.done(function (data, txtStatus, jqxhr) {
                                      var groups = JSON.parse(data);
                                      var status = jqxhr.status;
                                      if (status == 200) {
                                          groups = groups.data;
                                          if (groups.length <= 0) {
                                              $('#user-groups').html("There is no any groups available");
                                              return;
                                          }
                                          var str = '<br /><select id="assign-group-selector" style="color:#3f3f3f;padding:5px;width:250px;">';
                                          for (var group in groups) {
                                              str += '<option value="' + groups[group].id + '">' + groups[group].name + '</option>';
                                          }
                                          str += '</select>';
                                          $('#user-groups').html(str);
                                          $("a#group-device-yes-link").show();
                                          $("a#group-device-yes-link").click(function () {
                                              var selectedGroupId = $('#assign-group-selector').val();
                                              endPoint = "api/group/id/" + selectedGroupId + "/assign";
                                              var device = {"deviceId": deviceId, "deviceType": deviceType};

                                              var assignRequest = $.ajax({
                                                                             url: endPoint,
                                                                             method: "POST",
                                                                             contentType: "application/json",
                                                                             accept: "application/json",
                                                                             data: JSON.stringify(device)
                                                                         });

                                              assignRequest.done(function (data, txtStatus, jqxhr) {
                                                                     var status = jqxhr.status;
                                                                     if (status == 200) {
                                                                         $(modalPopupContent).html($('#group-associate-device-200-content').html());
                                                                         setTimeout(function () {
                                                                             hidePopup();
                                                                             location.reload(false);
                                                                         }, 2000);
                                                                     } else {
                                                                         displayDeviceErrors(jqXHR);
                                                                     }
                                                                 }
                                              );
                                              assignRequest.fail(function (jqXHR) {
                                                  displayDeviceErrors(jqXHR);
                                              });
                                          });
                                      }
                                  }
            );
            getGroupsRequest.fail(function (jqXHR) {
                displayDeviceErrors(jqXHR);
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
        var deviceId = $(this).data("deviceid");
        var deviceType = $(this).data("devicetype");
        var removeDeviceAPI = "/devicemgt/api/devices/" + deviceType + "/" + deviceId + "/remove";

        $(modalPopupContent).html($('#remove-device-modal-content').html());
        showPopup();

        $("a#remove-device-yes-link").click(function () {
            var postOperationRequest = $.ajax({
                                                  url: removeDeviceAPI,
                                                  method: "post"
                                              });
            postOperationRequest.done(function (data) {
                $(modalPopupContent).html($('#remove-device-200-content').html());
                window.location.reload(false);
            });
            postOperationRequest.fail(function (jqXHR, textStatus) {
                displayDeviceErrors(jqXHR);
            });
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
        var editDeviceAPI = "/devicemgt/api/devices/" + deviceType + "/" + deviceId + "/update?name=";

        $(modalPopupContent).html($('#edit-device-modal-content').html());
        $('#edit-device-name').val(deviceName);
        showPopup();

        $("a#edit-device-yes-link").click(function () {
            var newDeviceName = $('#edit-device-name').val();
            var postOperationRequest = $.ajax({
                                                  url: editDeviceAPI + newDeviceName,
                                                  method: "post"
                                              });
            postOperationRequest.done(function (data) {
                $(modalPopupContent).html($('#edit-device-200-content').html());
                $("h4[data-deviceid='" + deviceId + "']").html(newDeviceName);
                setTimeout(function () {
                    hidePopup();
                }, 2000);
            });
            postOperationRequest.fail(function (jqXHR, textStatus) {
                displayDeviceErrors(jqXHR);
            });
        });

        $("a#edit-device-cancel-link").click(function () {
            hidePopup();
        });
    });
}

function displayDeviceErrors(jqXHR) {
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

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
            results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}
