/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function onRequest(context) {
    var utility = require("/app/modules/utility.js").utility;
    var groupModule = require("/app/modules/business-controllers/group.js")["groupModule"];
    var groupId = context.uriParams.id;
    var group = groupModule.getGroup(groupId);
    var devices = [];
    var deviceResponse = groupModule.getGroupDevices(groupId).responseText;

    if(deviceResponse != null) {
        var deviceResponseObj = parse(deviceResponse);
        devices = deviceResponseObj.devices;
    }
    var page = {
        "groupId": groupId,
        "groupName": group.name,
        "title": group.name + " Analytics"
    };
    if (devices) {
        var deviceTypes = [];
        for (var i = 0; i < devices.length; i++) {
            var hasDeviceType = false;
            for (var j = 0; j < deviceTypes.length; j++) {
                if (deviceTypes[j].type === devices[i].type) {
                    deviceTypes[j].devices.push(devices[i]);
                    hasDeviceType = true;
                    break;
                }
            }
            if (!hasDeviceType) {
                var deviceType = {};
                deviceType.type = devices[i].type;
                deviceType.devices = [];
                deviceType.devices.push(devices[i]);
                deviceType.deviceAnalyticsViewUnitName = utility.getTenantedDeviceUnitName(deviceType.type, "analytics-view");
                deviceTypes.push(deviceType);
            }
        }
        page.deviceTypes = deviceTypes;
        page.devices = devices;
    }

    return page;
}
