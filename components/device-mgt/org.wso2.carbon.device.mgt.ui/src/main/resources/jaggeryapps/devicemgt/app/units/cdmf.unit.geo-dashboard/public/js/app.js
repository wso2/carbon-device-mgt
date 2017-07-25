/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * App.js
 */
$(".modal").draggable({
    handle: ".modal-header"
});

//Clear modal content for reuse the wrapper by other functions
$('body').on('hidden.bs.modal', '.modal', function () {
    $(this).removeData('bs.modal');
});

/*Map layer configurations*/
var map;

var zoomLevel = 15;
var tileSet = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";
var attribution = "&copy; <a href='https://openstreetmap.org/copyright'>OpenStreetMap</a> contributors";

function initialLoad() {
    if (document.getElementById('map') == null) {
        setTimeout(initialLoad, 500); // give everything some time to render
    } else {
        initializeMap();
        processAfterInitializationMap();
        $("#loading").hide();
    }
}

function initializeMap() {
    if (typeof(map) !== 'undefined') {
        map.remove();
    }
    if (document.getElementById('map') == null) {
        console.log("no map");
    } else {
    }
    map = L.map("map", {
        zoom: 14,
        center: [6.927078, 79.861243],
        layers: [defaultOSM],
        zoomControl: true,
        attributionControl: false,
        maxZoom: 20,
        maxNativeZoom: 18
    });
    L.tileLayer(tileSet, {attribution: attribution}).addTo(map);

    map.zoomControl.setPosition('bottomright');
    map.on('click', function (e) {
        $.noty.closeAll();
    });

    map.on('zoomend', function () {
        if (map.getZoom() < 14) {
            // remove busStops
            var layer;
            for (var key in currentSpatialObjects) {
                if (currentSpatialObjects.hasOwnProperty(key)) {
                    object = currentSpatialObjects[key];
                    if (object.type == "STOP")
                        map.removeLayer(object.geoJson);
                }
            }
        } else {

            var layer;
            for (var key in currentSpatialObjects) {
                if (currentSpatialObjects.hasOwnProperty(key)) {
                    object = currentSpatialObjects[key];
                    if (object.type == "STOP")
                        map.addLayer(object.geoJson);
                }
            }
        }

    });
    //setting the sidebar to be opened when page loads
    $("a[href='#left_side_pannel']").trigger('click');
}

/* Attribution control */
function updateAttribution(e) {
    $.each(map._layers, function (index, layer) {
        if (layer.getAttribution) {
            $("#attribution").html((layer.getAttribution()));
        }
    });
}

var attributionControl;
var geoAlertsBar;
var groupedOverlays;
var layerControl;

function processAfterInitializationMap() {
    attributionControl = L.control({
        position: "bottomright"
    });
    attributionControl.onAdd = function (map) {
        var div = L.DomUtil.create("div", "leaflet-control-attribution");
        div.innerHTML = "<a href='#' onclick='$(\"#attributionModal\").modal(\"show\"); return false;'>Attribution</a>";
        return div;
    };
    map.addControl(L.control.fullscreen({position: 'bottomright'}));

    geoAlertsBar = L.control.geoAlerts({position: 'topright'});
    map.addControl(geoAlertsBar);

    groupedOverlays = {
        "Web Map Service layers": {}
    };

    layerControl = L.control.groupedLayers(baseLayers, groupedOverlays, {
        collapsed: true,
        position: 'bottomleft'
    }).addTo(map);

}

/* Highlight search box text on click */
$("#searchbox").click(function () {
    $(this).select();
});

/* TypeAhead search functionality */

var substringMatcher = function () {
    return function findMatches(q, cb) {
        var matches, substrRegex;
        matches = [];
        substrRegex = new RegExp(q, 'i');
        $.each(currentSpatialObjects, function (i, str) {
            if (substrRegex.test(i)) {
                matches.push({value: i});
            }
        });

        cb(matches);
    };
};

