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
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Device.Status;
import org.wso2.carbon.device.mgt.common.Device.OwnerShip;
import org.wso2.carbon.device.mgt.core.TestUtils;
import org.wso2.carbon.device.mgt.core.common.DBTypes;
import org.wso2.carbon.device.mgt.core.common.TestDBConfiguration;
import org.wso2.carbon.device.mgt.core.common.TestDBConfigurations;
import org.wso2.carbon.device.mgt.core.dto.Device;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.sql.*;
import java.util.Date;

public class DeviceManagementDAOTests {

    private DataSource dataSource;
    private static final Log log = LogFactory.getLog(DeviceManagementDAOTests.class);

    @AfterClass
    public void deleteData() throws Exception{
        Connection connection = dataSource.getConnection();
        connection.createStatement().execute("DELETE FROM DM_DEVICE");
        connection.createStatement().execute("DELETE FROM DM_DEVICE_TYPE");
    }

    @BeforeClass
    @Parameters("dbType")
    public void setUpDB(String dbTypeStr) throws Exception {
        DBTypes dbType = DBTypes.valueOf(dbTypeStr);
        TestDBConfiguration dbConfig = getTestDBConfiguration(dbType);

        switch (dbType) {
            case H2:
                PoolProperties properties = new PoolProperties();
                properties.setUrl(dbConfig.getConnectionUrl());
                properties.setDriverClassName(dbConfig.getDriverClass());
                properties.setUsername(dbConfig.getUserName());
                properties.setPassword(dbConfig.getPwd());
                dataSource = new org.apache.tomcat.jdbc.pool.DataSource(properties);
                this.initSQLScript();
                DeviceManagementDAOFactory.init(dataSource);
            default:
        }
    }

    private TestDBConfiguration getTestDBConfiguration(DBTypes dbType) throws DeviceManagementDAOException,
            DeviceManagementException {
        File deviceMgtConfig = new File("src/test/resources/testdbconfig.xml");
        Document doc;
        TestDBConfigurations dbConfigs;

        doc = DeviceManagerUtil.convertToDocument(deviceMgtConfig);
        JAXBContext testDBContext;

        try {
            testDBContext = JAXBContext.newInstance(TestDBConfigurations.class);
            Unmarshaller unmarshaller = testDBContext.createUnmarshaller();
            dbConfigs = (TestDBConfigurations) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new DeviceManagementDAOException("Error parsing test db configurations", e);
        }
        for (TestDBConfiguration config : dbConfigs.getDbTypesList()) {
            if (config.getDbType().equals(dbType.toString())) {
                return config;
            }
        }
        return null;
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

    @Test
    public void addDeviceTypeTest() throws DeviceManagementDAOException, DeviceManagementException {
        DeviceTypeDAO deviceTypeMgtDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();

        DeviceType deviceType = new DeviceType();
        deviceType.setName("IOS");
        deviceTypeMgtDAO.addDeviceType(deviceType);

        int id = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        String sql = "SELECT dt.ID, dt.NAME FROM DM_DEVICE_TYPE dt where dt.NAME = 'IOS'";
        try {
            conn = this.getDataSource().getConnection();
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                id = rs.getInt("ID");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("error in fetch device type by name IOS", e);
        } finally {
            TestUtils.cleanupResources(conn, stmt, null);
        }
        Assert.assertNotNull(id, "Device Type Id is null");
        deviceType.setId(id);
    }

    @Test(dependsOnMethods = {"addDeviceTypeTest"})
    public void addDeviceTest() throws DeviceManagementDAOException, DeviceManagementException {
        DeviceDAO deviceMgtDAO = DeviceManagementDAOFactory.getDeviceDAO();

        Device device = new Device();
        device.setDateOfEnrollment(new Date().getTime());
        device.setDateOfLastUpdate(new Date().getTime());
        device.setDescription("test description");
        device.setStatus(Status.ACTIVE);
        device.setDeviceIdentificationId("111");

        DeviceType deviceType = new DeviceType();
        deviceType.setId(Integer.parseInt("1"));

        device.setDeviceTypeId(deviceType.getId());
        device.setOwnerShip(OwnerShip.BYOD.toString());
        device.setOwnerId("111");
        device.setTenantId(-1234);
        deviceMgtDAO.addDevice(device);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        Long id = null;
        String status = null;
        try {
            conn = this.getDataSource().getConnection();
            String sql = "SELECT ID, STATUS from DM_DEVICE DEVICE where DEVICE.DEVICE_IDENTIFICATION = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "111");

            rs = stmt.executeQuery();
            if (rs.next()) {
                id = rs.getLong("ID");
                status = rs.getString("STATUS");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error in fetch device by device identification id", e);
        } finally {
            TestUtils.cleanupResources(conn, stmt, rs);
        }
        Assert.assertNotNull(id, "Device Id is null");
        Assert.assertNotNull(status, "Device status is null");
        Assert.assertEquals(status, "ACTIVE", "Enroll device status should active");
    }

    private DataSource getDataSource() {
        return dataSource;
    }

}
