/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.authorization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.dao.*;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.user.api.UserStoreManager;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DeviceAccessAuthorizationServiceTest extends BaseDeviceManagementTest {
    private static final Log log = LogFactory.getLog(DeviceAccessAuthorizationServiceTest.class);
    DeviceDAO deviceDAO;
    DeviceTypeDAO deviceTypeDAO;
    DeviceAccessAuthorizationServiceImpl deviceAccessAuthorizationService = new DeviceAccessAuthorizationServiceImpl();

   // UserStoreManager userStoreManager = mock(UserStoreManager.class);

    @BeforeClass
    @Override
    public void init() throws Exception {
        this.initDataSource();
        deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
    }

    @Test(groups = "device.mgt.test", description = "Testing the first test case with testng.")
    public void setUp() throws Exception {
        log.info("test start");

        DeviceManagementDAOFactory.beginTransaction();
        DeviceType deviceType = new DeviceType();
        deviceType.setName("Sample");

        deviceTypeDAO.addDeviceType(deviceType, -1234, true);
        deviceType = deviceTypeDAO.getDeviceType("Sample", -1234);
        log.info(deviceType.getId());
        Assert.assertEquals(deviceType.getName(), "Sample");

        Device device = new Device();
        device.setId(1);
        device.setDeviceIdentifier("device1");
        device.setName("sample device");
        device.setType("Sample");

        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setOwner("Lahiru");
        device.setEnrolmentInfo(enrolmentInfo);
        deviceDAO.addDevice(1,device,-1234);

        DeviceManagementDAOFactory.closeConnection();
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId("1");
        deviceIdentifier.setType("Sample");

     //   Assert.assertTrue(deviceAccessAuthorizationService.isUserAuthorized(deviceIdentifier,"Lahiru"));

    }


}
