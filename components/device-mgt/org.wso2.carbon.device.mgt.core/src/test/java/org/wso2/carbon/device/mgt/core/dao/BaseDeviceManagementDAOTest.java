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
package org.wso2.carbon.device.mgt.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.TestUtils;
import org.wso2.carbon.device.mgt.core.common.DBTypes;
import org.wso2.carbon.device.mgt.core.common.TestDBConfiguration;
import org.wso2.carbon.device.mgt.core.common.TestDBConfigurations;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class BaseDeviceManagementDAOTest {

    private DataSource dataSource;
    private static final Log log = LogFactory.getLog(BaseDeviceManagementDAOTest.class);

    @BeforeSuite
    public void setupDataSource() throws Exception {
        this.dataSource = this.getDataSource(this.readDataSourceConfig());
        this.initSQLScript();
        DeviceManagementDAOFactory.init(dataSource);
    }

    private DataSource getDataSource(TestDBConfiguration config) {
        PoolProperties properties = new PoolProperties();
        properties.setUrl(config.getConnectionUrl());
        properties.setDriverClassName(config.getDriverClass());
        properties.setUsername(config.getUserName());
        properties.setPassword(config.getPwd());
        return new org.apache.tomcat.jdbc.pool.DataSource(properties);
    }

    private TestDBConfiguration readDataSourceConfig() throws DeviceManagementDAOException,
            DeviceManagementException {
        File config = new File("src/test/resources/data-source-config.xml");
        Document doc;
        TestDBConfigurations dbConfigs;

        doc = DeviceManagerUtil.convertToDocument(config);
        JAXBContext testDBContext;

        try {
            testDBContext = JAXBContext.newInstance(TestDBConfigurations.class);
            Unmarshaller unmarshaller = testDBContext.createUnmarshaller();
            dbConfigs = (TestDBConfigurations) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new DeviceManagementDAOException("Error parsing test db configurations", e);
        }

    }

    private void initSQLScript() throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getDataSource().getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("RUNSCRIPT FROM './src/test/resources/sql/h2.sql'");
        } finally {
            TestUtils.cleanupResources(conn, stmt, null);
        }
    }

    @AfterSuite
    public void deleteData() {
        Connection conn = null;
        try {
            conn = getDataSource().getConnection();
            conn.setAutoCommit(false);

            this.cleanupEnrolmentData(conn);
            this.cleanupDeviceData(conn);
            this.cleanupDeviceTypeData(conn);

            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                log.error("Error occurred while roll-backing the transaction", e);
            }
            String msg = "Error occurred while cleaning up temporary data generated during test execution";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.warn("Error occurred while closing the connection", e);
                }
            }
        }
    }

    private void cleanupEnrolmentData(Connection conn) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("DELETE FROM DM_ENROLMENT");
            stmt.execute();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    private void cleanupDeviceData(Connection conn) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("DELETE FROM DM_DEVICE");
            stmt.execute();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    private void cleanupDeviceTypeData(Connection conn) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("DELETE FROM DM_DEVICE_TYPE");
            stmt.execute();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

}
