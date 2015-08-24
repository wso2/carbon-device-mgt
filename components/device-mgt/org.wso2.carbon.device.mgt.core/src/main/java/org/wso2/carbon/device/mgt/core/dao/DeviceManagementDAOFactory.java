/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.device.mgt.core.config.datasource.JNDILookupDefinition;
import org.wso2.carbon.device.mgt.core.dao.impl.*;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

/**
 * This class intends to act as the primary entity that hides all DAO instantiation related complexities and logic so
 * that the business objection handling layer doesn't need to be aware of the same providing seamless plug-ability of
 * different data sources, connection acquisition mechanisms as well as different forms of DAO implementations to the
 * high-level implementations that require device management related metadata persistence.
 * <p/>
 * In addition, this also provides means to handle transactions across multiple device management related DAO objects.
 * Any high-level business logic that requires transaction handling to be done via utility methods provided in
 * DeviceManagementDAOFactory should adhere the following guidelines to avoid any unexpected behaviour that can cause
 * as a result of improper use of the aforementioned utility method.
 * <p/>
 * Any transaction that commits data into the underlying data persistence mechanism MUST follow the sequence of
 * operations mentioned below.
 * <p/>
 * <pre>
 * {@code
 * try {
 *      DeviceManagementDAOFactory.beginTransaction();
 *      .....
 *      DeviceManagementDAOFactory.commitTransaction();
 *      return success;
 * } catch (Exception e) {
 *      DeviceManagementDAOFactory.rollbackTransaction();
 *      throw new DeviceManagementException("Error occurred while ...", e);
 * } finally {
 *      DeviceManagementDAOFactory.closeConnection();
 * }
 * }
 * </pre>
 * <p/>
 * Any transaction that retrieves data from the underlying data persistence mechanism MUST follow the sequence of
 * operations mentioned below.
 * <p/>
 * <pre>
 * {@code
 * try {
 *      DeviceManagementDAOFactory.openConnection();
 *      .....
 * } catch (Exception e) {
 *      throw new DeviceManagementException("Error occurred while ..., e);
 * } finally {
 *      DeviceManagementDAOFactory.closeConnection();
 * }
 * }
 * </pre>
 */
public class DeviceManagementDAOFactory {

    private static DataSource dataSource;
    private static final Log log = LogFactory.getLog(DeviceManagementDAOFactory.class);
    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>();

    public static DeviceDAO getDeviceDAO() {
        return new DeviceDAOImpl();
    }

    public static DeviceTypeDAO getDeviceTypeDAO() {
        return new DeviceTypeDAOImpl();
    }

    public static EnrolmentDAO getEnrollmentDAO() {
        return new EnrolmentDAOImpl();
    }

    public static ApplicationDAO getApplicationDAO() {
        return new ApplicationDAOImpl();
    }

    public static ApplicationMappingDAO getApplicationMappingDAO() {
        return new ApplicationMappingDAOImpl();
    }

    public static void init(DataSourceConfig config) {
        dataSource = resolveDataSource(config);
    }

    public static void init(DataSource dtSource) {
        dataSource = dtSource;
    }

    public static void beginTransaction() throws TransactionManagementException {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            currentConnection.set(conn);
        } catch (SQLException e) {
            throw new TransactionManagementException("Error occurred while retrieving config.datasource connection", e);
        }
    }

    public static void openConnection() throws SQLException {
        currentConnection.set(dataSource.getConnection());
    }

    public static Connection getConnection() throws SQLException {
        if (currentConnection.get() == null) {
            currentConnection.set(dataSource.getConnection());
        }
        return currentConnection.get();
    }

    public static void commitTransaction() {
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
            log.error("Error occurred while committing the transaction", e);
        }
    }

    public static void rollbackTransaction() {
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
            log.warn("Error occurred while rollbacking the transaction", e);
        }
    }

    public static void closeConnection() {
        Connection con = currentConnection.get();
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.warn("Error occurred while close the connection");
            }
            currentConnection.remove();
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
                dataSource = DeviceManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), jndiProperties);
            } else {
                dataSource = DeviceManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), null);
            }
        }
        return dataSource;
    }

}
