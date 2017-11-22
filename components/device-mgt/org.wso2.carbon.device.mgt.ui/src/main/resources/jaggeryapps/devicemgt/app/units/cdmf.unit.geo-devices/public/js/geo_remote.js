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

/* All the remote calls to backend server*/

/* close all opened modals and side pane */
function addTileUrl() {

    var tileUrl = $('#tileUrl').val();
    var urlName = $('#tileName').val();
    var maxzoom = $('#maxzoom').val();
    subdomains = $('#sub_domains').val();
    var attribution = $('#data_attribution').val();

    /* Add to base layers*/
    var newTileLayer = L.tileLayer(tileUrl, {
        maxZoom: parseInt(maxzoom),
        attribution: attribution
    });
    layerControl.addBaseLayer(newTileLayer, urlName);

    inputs = layerControl._form.getElementsByTagName('input');
    inputsLen = inputs.length;
    for (i = 0; i < inputsLen; i++) {
        input = inputs[i];
        obj = layerControl._layers[input.layerId];
        if (layerControl._map.hasLayer(obj.layer)) {
            map.removeLayer(obj.layer);
        }
    }
    map.addLayer(newTileLayer);

    /* Do ajax save */
    var data = {
        url: tileUrl,
        'name': urlName,
        'attribution': attribution,
        'maxzoom': maxzoom,
        'subdomains': subdomains
    };
    var serverUrl = "/portal/store/carbon.super/fs/gadget/geo-dashboard/controllers/tile_servers.jag";
    // TODO: If failure happens notify user about the error message
    $.post(serverUrl, data, function (response) {
        noty({text: '<span style="color: dodgerblue">' + response + '</span>', type: 'success' });
        closeAll();
    });
}

var defaultOSM = L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19
});

var baseLayers = {
    "Open Street Maps": defaultOSM
};

function getTileServers() {
    /*var backendApiUrl = $("#arduino-div-chart").data("backend-api-url") + "?from=" + from + "&to=" + to;
     invokerUtil.get(backendApiUrl, successCallback, function (message) {

     });*/
    $.getJSON("/api/controllers/tile_servers?serverId=all", function (data) {
        console.log(JSON.stringify(data));
        $.each(data, function (key, val) {
            noty({text: 'Loading... <span style="color: #ccfcff">' + val.NAME + '</span>', type: 'info'});
            //baseLayers[val.name]
            var newTileLayer = L.tileLayer(
                val.URL, {
                    maxZoom: val.MAXZOOM, // TODO: if no maxzoom level do not set this attribute
                    attribution: val.ATTRIBUTION
                }
            );
            layerControl.addBaseLayer(newTileLayer, val.NAME); // TODO: implement single method for #20  and this and do validation
            //map.addLayer(newTileLayer);
        });
    });
}




// TODO:this is not a remote call , move this to application.js
function closeAll() {
    $('.modal').modal('hide');
    setTimeout(function () {
        $.noty.closeAll();
    }, 100);
}

