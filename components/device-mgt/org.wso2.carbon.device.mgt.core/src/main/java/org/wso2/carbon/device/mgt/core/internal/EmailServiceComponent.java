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

package org.wso2.carbon.device.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.email.sender.EmailServiceProviderImpl;
import org.wso2.carbon.device.mgt.core.service.EmailService;
import org.wso2.carbon.device.mgt.core.service.EmailServiceImpl;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component component.name="org.wso2.carbon.device.emailmanager" immediate="true"
 * @scr.reference name="configurationcontext.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class EmailServiceComponent {

    private static Log log = LogFactory.getLog(EmailServiceComponent.class);

    /**
     * initialize the email service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing email service bundle");
            }

            DeviceConfigurationManager.getInstance().initConfig();
            DeviceManagementConfig config =
                    DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
		    /* Initializing Email Service Configurations */

            EmailService emailServiceProvider = new EmailServiceProviderImpl();
            EmailServiceDataHolder.getInstance().setEmailServiceProvider(emailServiceProvider); ;

            this.registerServices(context);

            if (log.isDebugEnabled()) {
                log.debug("Email management core bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing device management core bundle", e);
        }
    }
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        EmailServiceDataHolder.getInstance().setConfigurationContextService(configurationContextService);
    }
    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        EmailServiceDataHolder.getInstance().setConfigurationContextService(null);
    }

    private void registerServices(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Registering OSGi service Email Service Impl");
        }
        /* Registering Email Service */
        BundleContext bundleContext = componentContext.getBundleContext();
        bundleContext.registerService(EmailService.class.getName(), new EmailServiceImpl(), null);
    }

}
