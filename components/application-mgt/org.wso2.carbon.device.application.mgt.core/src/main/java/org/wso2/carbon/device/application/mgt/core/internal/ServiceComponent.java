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
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.application.mgt.common.exception.InvalidConfigurationException;
import org.wso2.carbon.device.application.mgt.common.services.*;
import org.wso2.carbon.device.application.mgt.core.config.ConfigurationManager;
import org.wso2.carbon.device.application.mgt.core.dao.common.DAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.util.ApplicationManagementUtil;
import org.wso2.carbon.device.application.mgt.core.util.Constants;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

import javax.naming.NamingException;

/**
 * @scr.component name="org.wso2.carbon.application.mgt.service" immediate="true"
 * @scr.reference name="org.wso2.carbon.device.manager"
 * interface="org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDeviceManagementService"
 * unbind="unsetDeviceManagementService"
 * @scr.reference name="realm.service"
 * immediate="true"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="datasource.service"
 * interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDataSourceService"
 * unbind="unsetDataSourceService"
 */
public class ServiceComponent {

    private static Log log = LogFactory.getLog(ServiceComponent.class);


    protected void activate(ComponentContext componentContext) throws NamingException {
        BundleContext bundleContext = componentContext.getBundleContext();
        try {
            String datasourceName = ConfigurationManager.getInstance().getConfiguration().getDatasourceName();
            DAOFactory.init(datasourceName);

            ApplicationManager applicationManager = ApplicationManagementUtil.getApplicationManagerInstance();
            DataHolder.getInstance().setApplicationManager(applicationManager);
            bundleContext.registerService(ApplicationManager.class.getName(), applicationManager, null);

            ApplicationReleaseManager applicationReleaseManager = ApplicationManagementUtil
                    .getApplicationReleaseManagerInstance();
            DataHolder.getInstance().setReleaseManager(applicationReleaseManager);
            bundleContext.registerService(ApplicationReleaseManager.class.getName(), applicationReleaseManager, null);

            CategoryManager categoryManager = ApplicationManagementUtil.getCategoryManagerInstance();
            DataHolder.getInstance().setCategoryManager(categoryManager);
            bundleContext.registerService(CategoryManager.class.getName(), categoryManager, null);

            CommentsManager commentsManager = ApplicationManagementUtil.getCommentsManagerInstance();
            DataHolder.getInstance().setCommentsManager(commentsManager);
            bundleContext.registerService(CommentsManager.class.getName(), commentsManager, null);

            LifecycleStateManager lifecycleStateManager = ApplicationManagementUtil.getLifecycleStateManagerInstance();
            DataHolder.getInstance().setLifecycleStateManager(lifecycleStateManager);
            bundleContext.registerService(LifecycleStateManager.class.getName(), lifecycleStateManager, null);

            PlatformManager platformManager = ApplicationManagementUtil.getPlatformManagerInstance();
            DataHolder.getInstance().setPlatformManager(platformManager);
            bundleContext.registerService(PlatformManager.class.getName(), platformManager, null);

            SubscriptionManager subscriptionManager = ApplicationManagementUtil.getSubscriptionManagerInstance();
            DataHolder.getInstance().setSubscriptionManager(subscriptionManager);
            bundleContext.registerService(SubscriptionManager.class.getName(), subscriptionManager, null);

            VisibilityManager visibilityManager = ApplicationManagementUtil.getVisibilityManagerInstance();
            DataHolder.getInstance().setVisibilityManager(visibilityManager);
            bundleContext.registerService(VisibilityManager.class.getName(), visibilityManager, null);

            ApplicationStorageManager applicationStorageManager = ApplicationManagementUtil
                    .getApplicationStorageManagerInstance();
            DataHolder.getInstance().setApplicationStorageManager(applicationStorageManager);
            bundleContext.registerService(ApplicationStorageManager.class.getName(), applicationStorageManager, null);

            PlatformStorageManager platformStorageManager = ApplicationManagementUtil
                    .getPlatformStorageManagerInstance();
            DataHolder.getInstance().setPlatformStorageManager(platformStorageManager);
            bundleContext.registerService(PlatformStorageManager.class.getName(), platformStorageManager, null);

            bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                    new PlatformManagementAxis2ConfigurationObserverImpl(), null);

            DAOFactory.init(datasourceName);
            DAOFactory.initDatabases();
            log.info("ApplicationManagement core bundle has been successfully initialized");
        } catch (InvalidConfigurationException e) {
            log.error("Error while activating Application Management core component. ", e);
        } catch (ApplicationManagementDAOException e) {
            log.error("Error while activating Application Management core component.Failed to create the database ", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    protected void setDeviceManagementService(DeviceManagementProviderService deviceManagementProviderService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Application Management OSGI Manager");
        }
        DataHolder.getInstance().setDeviceManagementService(deviceManagementProviderService);
    }

    protected void unsetDeviceManagementService(DeviceManagementProviderService deviceManagementProviderService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing Application Management OSGI Manager");
        }
        DataHolder.getInstance().setDeviceManagementService(null);
    }

    protected void setRealmService(RealmService realmService) {
        DataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        DataHolder.getInstance().setRealmService(null);
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        /*Not implemented. Not needed but to make sure the datasource service are registered, as it is needed create
         databases. */
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        /*Not implemented. Not needed but to make sure the datasource service are registered, as it is needed to create
         databases.*/
    }
}
