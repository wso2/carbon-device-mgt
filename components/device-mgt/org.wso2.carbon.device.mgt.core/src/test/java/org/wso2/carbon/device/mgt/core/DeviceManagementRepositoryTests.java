/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;

public class DeviceManagementRepositoryTests {

    private DeviceManagementPluginRepository repository;

    @BeforeClass
    public void initRepository() {
        this.repository = new DeviceManagementPluginRepository();
    }

    @Test
    public void testAddDeviceManagementService() {
        DeviceManagementService sourceProvider = new TestDeviceManagementService(TestDataHolder.TEST_DEVICE_TYPE);
        try {
            this.getRepository().addDeviceManagementProvider(sourceProvider);
        } catch (DeviceManagementException e) {
            Assert.fail("Unexpected error occurred while invoking addDeviceManagementProvider functionality", e);
        }
        DeviceManagementService targetProvider =
                this.getRepository().getDeviceManagementService(TestDataHolder.TEST_DEVICE_TYPE);
        Assert.assertEquals(targetProvider.getType(), sourceProvider.getType());
    }

    @Test(dependsOnMethods = "testAddDeviceManagementService")
    public void testRemoveDeviceManagementService() {
        DeviceManagementService sourceProvider = new TestDeviceManagementService(TestDataHolder.TEST_DEVICE_TYPE);
        try {
            this.getRepository().removeDeviceManagementProvider(sourceProvider);
        } catch (DeviceManagementException e) {
            Assert.fail("Unexpected error occurred while invoking removeDeviceManagementProvider functionality", e);
        }
        DeviceManagementService targetProvider =
                this.getRepository().getDeviceManagementService(TestDataHolder.TEST_DEVICE_TYPE);
        Assert.assertNull(targetProvider);
    }

    private DeviceManagementPluginRepository getRepository() {
        return repository;
    }

}
