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

package org.wso2.carbon.device.mgt.analytics.dashboard.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.impl.GenericGadgetDataServiceDAOImpl;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.impl.MSSQLGadgetDataServiceDAOImpl;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.impl.OracleGadgetDataServiceDAOImpl;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.impl.PostgreSQLGadgetDataServiceDAOImpl;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.IllegalTransactionStateException;
import org.wso2.carbon.device.mgt.common.UnsupportedDatabaseEngineException;
import org.wso2.carbon.device.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.device.mgt.core.config.datasource.JNDILookupDefinition;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

@SuppressWarnings("unused")
public class GadgetDataServiceDAOFactory {

    private static final Log log = LogFactory.getLog(GadgetDataServiceDAOFactory.class);
    private static DataSource dataSource;
    private static String databaseEngine;
    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<>();

    public static GadgetDataServiceDAO getGadgetDataServiceDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_H2:
                    return new GenericGadgetDataServiceDAOImpl();
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_MYSQL:
                    return new GenericGadgetDataServiceDAOImpl();
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_MSSQL:
                    return new MSSQLGadgetDataServiceDAOImpl();
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                    return new PostgreSQLGadgetDataServiceDAOImpl();
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_ORACLE:
                    return new OracleGadgetDataServiceDAOImpl();
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
            log.error("Error occurred while retrieving config.datasource connection.", e);
        }
    }

    public static void init(DataSource dtSource) {
        dataSource = dtSource;
        try {
            databaseEngine = dataSource.getConnection().getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            log.error("Error occurred while retrieving config.datasource connection.", e);
        }
    }

    public static void openConnection() throws SQLException {
        Connection conn = currentConnection.get();
        if (conn != null) {
            throw new IllegalTransactionStateException("A transaction is already active within the context of " +
                "this particular thread. Therefore, calling 'beginTransaction/openConnection' while another " +
                    "transaction is already active is a sign of improper transaction handling.");
        }
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {

        }
        currentConnection.set(conn);
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("No connection is associated with the current transaction. " +
                "This might have ideally been caused by not properly initiating the transaction via " +
                    "'beginTransaction'/'openConnection' methods.");
        }
        return conn;
    }

    public static void closeConnection() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("No connection is associated with the current transaction. " +
                "This might have ideally been caused by not properly initiating the transaction via " +
                    "'beginTransaction'/'openConnection' methods.");
        }
        try {
            conn.close();
        } catch (SQLException e) {
            log.warn("Error occurred while close the connection.");
        }
        currentConnection.remove();
    }


    /**
     * Resolve data source from the data source definition.
     *
     * @param config data source configuration.
     * @return data source resolved from the data source definition.
     */
    private static DataSource resolveDataSource(DataSourceConfig config) {
        DataSource dataSource = null;
        if (config == null) {
            throw new RuntimeException(
                "Device Management Repository data source configuration is null and " +
                    "thus, is not initialized.");
        }
        JNDILookupDefinition jndiConfig = config.getJndiLookupDefinition();
        if (jndiConfig != null) {
            if (log.isDebugEnabled()) {
                log.debug("Initializing Device Management Repository data source using the JNDI Lookup Definition.");
            }
            List<JNDILookupDefinition.JNDIProperty> jndiPropertyList = jndiConfig.getJndiProperties();
            if (jndiPropertyList != null) {
                Hashtable<Object, Object> jndiProperties = new Hashtable<>();
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
