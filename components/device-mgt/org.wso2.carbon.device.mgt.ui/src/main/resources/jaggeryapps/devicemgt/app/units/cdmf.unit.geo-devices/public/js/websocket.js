/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var debugObject; // assign object and debug from browser console, this is for debugging purpose , unless this var is unused
var showPathFlag = false; // Flag to hold the status of draw objects path
var currentSpatialObjects = {};
var selectedSpatialObject; // This is set when user search for an object from the search box
var spatialWebsocket;
var onAlertWebsocket;
var onTrafficStreamWebsocket;
var currentPredictions = {};
// Make the function wait until the connection is made...
var waitTime = 1000;
var webSocketURL, alertWebSocketURL, trafficStreamWebSocketURL;
var deviceId;
var deviceType;
var isBatchModeOn = false;
var wsToken;
var geoPublicUri;
var initLoading = true;

function processPointMessage(geoJsonFeature) {
    if (geoJsonFeature.id in currentSpatialObjects) {
        var excitingObject = currentSpatialObjects[geoJsonFeature.id];
        excitingObject.update(geoJsonFeature);
    }
    else {
        var receivedObject = new SpatialObject(geoJsonFeature);
        receivedObject.update(geoJsonFeature);
        currentSpatialObjects[receivedObject.id] = receivedObject;
        currentSpatialObjects[receivedObject.id].addTo(map);
    }
}

window.onbeforeunload = function () {
    disconnect();
};

function initializeSpatialStreamWebSocket() {
    spatialWebsocket = new WebSocket(webSocketURL);
    spatialWebsocket.onopen = webSocketSpatialOnOpen;
    spatialWebsocket.onmessage = webSocketSpatialOnMessage;
    spatialWebsocket.onclose = webSocketSpatialOnClose;
    spatialWebsocket.onerror = webSocketSpatialOnError;
}

function initializeOnAlertWebSocket() {
    onAlertWebsocket = new WebSocket(alertWebSocketURL);
    onAlertWebsocket.onmessage = webSocketOnAlertMessage;
    onAlertWebsocket.onclose = webSocketOnAlertClose;
    onAlertWebsocket.onerror = webSocketOnAlertError;
    onAlertWebsocket.onopen = webSocketOnAlertOpen;
}

function initializeGeoLocation(geoFencingEnabled) {
    var geoCharts = $("#geo-charts");
    var wsEndPoint = geoCharts.data("ws-endpoint");
    wsToken = geoCharts.data("ws-token");
    geoPublicUri = geoCharts.data("geo-public-uri");
    geoPublicUri = geoCharts.data("geo-public-uri");
    webSocketURL = wsEndPoint + "iot.per.device.stream.geo.FusedSpatialEvent/1.0.0?" + "&websocketToken=" + wsToken;
    alertWebSocketURL = wsEndPoint + "iot.per.device.stream.geo.AlertNotifications/1.0.0?" + "&websocketToken=" + wsToken;
    $("#proximity_alert").hide();

    if (geoFencingEnabled) {
        disconnect();
        initializeSpatialStreamWebSocket();
        initializeOnAlertWebSocket();
    }
    initialLoad(geoFencingEnabled);
}

function disconnect(){
    if (spatialWebsocket && spatialWebsocket.readyState == spatialWebsocket.OPEN){
        spatialWebsocket.close();
    }

    if (onAlertWebsocket && onAlertWebsocket.readyState == onAlertWebsocket.OPEN){
        onAlertWebsocket.close();
    }
}

function popupDateRange() {
    $('#dateRangePopup').attr('title', 'Device ID - ' + deviceId + " Device Type - " + deviceType).dialog();
}

var headings = ["North", "NorthEast", "East", "SouthEast", "South", "SouthWest", "West", "NorthWest"];

function angleToHeading(angle) {
    var angle = (angle + 360 + 22.5 ) % 360;
    angle = Math.floor(angle / 45);
    return headings[angle];
}

function processTrafficMessage(json) {

    if (json.id in currentSpatialObjects) {
        var existingObject = currentSpatialObjects[json.id];
        existingObject.update(json);
    }
    else {
        var receivedObject = new GeoAreaObject(json);
        currentSpatialObjects[json.id] = receivedObject;
        currentSpatialObjects[json.id].addTo(map);
    }
}

