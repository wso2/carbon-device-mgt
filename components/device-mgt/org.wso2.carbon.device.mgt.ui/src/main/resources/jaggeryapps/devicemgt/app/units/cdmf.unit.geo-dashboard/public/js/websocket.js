/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function SpatialObject(json) {
    this.id = json.id;
    this.type = json.properties.type;

    // Have to store the coordinates , to use when user wants to draw path
    this.pathGeoJsons = []; // GeoJson standard MultiLineString(http://geojson.org/geojson-spec.html#id6) can't use here because this is a collection of paths(including property attributes)
    this.path = []; // Path is an array of sections, where each section is a notified state of the path


    this.speedHistory = new LocalStorageArray(this.id);
    this.geoJson = L.geoJson(json, {
        pointToLayer: function (feature, latlng) {
            return L.marker(latlng, {icon: normalMovingIcon, iconAngle: this.heading});
        }
    }); // Create Leaflet GeoJson object

    this.marker = this.geoJson.getLayers()[0];
    this.marker.options.title = this.id;

    if ("stationary" != this.type.toLowerCase() || "stop" != this.type.toLowerCase()) {
        this.popupTemplate = $('#markerPopup');
    } else {
        this.popupTemplate = $('#markerPopupStop');
    }

    this.marker.bindPopup(this.popupTemplate.html());
    return this;
}

function popupDateRange() {
    $('#dateRangePopup').attr('title', 'Device ID - ' + deviceId + " Device Type - " + deviceType).dialog();
}

SpatialObject.prototype.update = function (geoJSON) {
    this.latitude = geoJSON.geometry.coordinates[1];
    this.longitude = geoJSON.geometry.coordinates[0];
    this.setSpeed(geoJSON.properties.speed);
    this.state = geoJSON.properties.state;
    this.heading = geoJSON.properties.heading;

    this.information = geoJSON.properties.information;
    this.type = geoJSON.properties.type;

    if (this.type == "STOP") {
        this.information = "Bus Stop";
    }

    if (geoJSON.properties.notify) {
        /*
         //This is implemented in alertWebSocket
         if (this.state != "NORMAL") {
         notifyAlert("Object ID: <span style='color: blue;cursor: pointer' onclick='focusOnSpatialObject(" + this.id + ")'>" + this.id + "</span> change state to: <span style='color: red'>" + geoJSON.properties.state + "</span> Info : " + this.information);
         }*/
        var newLineStringGeoJson = this.createLineStringFeature(this.state, this.information, [this.latitude, this.longitude]);
        this.pathGeoJsons.push(newLineStringGeoJson);

        // only add the new path section to map if the spatial object is selected
        if (selectedSpatialObject == this.id) {
            var newPathSection = new L.polyline(newLineStringGeoJson.geometry.coordinates, this.getSectionStyles(geoJSON.properties.state));
            newPathSection.bindPopup("Alert Information: " + newLineStringGeoJson.properties.information);

            // Creating two sections joint // TODO : line color confusing , use diffrent color or seperator
            var lastSection = this.path[this.path.length - 1].getLatLngs();
            var joinLine = [lastSection[lastSection.length - 1], [this.latitude, this.longitude]];
            var sectionJoin = new L.polyline(joinLine, this.getSectionStyles());
            sectionJoin.setStyle({className: "sectionJointStyle"});// Make doted line for section join , this class is currently defined in map.jag as a inner css

            this.path.push(sectionJoin);
            this.path.push(newPathSection); // Order of the push matters , last polyLine object should be the `newPathSection` not the `sectionJoin`

            sectionJoin.addTo(map);
            newPathSection.addTo(map);
        }
    }

    // Update the spatial object leaflet marker
    this.marker.setLatLng([this.latitude, this.longitude]);

    if (this.latitude, this.longitude) {
        map.setView([this.latitude, this.longitude]);
    }
    this.marker.setIconAngle(this.heading);
    this.marker.setIcon(this.stateIcon());

    if (this.pathGeoJsons.length > 0) {
        // To prevent conflicts in
        // Leaflet(http://leafletjs.com/reference.html#latlng) and geoJson standards(http://geojson.org/geojson-spec.html#id2),
        // have to do this swapping, but the resulting geoJson in not upto geoJson standards
        // TODO: write func to swap coordinates
        this.pathGeoJsons[this.pathGeoJsons.length - 1].geometry.coordinates.push([geoJSON.geometry.coordinates[1], geoJSON.geometry.coordinates[0]]);
    }
    else {
        newLineStringGeoJson = this.createLineStringFeature(this.state, this.information, [geoJSON.geometry.coordinates[1], geoJSON.geometry.coordinates[0]]);
        this.pathGeoJsons.push(newLineStringGeoJson);
    }

    if (selectedSpatialObject == this.id) {
        this.updatePath([geoJSON.geometry.coordinates[1], geoJSON.geometry.coordinates[0]]);
        if (speedGraphControl) {
            chart.load({columns: [this.speedHistory.getArray()]});
        }
        map.setView([this.latitude, this.longitude]);
    }

    // TODO: use general popup DOM
    this.popupTemplate.find('#objectId').html(this.id);
    this.popupTemplate.find('#information').html(this.information);

    this.popupTemplate.find('#speed').html(Math.round(this.speed * 10) / 10);
    this.popupTemplate.find('#heading').html(angleToHeading(this.heading));
    this.marker.setPopupContent(this.popupTemplate.html())
};

