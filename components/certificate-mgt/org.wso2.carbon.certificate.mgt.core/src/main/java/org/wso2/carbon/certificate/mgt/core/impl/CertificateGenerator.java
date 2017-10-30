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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSAbsentContent;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.Store;
import org.jscep.message.*;
import org.jscep.transaction.FailInfo;
import org.jscep.transaction.Nonce;
import org.jscep.transaction.TransactionId;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateDAO;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOException;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOFactory;
import org.wso2.carbon.certificate.mgt.core.dto.CAStatus;
import org.wso2.carbon.certificate.mgt.core.dto.CertificateResponse;
import org.wso2.carbon.certificate.mgt.core.dto.SCEPResponse;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.exception.TransactionManagementException;
import org.wso2.carbon.certificate.mgt.core.util.CertificateManagementConstants;
import org.wso2.carbon.certificate.mgt.core.util.CommonUtil;
import org.wso2.carbon.certificate.mgt.core.util.Serializer;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.security.auth.x500.X500Principal;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class CertificateGenerator {

    private static final Log log = LogFactory.getLog(CertificateGenerator.class);

    public static String getCommonName(X509Certificate requestCertificate) {
        String distinguishedName = requestCertificate.getSubjectDN().getName();
        if (distinguishedName != null && !distinguishedName.isEmpty()) {
            String[] dnSplits = distinguishedName.split(",");
            for (String dnSplit : dnSplits) {
                if (dnSplit.contains("CN=")) {
                    String[] cnSplits = dnSplit.split("=");
                    if (cnSplits[1] != null) {
                        return cnSplits[1];
                    }
                }
            }
        }
        return null;
    }

    public static void extractCertificateDetails(byte[] certificateBytes, CertificateResponse certificateResponse)
            throws CertificateManagementDAOException {
        try {
            if (certificateBytes != null) {
                java.security.cert.Certificate x509Certificate =
                        (java.security.cert.Certificate) Serializer.deserialize(certificateBytes);
                if (x509Certificate instanceof X509Certificate) {
                    X509Certificate certificate = (X509Certificate) x509Certificate;
                    certificateResponse.setNotAfter(certificate.getNotAfter().getTime());
                    certificateResponse.setNotBefore(certificate.getNotBefore().getTime());
                    certificateResponse.setCertificateserial(certificate.getSerialNumber());
                    certificateResponse.setIssuer(certificate.getIssuerDN().getName());
                    certificateResponse.setSubject(certificate.getSubjectDN().getName());
                    certificateResponse.setCertificateVersion(certificate.getVersion());
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            String errorMsg = "Error while deserializing the certificate.";
            throw new CertificateManagementDAOException(errorMsg, e);
        }

    }

    public List<X509Certificate> getRootCertificates(byte[] ca, byte[] ra) throws KeystoreException {

        if (ca == null) {
            throw new KeystoreException("CA certificate is mandatory");
        }

        if (ra == null) {
            throw new KeystoreException("RA certificate is mandatory");
        }

        List<X509Certificate> certificateList = new ArrayList<X509Certificate>();
        InputStream caInputStream = null;
        InputStream raInputStream = null;

        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance(CertificateManagementConstants.X_509);
            caInputStream = new ByteArrayInputStream(ca);
            raInputStream = new ByteArrayInputStream(ra);

            X509Certificate caCert = (X509Certificate) certificateFactory.generateCertificate(caInputStream);
            X509Certificate raCert = (X509Certificate) certificateFactory.generateCertificate(raInputStream);

            certificateList.add(caCert);
            certificateList.add(raCert);
        } catch (CertificateException e) {
            String errorMsg = "Error occurred while fetching root certificates";
            throw new KeystoreException(errorMsg, e);
        } finally {
            if (caInputStream != null) {
                try {
                    caInputStream.close();
                } catch (IOException e) {
                    log.error("Error occurred when closing CA input stream");
                }
            }

            if (raInputStream != null) {
                try {
                    raInputStream.close();
                } catch (IOException e) {
                    log.error("Error occurred when closing RA input stream");
                }
            }
        }

        return certificateList;
    }

    public X509Certificate generateX509Certificate() throws KeystoreException {

        CommonUtil commonUtil = new CommonUtil();
        Date validityBeginDate = commonUtil.getValidityStartDate();
        Date validityEndDate = commonUtil.getValidityEndDate();

        Security.addProvider(new BouncyCastleProvider());

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                    CertificateManagementConstants.RSA, CertificateManagementConstants.PROVIDER);
            keyPairGenerator.initialize(CertificateManagementConstants.RSA_KEY_LENGTH, new SecureRandom());
            KeyPair pair = keyPairGenerator.generateKeyPair();
            X500Principal principal = new X500Principal(CertificateManagementConstants.DEFAULT_PRINCIPAL);

            X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                    principal, CommonUtil.generateSerialNumber(), validityBeginDate, validityEndDate,
                    principal, pair.getPublic());
            ContentSigner contentSigner = new JcaContentSignerBuilder(CertificateManagementConstants.SHA256_RSA)
                    .setProvider(CertificateManagementConstants.PROVIDER).build(
                            pair.getPrivate());
            X509Certificate certificate = new JcaX509CertificateConverter()
                    .setProvider(CertificateManagementConstants.PROVIDER).getCertificate(
                            certificateBuilder.build(contentSigner));

            // cert.checkValidity();

            certificate.verify(certificate.getPublicKey());

            List<org.wso2.carbon.certificate.mgt.core.bean.Certificate> certificates = new ArrayList<>();
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

    public byte[] getPKIMessage(InputStream inputStream) throws KeystoreException {

        try {
            CMSSignedData signedData = new CMSSignedData(inputStream);
            Store reqStore = signedData.getCertificates();
            @SuppressWarnings("unchecked")
            Collection<X509CertificateHolder> reqCerts = reqStore.getMatches(null);

            KeyStoreReader keyStoreReader = new KeyStoreReader();
            PrivateKey privateKeyRA = keyStoreReader.getRAPrivateKey();
            PrivateKey privateKeyCA = keyStoreReader.getCAPrivateKey();
            X509Certificate certRA = (X509Certificate) keyStoreReader.getRACertificate();
            X509Certificate certCA = (X509Certificate) keyStoreReader.getCACertificate();

            CertificateFactory certificateFactory = CertificateFactory.getInstance(CertificateManagementConstants.X_509);
            X509CertificateHolder holder = reqCerts.iterator().next();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(holder.getEncoded());
            X509Certificate reqCert = (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);

            PkcsPkiEnvelopeDecoder envelopeDecoder = new PkcsPkiEnvelopeDecoder(certRA, privateKeyRA);
            PkiMessageDecoder messageDecoder = new PkiMessageDecoder(reqCert, envelopeDecoder);
            PkiMessage<?> pkiMessage = messageDecoder.decode(signedData);
            Object msgData = pkiMessage.getMessageData();

            Nonce senderNonce = Nonce.nextNonce();
            TransactionId transId = pkiMessage.getTransactionId();
            Nonce recipientNonce = pkiMessage.getSenderNonce();
            CertRep certRep;

            PKCS10CertificationRequest certRequest = (PKCS10CertificationRequest) msgData;
            X509Certificate generatedCert = generateCertificateFromCSR(
                    privateKeyCA, certRequest, certCA.getIssuerX500Principal().getName());

            List<X509Certificate> issued = new ArrayList<X509Certificate>();
            issued.add(generatedCert);

            if (issued.size() == 0) {
                certRep = new CertRep(transId, senderNonce, recipientNonce, FailInfo.badCertId);
            } else {
                CMSSignedData messageData = getMessageData(issued);
                certRep = new CertRep(transId, senderNonce, recipientNonce, messageData);
            }

            PkcsPkiEnvelopeEncoder envEncoder = new PkcsPkiEnvelopeEncoder(reqCert, CertificateManagementConstants.DES_EDE);
            PkiMessageEncoder encoder = new PkiMessageEncoder(privateKeyRA, certRA, envEncoder);
            CMSSignedData cmsSignedData = encoder.encode(certRep);

            return cmsSignedData.getEncoded();

        } catch (CertificateException e) {
            String errorMsg = "Certificate issue occurred when generating getPKIMessage";
            throw new KeystoreException(errorMsg, e);
        } catch (MessageEncodingException e) {
            String errorMsg = "Message encoding issue occurred when generating getPKIMessage";
            throw new KeystoreException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Input output issue occurred when generating getPKIMessage";
            throw new KeystoreException(errorMsg, e);
        } catch (MessageDecodingException e) {
            String errorMsg = "Message decoding issue occurred when generating getPKIMessage";
            throw new KeystoreException(errorMsg, e);
        } catch (CMSException e) {
            String errorMsg = "CMS issue occurred when generating getPKIMessage";
            throw new KeystoreException(errorMsg, e);
        }
    }

    public boolean verifySignature(String headerSignature) throws KeystoreException {
        Certificate certificate = extractCertificateFromSignature(headerSignature);
        return (certificate != null);
    }

    public CertificateResponse verifyPEMSignature(X509Certificate requestCertificate) throws KeystoreException {
        if (requestCertificate == null) {
            throw new IllegalArgumentException("Certificate of which the signature needs to be validated cannot " +
                    "be null");
        }
        KeyStoreReader keyStoreReader = new KeyStoreReader();
        CertificateResponse lookUpCertificate;

        String commonNameExtracted = getCommonName(requestCertificate);
        lookUpCertificate = keyStoreReader.getCertificateBySerial(commonNameExtracted);
        return lookUpCertificate;
    }

    public CertificateResponse verifyCertificateDN(String distinguishedName) throws KeystoreException {
        CertificateResponse lookUpCertificate = null;
        KeyStoreReader keyStoreReader = new KeyStoreReader();
        if (distinguishedName != null && !distinguishedName.isEmpty()) {
            String[] dnSplits = distinguishedName.split("/CN=");
            if (dnSplits != null) {
                String commonNameExtracted = dnSplits[dnSplits.length - 1];
                lookUpCertificate = keyStoreReader.getCertificateBySerial(commonNameExtracted);
            }
        }
        return lookUpCertificate;
    }

    public X509Certificate pemToX509Certificate(String pem)
            throws KeystoreException {
        InputStream inputStream = null;
        X509Certificate x509Certificate = null;
        try {

            inputStream = new ByteArrayInputStream(Base64.decodeBase64(pem.getBytes()));
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            x509Certificate = (X509Certificate) cf.generateCertificate(inputStream);

        } catch (CertificateException e) {
            String errorMsg = "Certificate issue occurred when generating converting PEM to x509Certificate";
            log.error(errorMsg, e);
            throw new KeystoreException(errorMsg, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("Error closing Certificate input stream", e);
            }
        }
        return x509Certificate;
    }

    public X509Certificate extractCertificateFromSignature(String headerSignature) throws KeystoreException {

        if (headerSignature == null || headerSignature.isEmpty()) {
            return null;
        }

        try {
            KeyStoreReader keyStoreReader = new KeyStoreReader();
            CMSSignedData signedData = new CMSSignedData(Base64.decodeBase64(headerSignature.getBytes()));
            Store reqStore = signedData.getCertificates();
            @SuppressWarnings("unchecked")
            Collection<X509CertificateHolder> reqCerts = reqStore.getMatches(null);

            if (reqCerts != null && reqCerts.size() > 0) {
                CertificateFactory certificateFactory = CertificateFactory.getInstance(CertificateManagementConstants.X_509);
                X509CertificateHolder holder = reqCerts.iterator().next();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(holder.getEncoded());
                X509Certificate reqCert = (X509Certificate) certificateFactory.
                        generateCertificate(byteArrayInputStream);

                if (reqCert != null && reqCert.getSerialNumber() != null) {
                    Certificate lookUpCertificate = keyStoreReader.getCertificateByAlias(
                            reqCert.getSerialNumber().toString());

                    if (lookUpCertificate instanceof X509Certificate) {
                        return (X509Certificate) lookUpCertificate;
                    }
                }

            }
        } catch (CMSException e) {
            String errorMsg = "CMSException when decoding certificate signature";
            throw new KeystoreException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "IOException when decoding certificate signature";
            throw new KeystoreException(errorMsg, e);
        } catch (CertificateException e) {
            String errorMsg = "CertificateException when decoding certificate signature";
            throw new KeystoreException(errorMsg, e);
        }

        return null;
    }

    public X509Certificate generateCertificateFromCSR(PrivateKey privateKey,
                                                      PKCS10CertificationRequest request,
                                                      String issueSubject)
            throws KeystoreException {

        CommonUtil commonUtil = new CommonUtil();
        Date validityBeginDate = commonUtil.getValidityStartDate();
        Date validityEndDate = commonUtil.getValidityEndDate();

        X500Name certSubject = new X500Name(CertificateManagementConstants.DEFAULT_PRINCIPAL);
        //X500Name certSubject = request.getSubject();

        Attribute attributes[] = request.getAttributes();

//        if (certSubject == null) {
//            certSubject = new X500Name(ConfigurationUtil.DEFAULT_PRINCIPAL);
//        } else {
//            org.bouncycastle.asn1.x500.RDN[] rdn = certSubject.getRDNs();
//
//            if (rdn == null || rdn.length == 0) {
//                certSubject = new X500Name(ConfigurationUtil.DEFAULT_PRINCIPAL);
//            }
//        }


        RDN[] certUniqueIdRDN;
        BigInteger certUniqueIdentifier;

        // IMPORTANT: "Serial-Number" of the certificate used when creating it, is set as its "Alias" to save to
        // keystore.
        if (request.getSubject().getRDNs(BCStyle.UNIQUE_IDENTIFIER).length != 0) {
            // if certificate attribute "UNIQUE_IDENTIFIER" exists use its hash as the "Serial-Number" for the
            // certificate.
            certUniqueIdRDN = request.getSubject().getRDNs(BCStyle.UNIQUE_IDENTIFIER);
            certUniqueIdentifier = BigInteger.valueOf(certUniqueIdRDN[0].getFirst().getValue().toString().hashCode());

        } else if (request.getSubject().getRDNs(BCStyle.SERIALNUMBER).length != 0) {
            // else if certificate attribute "SERIAL_NUMBER" exists use its hash as the "Serial-Number" for the
            // certificate.
            certUniqueIdRDN = request.getSubject().getRDNs(BCStyle.SERIALNUMBER);
            certUniqueIdentifier = BigInteger.valueOf(certUniqueIdRDN[0].getFirst().getValue().toString().hashCode());

        } else {
            // else get the BigInteger Value of the integer that is the current system-time in millis as the
            // "Serial-Number".
            certUniqueIdentifier = CommonUtil.generateSerialNumber();
        }

        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                new X500Name(issueSubject), certUniqueIdentifier, validityBeginDate, validityEndDate, certSubject,
                request.getSubjectPublicKeyInfo());

        ContentSigner sigGen;
        X509Certificate issuedCert;

        try {
            certificateBuilder.addExtension(X509Extension.keyUsage, true, new KeyUsage(
                    KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

            if (attributes != null) {
                ASN1Encodable extractedValue = getChallengePassword(attributes);

                if (extractedValue != null) {
                    certificateBuilder.addExtension(PKCSObjectIdentifiers.pkcs_9_at_challengePassword, true,
                                                    extractedValue);
                }
            }

            sigGen = new JcaContentSignerBuilder(CertificateManagementConstants.SHA256_RSA)
                    .setProvider(CertificateManagementConstants.PROVIDER).build(privateKey);
            issuedCert = new JcaX509CertificateConverter().setProvider(
                    CertificateManagementConstants.PROVIDER).getCertificate(
                    certificateBuilder.build(sigGen));
            org.wso2.carbon.certificate.mgt.core.bean.Certificate certificate =
                    new org.wso2.carbon.certificate.mgt.core.bean.Certificate();
            List<org.wso2.carbon.certificate.mgt.core.bean.Certificate> certificates = new ArrayList<>();
            certificate.setTenantId(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            certificate.setCertificate(issuedCert);
            certificates.add(certificate);
            saveCertInKeyStore(certificates);
        } catch (CertIOException e) {
            String errorMsg = "Certificate Input output issue occurred when generating generateCertificateFromCSR";
            throw new KeystoreException(errorMsg, e);
        } catch (OperatorCreationException e) {
            String errorMsg = "Operator creation issue occurred when generating generateCertificateFromCSR";
            throw new KeystoreException(errorMsg, e);
        } catch (CertificateException e) {
            String errorMsg = "Certificate issue occurred when generating generateCertificateFromCSR";
            throw new KeystoreException(errorMsg, e);
        }

        return issuedCert;
    }

    private ASN1Encodable getChallengePassword(Attribute[] attributes) {

        for (Attribute attribute : attributes) {
            if (PKCSObjectIdentifiers.pkcs_9_at_challengePassword.equals(attribute.getAttrType())) {
                if (attribute.getAttrValues() != null && attribute.getAttrValues().size() > 0) {
                    return attribute.getAttrValues().getObjectAt(0);
                }
            }
        }

        return null;
    }

    private CMSSignedData getMessageData(final List<X509Certificate> certs) throws KeystoreException {

        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        JcaCertStore store;
        try {
            store = new JcaCertStore(certs);
            generator.addCertificates(store);

            return generator.generate(new CMSAbsentContent());
        } catch (CertificateEncodingException e) {
            String errorMsg = "Certificate encoding issue occurred when generating getMessageData";
            throw new KeystoreException(errorMsg, e);
        } catch (CMSException e) {
            String errorMsg = "Message decoding issue occurred when generating getMessageData";
            throw new KeystoreException(errorMsg, e);
        }
    }

//    private PrivateKey getSignerKey(String signerPrivateKeyPath) throws KeystoreException {
//
//        File file = new File(signerPrivateKeyPath);
//        FileInputStream fis;
//
//        try {
//            fis = new FileInputStream(file);
//            DataInputStream dis = new DataInputStream(fis);
//            byte[] keyBytes = new byte[(int) file.length()];
//            dis.readFully(keyBytes);
//            dis.close();
//
//            String temp = new String(keyBytes);
//            String privateKeyPEM = temp.replace(
//                    CertificateManagementConstants.RSA_PRIVATE_KEY_BEGIN_TEXT, CertificateManagementConstants.EMPTY_TEXT);
//            privateKeyPEM = privateKeyPEM
//                    .replace(CertificateManagementConstants.RSA_PRIVATE_KEY_END_TEXT, CertificateManagementConstants.EMPTY_TEXT);
//
//            byte[] decoded = Base64.decodeBase64(privateKeyPEM);
//            PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(decoded);
//            KeyFactory keyFactory = KeyFactory.getInstance(CertificateManagementConstants.RSA);
//
//            return keyFactory.generatePrivate(encodedKeySpec);
//        } catch (FileNotFoundException e) {
//            String errorMsg = "Private key file not found in getSignerKey";
//            throw new KeystoreException(errorMsg, e);
//        } catch (IOException e) {
//            String errorMsg = "Input output issue in getSignerKey";
//            throw new KeystoreException(errorMsg, e);
//        } catch (NoSuchAlgorithmException e) {
//            String errorMsg = "Algorithm not not found in getSignerKey";
//            throw new KeystoreException(errorMsg, e);
//        } catch (InvalidKeySpecException e) {
//            String errorMsg = "Invalid key found in getSignerKey";
//            throw new KeystoreException(errorMsg, e);
//        }
//    }
//
//    private X509Certificate getSigner(String signerCertificatePath) throws KeystoreException {
//
//        X509Certificate certificate;
//        try {
//            CertificateFactory certificateFactory = CertificateFactory.getInstance(CertificateManagementConstants.X_509);
//            certificate = (X509Certificate) certificateFactory.generateCertificate(
//                    new FileInputStream(signerCertificatePath));
//
//            return certificate;
//        } catch (CertificateException e) {
//            String errorMsg = "Certificate related issue occurred in getSigner";
//            throw new KeystoreException(errorMsg, e);
//        } catch (FileNotFoundException e) {
//            String errorMsg = "Signer certificate path not found in getSigner";
//            throw new KeystoreException(errorMsg, e);
//        }
//    }

    public SCEPResponse getCACert() throws KeystoreException {

        try {
            SCEPResponse scepResponse = new SCEPResponse();
            KeyStoreReader keyStoreReader = new KeyStoreReader();

            byte[] caBytes = keyStoreReader.getCACertificate().getEncoded();
            byte[] raBytes = keyStoreReader.getRACertificate().getEncoded();

            final List<X509Certificate> certs = getRootCertificates(caBytes, raBytes);

            byte[] bytes;
            if (certs.size() == 0) {
                scepResponse.setResultCriteria(CAStatus.CA_CERT_FAILED);
                bytes = new byte[0];
            } else if (certs.size() == 1) {
                scepResponse.setResultCriteria(CAStatus.CA_CERT_RECEIVED);
                bytes = certs.get(0).getEncoded();
            } else {
                scepResponse.setResultCriteria(CAStatus.CA_RA_CERT_RECEIVED);
                CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
                JcaCertStore store = new JcaCertStore(certs);
                generator.addCertificates(store);
                CMSSignedData degenerateSd = generator.generate(new CMSAbsentContent());
                bytes = degenerateSd.getEncoded();
            }
            scepResponse.setEncodedResponse(bytes);

            return scepResponse;
        } catch (CertificateEncodingException e) {
            String errorMsg = "Certificate encoding issue occurred in getCACert";
            throw new KeystoreException(errorMsg, e);
        } catch (CMSException e) {
            String errorMsg = "CMS issue occurred in getCACert";
            throw new KeystoreException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Input output issue occurred in getCACert";
            throw new KeystoreException(errorMsg, e);
        }
    }

    public void saveCertInKeyStore(List<org.wso2.carbon.certificate.mgt.core.bean.Certificate> certificate)
            throws KeystoreException {

        if (certificate == null) {
            return;
        }

        try {
            CertificateDAO certificateDAO = CertificateManagementDAOFactory.getCertificateDAO();
            CertificateManagementDAOFactory.beginTransaction();
            certificateDAO.addCertificate(certificate);
            CertificateManagementDAOFactory.commitTransaction();
        } catch (CertificateManagementDAOException e) {
            String errorMsg = "Error occurred when saving the generated certificate";
            CertificateManagementDAOFactory.rollbackTransaction();
            throw new KeystoreException(errorMsg, e);
        } catch (TransactionManagementException e) {
            String errorMsg = "Error occurred when saving the generated certificate";
            throw new KeystoreException(errorMsg, e);
        }
    }

    public String extractChallengeToken(X509Certificate certificate) {

        byte[] challengePassword = certificate.getExtensionValue(
                PKCSObjectIdentifiers.pkcs_9_at_challengePassword.toString());

        if (challengePassword != null) {
            return new String(challengePassword);
        }

        return null;
    }

//    private ASN1Primitive toASN1Primitive(byte[] data) {
//
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
//        ASN1InputStream inputStream = new ASN1InputStream(byteArrayInputStream);
//
//        try {
//            return inputStream.readObject();
//        } catch (IOException e) {
//            String errorMsg = "IOException occurred when converting binary array to ASN1Primitive";
//            log.error(errorMsg, e);
//        } finally {
//            try {
//                byteArrayInputStream.close();
//                inputStream.close();
//            } catch (IOException e) {
//                String errorMsg = "IOException occurred when closing streams";
//                log.error(errorMsg, e);
//            }
//        }
//
//        return null;
//    }

    /**
     * This method is used to retrieve signed certificate from certificate signing request.
     *
     * @param binarySecurityToken CSR that comes from the client as a String value.It is base 64 encoded request
     *                            security token.
     * @return Return signed certificate in X508Certificate type object.
     * @throws KeystoreException
     */
    public X509Certificate getSignedCertificateFromCSR(String binarySecurityToken)
            throws KeystoreException {
        byte[] byteArrayBst = DatatypeConverter.parseBase64Binary(binarySecurityToken);
        PKCS10CertificationRequest certificationRequest;
        KeyStoreReader keyStoreReader = new KeyStoreReader();
        PrivateKey privateKeyCA = keyStoreReader.getCAPrivateKey();
        X509Certificate certCA = (X509Certificate) keyStoreReader.getCACertificate();

        try {
            certificationRequest = new PKCS10CertificationRequest(byteArrayBst);
        } catch (IOException e) {
            throw new KeystoreException("CSR cannot be recovered.", e);
        }
        return generateCertificateFromCSR(privateKeyCA, certificationRequest,
                certCA.getIssuerX500Principal().getName());
    }
}