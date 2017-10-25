/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
L.Control.GeoAlertCompact = L.Control.extend({
   options: {
       position: 'topright',
       icon: 'fw fw-notification',  // icon-location or icon-direction
       strings: {
           title: "Show the device"
       },
       marker: {},
       zoomLevel: 13,
       alerts: {}
   },

   onAdd: function (map) {
       var self = this;
       var container = L.DomUtil.create('div', 'leaflet-bar leaflet-control');

       var link = L.DomUtil.create('a', 'leaflet-bar-part leaflet-bar-part-single', container);
       link.href = '#';
       link.title = this.options.strings.title;

       var alertsCount = this.options.alerts.length;
       if (alertsCount > 0) {
           var spanMain = L.DomUtil.create('i', 'fw-stack', link);
           L.DomUtil.create('i', this.options.icon + ' fw-stack-2x', spanMain);
           var span = L.DomUtil.create('span', 'fw-stack fw-move-right fw-move-bottom', spanMain);
           L.DomUtil.create('i', 'fw fw-circle fw-stack-2x fw-stroke', span);
           var num = L.DomUtil.create('i', 'fw fw-number fw-stack-1x fw-inverse', span);
           num.innerHTML = (alertsCount < 100) ? alertsCount + "" : "99+";
       } else {
           L.DomUtil.create('i', this.options.icon, link);
       }

       L.DomEvent
           .on(link, 'click', L.DomEvent.stopPropagation)
           .on(link, 'click', L.DomEvent.preventDefault)
           .on(link, 'click', function () {
               link.remove();
               self.removeFrom(map);
               toggleSpeedGraph();
               map.addControl(L.control.geoAlerts());
               for (var i = 0; i < self.options.alerts.length; i++) {
                   var alert = self.options.alerts[i];
                   L.DomUtil.get("geoAlertContainer").appendChild(alert);
                   i--; //because dom manipulation
               }
               toggleSpeedGraph();
               self = null;
           })
           .on(link, 'dblclick', L.DomEvent.stopPropagation);

       return container;
   }
});

L.control.geoAlertCompact = function (options) {
    return new L.Control.GeoAlertCompact(options);
};

L.Control.GeoAlerts = L.Control.extend({
    options: {
        position: 'topright'
    },

    onAdd: function (map) {
        var self = this;
        var container = L.DomUtil.create('div', 'leaflet-control list-group leaflet-geo-alerts');
        container.id = "geoAlertContainer";

        var title = L.DomUtil.create('h5', '', container);
        title.innerHTML = "Geo Alerts";

        var closeBtn = L.DomUtil.create('a', 'leaflet-popup-close-button', container);
        closeBtn.href = "#close";
        closeBtn.innerHTML = "×";
        L.DomEvent
            .addListener(closeBtn, 'click', L.DomEvent.stopPropagation)
            .addListener(closeBtn, 'click', L.DomEvent.preventDefault)
            .addListener(closeBtn, 'click', function () {
                closeBtn.remove();
                var alerts = L.DomUtil.get("geoAlertContainer").getElementsByClassName("leaflet-geo-alert");
                self.removeFrom(map);
                var alertCompactControl = L.control.geoAlertCompact({'alerts':alerts});
                toggleSpeedGraph();
                map.addControl(alertCompactControl);
                toggleSpeedGraph();
                self = null;
            });

        this.options.alertContainer = container;
        return container;
    },

    addAlert: function (state, text, alert){
        var container = L.DomUtil.get("geoAlertContainer");
        var link = L.DomUtil.create('a', 'leaflet-geo-alert list-group-item list-group-item alert alert-' + state, container);
        link.href = 'javascript:void()';
        link.innerHTML = text;
        link.setAttribute('onClick', 'showAlertInMap(this)');

        // Set HTML5 data attributes for later use
        link.setAttribute('data-id', alert.id);
        link.setAttribute('data-latitude', alert.latitude);
        link.setAttribute('data-longitude', alert.longitude);
        link.setAttribute('data-state', alert.state);
        link.setAttribute('data-information', alert.information);
    },

    getAllAlerts: function (){
        return L.DomUtil.get("geoAlertContainer").getElementsByClassName("leaflet-geo-alert");
    },

    clearAllAlerts: function () {
        var alerts = L.DomUtil.get("geoAlertContainer").getElementsByClassName("leaflet-geo-alert");
        while(alerts[0]){
            alerts[0].parentNode.removeChild(alerts[0]);
        }
    }
});

L.Map.addInitHook(function () {
    if (this.options.geoAlertsControl) {
        this.geoAlertsControl = L.control.geoAlerts();
        this.addControl(this.geoAlertsControl);
    }
});

L.control.geoAlerts = function (options) {
    return new L.Control.GeoAlerts(options);
};

L.Control.SpeedChart = L.Control.extend({
   options: {
       position: 'topleft',
       alerts: {}
   },

   onAdd: function (map) {
       var self = this;
       var container = L.DomUtil.create('div', 'leaflet-control list-group leaflet-geo-alerts');

       var title = L.DomUtil.create('h5', '', container);
       title.innerHTML = "Speed";

       var closeBtn = L.DomUtil.create('a', 'leaflet-popup-close-button', container);
       closeBtn.href = "#close";
       closeBtn.innerHTML = "×";
       L.DomEvent
           .addListener(closeBtn, 'click', L.DomEvent.stopPropagation)
           .addListener(closeBtn, 'click', L.DomEvent.preventDefault)
           .addListener(closeBtn, 'click', function () {
               closeBtn.remove();
               speedGraphControl.removeFrom(map);
               speedGraphControl = null;
           });

       var chartDiv = L.DomUtil.create('div', '', container);
       chartDiv.id = 'chart_div';
       this.options.alertContainer = container;
       return container;
   },

   updateGraph: function () {
       var alerts = L.DomUtil.get("chart_div");
   }
});

L.control.speedChart = function (options) {
    return new L.Control.SpeedChart(options);
};