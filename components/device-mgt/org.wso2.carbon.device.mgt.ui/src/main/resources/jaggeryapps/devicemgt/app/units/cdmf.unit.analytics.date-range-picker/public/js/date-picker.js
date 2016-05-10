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

var fromDate, toDate, currentDay = new Date();
var startDate = new Date(currentDay.getTime() - (60 * 60 * 24 * 100));
var endDate = new Date(currentDay.getTime());

function initDate() {
    currentDay = new Date();
}

var DateRange = convertDate(startDate) + " to " + convertDate(endDate);

$(document).ready(function () {
    initDate();
    var configObject = {
        startOfWeek: 'monday',
        separator: ' to ',
        format: 'YYYY-MM-DD HH:mm',
        autoClose: false,
        time: {
            enabled: true
        },
        shortcuts: 'hide',
        endDate: currentDay,
        maxDays: 2,
        getValue: function () {
            return this.value;
        },
        setValue: function (s) {
            this.value = s;
        }
    };
    $('#date-range').html(DateRange);
    $('#date-range').dateRangePicker(configObject)
            .bind('datepicker-apply', function (event, dateRange) {
                      $(this).addClass('active');
                      $(this).siblings().removeClass('active');
                      fromDate = dateRange.date1 != "Invalid Date" ? dateRange.date1.getTime() / 1000 : null;
                      toDate = dateRange.date2 != "Invalid Date" ? dateRange.date2.getTime() / 1000 : null;
                      drawGraph(fromDate, toDate);
                  }
            );
    setDateTime(currentDay.getTime() - 3600000, currentDay.getTime());
    $('#hour-btn').addClass('active');
});

//hour
$('#hour-btn').on('click', function () {
    initDate();
    setDateTime(currentDay.getTime() - 3600000, currentDay.getTime());
});

//12 hours
$('#h12-btn').on('click', function () {
    initDate();
    setDateTime(currentDay.getTime() - (3600000 * 12), currentDay.getTime());
});

//24 hours
$('#h24-btn').on('click', function () {
    initDate();
    setDateTime(currentDay.getTime() - (3600000 * 24), currentDay.getTime());
});

//48 hours
$('#h48-btn').on('click', function () {
    initDate();
    setDateTime(currentDay.getTime() - (3600000 * 48), currentDay.getTime());
});

$('body').on('click', '.btn-group button', function (e) {
    $(this).addClass('active');
    $(this).siblings().removeClass('active');
});

function setDateTime(from, to) {
    fromDate = from;
    toDate = to;
    startDate = new Date(from);
    endDate = new Date(to);
    DateRange = convertDate(startDate) + " to " + convertDate(endDate);
    $('#date-range').html(DateRange);
    var tzOffset = new Date().getTimezoneOffset() * 60 / 1000;
    from += tzOffset;
    to += tzOffset;

    // Implement drawGraph_<device type name> method in your UI unit for analytics.
    var deviceTypes = $("#device-type-details").data("devicetypes");
    for (var i = 0; i < deviceTypes.length; i++){
        window["drawGraph_" + deviceTypes](parseInt(from / 1000), parseInt(to / 1000));
    }
}

function convertDate(date) {
    var month = date.getMonth() + 1;
    var day = date.getDate();
    var hour = date.getHours();
    var minute = date.getMinutes();
    return date.getFullYear() + '-' + (('' + month).length < 2 ? '0' : '') + month + '-' +
           (('' + day).length < 2 ? '0' : '') + day + " " + (('' + hour).length < 2 ? '0' : '') +
           hour + ":" + (('' + minute).length < 2 ? '0' : '') + minute;
}
