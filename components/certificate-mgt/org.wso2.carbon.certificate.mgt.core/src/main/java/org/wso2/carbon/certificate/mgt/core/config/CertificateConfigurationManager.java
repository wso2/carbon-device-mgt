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

package org.wso2.carbon.certificate.mgt.core.config;

import org.w3c.dom.Document;
import org.wso2.carbon.certificate.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.certificate.mgt.core.exception.CertificateManagementException;
import org.wso2.carbon.certificate.mgt.core.util.CertificateManagementConstants;
import org.wso2.carbon.certificate.mgt.core.util.CertificateManagerUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Class responsible for the certificate manager configuration initialization
 */
public class CertificateConfigurationManager {

    private CertificateManagementConfig certificateManagementConfig;
    private static CertificateConfigurationManager certificateConfigurationManager;

    private final String certMgtConfigXMLPath = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                                                CertificateManagementConstants.CERTIFICATE_CONFIG_XML_FILE;

    public static CertificateConfigurationManager getInstance() {
        if (certificateConfigurationManager == null) {
            synchronized (CertificateConfigurationManager.class) {
                if (certificateConfigurationManager == null) {
                    certificateConfigurationManager = new CertificateConfigurationManager();
                }
            }
        }
        return certificateConfigurationManager;
    }

    public synchronized void initConfig() throws CertificateManagementException {
        try {
            File certMgtConfig = new File(certMgtConfigXMLPath);
            Document doc = CertificateManagerUtil.convertToDocument(certMgtConfig);

            /* Un-marshaling Certificate Management configuration */
            JAXBContext rssContext = JAXBContext.newInstance(CertificateManagementConfig.class);
            Unmarshaller unmarshaller = rssContext.createUnmarshaller();
            this.certificateManagementConfig = (CertificateManagementConfig) unmarshaller.unmarshal(doc);
        } catch (Exception e) {
            throw new CertificateManagementException("Error occurred while initializing certificate config", e);
        }
    }

    public CertificateManagementConfig getCertificateManagementConfig() throws CertificateManagementException {
        if (certificateManagementConfig == null) {
            initConfig();
        }
        return certificateManagementConfig;
    }

    public DataSourceConfig getDataSourceConfig() throws CertificateManagementException {
        if (certificateManagementConfig == null) {
            initConfig();
        }
        return certificateManagementConfig.getCertificateManagementRepository().getDataSourceConfig();
    }

    public CertificateKeystoreConfig getCertificateKeyStoreConfig() throws CertificateManagementException {
        if (certificateManagementConfig == null) {
            initConfig();
        }
        return certificateManagementConfig.getCertificateKeystoreConfig();
    }
}