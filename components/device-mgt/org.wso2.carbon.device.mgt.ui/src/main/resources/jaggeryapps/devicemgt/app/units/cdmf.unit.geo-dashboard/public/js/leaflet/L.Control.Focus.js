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
L.Control.Focus = L.Control.extend({
    options: {
        position: 'topleft',
        icon: 'icon-location',  // icon-location or icon-direction
        iconLoading: 'icon-spinner animate-spin',
        strings: {
            title: "Show the device"
        },
        marker: {},
        zoomLevel: 13
    },

    onAdd: function (map) {
        var self = this;
        var container = L.DomUtil.create('div',
            'leaflet-control-locate leaflet-bar leaflet-control');

        var link = L.DomUtil.create('a', 'leaflet-bar-part leaflet-bar-part-single ' + this.options.icon, container);
        link.href = '#';
        link.title = this.options.strings.title;

        L.DomEvent
            .on(link, 'click', L.DomEvent.stopPropagation)
            .on(link, 'click', L.DomEvent.preventDefault)
            .on(link, 'click', function () {
                // self.options.latlng
                map.setView(self.options.marker.getLatLng(), self.options.marker.zoomLevel, {animate: true});
                self.options.marker.openPopup();
            })
            .on(link, 'dblclick', L.DomEvent.stopPropagation);

        return container;
    }
});

L.Map.addInitHook(function () {
    if (this.options.focusControl) {
        this.focusControl = L.control.focus();
        this.addControl(this.focusControl);
    }
});

L.control.focus = function (options) {
    return new L.Control.Focus(options);
};