var headings = ["North", "NorthEast", "East", "SouthEast", "South", "SouthWest", "West", "NorthWest"];

function angleToHeading(angle) {
    var angle = (angle + 360 + 22.5 ) % 360;
    angle = Math.floor(angle / 45);
    return headings[angle];
}

SpatialObject.prototype.removeFromMap = function () {
    this.removePath();
    this.marker.closePopup();
    map.removeLayer(this.marker);
};

function clearMap() {
    for (var spacialObject in currentSpatialObjects) {
        console.log(spacialObject);
        currentSpatialObjects[spacialObject].removePath();
        currentSpatialObjects[spacialObject].removeFromMap();
    }
    currentSpatialObjects = {};
}

SpatialObject.prototype.createLineStringFeature = function (state, information, coordinates) {
    return {
        "type": "Feature",
        "properties": {
            "state": state,
            "information": information
        },
        "geometry": {
            "type": "LineString",
            "coordinates": [coordinates]
        }
    };
};

SpatialObject.prototype.setSpeed = function (speed) {
    this.speed = speed;
    this.speedHistory.push(speed);
//    console.log("DEBUG: this.speedHistory.length = "+this.speedHistory.length+" ApplicationOptions.constance.SPEED_HISTORY_COUNT = "+ApplicationOptions.constance.SPEED_HISTORY_COUNT);
    if (this.speedHistory.length > ApplicationOptions.constance.SPEED_HISTORY_COUNT) {
        this.speedHistory.splice(1, 1);
    }
};

SpatialObject.prototype.addTo = function (map) {
    this.geoJson.addTo(map);
};

SpatialObject.prototype.updatePath = function (LatLng) {
    this.path[this.path.length - 1].addLatLng(LatLng); // add LatLng to last section
};

SpatialObject.prototype.drawPath = function () {
    var previousSectionLastPoint = []; // re init all the time when calls the function
    if (this.path.length > 0) {
        this.removePath();
//            throw "geoDashboard error: path already exist,remove current path before drawing a new path, if need to update LatLngs use setLatLngs method instead"; // Path already exist
    }
    for (var lineString in this.pathGeoJsons) {
        if (!this.pathGeoJsons.hasOwnProperty(lineString)) {
            continue
        }
        var currentSectionState = this.pathGeoJsons[lineString].properties.state;
        var currentSection = new L.polyline(this.pathGeoJsons[lineString].geometry.coordinates, this.getSectionStyles(currentSectionState)); // Create path object when and only drawing the path (save memory) TODO: if need directly draw line from geojson

        var currentSectionFirstPoint = this.pathGeoJsons[lineString].geometry.coordinates[0];
        console.log("DEBUG: previousSectionLastPoint = " + previousSectionLastPoint + " currentSectionFirstPoint = " + currentSectionFirstPoint);
        previousSectionLastPoint.push(currentSectionFirstPoint);
        var sectionJoin = new L.polyline(previousSectionLastPoint, this.getSectionStyles());
        sectionJoin.setStyle({className: "sectionJointStyle"});// Make doted line for section join , this class is currently defined in map.jag as a inner css

        previousSectionLastPoint = [this.pathGeoJsons[lineString].geometry.coordinates[this.pathGeoJsons[lineString].geometry.coordinates.length - 1]];
        sectionJoin.addTo(map);
        this.path.push(sectionJoin);
        console.log("DEBUG: Alert Information: " + this.pathGeoJsons[lineString].properties.information);
        currentSection.bindPopup("Alert Information: " + this.pathGeoJsons[lineString].properties.information);
        currentSection.addTo(map);
        this.path.push(currentSection);
    }
};

