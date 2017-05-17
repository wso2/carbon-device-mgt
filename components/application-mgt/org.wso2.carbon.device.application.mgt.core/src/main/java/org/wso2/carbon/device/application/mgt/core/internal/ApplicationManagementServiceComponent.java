/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.BundleContext;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManagementService;
import org.wso2.carbon.device.application.mgt.core.services.impl.ApplicationManagementServiceImpl;
import org.wso2.carbon.device.application.mgt.core.config.ApplicationConfigurationManager;
import org.wso2.carbon.device.application.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;

import javax.naming.NamingException;

/**
 * @scr.component name="org.wso2.carbon.application.mgt.service" immediate="true"
 * @scr.reference name="org.wso2.carbon.device.manager"
 * interface="org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDeviceManagementService"
 * unbind="unsetDeviceManagementService"
 */
public class ApplicationManagementServiceComponent {

    private static Log log = LogFactory.getLog(ApplicationManagementServiceComponent.class);


    protected void activate(ComponentContext componentContext) throws NamingException {
        BundleContext bundleContext = componentContext.getBundleContext();
        ApplicationManagementService applicationManagementService = new ApplicationManagementServiceImpl();
        ApplicationManagementDataHolder.getInstance().setApplicationManagementService(applicationManagementService);
        bundleContext.registerService(ApplicationManagementService.class.getName(),
                applicationManagementService, null);


        DataSourceConfig dataSourceConfig  = ApplicationConfigurationManager.getInstance()
                .getApplicationManagerConfiguration().getApplicationManagerRepository().getDataSourceConfig();
        ApplicationManagementDAOFactory.init(dataSourceConfig);

        log.info("ApplicationManagement core bundle has been successfully initialized");

        if (log.isDebugEnabled()) {
            log.debug("ApplicationManagement core bundle has been successfully initialized");
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    protected void setDeviceManagementService(DeviceManagementProviderService deviceManagementProviderService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Application Management OSGI Manager");
        }
        ApplicationManagementDataHolder.getInstance().setDeviceManagementService(deviceManagementProviderService);
    }

    protected void unsetDeviceManagementService(DeviceManagementProviderService deviceManagementProviderService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing Application Management OSGI Manager");
        }
        ApplicationManagementDataHolder.getInstance().setDeviceManagementService(null);
    }
}