var chart;
function createChart() {
    chart = c3.generate({
        bindto: '#chart_div',
        data: {
            columns: [
                ['speed']
            ]
        },
        subchart: {
            show: true
        },
        axis: {
            y: {
                label: {
                    text: 'Speed',
                    position: 'outer-middle'
                }
            }
        },
        legend: {
            show: false
        }
    });
}

var predictionChart;
function createPredictionChart() {
    predictionChart = c3.generate({
        bindto: '#prediction_chart_div',
        data: {
            x: 'x',
            columns: [
                ['traffic']
            ]
        },
        subchart: {
            show: true
        },
        axis: {
            y: {
                label: {
                    text: 'Traffic',
                    position: 'outer-middle'
                }
            },
            x: {
                label: {
                    text: 'UTC hour for today',
                    position: 'outer-middle'
                }
            }

        },
        legend: {
            show: false
        }
    });
}

$('#searchbox').typeahead({
        hint: true,
        highlight: true,
        minLength: 1
    },
    {
        name: 'speed',
        displayKey: 'value',
        source: substringMatcher()
    }).on('typeahead:selected', function ($e, datum) {
    objectId = datum['value'];
    focusOnSpatialObject(objectId)
});

var toggled = false;

function focusOnSpatialObject(objectId) {
    console.log("Selecting" + objectId);
    var spatialObject = currentSpatialObjects[objectId];// (local)
    if (!spatialObject) {
        noty({text: "Device <span style='color:red'>" + objectId + "</span> not in the Map!!", type: "warning"});
        return false;
    }
    clearFocus(); // Clear current focus if any
    selectedSpatialObject = objectId; // (global) Why not use 'var' other than implicit declaration http://stackoverflow.com/questions/1470488/what-is-the-function-of-the-var-keyword-and-when-to-use-it-or-omit-it#answer-1471738

    console.log("Selected " + objectId + " type " + spatialObject.type);
    if (spatialObject.type == "area") {
        spatialObject.focusOn(map);
        return true;
    }

    map.setView(spatialObject.marker.getLatLng(), zoomLevel, {animate: true});
    // TODO: check the map._layersMaxZoom and set the zoom level accordingly

    spatialObject.marker.openPopup();
    getAlertsHistory(deviceType, deviceId, new Date($('#timeFrom').val()).getTime(), new Date($('#timeTo').val()).getTime());
    spatialObject.drawPath();
    if (speedGraphControl) {
        setTimeout(function () {
            createChart();
            chart.load({columns: [spatialObject.speedHistory.getArray()]});
        }, 100);
    }
    map.addControl(L.control.focus({position: 'bottomright', marker: spatialObject.marker, zoomLevel: zoomLevel}));
}


var getProviderData = function (timeFrom, timeTo) {
    //TODO send through invoker util
    var tableData = [];
    var deviceDetails = $(".device-id");
    deviceId = deviceDetails.data("deviceid");
    deviceType = deviceDetails.data("type");

    if (geoFencingEnabled) {
        var serviceUrl = '/api/device-mgt/v1.0/geo-services/stats/' + deviceType + '/' + deviceId + '?from=' + timeFrom + '&to=' + timeTo;
        invokerUtil.get(serviceUrl,
                        function (data) {
                            tableData = JSON.parse(data);
                            if (tableData.length === 0) {
                            showCurrentLocation(tableData);
                            }
                        }, function (message) {
                            showCurrentLocation(tableData);
                        });
    } else {
        showCurrentLocation(tableData);
    }
    return tableData;
};

function showCurrentLocation(tableData){
    var geoCharts = $("#geo-charts");
    var location = geoCharts.data("device-location");
    location.heading = 0;
    location.id = deviceId;
    location.values = {};
    location.values.id = deviceId;
    location.values.information = "Last seen " + timeSince(new Date(location.updatedTime));
    location.values.notify = false;
    location.values.speed = 0;
    location.values.state = "NORMAL";
    location.values.longitude = location.longitude;
    location.values.latitude = location.latitude;
    location.timestamp = location.updatedTime;
    location.values.timeStamp = location.updatedTime;
    location.values.type = deviceType;
    location._version = "1.0.0";
    tableData.push(location);
}

