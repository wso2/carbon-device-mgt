/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.certificate.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.bean.Certificate;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateDAO;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOException;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOFactory;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOUtil;
import org.wso2.carbon.certificate.mgt.core.dto.CertificateResponse;
import org.wso2.carbon.certificate.mgt.core.impl.CertificateGenerator;
import org.wso2.carbon.certificate.mgt.core.util.Serializer;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the generic implementation of CertificateDAO which can be used to support ANSI db syntax.
 */
public abstract class AbstractCertificateDAOImpl implements CertificateDAO{

    private static final Log log = LogFactory.getLog(GenericCertificateDAOImpl.class);

    @Override
    public void addCertificate(List<Certificate> certificates)
            throws CertificateManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(
                    "INSERT INTO DM_DEVICE_CERTIFICATE (SERIAL_NUMBER, CERTIFICATE, TENANT_ID, USERNAME, NAME)"
                    + " VALUES (?,?,?,?,?)");
            PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.
                                                                                              getThreadLocalCarbonContext();
            String username = threadLocalCarbonContext.getUsername();
            for (Certificate certificate : certificates) {
                // the serial number of the certificate used for its creation is set as its alias.
                String serialNumber = certificate.getSerial();
                if (serialNumber == null || serialNumber.isEmpty()) {
                    serialNumber = String.valueOf(certificate.getCertificate().getSerialNumber());
                }
                byte[] bytes = Serializer.serialize(certificate.getCertificate());

                stmt.setString(1, serialNumber);
                stmt.setBytes(2, bytes);
                stmt.setInt(3, certificate.getTenantId());
                stmt.setString(4, username);
                stmt.setString(5, certificate.getName());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException | IOException e) {
            throw new CertificateManagementDAOException("Error occurred while saving certificates. "
                    , e);
        } finally {
            CertificateManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public CertificateResponse retrieveCertificate(String serialNumber)
            throws CertificateManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        CertificateResponse certificateResponse = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query =
                    "SELECT CERTIFICATE, SERIAL_NUMBER, TENANT_ID, USERNAME FROM"
                    + " DM_DEVICE_CERTIFICATE WHERE SERIAL_NUMBER = ? AND TENANT_ID = ? ";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, serialNumber);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                certificateResponse = new CertificateResponse();
                byte[] certificateBytes = resultSet.getBytes("CERTIFICATE");
                certificateResponse.setCertificate(certificateBytes);
                certificateResponse.setSerialNumber(resultSet.getString("SERIAL_NUMBER"));
                certificateResponse.setTenantId(resultSet.getInt("TENANT_ID"));
                certificateResponse.setUsername(resultSet.getString("USERNAME"));
                CertificateGenerator.extractCertificateDetails(certificateBytes, certificateResponse);
            }
        } catch (SQLException e) {
            String errorMsg =
                    "Unable to get the read the certificate with serial" + serialNumber;
            log.error(errorMsg, e);
            throw new CertificateManagementDAOException(errorMsg, e);
        } finally {
            CertificateManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return certificateResponse;
    }

    @Override
    public List<CertificateResponse> searchCertificate(String serialNumber)
            throws CertificateManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        CertificateResponse certificateResponse = null;
        List<CertificateResponse> certificates = new ArrayList<>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query =
                    "SELECT CERTIFICATE, SERIAL_NUMBER, TENANT_ID, USERNAME FROM DM_DEVICE_CERTIFICATE "
                    + "WHERE SERIAL_NUMBER LIKE ? AND TENANT_ID = ? ";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, "%" + serialNumber + "%");
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                certificateResponse = new CertificateResponse();
                byte[] certificateBytes = resultSet.getBytes("CERTIFICATE");
                certificateResponse.setSerialNumber(resultSet.getString("SERIAL_NUMBER"));
                certificateResponse.setTenantId(resultSet.getInt("TENANT_ID"));
                certificateResponse.setUsername(resultSet.getString("USERNAME"));
                CertificateGenerator.extractCertificateDetails(certificateBytes, certificateResponse);
                certificates.add(certificateResponse);
            }
        } catch (SQLException e) {
            String errorMsg =
                    "Unable to get the read the certificate with serial" + serialNumber;
            log.error(errorMsg, e);
            throw new CertificateManagementDAOException(errorMsg, e);
        } finally {
            CertificateManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return certificates;
    }

    @Override
    public List<CertificateResponse> getAllCertificates() throws CertificateManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        CertificateResponse certificateResponse;
        List<CertificateResponse> certificates = new ArrayList<>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            Connection conn = this.getConnection();
            String sql = "SELECT CERTIFICATE, SERIAL_NUMBER, TENANT_ID, USERNAME"
                         + " FROM DM_DEVICE_CERTIFICATE WHERE TENANT_ID = ? ORDER BY ID DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                certificateResponse = new CertificateResponse();
                byte[] certificateBytes = resultSet.getBytes("CERTIFICATE");
                certificateResponse.setSerialNumber(resultSet.getString("SERIAL_NUMBER"));
                certificateResponse.setTenantId(resultSet.getInt("TENANT_ID"));
                certificateResponse.setUsername(resultSet.getString("USERNAME"));
                CertificateGenerator.extractCertificateDetails(certificateBytes, certificateResponse);
                certificates.add(certificateResponse);
            }
        } catch (SQLException e) {
            String errorMsg = "SQL error occurred while retrieving the certificates.";
            log.error(errorMsg, e);
            throw new CertificateManagementDAOException(errorMsg, e);
        } finally {
            CertificateManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return certificates;
    }

    @Override
    public boolean removeCertificate(String serialNumber) throws CertificateManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query =
                    "DELETE FROM DM_DEVICE_CERTIFICATE WHERE SERIAL_NUMBER = ?" +
                    " AND TENANT_ID = ? ";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, serialNumber);
            stmt.setInt(2, tenantId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            String msg = "Unable to get the read the certificate with serial" + serialNumber;
            log.error(msg, e);
            throw new CertificateManagementDAOException(msg, e);
        } finally {
            CertificateManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    private Connection getConnection() throws SQLException {
        return CertificateManagementDAOFactory.getConnection();
    }
}
