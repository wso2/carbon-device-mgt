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

package org.wso2.carbon.device.mgt.extensions.device.type.deployer.template.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.exception.DeviceTypeMgtPluginException;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.internal.DeviceTypeManagementDataHolder;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Contains utility methods used by plugin.
 */
public class DeviceTypeUtils {

    private static Log log = LogFactory.getLog(DeviceTypeUtils.class);

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
     * Creates the device management schema.
     */
    public static void setupDeviceManagementSchema(String datasourceName, String deviceType, String testTableName)
            throws DeviceTypeMgtPluginException {
        try {
            Context ctx = new InitialContext();
            DataSource dataSource = (DataSource) ctx.lookup(datasourceName);
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
            DeviceSchemaInitializer initializer = new DeviceSchemaInitializer(dataSource, deviceType, tenantDomain);
            String checkSql = "select * from " + testTableName;
            if (!initializer.isDatabaseStructureCreated(checkSql)) {
                log.info("Initializing device management repository database schema");
                initializer.createRegistryDatabase();
            } else {
                log.info("Device management repository database already exists. Not creating a new database.");
            }
        } catch (Exception e) {
            throw new DeviceTypeMgtPluginException("Error occurred while initializing Device " +
                                                                       "Management database schema", e);
        }
    }

    public static Registry getConfigurationRegistry() throws DeviceTypeMgtPluginException {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return DeviceTypeManagementDataHolder.getInstance().getRegistryService()
                    .getConfigSystemRegistry(tenantId);
        } catch (RegistryException e) {
            throw new DeviceTypeMgtPluginException("Error in retrieving conf registry instance: " + e.getMessage(), e);
        }
    }

    public static boolean putRegistryResource(String path, Resource resource) throws DeviceTypeMgtPluginException {
        boolean status;
        try {
            DeviceTypeUtils.getConfigurationRegistry().beginTransaction();
            DeviceTypeUtils.getConfigurationRegistry().put(path, resource);
            DeviceTypeUtils.getConfigurationRegistry().commitTransaction();
            status = true;
        } catch (RegistryException e) {
            throw new DeviceTypeMgtPluginException("Error occurred while persisting registry resource : " +
                            e.getMessage(), e);
        }
        return status;
    }

    public static Resource getRegistryResource(String path) throws DeviceTypeMgtPluginException {
        try {
            if(DeviceTypeUtils.getConfigurationRegistry().resourceExists(path)){
                return DeviceTypeUtils.getConfigurationRegistry().get(path);
            }
            return null;
        } catch (RegistryException e) {
            throw new DeviceTypeMgtPluginException("Error in retrieving registry resource : " + e.getMessage(), e);
        }
    }

}
