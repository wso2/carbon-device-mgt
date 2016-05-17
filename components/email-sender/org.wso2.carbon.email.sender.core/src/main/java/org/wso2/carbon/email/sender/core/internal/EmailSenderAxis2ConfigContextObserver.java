/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.email.sender.core.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.email.sender.core.EmailSenderConfigurationFailedException;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FilenameFilter;

public class EmailSenderAxis2ConfigContextObserver implements Axis2ConfigurationContextObserver {

    private static final String EMAIL_TEMPLATE_DIR_RELATIVE_REGISTRY_PATH = "email-templates";
    private static final Log log = LogFactory.getLog(EmailSenderAxis2ConfigContextObserver.class);

    @Override
    public void creatingConfigurationContext(int tenantId) {

    }

    @Override
    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        try {
            this.setupEmailTemplates();
        } catch (EmailSenderConfigurationFailedException e) {
            log.error("Error occurred while setting up email templates", e);
        }
    }

    @Override
    public void terminatingConfigurationContext(ConfigurationContext configurationContext) {

    }

    @Override
    public void terminatedConfigurationContext(ConfigurationContext configurationContext) {

    }

    private void setupEmailTemplates() throws EmailSenderConfigurationFailedException {
        File templateDir =
                new File(CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources"
                         + File.separator + "email-templates");
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
                        CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_CONFIGURATION);
                if (!registry.resourceExists(EMAIL_TEMPLATE_DIR_RELATIVE_REGISTRY_PATH)) {
                    Collection collection = registry.newCollection();
                    registry.put(EMAIL_TEMPLATE_DIR_RELATIVE_REGISTRY_PATH, collection);
                    for (File template : templates) {
                        Resource resource = registry.newResource();
                        resource.setContent(template);
                        registry.put(EMAIL_TEMPLATE_DIR_RELATIVE_REGISTRY_PATH + "/" + template.getName(), resource);
                    }
                } else {
                    for (File template : templates) {
                        if (!registry.resourceExists(
                                EMAIL_TEMPLATE_DIR_RELATIVE_REGISTRY_PATH + "/" + template.getName())) {
                            Resource resource = registry.newResource();
                            resource.setContent(template);
                            registry.put(
                                    EMAIL_TEMPLATE_DIR_RELATIVE_REGISTRY_PATH + "/" + template.getName(), resource);
                        }
                    }
                }
            } catch (RegistryException e) {
                throw new EmailSenderConfigurationFailedException("Error occurred while setting up email templates", e);
            }
        }
    }

}
