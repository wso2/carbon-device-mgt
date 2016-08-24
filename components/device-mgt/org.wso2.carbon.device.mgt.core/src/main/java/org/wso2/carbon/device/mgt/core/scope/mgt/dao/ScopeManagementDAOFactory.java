/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.scope.mgt.dao;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.IllegalTransactionStateException;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.scope.mgt.dao.impl.ScopeManagementDAOImpl;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ScopeManagementDAOFactory {

    private static final Log log = LogFactory.getLog(ScopeManagementDAOFactory.class);
    private static DataSource dataSource;
    private static String databaseEngine;
    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>();

    public static ScopeManagementDAO getScopeManagementDAO() {
        return new ScopeManagementDAOImpl();
    }

    public static void init(String dataSourceName) {
        dataSource = resolveDataSource(dataSourceName);
        try {
            databaseEngine = dataSource.getConnection().getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            log.error("Error occurred while retrieving config.datasource connection", e);
        }
    }

    public static void beginTransaction() throws TransactionManagementException {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            currentConnection.set(conn);
        } catch (SQLException e) {
            throw new TransactionManagementException(
                    "Error occurred while retrieving config.datasource connection", e);
        }
    }

    public static void openConnection() throws SQLException {
        currentConnection.set(dataSource.getConnection());
    }

    public static Connection getConnection() throws SQLException {
        if (currentConnection.get() == null) {
            throw new IllegalTransactionStateException("No connection is associated with the current transaction. " +
                    "This might have ideally caused by not properly initiating the transaction via " +
                    "'beginTransaction'/'openConnection' methods");
        }
        return currentConnection.get();
    }

    public static void closeConnection() {
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
            log.error("Error occurred while roll-backing the transaction", e);
        }
    }

    /**
     * Resolve data source from the data source name.
     *
     * @param dataSourceName data source name
     * @return data source resolved from the data source definition
     */
    private static DataSource resolveDataSource(String dataSourceName) {
        DataSource dataSource;
        if (dataSourceName == null || dataSourceName.isEmpty()) {
            throw new RuntimeException("Scope Management Repository data source configuration is null and " +
                    "thus, is not initialized");
        }
        if (log.isDebugEnabled()) {
            log.debug("Initializing Scope Management Repository data source using the JNDI Lookup Definition");
        }
        dataSource = DeviceManagementDAOUtil.lookupDataSource(dataSourceName, null);
        return dataSource;
    }

}
