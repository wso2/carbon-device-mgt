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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSAbsentContent;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.certificate.mgt.core.common.BaseDeviceManagementCertificateTest;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOFactory;
import org.wso2.carbon.certificate.mgt.core.dto.CAStatus;
import org.wso2.carbon.certificate.mgt.core.dto.CertificateResponse;
import org.wso2.carbon.certificate.mgt.core.dto.SCEPResponse;
import org.wso2.carbon.certificate.mgt.core.exception.CertificateManagementException;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementServiceImpl;
import org.wso2.carbon.certificate.mgt.core.service.PaginationResult;
import org.wso2.carbon.certificate.mgt.core.util.CSRGenerator;
import org.wso2.carbon.certificate.mgt.core.util.CertificateManagementConstants;
import org.wso2.carbon.certificate.mgt.core.util.DummyCertificate;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import sun.misc.BASE64Encoder;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.List;

public class CertificateManagementServiceImplTests extends BaseDeviceManagementCertificateTest {

    private static Log log = LogFactory.getLog(CertificateManagementServiceImplTests.class);
    private static final String CA_CERT_PEM = "src/test/resources/ca_cert.pem";
    private static final String RA_CERT_PEM = "src/test/resources/ra_cert.pem";
    CertificateManagementServiceImpl managementService = null;

    @Test(description = "This test case tests initialization of CertificateManagementServiceImpl instance")
    public void testGetInstance() {
        CertificateManagementServiceImpl instance = CertificateManagementServiceImpl.getInstance();
        Assert.assertNotNull(instance);
        log.info("getInstance Test Successful");
    }

