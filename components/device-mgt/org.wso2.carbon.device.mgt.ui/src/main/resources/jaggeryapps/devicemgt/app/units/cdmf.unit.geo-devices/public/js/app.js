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
var geoClusters;
var markersLayer = new L.LayerGroup();
var popupContent;
var clusterLat;
var clusterLong;
var lastSeen;
var geoFencingEnabled;

var zoomLevel = 15;
var tileSet = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";
var attribution = "&copy; <a href='https://openstreetmap.org/copyright'>OpenStreetMap</a> contributors";

function initialLoad(geoFencingEnabled) {
    window.geoFencingEnabled = geoFencingEnabled;
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
        zoom: 3,
        center: [0, 0],
        layers: [defaultOSM],
        zoomControl: true,
        attributionControl: false,
        maxZoom: 20,
        maxNativeZoom: 18
    });
    L.tileLayer(tileSet, {attribution: attribution}).addTo(map);
    markersLayer.clearLayers();
    markersLayer.addTo(map);

    showMarkersOnChange();

    map.zoomControl.setPosition('bottomright');
    map.on('click', function (e) {
        $.noty.closeAll();
    });

    map.on('zoomend', function () {
        currentZoomLevel = map.getZoom();
        setTimeout(showMarkesOnZoomEnd(currentZoomLevel),2000);
    });

    map.on('dragend',function(){
        showMarkersOnChange();
    });
    //setting the sidebar to be opened when page loads
    $("a[href='#left_side_pannel']").trigger('click');
}

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
    if (geoFencingEnabled) {
        map.addControl(geoAlertsBar);
    }

    groupedOverlays = {
        "Web Map Service layers": {}
    };

    layerControl = L.control.groupedLayers(baseLayers, groupedOverlays, {
        collapsed: true,
        position: 'bottomleft'
    }).addTo(map);

    if (geoFencingEnabled) {
        var alertsFromDate = new Date();
        var toDate = new Date();
        alertsFromDate.setHours(alertsFromDate.getHours() - 24); //last 24 hours
        getAlertsHistory(alertsFromDate.valueOf(), toDate.valueOf());
    };
}


var showMarkesOnZoomEnd = function (zoomLevel) {
     if(map.getZoom()===zoomLevel){
         showMarkersOnChange();
     }
}
var showMarkersOnChange=function(){
    var bounds = map.getBounds();
    var maxLat = bounds._northEast.lat;
    var maxLong = bounds._northEast.lng;
    var minLat = bounds._southWest.lat;
    var minLong = bounds._southWest.lng;
    var zoom = map.getZoom();
    var backEndUrl = '/api/device-mgt/v1.0/geo-services/1.0.0/stats/device-locations'+
        '?'+'&minLat='+minLat+'&maxLat='+maxLat+'&minLong='+minLong+
        '&maxLong='+maxLong+'&zoom='+zoom;
    markersLayer.clearLayers();
    invokerUtil.get(backEndUrl,successCallBackRectangles,function (error) {
        console.log("error when calling backend api to retrieve geo clusters");
        console.log(error);
    });
}


var successCallBackRectangles = function (clusters) {
    geoClusters=clusters;
    geoGridControl();
};


/* Geo-Grid control*/
function geoGridControl(){

    var geoClustersJsonObject=JSON.parse(geoClusters);
    for (var key in geoClustersJsonObject) {
        if (geoClustersJsonObject.hasOwnProperty(key)) {
            var cluster = geoClustersJsonObject[key];
            var count= parseInt(cluster.count);
            geoClusterMarker(count,cluster.coordinates.latitude,cluster.coordinates.longitude,
                cluster.southWestBound.latitude,
                    cluster.northEastBound.latitude,cluster.southWestBound.longitude,cluster.northEastBound.longitude,
                cluster.deviceIdentification,cluster.deviceType, cluster.lastSeen);

        }
    }
}

