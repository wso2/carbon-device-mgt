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
package org.wso2.carbon.webapp.authenticator.framework.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationFrameworkUtil;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticatorFrameworkException;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.List;

@XmlRootElement(name = "WebappAuthenticatorConfig")
public class WebappAuthenticatorConfig {

    private List<AuthenticatorConfig> authenticators;
    private static WebappAuthenticatorConfig config;

    private static final Log log = LogFactory.getLog(WebappAuthenticatorConfig.class);
    private static final String AUTHENTICATOR_CONFIG_PATH =
            CarbonUtils.getEtcCarbonConfigDirPath() + File.separator + "webapp-authenticator-config.xml";
    private static final String AUTHENTICATOR_CONFIG_SCHEMA_PATH =
            "resources/config/schema/webapp-authenticator-config-schema.xsd";

    private WebappAuthenticatorConfig() {
    }

    public static WebappAuthenticatorConfig getInstance() {
        if (config == null) {
            throw new InvalidConfigurationStateException("Webapp Authenticator Configuration is not " +
                    "initialized properly");
        }
        return config;
    }

    @XmlElementWrapper(name = "Authenticators", required = true)
    @XmlElement(name = "Authenticator", required = true)
    public List<AuthenticatorConfig> getAuthenticators() {
        return authenticators;
    }

    @SuppressWarnings("unused")
    public void setAuthenticators(List<AuthenticatorConfig> authenticators) {
        this.authenticators = authenticators;
    }

    public static void init() throws AuthenticatorFrameworkException {
        try {
            File authConfig = new File(WebappAuthenticatorConfig.AUTHENTICATOR_CONFIG_PATH);
            Document doc = AuthenticationFrameworkUtil.convertToDocument(authConfig);

            /* Un-marshaling Webapp Authenticator configuration */
            JAXBContext ctx = JAXBContext.newInstance(WebappAuthenticatorConfig.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            //unmarshaller.setSchema(getSchema());
            config = (WebappAuthenticatorConfig) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new AuthenticatorFrameworkException("Error occurred while un-marshalling Webapp Authenticator " +
                    "Framework Config", e);
        }
    }

    private static Schema getSchema() throws AuthenticatorFrameworkException {
        try {
            File deviceManagementSchemaConfig = new File(WebappAuthenticatorConfig.AUTHENTICATOR_CONFIG_SCHEMA_PATH);
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            return factory.newSchema(deviceManagementSchemaConfig);
        } catch (SAXException e) {
            throw new AuthenticatorFrameworkException("Error occurred while initializing the schema of " +
                    "webapp-authenticator-config.xml", e);
        }
    }

}
