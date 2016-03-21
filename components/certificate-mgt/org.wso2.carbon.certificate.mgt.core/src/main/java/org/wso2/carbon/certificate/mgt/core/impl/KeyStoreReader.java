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
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOException;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOFactory;
import org.wso2.carbon.certificate.mgt.core.dto.CertificateResponse;
import org.wso2.carbon.certificate.mgt.core.util.ConfigurationUtil;
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

    private KeyStore loadKeyStore(String configEntryKeyStoreType, String configEntryKeyStorePath,
                                  String configEntryKeyStorePassword) throws KeystoreException {

        InputStream inputStream = null;
        KeyStore keystore;

        try {
            keystore = KeyStore.getInstance(ConfigurationUtil.getConfigEntry(configEntryKeyStoreType));
            inputStream = new FileInputStream(ConfigurationUtil.getConfigEntry(configEntryKeyStorePath));
            keystore.load(inputStream, ConfigurationUtil.getConfigEntry(configEntryKeyStorePassword).toCharArray());

        } catch (KeyStoreException e) {
            String errorMsg = "KeyStore issue occurred when loading KeyStore";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } catch (FileNotFoundException e) {
            String errorMsg = "KeyStore file not found when loading KeyStore";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found when loading KeyStore";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } catch (CertificateException e) {
            String errorMsg = "CertificateException when loading KeyStore";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Input output issue occurred when loading KeyStore";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("Error closing KeyStore input stream", e);
            }
        }

        return keystore;
    }

    private synchronized void saveKeyStore(KeyStore keyStore, String configEntryKeyStorePath,
                                  String configEntryKeyStorePassword) throws KeystoreException {

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(
                    ConfigurationUtil.getConfigEntry(configEntryKeyStorePath));
            keyStore.store(outputStream, ConfigurationUtil.getConfigEntry(configEntryKeyStorePassword).toCharArray());
            outputStream.close();

        } catch (KeyStoreException e) {
            String errorMsg = "KeyStore issue occurred when loading KeyStore";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } catch (FileNotFoundException e) {
            String errorMsg = "KeyStore file not found when loading KeyStore";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found when loading KeyStore";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } catch (CertificateException e) {
            String errorMsg = "CertificateException when loading KeyStore";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Input output issue occurred when loading KeyStore";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                log.error("Error closing KeyStore output stream", e);
            }
        }
    }


    KeyStore loadCertificateKeyStore() throws KeystoreException {
        return loadKeyStore(ConfigurationUtil.CERTIFICATE_KEYSTORE, ConfigurationUtil.PATH_CERTIFICATE_KEYSTORE,
                ConfigurationUtil.CERTIFICATE_KEYSTORE_PASSWORD);
    }

    void saveCertificateKeyStore(KeyStore keyStore) throws KeystoreException {
        saveKeyStore(keyStore, ConfigurationUtil.PATH_CERTIFICATE_KEYSTORE,
                ConfigurationUtil.CERTIFICATE_KEYSTORE_PASSWORD);
    }

    public Certificate getCACertificate() throws KeystoreException {

        KeyStore keystore = loadCertificateKeyStore();
        Certificate caCertificate;

        try {
            caCertificate = keystore.getCertificate(ConfigurationUtil.getConfigEntry(ConfigurationUtil.CA_CERT_ALIAS));
        } catch (KeyStoreException e) {
            String errorMsg = "KeyStore issue occurred when loading KeyStore";
            log.error(errorMsg, e);
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
            caPrivateKey = (PrivateKey) (keyStore.getKey(
                    ConfigurationUtil.getConfigEntry(ConfigurationUtil.CA_CERT_ALIAS),
                    ConfigurationUtil.getConfigEntry(ConfigurationUtil.KEYSTORE_CA_CERT_PRIV_PASSWORD).toCharArray()));
        } catch (UnrecoverableKeyException e) {
            String errorMsg = "Key is unrecoverable when retrieving CA private key";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } catch (KeyStoreException e) {
            String errorMsg = "KeyStore issue occurred when retrieving CA private key";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found when retrieving CA private key";
            log.error(errorMsg, e);
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
            raCertificate = keystore.getCertificate(ConfigurationUtil.getConfigEntry(ConfigurationUtil.RA_CERT_ALIAS));
        } catch (KeyStoreException e) {
            String errorMsg = "KeyStore issue occurred when retrieving RA private key";
            log.error(errorMsg, e);
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
            CertificateResponse certificateResponse = CertificateManagementDAOFactory.getCertificateDAO().
                    retrieveCertificate(alias);
            if(certificateResponse != null) {
                raCertificate = (Certificate) Serializer.deserialize(certificateResponse.getCertificate());
            }
        } catch (CertificateManagementDAOException e) {
            String errorMsg = "Error when retrieving certificate the the database for the alias " + alias;
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } catch (ClassNotFoundException | IOException e) {
            String errorMsg = "Error when deserializing saved certificate.";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } catch (SQLException e) {
            String errorMsg = "Error when making a connection to the database.";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } finally {
            CertificateManagementDAOFactory.closeConnection();
        }
        return raCertificate;
    }

    PrivateKey getRAPrivateKey() throws KeystoreException {

        KeyStore keystore = loadCertificateKeyStore();
        PrivateKey raPrivateKey;
        try {
            raPrivateKey = (PrivateKey) (keystore.getKey(
                    ConfigurationUtil.getConfigEntry(ConfigurationUtil.RA_CERT_ALIAS),
                    ConfigurationUtil.getConfigEntry(ConfigurationUtil.KEYSTORE_RA_CERT_PRIV_PASSWORD).toCharArray()));
        } catch (UnrecoverableKeyException e) {
            String errorMsg = "Key is unrecoverable when retrieving RA private key";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } catch (KeyStoreException e) {
            String errorMsg = "KeyStore issue occurred when retrieving RA private key";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found when retrieving RA private key";
            log.error(errorMsg, e);
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
            certificateResponse = CertificateManagementDAOFactory.getCertificateDAO().
                    retrieveCertificate(serialNumber);
            if(certificateResponse != null && certificateResponse.getCertificate() != null) {
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
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } catch (SQLException e) {
            String errorMsg = "Error when making a connection to the database.";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } catch (ClassNotFoundException | IOException e) {
            String errorMsg = "Error when deserializing saved certificate.";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } finally {
            CertificateManagementDAOFactory.closeConnection();
        }
        return certificateResponse;
    }
}
