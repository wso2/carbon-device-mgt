/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.email.sender.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.email.sender.core.EmailSenderConfig;
import org.wso2.carbon.email.sender.core.service.EmailSenderService;
import org.wso2.carbon.email.sender.core.service.EmailSenderServiceImpl;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.email.sender.EmailSenderServiceComponent" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="0..1"
 * policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class EmailSenderServiceComponent {

    private static Log log = LogFactory.getLog(EmailSenderServiceComponent.class);

    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing email sender core bundle");
            }
            /* Initializing email sende configuration */
            EmailSenderConfig.init();

            /* Setting up default email templates */
            EmailUtils.setupEmailTemplates();

            /* Registering declarative service instances exposed by EmailSenderServiceComponent */
            this.registerServices(componentContext);

            if (log.isDebugEnabled()) {
                log.debug("Email sender core bundle has been successfully initialized");
            }
            componentContext.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(),
                                                                new EmailSenderAxis2ConfigContextObserver(), null);
        } catch (Throwable e) {
            log.error("Error occurred while initializing email sender core bundle", e);
        }
    }

    @SuppressWarnings("unused")
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    private void registerServices(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Registering email sender service");
        }
        EmailSenderService emailServiceProvider = new EmailSenderServiceImpl();
        EmailSenderDataHolder.getInstance().setEmailServiceProvider(emailServiceProvider);
        componentContext.getBundleContext().registerService(EmailSenderService.class, emailServiceProvider, null);
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
        EmailSenderDataHolder.getInstance().setRegistryService(registryService);
    }

    /**
     * Unsets Registry Service.
     *
     * @param registryService An instance of RegistryService
     */
    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Un setting Registry Service");
        }
        EmailSenderDataHolder.getInstance().setRegistryService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting ConfigurationContextService");
        }
        EmailSenderDataHolder.getInstance().setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("Un-setting ConfigurationContextService");
        }
        EmailSenderDataHolder.getInstance().setConfigurationContextService(null);
    }

}