SpatialObject.prototype.removePath = function () {
    for (var section in this.path) {
        if (this.path.hasOwnProperty(section)) {
            map.removeLayer(this.path[section]);
        }
    }
    this.path = []; // Clear the path layer (save memory)
};

SpatialObject.prototype.getSectionStyles = function (state) {
    // TODO:<done> use option object to assign hardcode values
    var pathColor;
    switch (state) {
        case "NORMAL":
            pathColor = ApplicationOptions.colors.states.NORMAL; // Scope of function
            break;
        case "ALERTED":
            pathColor = ApplicationOptions.colors.states.ALERTED;
            break;
        case "WARNING":
            pathColor = ApplicationOptions.colors.states.WARNING;
            break;
        case "OFFLINE":
            pathColor = ApplicationOptions.colors.states.OFFLINE;
            break;
        default: // TODO: set path var
            return {color: ApplicationOptions.colors.states.UNKNOWN, weight: 8};
    }
    return {color: pathColor, weight: 8};
};

function processTrafficMessage(json) {

    if (json.id in currentSpatialObjects) {
        var existingObject = currentSpatialObjects[json.id];
        existingObject.update(json);
        console.log("existing area");
    }
    else {
        var receivedObject = new GeoAreaObject(json);
        currentSpatialObjects[json.id] = receivedObject;
        currentSpatialObjects[json.id].addTo(map);
    }
}

function processAlertMessage(json) {
    if (json.state != "NORMAL" && json.state != "MINIMAL") {
        console.log(json);
        notifyAlert("Object ID: <span style='color: blue;cursor: pointer' onclick='focusOnSpatialObject(" + json.id + ")'>" + json.id + "</span> change state to: <span style='color: red'>" + json.state + "</span> Info : " + json.information);
    }
}

/*function setPropertySafe(obj)
 {
 function isObject(o)
 {
 if (o === null) return false;
 var type = typeof o;
 return type === 'object' || type === 'function';
 }

 if (!isObject(obj)) return;

 var prop;
 for (var i=1; i < arguments.length-1; i++)
 {
 prop = arguments[i];
 if (!isObject(obj[prop])) obj[prop] = {};
 if (i < arguments.length-2) obj = obj[prop];
 }

 obj[prop] = arguments[i];
 }*/

function processPredictionMessage(json) {
    setPropertySafe(currentPredictions, json.day, json.hour, json.longitude, json.latitude, json.traffic - 1);
    //console.log(json);
}

WebSocket.prototype.set_opened = function () {
    this._opened = true;
};

WebSocket.prototype.get_opened = function () {
    return this._opened || false;
};


