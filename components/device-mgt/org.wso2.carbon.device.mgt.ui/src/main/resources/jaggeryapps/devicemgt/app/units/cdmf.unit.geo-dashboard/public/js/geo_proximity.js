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

var centerLocation = new L.LatLng(6.999833130825296, 79.99855044297874);
var resizeIconLocation = new L.LatLng(6.99847, 80.14412);
var proximityMap = L.map("proximityMap", {
        zoom: 10,
        center: centerLocation,
        zoomControl: false,
        attributionControl: false,
        maxZoom: 20
    });
var proximityDistance = $("#proximityDistance");
//TODO invoker-util
var serverUrl = "/api/device-mgt/v1.0/geo-services/alerts/Proximity/" + deviceType + "/" + deviceId;
// var serverUrl = "/portal/store/carbon.super/fs/gadget/geo-dashboard/controllers/get_alerts.jag?executionPlanType=Proximity&deviceId=" + deviceId;
invokerUtil.get(serverUrl, function (response) {
    response = JSON.parse(response);
    proximityDistance.val(response.proximityDistance);
    $("#proximityTime").val(response.proximityTime);
});

L.grid({
    redraw: 'move'
    }).addTo(proximityMap);

proximityMap.scrollWheelZoom.disable();

var marker = L.marker(centerLocation).setIcon(normalMovingIcon);

marker.addTo(proximityMap);

var resizeIcon = L.icon({
    iconUrl: ApplicationOptions.leaflet.iconUrls.resizeIcon,
    iconAnchor: [24, 24]
});

var resizeMarker = L.marker(resizeIconLocation, {icon: resizeIcon, draggable: 'true'}).addTo(proximityMap);
resizeMarker.on('drag', updateRuler);

var measureLine = new L.Polyline(
    [centerLocation, resizeIconLocation ],
    { color: "black", opacity: 0.5, stroke: true });


proximityMap.addLayer(measureLine);
measureLine._path.setAttribute("class", 'measuring-line-for-look');

var options = {
    minWidth: 50,
    autoPan: false,
    closeButton: true, // should the popups have a close option?
    displayTotalDistance: true,
    displayPartialDistance: false,
    className: 'measuring-label-tooltip' /*css label class name*/
};
//                var totalDistancePopup = new L.Popup(options,measureLine);
var initialDistance = centerLocation.distanceTo(resizeIconLocation);

var measureCircle = L.circle(centerLocation, initialDistance).addTo(proximityMap);

function updateRuler(e) {
    var target = e.target;
    resizeIconLocation = target.getLatLng();
    measureLine.setLatLngs([centerLocation, resizeIconLocation]);
    setDistancePopup(centerLocation, resizeIconLocation)
}

function setDistancePopup(startLatLng, endLatLng) {
    var centerPos = new L.LatLng((startLatLng.lat + endLatLng.lat) / 2,
    (startLatLng.lng + endLatLng.lng) / 2),
    distance = startLatLng.distanceTo(endLatLng);
    proximityDistance.val(distance.toFixed(2));
    measureCircle.setRadius(distance);
}
