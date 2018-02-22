/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.device.mgt.common.DeviceHierarchyMetadataHolder;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.TestUtils;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.dao.impl.DeviceHierarchyDAOImpl;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeviceHierarchyPersistenceTest extends BaseDeviceManagementTest {

    private static final Log log = LogFactory.getLog(DeviceHierarchyPersistenceTest.class);

    DeviceHierarchyDAOImpl deviceHierarchyDAOImpl;
    List<DeviceHierarchyMetadataHolder> expectedArray;
    DeviceDAO deviceDAO;
    DeviceTypeDAO deviceTypeDAO;
    Device device = TestDataHolder.generateDummyDeviceData(TestDataHolder.TEST_DEVICE_TYPE);
    int tenantId = TestDataHolder.SUPER_TENANT_ID;

    @BeforeClass
    @Override
    public void init() throws Exception {
        initDataSource();
        preliminaryDevicePrep();
        deviceHierarchyDAOImpl = new DeviceHierarchyDAOImpl();
        expectedArray = new ArrayList<>();
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

    @Test
    public void addDeviceToHierarchyTest() {
        boolean isAddSuccess;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            isAddSuccess = deviceHierarchyDAOImpl.addDeviceToHierarchy(device.getDeviceIdentifier(),
                    "g0", 0, tenantId);
            DeviceManagementDAOFactory.commitTransaction();
            Assert.assertEquals(isAddSuccess, true);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (DeviceHierarchyDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Unable to perform action with hierarchy";
            log.error(msg, e);
            Assert.fail(msg, e);
        }
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


    /**
     * This method is used to compare to ArrayList objects by checking individual elements
     *
     * @param resultArray   array that is retrieved from DAO method
     * @param expectedArray array that is expected from the DAO method retrieval
     */
    private void arraylistAssertion(List<DeviceHierarchyMetadataHolder> resultArray,
                                    List<DeviceHierarchyMetadataHolder> expectedArray) {
        for (int counter = 0; counter <= (expectedArray.size() - 1); counter++) {
            Assert.assertEquals(resultArray.get(counter).getDeviceId(), expectedArray.get(counter).getDeviceId());
            Assert.assertEquals(resultArray.get(counter).getDeviceParent(), expectedArray.get(counter).
                    getDeviceParent());
            Assert.assertEquals(resultArray.get(counter).getIsParent(), expectedArray.get(counter).getIsParent());
            Assert.assertEquals(resultArray.get(counter).getTenantId(), expectedArray.get(counter).getTenantId());
        }
    }

    private void preliminaryDevicePrep() {
        deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
        DeviceType deviceType = TestDataHolder.generateDeviceTypeData(TestDataHolder.TEST_DEVICE_TYPE);
        try {
            DeviceManagementDAOFactory.beginTransaction();
            deviceTypeDAO.addDeviceType(deviceType, TestDataHolder.SUPER_TENANT_ID, true);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding device type '" + deviceType.getName() + "'";
            log.error(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction to persist device type '" +
                    deviceType.getName() + "'";
            log.error(msg, e);
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
}
