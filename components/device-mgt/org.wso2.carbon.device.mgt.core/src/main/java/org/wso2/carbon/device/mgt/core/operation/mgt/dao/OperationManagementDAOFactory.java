/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.core.operation.mgt.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.device.mgt.core.config.datasource.JNDILookupDefinition;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.impl.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

public class OperationManagementDAOFactory {

    private static DataSource dataSource;
    private static final Log log = LogFactory.getLog(OperationManagementDAOFactory.class);
    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>();

    public static OperationDAO getCommandOperationDAO() {
        return new CommandOperationDAOImpl();
    }

    public static OperationDAO getConfigOperationDAO() {
        return new ConfigOperationDAOImpl();
    }

    public static OperationDAO getProfileOperationDAO() {
        return new ProfileOperationDAOImpl();
    }

    public static OperationDAO getPolicyOperationDAO() {
        return new PolicyOperationDAOImpl();
    }

    public static OperationMappingDAO getOperationMappingDAO() {
        return new OperationMappingDAOImpl();
    }

    public static OperationDAO getOperationDAO() {
        return new OperationDAOImpl();
    }

    public static void init(DataSource dtSource) {
        dataSource = dtSource;
    }

    public static void init(DataSourceConfig config) {
        dataSource = resolveDataSource(config);
    }

    public static void beginTransaction() throws OperationManagementDAOException {

        try {
            currentConnection.set(dataSource.getConnection());
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while retrieving datasource connection", e);
        }
    }

    public static Connection getConnection() throws OperationManagementDAOException {

        if (currentConnection.get() == null) {
            try {
                currentConnection.set(dataSource.getConnection());
            } catch (SQLException e) {
                throw new OperationManagementDAOException("Error occurred while retrieving data source connection",
                        e);
            }
        }
        return currentConnection.get();
    }

    public static void closeConnection() throws OperationManagementDAOException {

        Connection con = currentConnection.get();
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error("Error occurred while close the connection");
            }
            currentConnection.remove();
        }
    }

    public static void commitTransaction() throws OperationManagementDAOException {
        try {
            Connection conn = currentConnection.get();
            if (conn != null) {
                conn.commit();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Datasource connection associated with the current thread is null, hence commit " +
                            "has not been attempted");
                }
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while committing the transaction", e);
        } finally {
            closeConnection();
        }
    }

    public static void rollbackTransaction() throws OperationManagementDAOException {
        try {
            Connection conn = currentConnection.get();
            if (conn != null) {
                conn.rollback();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Datasource connection associated with the current thread is null, hence rollback " +
                            "has not been attempted");
                }
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while rollbacking the transaction", e);
        } finally {
            closeConnection();
        }
    }

    /**
     * Resolve data source from the data source definition
     *
     * @param config data source configuration
     * @return data source resolved from the data source definition
     */
    private static DataSource resolveDataSource(DataSourceConfig config) {
        DataSource dataSource = null;
        if (config == null) {
            throw new RuntimeException(
                    "Device Management Repository data source configuration " + "is null and " +
                            "thus, is not initialized");
        }
        JNDILookupDefinition jndiConfig = config.getJndiLookupDefinition();
        if (jndiConfig != null) {
            if (log.isDebugEnabled()) {
                log.debug("Initializing Device Management Repository data source using the JNDI " +
                        "Lookup Definition");
            }
            List<JNDILookupDefinition.JNDIProperty> jndiPropertyList =
                    jndiConfig.getJndiProperties();
            if (jndiPropertyList != null) {
                Hashtable<Object, Object> jndiProperties = new Hashtable<Object, Object>();
                for (JNDILookupDefinition.JNDIProperty prop : jndiPropertyList) {
                    jndiProperties.put(prop.getName(), prop.getValue());
                }
                dataSource = DeviceManagementDAOUtil
                        .lookupDataSource(jndiConfig.getJndiName(), jndiProperties);
            } else {
                dataSource =
                        DeviceManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), null);
            }
        }
        return dataSource;
    }

}
