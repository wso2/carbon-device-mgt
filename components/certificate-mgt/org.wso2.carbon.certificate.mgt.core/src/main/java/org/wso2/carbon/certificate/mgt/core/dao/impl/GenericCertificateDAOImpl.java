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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOException;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOFactory;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOUtil;
import org.wso2.carbon.certificate.mgt.core.dto.CertificateResponse;
import org.wso2.carbon.certificate.mgt.core.impl.CertificateGenerator;
import org.wso2.carbon.certificate.mgt.core.service.PaginationResult;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the generic implementation of CertificateDAO which can be used to support ANSI db syntax for pagination
 * queries.
 */
public class GenericCertificateDAOImpl extends AbstractCertificateDAOImpl {

    private static final Log log = LogFactory.getLog(GenericCertificateDAOImpl.class);


    private Connection getConnection() throws SQLException {
        return CertificateManagementDAOFactory.getConnection();
    }

    @Override
    public PaginationResult getAllCertificates(int rowNum, int limit) throws CertificateManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        CertificateResponse certificateResponse;
        List<CertificateResponse> certificates = new ArrayList<>();
        PaginationResult paginationResult;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            Connection conn = this.getConnection();
            String sql = "SELECT CERTIFICATE, SERIAL_NUMBER, TENANT_ID, USERNAME FROM "
                         + "DM_DEVICE_CERTIFICATE WHERE TENANT_ID = ? ORDER BY ID DESC LIMIT ?,?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setInt(2, rowNum);
            stmt.setInt(3, limit);
            resultSet = stmt.executeQuery();

            int resultCount = 0;
            while (resultSet.next()) {
                certificateResponse = new CertificateResponse();
                byte[] certificateBytes = resultSet.getBytes("CERTIFICATE");
                certificateResponse.setSerialNumber(resultSet.getString("SERIAL_NUMBER"));
                certificateResponse.setTenantId(resultSet.getInt("TENANT_ID"));
                certificateResponse.setUsername(resultSet.getString("USERNAME"));
                CertificateGenerator.extractCertificateDetails(certificateBytes, certificateResponse);
                certificates.add(certificateResponse);
                resultCount++;
            }
            paginationResult = new PaginationResult();
            paginationResult.setData(certificates);
            paginationResult.setRecordsTotal(resultCount);
        } catch (SQLException e) {
            String errorMsg = "SQL error occurred while retrieving the certificates.";
            log.error(errorMsg, e);
            throw new CertificateManagementDAOException(errorMsg, e);
        } finally {
            CertificateManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return paginationResult;
    }
}
