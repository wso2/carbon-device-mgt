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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.search.SearchContext;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.device.details.mgt.impl.DeviceInformationManagerImpl;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchManagerService;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchMgtException;
import org.wso2.carbon.device.mgt.core.search.mgt.impl.SearchManagerServiceImpl;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceManagementService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.Response;

import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class includes unit tests for testing the functionality of {@link DeviceManagementServiceImpl}
 */
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
    private static final String DEFAULT_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";
    private DeviceManagementService deviceManagementService;
    private DeviceAccessAuthorizationService deviceAccessAuthorizationService;
    private DeviceManagementProviderService deviceManagementProviderService;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeClass
    public void init() {
        log.info("Initializing DeviceManagementServiceImpl tests");
        initMocks(this);
        this.deviceManagementProviderService = Mockito
                .mock(DeviceManagementProviderServiceImpl.class, Mockito.RETURNS_MOCKS);
        this.deviceManagementService = new DeviceManagementServiceImpl();
        this.deviceAccessAuthorizationService = Mockito.mock(DeviceAccessAuthorizationServiceImpl.class);
    }

    @Test(description = "Testing if the device is enrolled when the device is enrolled.")
    public void testIsEnrolledWhenDeviceIsEnrolled() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.isEnrolled(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(true);
        Response response = this.deviceManagementService.isEnrolled(TEST_DEVICE_TYPE, UUID.randomUUID().toString());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Testing if the device is enrolled when the device is not enrolled.",
            dependsOnMethods = "testIsEnrolledWhenDeviceIsEnrolled")
    public void testIsEnrolledWhenDeviceIsNotEnrolled() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.isEnrolled(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(false);
        Response response = this.deviceManagementService.isEnrolled(TEST_DEVICE_TYPE, UUID.randomUUID().toString());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Testing if the device enrolled api when exception occurred.",
            dependsOnMethods = "testIsEnrolledWhenDeviceIsNotEnrolled")
    public void testIsEnrolledError() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.isEnrolled(Mockito.any(DeviceIdentifier.class)))
                .thenThrow(new DeviceManagementException());
        Response response = this.deviceManagementService.isEnrolled(TEST_DEVICE_TYPE, UUID.randomUUID().toString());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Testing get devices when request exists both name and role.")
    public void testGetDevicesWhenBothNameAndRoleAvailable() {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceAccessAuthorizationService"))
                .toReturn(this.deviceAccessAuthorizationService);
        Response response = this.deviceManagementService
                .getDevices(TEST_DEVICE_NAME, TEST_DEVICE_TYPE, DEFAULT_USERNAME, null, DEFAULT_ROLE, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, null, null, false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test(description = "Testing get devices with correct request.")
    public void testGetDevices() {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceAccessAuthorizationService"))
                .toReturn(this.deviceAccessAuthorizationService);
        PowerMockito.stub(PowerMockito.method(MultitenantUtils.class, "getTenantAwareUsername"))
                .toReturn(TENANT_AWARE_USERNAME);
        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS));

        Response response = this.deviceManagementService
                .getDevices(null, TEST_DEVICE_TYPE, DEFAULT_USERNAME, null, DEFAULT_ROLE, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, null, null, false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        response = this.deviceManagementService
                .getDevices(TEST_DEVICE_NAME, TEST_DEVICE_TYPE, DEFAULT_USERNAME, null, null, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, null, null, false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        response = this.deviceManagementService
                .getDevices(TEST_DEVICE_NAME, TEST_DEVICE_TYPE, null, null, null, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, null, null, false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        response = this.deviceManagementService
                .getDevices(TEST_DEVICE_NAME, TEST_DEVICE_TYPE, null, null, null, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, null, null, true, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test(description = "Testing get devices when DeviceAccessAuthorizationService is not available")
    public void testGetDevicesWithErroneousDeviceAccessAuthorizationService() {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceAccessAuthorizationService"))
                .toReturn(null);
        Response response = this.deviceManagementService
                .getDevices(null, TEST_DEVICE_TYPE, DEFAULT_USERNAME, null, DEFAULT_ROLE, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, null, null, false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test(description = "Testing get devices when user is the device admin")
    public void testGetDevicesWhenUserIsAdmin() throws DeviceAccessAuthorizationException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceAccessAuthorizationService"))
                .toReturn(this.deviceAccessAuthorizationService);
        PowerMockito.stub(PowerMockito.method(MultitenantUtils.class, "getTenantAwareUsername"))
                .toReturn(TENANT_AWARE_USERNAME);
        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS));
        Mockito.when(deviceAccessAuthorizationService.isDeviceAdminUser()).thenReturn(true);

        Response response = this.deviceManagementService
                .getDevices(null, TEST_DEVICE_TYPE, DEFAULT_USERNAME, null, DEFAULT_ROLE, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, null, null, false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        response = this.deviceManagementService
                .getDevices(null, TEST_DEVICE_TYPE, null, DEFAULT_USERNAME, DEFAULT_ROLE, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, null, null, false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test(description = "Testing get devices when user is unauthorized.")
    public void testGetDevicesWhenUserIsUnauthorized() throws Exception {
        PowerMockito.spy(MultitenantUtils.class);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceAccessAuthorizationService"))
                .toReturn(this.deviceAccessAuthorizationService);
        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS));
        PowerMockito.doReturn(TENANT_AWARE_USERNAME)
                .when(MultitenantUtils.class, "getTenantAwareUsername", DEFAULT_USERNAME);
        PowerMockito.doReturn("newuser@carbon.super").when(MultitenantUtils.class, "getTenantAwareUsername", "newuser");
        Mockito.when(this.deviceAccessAuthorizationService.isDeviceAdminUser()).thenReturn(false);

        Response response = this.deviceManagementService
                .getDevices(null, TEST_DEVICE_TYPE, "newuser", null, DEFAULT_ROLE, DEFAULT_OWNERSHIP, DEFAULT_STATUS, 1,
                        null, null, false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
        Mockito.reset(this.deviceAccessAuthorizationService);
    }

    @Test(description = "Testing get devices with IF-Modified-Since")
    public void testGetDevicesWithModifiedSince() {
        String ifModifiedSince = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date());
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceAccessAuthorizationService"))
                .toReturn(this.deviceAccessAuthorizationService);
        PowerMockito.stub(PowerMockito.method(MultitenantUtils.class, "getTenantAwareUsername"))
                .toReturn(TENANT_AWARE_USERNAME);
        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS));

        Response response = this.deviceManagementService
                .getDevices(null, TEST_DEVICE_TYPE, DEFAULT_USERNAME, null, DEFAULT_ROLE, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, null, ifModifiedSince, false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_MODIFIED.getStatusCode());
        response = this.deviceManagementService
                .getDevices(null, TEST_DEVICE_TYPE, DEFAULT_USERNAME, null, DEFAULT_ROLE, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, null, ifModifiedSince, true, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_MODIFIED.getStatusCode());
        response = this.deviceManagementService
                .getDevices(null, TEST_DEVICE_TYPE, DEFAULT_USERNAME, null, DEFAULT_ROLE, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, null, "ErrorModifiedSince", false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test(description = "Testing get devices with Since")
    public void testGetDevicesWithSince() {
        String since = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date());
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceAccessAuthorizationService"))
                .toReturn(this.deviceAccessAuthorizationService);
        PowerMockito.stub(PowerMockito.method(MultitenantUtils.class, "getTenantAwareUsername"))
                .toReturn(TENANT_AWARE_USERNAME);
        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS));

        Response response = this.deviceManagementService
                .getDevices(null, TEST_DEVICE_TYPE, DEFAULT_USERNAME, null, DEFAULT_ROLE, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, since, null, false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        response = this.deviceManagementService
                .getDevices(null, TEST_DEVICE_TYPE, DEFAULT_USERNAME, null, DEFAULT_ROLE, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, since, null, true, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        response = this.deviceManagementService
                .getDevices(null, TEST_DEVICE_TYPE, DEFAULT_USERNAME, null, DEFAULT_ROLE, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, "ErrorSince", null, false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test(description = "Testing get devices when unable to retrieve devices")
    public void testGetDeviceServerErrorWhenGettingDeviceList() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceAccessAuthorizationService"))
                .toReturn(this.deviceAccessAuthorizationService);
        PowerMockito.stub(PowerMockito.method(MultitenantUtils.class, "getTenantAwareUsername"))
                .toReturn(TENANT_AWARE_USERNAME);
        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS));
        Mockito.when(this.deviceManagementProviderService
                .getAllDevices(Mockito.any(PaginationRequest.class), Mockito.anyBoolean()))
                .thenThrow(new DeviceManagementException());

        Response response = this.deviceManagementService
                .getDevices(null, TEST_DEVICE_TYPE, DEFAULT_USERNAME, null, DEFAULT_ROLE, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, null, null, false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Testing get devices when unable to check if the user is the admin user")
    public void testGetDevicesServerErrorWhenCheckingAdminUser() throws DeviceAccessAuthorizationException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceAccessAuthorizationService"))
                .toReturn(this.deviceAccessAuthorizationService);
        PowerMockito.stub(PowerMockito.method(MultitenantUtils.class, "getTenantAwareUsername"))
                .toReturn(TENANT_AWARE_USERNAME);
        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS));
        Mockito.when(this.deviceAccessAuthorizationService.isDeviceAdminUser())
                .thenThrow(new DeviceAccessAuthorizationException());

        Response response = this.deviceManagementService
                .getDevices(null, TEST_DEVICE_TYPE, DEFAULT_USERNAME, null, DEFAULT_ROLE, DEFAULT_OWNERSHIP,
                        DEFAULT_STATUS, 1, null, null, false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        Mockito.reset(this.deviceAccessAuthorizationService);
    }

    @Test(description = "Testing get devices with correct request")
    public void testGetDeviceTypesByUser() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS));

        Response response = this.deviceManagementService.getDeviceByUser(true, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        response = this.deviceManagementService.getDeviceByUser(false, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test(description = "Testing get devices with correct request when unable to get devices.")
    public void testGetDeviceTypesByUserException() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS));
        Mockito.when(this.deviceManagementProviderService.getDevicesOfUser(Mockito.any(PaginationRequest.class)))
                .thenThrow(new DeviceManagementException());

        Response response = this.deviceManagementService.getDeviceByUser(true, 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test(description = "Testing delete device with correct request.")
    public void testDeleteDevice() {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Response response = this.deviceManagementService.deleteDevice(TEST_DEVICE_TYPE, UUID.randomUUID().toString());
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test(description = "Testing delete unavailable device.")
    public void testDeleteUnavailableDevice() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService
                .getDevice(Mockito.any(DeviceIdentifier.class), Mockito.anyBoolean())).thenReturn(null);
        Response response = this.deviceManagementService.deleteDevice(TEST_DEVICE_TYPE, UUID.randomUUID().toString());
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Testing delete device when unable to delete device.")
    public void testDeleteDeviceException() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.disenrollDevice(Mockito.any(DeviceIdentifier.class)))
                .thenThrow(new DeviceManagementException());
        Response response = this.deviceManagementService.deleteDevice(TEST_DEVICE_TYPE, UUID.randomUUID().toString());
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Testing getting device location")
    public void testGetDeviceLocation() {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceInformationManagerService")).
                toReturn(Mockito.mock(DeviceInformationManagerImpl.class, Mockito.RETURNS_MOCKS));
        Response response = this.deviceManagementService
                .getDeviceLocation(TEST_DEVICE_TYPE, UUID.randomUUID().toString(), null);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test(description = "Testing getting device location when unable to retrieve location")
    public void testGetDeviceLocationException() throws DeviceDetailsMgtException {
        DeviceInformationManager deviceInformationManager = Mockito
                .mock(DeviceInformationManagerImpl.class, Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceInformationManagerService")).
                toReturn(deviceInformationManager);
        Mockito.when(deviceInformationManager.getDeviceLocation(Mockito.any(DeviceIdentifier.class)))
                .thenThrow(new DeviceDetailsMgtException());
        Response response = this.deviceManagementService
                .getDeviceLocation(TEST_DEVICE_TYPE, UUID.randomUUID().toString(), null);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test(description = "Testing getting device information")
    public void testGetDeviceInformation() {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceInformationManagerService")).
                toReturn(Mockito.mock(DeviceInformationManagerImpl.class, Mockito.RETURNS_MOCKS));
        Response response = this.deviceManagementService
                .getDeviceInformation(TEST_DEVICE_TYPE, UUID.randomUUID().toString(), null);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test(description = "Testing getting device information when unable to retrieve information")
    public void testGetDeviceInformationException() throws DeviceDetailsMgtException {
        DeviceInformationManager deviceInformationManager = Mockito
                .mock(DeviceInformationManagerImpl.class, Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceInformationManagerService")).
                toReturn(deviceInformationManager);
        Mockito.when(deviceInformationManager.getDeviceInfo(Mockito.any(DeviceIdentifier.class)))
                .thenThrow(new DeviceDetailsMgtException());
        Response response = this.deviceManagementService
                .getDeviceInformation(TEST_DEVICE_TYPE, UUID.randomUUID().toString(), null);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test(description = "Testing getting device features")
    public void testGetFeaturesOfDevice() {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Response response = this.deviceManagementService
                .getFeaturesOfDevice(TEST_DEVICE_TYPE, UUID.randomUUID().toString(), null);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test(description = "Testing getting device features when feature manager is not registered for the device type")
    public void testGetFeaturesOfDeviceWhenFeatureManagerIsNotRegistered() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.getFeatureManager(Mockito.anyString())).thenReturn(null);
        Response response = this.deviceManagementService
                .getFeaturesOfDevice(TEST_DEVICE_TYPE, UUID.randomUUID().toString(), null);
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Testing getting device features when unable to get the feature manager")
    public void testGetFeaturesException() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.getFeatureManager(Mockito.anyString()))
                .thenThrow(new DeviceManagementException());
        Response response = this.deviceManagementService
                .getFeaturesOfDevice(TEST_DEVICE_TYPE, UUID.randomUUID().toString(), null);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Testing search devices")
    public void testSearchDevices() {
        SearchManagerService searchManagerService = Mockito.mock(SearchManagerServiceImpl.class, Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getSearchManagerService"))
                .toReturn(searchManagerService);
        Response response = this.deviceManagementService
                .searchDevices(10, 5, new SearchContext());
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Expects to return HTTP 200 when the search is successful");
    }

    @Test(description = "Testing search devices when unable to search devices")
    public void testSearchDevicesException() throws SearchMgtException {
        SearchManagerService searchManagerService = Mockito.mock(SearchManagerServiceImpl.class, Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getSearchManagerService"))
                .toReturn(searchManagerService);
        Mockito.when(searchManagerService.search(Mockito.any(SearchContext.class))).thenThrow(new SearchMgtException());
        Response response = this.deviceManagementService
                .searchDevices(10, 5, new SearchContext());
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Expects HTTP 500 when an exception occurred while searching the device");
    }

    @Test(description = "Testing getting installed applications of a device")
    public void testGetInstalledApplications() {
        ApplicationManagementProviderService applicationManagementProviderService = Mockito
                .mock(ApplicationManagementProviderService.class, Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getAppManagementService"))
                .toReturn(applicationManagementProviderService);
        Response response = this.deviceManagementService
                .getInstalledApplications(TEST_DEVICE_TYPE, UUID.randomUUID().toString(), "", 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Expects to return HTTP 200 when the application list is retrieved successfully.");
    }

    @Test(description = "Testing getting installed applications of a device when unable to fetch applications")
    public void testGetInstalledApplicationsException() throws ApplicationManagementException {
        ApplicationManagementProviderService applicationManagementProviderService = Mockito
                .mock(ApplicationManagementProviderService.class, Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getAppManagementService"))
                .toReturn(applicationManagementProviderService);
        Mockito.when(
                applicationManagementProviderService.getApplicationListForDevice(Mockito.any(DeviceIdentifier.class)))
                .thenThrow(new ApplicationManagementException());
        Response response = this.deviceManagementService
                .getInstalledApplications(TEST_DEVICE_TYPE, UUID.randomUUID().toString(), "", 10, 5);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Expects HTTP 500 when an exception occurred while retrieving application list of the device");
    }

    @Test(description = "Testing getting operation list of a device")
    public void testGetDeviceOperations() {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Response response = this.deviceManagementService
                .getDeviceOperations(TEST_DEVICE_TYPE, UUID.randomUUID().toString(), "", 10, 5, DEFAULT_USERNAME);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Expects to return HTTP 200 when the operation is retrieved successfully.");
    }

    @Test(description = "Testing getting operation list of a device when unable to retrieve operations")
    public void testGetDeviceOperationsException() throws OperationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.getOperations(Mockito.any(DeviceIdentifier.class),
                Mockito.any(PaginationRequest.class))).thenThrow(new OperationManagementException());
        Response response = this.deviceManagementService
                .getDeviceOperations(TEST_DEVICE_TYPE, UUID.randomUUID().toString(), "", 10, 5, DEFAULT_USERNAME);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Expects to return HTTP 500 when an exception occurred while retrieving operation list of the device");
    }

    @Test(description = "Testing getting effective policy of a device")
    public void testGetEffectivePolicyOfDevice() throws PolicyManagementException {
        PolicyManagerService policyManagerService = Mockito.mock(PolicyManagerService.class, Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getPolicyManagementService"))
                .toReturn(policyManagerService);
        Response response = this.deviceManagementService
                .getEffectivePolicyOfDevice(TEST_DEVICE_TYPE, UUID.randomUUID().toString(), null);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Expects to return HTTP 200 when retrieving effective policy is successful");
    }

    @Test(description = "Testing getting effective policy of a device when unable to retrieve effective policy")
    public void testGetEffectivePolicyOfDeviceException() throws PolicyManagementException {
        PolicyManagerService policyManagerService = Mockito.mock(PolicyManagerService.class, Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getPolicyManagementService"))
                .toReturn(policyManagerService);
        Mockito.when(policyManagerService.getAppliedPolicyToDevice(Mockito.any(DeviceIdentifier.class)))
                .thenThrow(new PolicyManagementException());
        Response response = this.deviceManagementService
                .getEffectivePolicyOfDevice(TEST_DEVICE_TYPE, UUID.randomUUID().toString(), null);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Expects to return HTTP 500 when an exception occurred while getting effective policy of the device");
    }
}
