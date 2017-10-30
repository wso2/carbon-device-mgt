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
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var deviceModule = require("/app/modules/business-controllers/device.js")["deviceModule"];
    var groupModule = require("/app/modules/business-controllers/group.js")["groupModule"];
    var types = {};

    types.isAuthorized = userModule.isAuthorized("/permission/admin/device-mgt/policies/manage");
    types.isAuthorizedViewUsers = userModule.isAuthorized("/permission/admin/device-mgt/roles/view");
    types.isAuthorizedViewRoles = userModule.isAuthorized("/permission/admin/device-mgt/users/view");
    types.isAuthorizedViewGroups = userModule.isAuthorized("/permission/admin/device-mgt/groups/view");
    types["types"] = [];

    var typesListResponse = deviceModule.getDeviceTypes();
    if (typesListResponse["status"] == "success") {
        for (var type in typesListResponse["content"]["deviceTypes"]) {
            var content = {};
            var deviceType = typesListResponse["content"]["deviceTypes"][type];
            content["name"] = deviceType;
            var configs = utility.getDeviceTypeConfig(deviceType);
            var deviceTypeLabel = deviceType;
            if (configs && configs[DTYPE_CONF_DEVICE_TYPE_KEY][DTYPE_CONF_DEVICE_TYPE_LABEL_KEY]) {
                deviceTypeLabel = configs[DTYPE_CONF_DEVICE_TYPE_KEY][DTYPE_CONF_DEVICE_TYPE_LABEL_KEY];
            }
            var policyWizardSrc = "/app/units/" + utility.getTenantedDeviceUnitName(deviceType, "policy-wizard");
            if (new File(policyWizardSrc).isExists()) {
                content["icon"] = utility.getDeviceThumb(deviceType);
                content["label"] = deviceTypeLabel;
                var policyOperationsTemplateSrc = policyWizardSrc + "/public/templates/" + deviceType + "-policy-operations.hbs";
                if (new File(policyOperationsTemplateSrc).isExists()) {
                    content["template"] = "/public/cdmf.unit.device.type." + deviceType +
                        ".policy-wizard/templates/" + deviceType + "-policy-operations.hbs";
                }
                var policyOperationsScriptSrc = policyWizardSrc + "/public/js/" + deviceType + "-policy-operations.js";
                if (new File(policyOperationsScriptSrc).isExists()) {
                    content["script"] = "/public/cdmf.unit.device.type." + deviceType +
                        ".policy-wizard/js/" + deviceType + "-policy-operations.js";;
                }
                var policyOperationsStylesSrc = policyWizardSrc + "/public/css/" + deviceType + "-policy-operations.css";
                if (new File(policyOperationsStylesSrc).isExists()) {
                    content["style"] = "/public/cdmf.unit.device.type." + deviceType +
                        ".policy-wizard/css/" + deviceType + "-policy-operations.css";;
                }
                types["types"].push(content);
            }
        }
    }

    var roles = userModule.getRoles();
    if (roles["status"] == "success") {
        types["roles"] = roles["content"];
    }
    types["groups"] = groupModule.getGroups();
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    types["isCloud"] = devicemgtProps.isCloud;

    return types;
}
