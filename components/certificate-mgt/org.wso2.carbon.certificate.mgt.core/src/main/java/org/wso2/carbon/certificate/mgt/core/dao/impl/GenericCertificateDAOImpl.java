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

package org.wso2.carbon.certificate.mgt.core.dao.impl;

import org.wso2.carbon.certificate.mgt.core.dao.CertificateDAO;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOException;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOFactory;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOUtil;

import java.io.ByteArrayInputStream;
import java.sql.*;

public class GenericCertificateDAOImpl implements CertificateDAO {
    @Override
    public void addCertificate(ByteArrayInputStream byteArrayInputStream, String serialNumber)
            throws CertificateManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("INSERT INTO DM_DEVICE_CERTIFICATE (SERIAL_NUMBER, CERTIFICATE) VALUES (?,?)");
            stmt.setString(1, serialNumber);
            stmt.setObject(2, byteArrayInputStream);
        } catch (SQLException e) {
            throw new CertificateManagementDAOException("Error occurred while saving certificate with serial " +
                                                        serialNumber, e);
        } finally {
            CertificateManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public byte[] retrieveCertificate(String serialNumber)
            throws CertificateManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        byte[] binaryStream = null;
        try {
            conn = this.getConnection();
            String query = "SELECT CERTIFICATE FROM DM_DEVICE_CERTIFICATE WHERE SERIAL_NUMBER = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, serialNumber);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                binaryStream = resultSet.getBytes("CERTIFICATE");
            }
        } catch (SQLException e) {
            throw new CertificateManagementDAOException(
                    "Unable to get the read the certificate with serial" + serialNumber, e);
        } finally {
            CertificateManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return binaryStream;
    }

    private Connection getConnection() throws SQLException {
        return CertificateManagementDAOFactory.getConnection();
    }
}
