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
package org.wso2.carbon.certificate.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.config.CertificateConfigurationManager;
import org.wso2.carbon.certificate.mgt.core.config.CertificateKeystoreConfig;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateDAO;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOException;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOFactory;
import org.wso2.carbon.certificate.mgt.core.dto.CertificateResponse;
import org.wso2.carbon.certificate.mgt.core.exception.CertificateManagementException;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.util.Serializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;

public class KeyStoreReader {

    private static final Log log = LogFactory.getLog(KeyStoreReader.class);

    private CertificateDAO certDao;

    public KeyStoreReader() {
        this.certDao = CertificateManagementDAOFactory.getCertificateDAO();
    }

    private KeyStore loadKeyStore(
            String configEntryKeyStoreType, String configEntryKeyStorePath,
            String configEntryKeyStorePassword) throws KeystoreException {
        InputStream is = null;
        KeyStore keystore;
        try {
            keystore = KeyStore.getInstance(configEntryKeyStoreType);
            is = new FileInputStream(configEntryKeyStorePath);
            keystore.load(is, configEntryKeyStorePassword.toCharArray());
        } catch (KeyStoreException e) {
            String errorMsg = "KeyStore issue occurred when loading KeyStore";
            throw new KeystoreException(errorMsg, e);
        } catch (FileNotFoundException e) {
            String errorMsg = "KeyStore file not found when loading KeyStore";
            throw new KeystoreException(errorMsg, e);
        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found when loading KeyStore";
            throw new KeystoreException(errorMsg, e);
        } catch (CertificateException e) {
            String errorMsg = "CertificateException when loading KeyStore";
            throw new KeystoreException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Input output issue occurred when loading KeyStore";
            throw new KeystoreException(errorMsg, e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                log.error("Error closing KeyStore input stream", e);
            }
        }

        return keystore;
    }

