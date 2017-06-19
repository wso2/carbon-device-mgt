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
var ApplicationOptions = {
    colors: {
        states: {
            NORMAL: 'blue',
            WARNING: 'blue',
            OFFLINE: 'grey',
            ALERTED: 'red',
            UNKNOWN: 'black'
        },
        application: {
            header: 'grey'
        }
    },
    constance:{
        CEP_WEB_SOCKET_OUTPUT_ADAPTOR_NAME: 'iot.per.device.stream.geo.FusedSpatialEvent',
        CEP_ON_ALERT_WEB_SOCKET_OUTPUT_ADAPTOR_NAME: 'org.wso2.geo.AlertsNotifications',
        CEP_Traffic_STREAM_WEB_SOCKET_OUTPUT_ADAPTOR_NAME: 'DefaultWebsocketOutputAdaptorOnTrafficStream',
        CEP_WEB_SOCKET_OUTPUT_ADAPTOR_WEBAPP_NAME: 'secured-websocket',
        TENANT_INDEX: 't',
        COLON : ':',
        PATH_SEPARATOR : '/',
        VERSION: '1.0.0',
        SPEED_HISTORY_COUNT: 20,
        NOTIFY_INFO_TIMEOUT: 1000,
        NOTIFY_SUCCESS_TIMEOUT: 1000,
        NOTIFY_WARNING_TIMEOUT: 3000,
        NOTIFY_DANGER_TIMEOUT: 5000
    },
    messages:{
        app:{

        }
    },
    leaflet: {
        iconUrls: {
            //TODO path needs to be changed
            normalMovingIcon: '/img/markers/object-types/default/moving/alerted.png',
            alertedMovingIcon: '/img/markers/moving/arrow_alerted.png',
            offlineMovingIcon: '/img/markers/moving/arrow_offline.png',
            warningMovingIcon: '/img/markers/moving/arrow_warning.png',
            defaultMovingIcon: '/img/markers/moving/arrow_normal.png',

            normalNonMovingIcon: '/img/markers/non_moving/dot_normal.png',
            alertedNonMovingIcon: '/img/markers/non_moving/dot_alerted.png',
            offlineNonMovingIcon: '/img/markers/non_moving/dot_offline.png',
            warningNonMovingIcon: '/img/markers/non_moving/dot_warning.png',
            defaultNonMovingIcon: '/img/markers/non_moving/dot_normal.png',

            normalPlaceIcon: '/img/markers/places/marker-icon.png',
            alertedPlaceIcon: '/img/markers/places/redMarker.png',
            offlinePlaceIcon: '/img/markers/places/ashMarker.png',
            warningPlaceIcon: '/img/markers/places/pinkMarker.png',
            defaultPlaceIcon: '/img/markers/places/marker-icon.png',

            defaultIcon: '/img/markers/moving/default_icons/marker-icon.png',
            resizeIcon: '/img/markers/resize.png',
            stopIcon:  '/img/markers/stopIcon.png'
        }
    }
};