/*
 var _longitudeStart = -0.0925
 var _latitudeStart = 51.4985
 var _unit = 0.005;

 function requestPredictions(longitude, latitude, d) {

 var serverUrl = "http://localhost:9763/endpoints/GpsDataOverHttp/predictionInput";
 function loop(i) {
 setTimeout(function() {
 var data = {
 day : d.getUTCDate() - 3,
 hour : d.getUTCHours() + i + 1,
 latitude : Math.round((latitude - _latitudeStart)/_unit),
 longitude : Math.round((longitude - _longitudeStart)/_unit)
 };
 var json = JSON.stringify(data);
 $.ajax({
 url: serverUrl,
 type: "POST",
 data: json,
 contentType: "application/json; charset=UTF-8"
 });
 if(i<6) {
 loop(i+1);
 }
 },500);
 }
 loop(0);
 }
 var d= new Date();
 //requestPredictions(-0.09,51.5,d);

 function getPredictions(longitude, latitude, d) {
 var longitude = Math.round((longitude - _longitudeStart)/_unit);
 var latitude = Math.round((latitude - _latitudeStart)/_unit);
 var traffic = [['x',0,0,0,0,0,0],['data',0,0,0,0,0,0]];
 var hour = d.getUTCHours();
 var day = d.getUTCDate() - 3;
 for (var i = 0; i < 6; i++) {
 hour = hour + 1;
 if (hour > 23) {
 hour = hour - 24;
 day = day + 1;
 }
 try{
 traffic[0][i+1] = hour;
 traffic[1][i+1] = currentPredictions[day][hour][longitude][latitude];
 } catch(e) {
 console.log(i);
 }
 }
 return traffic;
 }*/

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

    console.log("update called");
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

function LocalStorageArray(id) {
    if (typeof (sessionStorage) === 'undefined') {
        // Sorry! No Web Storage support..
        return ['speed']; // TODO: fetch this array from backend DB rather than keeping as in-memory array
    }
    if (id === undefined) {
        throw 'Should provide an id to create a local storage!';
    }
    var DELIMITER = ','; // Private variable delimiter
    this.storageId = id;
    sessionStorage.setItem(id, 'speed'); // TODO: <note> even tho we use `sessionStorage` because of this line previous it get overwritten in each page refresh
    this.getArray = function () {
        return sessionStorage.getItem(this.storageId).split(DELIMITER);
    };

    this.length = this.getArray().length;

    this.push = function (value) {
        var currentStorageValue = sessionStorage.getItem(this.storageId);
        var updatedStorageValue;
        if (currentStorageValue === null) {
            updatedStorageValue = value;
        } else {
            updatedStorageValue = currentStorageValue + DELIMITER + value;
        }
        sessionStorage.setItem(this.storageId, updatedStorageValue);
        this.length += 1;
    };
    this.isEmpty = function () {
        return (this.getArray().length === 0);
    };
    this.splice = function (index, howmany) {
        var currentArray = this.getArray();
        currentArray.splice(index, howmany);
        var updatedStorageValue = currentArray.toString();
        sessionStorage.setItem(this.storageId, updatedStorageValue);
        this.length -= howmany;
        // TODO: should return spliced section as array
    };
}

var initLoading = true;

var webSocketOnAlertOpen = function () {
    onAlertWebsocket.set_opened();
    $('#ws-alert-stream').removeClass('text-muted text-danger text-success').addClass('text-success');
};

var webSocketOnAlertMessage = function processMessage(message) {
    if (!isBatchModeOn) {
        var json = $.parseJSON(message.data);
        if (json.messageType == "Alert") {
            processAlertMessage(json);
        } else {
            console.log("Message type not supported.");
        }
    }
};

var webSocketOnAlertClose = function (e) {
    if (onAlertWebsocket.get_opened()) {
        $('#ws-alert-stream').removeClass('text-muted text-danger text-success').addClass('text-danger');
    }
    waitForSocketConnection(onAlertWebsocket, initializeOnAlertWebSocket);
};

var webSocketOnAlertError = function (e) {
    if (!onAlertWebsocket.get_opened()) return;
    noty({text: 'Something went wrong when trying to connect to <b>' + alertWebSocketURL + '<b/>', type: 'error'});
};

var webSocketSpatialOnOpen = function () {
    spatialWebsocket.set_opened();
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
        } else {
            console.log("Message type not supported.");
        }
    }
};

var webSocketSpatialOnClose = function (e) {
    if (spatialWebsocket.get_opened()) {
        $('#ws-spatial-stream').removeClass('text-muted text-danger text-success').addClass('text-danger');
    }
    waitForSocketConnection(spatialWebsocket, initializeSpatialStreamWebSocket);
};

var webSocketSpatialOnError = function (err) {
    if (!spatialWebsocket.get_opened()) return;
    noty({text: 'Something went wrong when trying to connect to <b>' + webSocketURL + '<b/>', type: 'error'});
};