    @BeforeClass
    public void initCertificateManagementService() throws DeviceManagementException {
        //save certificatemanagementservice instance as class variable
        managementService = CertificateManagementServiceImpl.getInstance();
        //set Bouncycastle as a provider for testing
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    @Test(description = "This test case tests retrieval of CA Certificate from the keystore")
    public void testGetCACertificate() throws KeystoreException {
        CertificateManagementServiceImpl instance = CertificateManagementServiceImpl.getInstance();
        Certificate caCertificate = instance.getCACertificate();
        Assert.assertNotNull(caCertificate);
        Assert.assertEquals(caCertificate.getType(), CertificateManagementConstants.X_509);
        log.info("GetCACertificate Test Successful");
    }

    @Test(description = "This test case tests retrieval of RA Certificate from the keystore")
    public void testGetRACertificate() throws KeystoreException {
        Certificate raCertificate = managementService.getRACertificate();
        Assert.assertNotNull(raCertificate);
        Assert.assertEquals(raCertificate.getType(), CertificateManagementConstants.X_509);
        log.info("GetRACertificate Test Successful");
    }

    @Test(description = "This test case test generation of root certificates")
    public void testGetRootCertificate() throws IOException, KeystoreException {
        File caCert = new File(CA_CERT_PEM);
        File raCert = new File(RA_CERT_PEM);
        //read file to byte arrays
        byte[] caBytes = FileUtils.readFileToByteArray(caCert);
        byte[] raBytes = FileUtils.readFileToByteArray(raCert);
        List<X509Certificate> rootCertificates = managementService.getRootCertificates(caBytes, raBytes);
        Assert.assertNotNull(rootCertificates);
        Assert.assertEquals(rootCertificates.get(0).getType(), CertificateManagementConstants.X_509);
        Assert.assertEquals(rootCertificates.get(1).getType(), CertificateManagementConstants.X_509);
        log.info("GetRootCertificate Test Successful");
    }

    @Test(description = "This test case tests generation of X509Certificate")
    public void testGenerateX509Certificate() throws KeystoreException {
        X509Certificate x509Certificate = managementService.generateX509Certificate();
        Assert.assertNotNull(x509Certificate);
        Assert.assertEquals(x509Certificate.getType(), CertificateManagementConstants.X_509);
        log.info("GenerateX509Certificate Test Successful");
    }

    @Test(description = "This test case tests retrieving SCEP CA Certificate")
    public void testGetCACertSCEP() throws KeystoreException {
        SCEPResponse caCertSCEP = managementService.getCACertSCEP();
        Assert.assertNotNull(caCertSCEP);
        Assert.assertEquals(caCertSCEP.getResultCriteria(), CAStatus.CA_RA_CERT_RECEIVED);
        log.info("GetCACertSCEP Test Successful");
    }

    @Test
    public void testGetCACapsSCEP() {
        byte[] caCapsSCEP = managementService.getCACapsSCEP();
        Assert.assertNotNull(caCapsSCEP);
        Assert.assertEquals(caCapsSCEP, CertificateManagementConstants.POST_BODY_CA_CAPS.getBytes());
        log.info("GetCACapsSCEP Test Successful");
    }

    @Test(description = "This test case tests generation of a X509Certificate from a CSR")
    public void testGenerateCertificateFromCSR() throws KeystoreException, IOException {
        CSRGenerator csrGeneration = new CSRGenerator();
        KeyStoreReader keyStoreReader = new KeyStoreReader();
        // Generate key pair
        KeyPair keyPair = csrGeneration.generateKeyPair("RSA", 1024);
        byte[] csrData = csrGeneration.generateCSR("SHA256WithRSA", keyPair);
        PKCS10CertificationRequest certificationRequest;
        PrivateKey privateKeyCA = keyStoreReader.getCAPrivateKey();
        X509Certificate certCA = (X509Certificate) keyStoreReader.getCACertificate();
        certificationRequest = new PKCS10CertificationRequest(csrData);
        X509Certificate x509Certificate = managementService.generateCertificateFromCSR(privateKeyCA,
                certificationRequest, certCA.getIssuerX500Principal().getName());
        Assert.assertNotNull(x509Certificate);
        Assert.assertEquals(x509Certificate.getType(), CertificateManagementConstants.X_509);
        log.info("GenerateCertificateFromCSR Test Successful");
    }

    @Test(description = "This test case tests retrieval of a Certificate from the keystore from the Serial Number")
    public void testGetCertificateBySerial() throws KeystoreException, DeviceManagementException {
        X509Certificate x509Certificate = null;
        //generate and save a certificate
        x509Certificate = managementService.generateX509Certificate();
        //initialize DeviceConfigurationManager
        DeviceConfigurationManager.getInstance().initConfig();
        CertificateResponse certificateBySerial = managementService.getCertificateBySerial(x509Certificate.getSerialNumber().toString());
        Assert.assertNotNull(certificateBySerial);
        Assert.assertEquals(certificateBySerial.getSerialNumber(), x509Certificate.getSerialNumber().toString());
        log.info("GetCertificateBySerial Test Successful");
    }

    @Test(description = "This test case tests retrieval of a Certificate from the keystore from the Alias")
    public void testGetCertificateByAlias() throws KeystoreException, DeviceManagementException {
        X509Certificate x509Certificate = null;
        //generate and save a certificate
        x509Certificate = managementService.generateX509Certificate();
        //initialize DeviceConfigurationManager
        DeviceConfigurationManager.getInstance().initConfig();
        Certificate certificateByAlias = managementService.getCertificateByAlias(x509Certificate.getSerialNumber().toString());
        Assert.assertNotNull(certificateByAlias);
        Assert.assertEquals(certificateByAlias.getType(), CertificateManagementConstants.X_509);
        log.info("GetCertificateByAlias Test Successful");
    }

    @Test(description = "This test case tests Signature verification of a Certificate against the keystore")
    public void testVerifySignature() throws KeystoreException, CertificateEncodingException, CMSException, IOException {
        BASE64Encoder encoder = new BASE64Encoder();
        //generate and save a certificate in the keystore
        X509Certificate x509Certificate = managementService.generateX509Certificate();
        //Generate CMSdata
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        List<X509Certificate> list = new ArrayList<>();
        list.add(x509Certificate);
        JcaCertStore store = new JcaCertStore(list);
        generator.addCertificates(store);
        CMSSignedData degenerateSd = generator.generate(new CMSAbsentContent());
        byte[] signature = degenerateSd.getEncoded();
        boolean verifySignature = managementService.verifySignature(encoder.encode(signature));
        Assert.assertNotNull(verifySignature);
        Assert.assertTrue(verifySignature);
        log.info("VerifySignature Test Successful");
    }

    @Test(description = "This test case tests DN verification of a Certificate against the keystore")
    public void testVerifySubjectDN() throws DeviceManagementException, KeystoreException {
        DeviceConfigurationManager.getInstance().initConfig();
        X509Certificate x509Certificate = managementService.generateX509Certificate();
        log.info(x509Certificate.getIssuerX500Principal().getName());
        managementService.verifySubjectDN(x509Certificate.getIssuerDN().getName());
    }

    @Test(description = "This test case tests retrieval of a Certificate from the keystore from the Serial")
    public void testRetrieveCertificate() throws KeystoreException, CertificateManagementException {
        X509Certificate x509Certificate = managementService.generateX509Certificate();
        CertificateResponse certificateResponse = managementService.retrieveCertificate(x509Certificate.getSerialNumber().toString());
        Assert.assertNotNull(certificateResponse);
        Assert.assertEquals(x509Certificate.getSerialNumber(), certificateResponse.getCertificateserial());
    }

    @Test(description = "This test case tests the retrieval of Certificates from keystore in desired pagination")
    public void testGetAllCertificatesPaginated() throws CertificateManagementException, KeystoreException {
        managementService.generateX509Certificate();
        managementService.generateX509Certificate();
        PaginationResult allCertificates = managementService.getAllCertificates(0, 2);
        Assert.assertEquals(allCertificates.getData().size(), 2);
        log.info("GetAllCertificatesPaginated Test Successful");
    }

    @Test(description = "This test casae tests retrieval of all Certificates from keystore")
    public void testGetCertificates() throws CertificateManagementException, KeystoreException {
        List<CertificateResponse> certificatesBefore = managementService.getCertificates();
        managementService.generateX509Certificate();
        managementService.generateX509Certificate();
        List<CertificateResponse> certificatesAfter = managementService.getCertificates();
        Assert.assertNotNull(certificatesBefore);
        Assert.assertNotNull(certificatesAfter);
        Assert.assertEquals((certificatesBefore.size() + 2), certificatesAfter.size());
        log.info("GetCertificates Test Successful");
    }

    @Test(description = "This test case tests deleting Certificate from the keystore")
    public void testRemoveCertificate() throws CertificateManagementException, KeystoreException {
        X509Certificate x509Certificate = managementService.generateX509Certificate();
        List<CertificateResponse> certificates = managementService.getCertificates();
        int size = certificates.size();
        boolean removed = managementService.removeCertificate(x509Certificate.getSerialNumber().toString());
        certificates = managementService.getCertificates();
        int sizeAfter = certificates.size();
        Assert.assertNotNull(removed);
        Assert.assertTrue(removed);
        Assert.assertEquals((size - 1), sizeAfter);
        log.info("RemoveCertificate Test Successful");
    }


    @Test(description = "This test case tests searching for a list of certificates by the serial number")
    public void testSearchCertificates() throws CertificateManagementException, KeystoreException {
        X509Certificate x509Certificate = managementService.generateX509Certificate();
        List<CertificateResponse> certificateResponses = managementService.searchCertificates(x509Certificate.getSerialNumber().toString());
        Assert.assertNotNull(certificateResponses);
        Assert.assertEquals(1, certificateResponses.size());
        Assert.assertEquals(certificateResponses.get(0).getSerialNumber(), x509Certificate.getSerialNumber().toString());
        log.info("SearchCertificates Test Successful");
    }

    @Test(description = "This test case tests generation of signed Certificate from a CSR")
    public void testGetSignedCertificateFromCSR() throws KeystoreException {
        CSRGenerator csrGeneration = new CSRGenerator();
        BASE64Encoder encoder = new BASE64Encoder();
        // Generate key pair
        KeyPair keyPair = csrGeneration.generateKeyPair("RSA", 1024);
        byte[] csrData = csrGeneration.generateCSR("SHA256WithRSA", keyPair);
        X509Certificate signedCertificateFromCSR = managementService.getSignedCertificateFromCSR(encoder.encode(csrData));
        Assert.assertNotNull(signedCertificateFromCSR);
        Assert.assertEquals(signedCertificateFromCSR.getType(), CertificateManagementConstants.X_509);
        log.info("GetSignedCertificateFromCSR Test Successful");
    }

    @Test(description = "This test case tests the extraction of Challenge token from a Certificate")
    public void testExtractChallengeToken() throws KeystoreException {
        X509Certificate x509Certificate1 = new DummyCertificate();
        String token = managementService.extractChallengeToken(x509Certificate1);
        Assert.assertNotNull(token);
        Assert.assertEquals(token, DummyCertificate.EXT);
        log.info("extractChallengeToken Test Successful");
    }

    @Test(description = "This test case tests saving a list of Certificates in the keystore")
    public void testSaveCertificate() throws CertificateManagementException, IOException, CertificateException, KeystoreException {
        File caCert = new File(CA_CERT_PEM);
        int before = managementService.getCertificates().size();
        byte[] caBytes = FileUtils.readFileToByteArray(caCert);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(caBytes));
        List<org.wso2.carbon.certificate.mgt.core.bean.Certificate> certificates = new ArrayList<>();
        org.wso2.carbon.certificate.mgt.core.bean.Certificate certificateToStore =
                new org.wso2.carbon.certificate.mgt.core.bean.Certificate();
        certificateToStore.setTenantId(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
        certificateToStore.setCertificate(cert);
        certificates.add(certificateToStore);
        managementService.saveCertificate(certificates);
        int after = managementService.getCertificates().size();
        Assert.assertEquals((before + 1), after);
        log.info("SaveCertificate Test Successful");
    }

    @Test(description = "This test case tests converting a pem file to X509 Certificate")
    public void testPemToX509Certificate() throws IOException, KeystoreException {
        File caCert = new File(CA_CERT_PEM);
        BASE64Encoder encoder = new BASE64Encoder();
        byte[] caBytes = FileUtils.readFileToByteArray(caCert);
        X509Certificate certificate = managementService.pemToX509Certificate(encoder.encode(caBytes));
        Assert.assertNotNull(certificate);
        Assert.assertEquals(certificate.getType(), CertificateManagementConstants.X_509);
        log.info("PemToX509Certificate Test Successful");
    }

    @Test(description = "This test case tests extracting Certificate from the header Signature")
    public void testExtractCertificateFromSignature() throws KeystoreException, CertificateEncodingException, CMSException, IOException {
        BASE64Encoder encoder = new BASE64Encoder();
        //generate and save a certificate in the keystore
        X509Certificate x509Certificate = managementService.generateX509Certificate();
        //Generate CMSdata
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        List<X509Certificate> list = new ArrayList<>();
        list.add(x509Certificate);
        JcaCertStore store = new JcaCertStore(list);
        generator.addCertificates(store);
        CMSSignedData degenerateSd = generator.generate(new CMSAbsentContent());
        byte[] signature = degenerateSd.getEncoded();
        X509Certificate certificate = managementService.extractCertificateFromSignature(encoder.encode(signature));
        Assert.assertNotNull(certificate);
        Assert.assertEquals(certificate.getType(), CertificateManagementConstants.X_509);
        log.info("ExtractCertificateFromSignature Test Successful");
    }

    @BeforeClass
    public void init() throws Exception {
        initDataSource();
        CertificateManagementDAOFactory.init(this.getDataSource());
    }
}
