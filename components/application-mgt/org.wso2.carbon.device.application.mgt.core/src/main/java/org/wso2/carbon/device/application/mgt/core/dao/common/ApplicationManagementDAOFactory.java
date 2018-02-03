/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.core.dao.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.exception.UnsupportedDatabaseEngineException;
import org.wso2.carbon.device.application.mgt.core.config.ConfigurationManager;
import org.wso2.carbon.device.application.mgt.core.dao.*;
import org.wso2.carbon.device.application.mgt.core.dao.impl.Comment.CommentDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.application.GenericApplicationDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.application.release.GenericApplicationReleaseDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.application.release.OracleApplicationDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.lifecyclestate.GenericLifecycleStateImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.subscription.GenericSubscriptionDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.visibility.GenericVisibilityDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.util.ApplicationMgtDatabaseCreator;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.application.mgt.core.util.Constants;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dao.impl.DeviceTypeDAOImpl;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.sql.SQLException;

/**
 * This class intends to act as the primary entity that hides all DAO instantiation related complexities and logic so
 * that the business objection handling layer doesn't need to be aware of the same providing seamless plug-ability of
 * different data sources, connection acquisition mechanisms as well as different forms of DAO implementations to the
 * high-level implementations that require Application management related metadata persistence.
 */
public class ApplicationManagementDAOFactory {

    private static String databaseEngine;
    private static final Log log = LogFactory.getLog(ApplicationManagementDAOFactory.class);

    public static void init(String datasourceName) {
        ConnectionManagerUtil.resolveDataSource(datasourceName);
        databaseEngine = ConnectionManagerUtil.getDatabaseType();
    }

    public static ApplicationDAO getApplicationDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case Constants.DataBaseTypes.DB_TYPE_H2:
                case Constants.DataBaseTypes.DB_TYPE_MYSQL:
                case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                    return new GenericApplicationDAOImpl();
                case Constants.DataBaseTypes.DB_TYPE_ORACLE:
                    return new OracleApplicationDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    public static LifecycleStateDAO getLifecycleStateDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case Constants.DataBaseTypes.DB_TYPE_H2:
                case Constants.DataBaseTypes.DB_TYPE_MYSQL:
                case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                case Constants.DataBaseTypes.DB_TYPE_ORACLE:
                    return new GenericLifecycleStateImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    /**
     * To get the instance of ApplicationReleaseDAOImplementation of the particular database engine.
     *
     * @return specific ApplicationReleaseDAOImplementation
     */
    public static ApplicationReleaseDAO getApplicationReleaseDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case Constants.DataBaseTypes.DB_TYPE_H2:
                case Constants.DataBaseTypes.DB_TYPE_MYSQL:
                case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                case Constants.DataBaseTypes.DB_TYPE_ORACLE:
                    return new GenericApplicationReleaseDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    /**
     * To get the instance of VisibilityDAOImplementation of the particular database engine.
     * @return specific VisibilityDAOImplementation
     */
    public static VisibilityDAO getVisibilityDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case Constants.DataBaseTypes.DB_TYPE_H2:
                case Constants.DataBaseTypes.DB_TYPE_MYSQL:
                    return new GenericVisibilityDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    /**
     * To get the instance of SubscriptionDAOImplementation of the particular database engine.
     * @return GenericSubscriptionDAOImpl
     */
    public static SubscriptionDAO getSubscriptionDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case Constants.DataBaseTypes.DB_TYPE_H2:
                case Constants.DataBaseTypes.DB_TYPE_MYSQL:
                case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                    return new GenericSubscriptionDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    /**
     * To get the instance of DeviceTypeDAOImpl of the particular database engine.
     * @return DeviceTypeDAOImpl
     */
    public static DeviceTypeDAO getDeviceTypeDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
            case Constants.DataBaseTypes.DB_TYPE_H2:
            case Constants.DataBaseTypes.DB_TYPE_MYSQL:
            case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                return new DeviceTypeDAOImpl();
            default:
                throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    public static CommentDAO getCommentDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
            case Constants.DataBaseTypes.DB_TYPE_H2:
            case Constants.DataBaseTypes.DB_TYPE_MYSQL:
            case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                return new CommentDAOImpl();
            default:
                throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    /**
     * This method initializes the databases by creating the database.
     *
     * @throws ApplicationManagementDAOException Exceptions thrown during the creation of the tables
     */
    public static void initDatabases() throws ApplicationManagementDAOException {
        String dataSourceName = ConfigurationManager.getInstance().getConfiguration().getDatasourceName();
        String validationQuery = "SELECT * from APPM_PLATFORM";
        try {
            if (System.getProperty("setup") == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Application Management Database schema initialization check was skipped since "
                            + "\'setup\' variable was not given during startup");
                }
            } else {
                DatabaseCreator databaseCreator = new ApplicationMgtDatabaseCreator(dataSourceName);
                if (!databaseCreator.isDatabaseStructureCreated(validationQuery)) {
                    databaseCreator.createRegistryDatabase();
                    log.info("Application Management tables are created in the database");
                } else {
                    log.info("Application Management Database structure already exists. Not creating the database.");
                }
            }
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error while creating application-mgt database during the " + "startup ", e);
        } catch (Exception e) {
            throw new ApplicationManagementDAOException(
                    "Error while creating application-mgt database in the " + "startup ", e);
        }
    }
}
