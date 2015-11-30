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

var graph;
var xAxis;

var deviceType = $("#details").data("devicetype");
var deviceId = $("#details").data("deviceid");
var monitor_operations = $("#details").data("monitor");

var marker_1 = '/store/extensions/app/store-device-type/themes/store/img/map-marker-1.png';
var marker_2 = '/store/extensions/app/store-device-type/themes/store/img/map-marker-2.png';

var map;
var mapPoints = [], mapPaths = [], mapMarkers = [];
function initMap() {
    map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 6.9344, lng: 79.8428},
        zoom: 12
    });
}

function formatDates() {
    $(".formatDate").each(function () {
        var timeStamp = $(this).html();
        $(this).html(getDateString(timeStamp));
    });
}

function getDateString(timeStamp) {
    var monthNames = [
        "Jan", "Feb", "Mar",
        "Apr", "May", "Jun", "Jul",
        "Aug", "Sept", "Oct",
        "Nov", "Dec"
    ];

    var date = new Date(parseInt(timeStamp));
    var day = date.getDate();
    var monthIndex = date.getMonth() + 1;
    if (monthIndex < 10) {
        monthIndex = "0" + monthIndex;
    }
    var year = date.getFullYear();

    var hours = date.getHours();
    var amPm = hours < 12 ? "AM" : "PM";
    if (hours > 12) {
        hours -= 12;
    }
    if (hours == 0) {
        hours = 12;
    }
    return day + '-'
           + monthNames[monthIndex - 1] + '-'
           + year + ' ' + hours + ':' + date.getMinutes() + amPm;
}

$(window).on('resize', function () {
    if (graph) {
        location.reload(false);
    }
});

$(document).ready(function () {
    formatDates();
    updateGraphs();
});

$("form").on('submit', function (e) {
    var postOperationRequest = $.ajax({
        url: $(this).attr("action") + '&' + $(this).serialize(),
        method: "post"
    });

    var lblSending = $('#lblSending', this);
    lblSending.removeClass('hidden');

    var lblSent = $('#lblSent', this);
    var sentValue = $(this).find('input[name="value"]').val();
    postOperationRequest.done(function (data) {
        lblSending.addClass('hidden');
        lblSent.removeClass('hidden');
        setTimeout(function () {
            lblSent.addClass('hidden');
        }, 3000);
        $('#lblLastState').text('Current value: ' + (sentValue == '1' ? 'On' : 'Off'));
    });

    postOperationRequest.fail(function (jqXHR, textStatus) {
        lblSending.addClass('hidden');
        lblSent.addClass('hidden');
    });
    e.preventDefault();
});

function updateGraphs() {
    var tv = 5000;

    var fields = [];
    for (var op in monitor_operations) {
        if (monitor_operations[op].name == 'gps') {
            $('#map').removeClass('hidden');
        } else {
            fields.push({name: monitor_operations[op].name});
        }
    }

    // instantiate our graph!
    graph = new Rickshaw.Graph({
        element: document.getElementById("chart"),
        width: $("#chartWrapper").width() - 50,
        height: 300,
        renderer: 'line',
        series: new Rickshaw.Series.FixedDuration(fields, undefined, {
            timeInterval: 10000,
            maxDataPoints: 20,
            timeBase: new Date().getTime() / 1000
        })
    });

    var iv = setInterval(function () {

        var getStatsRequest = $.ajax({
            url: "/store/apis/operations/" + deviceType + "/stats?deviceId=" + deviceId,
            method: "get"
        });

        getStatsRequest.done(function (data) {
            var stats = data.data;
            var lastUpdate = -1;
            var graphVals = {};
            for (var s in stats) {
                var val = stats[s];
                if (!val){
                    continue;
                }
                if (val.time > lastUpdate) {
                    lastUpdate = val.time;
                }
                delete val['time'];
                if (val.map) {
                    mapPoints.push(val.map);
                    var marker = new google.maps.Marker({
                        position: val.map,
                        map: map,
                        icon: marker_1,
                        title: 'Seen at ' + getDateString(lastUpdate)
                    });
                    marker.setMap(map);
                    map.panTo(val.map);
                    mapMarkers.push(marker);

                    if (mapPoints.length > 1 ){
                        var l = mapPoints.length;
                        var path = new google.maps.Polyline({
                            path: [mapPoints[l - 1], mapPoints[l - 2]],
                            geodesic: true,
                            strokeColor: '#FF0000',
                            strokeOpacity: 1.0,
                            strokeWeight: 2
                        });

                        path.setMap(map);
                        mapPaths.push(path);

                        mapMarkers[l - 2].setIcon(marker_2);
                    }

                    if (mapPoints.length >= 10){
                        mapMarkers[0].setMap(null);
                        mapMarkers.splice(0, 1);

                        mapPaths[0].setMap(null);
                        mapPaths.splice(0, 1);

                        mapPoints.splice(0, 1);
                    }
                } else {
                    for (var key in val) {
                        graphVals[key] = val[key];
                    }
                }
            }
            graph.series.addData(graphVals);

            if (lastUpdate == -1){
                $('#last_seen').text("Not seen recently");
            }

            var timeDiff = new Date().getTime() - lastUpdate;
            if (timeDiff < tv * 2) {
                graph.render();
                $('#last_seen').text("Last seen: A while ago");
            } else if (timeDiff < 60 * 1000) {
                graph.render();
                $('#last_seen').text("Last seen: Less than a minute ago");
            } else if (timeDiff < 60 * 60 * 1000) {
                $('#last_seen').text("Last seen: " + Math.round(timeDiff / (60 * 1000)) + " minutes ago");
            } else {
                $('#last_seen').text("Last seen: " + getDateString(lastUpdate));
            }
        });

        //var data = {Temperature: Math.floor(Math.random() * (50 - 20) + 20)};
        //
        //graph.series.addData(data);
        //graph.render();

    }, tv);

    graph.render();

    xAxis = new Rickshaw.Graph.Axis.Time({
        graph: graph
    });

    xAxis.render();

    var y_ticks = new Rickshaw.Graph.Axis.Y({
        graph: graph,
        orientation: 'left',
        height: 300,
        tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
        element: document.getElementById('y_axis')
    });

    var legend = new Rickshaw.Graph.Legend({
        graph: graph,
        element: document.getElementById('legend')
    });

    var hoverDetail = new Rickshaw.Graph.HoverDetail({
        graph: graph
    });

    var shelving = new Rickshaw.Graph.Behavior.Series.Toggle({
        graph: graph,
        legend: legend
    });

    var order = new Rickshaw.Graph.Behavior.Series.Order({
        graph: graph,
        legend: legend
    });

    var highlighter = new Rickshaw.Graph.Behavior.Series.Highlight({
        graph: graph,
        legend: legend
    });
}