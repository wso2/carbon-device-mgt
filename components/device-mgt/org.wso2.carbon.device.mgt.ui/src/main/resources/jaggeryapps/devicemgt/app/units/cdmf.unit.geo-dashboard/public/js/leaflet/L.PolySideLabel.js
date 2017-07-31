/*The class shows the length of each of the side for a Polygon and Polyline.
* The distance is displayed in meter or kilometes.
* If the distance between each side is too small (by default 40m.) then instead of
* the distance a letter is shown for all sides and a pop over is shown on the side
* which includes the values for each of the letters.
* If the area is too small (becuase you zoomed out too much) the info is not shown.
* Included a limit on number of sides. Which is helpful is you think that showing more
* than 8, 10, 20 sides values is not helpful.
*
* TODOS:
*    1) Handling on IE7/8 is not the best, it works by showing all the sides of all
        the polygons/polylines.
*    2) If the area is too small, only the pop over should be shown, showing the area
        or total length.
*/

L.PolySideLabel = L.Class.extend({

    options: {
        minWidth: 40,
        autoPan: false,
        closeButton: false,
        className: 'poly-label',
        offset: new L.Point(0, -20),
        minSideLength: 40, //Min length for a side in meters before using Charactes
        minAreaToShow: 0.0025, //Min area for which is worth showing the distance info
        bordersLimit: 8
    },
    
    initialize: function (polyObj, options) {
        L.Util.setOptions(this, options);
        this._polygon = polyObj;
        this._labelsList = [];
        this._numberOfBordersLimit = this.options.bordersLimit;
        this._isViewable = null;

        //If it is attached to the map
        //and it has sides (circles won't work here)
        //then labels will be added to each side of the polygon
        if (this._polygon._path && this._hasSides(this._polygon)) { //this._polygon.getLatLngs) {
            L.DomEvent
                .addListener(this._polygon._path, 'mouseover', this._showLabels, this)
                .addListener(this._polygon._path, 'mouseout', this._hideLabels, this);

            this._map = this._polygon._map;

            //NOTE: Doing this, will show all the labels for all the geometries when you are over one geometry
            //This part will only work for IE7 and IE8
            var _this = this;
            $(".leaflet-vml-shape")
                .mouseover(function () { _this._showLabels(); })
                .mouseout(function () { _this._hideLabels(); });
                
            this._map.on("zoomend", this._isLabelViewable, this);
            this._isLabelViewable();
        }
    },

    /*TODO: This method should be re writen, it is too messy*/
    _showLabels: function () {
        if (this._labelsList.length > 0) {
            console.log('the label list is full');
            return;
        }
        if (!this._isViewable) {
            console.log('it is too small to be viewable');
            return;
        }

        //var sum = 0;
        var charCounter = 65;
        this.myDescription = "";
        var showLabels = true;

        var latlngs = this._polygon.getLatLngs();
        var numberOfSides = latlngs.length;
        var isPolygon = this._isPolygon(this._polygon);
        if (!isPolygon) {
            numberOfSides--;
        }

        if (latlngs.length > this._numberOfBordersLimit) {
            showLabels = false;
            console.log('max number of borders: '+this._numberOfBordersLimit+', there are: ' + latlngs.length);
            return;
        }

        var pointsArray = [];
        var distance;
        var isSmall = false;
        var nextPoint = 1;
        var i = 0;
        for (i = 0; i < numberOfSides; i++) {
            if ((nextPoint == numberOfSides) && (isPolygon)) {
                nextPoint = 0;
            }
            distance = latlngs[i].distanceTo(latlngs[nextPoint]);
     
            if (showLabels) {
                pointsArray.push({ Distance: distance,
                    Coord: this._getMiddleLatLng(latlngs[i], latlngs[nextPoint]),
                    Unit: this.LengthUnit(distance)
                });
                if (distance < this.options.minSideLength /*meters*/) {
                    isSmall = true;
                }
            }
            nextPoint++;
        }
        if (showLabels) {
            var pu, description = "";
            for (i = 0; i < pointsArray.length; i++) {
                pu = new L.Popup(this.options, this._polygon);
                pu.setContent(isSmall ? String.fromCharCode(charCounter) : pointsArray[i].Unit);
                pu.setLatLng(pointsArray[i].Coord);

                this._map.addLayer(pu);
                //this._map.fire('popupopen', {popup: pu});

                this._labelsList.push(pu);

                description += String.fromCharCode(charCounter++) + ": " + pointsArray[i].Unit + "<br/>";
            }

            if (isSmall) {
                var bounds = this._polygon.getBounds();
                var latLngNE = bounds.getNorthEast();
                latLngNE = this._shiftPosition(latLngNE, 0, 35);
                pu = new L.Popup(this.options, this._polygon);
                pu.setContent(description);
                pu.setLatLng(latLngNE);
                this._map.addLayer(pu);
                this._map.fire('popupopen', {popup: pu});

                this._labelsList.push(pu);
            }
        }
    },

    _getMiddleLatLng: function (latlng1, latlng2) {
        var p1 = this._map.latLngToLayerPoint(latlng1),
            p2 = this._map.latLngToLayerPoint(latlng2);

        return this._map.layerPointToLatLng(p1._add(p2).divideBy(2));
    },

    _shiftPosition: function (latlng, northPx, eastPx) {
        var p1 = this._map.latLngToLayerPoint(latlng);
        p1.y += northPx;
        p1.x += eastPx;
        return this._map.layerPointToLatLng(p1);
    },

    _hasSides: function () {
        //polygon inherits from polyline, so all polygons are polylines
        if (this._polygon instanceof L.Polyline) {
            return true;
        }
        return false;
    },

    _isPolygon: function () {
        if (this._polygon instanceof L.Polygon) {
            return true;
        }
        return false;
    },

    _hideLabels: function () {
        for (var i = 0; i < this._labelsList.length; i++) {
            this._map.removeLayer(this._labelsList[i]);
        }
        this._labelsList = [];
    },
    
    //This is thought as:
    /*
        Get the area of the geometry as well as the map area.
        If the area of the geometry is less than 0.25% of the map
        then it is too small to distinguish the sides and there for
        it should not be drawn.
    */
    _isLabelViewable: function () {
        var mapSize = this._map.getSize(),
            mapArea = mapSize.x * mapSize.y,
            polyBounds = this._polygon.getBounds();
            polySW = this._map.project(polyBounds.getSouthWest()),
            polyNE = this._map.project(polyBounds.getNorthEast()),
            polyArea = Math.abs( (polySW.x - polyNE.x) * (polySW.y - polyNE.y) );
        
        var result = (polyArea/mapArea);
        //console.log("% viewable: " + result + " --- poly area is: " + polyArea);
        if (result < this.options.minAreaToShow /* in m2 ... 0.0025*/) {
            this._isViewable = false;
        }
        this._isViewable = true;
    },

    LengthUnit: function (meters) {
        var str = meters.toFixed(1) + "";
        return meters > 999 ? ((str / 100) / 10).toFixed(1) + "km" : str + "m";
    },

    AreaUnit: function (meters) {
        var str = meters.toFixed(1) + "";
        if (meters > 1000000) {
            return ((meters / 1000000) / 10).toFixed(1) + "km\xB2";
        } else if (meters > 1000) {
            return ((meters / 1000) / 10).toFixed(1) + "ha";
        } else {
            return str + "m\xB2";
        }
    }
});

