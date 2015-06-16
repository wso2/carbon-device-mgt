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

package org.wso2.carbon.device.mgt.core.email.sender;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.mail.MailConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EmailMessageProperties;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.config.email.EmailConfigurations;
import org.wso2.carbon.device.mgt.core.internal.EmailServiceDataHolder;
import org.wso2.carbon.device.mgt.core.service.EmailService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EmailServiceProviderImpl implements EmailService {

    private static ThreadPoolExecutor threadPoolExecutor;

    static {
        EmailConfigurations emailConfig =
                DeviceConfigurationManager.getInstance().getDeviceManagementConfig().
                        getDeviceManagementConfigRepository().getEmailConfigurations();

        threadPoolExecutor = new ThreadPoolExecutor(emailConfig.getMinNumOfThread(),
                emailConfig.getMaxNumOfThread(), emailConfig.getKeepAliveTime(), TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(emailConfig.getThreadQueueCapacity()));
    }

    private static final String EMAIL_URI_SCHEME = "mailto:";
    private static Log log = LogFactory.getLog(EmailServiceProviderImpl.class);

    @Override
    public void sendEmail(EmailMessageProperties emailMessageProperties) throws DeviceManagementException {
        for (String toAddr : emailMessageProperties.getMailTo()) {
            threadPoolExecutor
                    .submit(new EmailSender(toAddr, emailMessageProperties.getSubject(),
                            emailMessageProperties.getMessageBody()));
        }
    }

    public static class EmailSender implements Runnable {

        String to;
        String subject;
        String body;

        EmailSender(String to, String subject, String body) {
            this.to = to;
            this.subject = subject;
            this.body = body;
        }

        public void run() {
            Map<String, String> headerMap = new HashMap<String, String>();
            headerMap.put(MailConstants.MAIL_HEADER_SUBJECT, subject);
            OMElement payload = OMAbstractFactory.getOMFactory().createOMElement(
                    BaseConstants.DEFAULT_TEXT_WRAPPER, null);
            payload.setText(body);
            try {
                ServiceClient serviceClient;
                ConfigurationContext configContext = EmailServiceDataHolder.getInstance()
                        .getConfigurationContextService().getClientConfigContext();
                //Set configuration service client if available, else create new service client
                if (configContext != null) {
                    serviceClient = new ServiceClient(configContext, null);
                } else {
                    serviceClient = new ServiceClient();
                }
                Options options = new Options();
                options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
                options.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);
                options.setProperty(MailConstants.TRANSPORT_MAIL_FORMAT,
                        MailConstants.TRANSPORT_FORMAT_TEXT);
                options.setTo(new EndpointReference(EMAIL_URI_SCHEME + to));
                serviceClient.setOptions(options);
                serviceClient.fireAndForget(payload);
                log.debug("Sending confirmation mail to " + to);
            } catch (AxisFault e) {
                log.error("Error in delivering the message, subject: '" + subject + "', to: '" + to + "'", e);
            }
        }
    }
}