function geoClusterMarker(count, clusterLat, clusterLong, minLat, maxLat, minLong, maxLong,deviceIdentification,
                          deviceType, lastSeen) {
    window.clusterLat = clusterLat;
    window.clusterLong = clusterLong;
    window.lastSeen = lastSeen;
    var deviceMarker = L.AwesomeMarkers.icon({
        icon: ' ',
        markerColor: 'blue'
    });
    var rectangle_details = {count: count,minLat: minLat-0.001, maxLat: maxLat+0.001, minLong: minLong-0.001,
        maxLong: maxLong+0.001,deviceIdentification:deviceIdentification,deviceType:deviceType};
    var event_capture = function (extra_data,marker) {
        return function (event) {
            // event and extra_data will be available here
            handleMarkerEvents(event,extra_data,marker);
        };
    };
    if(count == 1) {
        var marker = L.marker([clusterLat, clusterLong], {
            icon: deviceMarker
        });
        marker.addEventListener("mouseover",event_capture(rectangle_details,marker));
    }else{
        var marker = L.marker([clusterLat, clusterLong], {
            icon: L.divIcon({
                iconSize: [30, 30], html: count
            })
        }).bindPopup(null);
    }
    marker.addEventListener("click",event_capture(rectangle_details,marker));
    marker.addTo(markersLayer);
}

function handleMarkerEvents(event,extra_data,marker) {
    if(event.type==="click") {
        var southWestCorner = L.latLng(extra_data.minLat, extra_data.minLong);
        var northEastCorner = L.latLng(extra_data.maxLat, extra_data.maxLong);
        var rectangleBounds = L.latLngBounds(southWestCorner, northEastCorner);
        map.fitBounds(rectangleBounds);
    }else if (event.type=="mouseover"){
        var deviceType = extra_data.deviceType;
        var deviceIdentification = extra_data.deviceIdentification;
        devicePopupContentBackEndCall(deviceType,deviceIdentification,marker,function () {
            marker.bindPopup(popupContent);
            marker.openPopup();
        });
    }

}

var devicePopupManagement= function(deviceName, deviceType, deviceIdentifier,deviceStatus,deviceOwner) {
    if (!geoFencingEnabled) {
        var deviceMgtUrl = "/devicemgt/device/";
        var html1 = '<div>';
        var html2 = '<p><h3>' + '<a href="' + deviceMgtUrl + deviceType + '?id=' + deviceIdentifier + '" target="_blank">' + deviceName + '</a>' + '</h3></p>';
        var html3 = '<p>' + 'Type : ' + deviceType + '</p>';
        var html4 = '<p>' + 'Status : ' + deviceStatus + '</p>';
        var html5 = '<p>' + 'Owner : ' + deviceOwner + '</p>';
        var html6 = '</div>';
        var html = html1 + html2 + html3 + html4 + html5 + html6;
        return html;
    }

    var popupTemplate = $('#markerPopup');
    var fromDate = new Date();
    fromDate.setHours(fromDate.getHours() - 2);
    var toDate = new Date();
    var tableData = getAnalyticsData(deviceIdentifier,deviceType,fromDate.valueOf(),toDate.valueOf());
    var data = tableData[tableData.length-1];

    popupTemplate.find('#objectId').html(deviceIdentifier);
    popupTemplate.find('#information').html(data.values.information);
    popupTemplate.find('#speed').html(Math.round(data.values.speed * 10) / 10);
    popupTemplate.find('#heading').html(angleToHeading(data.values.heading));

    return popupTemplate.html();
};

var devicePopupContentBackEndCall = function(type,deviceIdentification,marker,callback){
    var popupContentBackEndUrl='/api/device-mgt/v1.0/devices/1.0.0/'+type+'/'+deviceIdentification;
    invokerUtil.get(popupContentBackEndUrl,successCallBackDeviceDetails,function (error) {
        console.log("error when calling backend api to retrive device data");
        console.log(error);
    });
    callback();
}

var successCallBackDeviceDetails=function(device){
    var deviceJsonObject = JSON.parse(device);
    var deviceName = deviceJsonObject.name;
    var deviceType = deviceJsonObject.type;
    var deviceIdentifier = deviceJsonObject.deviceIdentifier;
    var deviceStatus = deviceJsonObject.enrolmentInfo.status;
    var deviceOwner = deviceJsonObject.enrolmentInfo.owner;
    popupContent = devicePopupManagement(deviceName,deviceType,deviceIdentifier,deviceStatus,deviceOwner);

}

