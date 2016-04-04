/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.email.sender.core;

import org.w3c.dom.Document;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

@XmlRootElement(name = "EmailSenderConfig")
public class EmailSenderConfig {

    private int minThreads;
    private int maxThreads;
    private int keepAliveDuration;
    private int threadQueueCapacity;

    private static EmailSenderConfig config;

    private static final String EMAIL_SENDER_CONFIG_PATH =
            CarbonUtils.getEtcCarbonConfigDirPath() + File.separator + "email-sender-config.xml";

    private EmailSenderConfig() {
    }

    public static EmailSenderConfig getInstance() {
        if (config == null) {
            throw new InvalidConfigurationStateException("Webapp Authenticator Configuration is not " +
                    "initialized properly");
        }
        return config;
    }

    @XmlElement(name = "MinThreads", required = true)
    public int getMinThreads() {
        return minThreads;
    }

    public void setMinThreads(int minThreads) {
        this.minThreads = minThreads;
    }

    @XmlElement(name = "MaxThreads", required = true)
    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    @XmlElement(name = "KeepAliveDuration", required = true)
    public int getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public void setKeepAliveDuration(int keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
    }
    @XmlElement(name = "ThreadQueueCapacity", required = true)
    public int getThreadQueueCapacity() {
        return threadQueueCapacity;
    }

    public void setThreadQueueCapacity(int threadQueueCapacity) {
        this.threadQueueCapacity = threadQueueCapacity;
    }

    public static void init() throws EmailSenderConfigurationFailedException {
        try {
            File emailSenderConfig = new File(EMAIL_SENDER_CONFIG_PATH);
            Document doc = EmailSenderUtil.convertToDocument(emailSenderConfig);

            /* Un-marshaling Email Sender configuration */
            JAXBContext ctx = JAXBContext.newInstance(EmailSenderConfig.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            //unmarshaller.setSchema(getSchema());
            config = (EmailSenderConfig) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new EmailSenderConfigurationFailedException("Error occurred while un-marshalling Email " +
                    "Sender Config", e);
        }
    }

}
