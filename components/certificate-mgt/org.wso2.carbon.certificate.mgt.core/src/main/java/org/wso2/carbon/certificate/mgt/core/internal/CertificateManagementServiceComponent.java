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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.certificate.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.certificate.mgt.core.config.CertificateConfigurationManager;
import org.wso2.carbon.certificate.mgt.core.config.CertificateManagementConfig;
import org.wso2.carbon.certificate.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOFactory;
import org.wso2.carbon.certificate.mgt.core.exception.CertificateManagementException;
import org.wso2.carbon.certificate.mgt.core.scep.SCEPManager;
import org.wso2.carbon.certificate.mgt.core.scep.SCEPManagerImpl;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementServiceImpl;
import org.wso2.carbon.certificate.mgt.core.util.CertificateManagementConstants;
import org.wso2.carbon.certificate.mgt.core.util.CertificateMgtSchemaInitializer;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;

/**
 * @scr.component name="org.wso2.carbon.certificate.mgt" immediate="true"
 * @scr.reference name="org.wso2.carbon.device.manager"
 * interface="org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDeviceManagementService"
 * unbind="unsetDeviceManagementService"
 */
public class CertificateManagementServiceComponent {

    private static Log log = LogFactory.getLog(CertificateManagementServiceComponent.class);

    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing certificate management core bundle");
            }
            CertificateConfigurationManager.getInstance().initConfig();
            CertificateManagementConfig config = CertificateConfigurationManager.getInstance().getCertificateManagementConfig();
            DataSourceConfig dsConfig = config.getCertificateManagementRepository().getDataSourceConfig();
            CertificateManagementDAOFactory.init(dsConfig);

            BundleContext bundleContext = componentContext.getBundleContext();

            /* If -Dsetup option enabled then create Certificate management database schema */
            String setupOption =
                    System.getProperty(CertificateManagementConstants.SETUP_PROPERTY);
            if (setupOption != null) {
                if (log.isDebugEnabled()) {
                    log.debug("-Dsetup is enabled. Certificate management repository schema initialization is about to " +
                              "begin");
                }
                this.setupDeviceManagementSchema(dsConfig);
            }
            bundleContext.registerService(CertificateManagementService.class.getName(),
                    CertificateManagementServiceImpl.getInstance(), null);

            bundleContext.registerService(SCEPManager.class.getName(),
                    new SCEPManagerImpl(), null);

            if (log.isDebugEnabled()) {
                log.debug("Certificate management core bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing certificate management core bundle", e);
        }
    }

    @SuppressWarnings("unused")
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    protected void setDeviceManagementService(DeviceManagementProviderService deviceManagerService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Device Management Service");
        }
        CertificateManagementDataHolder.getInstance().setDeviceManagementService(deviceManagerService);
    }

    protected void unsetDeviceManagementService(DeviceManagementProviderService deviceManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing Device Management Service");
        }
        CertificateManagementDataHolder.getInstance().setDeviceManagementService(null);
    }

    private void setupDeviceManagementSchema(DataSourceConfig config) throws CertificateManagementException {
        CertificateMgtSchemaInitializer initializer = new CertificateMgtSchemaInitializer(config);
        log.info("Initializing Certificate management repository database schema");
        try {
            initializer.createRegistryDatabase();
        } catch (Exception e) {
            throw new CertificateManagementException(
                    "Error occurred while initializing Certificate Management database schema", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Certificate management metadata repository schema has been successfully initialized");
        }
    }


}
