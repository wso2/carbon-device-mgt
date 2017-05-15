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
import org.wso2.carbon.device.application.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.device.application.mgt.core.dao.impl.GenericAppManagementDAO;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import  org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil.DatabaseType;

import javax.sql.DataSource;
import java.sql.SQLException;

public class ApplicationManagementDAOFactory {

    public static final String H2 = "H2";
    private DatabaseType databaseType;
    private static DataSource dataSource;

    private static final Log log = LogFactory.getLog(ApplicationManagementDAOFactory.class);

    public ApplicationManagementDAOFactory(DataSourceConfig dataSourceConfig) {
        dataSource = ConnectionManagerUtil.resolveDataSource(dataSourceConfig);
        ConnectionManagerUtil.setDataSource(dataSource);
        String databaseEngine = H2;
        try {
            databaseEngine = dataSource.getConnection().getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            log.error("Error occurred while retrieving config.datasource connection", e);
        }
        this.databaseType = DatabaseType.lookup(databaseEngine);
    }

    public ApplicationManagementDAO getApplicationManagementDAO(){
        switch (databaseType) {
            default:
                return new GenericAppManagementDAO();
        }
    }
}
