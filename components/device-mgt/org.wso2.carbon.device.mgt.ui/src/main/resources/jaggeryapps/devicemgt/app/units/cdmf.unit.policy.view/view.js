/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function onRequest(context) {
    var utility = require("/app/modules/utility.js").utility;
    var page = {};
    var deviceType = request.getParameter("deviceType");
    var policyViewSrc = "/app/units/" + utility.getTenantedDeviceUnitName(deviceType, "policy-view");
    if (new File(policyViewSrc).isExists()) {
        var policyOperationsTemplateSrc = policyViewSrc + "/public/templates/" + deviceType + "-policy-view.hbs";
        if (new File(policyOperationsTemplateSrc).isExists()) {
            page.template = "/public/cdmf.unit.device.type." + deviceType + ".policy-view/templates/" + deviceType +
                "-policy-view.hbs";
        }
        var policyOperationsScriptSrc = policyViewSrc + "/public/js/" + deviceType + "-policy-view.js";
        if (new File(policyOperationsScriptSrc).isExists()) {
            page.script = "/public/cdmf.unit.device.type." + deviceType + ".policy-view/js/" + deviceType +
                "-policy-view.js";
        }
        var policyOperationsStylesSrc = policyViewSrc + "/public/css/" + deviceType + "-policy-view.css";
        if (new File(policyOperationsStylesSrc).isExists()) {
            page.style = "/public/cdmf.unit.device.type." + deviceType + ".policy-view/css/" + deviceType +
                "-policy-view.css";
        }
    }
    page.isAuthorized = userModule.isAuthorized("/permission/admin/device-mgt/policies/view");
    return page;
}