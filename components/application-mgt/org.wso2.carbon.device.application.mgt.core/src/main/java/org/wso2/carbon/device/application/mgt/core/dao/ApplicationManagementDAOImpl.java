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
package org.wso2.carbon.device.application.mgt.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.application.mgt.core.dto.Application;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ApplicationManagementDAOImpl implements ApplicationManagementDAO {

    private DatabaseType databaseType;
    private static DataSource dataSource;

    private static final Log log = LogFactory.getLog(ApplicationManagementDAOImpl.class);

    public ApplicationManagementDAOImpl(DataSourceConfig dataSourceConfig) {
        dataSource = ConnectionManagerUtil.resolveDataSource(dataSourceConfig);
        ConnectionManagerUtil.setDataSource(dataSource);
        String databaseEngine = "H2";
        try {
            databaseEngine = dataSource.getConnection().getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            log.error("Error occurred while retrieving config.datasource connection", e);
        }
        this.databaseType = DatabaseType.lookup(databaseEngine);
    }

    @Override
    public void createApplication(Application application) throws ApplicationManagementDAOException {

    }

    @Override
    public List<Application> getApplications() throws ApplicationManagementDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = null;
        List<Application> applications;

        try {
            conn = ConnectionManagerUtil.getCurrentConnection().get();
            switch (databaseType) {
                case H2:
                case MYSQL:
                    sql = "SELECT * FROM APPM_APPLICATION";
            }

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            applications = new ArrayList<>();
            while (rs.next()) {
                applications.add(ApplicationManagementDAOUtil.loadApplication(rs));
            }

        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while getting application List", e);
        } finally {
            ApplicationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return applications;
    }
}
