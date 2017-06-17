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

var drawControl;
var speedGraphControl;
var removeAllControl;
var drawnItems;
var lastId;
var controlDiv;

loadGeoFencing();

function loadGeoFencing() {
    if (map == null) {
        setTimeout(loadGeoFencing, 1000); // give everything some time to render
    } else {
        map.on('draw:created', function (e) {
            var type = e.layerType, layer = e.layer;
            drawnItems.addLayer(layer);
            console.log("created layer for "+ lastId);
            createPopup(layer,lastId);
        });
    }
}

function openTools(id) {
    lastId = id;
	if(drawControl){
		try{
    	map.removeControl(drawControl);
    	}catch(e){
    		console.log("error: " + e.message);
    	}
		console.log("removed drawControl");
    }
    if(removeAllControl){
    	try{
    	map.removeControl(removeAllControl);
    	}catch(e){
    		console.log("error: " + e.message);
    	}
		console.log("removed removeAllControl");
    }
    if(drawnItems){
    	try{
    	map.removeLayer(drawnItems);
        console.log("removing layer");
    	}catch(e){
    		console.log("error: " + e.message);
    	}
		console.log("removed drawnItems");
    }
    
    
    closeAll();
    noty({text: "Please draw the required area on the map", type: "information"});

    L.Control.RemoveAll = L.Control.extend(
        {
            options: {
                position: 'topleft'
            },
            onAdd: function (map) {
                controlDiv = L.DomUtil.create('div', 'leaflet-draw-toolbar leaflet-bar');
                L.DomEvent
                    .addListener(controlDiv, 'click', L.DomEvent.stopPropagation)
                    .addListener(controlDiv, 'click', L.DomEvent.preventDefault)
                    .addListener(controlDiv, 'click', function () {
                        controlDiv.remove();
                        drawControl.removeFrom(map);
                        drawnItems.clearLayers();
                        if(id == "Prediction"){
                            $('#predictionResults').animate({width: ['toggle','swing']},200);
                       	    toggeled = false;
                        }
                    });

                var controlUI = L.DomUtil.create('a', 'fa fa-times fa-lg drawControlCloseButton', controlDiv);
                $(controlUI).css("background-image", "none"); // Remove default control icon
                // TODO: bad usage of .hover() use CSS instead
                $(controlUI).mouseenter(function () {
                    $(this).css("color", "red");
                }).mouseleave(function () {
                    $(this).css("color", "black")
                });

                controlUI.title = 'Close drawer tools';
                controlUI.href = '#';
                return controlDiv;
            }
        });
    removeAllControl = new L.Control.RemoveAll();
    map.addControl(removeAllControl);


    // Initialise the FeatureGroup to store editable layers
    drawnItems = new L.FeatureGroup();
    map.addLayer(drawnItems);
	
    if(id=="WithIn"){
        // Initialise the draw control and pass it the FeatureGroup of editable layers
        drawControl = new L.Control.Draw({
            draw: {
                polygon: {
                    allowIntersection: false, // Restricts shapes to simple polygons
                    drawError: {
                        color: '#e1e100', // Color the shape will turn when intersects
                        message: '<strong>Oh snap!<strong> you can\'t draw that!' // Message that will show when intersect
                    },
                    shapeOptions: {
                        color: '#ff0043'
                    }
                },
                rectangle: {
                    shapeOptions: {
                        color: '#002bff'
                    }
                },
                polyline: false,
                circle: false, // Turns off this drawing tool
                marker: false // Markers are not applicable for within geo fencing
            },
            edit: {
                featureGroup: drawnItems
            }
        });
    } else if(id=="Exit"){
        // Initialise the draw control and pass it the FeatureGroup of editable layers
        drawControl = new L.Control.Draw({
                                             draw: {
                                                 polygon: {
                                                     allowIntersection: false, // Restricts shapes to simple polygons
                                                     drawError: {
                                                         color: '#e1e100', // Color the shape will turn when intersects
                                                         message: '<strong>Oh snap!<strong> you can\'t draw that!' // Message that will show when intersect
                                                     },
                                                     shapeOptions: {
                                                         color: '#ff0043'
                                                     }
                                                 },
                                                 rectangle: {
                                                     shapeOptions: {
                                                         color: '#002bff'
                                                     }
                                                 },
                                                 polyline: false,
                                                 circle: false, // Turns off this drawing tool
                                                 marker: false // Markers are not applicable for within geo fencing
                                             },
                                             edit: {
                                                 featureGroup: drawnItems
                                             }
                                         });
    } else if(id=="Stationery"){
        // Initialise the draw control and pass it the FeatureGroup of editable layers

        drawControl = new L.Control.Draw({
            draw: {
                polygon: {
                    allowIntersection: false, // Restricts shapes to simple polygons
                    drawError: {
                        color: '#e1e100', // Color the shape will turn when intersects
                        message: '<strong>Oh snap!<strong> you can\'t draw that!' // Message that will show when intersect
                    },
                    shapeOptions: {
                        color: '#ff0043'
                    }
                },
                rectangle: {
                    shapeOptions: {
                        color: '#002bff'
                    }
                },
                polyline: false,
                circle: {
                    shapeOptions: {
                        color: '#ff0043'
                    }
                },
                marker: false // Markers are not applicable for within geo fencing
            },
            edit: {
                featureGroup: drawnItems
            }
        });
    } else if(id=="Traffic"){
        // Initialise the draw control and pass it the FeatureGroup of editable layers
        drawControl = new L.Control.Draw({
            draw: {
                polygon: {
                    allowIntersection: false, // Restricts shapes to simple polygons
                    drawError: {
                        color: '#e1e100', // Color the shape will turn when intersects
                        message: '<strong>Oh snap!<strong> you can\'t draw that!' // Message that will show when intersect
                    },
                    shapeOptions: {
                        color: '#ff0043'
                    }
                },
                rectangle: {
                    shapeOptions: {
                        color: '#002bff'
                    }
                },
                polyline: false,
                circle: {
                    shapeOptions: {
                        color: '#ff0043'
                    }
                },
                marker: {
                	shapeOptions: {
                		color: '#ff0043'
                	}
                }
            },
            edit: {
                featureGroup: drawnItems
            }
        });
    } else if(id =="Prediction"){
    	 drawControl = new L.Control.Draw({
            draw: {
                polygon: false,
                rectangle: false,
                polyline: false,
                circle: false,
                marker: {
                	shapeOptions: {
                		color: '#ff0043'
                	}
                }
            },
            edit: {
                featureGroup: drawnItems
            }
        });
        console.log("prediction tool opened");
    }

    map.addControl(drawControl);
	
   

}

