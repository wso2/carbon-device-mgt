/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.policy.mgt.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.policy.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.policy.mgt.core.config.datasource.JNDILookupDefinition;
import org.wso2.carbon.policy.mgt.core.dao.impl.FeatureDAOImpl;
import org.wso2.carbon.policy.mgt.core.dao.impl.MonitoringDAOImpl;
import org.wso2.carbon.policy.mgt.core.dao.impl.PolicyDAOImpl;
import org.wso2.carbon.policy.mgt.core.dao.impl.ProfileDAOImpl;
import org.wso2.carbon.policy.mgt.core.dao.util.PolicyManagementDAOUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

public class PolicyManagementDAOFactory {

    private static DataSource dataSource;
    private static final Log log = LogFactory.getLog(PolicyManagementDAOFactory.class);
    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>();

    public static void init(DataSourceConfig config) {
        dataSource = resolveDataSource(config);
    }

    public static void init(DataSource dtSource) {
        dataSource = dtSource;
    }

    public static DataSource getDataSource() {
        if (dataSource != null) {
            return dataSource;
        }
        throw new RuntimeException("Data source is not yet configured.");
    }

    public static PolicyDAO getPolicyDAO() {
        return new PolicyDAOImpl();
    }

    public static ProfileDAO getProfileDAO() {
        return new ProfileDAOImpl();
    }

    public static FeatureDAO getFeatureDAO() {
        return new FeatureDAOImpl();
    }

    public static MonitoringDAO getMonitoringDAO() {
        return new MonitoringDAOImpl();
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
            throw new RuntimeException("Device Management Repository data source configuration " +
                    "is null and thus, is not initialized");
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
                dataSource =
                        PolicyManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), jndiProperties);
            } else {
                dataSource = PolicyManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), null);
            }
        }
        return dataSource;
    }

    public static void beginTransaction() throws PolicyManagerDAOException {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            currentConnection.set(conn);
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while retrieving config.datasource connection", e);
        }
    }

    public static Connection getConnection() throws PolicyManagerDAOException {
        if (currentConnection.get() == null) {
            try {
                Connection conn = dataSource.getConnection();
                conn.setAutoCommit(false);
                currentConnection.set(conn);

            } catch (SQLException e) {
                throw new PolicyManagerDAOException("Error occurred while retrieving data source connection",
                        e);
            }
        }
//        if (log.isDebugEnabled()) {
//            log.debug(" Print the connction : :::::::  " + currentConnection.get().toString());
//            StackTraceElement[] sts = Thread.currentThread().getStackTrace();
//            for (StackTraceElement st: sts) {
//                log.debug(st.getClassName()  +  " -- " + st.getLineNumber());
////                break;
//            }
//            log.debug(Thread.currentThread().getStackTrace());
//        }
        return currentConnection.get();
    }

    public static void closeConnection() throws PolicyManagerDAOException {

        Connection con = currentConnection.get();
        try {
            con.close();
        } catch (SQLException e) {
            log.error("Error occurred while close the connection");
        }
        currentConnection.remove();
    }

    public static void commitTransaction() throws PolicyManagerDAOException {
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
            throw new PolicyManagerDAOException("Error occurred while committing the transaction", e);
        } finally {
            closeConnection();
        }
    }

    public static void rollbackTransaction() throws PolicyManagerDAOException {
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
            throw new PolicyManagerDAOException("Error occurred while rollbacking the transaction", e);
        } finally {
            closeConnection();
        }
    }

}
