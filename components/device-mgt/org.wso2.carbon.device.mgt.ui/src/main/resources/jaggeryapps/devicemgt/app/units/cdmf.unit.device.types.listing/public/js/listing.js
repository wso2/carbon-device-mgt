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
})();

/*
 * Setting-up global variables.
 */
var deviceCheckbox = "#ast-container .ctrl-wr-asset .itm-select input[type='checkbox']";
var assetContainer = "#ast-container";

/*
 * DOM ready functions.
 */
$(document).ready(function () {
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

    $(".ast-container").on("click", ".claim-btn", function (e) {
        e.stopPropagation();
        var deviceId = $(this).data("deviceid");
        var deviceListing = $("#device-listing");
        var currentUser = deviceListing.data("current-user");
        var serviceURL = "/temp-controller-agent/enrollment/claim?username=" + currentUser;
        var deviceIdentifier = {id: deviceId, type: "TemperatureController"};
        invokerUtil.put(serviceURL, deviceIdentifier, function (message) {
            console.log(message);
        }, function (message) {
            console.log(message);
        });
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

var deviceTypeCount, compiledDeviceTypesCount = 0;

function htmlspecialchars(text) {
    return jQuery('<div/>').text(text).html();
}

function loadDevices(searchType, searchParam) {
    var deviceListing = $("#device-listing");
    var deviceListingSrc = deviceListing.attr("src");
    var currentUser = deviceListing.data("currentUser");

    $('#ast-container').html("");
    deviceTypeCount = deviceTypesList.length;
    if (deviceTypesList.length > 0) {
        for (var i = 0; i < deviceTypesList.length; i++) {
            var viewModel = {};
            viewModel.thumb = deviceTypesList[i].thumb;
            viewModel.appContext = clientJsAppContext;
            viewModel.deviceTypeName = htmlspecialchars(deviceTypesList[i].deviceTypeName);
            viewModel.deviceTypeId = htmlspecialchars(deviceTypesList[i].deviceTypeId);
            viewModel.deviceCategory = htmlspecialchars(deviceTypesList[i].deviceCategory);
            viewModel.deviceTypeLabel = htmlspecialchars(deviceTypesList[i].deviceTypeLabel);
            compileTemplate(viewModel, deviceListingSrc);
        }
    } else {
        $('#device-type-grid').addClass('hidden');
    }

    $(".icon .text").res_text(0.2);

    /*
     * On device checkbox select add parent selected style class
     */
    $(deviceCheckbox).click(function () {
        addDeviceSelectedClass(this);
    });

}

function compileTemplate(viewModel, templateSrc) {
    $.template("device-listing", templateSrc, function (template) {
        $("#ast-container").html($("#ast-container").html() + template(viewModel));
        compiledDeviceTypesCount++;
        if (deviceTypeCount == compiledDeviceTypesCount) {
            $('#device-type-grid').datatables_extended({"bFilter": false, "order": [[1, "asc"]]});
        }
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


/*
 * DOM ready functions.
 */
$(document).ready(function () {
    loadDevices();
    //$('#device-type-grid').datatables_extended();

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

    $(".ast-container").on("click", ".claim-btn", function (e) {
        e.stopPropagation();
        var deviceId = $(this).data("deviceid");
        var deviceListing = $("#device-listing");
        var currentUser = deviceListing.data("current-user");
        var serviceURL = "/temp-controller-agent/enrollment/claim?username=" + currentUser;
        var deviceIdentifier = {id: deviceId, type: "TemperatureController"};
        invokerUtil.put(serviceURL, deviceIdentifier, function (message) {
            console.log(message);
        }, function (message) {
            console.log(message);
        });
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

    $(document).on("click", "tr.clickable-row", function () {
        window.document.location = $(this).data('href');
    })

});