var waitTime = 1000;
var waitQueue = {};
function waitForSocketConnection(socket, callback) {
    if(waitQueue[socket.url]) return;
    setTimeout(
        function () {
            if (socket.readyState === 1) {
                //connectToSource();
                console.log("Connection is made");
                if (callback != null) {
                    callback();
                }
                return;
            } else {
                ws = new WebSocket(socket.url);
                if(socket)socket.close();
                waitTime += 400;
                waitForSocketConnection(ws, callback);
            }
        }, waitTime); // wait 5 milisecond for the connection...
    waitQueue[socket.url] = true;
}


function initializeSpatialStreamWebSocket() {
    if(spatialWebsocket) spatialWebsocket.close();
    spatialWebsocket = new WebSocket(webSocketURL);
    spatialWebsocket.onopen = webSocketSpatialOnOpen;
    spatialWebsocket.onmessage = webSocketSpatialOnMessage;
    spatialWebsocket.onclose = webSocketSpatialOnClose;
    spatialWebsocket.onerror = webSocketSpatialOnError;
}

function initializeOnAlertWebSocket() {
    if(onAlertWebsocket) onAlertWebsocket.close();
    onAlertWebsocket = new WebSocket(alertWebSocketURL);
    onAlertWebsocket.onmessage = webSocketOnAlertMessage;
    onAlertWebsocket.onclose = webSocketOnAlertClose;
    onAlertWebsocket.onerror = webSocketOnAlertError;
    onAlertWebsocket.onopen = webSocketOnAlertOpen;
}

function initializeGeoLocation(geoFencingEnabled) {
    var deviceDetails = $(".device-id");
    deviceId = deviceDetails.data("deviceid");
    deviceType = deviceDetails.data("type");
    var loggedInUser  = $("#logged-in-user");
    var username = loggedInUser.data("username");
    var userDomain = loggedInUser.data("domain");
    if (deviceId && deviceType) {
        var geoCharts = $("#geo-charts");
        var wsEndPoint = geoCharts.data("ws-endpoint");
        wsToken = geoCharts.data("ws-token");
        geoPublicUri = geoCharts.data("geo-public-uri");
        webSocketURL = wsEndPoint + userDomain + "/iot.per.device.stream.geo.FusedSpatialEvent/1.0.0?"
                       + "deviceId=" + deviceId + "&deviceType=" + deviceType + "&websocketToken=" + wsToken;
        alertWebSocketURL = wsEndPoint + userDomain + "/org.wso2.geo.AlertsNotifications/1.0.0?"
                            + "deviceId=" + deviceId + "&deviceType=" + deviceType + "&websocketToken=" + wsToken;
        $("#proximity_alert").hide();

        initialLoad();
        InitSpatialObject(geoFencingEnabled);

        if (geoFencingEnabled) {
            initializeSpatialStreamWebSocket();
            initializeOnAlertWebSocket();
            window.onbeforeunload = function () {
                spatialWebsocket.close();
                onAlertWebsocket.close();
            }
        }
    } else {
        noty({text: 'Invalid Access! No device information provided to track!', type: 'error'});
    }
}

SpatialObject.prototype.stateIcon = function () {
    //TODO : Need to add separate icons for each device type
    var iconUrl = geoPublicUri + "/img/markers/object-types/default";
    if (0 < this.speed && (-360 <= this.heading && 360 >= this.heading)) {
        iconUrl = iconUrl + "/moving/" + this.state.toLowerCase();
    } else {
        iconUrl = iconUrl + "/non-moving/" + this.state.toLowerCase();
    }

    return L.icon({
                      iconUrl: iconUrl + ".png",
                      shadowUrl: false,
                      iconSize: [24, 24],
                      iconAnchor: [+12, +12],
                      popupAnchor: [-2, -5]
                  });
};


var normalMovingIcon = L.icon({
                                  iconUrl: ApplicationOptions.leaflet.iconUrls.normalMovingIcon,
                                  shadowUrl: false,
                                  iconSize: [24, 24],
                                  iconAnchor: [+12, +12],
                                  popupAnchor: [-2, -5]
                              });