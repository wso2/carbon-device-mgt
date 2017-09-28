package org.wso2.carbon.certificate.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.io.FileUtils;
import org.apache.woden.tool.converter.Convert;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSAbsentContent;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemReader;
import org.eclipse.wst.xml.core.internal.contenttype.ByteReader;
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
import org.wso2.carbon.certificate.mgt.core.util.CSRGenerator;
import org.wso2.carbon.certificate.mgt.core.util.CertificateManagementConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CertificateManagementServiceImplTests extends BaseDeviceManagementCertificateTest {

    private static Log log = LogFactory.getLog(CertificateManagementServiceImplTests.class);
    private static final String CA_CERT_PEM = "src/test/resources/ca_cert.pem";
    private static final String RA_CERT_PEM = "src/test/resources/ra_cert.pem";
    private static final String CA_CERT_DER = "src/test/resources/ca_cert.der";
    CertificateManagementServiceImpl managementService = null;

    @Test
    public void testGetInstance() {
        try {
            CertificateManagementServiceImpl instance = CertificateManagementServiceImpl.getInstance();
            Assert.assertNotNull(instance);
            log.info("Successfully created instance");

        } catch (NullPointerException e) {
            log.error("Error while initializing CertificateManagementService", e);
            Assert.fail();
        }


    }

    @BeforeClass
    public void initCertificateManagementService() throws DeviceManagementException {
        managementService = CertificateManagementServiceImpl.getInstance();
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

    }

    @Test
    public void testGetCACertificate() {
        try {
            CertificateManagementServiceImpl instance = CertificateManagementServiceImpl.getInstance();
            Certificate caCertificate = instance.getCACertificate();
            Assert.assertNotNull(caCertificate);
            Assert.assertEquals(caCertificate.getType(), CertificateManagementConstants.X_509);
            log.info("Successfully returned CA Certificate");

        } catch (KeystoreException e) {
            String msg = "Error while getting the CA Certificate";
            log.error(msg, e);
            Assert.fail(msg, e);
        }
    }

    @Test
    public void testGetRACertificate() {
        try {
            Certificate raCertificate = managementService.getRACertificate();
            Assert.assertNotNull(raCertificate);
            Assert.assertEquals(raCertificate.getType(), CertificateManagementConstants.X_509);
            log.info("Successfully returned RA Certificate");
        } catch (KeystoreException e) {
            String msg = "Error while getting the RA Certificate";
            log.error(msg, e);
            Assert.fail(msg, e);
        }
    }

    @Test
    public void testGetRootCertificate() {
        File caCert = new File(CA_CERT_PEM);
        File raCert = new File(RA_CERT_PEM);

        try {
            byte[] caBytes = FileUtils.readFileToByteArray(caCert);
            byte[] raBytes = FileUtils.readFileToByteArray(raCert);

            List<X509Certificate> rootCertificates = managementService.getRootCertificates(caBytes, raBytes);
            Assert.assertNotNull(rootCertificates);
            Assert.assertEquals(rootCertificates.get(0).getType(), CertificateManagementConstants.X_509);
            Assert.assertEquals(rootCertificates.get(1).getType(), CertificateManagementConstants.X_509);
            log.info("Successfully returned root Certificate");

        } catch (IOException e) {
            String msg = "Error reading byte streams";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (KeystoreException e) {
            String msg = "Error retrieving root certificates";
            log.error(msg, e);
            Assert.fail(msg, e);
        }

    }

    @Test
    public void testGenerateX509Certificate() {
        try {

            X509Certificate x509Certificate = managementService.generateX509Certificate();
            Assert.assertNotNull(x509Certificate);
            Assert.assertEquals(x509Certificate.getType(), CertificateManagementConstants.X_509);

        } catch (KeystoreException e) {
            String msg = "Error while generating X509 Certificate";
            log.error(msg, e);
            Assert.fail(msg, e);
        }
    }

    @Test
    public void testGetCACertSCEP() {
        try {

            SCEPResponse caCertSCEP = managementService.getCACertSCEP();
            Assert.assertNotNull(caCertSCEP);
            Assert.assertEquals(caCertSCEP.getResultCriteria(), CAStatus.CA_RA_CERT_RECEIVED);

        } catch (KeystoreException e) {
            String msg = "Error while Retrieving CA Certificate";
            log.error(msg, e);
            Assert.fail(msg, e);
        }

    }

    @Test
    public void testGetCACapsSCEP() {

        byte[] caCapsSCEP = managementService.getCACapsSCEP();
        Assert.assertNotNull(caCapsSCEP);
        Assert.assertEquals(caCapsSCEP, CertificateManagementConstants.POST_BODY_CA_CAPS.getBytes());


    }

    @Test
    public void testGenerateCertificateFromCSR() {
        CSRGenerator csrGeneration = new CSRGenerator();
        KeyStoreReader keyStoreReader = new KeyStoreReader();

        // Generate key pair
        KeyPair keyPair = csrGeneration.generateKeyPair("RSA", 1024);
        byte[] csrData = csrGeneration.generateCSR("SHA256WithRSA", keyPair);

        //remove headers from csr
        String csrString = new String(csrData);
        csrString = csrString.replace("-----BEGIN NEW CERTIFICATE REQUEST-----", "")
                .replace("-----END NEW CERTIFICATE REQUEST-----", "");

        byte[] byteArrayBst = DatatypeConverter.parseBase64Binary(csrString);
        PKCS10CertificationRequest certificationRequest;

        try {
            PrivateKey privateKeyCA = keyStoreReader.getCAPrivateKey();
            X509Certificate certCA = (X509Certificate) keyStoreReader.getCACertificate();
            certificationRequest = new PKCS10CertificationRequest(byteArrayBst);
            X509Certificate x509Certificate = managementService.generateCertificateFromCSR(privateKeyCA,
                    certificationRequest, certCA.getIssuerX500Principal().getName());

            Assert.assertNotNull(x509Certificate);
            Assert.assertEquals(x509Certificate.getType(), CertificateManagementConstants.X_509);

        } catch (KeystoreException e) {
            String msg = "Error while reading Certificates from the keystore";
            log.error(msg, e);
            Assert.fail(msg, e);

        } catch (IOException e) {
            String msg = "Error while reading byte streams";
            log.error(msg, e);
            Assert.fail(msg, e);
        }

    }

    @Test
    public void testGetCertificateBySerial() {

        X509Certificate x509Certificate = null;
        try {
            //generate and save a certificate
            x509Certificate = managementService.generateX509Certificate();
            //initialize DeviceConfigurationManager
            DeviceConfigurationManager.getInstance().initConfig();
            CertificateResponse certificateBySerial = managementService.getCertificateBySerial(x509Certificate.getSerialNumber().toString());

            Assert.assertNotNull(certificateBySerial);
            Assert.assertEquals(certificateBySerial.getSerialNumber(), x509Certificate.getSerialNumber().toString());

        } catch (KeystoreException e) {
            String msg = "Error while receiving the certificate";
            log.error(msg, e);
            Assert.fail(msg, e);

        } catch (DeviceManagementException e) {
            String msg = "Error while initilizing DeviceConfigurationManager";
            log.error(msg, e);
        }


    }

    @Test
    public void testGetCertificateByAlias() {
        X509Certificate x509Certificate = null;
        try {
            //generate and save a certificate
            x509Certificate = managementService.generateX509Certificate();

            //initialize DeviceConfigurationManager
            DeviceConfigurationManager.getInstance().initConfig();
            Certificate certificateByAlias = managementService.getCertificateByAlias(x509Certificate.getSerialNumber().toString());


            Assert.assertNotNull(certificateByAlias);
            Assert.assertEquals(certificateByAlias.getType(), CertificateManagementConstants.X_509);

        } catch (KeystoreException e) {
            String msg = "Error while receiving the certificate";
            log.error(msg, e);
            Assert.fail(msg, e);

        } catch (DeviceManagementException e) {
            String msg = "Error while initilizing DeviceConfigurationManager";
            log.error(msg, e);
        }
    }

    @Test
    public void testVerifySignature() {
        BASE64Encoder encoder = new BASE64Encoder();

        try {
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

        } catch (CertificateEncodingException e) {
            String msg = "Error in Certificate encoding";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (IOException e) {
            String msg = "Error reading encoded signature";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (CMSException e) {
            String msg = "Error Adding certificates";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (KeystoreException e) {
            String msg = "Error while accessing the keystore";
            log.error(msg, e);
            Assert.fail(msg, e);
        }


    }

    @Test
    public void testVerifyPEMSignature() {


    }

    @Test
    public void testVerifySubjectDN() {
        try {
            DeviceConfigurationManager.getInstance().initConfig();
            X509Certificate x509Certificate = managementService.generateX509Certificate();
            log.info(x509Certificate.getIssuerDN().getName());
            managementService.verifySubjectDN(x509Certificate.getIssuerDN().getName());

        } catch (KeystoreException e) {
            e.printStackTrace();
        } catch (DeviceManagementException e) {
            e.printStackTrace();
        }
    }


    @BeforeClass
    @Override
    public void init() throws Exception {
        initDataSource();
        CertificateManagementDAOFactory.init(this.getDataSource());
    }
}
