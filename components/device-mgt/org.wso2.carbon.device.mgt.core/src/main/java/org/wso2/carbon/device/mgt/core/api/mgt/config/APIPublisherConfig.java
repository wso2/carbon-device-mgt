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
package org.wso2.carbon.device.mgt.core.api.mgt.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.api.mgt.APIConfig;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.List;

/**
 * This class carries all API configurations used by device management components, that need to be published within
 * the underlying API-Management infrastructure.
 */
@XmlRootElement(name = "APIPublisherConfig")
public class APIPublisherConfig {

    private List<APIConfig> apiConfigs;
    private static APIPublisherConfig config;

    private static final Log log = LogFactory.getLog(APIPublisherConfig.class);
    private static final String USER_DEFINED_API_CONFIG_PATH =
            CarbonUtils.getEtcCarbonConfigDirPath() + File.separator + "user-api-publisher-config.xml";

    private static final Object LOCK = new Object();

    public static APIPublisherConfig getInstance() {
        if (config == null) {
            synchronized (LOCK) {
                try {
                    init();
                } catch (DeviceManagementException e) {
                    log.error("Error occurred while initializing API Publisher Config", e);
                }
            }
        }
        return config;
    }

    @XmlElementWrapper(name = "APIs", required = true)
    @XmlElement(name = "API", required = true)
    public List<APIConfig> getApiConfigs() {
        return apiConfigs;
    }

    @SuppressWarnings("unused")
    public void setApiConfigs(List<APIConfig> apiConfigs) {
        this.apiConfigs = apiConfigs;
    }

    private static void init() throws DeviceManagementException {
        try {
            File publisherConfig = new File(APIPublisherConfig.USER_DEFINED_API_CONFIG_PATH);
            Document doc = DeviceManagerUtil.convertToDocument(publisherConfig);

            /* Un-marshaling Device Management configuration */
            JAXBContext ctx = JAXBContext.newInstance(APIPublisherConfig.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            config = (APIPublisherConfig) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new DeviceManagementException("Error occurred while un-marshalling API Publisher Config", e);
        }
    }

}
