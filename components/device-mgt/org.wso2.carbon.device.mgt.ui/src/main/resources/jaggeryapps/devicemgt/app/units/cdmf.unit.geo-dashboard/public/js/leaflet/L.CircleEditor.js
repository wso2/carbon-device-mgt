/*
 * L.CircleEditor is an extension of L.Circle, just to add the edition part (remember, radius in meters).
 */

L.CircleEditor = L.Circle.extend ({

    options: {
        icon: new L.DivIcon({
            iconSize: new L.Point(8, 8),
            className: 'leaflet-div-icon leaflet-editing-icon'
        }),
        extendedIconClass : 'extend-icon'
    },

    onAdd: function (map) {
        L.Path.prototype.onAdd.call(this, map);

        this.addHooks();
    },

    onRemove: function (map) {
        this.removeHooks();

        L.Path.prototype.onRemove.call(this, map);
    },


    addHooks: function () {
        if (this._map) {
            if (!this._markerGroup) {
                this._initMarkers();
            }
            this._map.addLayer(this._markerGroup);
        }
    },

    removeHooks: function () {
        if (this._map) {
            this._map.removeLayer(this._markerGroup);
            delete this._markerGroup;
            delete this._markers;
        }
    },

    updateMarkers: function () {
        this._markerGroup.clearLayers();
        this._initMarkers();
    },

    _initMarkers: function () {
        this._markerGroup = new L.LayerGroup();
        this._markers = [];

        var markerCenter = this._createMarker(this._latlng, 0, true);
        this._markers.push(markerCenter);

        var circleBounds = this.getBounds(),
            center = circleBounds.getCenter(),
            neCoord = circleBounds.getNorthEast(),
            northCenterCoord = new L.LatLng(center.lat, neCoord.lng, true);
            markerNorthCenter = this._createMarker(northCenterCoord, 1);
        this._markers.push(markerNorthCenter);
    },

    _createMarker: function (latlng, index, isCenter) {
        var marker = new L.Marker(latlng, {
            draggable: true,
            icon: this.options.icon
        });

        if (isCenter === undefined) {
            isCenter = false;
        }
        //console.log("this is center point: " + isCenter);

        marker._origLatLng = latlng;
        marker._index = index;
        marker._isCenter = isCenter;

        if (isCenter) {
            marker.on('drag', this._onCenterMove, this)
                  .on('dragend', this._onCenterMoveEnd, this);
        } else {
            marker.on('drag', this._onMarkerDrag, this);
        }
        marker.on('dragend', this._fireEdit, this)
              .on('mouseover', this._onMouseOver, this)
              .on('mouseout', this._onMouseOut, this);

        this._markerGroup.addLayer(marker);

        return marker;
    },
    _onMouseOver: function (e) {
        var target = e.target,
            icon = target._icon,
            classValues = icon.getAttribute("class");
        //icon.setAttribute("class", "extend-icon " + classValues);
        icon.setAttribute("class", this.options.extendedIconClass + " " + classValues);
    },
    _onMouseOut: function (e) {
        var target = e.target,
            icon = target._icon,
            classValues = icon.getAttribute("class");
        //icon.setAttribute("class", classValues.replace("extend-icon", ""));
        icon.setAttribute("class", classValues.replace(this.options.extendedIconClass, ""));
    },

    _fireEdit: function () {
        this.fire('edit');
    },

    _onCenterMove: function (e) {
        var marker = e.target;
        //console.log("center move - START");

        L.Util.extend(marker._origLatLng, marker._latlng);

        var mm = this._markers[1];
        mm.setOpacity(0.1);

        this.redraw();
        
        //console.log("END");
    },

    _onCenterMoveEnd: function (e) {
        var marker = e.target;
        
        //now resetting the side point
        var circleBounds = this.getBounds(),
            center = circleBounds.getCenter(),
            neCoord = circleBounds.getNorthEast(),
            northCenterCoord = new L.LatLng(center.lat, neCoord.lng, true);

        var mm = this._markers[1];
        mm.setLatLng(northCenterCoord);
        mm.setOpacity(1);

        this.fire('centerchange');
    },

    _onMarkerDrag: function (e) {
        var marker = e.target;
        //console.log("marker drag - START");
        var center = this._markers[0].getLatLng();
        var axis = marker._latlng;

        var distance = center.distanceTo(axis);

        this.setRadius(distance);
        
        this.redraw();
        //console.log("END");

        this.fire('radiuschange');
    },

    centerchange: function() {},
    radiuschange: function() {}
});
