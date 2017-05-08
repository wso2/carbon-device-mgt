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

var utility;
utility = function () {

    var constants = require('/app/modules/constants.js');
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var log = new Log("/app/modules/utility.js");
    var JavaClass = Packages.java.lang.Class;
    var PrivilegedCarbonContext = Packages.org.wso2.carbon.context.PrivilegedCarbonContext;
    var server = require("carbon")["server"];

    var getOsgiService = function (className) {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(JavaClass.forName(className));
    };

    var deviceTypeConfigMap = {};

    var publicMethods = {};

    publicMethods.startTenantFlow = function (userInfo) {
        var context, carbon = require('carbon');
        PrivilegedCarbonContext.startTenantFlow();
        context = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        context.setTenantDomain(carbon.server.tenantDomain({
            tenantId: userInfo.tenantId
        }));
        context.setTenantId(userInfo.tenantId);
        context.setUsername(userInfo.username || null);
    };

    publicMethods.endTenantFlow = function () {
        PrivilegedCarbonContext.endTenantFlow();
    };

    publicMethods.getDeviceManagementService = function () {
        return getOsgiService('org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService');
    };

    publicMethods.getUserManagementService = function () {
        return getOsgiService("org.wso2.carbon.device.mgt.user.core.UserManager");
    };

    publicMethods.getPolicyManagementService = function () {
        return getOsgiService("org.wso2.carbon.policy.mgt.core.PolicyManagerService");
    };

    publicMethods.getDeviceTypeConfig = function (deviceType) {
        var unitName = publicMethods.getTenantedDeviceUnitName(deviceType, "type-view");

        if (deviceType in deviceTypeConfigMap) {
            return deviceTypeConfigMap[deviceType];
        }
        var deviceTypeConfig;
        var deviceTypeConfigFile = new File("/app/units/" + unitName + "/private/config.json");
        if (deviceTypeConfigFile.isExists()) {
            try {
                deviceTypeConfigFile.open("r");
                var config = deviceTypeConfigFile.readAll();
                config = config.replace("%https.ip%", server.address("https"));
                config = config.replace("%http.ip%", server.address("http"));
                deviceTypeConfig = parse(config);
            } catch (err) {
                log.error("Error while reading device config file for `" + deviceType + "`: " + err);
            } finally {
                deviceTypeConfigFile.close();
            }
        }
        deviceTypeConfigMap[deviceType] = deviceTypeConfig;
        return deviceTypeConfig;
    };

    publicMethods.getOperationIcon = function (deviceType, operation) {
        var unitName = publicMethods.getTenantedDeviceUnitName(deviceType, "type-view");
        var iconPath = "/app/units/" + unitName + "/public/images/operations/" + operation + ".png";
        var icon = new File(iconPath);
        if (icon.isExists()) {
            return devicemgtProps["appContext"] + "public/" + unitName + "/images/operations/" + operation + ".png";
        } else {
            return null;
        }
    };

    publicMethods.getDeviceThumb = function (deviceType) {
        var unitName = publicMethods.getTenantedDeviceUnitName(deviceType, "type-view");
        var iconPath = "/app/units/" + unitName + "/public/images/thumb.png";
        var icon = new File(iconPath);
        if (icon.isExists()) {
            return devicemgtProps["appContext"] + "public/" + unitName + "/images/thumb.png";
        } else {
            return null;
        }
    };

    publicMethods.getTenantedDeviceUnitName = function (deviceType, unitPostfix) {
        var user = session.get(constants.USER_SESSION_KEY);
        if (!user) {
            log.error("User object was not found in the session");
            throw constants.ERRORS.USER_NOT_FOUND;
        }
        var unitName = user.domain + ".cdmf.unit.device.type." + deviceType + "." + unitPostfix;
        if (new File("/app/units/" + unitName).isExists()) {
            return unitName;
        }
        unitName = "cdmf.unit.device.type." + deviceType + "." + unitPostfix;
        if (new File("/app/units/" + unitName).isExists()) {
            return unitName;
        }
        return null;
    };

    publicMethods.getDeviceTypesScopesList = function () {
        var dirs = new File("/app/units/").listFiles();
        var scopesList = [];
        for (var i = 0; i < dirs.length; i++) {
            var unitName = dirs[i].getName();
            if (unitName.match(/^cdmf\.unit\.device\.type\..*\.type-view$/g)) {
                var deviceTypeConfigFile = new File("/app/units/" + unitName + "/private/config.json");
                if (deviceTypeConfigFile.isExists()) {
                    try {
                        deviceTypeConfigFile.open("r");
                        var config = deviceTypeConfigFile.readAll();
                        config = config.replace("%https.ip%", server.address("https"));
                        config = config.replace("%http.ip%", server.address("http"));
                        var deviceTypeConfig = parse(config);
                        if (deviceTypeConfig.deviceType && deviceTypeConfig.deviceType.scopes) {
                            scopesList = scopesList.concat(deviceTypeConfig.deviceType.scopes);
                        }
                    } catch (err) {
                        log.error("Error while reading device config file for `" + deviceType + "`: " + err);
                    } finally {
                        deviceTypeConfigFile.close();
                    }
                }
            }
        }
        return scopesList;
    };


    /**
     * Escapes special characters such as <,>,',",...etc
     * This will prevent XSS attacks upon JSON.
     * @param text
     * @returns {*}
     */
    publicMethods.encodeJson = function (text) {
        if (text) {
            return text
                .replace(/\\u003c/g, "&lt;")
                .replace(/</g, "&lt;")
                .replace(/\\u003e/g, "&gt;")
                .replace(/>/g, "&gt;")
                .replace(/\\u0027/g, "&#39;")
                .replace(/'/g, "&#39;")
                .replace(/\\"/g, "&quot;")
                .replace(/\\u0022/g, "&quot;");
        } else {
            return "";
        }
    };

    return publicMethods;
}();
