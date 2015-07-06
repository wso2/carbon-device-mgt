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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo.OwnerShip;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo.Status;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.TestUtils;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.sql.*;
import java.util.Date;

public class DeviceManagementDAOTests extends BaseDeviceManagementDAOTest {

    DeviceDAO deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
    DeviceTypeDAO deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();

    private static final Log log = LogFactory.getLog(DeviceManagementDAOTests.class);

    @BeforeClass
    @Override
    public void init() throws Exception {
        initDatSource();
    }

    @Test
    public void testAddDeviceTypeTest() {
        DeviceType deviceType = this.loadDummyDeviceType();
        try {
            DeviceManagementDAOFactory.openConnection();
            deviceTypeDAO.addDeviceType(deviceType);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while adding device type '" + deviceType.getName() + "'";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }

        int targetTypeId = -1;
        try {
            targetTypeId = this.getDeviceTypeId();
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving target device type id";
            log.error(msg, e);
            Assert.fail(msg, e);
        }

        Assert.assertNotNull(targetTypeId, "Device Type Id is null");
        deviceType.setId(targetTypeId);
    }

    @Test(dependsOnMethods = {"testAddDeviceTypeTest"})
    public void testAddDeviceTest() {
        DeviceType deviceType = this.loadDummyDeviceType();
        deviceType.setId(1);

        int tenantId = -1234;
        Device device = this.loadDummyDevice();
        try {
            DeviceManagementDAOFactory.openConnection();
            int deviceId = deviceDAO.addDevice(deviceType.getId(), device, tenantId);
            device.setId(deviceId);
            deviceDAO.addEnrollment(device, tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while adding '" + device.getType() + "' device with the identifier '" +
                    device.getDeviceIdentifier() + "'";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }

        int targetId = -1;
        try {
            targetId = this.getDeviceId();
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving device id";
            log.error(msg, e);
            Assert.fail(msg, e);
        }
        Assert.assertNotNull(targetId, "Device Id persisted in device management metadata repository upon '" +
                device.getType() + "' carrying the identifier '" + device.getDeviceIdentifier() + "', is null");
    }

    private void addDeviceEnrolment() {
        Device device = this.loadDummyDevice();
        try {
            DeviceManagementDAOFactory.openConnection();
            deviceDAO.addEnrollment(device, -1234);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while adding enrolment configuration upon '" + device.getType() +
                    "' device with the identifier '" + device.getDeviceIdentifier() + "'";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }
    }

    private int getDeviceId() throws DeviceManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        int id = -1;
        try {
            Assert.assertNotNull(getDataSource(), "Data Source is not initialized properly");
            conn = getDataSource().getConnection();
            String sql = "SELECT ID FROM DM_DEVICE WHERE DEVICE_IDENTIFICATION = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "111");
            stmt.setInt(2, -1234);

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

    private int getDeviceTypeId() throws DeviceManagementDAOException {
        int id = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        String sql = "SELECT ID, NAME FROM DM_DEVICE_TYPE WHERE NAME = ?";

        DeviceType deviceType = this.loadDummyDeviceType();
        try {
            Assert.assertNotNull(getDataSource(), "Data Source is not initialized properly");
            conn = getDataSource().getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceType.getName());
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
        Device device = this.loadDummyDevice();
        try {
            DeviceManagementDAOFactory.openConnection();
            DeviceIdentifier deviceId = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
            deviceDAO.setEnrolmentStatus(deviceId, device.getEnrolmentInfo().getOwner(), Status.ACTIVE, -1234);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while setting enrolment status";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }
        Status target = null;
        try {
            target = this.getEnrolmentStatus();
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the target enrolment status";
            log.error(msg, e);
            Assert.fail(msg, e);
        }

        Assert.assertNotNull(target, "Enrolment status retrieved for the device carrying its identifier as '" +
                device.getDeviceIdentifier() + "' is null");
        Assert.assertEquals(target, Status.ACTIVE, "Enrolment status retrieved is not as same as what's configured");
    }

    private Status getEnrolmentStatus() throws DeviceManagementDAOException {
        Device device = this.loadDummyDevice();
        try {
            DeviceManagementDAOFactory.openConnection();
            DeviceIdentifier deviceId = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
            return deviceDAO.getEnrolmentStatus(deviceId, device.getEnrolmentInfo().getOwner(), -1234);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the current status of the " +
                    "enrolment", e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }
    }

    private Device loadDummyDevice() {
        Device device = new Device();
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setDateOfEnrolment(new Date().getTime());
        enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
        enrolmentInfo.setOwner("admin");
        enrolmentInfo.setOwnership(OwnerShip.BYOD);
        enrolmentInfo.setStatus(Status.CREATED);
        device.setEnrolmentInfo(enrolmentInfo);
        device.setDescription("Test Description");
        device.setDeviceIdentifier("1234");
        device.setType(this.loadDummyDeviceType().getName());
        return device;
    }

    private DeviceType loadDummyDeviceType() {
        return new DeviceType("iOS");
    }

}
