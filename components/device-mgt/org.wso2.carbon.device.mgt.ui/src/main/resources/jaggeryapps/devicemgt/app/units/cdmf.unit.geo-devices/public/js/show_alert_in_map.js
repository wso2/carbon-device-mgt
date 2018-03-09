/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function showAlertInMap(alertData) {
    clearFocus();
    var id = $(alertData).attr("data-id");
    var latitude = $(alertData).attr("data-latitude");
    var longitude = $(alertData).attr("data-longitude");
    var state = $(alertData).attr("data-state");
    var information = $(alertData).attr("data-information");

    console.log(information);

    var alertLatLngPoint = L.latLng(latitude,longitude);

    var alertOccouredArea = L.circle(alertLatLngPoint, 10, {
        color: '#FF9900',
        fillColor: '#FF00FF',
        fillOpacity: 0.5
    }).addTo(map);

    alertOccouredArea.bindPopup("Id: <b>"+id+"</b><br>"+
            "State: <b>"+state+"</b><br>"+
            "Information: <b>"+information+"</b><br>"
    ).openPopup();
    $(alertOccouredArea._popup._closeButton).on("click",function(){map.removeLayer(alertOccouredArea)});
    map.setView(alertLatLngPoint,18);

    /* TODO: for reference <Update lib or remove if not in use>: This `R`(RaphaelLayer: https://github.com/dynmeth/RaphaelLayer) library is dam buggy can't use it reliably */
    /*
    var alertPulse = new R.Pulse(
     alertLatLngPoint,
     8,
     {'stroke': '#FF9E0E', 'fill': '#FF0000'},
     {'stroke': '#FF3E2F', 'stroke-width': 3});
     map.addLayer(alertPulse);
     */


}
