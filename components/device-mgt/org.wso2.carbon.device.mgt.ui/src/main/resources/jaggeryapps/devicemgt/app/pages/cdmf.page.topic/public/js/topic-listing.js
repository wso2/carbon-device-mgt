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
    if (!$(".select-enable-btn").text() == "Select" && !$(this).hasClass("btn")) {
        url = $(this).parent().data("url");
        $(location).attr('href', url);
    }
}

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

    name = getParameterByName("name");

    /* for device list sorting drop down */
    $(".ctrl-filter-type-switcher").popover({
        html: true,
        content: function () {
            return $("#content-filter-types").html();
        }
    });
});

function loadTopics(searchType, searchParam) {

    var serviceURL;
    if ($.hasPermission("VIEW_TOPICS")) {
        serviceURL = "/api/mqtt-topics/v1.0/admin/topics";
    } else {
        $("#loading-content").remove();
        $('#topics-table').addClass('hidden');
        $('#topics-listing-status-msg').text('Permission denied.');
        $("#topics-listing-status").removeClass(' hidden');
        return;
    }

    var columns = [
        {
            targets: 0,
            data: 'localSubscribedQueueOrTopicName',
            class: 'remove-padding-top viewEnabledIcon',
            render: function (data, type, row, meta) {
                return '<div class="thumbnail icon"><img class="square-element text fw fw-bookmark" src=""/></div>';
            }
        },
        {
            targets: 1,
            data: 'localSubscribedQueueOrTopicName',
            class: 'remove-padding-top viewEnabledIcon',
        },
        {
            targets: 2,
            data: 'localNumberOfMessagesRemainingForSubscriber',
            class: 'remove-padding-top viewEnabledIcon',
        },
        {
            targets: 3,
            data: 'localActive',
            class: 'remove-padding-top viewEnabledIcon',
        },
        {
            targets: 4,
            data: 'localDurable',
            class: 'remove-padding-top viewEnabledIcon',
        },
        {
            targets: 5,
            data: 'localSubscriberQueueName',
            class: 'remove-padding-top viewEnabledIcon',
        },
        {
            targets: 6,
            data: 'localSubscriptionIdentifier',
            class: 'remove-padding-top viewEnabledIcon',
        }
    ];

    var fnCreatedRow = function (row, data, dataIndex) {
        $(row).attr('data-type', 'selectable');
        $(row).attr('localSubscribedQueueOrTopicName', htmlspecialchars(data.localSubscribedQueueOrTopicName));
        $(row).attr('localNumberOfMessagesRemainingForSubscriber', htmlspecialchars(data.localNumberOfMessagesRemainingForSubscriber));
        $(row).attr('data-url', context + '/device/' + htmlspecialchars(data.localSubscribedQueueOrTopicName) + '?id=' + htmlspecialchars(data.localNumberOfMessagesRemainingForSubscriber));

        var topicName = htmlspecialchars(data.localSubscribedQueueOrTopicName);
        var remainingMesgCount = htmlspecialchars(data.localNumberOfMessagesRemainingForSubscriber);
        var isActive = htmlspecialchars(data.localActive);
        var isDurable = htmlspecialchars(data.localDurable);
        var localSubscriberQueueName = htmlspecialchars(data.localSubscriberQueueName);
        var subscriptionIdentifier = htmlspecialchars(data.localSubscriptionIdentifier);
        $.each($('td', row), function (colIndex) {
            switch (colIndex) {
                case 1:
                    $(this).attr('data-grid-label', "Topic Name");
                    $(this).attr('data-search', topicName);
                    $(this).attr('data-display', topicName);
                    break;
                case 2:
                    $(this).attr('data-grid-label', "Remaining Messages");
                    $(this).attr('data-search', remainingMesgCount);
                    $(this).attr('data-display', remainingMesgCount);
                    break;
                case 3:
                    $(this).attr('data-grid-label', "Active");
                    $(this).attr('data-search', isActive);
                    $(this).attr('data-display', isActive);
                    break;
                case 4:
                    $(this).attr('data-grid-label', "Durable");
                    $(this).attr('data-search', isDurable);
                    $(this).attr('data-display', isDurable);
                    break;
                case 5:
                    $(this).attr('data-grid-label', "Subscriber Queue Name");
                    $(this).attr('data-search', localSubscriberQueueName);
                    $(this).attr('data-display', localSubscriberQueueName);
                    break;
                case 6:
                    $(this).attr('data-grid-label', "SubscriptionIdentifier");
                    $(this).attr('data-search', subscriptionIdentifier);
                    $(this).attr('data-display', subscriptionIdentifier);
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
        $(data.subscriptions).each(function (index) {
            objects.push(
                {
                    localSubscribedQueueOrTopicName: data.subscriptions[index].localSubscribedQueueOrTopicName,
                    localNumberOfMessagesRemainingForSubscriber: data.subscriptions[index].localNumberOfMessagesRemainingForSubscriber,
                    localActive: data.subscriptions[index].localActive,
                    localDurable: data.subscriptions[index].localDurable,
                    localSubscriberQueueName: data.subscriptions[index].localSubscriberQueueName,
                    localSubscriptionIdentifier: data.subscriptions[index].localSubscriptionIdentifier,
                    name: data.subscriptions[index].localSubscribedQueueOrTopicName
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

    $('#topics-grid').datatables_extended_serverside_paging(
        null,
        serviceURL,
        dataFilter,
        columns,
        fnCreatedRow,
        function () {
            $(".icon .text").res_text(0.2);
            $('#topics-grid').removeClass('hidden');
            $("#loading-content").remove();
        }, {
            "placeholder": "Search By Topic Name",
            "searchKey": "name"
        }
    );
}

/*
 * DOM ready functions.
 */
$(document).ready(function () {
    /* Adding selected class for selected devices */
    var permissionList = $("#permission").data("permission");
    for (var key in permissionList) {
        if (permissionList.hasOwnProperty(key)) {
            $.setPermission(key);
        }
    }
    loadTopics();
    $(".dataTables_toolbar").css("display", "none");

    /* for topic list sorting drop down */
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

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}
