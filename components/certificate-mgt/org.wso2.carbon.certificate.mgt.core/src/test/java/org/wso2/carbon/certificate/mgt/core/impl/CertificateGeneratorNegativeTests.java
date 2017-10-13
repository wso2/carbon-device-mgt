/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOFactory;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.util.CSRGenerator;
import org.wso2.carbon.certificate.mgt.core.util.CertificateManagementConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import javax.sql.DataSource;
import java.io.File;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;

/**
 * This class has the negative tests for CertificateGenerator class
 */
@PowerMockIgnore({"java.net.ssl", "javax.security.auth.x500.X500Principal"})
@PrepareForTest({CertificateGenerator.class})
public class CertificateGeneratorNegativeTests extends PowerMockTestCase {

    @Test(description = "This test case tests behaviour when a certificate IO error occurs",
            expectedExceptions = KeystoreException.class)
    public void negtiveTestGenerateCertificateFromCSR() throws Exception {
        CertificateGenerator generator = new CertificateGenerator();
        //Prepare mock objects
        X509v3CertificateBuilder mock = Mockito.mock(X509v3CertificateBuilder.class);
        Mockito.when(mock.addExtension(Matchers.any(ASN1ObjectIdentifier.class), Matchers.anyBoolean(),
                Matchers.any(ASN1Encodable.class))).thenThrow(new CertIOException("CERTIO"));
        PowerMockito.whenNew(X509v3CertificateBuilder.class).withAnyArguments().thenReturn(mock);
        //prepare input parameters
        CSRGenerator csrGeneration = new CSRGenerator();
        KeyStoreReader keyStoreReader = new KeyStoreReader();
        KeyPair keyPair = csrGeneration.generateKeyPair("RSA", 1024);
        byte[] csrData = csrGeneration.generateCSR("SHA256WithRSA", keyPair);
        PKCS10CertificationRequest certificationRequest;
        PrivateKey privateKeyCA = keyStoreReader.getCAPrivateKey();
        X509Certificate certCA = (X509Certificate) keyStoreReader.getCACertificate();
        certificationRequest = new PKCS10CertificationRequest(csrData);
        generator.generateCertificateFromCSR(privateKeyCA, certificationRequest, certCA.getIssuerX500Principal().getName());

    }

    @Test(description = "This test case tests behaviour when Certificate Operator creation error occurs",
            expectedExceptions = KeystoreException.class)
    public void negtiveTestGenerateCertificateFromCSR2() throws Exception {
        CertificateGenerator generator = new CertificateGenerator();

        //Prepare mock objects
        JcaContentSignerBuilder mock = Mockito.mock(JcaContentSignerBuilder.class);
        Mockito.when(mock.setProvider(Matchers.eq(CertificateManagementConstants.PROVIDER))).thenReturn(mock);
        Mockito.when(mock.build(Matchers.any(PrivateKey.class))).thenThrow(new OperatorCreationException("OPERATOR"));
        PowerMockito.whenNew(JcaContentSignerBuilder.class).withAnyArguments().thenReturn(mock);
        //prepare input parameters
        CSRGenerator csrGeneration = new CSRGenerator();
        KeyStoreReader keyStoreReader = new KeyStoreReader();
        KeyPair keyPair = csrGeneration.generateKeyPair("RSA", 1024);
        byte[] csrData = csrGeneration.generateCSR("SHA256WithRSA", keyPair);
        PKCS10CertificationRequest certificationRequest;
        PrivateKey privateKeyCA = keyStoreReader.getCAPrivateKey();
        X509Certificate certCA = (X509Certificate) keyStoreReader.getCACertificate();
        certificationRequest = new PKCS10CertificationRequest(csrData);
        generator.generateCertificateFromCSR(privateKeyCA, certificationRequest, certCA.getIssuerX500Principal().getName());
    }

    @Test(description = "This test case tests the behaviour when certificate exception occurs when verifying"
            , expectedExceptions = KeystoreException.class)
    public void negtiveTestGenerateCertificateFromCSR3() throws Exception {
        CertificateGenerator generator = new CertificateGenerator();

        //Prepare mock objects
        JcaX509CertificateConverter mock = Mockito.mock(JcaX509CertificateConverter.class);
        Mockito.when(mock.setProvider(Matchers.eq(CertificateManagementConstants.PROVIDER))).thenReturn(mock);
        Mockito.when(mock.getCertificate(Matchers.any(X509CertificateHolder.class))).thenThrow(new CertificateException());
        PowerMockito.whenNew(JcaX509CertificateConverter.class).withAnyArguments().thenReturn(mock);

        //prepare input parameters
        CSRGenerator csrGeneration = new CSRGenerator();
        KeyStoreReader keyStoreReader = new KeyStoreReader();
        KeyPair keyPair = csrGeneration.generateKeyPair("RSA", 1024);
        byte[] csrData = csrGeneration.generateCSR("SHA256WithRSA", keyPair);
        PKCS10CertificationRequest certificationRequest;
        PrivateKey privateKeyCA = keyStoreReader.getCAPrivateKey();
        X509Certificate certCA = (X509Certificate) keyStoreReader.getCACertificate();
        certificationRequest = new PKCS10CertificationRequest(csrData);
        generator.generateCertificateFromCSR(privateKeyCA, certificationRequest, certCA.getIssuerX500Principal().getName());

    }

