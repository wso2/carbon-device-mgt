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

package org.wso2.carbon.device.mgt.common.impl.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.device.mgt.common.impl.DeviceController;
import org.wso2.carbon.device.mgt.common.impl.UserManagement;
import org.wso2.carbon.device.mgt.common.impl.analytics.statistics.DeviceMgtEventsStatisticsClient;
import org.wso2.carbon.device.mgt.common.impl.analytics.statistics.DeviceMgtUsageStatisticsClient;
import org.wso2.carbon.device.mgt.common.impl.config.devicetype.DeviceTypeConfigurationManager;
import org.wso2.carbon.device.mgt.common.impl.config.devicetype.datasource.DeviceTypeConfig;
import org.wso2.carbon.device.mgt.common.impl.config.server.DeviceCloudConfigManager;
import org.wso2.carbon.device.mgt.common.impl.service.DeviceTypeService;
import org.wso2.carbon.device.mgt.common.impl.service.DeviceTypeServiceImpl;
import org.wso2.carbon.device.mgt.common.impl.startup.StartupUrlPrinter;
import org.wso2.carbon.device.mgt.common.impl.util.cdmdevice.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.common.impl.util.cdmdevice.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.common.impl.util.cdmdevice.exception.DeviceMgtPluginException;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.Map;

/**
 * @scr.component name="org.wso2.carbon.device.mgt.iot.common.internal.IotDeviceManagementServiceComponent"
 * immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="org.wso2.carbon.ndatasource"
 * interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDataSourceService"
 * unbind="unsetDataSourceService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="0..1"
 * policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="databridge.component"
 * interface="org.wso2.carbon.databridge.core.DataBridgeReceiverService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDataBridgeReceiverService"
 * unbind="unsetDataBridgeReceiverService"
 */
public class DeviceManagementServiceComponent {

    private static final Log log = LogFactory.getLog(DeviceManagementServiceComponent.class);
    public static ConfigurationContextService configurationContextService;

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Activating Iot Device Management Service Component");
        }
        try {
            BundleContext bundleContext = ctx.getBundleContext();              /* Initialize the data source configuration */
            DeviceCloudConfigManager.getInstance().initConfig();
            DeviceTypeConfigurationManager.getInstance().initConfig();
            Map<String, DeviceTypeConfig> dsConfigMap =
                    DeviceTypeConfigurationManager.getInstance().getDeviceTypeConfigMap();

            DeviceManagementDAOFactory.init(dsConfigMap);

            String setupOption = System.getProperty("setup");
            if (setupOption != null) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "-Dsetup is enabled. Iot Device management repository schema initialization is about " +
                            "to begin");
                }
                try {
                    for (String pluginType : dsConfigMap.keySet()) {
                        DeviceManagementDAOUtil
                                .setupDeviceManagementSchema(
                                        DeviceManagementDAOFactory.getDataSourceMap
                                                ().get(pluginType));
                    }
                } catch (DeviceMgtPluginException e) {
                    log.error(
                            "Exception occurred while initializing device management database schema",
                            e);
                }
            }

            DeviceMgtCommonDataHolder.getInstance().initialize();

            //TODO: handle

            DeviceController.init();
            DeviceMgtUsageStatisticsClient.initializeDataSource();
            DeviceMgtEventsStatisticsClient.initializeDataSource();
            UserManagement.registerApiAccessRoles();


            bundleContext.registerService(DeviceTypeService.class.getName(),
                                          new DeviceTypeServiceImpl(), null);

            if (log.isDebugEnabled()) {
                log.debug("Iot Device Management Service Component has been successfully activated");
            }

            bundleContext.registerService(ServerStartupObserver.class, new StartupUrlPrinter(), null);
        } catch (Throwable e) {
            log.error("Error occurred while activating Iot Device Management Service Component", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Iot Device Management Service Component");
        }

    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        /* This is to avoid iot device management component getting initialized before the
		underlying datasources
        are registered */
        if (log.isDebugEnabled()) {
            log.debug("Data source service set to mobile service component");
        }
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        //do nothing
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting ConfigurationContextService");
        }

        DeviceManagementServiceComponent.configurationContextService = configurationContextService;

    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("Un-setting ConfigurationContextService");
        }
        DeviceManagementServiceComponent.configurationContextService = null;
    }

    /**
     * Sets Realm Service
     *
     * @param realmService associated realm service reference
     */
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Realm Service");

        }
        UserManagement.setRealmService(realmService);

    }

    /**
     * Unsets Realm Service
     *
     * @param realmService associated realm service reference
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Realm Service");
        }
        UserManagement.setRealmService(realmService);
    }

    /**
     * Sets DataBridge Receiver Service
     *
     * @param dataBridgeReceiverService associated DataBridge service reference
     */
    protected void setDataBridgeReceiverService(
            DataBridgeReceiverService dataBridgeReceiverService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting DataBridge Receiver Service");
        }
        DeviceMgtCommonDataHolder.setDataBridgeReceiverService(dataBridgeReceiverService);
    }

    /**
     * Unsets Realm Service
     *
     * @param dataBridgeReceiverService associated DataBridge service reference
     */
    protected void unsetDataBridgeReceiverService(
            DataBridgeReceiverService dataBridgeReceiverService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting DataBridge Receiver Service");
        }
        DeviceMgtCommonDataHolder.setDataBridgeReceiverService(null);
    }
}
