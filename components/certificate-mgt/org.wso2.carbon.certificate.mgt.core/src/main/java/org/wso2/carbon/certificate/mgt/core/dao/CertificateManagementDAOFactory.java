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

package org.wso2.carbon.certificate.mgt.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.certificate.mgt.core.config.datasource.JNDILookupDefinition;
import org.wso2.carbon.certificate.mgt.core.dao.impl.GenericCertificateDAOImpl;
import org.wso2.carbon.certificate.mgt.core.dao.impl.OracleCertificateDAOImpl;
import org.wso2.carbon.certificate.mgt.core.dao.impl.PostgreSQLCertificateDAOImpl;
import org.wso2.carbon.certificate.mgt.core.dao.impl.SQLServerCertificateDAOImpl;
import org.wso2.carbon.certificate.mgt.core.exception.IllegalTransactionStateException;
import org.wso2.carbon.certificate.mgt.core.exception.TransactionManagementException;
import org.wso2.carbon.certificate.mgt.core.exception.UnsupportedDatabaseEngineException;
import org.wso2.carbon.certificate.mgt.core.util.CertificateManagementConstants;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

public class CertificateManagementDAOFactory {

    private static DataSource dataSource;
    private static String databaseEngine;
    private static final Log log = LogFactory.getLog(CertificateManagementDAOFactory.class);
    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<>();
    private static ThreadLocal<TxState> currentTxState = new ThreadLocal<>();

    private enum TxState {
        CONNECTION_NOT_BORROWED, CONNECTION_BORROWED, CONNECTION_CLOSED
    }

    public static CertificateDAO getCertificateDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case CertificateManagementConstants.DataBaseTypes.DB_TYPE_ORACLE:
                    return new OracleCertificateDAOImpl();
                case CertificateManagementConstants.DataBaseTypes.DB_TYPE_MSSQL:
                    return new SQLServerCertificateDAOImpl();
                case CertificateManagementConstants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                    return new PostgreSQLCertificateDAOImpl();
                case CertificateManagementConstants.DataBaseTypes.DB_TYPE_H2:
                case CertificateManagementConstants.DataBaseTypes.DB_TYPE_MYSQL:
                    return new GenericCertificateDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    public static void init(DataSourceConfig config) {
        dataSource = resolveDataSource(config);
        try {
            databaseEngine = dataSource.getConnection().getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            log.error("Error occurred while retrieving config.datasource connection", e);
        }
    }

    public static void init(DataSource dtSource) {
        dataSource = dtSource;
        try {
            databaseEngine = dataSource.getConnection().getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            log.error("Error occurred while retrieving a datasource connection", e);
        }
    }

    public static void beginTransaction() throws TransactionManagementException {
        Connection conn = currentConnection.get();
        if (conn != null) {
            throw new IllegalTransactionStateException("A transaction is already active within the context of " +
                                                       "this particular thread. Therefore, calling 'beginTransaction/openConnection' while another " +
                                                       "transaction is already active is a sign of improper transaction handling");
        }
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            throw new TransactionManagementException("Error occurred while retrieving a data source connection", e);
        }

        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            try {
                conn.close();
            } catch (SQLException e1) {
                log.warn("Error occurred while closing the borrowed connection. " +
                        "Transaction has ended pre-maturely", e1);
            }
            currentTxState.set(TxState.CONNECTION_CLOSED);
            throw new TransactionManagementException("Error occurred while setting auto-commit to false", e);
        }
        currentConnection.set(conn);
        currentTxState.set(TxState.CONNECTION_BORROWED);
    }

    public static void openConnection() throws SQLException {
        Connection conn = currentConnection.get();
        if (conn != null) {
            throw new IllegalTransactionStateException("A transaction is already active within the context of " +
                    "this particular thread. Therefore, calling 'beginTransaction/openConnection' while another " +
                    "transaction is already active is a sign of improper transaction handling");
        }
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            currentTxState.set(TxState.CONNECTION_NOT_BORROWED);
            throw e;
        }
        currentConnection.set(conn);
        currentTxState.set(TxState.CONNECTION_BORROWED);
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("No connection is associated with the current transaction. " +
                    "This might have ideally been caused by not properly initiating the transaction via " +
                    "'beginTransaction'/'openConnection' methods");
        }
        return conn;
    }

    public static void commitTransaction() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("No connection is associated with the current transaction. " +
                    "This might have ideally been caused by not properly initiating the transaction via " +
                    "'beginTransaction'/'openConnection' methods");
        }
        try {
            conn.commit();
        } catch (SQLException e) {
            log.error("Error occurred while committing the transaction", e);
        } finally {
            closeConnection();
        }
    }

    public static void rollbackTransaction() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("No connection is associated with the current transaction. " +
                    "This might have ideally been caused by not properly initiating the transaction via " +
                    "'beginTransaction'/'openConnection' methods");
        }
        try {
            conn.rollback();
        } catch (SQLException e) {
            log.warn("Error occurred while roll-backing the transaction", e);
        } finally {
            closeConnection();
        }
    }

    public static void closeConnection() {
        TxState txState = currentTxState.get();
        if (TxState.CONNECTION_NOT_BORROWED == txState) {
            if (log.isDebugEnabled()) {
                log.debug("No successful connection appears to have been borrowed to perform the underlying " +
                        "transaction even though the 'openConnection' method has been called. Therefore, " +
                        "'closeConnection' method is returning silently");
            }
            currentTxState.remove();
            return;
        }

        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("No connection is associated with the current transaction. " +
                    "This might have ideally been caused by not properly initiating the transaction via " +
                    "'beginTransaction'/'openConnection' methods");
        }
        try {
            conn.close();
        } catch (SQLException e) {
            log.warn("Error occurred while close the connection", e);
        }
        currentConnection.remove();
        currentTxState.remove();
    }


    /**
     * Resolve data source from the data source definition.
     *
     * @param config data source configuration
     * @return data source resolved from the data source definition
     */
    private static DataSource resolveDataSource(DataSourceConfig config) {
        DataSource dataSource = null;
        if (config == null) {
            throw new RuntimeException(
                    "Device Management Repository data source configuration " + "is null and " +
                            "thus, is not initialized"
            );
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
                dataSource = CertificateManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), jndiProperties);
            } else {
                dataSource = CertificateManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), null);
            }
        }
        return dataSource;
    }

}
