package org.wso2.carbon.certificate.mgt.core.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.util.ConfigurationUtil;

import java.io.File;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;

public class CertificateGeneratorTestSuite {

    private static Log log = LogFactory.getLog(CertificateGeneratorTestSuite.class);
    private static final String CA_CERT_PEM = "src/test/resources/ca_cert.pem";
    private static final String RA_CERT_PEM = "src/test/resources/ra_cert.pem";
    private static final String CA_PRIVATE_KEY_PATH = "src/test/resources/ca_private.key";
    private final CertificateGenerator certificateGenerator = new CertificateGenerator();

    @Test
    public void testGetRootCertificates() {
        try {
            File caPemFile = new File(CA_CERT_PEM);
            File raPemFile = new File(RA_CERT_PEM);

            byte[] ca = FileUtils.readFileToByteArray(caPemFile);
            byte[] ra = FileUtils.readFileToByteArray(raPemFile);

            List<X509Certificate> rootCertificates = certificateGenerator.getRootCertificates(ca, ra);
            Assert.assertNotNull(rootCertificates, "Root certificates retrieved");

            Assert.assertEquals(rootCertificates.get(0).getType(), ConfigurationUtil.X_509);
            Assert.assertEquals(rootCertificates.get(1).getType(), ConfigurationUtil.X_509);
        } catch (IOException e) {
            Assert.fail("Error reading byte streams for CA and RA ", e);
        } catch (KeystoreException e) {
            Assert.fail("Error retrieving root certificates ", e);
        }
    }

    @Test
    public void testGenerateX509Certificate() {
        try {
            X509Certificate certificate = certificateGenerator.generateX509Certificate();

            Assert.assertNotNull(certificate, "Certificate received");
            Assert.assertEquals(certificate.getType(), ConfigurationUtil.X_509);
        } catch (KeystoreException e) {
            Assert.fail("Error occurred while generating X509 certificate ", e);
        }
    }

//    @Test
//    public void testGetPKIMessage() {
//        try {
//            byte[] pkiMessage = certificateGenerator.getPKIMessage(null);
//        } catch (IOSEnrollmentException e) {
//            Assert.fail("Error occurred while retrieving PKI Message ", e);
//        }
//    }

    @Test
    public void testGenerateCertificateFromCSR() {
        try {
            X509Certificate certificate = certificateGenerator.generateX509Certificate();

            Assert.assertNotNull(certificate, "Certificate received");
            Assert.assertEquals(certificate.getType(), ConfigurationUtil.X_509);
        } catch (KeystoreException e) {
            Assert.fail("Error occurred while generating certificate ", e);
        }
    }

//    @Test
//    public void testGetSignerKey() {
//        try {
//            PrivateKey privateKey = certificateGenerator.getSignerKey(CA_PRIVATE_KEY_PATH);
//
//            Assert.assertNotNull(privateKey, "Private key received");
//            Assert.assertEquals(privateKey.getAlgorithm(), ConfigurationUtil.RSA);
//        } catch (KeystoreException e) {
//            Assert.fail("Error occurred while generating certificate ", e);
//        }
//    }
//
//    @Test
//    public void testGetSigner() {
//        try {
//            X509Certificate certificate = certificateGenerator.getSigner(CA_CERT_PEM);
//
//            Assert.assertNotNull(certificate, "Certificate received");
//            Assert.assertEquals(certificate.getType(), ConfigurationUtil.X_509);
//        } catch (KeystoreException e) {
//            Assert.fail("Error while retrieving certificate ", e);
//        }
//    }
}
