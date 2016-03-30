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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo.Status;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.TestUtils;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.sql.*;

public class DevicePersistTests extends BaseDeviceManagementTest {

    DeviceDAO deviceDAO;
    DeviceTypeDAO deviceTypeDAO;

    private static final Log log = LogFactory.getLog(DevicePersistTests.class);

    @BeforeClass
    @Override
    public void init() throws Exception {
        initDataSource();
        deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
    }

    @Test
    public void testAddDeviceTypeTest() {
        DeviceType deviceType = TestDataHolder.generateDeviceTypeData(TestDataHolder.TEST_DEVICE_TYPE);
        try {
            DeviceManagementDAOFactory.beginTransaction();
            deviceTypeDAO.addDeviceType(deviceType, TestDataHolder.SUPER_TENANT_ID, true);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding device type '" + deviceType.getName() + "'";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction to persist device type '" +
                    deviceType.getName() + "'";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        Integer targetTypeId = null;
        try {
            targetTypeId = this.getDeviceTypeId(TestDataHolder.TEST_DEVICE_TYPE);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving target device type id";
            log.error(msg, e);
            Assert.fail(msg, e);
        }
        Assert.assertNotNull(targetTypeId, "Device Type Id is null");
        deviceType.setId(targetTypeId);
        TestDataHolder.initialTestDeviceType = deviceType;
    }

    @Test(dependsOnMethods = {"testAddDeviceTypeTest"})
    public void testAddDeviceTest() {
        int tenantId = TestDataHolder.SUPER_TENANT_ID;
        Device device = TestDataHolder.generateDummyDeviceData(TestDataHolder.TEST_DEVICE_TYPE);

        try {
            DeviceManagementDAOFactory.beginTransaction();
            int deviceId = deviceDAO.addDevice(TestDataHolder.initialTestDeviceType.getId(), device, tenantId);
            device.setId(deviceId);
            deviceDAO.addEnrollment(device, tenantId);
            DeviceManagementDAOFactory.commitTransaction();
            TestDataHolder.initialTestDevice = device;
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding '" + device.getType() + "' device with the identifier '" +
                    device.getDeviceIdentifier() + "'";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        int targetId = -1;
        try {
            targetId = this.getDeviceId(TestDataHolder.initialTestDevice.getDeviceIdentifier(),
                    TestDataHolder.SUPER_TENANT_ID);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving device id";
            log.error(msg, e);
            Assert.fail(msg, e);
        }
        Assert.assertNotNull(targetId, "Device Id persisted in device management metadata repository upon '" +
                device.getType() + "' carrying the identifier '" + device.getDeviceIdentifier() + "', is null");
    }

    private int getDeviceId(String deviceIdentification, int tenantId) throws DeviceManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        int id = -1;
        try {
            Assert.assertNotNull(getDataSource(), "Data Source is not initialized properly");
            conn = getDataSource().getConnection();
            String sql = "SELECT ID FROM DM_DEVICE WHERE DEVICE_IDENTIFICATION = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceIdentification);
            stmt.setInt(2, tenantId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt("ID");
            }
            return id;
        } catch (SQLException e) {
            String msg = "Error in fetching device by device identification id";
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            TestUtils.cleanupResources(conn, stmt, rs);
        }
    }

    private int getDeviceTypeId(String deviceTypeName) throws DeviceManagementDAOException {
        int id = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        String sql = "SELECT ID, NAME FROM DM_DEVICE_TYPE WHERE NAME = ?";

        try {
            Assert.assertNotNull(getDataSource(), "Data Source is not initialized properly");
            conn = getDataSource().getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceTypeName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                id = rs.getInt("ID");
            }
            return id;
        } catch (SQLException e) {
            String msg = "Error in fetching device type by name IOS";
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            TestUtils.cleanupResources(conn, stmt, null);
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void testSetEnrolmentStatus() {

        Device device = TestDataHolder.initialTestDevice;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            DeviceIdentifier deviceId = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
            deviceDAO.setEnrolmentStatus(deviceId, device.getEnrolmentInfo().getOwner(), Status.ACTIVE,
                    TestDataHolder.SUPER_TENANT_ID);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while setting enrolment status";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        Status target = null;
        try {
            target = this.getEnrolmentStatus(device.getDeviceIdentifier(), device.getType(),
                    TestDataHolder.SUPER_TENANT_ID);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the target enrolment status";
            log.error(msg, e);
            Assert.fail(msg, e);
        }
        Assert.assertNotNull(target, "Enrolment status retrieved for the device carrying its identifier as '" +
                device.getDeviceIdentifier() + "' is null");
        Assert.assertEquals(target, Status.ACTIVE, "Enrolment status retrieved is not as same as what's configured");
    }

    private Status getEnrolmentStatus(String identifier, String deviceType, int tenantId)
            throws DeviceManagementDAOException {

        Device device = TestDataHolder.generateDummyDeviceData("ios");
        try {
            DeviceManagementDAOFactory.openConnection();
            DeviceIdentifier deviceId = new DeviceIdentifier(identifier, deviceType);
            return deviceDAO.getEnrolmentStatus(deviceId, device.getEnrolmentInfo().getOwner(), tenantId);
        } catch (DeviceManagementDAOException | SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the current status of the " +
                    "enrolment", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }
}
