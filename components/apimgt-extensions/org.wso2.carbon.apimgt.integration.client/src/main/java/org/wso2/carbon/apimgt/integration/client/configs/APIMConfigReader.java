/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.integration.client.configs;

import org.w3c.dom.Document;
import org.wso2.carbon.apimgt.integration.client.exception.APIMClientException;
import org.wso2.carbon.apimgt.integration.client.exception.InvalidConfigurationStateException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * This holds the configuration parser for api integration.xml
 */
public class APIMConfigReader {

    private static APIMConfig config;
    private static APIMConfigReader configReader= new APIMConfigReader();
    private static boolean isInitialized = false;
    private static final String API_INTEGRATION_CONFIG_PATH =
            CarbonUtils.getCarbonConfigDirPath() + File.separator + "apim-integration.xml";

    private APIMConfigReader() {

    }

    private static String apimIntegrationXmlFilePath = "";

    //TOD file may be a part of another file
    public static APIMConfigReader getInstance() {
        if (!isInitialized) {
            try {
                init();
            } catch (APIMClientException e) {
                throw new InvalidConfigurationStateException("Webapp Authenticator Configuration is not " +
                                                                     "initialized properly");
            }
        }
        return configReader;
    }

    public static void init() throws APIMClientException {
        try {
            File apimConfigFile = new File(API_INTEGRATION_CONFIG_PATH);
            Document doc = convertToDocument(apimConfigFile);

            JAXBContext ctx = JAXBContext.newInstance(APIMConfig.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            config = (APIMConfig) unmarshaller.unmarshal(doc);
            isInitialized = true;
        } catch (JAXBException e) {
            throw new APIMClientException("Error occurred while un-marshalling APIMConfig", e);
        }
    }

    private static Document convertToDocument(File file) throws APIMClientException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new APIMClientException("Error occurred while parsing file 'apim-integration.xml' to a org.w3c.dom.Document", e);
        }
    }

    public APIMConfig getConfig() {
        return config;
    }

}
