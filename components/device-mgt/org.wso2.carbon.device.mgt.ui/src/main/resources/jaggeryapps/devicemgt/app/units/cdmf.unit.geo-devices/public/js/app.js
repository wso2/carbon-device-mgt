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

var zoomLevel = 15;
var tileSet = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";
var attribution = "&copy; <a href='https://openstreetmap.org/copyright'>OpenStreetMap</a> contributors";

function initialLoad() {
    if (document.getElementById('map') == null) {
        setTimeout(initialLoad, 500); // give everything some time to render
    } else {
        initializeMap();
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
    var backEndUrl = '/api/device-mgt/v1.0/geo-services/1.0.0/stats/deviceLocations'+
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
                cluster.deviceIdentification,cluster.deviceType);

        }
    }
}

function geoClusterMarker(count, clusterLat, clusterLong, minLat, maxLat, minLong, maxLong,deviceIdentification,
                          deviceType) {
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

var devicePopupManagement= function(deviceName, deviceType, deviceIdentifier,deviceStatus,deviceOwner){

    var deviceMgtUrl= "/devicemgt/device/";
    var html1='<div>';
    var html2 = '<p><h3>'+'<a href="' + deviceMgtUrl +deviceType+'?id='+deviceIdentifier+ '" target="_blank">' + deviceName + '</a>'+'</h3></p>' ;
    var html3 = '<p>'+'Type : '+ deviceType+'</p>';
    var html4 = '<p>'+'Status : '+deviceStatus+'</p>';
    var html5 = '<p>'+ 'Owner : ' + deviceOwner + '</p>';
    var html6='</div>';
    var html=html1+html2+html3+html4+html5+html6;
    return html;
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
