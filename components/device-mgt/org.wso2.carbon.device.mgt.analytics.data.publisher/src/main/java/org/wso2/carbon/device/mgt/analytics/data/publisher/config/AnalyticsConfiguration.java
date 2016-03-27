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
package org.wso2.carbon.device.mgt.analytics.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.analytics.DeviceAnalyticsUtil;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;

/**
 * Configurations related to DAS data publisher and DAL.
 */
@XmlRootElement(name = "AnalyticsConfiguration")
public class AnalyticsConfiguration {

    private String receiverServerUrl;
    private String adminUsername;
    private String adminPassword;
    private boolean enable;

    private static AnalyticsConfiguration config;

    private static final Log log = LogFactory.getLog(AnalyticsConfiguration.class);
    private static final String DEVICE_ANALYTICS_CONFIG_PATH =
            CarbonUtils.getEtcCarbonConfigDirPath() + File.separator + "device-analytics-config.xml";

    private AnalyticsConfiguration() {
    }

    public static AnalyticsConfiguration getInstance() {
        if (config == null) {
            throw new InvalidConfigurationStateException("Device analytics configuration is not " +
                    "initialized properly");
        }
        return config;
    }


    @XmlElement(name = "AdminUsername", required = true)
    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    @XmlElement(name = "AdminPassword", required = true)
    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    @XmlElement(name = "ReceiverServerUrl", required = true)
    public String getReceiverServerUrl() {
        return receiverServerUrl;
    }

    public void setReceiverServerUrl(String receiverServerUrl) {
        this.receiverServerUrl = receiverServerUrl;
    }

    @XmlElement(name = "Enabled", required = true)
    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean status) {
        this.enable = status;
    }

    public static void init() throws DataPublisherConfigurationException {
        try {
            File authConfig = new File(AnalyticsConfiguration.DEVICE_ANALYTICS_CONFIG_PATH);
            Document doc = DeviceAnalyticsUtil.convertToDocument(authConfig);

            /* Un-marshaling device analytics configuration */
            JAXBContext ctx = JAXBContext.newInstance(AnalyticsConfiguration.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            config = (AnalyticsConfiguration) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new DataPublisherConfigurationException("Error occurred while un-marshalling device analytics " +
                    "Config", e);
        }
    }

}
