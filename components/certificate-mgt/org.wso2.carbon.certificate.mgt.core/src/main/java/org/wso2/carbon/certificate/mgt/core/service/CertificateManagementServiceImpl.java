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
import org.wso2.carbon.certificate.mgt.core.dao.CertificateDAO;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOException;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOFactory;
import org.wso2.carbon.certificate.mgt.core.dto.CertificateResponse;
import org.wso2.carbon.certificate.mgt.core.dto.SCEPResponse;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.impl.CertificateGenerator;
import org.wso2.carbon.certificate.mgt.core.impl.KeyStoreReader;
import org.wso2.carbon.certificate.mgt.core.util.ConfigurationUtil;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.List;

public class CertificateManagementServiceImpl implements CertificateManagementService {

    private static final Log log = LogFactory.getLog(CertificateManagementServiceImpl.class);
    private static CertificateManagementServiceImpl certificateManagementServiceImpl;
    private static KeyStoreReader keyStoreReader;
    private static CertificateGenerator certificateGenerator;

    private CertificateManagementServiceImpl() {
    }

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

    public CertificateResponse verifyPEMSignature(X509Certificate requestCertificate) throws KeystoreException {
        return certificateGenerator.verifyPEMSignature(requestCertificate);
    }

    public X509Certificate extractCertificateFromSignature(String headerSignature) throws KeystoreException {
        return certificateGenerator.extractCertificateFromSignature(headerSignature);
    }

    public String extractChallengeToken(X509Certificate certificate) {
        return certificateGenerator.extractChallengeToken(certificate);
    }

    public X509Certificate getSignedCertificateFromCSR(String binarySecurityToken) throws KeystoreException {
        return certificateGenerator.getSignedCertificateFromCSR(binarySecurityToken);
    }

    public CertificateResponse getCertificateBySerial(String serial) throws KeystoreException {
        return keyStoreReader.getCertificateBySerial(serial);
    }

    public void saveCertificate(List<org.wso2.carbon.certificate.mgt.core.bean.Certificate> certificate)
            throws KeystoreException {
        certificateGenerator.saveCertInKeyStore(certificate);
    }

    public X509Certificate pemToX509Certificate(String pem) throws KeystoreException {
        return certificateGenerator.pemToX509Certificate(pem);
    }

    public CertificateResponse retrieveCertificate(String serialNumber)
            throws CertificateManagementDAOException {
        CertificateDAO certificateDAO;
        try {
            CertificateManagementDAOFactory.openConnection();
            certificateDAO = CertificateManagementDAOFactory.getCertificateDAO();
            return certificateDAO.retrieveCertificate(serialNumber);
        } catch (SQLException e) {
            String errorMsg = "Error when opening connection";
            log.error(errorMsg, e);
            throw new CertificateManagementDAOException(errorMsg, e);
        } finally {
            CertificateManagementDAOFactory.closeConnection();
        }
    }

    public PaginationResult getAllCertificates(PaginationRequest request)
            throws CertificateManagementDAOException {
        try {
            CertificateManagementDAOFactory.openConnection();
            CertificateDAO certificateDAO = CertificateManagementDAOFactory.getCertificateDAO();
            return certificateDAO.getAllCertificates(request);
        } catch (SQLException e) {
            String errorMsg = "Error when opening connection";
            log.error(errorMsg, e);
            throw new CertificateManagementDAOException(errorMsg, e);
        } finally {
            CertificateManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean removeCertificate(String serialNumber) throws CertificateManagementDAOException {
        try {
            CertificateManagementDAOFactory.openConnection();
            CertificateDAO certificateDAO = CertificateManagementDAOFactory.getCertificateDAO();
            return certificateDAO.removeCertificate(serialNumber);
        } catch (SQLException e) {
            String errorMsg = "Error when opening connection";
            log.error(errorMsg, e);
            throw new CertificateManagementDAOException(errorMsg, e);
        } finally {
            CertificateManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<CertificateResponse> getCertificates() throws CertificateManagementDAOException {
        try {
            CertificateManagementDAOFactory.openConnection();
            CertificateDAO certificateDAO = CertificateManagementDAOFactory.getCertificateDAO();
            return certificateDAO.getAllCertificates();
        } catch (SQLException e) {
            String errorMsg = "Error when opening connection";
            log.error(errorMsg, e);
            throw new CertificateManagementDAOException(errorMsg, e);
        } finally {
            CertificateManagementDAOFactory.closeConnection();
        }
    }

}
