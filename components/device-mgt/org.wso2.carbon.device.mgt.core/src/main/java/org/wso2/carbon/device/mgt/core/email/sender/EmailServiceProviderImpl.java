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
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
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
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.email.EmailConfigurations;
import org.wso2.carbon.device.mgt.core.email.EmailData;
import org.wso2.carbon.device.mgt.core.email.EmailSendingFailedException;
import org.wso2.carbon.device.mgt.core.internal.EmailServiceDataHolder;
import org.wso2.carbon.device.mgt.core.service.EmailService;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
    public void sendEmail(Set<String> recipients, EmailData emailData) throws EmailSendingFailedException {
        for (String recipient : recipients) {
            threadPoolExecutor.submit(new EmailSender(recipient, emailData.getSubject(), emailData.getBody()));
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
            OMElement payload = null;
            try {
                payload = AXIOMUtil.stringToOM(body);
            } catch (XMLStreamException e) {
                log.error("Error occurred while converting email body contents to an XML", e);
            }
            try {
                ConfigurationContextService configCtxService =
                        EmailServiceDataHolder.getInstance().getConfigurationContextService();
                if (configCtxService == null) {
                    throw new IllegalStateException("Configuration Context Service is not available");
                }
                ConfigurationContext configCtx = configCtxService.getServerConfigContext();
                ServiceClient serviceClient = new ServiceClient(configCtx, null);

                Map<String, String> headerMap = new HashMap<>();
                headerMap.put(MailConstants.MAIL_HEADER_SUBJECT, subject);

                Options options = new Options();
                options.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);
                options.setProperty("FORCE_CONTENT_TYPE_BASED_FORMATTER", "true");
                options.setProperty(Constants.Configuration.MESSAGE_TYPE, "text/html");
                options.setProperty(Constants.Configuration.CONTENT_TYPE, "text/html");
                options.setTo(new EndpointReference(EMAIL_URI_SCHEME + to));

                serviceClient.setOptions(options);
                serviceClient.fireAndForget(payload);
                if (log.isDebugEnabled()) {
                    log.debug("Email has been successfully sent to '" + to + "'");
                }
            } catch (AxisFault e) {
                log.error("Error occurred while delivering the message, subject: '" + subject + "', to: '" + to +
                        "'", e);
            }
        }

    }

}