function timeSince(date) {

    if (!date) {
        return "time is unknown";
    }

    var seconds = Math.floor((new Date() - date) / 1000);
    var intervalType;

    var interval = Math.floor(seconds / 31536000);
    if (interval >= 1) {
        intervalType = 'year';
    } else {
        interval = Math.floor(seconds / 2592000);
        if (interval >= 1) {
            intervalType = 'month';
        } else {
            interval = Math.floor(seconds / 86400);
            if (interval >= 1) {
                intervalType = 'day';
            } else {
                interval = Math.floor(seconds / 3600);
                if (interval >= 1) {
                    intervalType = "hour";
                } else {
                    interval = Math.floor(seconds / 60);
                    if (interval >= 1) {
                        intervalType = "minute";
                    } else {
                        interval = seconds;
                        intervalType = "second";
                    }
                }
            }
        }
    }

    if (interval > 1 || interval === 0) {
        intervalType += 's';
    }

    return interval + ' ' + intervalType + ' ago';
}

function notifyError(message) {
    noty({text: message, type: "error"});
}

function enableRealTime() {
    document.getElementById('realTimeShow').style.display = 'none';
    spatialObject = currentSpatialObjects[selectedSpatialObject];
    if (spatialObject) {
        spatialObject.removePath();
        spatialObject.marker.closePopup();
    }
    selectedSpatialObject = null;
    clearFocus();
    clearMap();
    document.getElementById('objectInfo').style.display = 'none';
    isBatchModeOn = false;
}

var geoFencingEnabled = true;
function InitSpatialObject(geoFencingIsEnabled) {
    geoFencingEnabled = geoFencingIsEnabled;
    var spatialObject = drawSpatialObject();
    map.addControl(L.control.focus({position: 'bottomright', marker: spatialObject.marker, zoomLevel: zoomLevel}));
}

function focusOnRecentHistorySpatialObject(objectId) {
    drawSpatialObject();
    noty({text: "Showing last two hours geo history", type: "information"});
}

function drawSpatialObject() {
    var fromDate = new Date();
    fromDate.setHours(fromDate.getHours() - 2);
    var toDate = new Date();
    //console.log(fromDate.valueOf() + " " + toDate.valueOf());
    var tableData = getProviderData(fromDate.valueOf(), toDate.valueOf());
    for (var i = 0; i < tableData.length; i++) {
        var data = tableData[i];
        if (data.values.longitude && data.values.latitude) {
            var geoMessage = {
                "messageType": "Point",
                "type": "Feature",
                "id": data.values.id,
                "deviceId": data.values.id,
                "deviceType": data.values.type,
                "properties": {
                    "speed": data.values.speed,
                    "heading": data.values.heading,
                    "state": data.values.state,
                    "information": data.values.information,
                    "notify": data.values.notify,
                    "type": data.values.type
                },
                "geometry": {
                    "type": "Point",
                    "coordinates": [data.values.longitude, data.values.latitude]
                }
            };
            processPointMessage(geoMessage);
        }
    }
    var spatialObject = currentSpatialObjects[deviceId];// (local)
    if (!spatialObject) {
        noty({text: "Device <span style='color:red'>" + deviceId + "</span> not in the Map!!", type: "warning"});
        return false;
    }
    selectedSpatialObject = deviceId;
    if (spatialObject.type == "area") {
        spatialObject.focusOn(map);
        return true;
    }

    if (geoFencingEnabled) {
        var alertsFromDate = new Date();
        alertsFromDate.setHours(alertsFromDate.getHours() - 24); //last 24 hours
        getAlertsHistory(deviceType, deviceId, alertsFromDate.valueOf(), toDate.valueOf());
    }

    setTimeout(function () {
                   map.invalidateSize();
                   map.setView(spatialObject.marker.getLatLng(), spatialObject.marker.zoomLevel, {animate: true});
                   spatialObject.marker.openPopup();
                   spatialObject.drawPath();
               }, 500);

    if (speedGraphControl) {
        setTimeout(function () {
            createChart();
            chart.load({columns: [spatialObject.speedHistory.getArray()]});
        }, 100);
    }
    return spatialObject;
}


