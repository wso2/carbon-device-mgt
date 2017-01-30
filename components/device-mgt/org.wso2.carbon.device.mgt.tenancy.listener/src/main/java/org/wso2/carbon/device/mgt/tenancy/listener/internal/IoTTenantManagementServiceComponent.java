/*
 * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.device.mgt.tenancy.listener.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.tenancy.listener.core.IoTTenantManagementException;
import org.wso2.carbon.device.mgt.tenancy.listener.core.IoTTenantManagementListener;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**
 * @scr.component name="org.wso2.carbon.device.tenancy.listener" immediate="true"
 */
public class IoTTenantManagementServiceComponent {

    private static final Log log = LogFactory.getLog(IoTTenantManagementServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing IoTS tenant creation listener component...");
            }

            File iotTenantNotifierConfFile = new File("repository/conf/iot-tenant-notifier.properties");
            if (!iotTenantNotifierConfFile.exists()) {
                String msg = "IoT tenant creation notifier config file (" + iotTenantNotifierConfFile.getAbsolutePath() + ") does not exist.";
                log.error(msg);
                throw new IoTTenantManagementException(msg);
            }
            Properties tenantNotifierProps = new Properties();
            tenantNotifierProps.load(new FileReader(iotTenantNotifierConfFile));
            String iotBackendURL = tenantNotifierProps.getProperty("iot.backend");
            if (iotBackendURL == null || iotBackendURL.isEmpty()) {
                String msg = "IoTS backend URL is not specified for publishing tenant creation notifications.";
                log.error(msg);
                throw new IoTTenantManagementException(msg);
            }

            componentContext.getBundleContext().registerService(TenantMgtListener.class,
                    new IoTTenantManagementListener(iotBackendURL), null);
        } catch (Throwable e) {
            log.error("Error occurred while initializing IoTS tenant creation listener component.", e);
        }
    }
}


