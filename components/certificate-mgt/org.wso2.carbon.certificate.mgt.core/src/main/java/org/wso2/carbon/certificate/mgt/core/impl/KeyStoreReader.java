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
import org.wso2.carbon.certificate.mgt.core.util.ConfigurationUtil;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

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
            String errorMsg = "Certificate expired when loading KeyStore";
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

    KeyStore loadCertificateKeyStore() throws KeystoreException {
        return loadKeyStore(ConfigurationUtil.CERTIFICATE_KEYSTORE, ConfigurationUtil.PATH_CERTIFICATE_KEYSTORE,
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
}
