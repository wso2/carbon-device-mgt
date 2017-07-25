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

function addWmsEndPoint() {

    serviceName = $('#serviceName').val();
    layers = $('#layers').val();
    wmsVersion = $('#wmsVersion').val();
    serviceEndPoint = $('#serviceEndPoint').val();
    outputFormat = $('#outputFormat').val();

    var validated = false;

    if (serviceName === undefined || serviceName == "" || serviceName == null) {
        var message = "Service Provider name cannot be empty.";
        noty({text: '<span style="color: red">' + message + '</span>', type: 'error'});
    }

    /* if(layers === undefined || layers == "" || layers == null){
     layers = "";
     }



     if(wmsVersion === undefined || wmsVersion == "" || wmsVersion == null){
     wmsVersion = "";
     }


     if(outputFormat === undefined || outputFormat == "" || outputFormat == null){
     outputFormat = "image/png";
     }*/

    if (validated) {
        wmsLayer = L.tileLayer.wms(serviceEndPoint, {
            layers: layers.split(','),
            format: outputFormat ? outputFormat : 'image/png',
            version: wmsVersion,
            transparent: true,
            opacity: 0.4
        });

        layerControl.addOverlay(wmsLayer, serviceName, "Web Map Service layers");
        map.addLayer(wmsLayer);

        /*var temperature = L.tileLayer.wms('http://gis.srh.noaa.gov/arcgis/services/NDFDTemps/MapServer/WMSServer', {
         format: 'img/png',
         transparent: true,
         layers: 16
         });

         layerControl.addOverlay(temperature, "testWms", "Web Map Service layers");
         map.addLayer(temperature);*/

        var data = {
            'serviceName': serviceName,
            'layers': layers,
            'wmsVersion': wmsVersion,
            'serviceEndPoint': serviceEndPoint,
            'outputFormat': outputFormat
        };
        var serverUrl = "/api/controllers/wms_endpoints";
        // TODO: If failure happens notify user about the error message
        $.post(serverUrl, data, function (response) {
            console.log("------->><wms_endpoints>"+ response);
            noty({text: '<span style="color: dodgerblue">' + response + '</span>',type: 'success'});
            closeAll();
        });
    }


}

function loadWms() {
    // For refference {"wmsServerId" : 1, "serviceUrl" : "http://{s}.somedomain.com/blabla/{z}/{x}/{y}.png", "name" : "Sample server URL", "layers" : "asdsad,sd,adasd,asd", "version" : "1.0.2", "format" : "sadasda/asdas"}
    $.getJSON("/api/controllers/wms_endpoints?serverId=all", function (data) {
        $.each(data, function (key, val) {

            wmsLayer = L.tileLayer.wms(val.SERVICEURL, {
                layers: val.LAYERS.split(','),
                format: val.FORMAT ? val.FORMAT : 'image/png',
                version: val.VERSION,
                transparent: true,
                opacity: 0.4
            });
            layerControl.addOverlay(wmsLayer, val.NAME, "Web Map Service layers");
        });
    });
}

function setSpeedAlert() {
    //TODO: get the device Id from the URL
    var speedAlertValue = $("#speedAlertValue").val();

    if (speedAlertValue == null || speedAlertValue === undefined || speedAlertValue == "") {
        var message = "Speed cannot be empty.";
        noty({text:  message, type : 'error' });
    } else {
        data = {
            'parseData': JSON.stringify({'speedAlertValue': speedAlertValue, 'deviceId': deviceId}), // parseKey : parseValue pair , this key pair is replace with the key in the template file
            'executionPlan': 'Speed',
            'customName': null,
            'cepAction': 'edit',
            'deviceId': deviceId // TODO: what if setting speed alert for the first time ?? that should be a deployment ? try 'edit' if fails 'deploy' , need to handle at the jaggery back end
        };
        var serviceUrl = '/api/device-mgt/v1.0/geo-services/alerts/Speed/' + deviceType + '/' + deviceId;
        var responseHandler = function (data, textStatus, xhr) {
            closeAll();
            if (xhr.status == 200) {
                noty({text: 'Successfully added alert', type : 'success'});
            } else {
                var ptrn = /(?:<am\:description>)(.*)(?:<\/am\:description>)/g;
                var result = (ptrn.exec(data));
                var errorTxt;
                if (result) {
                    errorTxt = result.length > 1 ? result[1] : data;
                } else {
                    errorTxt = data;
                }
                noty({text: textStatus + ' : ' + errorTxt, type : 'error'});
            }
        };
        invokerUtil.put(serviceUrl,
                        data,
                        responseHandler, function (xhr) {
                responseHandler(xhr.responseText, xhr.statusText, xhr);
            });
    }


}
var lastToolLeafletId = null;

