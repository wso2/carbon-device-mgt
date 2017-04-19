/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.apimgt.handlers.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.wso2.carbon.apimgt.handlers.APIMCertificateMGTException;
import org.wso2.carbon.apimgt.handlers.beans.DCR;
import org.wso2.carbon.apimgt.handlers.config.IOTServerConfiguration;
import org.wso2.carbon.apimgt.handlers.invoker.RESTInvoker;
import org.wso2.carbon.apimgt.handlers.invoker.RESTResponse;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains util methods for synapse gateway authentication handler
 */
public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);
    private static final String IOT_APIS_CONFIG_FILE = "iot-api-config.xml";
    private static String clientId;
    private static String clientSecret;

    /**
     * This method initializes the iot-api-config.xml file.
     * @return
     */
    public static IOTServerConfiguration initConfig() {
        try {

            String IOTServerAPIConfigurationPath =
                    CarbonUtils.getCarbonConfigDirPath() + File.separator + IOT_APIS_CONFIG_FILE;
            File file = new File(IOTServerAPIConfigurationPath);
            Document doc = Utils.convertToDocument(file);

            JAXBContext fileContext = JAXBContext.newInstance(IOTServerConfiguration.class);
            Unmarshaller unmarshaller = fileContext.createUnmarshaller();
            return (IOTServerConfiguration) unmarshaller.unmarshal(doc);

        } catch (JAXBException | APIMCertificateMGTException e) {
            log.error("Error occurred while initializing Data Source config", e);
            return null;
        }
    }

    /**
     * This class build the iot-api-config.xml file.
     * @param file
     * @return
     * @throws APIMCertificateMGTException
     */
    public static Document convertToDocument(File file) throws APIMCertificateMGTException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new APIMCertificateMGTException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document", e);
        }
    }

    /**
     * This class get the access token from the key manager.
     * @param iotServerConfiguration
     * @return
     * @throws APIMCertificateMGTException
     */
    public static String getAccessToken(IOTServerConfiguration iotServerConfiguration)
            throws APIMCertificateMGTException {
        try {
            if (clientId == null || clientSecret == null) {
                getClientSecretes(iotServerConfiguration);
            }
            URI tokenUrl = new URI(iotServerConfiguration.getOauthTokenEndpoint());
            String tokenContent = "grant_type=password&username=" + iotServerConfiguration.getUsername()+ "&password=" +
                    iotServerConfiguration.getPassword() + "&scope=activity-view";
            String tokenBasicAuth = "Basic " + Base64.encode((clientId + ":" + clientSecret).getBytes());
            Map<String, String> tokenHeaders = new HashMap<String, String>();
            tokenHeaders.put("Authorization", tokenBasicAuth);
            tokenHeaders.put("Content-Type", "application/x-www-form-urlencoded");

            RESTInvoker restInvoker = new RESTInvoker();
            RESTResponse response = restInvoker.invokePOST(tokenUrl, tokenHeaders, null, null, tokenContent);
            if(log.isDebugEnabled()) {
                log.debug("Token response:" + response.getContent());
            }
            JSONObject jsonResponse = new JSONObject(response.getContent());
            String accessToken = jsonResponse.getString("access_token");
            return accessToken;

        } catch (URISyntaxException e) {
            throw new APIMCertificateMGTException("Error occurred while trying to call oauth token endpoint", e);
        } catch (JSONException e) {
            throw new APIMCertificateMGTException("Error occurred while converting the json to object", e);
        } catch (IOException e) {
            throw new APIMCertificateMGTException("Error occurred while trying to call oauth token endpoint", e);
        }
    }

    /**
     * This method register an application to get the client key and secret.
     * @param iotServerConfiguration
     * @throws APIMCertificateMGTException
     */
    private static void getClientSecretes(IOTServerConfiguration iotServerConfiguration)
            throws APIMCertificateMGTException {
        try {
            String username = iotServerConfiguration.getUsername();
            String password = iotServerConfiguration.getPassword();
            DCR dcr = new DCR();
            dcr.setOwner(iotServerConfiguration.getUsername());
            dcr.setClientName(AuthConstants.CLIENT_NAME);
            dcr.setGrantType(AuthConstants.GRANT_TYPE);
            dcr.setTokenScope(AuthConstants.TOKEN_SCOPE);
            dcr.setCallbackUrl(AuthConstants.CALLBACK_URL);
            dcr.setIsSaasApp(true);
            String dcrContent = dcr.toJSON();
            Map<String, String> dcrHeaders = new HashMap<String, String>();
            String basicAuth = Base64.encode((username + ":" + password).getBytes());
            dcrHeaders.put(AuthConstants.CONTENT_TYPE_HEADER, AuthConstants.CONTENT_TYPE);
            dcrHeaders.put(AuthConstants.AUTHORIZATION_HEADER, AuthConstants.BASIC_AUTH_PREFIX + basicAuth);
            URI dcrUrl = new URI(iotServerConfiguration.getDynamicClientRegistrationEndpoint());
            RESTInvoker restInvoker = new RESTInvoker();
            RESTResponse response = restInvoker.invokePOST(dcrUrl, dcrHeaders, null, null, dcrContent);
            if (log.isDebugEnabled()) {
                log.debug("DCR response :" + response.getContent());
            }
            JSONObject jsonResponse = new JSONObject(response.getContent());
            clientId = jsonResponse.getString(AuthConstants.CLIENT_ID);
            clientSecret = jsonResponse.getString(AuthConstants.CLIENT_SECRET);
        } catch (JSONException e) {
            throw new APIMCertificateMGTException("Error occurred while converting the json to object", e);
        } catch (IOException | URISyntaxException e) {
            throw new APIMCertificateMGTException("Error occurred while trying to call DCR endpoint", e);
        }

    }

}

