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
package org.wso2.carbon.certificate.mgt.core.util;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationUtil {

	public static final String PATH_CERTIFICATE_KEYSTORE = "CertificateKeystoreLocation";
	public static final String CERTIFICATE_KEYSTORE_PASSWORD = "CertificateKeystorePassword";
	public static final String KEYSTORE_CA_CERT_PRIV_PASSWORD = "CAPrivateKeyPassword";
	public static final String KEYSTORE_RA_CERT_PRIV_PASSWORD = "RAPrivateKeyPassword";
	public static final String CA_CERT_ALIAS = "CACertAlias";
	public static final String RA_CERT_ALIAS = "RACertAlias";
	public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
	public static final String PROVIDER = "BC";
	public static final String KEYSTORE = "Type";
	public static final String CERTIFICATE_KEYSTORE = "CertificateKeystoreType";
	public static final String RSA = "RSA";
	public static final String UTF_8 = "UTF-8";
	public static final String SHA256_RSA = "SHA256WithRSAEncryption";
	public static final String X_509 = "X.509";
	public static final String POST_BODY_CA_CAPS = "POSTPKIOperation\nSHA-1\nDES3\n";
	public static final String DES_EDE = "DESede";
	public static final String CONF_LOCATION = "conf.location";
    private static final String CARBON_HOME = "carbon.home";
    private static final String CERTIFICATE_CONFIG_XML = "certificate-config.xml";
    private static final String CARBON_HOME_ENTRY = "${carbon.home}";
    public static final String DEFAULT_PRINCIPAL = "O=WSO2, OU=Mobile, C=LK";
    public static final String RSA_PRIVATE_KEY_BEGIN_TEXT = "-----BEGIN RSA PRIVATE KEY-----\n";
    public static final String RSA_PRIVATE_KEY_END_TEXT = "-----END RSA PRIVATE KEY-----";
    public static final String EMPTY_TEXT = "";
	public static final int RSA_KEY_LENGTH = 1024;
	public static final long MILLI_SECONDS = 1000L * 60 * 60 * 24;


	private static ConfigurationUtil configurationUtil;
	private static final String[] certificateConfigEntryNames = { CA_CERT_ALIAS, RA_CERT_ALIAS,
			CERTIFICATE_KEYSTORE, PATH_CERTIFICATE_KEYSTORE, CERTIFICATE_KEYSTORE_PASSWORD,
			KEYSTORE_CA_CERT_PRIV_PASSWORD, KEYSTORE_RA_CERT_PRIV_PASSWORD };

	private static Map<String, String> configMap;

	private static Map<String, String> readCertificateConfigurations() throws KeystoreException {

		String certConfLocation = System.getProperty(CONF_LOCATION) + File.separator + CERTIFICATE_CONFIG_XML;

		if (configurationUtil == null || configMap == null) {

			configurationUtil = new ConfigurationUtil();
			configMap = new HashMap<String, String>();

			Document document;
			try {
				File fXmlFile = new File(certConfLocation);
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
				document = documentBuilder.parse(fXmlFile);
			} catch (ParserConfigurationException e) {
				throw new KeystoreException("Error parsing configuration in certificate-config.xml file");
			} catch (SAXException e) {
                throw new KeystoreException("SAX exception in certificate-config.xml file");
            } catch (IOException e) {
                throw new KeystoreException("Error reading certificate-config.xml file");
            }

			for (String configEntry : certificateConfigEntryNames) {
				NodeList elements = document.getElementsByTagName(configEntry);
				if (elements != null && elements.getLength() > 0) {
					configMap.put(configEntry, elements.item(0).getTextContent());
				}
			}

			String certKeyStoreLocation = replaceCarbonHomeEnvEntry(configMap.get(PATH_CERTIFICATE_KEYSTORE));
			if (certKeyStoreLocation != null) {
				configMap.put(PATH_CERTIFICATE_KEYSTORE, certKeyStoreLocation);
			}
		}

		return configMap;
	}

	public static String getConfigEntry(final String entry) throws KeystoreException {

		Map<String, String> configurationMap = readCertificateConfigurations();
		String configValue = configurationMap.get(entry);

		if (configValue == null) {
			throw new KeystoreException(String.format("Configuration entry %s not available", entry));
		}

		return configValue.trim();
	}

	private static String replaceCarbonHomeEnvEntry(String entry) {
		if (entry != null && entry.toLowerCase().contains(CARBON_HOME_ENTRY)) {
			return entry.replace(CARBON_HOME_ENTRY, System.getProperty(CARBON_HOME));
		}

		return null;
	}

    public static ConfigurationUtil getInstance() {
        if (configurationUtil == null) {
            synchronized (ConfigurationUtil.class) {
                if (configurationUtil == null) {
                    configurationUtil = new ConfigurationUtil();
                }
            }
        }
        return configurationUtil;
    }
}