function setWithinAlert(leafletId) {
    /*
     * TODO: replace double quote to single quote because of a conflict when deploying execution plan in CEP
     * this is against JSON standards so has been re-replaced when getting the data from governance registry
     * (look in get_alerts for .replace() method)
     * */
    var selectedAreaGeoJson = JSON.stringify(map._layers[leafletId].toGeoJSON().geometry).replace(/"/g, "'");
    var areaName = $("#areaName").val();
    var queryName = areaName;


    if (areaName == null || areaName === undefined || areaName == "") {
        var message = "Area Name cannot be empty.";
        noty({text: message, type : 'error' });
    } else {
        var data = {
            'parseData': JSON.stringify({
                                            'geoFenceGeoJSON': selectedAreaGeoJson,
                                            'executionPlanName': createExecutionPlanName(queryName, "WithIn", deviceId),
                                            'areaName': areaName,
                                            'deviceId' : deviceId
                                        }),
            'executionPlan': 'Within',
            'customName': areaName, // TODO: fix , When template copies there can be two queryName and areaName id elements in the DOM
            'queryName': queryName,
            'cepAction': 'deploy',
            'deviceId' : deviceId
        };

        var serviceUrl = '/api/device-mgt/v1.0/geo-services/alerts/Within/' + deviceType + '/' + deviceId;
        var responseHandler = function (data, textStatus, xhr) {
            closeTools(leafletId);
            if (xhr.status == 200) {
                noty({text: 'Successfully added alert', type : 'success'});
            } else {
                var ptrn = /(?:<am\:description>)(.*)(?:<\/am\:description>)/g;
                var errorTxt;
                if (result) {
                    errorTxt = result.length > 1 ? result[1] : data;
                } else {
                    errorTxt = data;
                }
                noty({text: textStatus + ' : ' + errorTxt, type : 'error'});
            }
        };
        invokerUtil.post(serviceUrl,
                         data,
                         responseHandler, function (xhr) {
                responseHandler(xhr.responseText, xhr.statusText, xhr);
            });
    }
}

function setExitAlert(leafletId) {
    /*
     * TODO: replace double quote to single quote because of a conflict when deploying execution plan in CEP
     * this is against JSON standards so has been re-replaced when getting the data from governance registry
     * (look in get_alerts for .replace() method)
     * */
    var selectedAreaGeoJson = JSON.stringify(map._layers[leafletId].toGeoJSON().geometry).replace(/"/g, "'");
    var areaName = $("#areaName").val();
    var queryName = areaName;


    if (areaName == null || areaName === undefined || areaName == "") {
        var message = "Area Name cannot be empty.";
        noty({text: message, type : 'error' });
    } else {
        var data = {
            'parseData': JSON.stringify({
                                            'geoFenceGeoJSON': selectedAreaGeoJson,
                                            'executionPlanName': createExecutionPlanName(queryName, "Exit", deviceId),
                                            'areaName': areaName,
                                            'deviceId' : deviceId
                                        }),
            'executionPlan': 'Exit',
            'customName': areaName, // TODO: fix , When template copies there can be two queryName and areaName id elements in the DOM
            'queryName': queryName,
            'cepAction': 'deploy',
            'deviceId' : deviceId
        };

        var serviceUrl = '/api/device-mgt/v1.0/geo-services/alerts/Exit/' + deviceType + '/' + deviceId;
        var responseHandler = function (data, textStatus, xhr) {
            closeTools(leafletId);
            if (xhr.status == 200) {
                noty({text: 'Successfully added alert', type : 'success'});
            } else {
                var ptrn = /(?:<am\:description>)(.*)(?:<\/am\:description>)/g;
                var errorTxt;
                if (result) {
                    errorTxt = result.length > 1 ? result[1] : data;
                } else {
                    errorTxt = data;
                }
                noty({text: textStatus + ' : ' + errorTxt, type : 'error'});
            }
        };
        invokerUtil.post(serviceUrl,
                         data,
                         responseHandler, function (xhr) {
                responseHandler(xhr.responseText, xhr.statusText, xhr);
            });
    }
}

function setStationeryAlert(leafletId) {

    var selectedAreaGeoJson = map._layers[leafletId].toGeoJSON().geometry;

    //if a circle is drawn adding radius for the object
    if (selectedAreaGeoJson.type == "Point") {

        var radius = map._layers[leafletId]._mRadius;
        selectedAreaGeoJson["radius"] = radius;
    }

    var selectedProcessedAreaGeoJson = JSON.stringify(selectedAreaGeoJson).replace(/"/g, "'");

    var stationeryName = $("#areaName").val();
    var queryName = stationeryName;
    var fluctuationRadius = $("#fRadius").val();
    var time = $("#time").val();

    if (stationeryName == null || stationeryName === undefined || stationeryName == "") {
        var message = "Stationery Name cannot be empty.";
        noty({text: message, type : 'error' });
    } else if (fluctuationRadius == null || fluctuationRadius === undefined || fluctuationRadius == "") {
        var message = "Fluctuation Radius cannot be empty.";
        noty({text: message, type : 'error' });
    } else if (time == null || time === undefined || time == "") {
        var message = "Time cannot be empty.";
        noty({text: message, type: 'error' });
    } else {
        var data = {
            'parseData': JSON.stringify({
                                            'geoFenceGeoJSON': selectedProcessedAreaGeoJson,
                                            'executionPlanName': createExecutionPlanName(queryName, "Stationery", deviceId),
                                            'stationeryName': stationeryName,
                                            'stationeryTime': time,
                                            'fluctuationRadius': fluctuationRadius
                                        }),
            'stationeryTime': time,
            'fluctuationRadius': fluctuationRadius,
            'executionPlan': 'Stationery',
            'customName': stationeryName, // TODO: fix , When template copies there can be two queryName and areaName id elements in the DOM
            'queryName': queryName,
            'cepAction': 'deploy',
            'deviceId': deviceId
        };
        var serviceUrl = '/api/device-mgt/v1.0/geo-services/alerts/Stationery/' + deviceType + '/' + deviceId;
        var responseHandler = function (data, textStatus, xhr) {
            closeTools(leafletId);
            if (xhr.status == 200) {
                noty({text: 'Successfully added alert', type : 'success'});
            } else {
                var ptrn = /(?:<am\:description>)(.*)(?:<\/am\:description>)/g;
                var errorTxt;
                if (result) {
                    errorTxt = result.length > 1 ? result[1] : data;
                } else {
                    errorTxt = data;
                }
                noty({text: textStatus + ' : ' + errorTxt, type : 'error'});
            }
        };
        invokerUtil.post(serviceUrl,
                         data,
                         responseHandler, function (xhr) {
                responseHandler(xhr.responseText, xhr.statusText, xhr);
            });
    }


}

var toggeled = false;
/*function getPrediction(leafletId) {
 *//*
 * TODO: replace double quote to single quote because of a conflict when deploying execution plan in CEP
 * this is against JSON standards so has been re-replaced when getting the data from governance registry
 * (look in get_alerts for .replace() method)
 * *//*
 console.log("leafletId: " + leafletId);
 var selectedAreaGeoJson = map._layers[leafletId].toGeoJSON().geometry;
 var d = new Date();
 console.log(d);

 var selectedProcessedAreaGeoJson = JSON.stringify(selectedAreaGeoJson).replace(/"/g, "'");

 requestPredictions(selectedAreaGeoJson.coordinates[0], selectedAreaGeoJson.coordinates[1], d);
 if(!toggeled){
 $('#predictionResults').animate({width: 'toggle'}, 100);
 toggeled = true;
 }

 $.UIkit.notify({
 message: "Generating Predictions",
 status: 'warning',
 timeout: 5000,
 pos: 'top-center'
 });

 setTimeout(function() {
 var arr = getPredictions(selectedAreaGeoJson.coordinates[0], selectedAreaGeoJson.coordinates[1], d);
 createPredictionChart();
 console.log(arr[1]);
 predictionChart.load({columns: arr});
 }
 , 5000);



 }*/


function setTrafficAlert(leafletId) {
    /*
     * TODO: replace double quote to single quote because of a conflict when deploying execution plan in CEP
     * this is against JSON standards so has been re-replaced when getting the data from governance registry
     * (look in get_alerts for .replace() method)
     * */
    console.log("leafletId: " + leafletId);
    var selectedAreaGeoJson = map._layers[leafletId].toGeoJSON().geometry;

    //if a circle is drawn adding radius for the object
    if (selectedAreaGeoJson.type == "Point") {

        var radius = map._layers[leafletId]._mRadius;
        selectedAreaGeoJson["radius"] = radius;
    }

    var selectedProcessedAreaGeoJson = JSON.stringify(selectedAreaGeoJson).replace(/"/g, "'");

    var areaName = $("#areaName").val();
    var queryName = areaName;
    //var time = $("#time").val();

    if (areaName == null || areaName === undefined || areaName == "") {
        var message = "Area Name cannot be empty.";
        noty({text: message, type : 'error' });
    } else {
        var data = {
            'parseData': JSON.stringify({
                                            'geoFenceGeoJSON': selectedProcessedAreaGeoJson,
                                            'executionPlanName': createExecutionPlanName(queryName, "Traffic", deviceId),
                                            'areaName': areaName
                                        }),
            'executionPlan': 'Traffic',
            'customName': areaName, // TODO: fix , When template copies there can be two queryName and areaName id elements in the DOM
            'queryName': queryName,
            'cepAction': 'deploy',
            'deviceId': deviceId
        };
        console.log(JSON.stringify(data));
        var serviceUrl = '/api/device-mgt/v1.0/geo-services/alerts/Traffic/' + deviceType + '/' + deviceId;
        var responseHandler = function (data, textStatus, xhr) {
            closeTools(leafletId);
            if (xhr.status == 200) {
                noty({text: 'Successfully added alert', type : 'success'});
            } else {
                var ptrn = /(?:<am\:description>)(.*)(?:<\/am\:description>)/g;
                var errorTxt;
                if (result) {
                    errorTxt = result.length > 1 ? result[1] : data;
                } else {
                    errorTxt = data;
                }
                noty({text: textStatus + ' : ' + errorTxt, type : 'error'});
            }
        };
        invokerUtil.post(serviceUrl,
                         data,
                         responseHandler, function (xhr) {
                responseHandler(xhr.responseText, xhr.statusText, xhr);
            });
    }
}

function removeGeoFence(geoFenceElement, id) {
    var queryName = $(geoFenceElement).attr('data-queryName');
    var areaName = $(geoFenceElement).attr('data-areaName');

    var serviceUrl = '/api/device-mgt/v1.0/geo-services/alerts/' + id + '/' + deviceType + '/' + deviceId + '?queryName='
                     + queryName;
    invokerUtil.delete(serviceUrl, function (response) {
                           noty({
                                    text: 'Successfully removed ' + id + ' alert',
                                    type: 'success'
                                });
                           closeAll();
                       },
                       function (xhr) {
                           noty({
                                    text: 'Could not remove ' + id + ' alert',
                                    type: 'error'
                                })
                       });
}



function getAlertsHistory(deviceType, deviceId, timeFrom, timeTo) {
    var timeRange = '';
    if (timeFrom && timeTo) {
        timeRange = '?from=' + timeFrom + '&to=' + timeTo;
    }
    var serviceUrl = '/api/device-mgt/v1.0/geo-services/alerts/history/' + deviceType + '/' + deviceId + timeRange;
    invokerUtil.get(serviceUrl,
                    function (data) {
                        geoAlertsBar.clearAllAlerts();
                        var alerts = JSON.parse(data);
                        $.each(alerts, function (key, val) {
                            if(val.values){
                                val = val.values;
                            }
                            var msg = deviceType.charAt(0).toUpperCase() + deviceType.slice(1)  +
                                      " " +  deviceId +" "+ val.information.replace("Alerts: ,", "") + " - " + timeSince(val.timeStamp);
                            switch (val.state) {
                                case "NORMAL":
                                    return;
                                case "WARNING":
                                    geoAlertsBar.addAlert('warn', msg, val);
                                    break;
                                case "ALERTED":
                                    geoAlertsBar.addAlert('danger', msg, val);
                                    break;
                                case "OFFLINE":
                                    geoAlertsBar.addAlert('info', msg, val);
                                    break;
                            }
                        });
                    }, function (message) {
        });
}


function setProximityAlert() {

    var proximityDistance = $("#proximityDistance").val();
    var proximityTime = $("#proximityTime").val();

    if (proximityDistance == null || proximityDistance === undefined || proximityDistance == "") {
        var message = "Proximity distance cannot be empty.";
        noty({text: message, type : 'error'});
    } else if (proximityTime == null || proximityTime === undefined || proximityTime == "") {
        var message = "Proximity Time cannot be empty.";
        noty({text: message, type : 'error'});
    } else {

        var data = {
            'parseData': JSON.stringify({'proximityTime': proximityTime, 'proximityDistance': proximityDistance}),
            'proximityTime': proximityTime,
            'proximityDistance': proximityDistance,
            'executionPlan': 'Proximity',
            'customName': null,
            'cepAction': 'edit',
            'deviceId': deviceId
        };
        var serviceUrl = '/api/device-mgt/v1.0/geo-services/alerts/Proximity/' + deviceType + '/' + deviceId;
        var responseHandler = function (data, textStatus, xhr) {
            closeAll();
            if (xhr.status == 200) {
                noty({text: 'Successfully added alert', type : 'success'});
            } else {
                var ptrn = /(?:<am\:description>)(.*)(?:<\/am\:description>)/g;
                var errorTxt;
                if (result) {
                    errorTxt = result.length > 1 ? result[1] : data;
                } else {
                    errorTxt = data;
                }
                noty({text: textStatus + ' : ' + errorTxt, type : 'error'});
            }
        };
        invokerUtil.put(serviceUrl,
                        data,
                        responseHandler, function (xhr) {
                responseHandler(xhr.responseText, xhr.statusText, xhr);
            });

    }
}

// TODO:this is not a remote call , move this to application.js
function createExecutionPlanName(queryName, id, deviceId) {

    if (id == "WithIn") {
        return 'Geo-ExecutionPlan-Within' + (queryName ? '_' + queryName : '') + "---" + (deviceId ? '_' + deviceId : '') + '_alert'; // TODO: value of the `queryName` can't be empty, because it will cause name conflicts in CEP, have to do validation(check not empty String)
    } else if(id == "Exit"){
        return 'Geo-ExecutionPlan-Exit' + (queryName ? '_' + queryName : '') + "---" + (deviceId ? '_' + deviceId : '') + '_alert'; // TODO: value of the `queryName` can't be empty, because it will cause name conflicts in CEP, have to do validation(check not empty String)
    } else if (id == "Stationery") {
        return 'Geo-ExecutionPlan-Stationery' + (queryName ? '_' + queryName : '') + "---" + (deviceId ? '_' + deviceId : '') + '_alert'; // TODO: value of the `queryName` can't be empty, because it will cause name conflicts in CEP, have to do validation(check not empty String)
    } else if (id == "Traffic") {
        return 'Geo-ExecutionPlan-Traffic' + (queryName ? '_' + queryName : '') + '_alert'; // TODO: value of the `queryName` can't be empty, because it will cause name conflicts in CEP, have to do validation(check not empty String)
    }

}

// TODO:this is not a remote call , move this to application.js
function closeAll() {
    $('.modal').modal('hide');
    setTimeout(function () {
        $.noty.closeAll();
    }, 100);
}

