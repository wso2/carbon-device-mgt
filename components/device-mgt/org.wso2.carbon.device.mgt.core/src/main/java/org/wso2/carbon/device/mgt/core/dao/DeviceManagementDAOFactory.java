/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.device.mgt.core.config.datasource.JNDILookupDefinition;
import org.wso2.carbon.device.mgt.core.dao.impl.*;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

public class DeviceManagementDAOFactory {

    private static DataSource dataSource;
    private static final Log log = LogFactory.getLog(DeviceManagementDAOFactory.class);
    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>();
    private static ThreadLocal<TransactionManager> currentTransaction = new ThreadLocal<TransactionManager>();
    private static ThreadLocal<XAResource> currentResource = new ThreadLocal<XAResource>();
    private static boolean isXADataSource = false;

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
        isXADataSource = config.isXAEnabled();
        dataSource = resolveDataSource(config);
    }

    public static void init(DataSource dtSource) {
        dataSource = dtSource;
    }

    public static void beginTransaction() throws DeviceManagementDAOException {

        if (isXADataSource) {
            if (currentTransaction.get() == null) {
                try {
                    TransactionManager transactionManager = getTransaction();
                    transactionManager.begin();
                    currentTransaction.set(transactionManager);
                    currentResource.remove();
                }catch(Exception ex){
                    String errorMsg = "Error in begin transaction";
                    log.error(errorMsg, ex);
                    throw new DeviceManagementDAOException(errorMsg, ex);
                }
            }
        } else {
            try {
                Connection conn = dataSource.getConnection();
                conn.setAutoCommit(false);
                currentConnection.set(conn);
            } catch (SQLException e) {
                throw new DeviceManagementDAOException("Error occurred while retrieving config.datasource connection",
                        e);
            }
        }
    }

    private static TransactionManager getTransaction() throws DeviceManagementDAOException {

        TransactionManager transactionManager;

        try {
            transactionManager = InitialContext.doLookup(
                    org.wso2.carbon.device.mgt.core.DeviceManagementConstants.Common.STANDARD_TRANSACTION_MANAGER_JNDI_NAME);
        } catch (NamingException e) {
            String errorMsg = "Naming exception occurred lookup " + org.wso2.carbon.device.mgt.core
                    .DeviceManagementConstants
                    .Common.STANDARD_TRANSACTION_MANAGER_JNDI_NAME;
            log.error(errorMsg, e);
            throw new DeviceManagementDAOException(errorMsg, e);
        }
        return transactionManager;
    }

    public static void openConnection() throws DeviceManagementDAOException {
        try {
            currentConnection.set(dataSource.getConnection());
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while acquiring config.datasource connection", e);
        }
    }

    public static Connection getConnection() throws DeviceManagementDAOException {
        if (currentConnection.get() == null) {
            try {
                currentConnection.set(dataSource.getConnection());
                if (isXADataSource && currentTransaction.get() != null && currentResource.get() == null) {
                    XAResource resource = ((XAConnection) currentConnection.get()).getXAResource();
                    try {
                        currentTransaction.get().getTransaction().enlistResource(resource);
                        currentResource.set(resource);
                    } catch (Exception e) {
                        // Above code block throws rollback and sql exceptions. But catch generic exception to
                        // communicate
                        // common error
                        String errorMsg = "Error occurred while enlist the resource";
                        log.error(errorMsg, e);
                        throw new DeviceManagementDAOException(errorMsg, e);
                    }
                }
            } catch (SQLException e) {
                throw new DeviceManagementDAOException("Error occurred while retrieving data source connection", e);
            }
        }
        return currentConnection.get();
    }

    public static void closeConnection() throws DeviceManagementDAOException {
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

    public static void commitTransaction() throws DeviceManagementDAOException {

        if (isXADataSource && currentTransaction.get() != null) {
            try {
                currentTransaction.get().commit();
            } catch (Exception e) {
                String errorMsg = "Error occurred commit transaction";
                log.error(errorMsg, e);
                throw new DeviceManagementDAOException(errorMsg, e);
            }

            currentResource.remove();
            currentTransaction.remove();
            closeConnection();
        } else {
            Connection conn = currentConnection.get();
            if (conn != null) {
                try {
                    conn.commit();
                } catch (SQLException e) {
                    throw new DeviceManagementDAOException("Error occurred while committing the transaction", e);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Datasource connection associated with the current thread is null, hence commit " +
                            "has not been attempted");
                }
            }
        }

    }

    public static void rollbackTransaction() throws DeviceManagementDAOException {

        if (isXADataSource && currentTransaction.get() != null) {
            try {
                currentTransaction.get().rollback();
            } catch (Exception e) {
                String errorMsg = "Error occurred commit transaction";
                log.error(errorMsg, e);
                throw new DeviceManagementDAOException(errorMsg, e);
            }
            currentResource.remove();
            currentTransaction.remove();
        } else {
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
                throw new DeviceManagementDAOException("Error occurred while rollback the transaction", e);
            }
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