function processAlertMessage(json) {
    if (json.state != "NORMAL" && json.state != "MINIMAL") {
        notifyAlert("Object ID: <span style='color: blue;cursor: pointer' onclick='focusOnSpatialObject(" + json.id + ")'>" + json.id + "</span> change state to: <span style='color: red'>" + json.state + "</span> Info : " + json.information);
    }
}

function processPredictionMessage(json) {
    setPropertySafe(currentPredictions, json.day, json.hour, json.longitude, json.latitude, json.traffic - 1);
}

WebSocket.prototype.set_opened = function () {
    this._opened = true;
};

WebSocket.prototype.get_opened = function () {
    return this._opened || false;
};

function GeoAreaObject(json) {
    this.id = json.id;
    this.type = "area";

    var myStyle = {
        "color": "#000001",
        "weight": 5,
        "opacity": 0,
        "fillOpacity": 0.75
    };

    switch (json.properties.state) {
        case "Moderate":
            myStyle["color"] = "#ffb13b";
            break;
        case "Severe":
            myStyle["color"] = "#ff3f3f";
            break;
        case "Minimal":
            return null;
    }

    this.geoJson = L.geoJson(json, {style: myStyle});
    this.marker = this.geoJson.getLayers()[0];
    this.marker.options.title = this.id;
    this.popupTemplate = $('#areaPopup');
    this.popupTemplate.find('#objectId').html(this.id);
    this.popupTemplate.find('#severity').html(json.properties.state);
    this.popupTemplate.find('#information').html(json.properties.information);
    this.marker.bindPopup(this.popupTemplate.html());
    return this;
}

GeoAreaObject.prototype.addTo = function (map) {
    this.geoJson.addTo(map);
};

GeoAreaObject.prototype.focusOn = function (map) {
    map.fitBounds(this.geoJson);
};

GeoAreaObject.prototype.removeFromMap = function () {
    map.removeLayer(this.geoJson);
};

GeoAreaObject.prototype.update = function (geoJSON) {

    this.information = geoJSON.properties.information;
    this.type = geoJSON.properties.type;

    // Update the spatial object leaflet marker
    this.marker.setLatLng([this.latitude, this.longitude]);
    this.marker.setIconAngle(this.heading);
    this.marker.setIcon(this.stateIcon());

    // TODO: use general popup DOM
    this.popupTemplate.find('#objectId').html(this.id);
    this.popupTemplate.find('#information').html(this.information);

    this.marker.setPopupContent(this.popupTemplate.html())
};

function notifyAlert(message) {
    noty({text: "Alert: " + message, type: 'warning'});
}

function Alert(type, message, level) {
    this.type = type;
    this.message = message;
    if (level)
        this.level = level;
    else
        this.level = 'information';

    this.notify = function () {
        noty({text: this.type + ' ' + this.message, type: level});
    }
}

var webSocketOnAlertOpen = function () {
    $('#ws-alert-stream').removeClass('text-muted text-danger text-success').addClass('text-success');
};

var webSocketOnAlertMessage = function processMessage(message) {
    if (!isBatchModeOn) {
        var json = $.parseJSON(message.data);
        if (json.messageType == "Alert") {
            processAlertMessage(json);
        }else {
            console.log("Message type not supported.");
        }
    }
};

var webSocketOnAlertClose = function (e) {
};

var webSocketOnAlertError = function (e) {
    var wsURL = alertWebSocketURL;
    wsURL = wsURL.replace("wss://","https://");
    var uriParts = wsURL.split("/");
    wsURL = uriParts[0] + "//" + uriParts[2];
    noty({text: 'Something went wrong when trying to connect to <b>' + wsURL + '<b/>', type: 'error'});
};

var webSocketSpatialOnOpen = function () {
    if (initLoading) {
        initLoading = false;
    }
    $('#ws-spatial-stream').removeClass('text-muted text-danger text-success').addClass('text-success');
};

var webSocketSpatialOnMessage = function (message) {
    if (!isBatchModeOn) {
        var json = $.parseJSON(message.data);
        if (json.messageType == "Point") {
            processPointMessage(json);
        } else if (json.messageType == "Prediction") {
            //processPredictionMessage(json);
        }
    }
};

var webSocketSpatialOnClose = function (e) {
};

var webSocketSpatialOnError = function (err) {
    var wsURL = webSocketURL;
    wsURL = wsURL.replace("wss://","https://");
    var uriParts = wsURL.split("/");
    wsURL = uriParts[0] + "//" + uriParts[2];
    noty({text: 'Something went wrong when trying to connect to <b>' + wsURL + '<b/>', type: 'error'});
};