function createPopup(layer,id) {
    if(id=="WithIn"){
        var popupTemplate = $('#setWithinAlert');
        popupTemplate.find('#addWithinAlert').attr('leaflet_id', layer._leaflet_id);
    } else if(id=="Exit"){
        var popupTemplate = $('#setExitAlert');
        popupTemplate.find('#addExitAlert').attr('leaflet_id', layer._leaflet_id);
    } else if(id=="Stationery"){
        var popupTemplate = $('#setStationeryAlert');
        popupTemplate.find('#addStationeryAlert').attr('leaflet_id', layer._leaflet_id);
    } else if(id=="Traffic"){
        var popupTemplate = $('#setTrafficAlert');
        popupTemplate.find('#addTrafficAlert').attr('leaflet_id', layer._leaflet_id);
        //console.log(">>got here " + id + " " +  popupTemplate.find('#addTrafficAlert') + " " + layer._leaflet_id);
    } else if(id=="Prediction"){
    	getPrediction(layer._leaflet_id);
    	return;
    }
    
    popupTemplate.find('#exportGeoJson').attr('leaflet_id', layer._leaflet_id);
    popupTemplate.find('#editGeoJson').attr('leaflet_id', layer._leaflet_id);

    layer.bindPopup(popupTemplate.html(), {closeOnClick: false, closeButton: false}).openPopup();
    // transparent the layer .leaflet-popup-content-wrapper
    $(layer._popup._container.childNodes[0]).css("background", "rgba(255,255,255,0.8)");
}

function toggleSpeedGraph(){
    if (speedGraphControl) {
        try {
            map.removeControl(speedGraphControl);
            speedGraphControl = null;
        } catch (e) {
            console.log("error: " + e.message);
        }
    } else {
        speedGraphControl = new L.control.speedChart({'position' : 'topright'});
        map.addControl(speedGraphControl);
    }
}

function closeTools(leafletId) {
    map.removeLayer(map._layers[leafletId]);
    map.removeControl(drawControl);
    controlDiv.remove();
    console.log("DEBUG: closeTools(leafletId) = "+leafletId);
}

/* Export selected area on the map as a json encoded geoJson standard file, no back-end calls simple HTML5 trick ;) */
function exportToGeoJSON(element, content) {
    // HTML5 features has been used here
    var geoJsonData = 'data:application/json;charset=utf-8,' + encodeURIComponent(content);
    // TODO: replace closest()  by using persistence id for templates, template id prefixed by unique id(i.e leaflet_id)
    var fileName = $(element).closest('form').attr('area-name') || 'geoJson';
    var link = document.createElement("a");
    link.download = fileName + '.json'; // Use the fence name given by the user as the file name of the JSON file;
    link.href = geoJsonData;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    delete link;
}

$(function () {
    $("#importGeoJsonFile").change(function () {
        var importedFile = this.files[0];
        var reader = new FileReader();
        reader.readAsText(importedFile);
        reader.onload = function (e) {
            $("#enterGeoJson").text(e.target.result.toString());
        };
    });
});
function importGeoJson() {
    var updatedGeoJson;
    updatedGeoJson = $('#enterGeoJson').val();
    updateDrawing(updatedGeoJson);
}

