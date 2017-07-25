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

var loadDeviceTypeBasedActionURL = function (action, deviceTypeName) {
    href = $("#ast-container").data("app-context") + "device-type/" + action + "?type=" + encodeURIComponent(deviceTypeName);
    $(location).attr('href', href);
};

$(function () {
    var sortableElem = '.wr-sortable';
    $(sortableElem).sortable({
        beforeStop: function () {
            $(this).sortable('toArray');
        }
    });
    $(sortableElem).disableSelection();
});

var apiBasePath = "/api/device-mgt/v1.0";
var modalPopup = ".modal";
var modalPopupContainer = modalPopup + " .modal-content";
var modalPopupContent = modalPopup + " .modal-content";
var body = "body";
var isInit = true;
var isCloud = false;


/**
 *
 * Fires the res_text when ever a data table redraw occurs making
 * the font icons change the size to respective screen resolution.
 *
 */
$(document).on('draw.dt', function () {
    $(".icon .text").res_text(0.2);
});


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
    //setPopupMaxHeight();
}

/*
 * hide popup function.
 */
function hidePopup() {
    $(modalPopupContent).html('');
    $(modalPopup).modal('hide');
    $('body').removeClass('modal-open').css('padding-right', '0px');
    $('.modal-backdrop').remove();
}


/**
 * Following function would execute
 * when a user clicks on the list item
 * initial mode and with out select mode.
 */
function InitiateViewOption() {
    // $(location).attr('href', $(this).data("url"));
}

function htmlspecialchars(text) {
    return jQuery('<div/>').text(text).html();
}

function loadDeviceTypes() {
    var loadingContent = $("#loading-content");
    loadingContent.show();

    var dataFilter = function (data) {
        data = JSON.parse(data);
        var objects = [];
        $(data).each(function (index) {
            objects.push(
                {
                    name: htmlspecialchars(data[index].name),
                    DT_RowId: "devicetype-" + htmlspecialchars(data[index].name),
					metaDefinition: (data[index].deviceTypeMetaDefinition ? true : false)
                }
            )
        });

        var json = {
            "recordsTotal": data.length,
            "recordsFiltered": data.length,
            "data": objects
        };

        return JSON.stringify(json);
    };

    //noinspection JSUnusedLocalSymbols
    var fnCreatedRow = function (nRow, aData, iDataIndex) {
        $(nRow).attr('data-type', 'selectable');
    };

    //noinspection JSUnusedLocalSymbols
    var columns = [
        {
            class: "remove-padding content-fill",
            data: null,
            defaultContent: "<div class='thumbnail icon'>" +
            "<i class='square-element text fw fw-devices' style='font-size: 74px;'></i>" +
            "</div>"
        },
        {
            class: "",
            data: "name",
            render: function (name, type, row, meta) {
                return '<h4>' + name.replace("devicemgt", "") + '</h4>';
            }
        },
        {
            class: "text-right content-fill text-left-on-grid-view no-wrap",
            data: null,
            render: function (data, type, row, meta) {
                var isCloud = false;
                if ($('#is-cloud').length > 0) {
                    isCloud = true;
                }

                var innerhtml = '';
				if (data.metaDefinition) {

					var editLink = '<a onclick="javascript:loadDeviceTypeBasedActionURL(\'edit\', \'' + data.name + '\')" ' +
						'data-devicetype="' + data.name + '" ' +
						'data-click-event="edit-form" ' +
						'class="btn padding-reduce-on-grid-view edit-devicetype-link">' +
						'<span class="fw-stack">' +
						'<i class="fw fw-circle-outline fw-stack-2x"></i>' +
						'<i class="fw fw-devices fw-stack-1x"></i>' +
						'<span class="fw-stack fw-move-right fw-move-bottom">' +
						'<i class="fw fw-circle fw-stack-2x fw-stroke fw-inverse"></i>' +
						'<i class="fw fw-circle fw-stack-2x"></i><i class="fw fw-edit fw-stack-1x fw-inverse"></i>' +
						'</span>' +
						'</span>' +
						'<span class="hidden-xs hidden-on-grid-view">Edit</span>' +
						'</a>';

					var editEventLink = '<a onclick="javascript:loadDeviceTypeBasedActionURL(\'edit-event\', \'' + data.name + '\')" ' +
						'data-devicetype="' + data.name + '" ' +
						'data-click-event="edit-form" ' +
						'class="btn padding-reduce-on-grid-view edit-event-link">' +
						'<span class="fw-stack">' +
						'<i class="fw fw-circle-outline fw-stack-2x"></i>' +
						'<i class="fw fw-document fw-stack-1x"></i>' +
						'<span class="fw-stack fw-move-right fw-move-bottom">' +
						'<i class="fw fw-circle fw-stack-2x fw-stroke fw-inverse"></i>' +
						'<i class="fw fw-circle fw-stack-2x"></i><i class="fw fw-edit fw-stack-1x fw-inverse"></i>' +
						'</span>' +
						'</span>' +
						'<span class="hidden-xs hidden-on-grid-view">Edit Event</span>' +
						'</a>';

					innerhtml = editLink + editEventLink;
				}
                return innerhtml;
            }
        }
    ];

    var options = {
        "placeholder": "Search By Device Type Name",
        "searchKey": "filter",
		"searching": false
    };
    var settings = {
        "sorting": false
    };
    var deviceTypeApiUrl = '/api/device-mgt/v1.0/admin/device-types';

    $('#devicetype-grid').datatables_extended_serverside_paging(settings, deviceTypeApiUrl, dataFilter, columns, fnCreatedRow, null, options);
    loadingContent.hide();

}

$(document).ready(function () {
    loadDeviceTypes();
});
