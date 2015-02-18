/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.device.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.LicenseManager;
import org.wso2.carbon.device.mgt.core.LicenseManagerImpl;
import org.wso2.carbon.device.mgt.core.config.license.LicenseConfig;
import org.wso2.carbon.device.mgt.core.config.license.LicenseConfigurationManager;
import org.wso2.carbon.device.mgt.core.service.LicenseManagementService;
import org.wso2.carbon.device.mgt.core.util.LicenseManagerUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

public class LicenseManagementServiceComponent {

    private static Log log = LogFactory.getLog(LicenseManagementServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing license management core bundle");
            }
            LicenseManager licenseManager = new LicenseManagerImpl();
            LicenseManagementDataHolder.getInstance().setLicenseManager(licenseManager);

            if (log.isDebugEnabled()) {
                log.debug("Configuring default licenses to be used for device enrolment");
            }
            LicenseConfigurationManager.getInstance().initConfig();
            LicenseConfig licenseConfig = LicenseConfigurationManager.getInstance().getLicenseConfig();

             /* If -Dsetup option enabled then configure device management related default licenses  */
            String setupOption =
                    System.getProperty(DeviceManagementConstants.Common.PROPERTY_SETUP);
            if (setupOption != null) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "-Dsetup is enabled. Configuring default device management licenses " +
                                    "is about to begin");
                }
                /* It is required to specifically run the following code snippet as Super Tenant in order to use
             * Super tenant registry to initialize the underlying GenericArtifactManager corresponding to the same */
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain
                            (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    LicenseManagerUtil.addDefaultLicenses(licenseConfig);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Registering OSGi service 'LicenseManagementService'");
            }
            BundleContext bundleContext = componentContext.getBundleContext();
            bundleContext
                    .registerService(LicenseManagementService.class.getName(), new LicenseManagementService(), null);
            if (log.isDebugEnabled()) {
                log.debug("License management core bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            String msg = "Error occurred while initializing license management core bundle";
            log.error(msg, e);
        }
    }

    /**
     * Sets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Realm Service in license management");
        }
        LicenseManagementDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Unsets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Un setting Realm Service in license management");
        }
        LicenseManagementDataHolder.getInstance().setRealmService(null);
    }

    /**
     * Sets Registry Service.
     *
     * @param registryService An instance of RegistryService
     */
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Registry Service");
        }
        LicenseManagementDataHolder.getInstance().setRegistryService(registryService);
    }

    /**
     * Unsets Registry Service.
     *
     * @param registryService An instance of RegistryService
     */
    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Registry Service");
        }
        LicenseManagementDataHolder.getInstance().setRegistryService(null);
    }

}
