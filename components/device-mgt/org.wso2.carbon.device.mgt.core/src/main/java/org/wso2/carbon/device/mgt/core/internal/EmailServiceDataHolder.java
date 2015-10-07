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

import org.wso2.carbon.device.mgt.core.service.EmailService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class EmailServiceDataHolder {

    private static EmailServiceDataHolder thisInstance = new EmailServiceDataHolder();
    private ConfigurationContextService configurationContextService;
    private EmailService emailServiceProvider;

    public static EmailServiceDataHolder getThisInstance() {
        return thisInstance;
    }

    public static void setThisInstance(EmailServiceDataHolder thisInstance) {
        EmailServiceDataHolder.thisInstance = thisInstance;
    }

    private EmailServiceDataHolder() {
    }

    public static EmailServiceDataHolder getInstance() {
        return thisInstance;
    }

    public ConfigurationContextService getConfigurationContextService() {
        if (configurationContextService == null) {
            throw new IllegalStateException("ConfigurationContext service is not initialized properly");
        }
        return configurationContextService;
    }

    public void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }
    public EmailService getEmailServiceProvider() {
        return emailServiceProvider;
    }

    public void setEmailServiceProvider(EmailService emailServiceProvider) {
        this.emailServiceProvider = emailServiceProvider;
    }
}