function updateDrawing(updatedGeoJson) {
    updatedGeoJson = JSON.parse(updatedGeoJson);
    // Pop the last LatLng pair because according to the geoJSON standard it use complete round LatLng set to store polygon coordinates
    updatedGeoJson.geometry.coordinates[0].pop();
    var leafletLatLngs = [];
    $.each(updatedGeoJson.geometry.coordinates[0], function (idx, pItem) {
        leafletLatLngs.push({lat: pItem[1], lng: pItem[0]});
    });
    var polygon = new L.Polygon(leafletLatLngs);
    polygon.editing.enable();
    map.addLayer(polygon);
    createPopup(polygon);
    closeAll();
}

function viewFence(geoFenceElement,id) {
    var geoJson = $(geoFenceElement).attr('data-geoJson');
    var matchResults = /(?:"geoFenceGeoJSON"):"{(.*)}"/g.exec(geoJson);
    if (matchResults && matchResults.length > 1) {
        geoJson = "{" + matchResults[1] + "}";
    }
    geoJson = JSON.parse(geoJson.replace(/'/g, '"'));
    var queryName = $(geoFenceElement).attr('data-queryName');
    var areaName = $(geoFenceElement).attr('data-areaName');
    var geometryShape;

    if(geoJson.type=="Point"){

        var circleOptions = {
            color: '#ff0043'
        };
        geometryShape= new L.circle([geoJson.coordinates[1],geoJson.coordinates[0]], geoJson.radius,circleOptions);
       // var marker=new L.marker([geoJson.coordinates[1],geoJson.coordinates[0]]);
        map.addLayer(geometryShape);
      //  map.addLayer(marker);
    } else if(geoJson.type=="Polygon"){
        geoJson.coordinates[0].pop(); // popout the last coordinate set(lat,lng pair) due to circular chain
        var leafletLatLngs = [];
        $.each(geoJson.coordinates[0], function (idx, pItem) {
            leafletLatLngs.push({lat: pItem[1], lng: pItem[0]});
        });
        geometryShape = new L.Polygon(leafletLatLngs);
        map.addLayer(geometryShape);
    }

    var geoPublicUri = $("#geo-charts").data("geo-public-uri");

    if(id=="Stationery"){

        var stationeryTime=$(geoFenceElement).attr('data-stationeryTime');
        $('#templateLoader').load(geoPublicUri + "/assets/html_templates/view_fence_popup.html #viewStationeryAlert", function () {
            var popupTemplate = $('#templateLoader').find('#viewStationeryAlert');
            popupTemplate.find('#exportGeoJson').attr('leaflet_id', geometryShape._leaflet_id);
            popupTemplate.find('#hideViewFence').attr('leaflet_id', geometryShape._leaflet_id);
            popupTemplate.find('#viewAreaName').html(areaName);
            popupTemplate.find('#stationaryAlertForm').attr('area-name', areaName);
            popupTemplate.find('#stationaryAlertForm').attr('query-name', queryName);
            popupTemplate.find('#viewAreaTime').html(stationeryTime);
            geometryShape.bindPopup(popupTemplate.html(), {closeButton: true}).openPopup();
            // transparent the layer .leaflet-popup-content-wrapper
            $(geometryShape._popup._container.childNodes[0]).css("background", "rgba(255,255,255,0.8)");

        });
    } else if(id=="WithIn"){

        $('#templateLoader').load(geoPublicUri + "/assets/html_templates/view_fence_popup.html #viewWithinAlert", function () {
            var popupTemplate = $('#templateLoader').find('#viewWithinAlert');
            popupTemplate.find('#exportGeoJson').attr('leaflet_id', geometryShape._leaflet_id);
            popupTemplate.find('#hideViewFence').attr('leaflet_id', geometryShape._leaflet_id);
            popupTemplate.find('#viewAreaName').html(areaName);
            popupTemplate.find('#withinAlertForm').attr('area-name', areaName);
            popupTemplate.find('#withinAlertForm').attr('query-name', queryName);
            geometryShape.bindPopup(popupTemplate.html(), {closeButton: true}).openPopup();
            // transparent the layer .leaflet-popup-content-wrapper
            $(geometryShape._popup._container.childNodes[0]).css("background", "rgba(255,255,255,0.8)");
        });
    } else if(id=="Exit"){

        $('#templateLoader').load(geoPublicUri + "/assets/html_templates/view_fence_popup.html #viewExitAlert", function () {
            var popupTemplate = $('#templateLoader').find('#viewExitAlert');
            popupTemplate.find('#exportGeoJson').attr('leaflet_id', geometryShape._leaflet_id);
            popupTemplate.find('#hideViewFence').attr('leaflet_id', geometryShape._leaflet_id);
            popupTemplate.find('#viewAreaName').html(areaName);
            popupTemplate.find('#exitAlertForm').attr('area-name', areaName);
            popupTemplate.find('#exitAlertForm').attr('query-name', queryName);
            geometryShape.bindPopup(popupTemplate.html(), {closeButton: true}).openPopup();
            // transparent the layer .leaflet-popup-content-wrapper
            $(geometryShape._popup._container.childNodes[0]).css("background", "rgba(255,255,255,0.8)");
        });
    } else if(id=="Traffic"){
    }
    closeAll();
}
