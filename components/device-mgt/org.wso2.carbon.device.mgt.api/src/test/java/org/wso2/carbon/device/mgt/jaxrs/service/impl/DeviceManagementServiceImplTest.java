/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceManagementService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.UUID;

import javax.ws.rs.core.Response;

import static org.mockito.MockitoAnnotations.initMocks;

@PowerMockIgnore("javax.ws.rs.*")
@SuppressStaticInitializationFor({"org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils",
        "org.wso2.carbon.context.CarbonContext"})
@PrepareForTest({DeviceMgtAPIUtils.class, MultitenantUtils.class, CarbonContext.class})
public class DeviceManagementServiceImplTest {

    private static final Log log = LogFactory.getLog(DeviceManagementServiceImplTest.class);
    private static final String TEST_DEVICE_TYPE = "TEST-DEVICE-TYPE";
    private static final String TEST_DEVICE_NAME = "TEST-DEVICE";
    private static final String DEFAULT_USERNAME = "admin";
    private static final String TENANT_AWARE_USERNAME = "admin@carbon.super";
    private static final String DEFAULT_ROLE = "admin";
    private static final String DEFAULT_OWNERSHIP = "BYOD";
    private static final String DEFAULT_STATUS = "ACTIVE";
    private DeviceManagementService deviceManagementService;
    private DeviceManagementProviderService deviceManagementProviderService;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeClass
    public void init() throws Exception {
        log.info("Initializing DeviceManagementServiceImpl tests");
        initMocks(this);
        this.deviceManagementProviderService = Mockito
                .mock(DeviceManagementProviderServiceImpl.class, Mockito.RETURNS_MOCKS);
        this.deviceManagementService = new DeviceManagementServiceImpl();
        Mockito.when(this.deviceManagementProviderService.isEnrolled(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(true).thenReturn(false).thenThrow(new DeviceManagementException());
    }

    @Test(description = "Testing if the device is enrolled when the device is enrolled.")
    public void testIsEnrolledWhenDeviceIsEnrolled() throws Exception {
        PowerMockito.spy(DeviceMgtAPIUtils.class);
        PowerMockito.doReturn(this.deviceManagementProviderService)
                .when(DeviceMgtAPIUtils.class, "getDeviceManagementService");
        Response response = this.deviceManagementService.isEnrolled(TEST_DEVICE_TYPE, UUID.randomUUID().toString());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test(description = "Testing if the device is enrolled when the device is not enrolled.",
            dependsOnMethods = "testIsEnrolledWhenDeviceIsEnrolled")
    public void testIsEnrolledWhenDeviceIsNotEnrolled() throws Exception {
        PowerMockito.spy(DeviceMgtAPIUtils.class);
        PowerMockito.doReturn(this.deviceManagementProviderService)
                .when(DeviceMgtAPIUtils.class, "getDeviceManagementService");
        Response response = this.deviceManagementService.isEnrolled(TEST_DEVICE_TYPE, UUID.randomUUID().toString());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test(description = "Testing if the device enrolled api when exception occurred.",
            dependsOnMethods = "testIsEnrolledWhenDeviceIsNotEnrolled")
    public void testIsEnrolledError() throws Exception {
        PowerMockito.spy(DeviceMgtAPIUtils.class);
        PowerMockito.doReturn(this.deviceManagementProviderService)
                .when(DeviceMgtAPIUtils.class, "getDeviceManagementService");
        Response response = this.deviceManagementService.isEnrolled(TEST_DEVICE_TYPE, UUID.randomUUID().toString());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test(description = "Testing get devices when request exists both name and role.")
    public void testGetDevicesWhenBothNameAndRoleAvailable() throws Exception {
        DeviceAccessAuthorizationService deviceAccessAuthorizationService = Mockito
                .mock(DeviceAccessAuthorizationServiceImpl.class);
        PowerMockito.mockStatic(DeviceMgtAPIUtils.class);
        PowerMockito.doReturn(this.deviceManagementProviderService)
                .when(DeviceMgtAPIUtils.class, "getDeviceManagementService");
        PowerMockito.doReturn(deviceAccessAuthorizationService)
                .when(DeviceMgtAPIUtils.class, "getDeviceAccessAuthorizationService");
        Response response = this.deviceManagementService
                .getDevices(TEST_DEVICE_NAME, TEST_DEVICE_TYPE, DEFAULT_USERNAME, null, DEFAULT_ROLE, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, null, null, false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test(description = "Testing get devices with correct request.")
    public void testGetDevices() throws Exception {
        DeviceAccessAuthorizationService deviceAccessAuthorizationService = Mockito
                .mock(DeviceAccessAuthorizationServiceImpl.class, Mockito.RETURNS_MOCKS);

        PowerMockito.spy(DeviceMgtAPIUtils.class);
        PowerMockito.spy(MultitenantUtils.class);
        PowerMockito.spy(CarbonContext.class);

        PowerMockito.doReturn(this.deviceManagementProviderService)
                .when(DeviceMgtAPIUtils.class, "getDeviceManagementService");
        PowerMockito.doReturn(deviceAccessAuthorizationService)
                .when(DeviceMgtAPIUtils.class, "getDeviceAccessAuthorizationService");
        PowerMockito.doReturn(TENANT_AWARE_USERNAME)
                .when(MultitenantUtils.class, "getTenantAwareUsername", Mockito.anyString());
        PowerMockito.doReturn(Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS))
                .when(CarbonContext.class, "getThreadLocalCarbonContext");

        Response response = this.deviceManagementService
                .getDevices(null, TEST_DEVICE_TYPE, DEFAULT_USERNAME, null, DEFAULT_ROLE, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, null, null, false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }
}
