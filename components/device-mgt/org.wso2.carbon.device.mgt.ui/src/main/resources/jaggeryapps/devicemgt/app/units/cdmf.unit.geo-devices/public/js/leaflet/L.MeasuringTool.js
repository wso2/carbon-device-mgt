/*
  This measuring tool is created to measure the distance between different points.
  * It allows to set the icons for the markers.
  * Sets a css class for the popup information (which you can style it yourself).
  * Sets the line with a css class so you can manipulate it as well.
  * You can measure the distance between multiple points.
  * You can indicate if you want to show the distance between each point, 
    the total distance or both.
  * You can indicate if the popups show a close button.
*/
L.MeasuringTool = L.Class.extend({
    initialize: function (map, options, iconStart, iconEnd) {
        L.Util.setOptions(this, options);
        this._map = map;
        this._layer = new L.LayerGroup().addTo(this._map);
        this._popupLayer = new L.LayerGroup().addTo(this._map);
        this._totalDistancePopup;

        this._measureLine = null;
        this._distancePopupList = [];
        this._markerList = [];
        var resizeIcon = L.icon({
            iconUrl: ApplicationOptions.leaflet.iconUrls.resizeIcon,
            iconAnchor: [0, 24]
        });

        this._markerIcons = null;
        if (iconStart || iconEnd) {
            this._markerIcons = [iconStart, iconEnd];
        }
    },

    options: {
        minWidth: 50,
        autoPan: false,
        closeButton: true, // should the popups have a close option?
        displayTotalDistance: true,
        displayPartialDistance: false,
        className: 'measuring-label', /*css label class name*/
        lineClassName: 'measuring-line-class' /*css class name for the line*/
    },

    enable: function() {
        this._map.on('click', this._addMarker, this);
    },

    disable: function () {
        this._map.off('click', this._addMarker, this);

        while (this._distancePopupList.length > 0) {
            this._layer.removeLayer(this._distancePopupList.pop());
        }
        
        if (this._measureLine) {
            this._layer.removeLayer(this._measureLine);
            this._measureLine = undefined;
        }

        for (var i = 0; this._markerList.length; i++) {
            var marker = this._markerList.pop();
            marker.off('drag', this._updateRuler, this)
                  .off('contextmenu', this._removeMarker, this);

            this._layer.removeLayer(marker);
        }

        this._popupLayer.clearLayers();
        this._layer.clearLayers();
    },

    // on click => adds a new point to the line
    _addMarker: function(markerLocation) {
        var markerPosition = this._markerList.length;

//        var markerLocation = e.latlng;
        // created and adds the marker to the map
        var marker = new L.Marker(markerLocation);
        this._layer.addLayer(marker);

        // sets the marker with the correct icons (if previously set)
        if (this._markerIcons && this._markerIcons.length == 2) {
            if (markerPosition == 0) { // if it is the first one
                marker.setIcon(normalMovingIcon);
            } else {
                marker.dragging.enable();
                marker.on('drag', this._updateRuler, this)
                    .on('contextmenu', this._removeMarker, this); // On right click remove marker
                marker.setIcon(resizeIcon);
                this._markerList[markerPosition - 1].setIcon(this._markerIcons[0]);
            }
        }

        // setting the position to the marker
        // (it is used when moving them or removing them)
        marker._pos = markerPosition;

        this._markerList.push(marker);
        this._setupLine();

        // show total distance after there are at least 2 points
        if (markerPosition >= 1) {
            this._showTotalDistance();
        }
    },

    // on right click => remove the point from the line
    _removeMarker: function(e) {
        var target = e.target;

        // removing the popup showing the total distance as it will
        // be recalculated
        this._popupLayer.removeLayer(this._totalDistancePopup);

        // removing the point from the polyline
        var listLatng = this._measureLine.getLatLngs();
        listLatng.splice(target._pos, 1);
        this._measureLine.setLatLngs(listLatng);

        // removing the marker
        var deleteMarker = this._markerList.splice(target._pos, 1);
        this._layer.removeLayer(deleteMarker[0]);

        var totalMarkers = this._markerList.length;
        // removing the popups involved and update the distance between
        // the new neighbouring points
        if (target._pos == 0) { // first point
            this._distancePopupList.splice(target._pos, 1);

            if (totalMarkers > 1) {
                var startLatLng = this._markerList[target._pos].getLatLng(),
                    endLatLng = this._markerList[target._pos + 1].getLatLng();

                this._setDistancePopup(startLatLng, endLatLng, 1);
            }
        } else if (target._pos == listLatng.length) { // last point
            this._distancePopupList.pop();

            if (totalMarkers > 1) {
                var startLatLng = this._markerList[target._pos - 2].getLatLng(),
                    endLatLng = this._markerList[target._pos - 1].getLatLng();

                this._setDistancePopup(startLatLng, endLatLng, target._pos - 1);
            }
        } else { // point in the middle
            this._distancePopupList.splice(target._pos, 1);

            var startLatLng = this._markerList[target._pos - 1].getLatLng(),
                endLatLng = this._markerList[target._pos].getLatLng();
            this._setDistancePopup(startLatLng, endLatLng, target._pos);
        }

        // update the position numbers on the marker list
        for (var i = 0; i < this._markerList.length; i++) {
            this._markerList[i]._pos = i;
        }

        // show the updated total distance
        this._showTotalDistance();
    },

    // creates the line and adds the additional points for every click on the map
    _setupLine: function() {
        var totalMarkers = this._markerList.length;
        // do nothing in case there is only one point
        if (totalMarkers <= 1) {
            return;
        }

        // getting the old last two points of the line
        var startLatLng = this._markerList[totalMarkers - 2].getLatLng(),
            endLatLng = this._markerList[totalMarkers - 1].getLatLng();

        // if there are two points => create the line
        if (totalMarkers == 2) {
            //Do not worry, I decided to set this as the standard behaviour.
            //But you can change the style by setting your own class "lineClassName"
            this._measureLine = new L.Polyline(
                [startLatLng, endLatLng ], 
                { color: "black", opacity: 0.5, stroke: true });

            this._layer.addLayer(this._measureLine);
            this._measureLine._path.setAttribute("class", this.options['lineClassName']);

            // setting up the popup for the total distance
            this._totalDistancePopup = new L.Popup(this.options, this._measureLine);
        } else { // for every extra point just add the new point to the line
            this._measureLine.addLatLng(endLatLng);
        }

        this._setDistancePopup(startLatLng, endLatLng, totalMarkers - 1);
    },

    // obtains the distance between two points and 
    // updates the information for that segment
    _setDistancePopup: function(startLatLng, endLatLng, index) {
        var centerPos = new L.LatLng((startLatLng.lat + endLatLng.lat)/2, 
                                     (startLatLng.lng + endLatLng.lng)/2),
            distance = startLatLng.distanceTo(endLatLng);

        this.setContent(distance, centerPos, index);
        this._showSegmentsInfo();
    },

    // updates the segment information in case that segment has been updated
    // when dragging the markers
    _updateRuler: function (e) {
        // if there is no line => do nothing
        if (!this._measureLine) {
            return;
        }

        var target = e.target,
            listLatng = this._measureLine.getLatLngs();

        // updating line with the new point
        listLatng[target._pos] = target.getLatLng();
        // setting the new coordingates to the line
        this._measureLine.setLatLngs(listLatng);

        // updating each of the segments which were afected by the 
        // move
        if (target._pos == 0) { // first marker
            var startLatLng = this._markerList[0].getLatLng(),
                endLatLng = this._markerList[1].getLatLng();
            this._setDistancePopup(startLatLng, endLatLng, 1);
        } else if (target._pos == listLatng.length - 1) { // last marker
            var totalMarkers = this._markerList.length,
                startLatLng = this._markerList[totalMarkers - 2].getLatLng(),
                endLatLng = this._markerList[totalMarkers - 1].getLatLng();
            this._setDistancePopup(startLatLng, endLatLng, target._pos);
        } else { // markers in betweek
            var startLatLng = this._markerList[target._pos - 1].getLatLng(),
                endLatLng = this._markerList[target._pos].getLatLng();
            this._setDistancePopup(startLatLng, endLatLng, target._pos);

            var startLatLng = endLatLng,
                endLatLng = this._markerList[target._pos + 1].getLatLng();
            this._setDistancePopup(startLatLng, endLatLng, target._pos + 1);
        }

        this._showTotalDistance();
    },

    // shows the popups for every segment with its distance
    _showSegmentsInfo: function() {
        this._popupLayer.clearLayers();
        // if the user decided not to show the partials => return
        if (!this.options['displayPartialDistance']) {
            return;
        }

        // this has to be done everytime I want to show more than one popup
        // at the same time
        for (var i = 0; i < this._distancePopupList.length; i++) {
            var popup = this._distancePopupList[i];
            this._popupLayer.addLayer(popup);
        }
    },

    // shows a popup displaying the total distance of the line
    _showTotalDistance: function() {
        // if the user decided not to show the total distance => return
        if (!this.options['displayTotalDistance']) {
            return;
        }
        // if there are less than 2 points => no point of showing anything
        var totalMarkers = this._markerList.length;
        if (totalMarkers < 2) {
            return;
        }

        // calculating the total distance
        var totalDistance = 0;
        for (var i = 0; i < this._distancePopupList.length; i++) {
            var popup = this._distancePopupList[i];
            totalDistance += popup._distance;
        }

        // getting the last marker, whom will be the one showing the info
        var marker = this._markerList[totalMarkers - 1];
        // setting the correct content to the popup
        this._totalDistancePopup.setContent('<b>Total distance:</b></br>' + totalDistance.toFixed(2) + 'm.');
        // moving the popup a bit on top so it doesn't hide the last marker
        var shiftedPosition = this._shiftPosition(marker.getLatLng(), -25, 0)
        this._totalDistancePopup.setLatLng(shiftedPosition);
        // displaying the popup
        this._popupLayer.addLayer(this._totalDistancePopup);
    },

    // helper funtion to shift a point
    _shiftPosition: function (latlng, northPx, eastPx) {
        var p1 = this._map.latLngToLayerPoint(latlng);
        p1.y += northPx;
        p1.x += eastPx;
        return this._map.layerPointToLatLng(p1);
    },

    // updates the information for a segment
    setContent: function (distance, coord, index) {
        var popup = this._distancePopupList[index - 1];
        // if the popup for the segment does not exist => create one
        if (!popup) {
            popup = new L.Popup(this.options, this._measureLine);
            this._distancePopupList.push(popup);
        }

        popup.setContent('<b>Distance:</b></br>' + distance.toFixed(2) + 'm.');
        popup.setLatLng(coord);

        // storing the partial distance in the popup
        popup._distance = distance;
    },

    fire: function (fnName, params) {
        if (fnName) {
            console.log("fn called is: " + fnName);
            if (this[fnName]) {
                this[fnName](params);
            }
        }
    },

    popupopen: function(obj) {},
    popupclose: function(obj) {}
});
