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
import org.wso2.carbon.email.sender.core.EmailSenderConfigurationFailedException;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

class EmailSenderAxis2ConfigContextObserver implements Axis2ConfigurationContextObserver {

    private static final Log log = LogFactory.getLog(EmailSenderAxis2ConfigContextObserver.class);

    @Override
    public void creatingConfigurationContext(int tenantId) {

    }

    @Override
    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        try {
            EmailUtils.setupEmailTemplates();
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

}
