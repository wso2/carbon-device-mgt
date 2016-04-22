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
    var log = new Log("/app/modules/utility.js");
    var JavaClass = Packages.java.lang.Class;
    var PrivilegedCarbonContext = Packages.org.wso2.carbon.context.PrivilegedCarbonContext;

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

    publicMethods.getConfigurationService = function () {
        return getOsgiService('org.wso2.carbon.device.mgt.iot.service.ConfigurationService');
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

    publicMethods.getIoTServerConfig = function (configName) {
        var path = "/config/iot-config.json";
        var file = new File(path);
        try {
            file.open("r");
            var content = file.readAll();
        } catch (err) {
            log.error("Error while reading IoT server config file `" + path + "`: " + err);
        } finally {
            file.close();
        }
        var json = parse(content);
        return json[configName];
    };

    publicMethods.getDeviceTypeConfig = function (deviceType) {
        var JFile = Packages.java.io.File;
        var sep = JFile.separator;

        var systemProcess = require('process');
        var parent = 'file:///' + (systemProcess.getProperty('jaggery.home') || systemProcess.getProperty('carbon.home')).replace(/[\\]/g, '/').replace(/^[\/]/g, '');

        if (deviceType in deviceTypeConfigMap) {
            return deviceTypeConfigMap[deviceType];
        }
        var deviceTypeConfig;
        var deviceTypeConfigFile = new File(parent + sep + "repository" + sep + "conf" + sep
                                            + "device-types" + sep + deviceType + ".json");
        if (deviceTypeConfigFile.isExists()) {
            try {
                deviceTypeConfigFile.open("r");
                deviceTypeConfig = parse(deviceTypeConfigFile.readAll());
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
        var iconPath = "/app/units/cdmf.unit.device.type."
                       + deviceType + ".type-view/public/images/operations/" + operation + ".png";
        var icon = new File(iconPath);
        if (icon.isExists()) {
            return "public/cdmf.unit.device.type." + deviceType + ".type-view/images/operations/" + operation + ".png";
        } else {
            return null;
        }
    };

    publicMethods.getDeviceThumb = function (deviceType) {
        var iconPath = "/app/units/cdmf.unit.device.type."
                       + deviceType + ".type-view/public/images/thumb.png";
        var icon = new File(iconPath);
        if (icon.isExists()) {
            return "/devicemgt/public/cdmf.unit.device.type." + deviceType + ".type-view/images/thumb.png";
        } else {
            return null;
        }
    };

    return publicMethods;
}();
