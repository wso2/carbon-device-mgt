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
import org.wso2.carbon.device.application.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.impl.application.GenericApplicationDAOImpl;
import org.wso2.carbon.device.application.mgt.core.util.ApplicationManagerConstants;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

import javax.sql.DataSource;


/**
 * This class intends to act as the primary entity that hides all DAO instantiation related complexities and logic so
 * that the business objection handling layer doesn't need to be aware of the same providing seamless plug-ability of
 * different data sources, connection acquisition mechanisms as well as different forms of DAO implementations to the
 * high-level implementations that require Application management related metadata persistence.
 */
public class ApplicationManagementDAOFactory {

    private static String databaseEngine;
    private static final Log log = LogFactory.getLog(ApplicationManagementDAOFactory.class);

    public static void init(DataSourceConfig config) {
        ConnectionManagerUtil.resolveDataSource(config);
        databaseEngine = ConnectionManagerUtil.getDatabaseType();
    }

    public static void init(DataSource dtSource) {
        ConnectionManagerUtil.setDataSource(dtSource);
        databaseEngine = ConnectionManagerUtil.getDatabaseType();
    }

    public static ApplicationDAO getApplicationDAO(){
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case ApplicationManagerConstants.DataBaseTypes.DB_TYPE_H2:
                case ApplicationManagerConstants.DataBaseTypes.DB_TYPE_MYSQL:
                    return new GenericApplicationDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }
}