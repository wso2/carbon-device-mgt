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
    var DTYPE_CONF_DEVICE_TYPE_KEY = "deviceType";
    var DTYPE_CONF_DEVICE_TYPE_LABEL_KEY = "label";

    var utility = require("/app/modules/utility.js").utility;
    var deviceModule = require("/app/modules/device.js").deviceModule;

    var types = {};
    types["types"] = [];
    var typesListResponse = deviceModule.getDeviceTypes();
    if (typesListResponse["status"] == "success") {
        for (var type in typesListResponse["content"]) {
            var deviceType = typesListResponse["content"][type]["name"];
            var configs = utility.getDeviceTypeConfig(deviceType);
            var deviceTypeLabel = deviceType;
            if (configs && configs[DTYPE_CONF_DEVICE_TYPE_KEY][DTYPE_CONF_DEVICE_TYPE_LABEL_KEY]) {
                deviceTypeLabel = configs[DTYPE_CONF_DEVICE_TYPE_KEY][DTYPE_CONF_DEVICE_TYPE_LABEL_KEY];
            }
            var policyWizard = new File("/app/units/" + utility.getTenantedDeviceUnitName(deviceType, "policy-wizard"));
            if(policyWizard.isExists()){
                typesListResponse["content"][type]["icon"] = utility.getDeviceThumb(deviceType);
                typesListResponse["content"][type]["label"] = deviceTypeLabel;
                types["types"].push(typesListResponse["content"][type]);
            }
        }
    }
    return types;
}