function getAnalyticsData(deviceId, deviceType, timeFrom, timeTo) {
    var tableData = [];
    var serviceUrl = '/api/device-mgt/v1.0/geo-services/stats/' + deviceType + '/' + deviceId + '?from=' + timeFrom + '&to=' + timeTo;
    invokerUtil.get(serviceUrl,
        function (data) {
            if(data === "") {
                showCurrentLocation(deviceId, deviceType, tableData);
            }
            tableData = JSON.parse(data);
            if (tableData.length === 0) {
                showCurrentLocation(deviceId, deviceType, tableData);
            }
        }, function (message) {
            showCurrentLocation(deviceId, deviceType, tableData);
        });
    return tableData;
}

function showRecentAlertsHistory() {
    if (geoFencingEnabled) {
        var alertsFromDate = new Date();
        var toDate = new Date();
        alertsFromDate.setHours(alertsFromDate.getHours() - 2); //last 24 hours
        getAlertsHistory(alertsFromDate.valueOf(), toDate.valueOf());
    }

    if (speedGraphControl) {
        setTimeout(function () {
            createChart();
            chart.load({columns: [0]}); //TODO: Somehow get device speed history here
        }, 100);
    }

    noty({text: "Showing last two hours geo history", type: "information"});
}

function showAlertsHistory(timeFrom, timeTo) {
    if (!timeFrom) {
        notifyError('No start time provided to show history. Please provide a suitable value' + timeFrom);
    } else if (!timeTo) {
        notifyError('No end time provided to show history. Please provide a suitable value' + timeTo);
    } else {
        $('#dateRangePopup').dialog('close');
        disableRealTime();
        isBatchModeOn = true;
        getAlertsHistory(new Date($('#timeFromCal').val()).getTime(), new Date($('#timeToCal').val()).getTime());

        if (speedGraphControl) {
            setTimeout(function () {
                createChart();
                chart.load({columns: [0]}); //TODO: Somehow get device speed history here
            }, 100);
        }
    }
}

function createGeoToolListItem(link, text, icon, menuRoot, alert) {
    var listItem = $("<div/>", { class: 'action-btn filter'}).appendTo(menuRoot);
    var anchor = $("<a/>", {href: link, text: ' ' + text}).appendTo(listItem);
    if (alert == 'Exit') {
        anchor.attr('data-toggle', 'modal');
        anchor.attr('data-target', '#modalExit');
    } else if (alert == 'Within') {
        anchor.attr('data-toggle', 'modal');
        anchor.attr('data-target', '#modalWithin');
    } else if (alert == 'Stationery') {
        anchor.attr('data-toggle', 'modal');
        anchor.attr('data-target', '#modalStationery');
    } else if (alert == 'Speed') {
        anchor.attr('data-toggle', 'modal');
        anchor.attr('data-target', '#modalSpeed');
    }
    $("<i/>", {class: icon}).prependTo(anchor);
    return listItem;
}

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

function notifyError(message) {
    noty({text: message, type: "error"});
}

function enableRealTime(geoFencingEnabled) {
    $("#realTimeShow").hide();
    $(".geo-alert").show();
    isBatchModeOn = false;
    initializeGeoLocation(geoFencingEnabled);
}

function disableRealTime(){
    $(".geo-alert").hide();
    $("#realTimeShow").show();
}

function showCurrentLocation(deviceId, deviceType, tableData){
    var location = {};
    location.latitude = clusterLat;
    location.longitude = clusterLong;
    location.updatedTime = lastSeen;
    location.id = deviceId;
    location.values = {};
    location.values.id = deviceId;
    location.values.information = "Last seen " + timeSince(new Date(location.updatedTime));
    location.values.notify = false;
    location.values.speed = 0;
    location.values.heading = 0;
    location.values.state = "NORMAL";
    location.values.longitude = location.longitude;
    location.values.latitude = location.latitude;
    location.timestamp = location.updatedTime;
    location.values.timeStamp = location.updatedTime;
    location.values.type = deviceType;
    location._version = "1.0.0";
    tableData.push(location);
}

function formatDate(date) {
    var hours = date.getHours();
    var minutes = date.getMinutes();
    var ampm = hours >= 12 ? 'pm' : 'am';
    hours = hours % 12;
    hours = hours ? hours : 12; // the hour '0' should be '12'
    minutes = minutes < 10 ? '0' + minutes : minutes;
    var strTime = hours + ':' + minutes + ' ' + ampm;
    return date.getDate()  + "/"  + (date.getMonth() + 1) + "/" + date.getFullYear() + " " + strTime;
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