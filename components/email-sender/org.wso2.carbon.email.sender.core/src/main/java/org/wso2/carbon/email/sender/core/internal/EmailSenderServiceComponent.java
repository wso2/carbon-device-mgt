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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.email.sender.core.EmailSenderConfig;
import org.wso2.carbon.email.sender.core.EmailSenderConfigurationFailedException;
import org.wso2.carbon.email.sender.core.service.EmailSenderService;
import org.wso2.carbon.email.sender.core.service.EmailSenderServiceImpl;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

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

    private static final String EMAIL_TEMPLATE_DIR_RELATIVE_REGISTRY_PATH = "/email-templates";

    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing email sender core bundle");
            }
            /* Initializing email sender configuration */
            EmailSenderConfig.init();

            /* Setting up default email templates */
            this.setupEmailTemplates();

            /* Registering declarative service instances exposed by EmailSenderServiceComponent */
            this.registerServices(componentContext);

            if (log.isDebugEnabled()) {
                log.debug("Email sender core bundle has been successfully initialized");
            }
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

    private void setupEmailTemplates() throws EmailSenderConfigurationFailedException {
        File templateDir =
                new File(CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                        "resources" + File.separator + "email-templates");
        if (!templateDir.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("The directory that is expected to use as the container for all email templates is not " +
                        "available. Therefore, no template is uploaded to the registry");
            }
        }
        if (templateDir.canRead()) {
            File[] templates = templateDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    name = name.toLowerCase();
                    return name.endsWith(".vm");
                }
            });
            try {
                Registry registry =
                        EmailSenderDataHolder.getInstance().getRegistryService().getConfigSystemRegistry();
                if (!registry.resourceExists(EMAIL_TEMPLATE_DIR_RELATIVE_REGISTRY_PATH)) {
                    Collection collection = registry.newCollection();
                    registry.put(EMAIL_TEMPLATE_DIR_RELATIVE_REGISTRY_PATH, collection);
                    for (File template : templates) {
                        Resource resource = registry.newResource();
                        String contents = FileUtils.readFileToString(template);
                        resource.setContent(contents.getBytes());
                        registry.put(EMAIL_TEMPLATE_DIR_RELATIVE_REGISTRY_PATH + "/" + template.getName(), resource);
                    }
                } else {
                    /* Existence of a given resource is not checked consciously, before performing registry.put() below.
                    *  The rationale is that, the only less expensive way that one can check if a resource exists is
                    *  that through registry.resourceExists(), which only checks if 'some' resource exists at the given
                    *  registry path. However, this does not capture scenarios where there can be updated contents to
                    *  the same resource of which the path hasn't changed after it has been initialized for the first
                    *  time. Therefore, whenever the server starts-up, all email templates are updated just to avoid
                    *  the aforementioned problem */
                    for (File template : templates) {
                        Resource resource = registry.newResource();
                        String contents = FileUtils.readFileToString(template);
                        resource.setContent(contents.getBytes());
                        registry.put(
                                EMAIL_TEMPLATE_DIR_RELATIVE_REGISTRY_PATH + "/" + template.getName(), resource);
                    }
                }
            } catch (RegistryException e) {
                throw new EmailSenderConfigurationFailedException("Error occurred while setting up email templates", e);
            } catch (FileNotFoundException e) {
                throw new EmailSenderConfigurationFailedException("Error occurred while writing template file " +
                        "contents as an input stream of a resource", e);
            } catch (IOException e) {
                throw new EmailSenderConfigurationFailedException("Error occurred while serializing file " +
                        "contents to a string", e);
            }
        }
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
