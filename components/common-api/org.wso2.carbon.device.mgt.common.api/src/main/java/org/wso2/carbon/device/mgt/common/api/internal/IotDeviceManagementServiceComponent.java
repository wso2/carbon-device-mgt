/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.device.mgt.common.api.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.device.mgt.common.api.DeviceController;
import org.wso2.carbon.device.mgt.common.api.analytics.statistics.IoTEventsStatisticsClient;
import org.wso2.carbon.device.mgt.common.api.config.devicetype.IotDeviceTypeConfigurationManager;
import org.wso2.carbon.device.mgt.common.api.config.devicetype.datasource.IotDeviceTypeConfig;
import org.wso2.carbon.device.mgt.common.api.startup.StartupUrlPrinter;
import org.wso2.carbon.device.mgt.common.api.util.iotdevice.dao.IotDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.common.api.UserManagement;
import org.wso2.carbon.device.mgt.common.api.service.DeviceTypeService;
import org.wso2.carbon.device.mgt.common.api.service.DeviceTypeServiceImpl;
import org.wso2.carbon.device.mgt.common.api.util.iotdevice.exception.IotDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.common.api.config.server.DeviceCloudConfigManager;
import org.wso2.carbon.device.mgt.common.api.analytics.statistics.IoTUsageStatisticsClient;
import org.wso2.carbon.device.mgt.common.api.util.iotdevice.dao.util.IotDeviceManagementDAOUtil;
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
 */
public class IotDeviceManagementServiceComponent {

    private static final Log log = LogFactory.getLog(IotDeviceManagementServiceComponent.class);
	public static ConfigurationContextService configurationContextService;
    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Activating Iot Device Management Service Component");
        }
        try {


            BundleContext bundleContext = ctx.getBundleContext();              /* Initialize the data source configuration */
			DeviceCloudConfigManager.getInstance().initConfig();
			IotDeviceTypeConfigurationManager.getInstance().initConfig();
			Map<String, IotDeviceTypeConfig> dsConfigMap =
					IotDeviceTypeConfigurationManager.getInstance().getIotDeviceTypeConfigMap();

			IotDeviceManagementDAOFactory.init(dsConfigMap);



			String setupOption = System.getProperty("setup");
			if (setupOption != null) {
				if (log.isDebugEnabled()) {
					log.debug(
							"-Dsetup is enabled. Iot Device management repository schema initialization is about " +
									"to begin");
				}
				try {
					for (String pluginType : dsConfigMap.keySet()){
						IotDeviceManagementDAOUtil
								.setupIotDeviceManagementSchema(
										IotDeviceManagementDAOFactory.getDataSourceMap
												().get(pluginType));
					}
				} catch (IotDeviceMgtPluginException e) {
					log.error(
							"Exception occurred while initializing mobile device management database schem ",
							e);
				}
			}

			IoTCommonDataHolder.getInstance().initialize();

			//TODO: handle

			DeviceController.init();
            IoTUsageStatisticsClient.initializeDataSource();
			IoTEventsStatisticsClient.initializeDataSource();
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

	protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
		if (log.isDebugEnabled()) {
			log.debug("Setting ConfigurationContextService");
		}

		IotDeviceManagementServiceComponent.configurationContextService=configurationContextService;

	}

	protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
		if (log.isDebugEnabled()) {
			log.debug("Un-setting ConfigurationContextService");
		}
		IotDeviceManagementServiceComponent.configurationContextService=null;
	}

	/**
	 * Sets Realm Service
	 * @param realmService associated realm service reference
	 */
	protected void setRealmService(RealmService realmService) {
		if (log.isDebugEnabled()) {
			log.debug("Setting Realm Service");

		}
		UserManagement userManagement= new UserManagement();
		userManagement.setRealmService(realmService);

	}

	/**
	 * Unsets Realm Service
	 * @param realmService associated realm service reference
	 */
	protected void unsetRealmService(RealmService realmService) {
		if (log.isDebugEnabled()) {
			log.debug("Unsetting Realm Service");
		}
		UserManagement userManagement= new UserManagement();
		userManagement.setRealmService(realmService);
	}

}
