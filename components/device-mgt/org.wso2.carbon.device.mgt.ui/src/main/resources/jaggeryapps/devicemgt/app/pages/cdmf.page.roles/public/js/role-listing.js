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

var loadRoleBasedActionURL = function (action, rolename) {
    var href = $("#ast-container").data("app-context") + "role/" + action + "/" + rolename;
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
$(".icon .text").res_text(0.2);


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
    $('body').removeClass('modal-open').css('padding-right','0px');
    $('.modal-backdrop').remove();
}


/**
 * Following function would execute
 * when a user clicks on the list item
 * initial mode and with out select mode.
 */
function InitiateViewOption() {
    $(location).attr('href', $(this).data("url"));
}

function loadRoles() {
    var loadingContent = $("#loading-content");
    loadingContent.show();

    var dataFilter = function (data) {
        data = JSON.parse(data);

        var objects = [];

        $(data.roles).each(function( index ) {
            objects.push(
                {
                    name: data.roles[index],
                    DT_RowId: "role-" + data.roles[index]
                }
            )
        });

        var json = {
            "recordsTotal": data.count,
            "recordsFiltered": data.count,
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
            class: "remove-padding icon-only content-fill",
            data: null,
            defaultContent: "<div class='thumbnail icon'>" +
                "<i class='square-element text fw fw-bookmark' style='font-size: 74px;'></i>" +
                "</div>"
        },
        {
            class: "fade-edge",
            data: "name",
            render: function (name, type, row, meta) {
                return '<h4>&nbsp;&nbsp;' + name + '&nbsp;role</h4>';
            }
        },
        {
            class: "text-right content-fill text-left-on-grid-view no-wrap",
            data: null,
            render: function (data, type, row, meta) {
                return '&nbsp;' +
                    '<a onclick="javascript:loadRoleBasedActionURL(\'edit\', \'' + data.name + '\')" ' +
                    'data-role="' + data.name + '" ' +
                    'data-click-event="edit-form" ' +
                    'class="btn padding-reduce-on-grid-view edit-role-link">' +
                    '<span class="fw-stack">' +
                    '<i class="fw fw-ring fw-stack-2x"></i>' +
                    '<i class="fw fw-bookmark fw-stack-1x"></i>' +
                    '<span class="fw-stack fw-move-right fw-move-bottom">' +
                    '<i class="fw fw-circle fw-stack-2x fw-stroke fw-inverse"></i>' +
                    '<i class="fw fw-circle fw-stack-2x"></i><i class="fw fw-edit fw-stack-1x fw-inverse"></i>' +
                    '</span>' +
                    '</span>' +
                    '<span class="hidden-xs hidden-on-grid-view">&nbsp;&nbsp;Edit</span>' +
                    '</a>' +
                    '&nbsp;<a onclick="javascript:loadRoleBasedActionURL(\'edit-permission\', \'' + data.name + '\')" ' +
                    'data-role="' + data.name + '" ' +
                    'data-click-event="edit-form" ' +
                    'class="btn padding-reduce-on-grid-view edit-permission-link">' +
                    '<span class="fw-stack">' +
                    '<i class="fw fw-ring fw-stack-2x"></i>' +
                    '<i class="fw fw-security-policy fw-stack-1x"></i>' +
                    '<span class="fw-stack fw-move-right fw-move-bottom">' +
                    '<i class="fw fw-circle fw-stack-2x fw-stroke fw-inverse"></i>' +
                    '<i class="fw fw-circle fw-stack-2x"></i><i class="fw fw-edit fw-stack-1x fw-inverse"></i>' +
                    '</span>' +
                    '</span>' +
                    '<span class="hidden-xs hidden-on-grid-view">&nbsp;&nbsp;Edit Permission</span>' +
                    '</a>' +
                    '&nbsp;<a data-role="' + data.name + '" ' +
                    'data-click-event="remove-form" ' +
                    'class="btn padding-reduce-on-grid-view remove-role-link">' +
                    '<span class="fw-stack">' +
                    '<i class="fw fw-ring fw-stack-2x"></i>' +
                    '<i class="fw fw-delete fw-stack-1x"></i>' +
                    '</span>' +
                    '<span class="hidden-xs hidden-on-grid-view">&nbsp;&nbsp;Remove</span>' +
                    '</a>';
            }
        }
    ];

    var options = {
        "placeholder": "Search By Role Name",
        "searchKey" : "filter"
    };

    $('#role-grid').datatables_extended_serverside_paging(null, '/api/device-mgt/v1.0/roles', dataFilter, columns, fnCreatedRow, null, options);
    loadingContent.hide();

}


/**
 * Following click function would execute
 * when a user clicks on "Remove" link
 * on Role Listing page in WSO2 Devicemgt Console.
 */
$("#role-grid").on("click", ".remove-role-link", function () {
    var role = $(this).data("role");
    var removeRoleAPI = apiBasePath + "/roles/" + role;

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

$(document).ready(function () {
    loadRoles();
});