    @Test(description = "This test case tests behaviour when the Certificate provider does not exist"
            , expectedExceptions = KeystoreException.class)
    public void negativeTestgenerateX509Certificate1() throws Exception {
        CertificateGenerator generator = new CertificateGenerator();

        X509Certificate mock = Mockito.mock(X509Certificate.class);
        PowerMockito.doThrow(new NoSuchProviderException()).when(mock).verify(Matchers.any());
        JcaX509CertificateConverter conv = Mockito.mock(JcaX509CertificateConverter.class);
        Mockito.when(conv.setProvider(Mockito.anyString())).thenReturn(conv);
        Mockito.when(conv.getCertificate(Mockito.any())).thenReturn(mock);
        PowerMockito.whenNew(JcaX509CertificateConverter.class).withNoArguments().thenReturn(conv);
        generator.generateX509Certificate();
    }

    @Test(description = "This test case tests behaviour when the Certificate Algorithm does not exist"
            , expectedExceptions = KeystoreException.class)
    public void negativeTestgenerateX509Certificate2() throws Exception {
        CertificateGenerator generator = new CertificateGenerator();

        X509Certificate mock = Mockito.mock(X509Certificate.class);
        PowerMockito.doThrow(new NoSuchAlgorithmException()).when(mock).verify(Matchers.any());
        JcaX509CertificateConverter conv = Mockito.mock(JcaX509CertificateConverter.class);
        Mockito.when(conv.setProvider(Mockito.anyString())).thenReturn(conv);
        Mockito.when(conv.getCertificate(Mockito.any())).thenReturn(mock);
        PowerMockito.whenNew(JcaX509CertificateConverter.class).withNoArguments().thenReturn(conv);
        generator.generateX509Certificate();
    }

    @Test(description = "This test case tests behaviour when the Signature validation fails"
            , expectedExceptions = KeystoreException.class)
    public void negativeTestgenerateX509Certificate3() throws Exception {
        CertificateGenerator generator = new CertificateGenerator();

        X509Certificate mock = Mockito.mock(X509Certificate.class);
        PowerMockito.doThrow(new SignatureException()).when(mock).verify(Matchers.any());
        JcaX509CertificateConverter conv = Mockito.mock(JcaX509CertificateConverter.class);
        Mockito.when(conv.setProvider(Mockito.anyString())).thenReturn(conv);
        Mockito.when(conv.getCertificate(Mockito.any())).thenReturn(mock);
        PowerMockito.whenNew(JcaX509CertificateConverter.class).withNoArguments().thenReturn(conv);
        generator.generateX509Certificate();
    }

    @Test(description = "This test case tests behaviour when the Certificate exception occurs"
            , expectedExceptions = KeystoreException.class)
    public void negativeTestgenerateX509Certificate4() throws Exception {
        CertificateGenerator generator = new CertificateGenerator();

        X509Certificate mock = Mockito.mock(X509Certificate.class);
        PowerMockito.doThrow(new CertificateException()).when(mock).verify(Matchers.any());
        JcaX509CertificateConverter conv = Mockito.mock(JcaX509CertificateConverter.class);
        Mockito.when(conv.setProvider(Mockito.anyString())).thenReturn(conv);
        Mockito.when(conv.getCertificate(Mockito.any())).thenReturn(mock);
        PowerMockito.whenNew(JcaX509CertificateConverter.class).withNoArguments().thenReturn(conv);
        generator.generateX509Certificate();
    }

    @Test(description = "This test case tests behaviour when the Certificate key is invalid"
            , expectedExceptions = KeystoreException.class)
    public void negativeTestgenerateX509Certificate5() throws Exception {
        CertificateGenerator generator = new CertificateGenerator();

        X509Certificate mock = Mockito.mock(X509Certificate.class);
        PowerMockito.doThrow(new InvalidKeyException()).when(mock).verify(Matchers.any());
        JcaX509CertificateConverter conv = Mockito.mock(JcaX509CertificateConverter.class);
        Mockito.when(conv.setProvider(Mockito.anyString())).thenReturn(conv);
        Mockito.when(conv.getCertificate(Mockito.any())).thenReturn(mock);
        PowerMockito.whenNew(JcaX509CertificateConverter.class).withNoArguments().thenReturn(conv);
        generator.generateX509Certificate();
    }

    @Test(description = "This test case tests  behavior when the CA certificate is null"
            , expectedExceptions = KeystoreException.class)
    public void negativeTestgetRootCertificates1() throws KeystoreException {
        CertificateGenerator generator = new CertificateGenerator();
        generator.getRootCertificates(null, new byte[10]);
    }

    @Test(description = "This test case tests  behavior when the CA certificate is null",
            expectedExceptions = KeystoreException.class)
    public void negativeTestgetRootCertificates2() throws KeystoreException {
        CertificateGenerator generator = new CertificateGenerator();
        generator.getRootCertificates(new byte[10], null);
    }


    @BeforeClass
    public void init() throws SQLException {
        if (System.getProperty("carbon.home") == null) {
            File file = new File("src/test/resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../../resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../../../resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
        }
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

        DataSource normalDatasource = Mockito.mock(DataSource.class, Mockito.RETURNS_DEEP_STUBS);
        DataSource daoExceptionDatasource = Mockito.mock(DataSource.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(normalDatasource.getConnection().getMetaData().getDatabaseProductName()).thenReturn("H2");

        CertificateManagementDAOFactory.init(normalDatasource);
        Mockito.when(daoExceptionDatasource.getConnection().getMetaData().getDatabaseProductName()).thenReturn("H2");
        Mockito.when(daoExceptionDatasource.getConnection().prepareStatement(Mockito.anyString())).thenThrow(new SQLException());
    }
}
