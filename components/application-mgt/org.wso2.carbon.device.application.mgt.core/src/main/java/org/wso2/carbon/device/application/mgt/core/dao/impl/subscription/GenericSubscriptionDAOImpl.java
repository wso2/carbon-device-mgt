/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.device.application.mgt.core.dao.impl.subscription;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.core.dao.SubscriptionDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;

import java.sql.*;

public class GenericSubscriptionDAOImpl extends AbstractDAOImpl implements SubscriptionDAO {
    private static Log log = LogFactory.getLog(GenericSubscriptionDAOImpl.class);

    @Override
    public int addDeviceApplicationMapping(String deviceIdentifier, String applicationUUID, boolean installed) throws
            ApplicationManagementException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int mappingId = -1;
        try {
            conn = this.getDBConnection();
            String sql = "SELECT ID FROM APPM_DEVICE_APPLICATION_MAPPING WHERE DEVICE_IDENTIFIER = ? AND " +
                    "APPLICATION_UUID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceIdentifier);
            stmt.setString(2, applicationUUID);
            rs = stmt.executeQuery();

            if (!rs.next()) {
                sql = "INSERT INTO APPM_DEVICE_APPLICATION_MAPPING (DEVICE_IDENTIFIER, APPLICATION_UUID, " +
                        "INSTALLED) VALUES (?, ?, ?)";
                stmt = conn.prepareStatement(sql, new String[]{"id"});
                stmt.setString(1, deviceIdentifier);
                stmt.setString(2, applicationUUID);
                stmt.setBoolean(3, installed);
                stmt.executeUpdate();

                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    mappingId = rs.getInt(1);
                }
                return mappingId;
            } else {
                log.warn("Device[" + deviceIdentifier + "] application[" + applicationUUID + "] mapping already " +
                        "exists in the DB");
                return -1;
            }
        } catch (SQLException e) {
            throw new ApplicationManagementException("Error occurred while adding device application mapping to DB", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }
}