function focusOnHistorySpatialObject(objectId, timeFrom, timeTo) {
    if (!timeFrom) {
        notifyError('No start time provided to show history. Please provide a suitable value' + timeFrom);
    } else if (!timeTo) {
        notifyError('No end time provided to show history. Please provide a suitable value' + timeTo);
    } else {
        $('#dateRangePopup').dialog('close');
        document.getElementById('realTimeShow').style.display = 'block';
        isBatchModeOn = true;
        clearFocus(); // Clear current focus if any
        clearMap();
        var fromDate = new Date(timeFrom);
        var toDate = new Date(timeTo);
        //console.log(fromDate.valueOf() + " " + toDate.valueOf());
        var tableData = getProviderData(fromDate.valueOf(), toDate.valueOf());
        for (var i = 0; i < tableData.length; i++) {
            var data = tableData[i];
            var geoMessage = {
                "messageType": "Point",
                "type": "Feature",
                "id": data.values.id,
                "deviceId": data.values.id,
                "deviceType": data.values.type,
                "properties": {
                    "speed": data.values.speed,
                    "heading": data.values.heading,
                    "state": data.values.state,
                    "information": data.values.information,
                    "notify": data.values.notify,
                    "type": data.values.type
                },
                "geometry": {
                    "type": "Point",
                    "coordinates": [data.values.longitude, data.values.latitude]
                }
            };
            processPointMessage(geoMessage);
        }
        var spatialObject = currentSpatialObjects[objectId];// (local)
        if (!spatialObject) {
            noty({text: "Device <span style='color:red'>" + deviceId + "</span> not in the Map!!", type: "warning"});
            return false;
        }
        selectedSpatialObject = objectId; // (global) Why not use 'var' other than implicit declaration http://stackoverflow.com/questions/1470488/what-is-the-function-of-the-var-keyword-and-when-to-use-it-or-omit-it#answer-1471738

        console.log("Selected " + objectId + " type " + spatialObject.type);
        if (spatialObject.type == "area") {
            spatialObject.focusOn(map);
            return true;
        }

        map.setView(spatialObject.marker.getLatLng(), zoomLevel, {animate: true});
        // TODO: check the map._layersMaxZoom and set the zoom level accordingly

        spatialObject.marker.openPopup();
        getAlertsHistory(deviceType, deviceId, new Date($('#timeFrom').val()).getTime(), new Date($('#timeTo').val()).getTime());
        spatialObject.drawPath();
        if (speedGraphControl) {
            setTimeout(function () {
                createChart();
                chart.load({columns: [spatialObject.speedHistory.getArray()]});
            }, 100);
        }
    }
}

// Unfocused on current searched spatial object
function clearFocus() {
    if (selectedSpatialObject) {
        spatialObject = currentSpatialObjects[selectedSpatialObject];
        spatialObject.removeFromMap();
        selectedSpatialObject = null;
    }
}

function createGeoToolListItem(link, text, icon, menuRoot) {
    var listItem = $("<div/>", { class: 'action-btn filter'}).appendTo(menuRoot);
    var anchor = $("<a/>", {href: link, text: ' ' + text}).appendTo(listItem);
    anchor.attr('data-toggle', 'modal');
    anchor.attr('data-target', '#commonModal');
    $("<i/>", {class: icon}).prependTo(anchor);
    return listItem;
}

function formatDate(date) {
    var hours = date.getHours();
    var minutes = date.getMinutes();
    var ampm = hours >= 12 ? 'pm' : 'am';
    hours = hours % 12;
    hours = hours ? hours : 12; // the hour '0' should be '12'
    minutes = minutes < 10 ? '0'+minutes : minutes;
    var strTime = hours + ':' + minutes + ' ' + ampm;
    return date.getDate()  + "/"  + date.getMonth()+1 + "/" + date.getFullYear() + " " + strTime;
}