    private synchronized void saveKeyStore(KeyStore keyStore, String configEntryKeyStorePath,
                                           String configEntryKeyStorePassword) throws KeystoreException {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(configEntryKeyStorePath);
            keyStore.store(os, configEntryKeyStorePassword.toCharArray());
        } catch (KeyStoreException e) {
            String errorMsg = "KeyStore issue occurred when loading KeyStore";
            throw new KeystoreException(errorMsg, e);
        } catch (FileNotFoundException e) {
            String errorMsg = "KeyStore file not found when loading KeyStore";
            throw new KeystoreException(errorMsg, e);
        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found when loading KeyStore";
            throw new KeystoreException(errorMsg, e);
        } catch (CertificateException e) {
            String errorMsg = "CertificateException when loading KeyStore";
            throw new KeystoreException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Input output issue occurred when loading KeyStore";
            throw new KeystoreException(errorMsg, e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                log.error("Error closing KeyStore output stream", e);
            }
        }
    }


    KeyStore loadCertificateKeyStore() throws KeystoreException {
        KeyStore keyStore = null;
        try {
            CertificateKeystoreConfig certificateKeystoreConfig = CertificateConfigurationManager.getInstance().
                    getCertificateKeyStoreConfig();
            keyStore = loadKeyStore(certificateKeystoreConfig.getCertificateKeystoreType(),
                                    certificateKeystoreConfig.getCertificateKeystoreLocation(),
                                    certificateKeystoreConfig.getCertificateKeystorePassword());
        } catch (CertificateManagementException e) {
            String errorMsg = "Unable to find KeyStore configuration in certificate-mgt.config file.";
            throw new KeystoreException(errorMsg, e);
        }
        return keyStore;
    }

    void saveCertificateKeyStore(KeyStore keyStore) throws KeystoreException {
        try {
            CertificateKeystoreConfig certificateKeystoreConfig = CertificateConfigurationManager.getInstance().
                    getCertificateKeyStoreConfig();
            saveKeyStore(keyStore, certificateKeystoreConfig.getCertificateKeystoreLocation(),
                         certificateKeystoreConfig.getCertificateKeystorePassword());
        } catch (CertificateManagementException e) {
            String errorMsg = "Unable to find KeyStore configuration in certificate-mgt.config file.";
            throw new KeystoreException(errorMsg, e);
        }
    }

    public Certificate getCACertificate() throws KeystoreException {
        KeyStore keystore = loadCertificateKeyStore();
        Certificate caCertificate;
        try {
            CertificateKeystoreConfig certificateKeystoreConfig = CertificateConfigurationManager.getInstance().
                    getCertificateKeyStoreConfig();
            caCertificate = keystore.getCertificate(certificateKeystoreConfig.getCACertAlias());
        } catch (KeyStoreException e) {
            String errorMsg = "KeyStore issue occurred when loading KeyStore";
            throw new KeystoreException(errorMsg, e);
        } catch (CertificateManagementException e) {
            String errorMsg = "Unable to find KeyStore configuration in certificate-mgt.config file.";
            throw new KeystoreException(errorMsg, e);
        }

        if (caCertificate == null) {
            throw new KeystoreException("CA certificate not found in KeyStore");
        }

        return caCertificate;
    }

    PrivateKey getCAPrivateKey() throws KeystoreException {
        KeyStore keyStore = loadCertificateKeyStore();
        PrivateKey caPrivateKey;
        try {
            CertificateKeystoreConfig certificateKeystoreConfig = CertificateConfigurationManager.getInstance().
                    getCertificateKeyStoreConfig();
            caPrivateKey = (PrivateKey) keyStore.getKey(certificateKeystoreConfig.getCACertAlias(), certificateKeystoreConfig
                    .
                            getCAPrivateKeyPassword().toCharArray());
        } catch (UnrecoverableKeyException e) {
            String errorMsg = "Key is unrecoverable when retrieving CA private key";
            throw new KeystoreException(errorMsg, e);
        } catch (KeyStoreException e) {
            String errorMsg = "KeyStore issue occurred when retrieving CA private key";
            throw new KeystoreException(errorMsg, e);
        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found when retrieving CA private key";
            throw new KeystoreException(errorMsg, e);
        } catch (CertificateManagementException e) {
            String errorMsg = "Unable to find KeyStore configuration in certificate-mgt.config file.";
            throw new KeystoreException(errorMsg, e);
        }

        if (caPrivateKey == null) {
            throw new KeystoreException("CA private key not found in KeyStore");
        }

        return caPrivateKey;
    }

    public Certificate getRACertificate() throws KeystoreException {
        KeyStore keystore = loadCertificateKeyStore();
        Certificate raCertificate;
        try {
            CertificateKeystoreConfig certificateKeystoreConfig = CertificateConfigurationManager.getInstance().
                    getCertificateKeyStoreConfig();
            raCertificate = keystore.getCertificate(certificateKeystoreConfig.getRACertAlias());
        } catch (KeyStoreException e) {
            String errorMsg = "KeyStore issue occurred when retrieving RA private key";
            throw new KeystoreException(errorMsg, e);
        } catch (CertificateManagementException e) {
            String errorMsg = "Unable to find KeyStore configuration in certificate-mgt.config file.";
            throw new KeystoreException(errorMsg, e);
        }

        if (raCertificate == null) {
            throw new KeystoreException("RA certificate not found in KeyStore");
        }

        return raCertificate;
    }

    public Certificate getCertificateByAlias(String alias) throws KeystoreException {
        Certificate raCertificate = null;
        try {
            CertificateManagementDAOFactory.openConnection();
            CertificateResponse certificateResponse = certDao.retrieveCertificate(alias);
            if (certificateResponse != null) {
                raCertificate = (Certificate) Serializer.deserialize(certificateResponse.getCertificate());
            }
        } catch (CertificateManagementDAOException e) {
            String errorMsg = "Error when retrieving certificate the the database for the alias " + alias;
            throw new KeystoreException(errorMsg, e);
        } catch (ClassNotFoundException | IOException e) {
            String errorMsg = "Error when de-serializing saved certificate.";
            throw new KeystoreException(errorMsg, e);
        } catch (SQLException e) {
            String errorMsg = "Error when making a connection to the database.";
            throw new KeystoreException(errorMsg, e);
        } finally {
            CertificateManagementDAOFactory.closeConnection();
        }
        return raCertificate;
    }

    public PrivateKey getRAPrivateKey() throws KeystoreException {
        KeyStore keystore = loadCertificateKeyStore();
        PrivateKey raPrivateKey;
        try {
            CertificateKeystoreConfig certificateKeystoreConfig = CertificateConfigurationManager.getInstance().
                    getCertificateKeyStoreConfig();
            raPrivateKey = (PrivateKey) keystore.getKey(certificateKeystoreConfig.getRACertAlias(),
                                                        certificateKeystoreConfig.getRAPrivateKeyPassword().toCharArray());
        } catch (UnrecoverableKeyException e) {
            String errorMsg = "Key is unrecoverable when retrieving RA private key";
            throw new KeystoreException(errorMsg, e);
        } catch (KeyStoreException e) {
            String errorMsg = "KeyStore issue occurred when retrieving RA private key";
            throw new KeystoreException(errorMsg, e);
        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found when retrieving RA private key";
            throw new KeystoreException(errorMsg, e);
        } catch (CertificateManagementException e) {
            String errorMsg = "Unable to find KeyStore configuration in certificate-mgt.config file.";
            throw new KeystoreException(errorMsg, e);
        }

        if (raPrivateKey == null) {
            throw new KeystoreException("RA private key not found in KeyStore");
        }

        return raPrivateKey;
    }

    public CertificateResponse getCertificateBySerial(String serialNumber) throws KeystoreException {
        CertificateResponse certificateResponse = null;
        try {
            CertificateManagementDAOFactory.openConnection();
            certificateResponse = certDao.retrieveCertificate(serialNumber);
            if (certificateResponse != null && certificateResponse.getCertificate() != null) {
                Certificate certificate = (Certificate) Serializer.deserialize(certificateResponse.getCertificate());
                if (certificate instanceof X509Certificate) {
                    X509Certificate x509cert = (X509Certificate) certificate;
                    String commonName = CertificateGenerator.getCommonName(x509cert);
                    certificateResponse.setCommonName(commonName);
                }
            }
        } catch (CertificateManagementDAOException e) {
            String errorMsg = "Error when retrieving certificate from the the database for the serial number: " +
                    serialNumber;
            throw new KeystoreException(errorMsg, e);
        } catch (SQLException e) {
            String errorMsg = "Error when making a connection to the database.";
            throw new KeystoreException(errorMsg, e);
        } catch (ClassNotFoundException | IOException e) {
            String errorMsg = "Error when de-serializing saved certificate.";
            throw new KeystoreException(errorMsg, e);
        } finally {
            CertificateManagementDAOFactory.closeConnection();
        }
        return certificateResponse;
    }
}
