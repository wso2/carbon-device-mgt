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
package org.wso2.carbon.certificate.mgt.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.wso2.carbon.certificate.mgt.core.dto.SCEPResponse;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.impl.CertificateGenerator;
import org.wso2.carbon.certificate.mgt.core.impl.KeyStoreReader;
import org.wso2.carbon.certificate.mgt.core.util.ConfigurationUtil;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

public class CertificateManagementServiceImpl implements CertificateManagementService {

    private static final Log log = LogFactory.getLog(CertificateManagementServiceImpl.class);
    private static CertificateManagementServiceImpl certificateManagementServiceImpl;
    private static KeyStoreReader keyStoreReader;
    private static CertificateGenerator certificateGenerator;

    private CertificateManagementServiceImpl() {}

    public static CertificateManagementServiceImpl getInstance() {

        if (certificateManagementServiceImpl == null) {
            certificateManagementServiceImpl = new CertificateManagementServiceImpl();
            keyStoreReader = new KeyStoreReader();
            certificateGenerator = new CertificateGenerator();
        }
        return certificateManagementServiceImpl;
    }

    public Certificate getCACertificate() throws KeystoreException {
        return keyStoreReader.getCACertificate();
    }

    public Certificate getRACertificate() throws KeystoreException {
        return keyStoreReader.getRACertificate();
    }

    public List<X509Certificate> getRootCertificates(byte[] ca, byte[] ra) throws KeystoreException {
        return certificateGenerator.getRootCertificates(ca, ra);
    }

    public X509Certificate generateX509Certificate() throws KeystoreException {
        return certificateGenerator.generateX509Certificate();
    }

    public SCEPResponse getCACertSCEP() throws KeystoreException {
        return certificateGenerator.getCACert();
    }

    public byte[] getCACapsSCEP() {
        return ConfigurationUtil.POST_BODY_CA_CAPS.getBytes();
    }

    public byte[] getPKIMessageSCEP(InputStream inputStream) throws KeystoreException {
        return certificateGenerator.getPKIMessage(inputStream);
    }

    public X509Certificate generateCertificateFromCSR(PrivateKey privateKey,
                                                              PKCS10CertificationRequest request,
                                                              String issueSubject) throws KeystoreException {
        return certificateGenerator.generateCertificateFromCSR(privateKey, request, issueSubject);
    }

    public Certificate getCertificateByAlias(String alias) throws KeystoreException {
        return keyStoreReader.getCertificateByAlias(alias);
    }

    public boolean verifySignature(String headerSignature) throws KeystoreException {
        return certificateGenerator.verifySignature(headerSignature);
    }

    public X509Certificate extractCertificateFromSignature(String headerSignature) throws KeystoreException {
        return certificateGenerator.extractCertificateFromSignature(headerSignature);
    }

    public String extractChallengeToken(X509Certificate certificate) {
        return certificateGenerator.extractChallengeToken(certificate);
    }

    public X509Certificate getSignCertificateFromCSR(String binarySecurityToken,
                                                     X509Certificate caCert, List certParameterList)
            throws KeystoreException {
        return certificateGenerator.getSignCertificateFromCSR(binarySecurityToken, caCert,
                certParameterList);
    }
}
