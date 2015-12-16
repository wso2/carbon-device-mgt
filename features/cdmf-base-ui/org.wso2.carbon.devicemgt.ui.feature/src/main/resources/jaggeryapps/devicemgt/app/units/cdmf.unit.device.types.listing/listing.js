/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    var log = new Log("listing.js");
    var DTYPE_CONF_DEVICE_TYPE_KEY = "deviceType";
    var DTYPE_CONF_DEVICE_TYPE_LABEL_KEY = "label";
    var DTYPE_UNIT_NAME_PREFIX = "cdmf.unit.device.type.";
    var DTYPE_UNIT_NAME_SUFFIX = ".type-view";
    var DTYPE_UNIT_LISTING_TEMPLATE_PATH = "/public/templates/listing.hbs";

    var viewModel = {};
    var deviceModule = require("/app/modules/device.js").deviceModule;
    var utility = require("/app/modules/utility.js").utility;
    var data = deviceModule.getDeviceTypes();

    if (data.data) {
        var deviceTypes = data.data;
        var deviceTypesList = [];
        for (var i = 0; i < deviceTypes.length; i++) {

            var deviceTypeLabel = deviceTypes[i].name;
            var configs = utility.getDeviceTypeConfig(deviceTypeLabel);

            if (configs && configs[DTYPE_CONF_DEVICE_TYPE_KEY][DTYPE_CONF_DEVICE_TYPE_LABEL_KEY]) {
                deviceTypeLabel = configs[DTYPE_CONF_DEVICE_TYPE_KEY][DTYPE_CONF_DEVICE_TYPE_LABEL_KEY];
            }

            var deviceTypeListingTemplateFile = getFile("../" + DTYPE_UNIT_NAME_PREFIX + deviceTypes[i].name + DTYPE_UNIT_NAME_SUFFIX + DTYPE_UNIT_LISTING_TEMPLATE_PATH);
            if (deviceTypeListingTemplateFile) {
                deviceTypesList.push({
                    "hasCustTemplate": true,
                    "deviceTypeLabel": deviceTypeLabel,
                    "deviceTypeName": deviceTypes[i].name,
                    "deviceTypeId": deviceTypes[i].id
                });
            } else {
                deviceTypesList.push({
                    "hasCustTemplate": false,
                    "deviceTypeLabel": deviceTypeLabel,
                    "deviceTypeName": deviceTypes[i].name,
                    "deviceTypeId": deviceTypes[i].id
                });
            }
        }

        viewModel.deviceTypesList = stringify(deviceTypesList);
    } else {
        log.error("Unable to fetch device types data");
        throw new Error("Unable to fetch device types!");
    }

    return viewModel;
}