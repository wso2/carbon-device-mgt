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

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.DeviceOrganizationMetadataHolder;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.DeviceOrganizationTestMetadataHolder;
import org.wso2.carbon.device.mgt.core.dao.impl.DeviceOrganizationDAOImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class DeviceOrganizationPersistTest extends BaseDeviceManagementTest {

    private static final Log log = LogFactory.getLog(DeviceOrganizationPersistTest.class);

    DeviceOrganizationDAOImpl deviceOrganizationDAOimpl;
    List<DeviceOrganizationMetadataHolder> expectedArray;

    @BeforeClass
    @Override
    public void init() throws Exception {
        deviceOrganizationDAOimpl = new DeviceOrganizationDAOImpl();
        expectedArray = new ArrayList<>();
    }

    @Test
    public void getDevicesInOrganizationTest() {
        dummyDeviceOrgData();
        boolean isAddSuccess;
        List<DeviceOrganizationMetadataHolder> resultArray;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            for (DeviceOrganizationMetadataHolder tempHolder : expectedArray) {
                String tempDeviceId = tempHolder.getDeviceId();
                String tempDeviceName = tempHolder.getDeviceName();
                String tempDeviceParent = tempHolder.getParent();
                int tempPingMins = tempHolder.getPingMins();
                int tempState = tempHolder.getState();
                int tempIsGateway = tempHolder.getIsGateway();
                isAddSuccess = deviceOrganizationDAOimpl.addDeviceOrganization(tempDeviceId, tempDeviceName,
                        tempDeviceParent, tempPingMins, tempState, tempIsGateway);
                if (!isAddSuccess) {
                    DeviceManagementDAOFactory.rollbackTransaction();
                    String msg = "Error occurred while adding device " + tempDeviceId + "to array";
                    log.error(msg);
                    Assert.fail(msg);
                }
            }
            resultArray = deviceOrganizationDAOimpl.getDevicesInOrganization();
            DeviceManagementDAOFactory.commitTransaction();
            arraylistAssertion(resultArray,expectedArray);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Unable to update device name";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test (dependsOnMethods = {"getDevicesInOrganizationTest"})
    public void getDeviceOrganizationParentTest() {
        String tempParent;
        String tempId = DeviceOrganizationTestMetadataHolder.getDeviceId();
        try {
            DeviceManagementDAOFactory.beginTransaction();
            tempParent = deviceOrganizationDAOimpl.getDeviceOrganizationParent(tempId);
            DeviceManagementDAOFactory.commitTransaction();
            Assert.assertEquals(tempParent, DeviceOrganizationTestMetadataHolder.getDeviceParent());
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Unable to retrieve device parent";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test (dependsOnMethods = {"getDevicesInOrganizationTest"})
    public void getDeviceOrganizationStateByIdTest() {
        int tempState;
        String tempId = DeviceOrganizationTestMetadataHolder.getDeviceId();
        try {
            DeviceManagementDAOFactory.beginTransaction();
            tempState = deviceOrganizationDAOimpl.getDeviceOrganizationStateById(tempId);
            DeviceManagementDAOFactory.commitTransaction();
            Assert.assertEquals(tempState, expectedArray.get(1).getState());
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Unable to retrieve device state by ID";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test (dependsOnMethods = {"getDevicesInOrganizationTest"})
    public void getDeviceOrganizationIsGatewayTest() {
        int actualIsGateway;
        String tempId = DeviceOrganizationTestMetadataHolder.getDeviceId();
        try {
            DeviceManagementDAOFactory.beginTransaction();
            actualIsGateway = deviceOrganizationDAOimpl.getDeviceOrganizationIsGateway(tempId);
            DeviceManagementDAOFactory.commitTransaction();
            Assert.assertEquals(actualIsGateway, expectedArray.get(1).getIsGateway());
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Unable to check if device is a Gateway";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test (dependsOnMethods = {"getDevicesInOrganizationTest"})
    public void getChildrenByParentIdTest() {
        boolean isAddSuccess;
        boolean isAddSuccessOverload;
        String deviceId;
        String deviceName;
        String deviceParent = "gatewayN";
        String deviceParentOverload = "gatewayNOverload";
        int pingMins;
        int state;
        int isGateway;
        List<DeviceOrganizationMetadataHolder> resultArray;
        List<DeviceOrganizationMetadataHolder> resultArrayOverload;
        List<DeviceOrganizationMetadataHolder> expectedArray = new ArrayList<>();
        try {
            DeviceManagementDAOFactory.beginTransaction();
            isAddSuccess = deviceOrganizationDAOimpl.addDeviceOrganization(deviceParent,"gatewayNew",
                    "./", 10, 0, 1);
            if (isAddSuccess) {
                for (int counter = 0; counter <= 2; counter++) {
                    Random rand = new Random();
                    deviceId = "dev" + counter;
                    deviceName = "device" + counter;
                    pingMins = rand.nextInt(60) + 10;
                    state = rand.nextInt(2) + 0;
                    isGateway = rand.nextInt(1) + 0;
                    expectedArray.add(new DeviceOrganizationMetadataHolder(deviceId, deviceName, deviceParent, pingMins,
                            state, isGateway));
                    isAddSuccess = deviceOrganizationDAOimpl.addDeviceOrganization(deviceId, deviceName, deviceParent,
                            pingMins, state, isGateway);
                    if (!isAddSuccess) {
                        DeviceManagementDAOFactory.rollbackTransaction();
                        String msg = "Error occurred while adding device " + deviceId + "to database";
                        log.error(msg);
                        Assert.fail(msg);
                    }
                }
                resultArray = deviceOrganizationDAOimpl.getChildrenByParentId(deviceParent);
                DeviceManagementDAOFactory.commitTransaction();
                arraylistAssertion(resultArray,expectedArray);
            } else {
                String msg = "Error occurred while adding test parent gateway to database";
                log.error(msg);
                Assert.fail(msg);
            }
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Unable to add test device";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test (dependsOnMethods = {"getDevicesInOrganizationTest"})
    public void updateDeviceOrganizationParentTest() {
        String newParent;
        String updateParent = "gatewayNew";
        String tempId = DeviceOrganizationTestMetadataHolder.getDeviceId();
        try {
            DeviceManagementDAOFactory.beginTransaction();
            newParent = deviceOrganizationDAOimpl.updateDeviceOrganizationParent(tempId,updateParent);
            DeviceManagementDAOFactory.commitTransaction();
            Assert.assertEquals(newParent,updateParent);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Unable to update device parent";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test (dependsOnMethods = {"getDevicesInOrganizationTest"})
    public void updateDeviceOrganizationNameTest() {
        String newName;
        String updateName = "device1UPDATE";
        String tempId = DeviceOrganizationTestMetadataHolder.getDeviceId();
        try {
            DeviceManagementDAOFactory.beginTransaction();
            newName = deviceOrganizationDAOimpl.updateDeviceOrganizationName(tempId, updateName);
            DeviceManagementDAOFactory.commitTransaction();
            Assert.assertEquals(newName, updateName);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Unable to update device name";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    /**
     * This method is used to populate a test ArrayList with randomly generated data for testing
     */
    private void dummyDeviceOrgData() {
        String deviceId;
        String deviceName;
        String deviceParent;
        int pingMins;
        int state;
        int isGateway;
        for (int counter = 0; counter<=9; counter++) {
            Random rand = new Random();
            deviceId = "d" + counter;
            deviceName = "device" + counter;
            deviceParent = "gateway" + counter;
            pingMins = rand.nextInt(60) + 10;
            state = rand.nextInt(2) + 0;
            isGateway = rand.nextInt(1) + 0;
            DeviceOrganizationMetadataHolder tempDevice = new DeviceOrganizationMetadataHolder(deviceId, deviceName,
                    deviceParent, pingMins, state, isGateway);
            expectedArray.add(tempDevice);
        }
    }


    /**
     * This method is used to compare to ArrayList objects
     */
    private void arraylistAssertion(List<DeviceOrganizationMetadataHolder> resultArray,
                                    List<DeviceOrganizationMetadataHolder> expectedArray) {
        for (int counter = 0; counter<=(expectedArray.size()-1); counter++) {
            Assert.assertEquals(resultArray.get(counter).getDeviceId(),expectedArray.get(counter).getDeviceId());
            Assert.assertEquals(resultArray.get(counter).getDeviceName(),expectedArray.get(counter).getDeviceName());
            Assert.assertEquals(resultArray.get(counter).getParent(),expectedArray.get(counter).getParent());
            Assert.assertEquals(resultArray.get(counter).getPingMins(),expectedArray.get(counter).getPingMins());
            Assert.assertEquals(resultArray.get(counter).getState(),expectedArray.get(counter).getState());
            Assert.assertEquals(resultArray.get(counter).getIsGateway(),expectedArray.get(counter).getIsGateway());
        }
    }

    private void stringArraylistAssertion(List<String> resultArray,
                                          List<String> expectedArray) {
        for (int counter = 0; counter<=(expectedArray.size()-1); counter++) {
            Assert.assertEquals(resultArray.get(counter), expectedArray.get(counter));
        }
    }
}
