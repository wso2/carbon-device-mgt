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

package org.wso2.carbon.device.mgt.core.cache;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.cache.impl.DeviceCacheManagerImpl;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.config.cache.DeviceCacheConfiguration;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/*
    Unit tests for deviceCacheManagerImpl
 */
public class DeviceCacheManagerImplTest extends BaseDeviceManagementTest {

    private static final int NO_OF_DEVICES = 5;
    private static final String UPDATE_NAME = "updatedName";
    private static final String DEVICE_TYPE = "TEST_TYPE";
    private static final String DEVICE_ID_PREFIX = "TEST-DEVICE-ID-";
    private DeviceCacheManagerImpl deviceCacheManager;
    private List<DeviceIdentifier> deviceIds = new ArrayList<>();
    private List<Device> devices = new ArrayList<>();
    private List<DeviceCacheKey> deviceCacheKeyList = new ArrayList<>();

    @BeforeClass
    public void init() throws DeviceManagementException, IOException {
        DeviceConfigurationManager.getInstance().initConfig();
        for (int i = 0; i < NO_OF_DEVICES; i++) {
            deviceIds.add(new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE));
        }
        devices = TestDataHolder.generateDummyDeviceData(this.deviceIds);
        deviceCacheManager = Mockito.mock(DeviceCacheManagerImpl.class, Mockito.CALLS_REAL_METHODS);
        DeviceCacheConfiguration configuration = new DeviceCacheConfiguration();
        configuration.setEnabled(true);
        DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        config.setDeviceCacheConfiguration(configuration);
        initializeCarbonContext();
    }

    private void initializeCarbonContext() throws IOException {
        if (System.getProperty("carbon.home") == null) {
            File file = new File("src/test/resources");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
        }
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID);
    }


    @Test(description = "Adding all test devices to the cache")
    public void testAddDeviceToCache() throws DeviceManagementException {
        for (int i = 0; i < NO_OF_DEVICES; i++) {
            deviceCacheManager.addDeviceToCache(deviceIds.get(i), devices.get(i), MultitenantConstants.SUPER_TENANT_ID);
        }
    }

    @Test(description = "Adding existing device again", dependsOnMethods = {"testAddDeviceToCache"})
    public void testAddExistingDeviceToCache() throws DeviceManagementException {
        deviceCacheManager.addDeviceToCache(deviceIds.get(0), devices.get(0), MultitenantConstants.SUPER_TENANT_ID);
    }

    @Test(description = "test updating and getting a device in Cache", dependsOnMethods = {"testAddExistingDeviceToCache"})
    public void testUpdateDeviceInCache() {
        devices.get(0).setName(UPDATE_NAME);
        deviceCacheManager.updateDeviceInCache(deviceIds.get(0), devices.get(0), MultitenantConstants.SUPER_TENANT_ID);

        Device tempDevice = deviceCacheManager.getDeviceFromCache(deviceIds.get(0), MultitenantConstants
                .SUPER_TENANT_ID);
        Assert.assertEquals(tempDevice.getName(), UPDATE_NAME);
    }

    @Test(description = "test getting a device from cache", dependsOnMethods = {"testAddDeviceToCache"})
    public void testGetDeviceFromCache() throws DeviceManagementException {
        Device tempDevice = deviceCacheManager.getDeviceFromCache(deviceIds.get(1), MultitenantConstants
                .SUPER_TENANT_ID);
        Assert.assertEquals(tempDevice, devices.get(1));
    }

    @Test(description = "test removing a device from cache", dependsOnMethods = {"testUpdateDeviceInCache"})
    public void testRemoveDeviceFromCache() throws DeviceManagementException {
        deviceCacheManager.removeDeviceFromCache(deviceIds.get(0), MultitenantConstants.SUPER_TENANT_ID);
    }

    @Test(description = "test removing list of devices from cache", dependsOnMethods = {"testRemoveDeviceFromCache"})
    public void testRemoveDevicesFromCache() {
        //starting from index 1 since 0 is already deleted
        for (int i = 1; i < NO_OF_DEVICES; i++) {
            DeviceCacheKey deviceCacheKey = new DeviceCacheKey();
            deviceCacheKey.setDeviceId(devices.get(i).getDeviceIdentifier());
            deviceCacheKey.setDeviceType(devices.get(i).getType());
            deviceCacheKey.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            deviceCacheKeyList.add(deviceCacheKey);
        }
        deviceCacheManager.removeDevicesFromCache(deviceCacheKeyList);
    }
}
