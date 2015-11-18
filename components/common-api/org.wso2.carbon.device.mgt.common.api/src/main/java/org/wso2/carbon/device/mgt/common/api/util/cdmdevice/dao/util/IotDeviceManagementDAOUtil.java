/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.common.api.util.cdmdevice.dao.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.api.util.cdmdevice.exception.IotDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.common.api.util.cdmdevice.util.IotDeviceManagementSchemaInitializer;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility method required by IotDeviceManagement DAO classes.
 */
public class IotDeviceManagementDAOUtil {

    private static final Log log = LogFactory.getLog(IotDeviceManagementDAOUtil.class);

    public static void cleanupResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing result set", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing prepared statement", e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing database connection", e);
            }
        }
    }

    public static void cleanupResources(PreparedStatement stmt, ResultSet rs) {
        cleanupResources(null, stmt, rs);
    }

    /**
     * Creates the iot device management schema.
     *
     * @param dataSource Iot data source
     */
    public static void setupIotDeviceManagementSchema(DataSource dataSource) throws
                                                                             IotDeviceMgtPluginException {
        IotDeviceManagementSchemaInitializer initializer =
                new IotDeviceManagementSchemaInitializer(dataSource);
        log.info("Initializing iot device management repository database schema");
        try {
            initializer.createRegistryDatabase();
        } catch (Exception e) {
            throw new IotDeviceMgtPluginException("Error occurred while initializing Iot Device " +
                                                "Management database schema", e);
        }
    }

}
