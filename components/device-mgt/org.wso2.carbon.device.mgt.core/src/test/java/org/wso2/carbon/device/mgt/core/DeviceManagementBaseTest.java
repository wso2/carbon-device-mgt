/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core;

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DeviceManagementBaseTest {

    private DataSource dataSource;

    public void init() {
        this.initDataSource();
        try {
            this.initDeviceManagementDatabaseSchema();
        } catch (SQLException e) {
            Assert.fail("Error occurred while initializing database schema", e);
        }
    }

    private void initDeviceManagementDatabaseSchema() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        try {
            if (dataSource == null) {
                Assert.fail("Device management datasource is not initialized peroperly");
            }
            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("RUNSCRIPT FROM './src/test/resources/sql/h2.sql'");
        } finally {
            TestUtils.cleanupResources(conn, stmt, null);
        }
    }

    private void initDataSource() {
        PoolProperties properties = new PoolProperties();
        properties.setUrl("jdbc:h2:mem:MDM_DB;DB_CLOSE_DELAY=-1");
        properties.setDriverClassName("org.h2.Driver");
        properties.setUsername("wso2carbon");
        properties.setPassword("wso2carbon");
        this.dataSource = new org.apache.tomcat.jdbc.pool.DataSource(properties);
    }

    protected DataSource getDataSource() {
        return dataSource;
    }

}
