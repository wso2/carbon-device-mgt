/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.webapp.authenticator.framework.util;

import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.wso2.carbon.certificate.mgt.core.bean.Certificate;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.impl.CertificateGenerator;
import org.wso2.carbon.certificate.mgt.core.util.CertificateManagementConstants;
import org.wso2.carbon.certificate.mgt.core.util.CommonUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is a mock implementation of {@link CertificateGenerator}.
 */
public class TestCertificateGenerator extends CertificateGenerator {
    private int count = 0;

    public X509Certificate generateX509Certificate() throws KeystoreException {
        BigInteger serialNumber = CommonUtil.generateSerialNumber();
        String defaultPrinciple = "CN=" + serialNumber + ",O=WSO2,OU=Mobile,C=LK";
        CommonUtil commonUtil = new CommonUtil();
        Date validityBeginDate = commonUtil.getValidityStartDate();
        Date validityEndDate = commonUtil.getValidityEndDate();
        Security.addProvider(new BouncyCastleProvider());

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator
                    .getInstance(CertificateManagementConstants.RSA, CertificateManagementConstants.PROVIDER);
            keyPairGenerator.initialize(CertificateManagementConstants.RSA_KEY_LENGTH, new SecureRandom());
            KeyPair pair = keyPairGenerator.generateKeyPair();
            X500Principal principal = new X500Principal(defaultPrinciple);
            X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(principal, serialNumber,
                    validityBeginDate, validityEndDate, principal, pair.getPublic());
            ContentSigner contentSigner = new JcaContentSignerBuilder(CertificateManagementConstants.SHA256_RSA)
                    .setProvider(CertificateManagementConstants.PROVIDER).build(pair.getPrivate());
            X509Certificate certificate = new JcaX509CertificateConverter()
                    .setProvider(CertificateManagementConstants.PROVIDER)
                    .getCertificate(certificateBuilder.build(contentSigner));
            certificate.verify(certificate.getPublicKey());
            List<Certificate> certificates = new ArrayList<>();
            org.wso2.carbon.certificate.mgt.core.bean.Certificate certificateToStore =
                    new org.wso2.carbon.certificate.mgt.core.bean.Certificate();
            certificateToStore.setTenantId(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            certificateToStore.setCertificate(certificate);
            certificates.add(certificateToStore);
            saveCertInKeyStore(certificates);
            return certificate;
        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "No such algorithm found when generating certificate";
            throw new KeystoreException(errorMsg, e);
        } catch (NoSuchProviderException e) {
            String errorMsg = "No such provider found when generating certificate";
            throw new KeystoreException(errorMsg, e);
        } catch (OperatorCreationException e) {
            String errorMsg = "Issue in operator creation when generating certificate";
            throw new KeystoreException(errorMsg, e);
        } catch (CertificateExpiredException e) {
            String errorMsg = "Certificate expired after generating certificate";
            throw new KeystoreException(errorMsg, e);
        } catch (CertificateNotYetValidException e) {
            String errorMsg = "Certificate not yet valid when generating certificate";
            throw new KeystoreException(errorMsg, e);
        } catch (CertificateException e) {
            String errorMsg = "Certificate issue occurred when generating certificate";
            throw new KeystoreException(errorMsg, e);
        } catch (InvalidKeyException e) {
            String errorMsg = "Invalid key used when generating certificate";
            throw new KeystoreException(errorMsg, e);
        } catch (SignatureException e) {
            String errorMsg = "Signature related issue occurred when generating certificate";
            throw new KeystoreException(errorMsg, e);
        }
    }

    public String extractChallengeToken(X509Certificate certificate) {
        if (count != 0) {
            return "WSO2 (Challenge)";
        } else {
            count++;
            return null;
        }
    }
}
