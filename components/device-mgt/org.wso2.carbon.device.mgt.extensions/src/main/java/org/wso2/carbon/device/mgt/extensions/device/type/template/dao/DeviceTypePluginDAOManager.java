/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.device.mgt.extensions.device.type.template.dao;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DeviceDetails;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class DeviceTypePluginDAOManager {

    private PluginDAO deviceTypePluginDAO;
    private DeviceTypeDAOHandler deviceTypeDAOHandler;
    private static final String DEFAULT_DATASOURCE_NAME = "jdbc/DM_DS";

    public DeviceTypePluginDAOManager(String datasourceName, DeviceDAODefinition deviceDAODefinition) {
        deviceTypeDAOHandler = new DeviceTypeDAOHandler(datasourceName);
        deviceTypePluginDAO = new DeviceTypePluginDAOImpl(deviceDAODefinition, deviceTypeDAOHandler);
    }

    public DeviceTypePluginDAOManager(String deviceType, DeviceDetails deviceDetails) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        try {
            deviceTypeDAOHandler = new DeviceTypeDAOHandler(DEFAULT_DATASOURCE_NAME);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        deviceTypePluginDAO = new PropertyBasedPluginDAOImpl(deviceDetails, deviceTypeDAOHandler, deviceType);
    }

    public PluginDAO getDeviceDAO() {
        return deviceTypePluginDAO;
    }

    public DeviceTypeDAOHandler getDeviceTypeDAOHandler() {
        return deviceTypeDAOHandler;
    }
}