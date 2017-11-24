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

import org.apache.axis2.AxisFault;
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
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.caching.impl.CacheImpl;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherService;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherServiceImpl;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceAgentService;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.DeviceTypeManagementAdminService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.DeviceMgtAPITestHelper;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub;
import org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto;
import org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.policy.mgt.core.PolicyManagerServiceImpl;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.cache.CacheManager;
import javax.ws.rs.core.Response;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class holds the unit tests for the class {@link DeviceAgentServiceImpl}
 */
@PowerMockIgnore("javax.ws.rs.*")
@SuppressStaticInitializationFor({"org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils",
        "org.wso2.carbon.context.CarbonContext", "org.wso2.carbon.context.internal.CarbonContextDataHolder"})
@PrepareForTest({DeviceMgtAPIUtils.class, DeviceManagementProviderService.class,
        DeviceAccessAuthorizationService.class, EventStreamAdminServiceStub.class, PrivilegedCarbonContext.class,
        CarbonContext.class, CarbonUtils.class})
public class DeviceAgentServiceTest {

    private static final Log log = LogFactory.getLog(DeviceTypeManagementAdminService.class);
    private DeviceManagementProviderService deviceManagementProviderService;
    private DeviceAgentService deviceAgentService;
    private EventStreamAdminServiceStub eventStreamAdminServiceStub;
    private PrivilegedCarbonContext privilegedCarbonContext;
    private CarbonContext carbonContext;
    private CacheManager cacheManager;
    private DeviceAccessAuthorizationService deviceAccessAuthorizationService;
    private static final String TEST_DEVICE_TYPE = "TEST-DEVICE-TYPE";
    private static final String TEST_DEVICE_IDENTIFIER = "11222334455";
    private static final String AUTHENTICATED_USER = "admin";
    private static final String MONITOR_OPERATION = "POLICY_MONITOR";
    private static Device demoDevice;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeClass
    public void init() {
        log.info("Initializing DeviceAgent tests");
        initMocks(this);
        this.deviceManagementProviderService = Mockito
                .mock(DeviceManagementProviderServiceImpl.class, Mockito.RETURNS_MOCKS);
        this.deviceAgentService = new DeviceAgentServiceImpl();
        this.deviceAccessAuthorizationService = Mockito.mock(DeviceAccessAuthorizationServiceImpl.class,
                Mockito.RETURNS_MOCKS);
        this.privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class, Mockito.RETURNS_MOCKS);
        this.carbonContext = Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS);
        this.eventStreamAdminServiceStub = Mockito.mock(EventStreamAdminServiceStub.class, Mockito.RETURNS_MOCKS);
        demoDevice = DeviceMgtAPITestHelper.generateDummyDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        this.cacheManager = Mockito.mock(CacheManager.class, Mockito.RETURNS_MOCKS);
    }

    @Test(description = "Test device Enrollment when the device is null")
    public void testEnrollDeviceWithNullDevice() {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Response response = this.deviceAgentService.enrollDevice(null);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
    }

    @Test(description = "Test device enrollment when device type is null.")
    public void testEnrollDeviceWithNullDeviceType() {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Device device = DeviceMgtAPITestHelper.generateDummyDevice(null, TEST_DEVICE_IDENTIFIER);
        Response response = this.deviceAgentService.enrollDevice(device);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
    }

    @Test(description = "Test device enrollment of a device with null device identifier.")
    public void testEnrollNewDeviceWithNullDeviceIdentifier() {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Device device = DeviceMgtAPITestHelper.generateDummyDevice(TEST_DEVICE_TYPE, null);
        Response response = this.deviceAgentService.enrollDevice(device);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
    }

    @Test(description = "Test an already enrolled device")
    public void testEnrollExistingDevice() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.getDevice(Mockito.any())).thenReturn(demoDevice);
        Device device = DeviceMgtAPITestHelper.generateDummyDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Mockito.when(this.deviceManagementProviderService.getDevice(Mockito.any())).thenReturn(device);
        Response response = this.deviceAgentService.enrollDevice(device);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test the device enrollment success scenario.")
    public void testEnrollDeviceSuccess() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getAuthenticatedUser"))
                .toReturn(AUTHENTICATED_USER);
        EnrolmentInfo enrolmentInfo = demoDevice.getEnrolmentInfo();
        enrolmentInfo.setStatus(EnrolmentInfo.Status.INACTIVE);
        demoDevice.setEnrolmentInfo(enrolmentInfo);
        Mockito.when(this.deviceManagementProviderService.getDevice(Mockito.any())).thenReturn(demoDevice);
        Response response = this.deviceAgentService.enrollDevice(demoDevice);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "The response status should be 200");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test the device enrollment with device management exception.")
    public void testEnrollDeviceWithDeviceManagementException() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getAuthenticatedUser"))
                .toReturn(AUTHENTICATED_USER);
        Device device = DeviceMgtAPITestHelper.generateDummyDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        EnrolmentInfo enrolmentInfo = device.getEnrolmentInfo();
        enrolmentInfo.setStatus(EnrolmentInfo.Status.INACTIVE);
        device.setEnrolmentInfo(enrolmentInfo);
        Mockito.when(this.deviceManagementProviderService.getDevice(Mockito.any())).thenReturn(device);
        Mockito.when(this.deviceManagementProviderService.enrollDevice(Mockito.any()))
                .thenThrow(new DeviceManagementException());
        Response response = this.deviceAgentService.enrollDevice(device);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test dis-enrolling the device success scenario.")
    public void testDisEnrollDeviceSuccess() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.disenrollDevice(Mockito.any())).thenReturn(true);
        Response response = deviceAgentService.disEnrollDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "The response status should be 200");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test dis-enrolling non existing device.")
    public void testDisEnrollWithNonExistingDevice() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Response response = deviceAgentService.disEnrollDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode(),
                "The response status should be 204");
    }

    @Test(description = "Test dis-enrolling device where device management exception is thrown.")
    public void testDisEnrollingDeviceWithDeviceManagementException() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.disenrollDevice(Mockito.any())).thenThrow(new
                DeviceManagementException());
        Response response = deviceAgentService.disEnrollDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test device update scenario with device management exception.")
    public void testUpdateDeviceWithDeviceManagementException() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.getDevice(Mockito.any())).thenThrow(new
                DeviceManagementException());
        Device testDevice = DeviceMgtAPITestHelper.generateDummyDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Response response = deviceAgentService.updateDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER, testDevice);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test update device scenario when the device is null.")
    public void testUpdateDeviceWithNoDevice() {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Response response = deviceAgentService.updateDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER, null);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
    }

    @Test(description = "Test the update device scenario when there is no enrolled device.")
    public void testUpdateDeviceWithNonExistingDevice() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.getDevice(Mockito.any())).thenReturn(null);
        Device testDevice = DeviceMgtAPITestHelper.generateDummyDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Response response = deviceAgentService.updateDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER, testDevice);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode(),
                "The response status should be 404");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test update device with device access authorization exception.")
    public void testEnrollDeviceWithDeviceAccessAuthorizationException() throws DeviceManagementException,
            DeviceAccessAuthorizationException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getDeviceAccessAuthorizationService")).toReturn(this.deviceAccessAuthorizationService);
        Device testDevice = DeviceMgtAPITestHelper.generateDummyDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Mockito.when(this.deviceManagementProviderService.getDevice(Mockito.any())).thenReturn(testDevice);
        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenThrow(new DeviceAccessAuthorizationException());
        Response response = deviceAgentService.updateDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER, testDevice);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        Mockito.reset(this.deviceManagementProviderService);
        Mockito.reset(this.deviceAccessAuthorizationService);
    }

    @Test(description = "Test update device when user does not have device access permission.")
    public void testUpdateDeviceWithNoDeviceAccessPermission() throws DeviceManagementException,
            DeviceAccessAuthorizationException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getDeviceAccessAuthorizationService")).toReturn(this.deviceAccessAuthorizationService);
        Device testDevice = DeviceMgtAPITestHelper.generateDummyDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Mockito.when(this.deviceManagementProviderService.getDevice(Mockito.any())).thenReturn(testDevice);
        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(false);
        Response response = deviceAgentService.updateDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER, testDevice);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(),
                "The response status should be 401");
        Mockito.reset(this.deviceManagementProviderService);
        Mockito.reset(this.deviceAccessAuthorizationService);
    }

    @Test(description = "Test update device when device modification is unsuccessful.")
    public void testUpdateDeviceWithUnsuccessfulDeviceModification() throws DeviceManagementException,
            DeviceAccessAuthorizationException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getDeviceAccessAuthorizationService")).toReturn(this.deviceAccessAuthorizationService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getAuthenticatedUser")).toReturn(AUTHENTICATED_USER);
        Device testDevice = DeviceMgtAPITestHelper.generateDummyDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Mockito.when(this.deviceManagementProviderService.getDevice(Mockito.any())).thenReturn(testDevice);
        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(true);
        Mockito.when(this.deviceManagementProviderService.modifyEnrollment(Mockito.any())).thenReturn(false);
        Response response = deviceAgentService.updateDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER, testDevice);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_MODIFIED.getStatusCode(),
                "The response status should be 304");
        Mockito.reset(this.deviceManagementProviderService);
        Mockito.reset(this.deviceAccessAuthorizationService);
    }

    @Test(description = "Test updating device when modify enrollment throws exception")
    public void testUpdateDeviceWithModifyEnrollmentFailure() throws DeviceManagementException,
            DeviceAccessAuthorizationException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getDeviceAccessAuthorizationService")).toReturn(this.deviceAccessAuthorizationService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getAuthenticatedUser")).toReturn(AUTHENTICATED_USER);
        Device testDevice = DeviceMgtAPITestHelper.generateDummyDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Mockito.when(this.deviceManagementProviderService.getDevice(Mockito.any())).thenReturn(testDevice);
        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(true);
        Mockito.when(this.deviceManagementProviderService.modifyEnrollment(Mockito.any()))
                .thenThrow(new DeviceManagementException());
        Response response = deviceAgentService.updateDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER, testDevice);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        Mockito.reset(this.deviceManagementProviderService);
        Mockito.reset(this.deviceAccessAuthorizationService);
    }

    @Test(description = "Test updating device success scenario.")
    public void testUpdateDeviceSuccess() throws DeviceManagementException, DeviceAccessAuthorizationException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getDeviceAccessAuthorizationService")).toReturn(this.deviceAccessAuthorizationService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getAuthenticatedUser")).toReturn(AUTHENTICATED_USER);
        Device testDevice = DeviceMgtAPITestHelper.generateDummyDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Mockito.when(this.deviceManagementProviderService.getDevice(Mockito.any())).thenReturn(testDevice);
        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(true);
        Mockito.when(this.deviceManagementProviderService.modifyEnrollment(Mockito.any())).thenReturn((true));
        Response response = deviceAgentService.updateDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER, testDevice);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.ACCEPTED.getStatusCode(),
                "The response status should be 202");
        Mockito.reset(this.deviceManagementProviderService);
        Mockito.reset(this.deviceAccessAuthorizationService);
    }

    @Test(description = "Test publish events with null payload.")
    public void testPublishEventsWithNullPayload() {
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(this.privilegedCarbonContext);
        Mockito.when(this.privilegedCarbonContext.getTenantDomain())
                .thenReturn(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Map<String, Object> payload = null;
        Response response = this.deviceAgentService.publishEvents(payload, TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
        List<Object> payloadList = null;
        Response response2 = this.deviceAgentService.publishEvents(payloadList, TEST_DEVICE_TYPE,
                TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response2, "Response should not be null");
        Assert.assertEquals(response2.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
    }

    @Test(description = "Test publish events with no device access authorization.")
    public void testPublishEventsWithoutAuthorization() throws DeviceAccessAuthorizationException {
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(this.privilegedCarbonContext);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getDeviceAccessAuthorizationService")).toReturn(this.deviceAccessAuthorizationService);
        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(false);
        Mockito.when(this.privilegedCarbonContext.getTenantDomain())
                .thenReturn(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Map<String, Object> payload = new HashMap<>();
        Response response = this.deviceAgentService.publishEvents(payload, TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(),
                "The response status should be 401");
        List<Object> payloadList = new ArrayList<>();
        Response response2 = this.deviceAgentService.publishEvents(payloadList, TEST_DEVICE_TYPE,
                TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response2, "Response should not be null");
        Assert.assertEquals(response2.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(),
                "The response status should be 401");
        Mockito.reset(this.deviceAccessAuthorizationService);
    }

    @Test(description = "Test publish events when device access authorization exception is thrown.")
    public void testPublishEventsWithDeviceAccessAuthorizationException() throws DeviceAccessAuthorizationException {
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(this.privilegedCarbonContext);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getDeviceAccessAuthorizationService")).toReturn(this.deviceAccessAuthorizationService);
        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenThrow(new DeviceAccessAuthorizationException());
        Mockito.when(this.privilegedCarbonContext.getTenantDomain())
                .thenReturn(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Map<String, Object> payload = new HashMap<>();
        Response response = this.deviceAgentService.publishEvents(payload, TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        List<Object> payloadList = new ArrayList<>();
        Response response2 = this.deviceAgentService.publishEvents(payloadList, TEST_DEVICE_TYPE,
                TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response2, "Response should not be null");
        Assert.assertEquals(response2.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        Mockito.reset(this.deviceAccessAuthorizationService);
    }

    @Test(description = "Test event publishing when the event stream dao is null.")
    public void testEventPublishWithNullEventAttributesAndNullEventStreamDefDAO()
            throws DeviceAccessAuthorizationException, RemoteException {
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(this.privilegedCarbonContext);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getDeviceAccessAuthorizationService")).toReturn(this.deviceAccessAuthorizationService);
        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(true);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getEventStreamAdminServiceStub"))
                .toReturn(this.eventStreamAdminServiceStub);
        Mockito.when(this.eventStreamAdminServiceStub.getStreamDefinitionDto(Mockito.anyString())).thenReturn(null);
        Map<String, Object> payload = new HashMap<>();
        CacheImpl cache = Mockito.mock(CacheImpl.class);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDynamicEventCache"))
                .toReturn(cache);
        Response response = this.deviceAgentService.publishEvents(payload, TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
        List<Object> payloadList = new ArrayList<>();
        Response response2 = this.deviceAgentService.publishEvents(payloadList, TEST_DEVICE_TYPE,
                TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response2, "Response should not be null");
        Assert.assertEquals(response2.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
        Mockito.reset(eventStreamAdminServiceStub);
    }

    @Test(description = "Test the error scenario of Publishing Events with null event attributes.")
    public void testEventPublishWithEventAttributesNULLAndPublishEventsFailure() throws
            DeviceAccessAuthorizationException, RemoteException {
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(this.privilegedCarbonContext);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getDeviceAccessAuthorizationService")).toReturn(this.deviceAccessAuthorizationService);
        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(true);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getEventStreamAdminServiceStub"))
                .toReturn(this.eventStreamAdminServiceStub);
        EventStreamAttributeDto eventStreamAttributeDto = Mockito.mock(EventStreamAttributeDto.class,
                Mockito.RETURNS_MOCKS);
        EventStreamDefinitionDto eventStreamDefinitionDto = Mockito.mock(EventStreamDefinitionDto.class,
                Mockito.RETURNS_MOCKS);
        Mockito.when(this.eventStreamAdminServiceStub.getStreamDefinitionDto(Mockito.anyString()))
                .thenReturn(eventStreamDefinitionDto);
        Mockito.when(eventStreamDefinitionDto.getPayloadData()).thenReturn(new EventStreamAttributeDto[]{});
        EventsPublisherService eventPublisherService = Mockito.mock(EventsPublisherServiceImpl.class,
                Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getEventPublisherService"))
                .toReturn(eventPublisherService);
        Map<String, Object> payload = new HashMap<>();
        CacheImpl cache = Mockito.mock(CacheImpl.class);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDynamicEventCache"))
                .toReturn(cache);
        Response response = this.deviceAgentService.publishEvents(payload, TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        List<Object> payloadList = new ArrayList<>();
        Response response2 = this.deviceAgentService.publishEvents(payloadList, TEST_DEVICE_TYPE,
                TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response2, "Response should not be null");
        Assert.assertEquals(response2.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
    }

    @Test(description = "Test Event publishing success scenario.")
    public void testEventPublishWithEventAttributesNULLAndPublishEventsSuccess()
            throws DeviceAccessAuthorizationException, RemoteException, DataPublisherConfigurationException {
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(this.privilegedCarbonContext);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getDeviceAccessAuthorizationService")).toReturn(this.deviceAccessAuthorizationService);
        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(true);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getEventStreamAdminServiceStub"))
                .toReturn(this.eventStreamAdminServiceStub);
        EventStreamAttributeDto eventStreamAttributeDto = Mockito.mock(EventStreamAttributeDto.class,
                Mockito.RETURNS_MOCKS);
        EventStreamDefinitionDto eventStreamDefinitionDto = Mockito.mock(EventStreamDefinitionDto.class,
                Mockito.RETURNS_MOCKS);
        Mockito.when(this.eventStreamAdminServiceStub.getStreamDefinitionDto(Mockito.anyString()))
                .thenReturn(eventStreamDefinitionDto);
        Mockito.when(eventStreamDefinitionDto.getPayloadData()).thenReturn(new EventStreamAttributeDto[]{});
        EventsPublisherService eventPublisherService = Mockito.mock(EventsPublisherServiceImpl.class,
                Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getEventPublisherService"))
                .toReturn(eventPublisherService);
        Mockito.when(eventPublisherService.publishEvent(Mockito.anyString(), Mockito.anyString(), Mockito.any(),
                Mockito.any(), Mockito.any())).thenReturn(true);
        Map<String, Object> payload = new HashMap<>();
        CacheImpl cache = Mockito.mock(CacheImpl.class);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDynamicEventCache"))
                .toReturn(cache);
        Response response = this.deviceAgentService.publishEvents(payload, TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "The response status should be 200");
        List<Object> payloadList = new ArrayList<>();
        Response response2 = this.deviceAgentService.publishEvents(payloadList,
                TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response2, "Response should not be null");
        Assert.assertEquals(response2.getStatus(), Response.Status.OK.getStatusCode(),
                "The response status should be 200");
    }

    @Test(description = "Test event publishing when PublishEvents throws DataPublisherConfigurationException.")
    public void testPublishEventsDataPublisherConfigurationException() throws DeviceAccessAuthorizationException,
            RemoteException, DataPublisherConfigurationException {
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(this.privilegedCarbonContext);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getDeviceAccessAuthorizationService")).toReturn(this.deviceAccessAuthorizationService);
        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(true);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getEventStreamAdminServiceStub"))
                .toReturn(this.eventStreamAdminServiceStub);
        EventStreamAttributeDto eventStreamAttributeDto = Mockito.mock(EventStreamAttributeDto.class,
                Mockito.RETURNS_MOCKS);
        EventStreamDefinitionDto eventStreamDefinitionDto = Mockito.mock(EventStreamDefinitionDto.class,
                Mockito.RETURNS_MOCKS);
        Mockito.when(this.eventStreamAdminServiceStub.getStreamDefinitionDto(Mockito.anyString()))
                .thenReturn(eventStreamDefinitionDto);
        Mockito.when(eventStreamDefinitionDto.getPayloadData()).thenReturn(new EventStreamAttributeDto[]{});
        EventsPublisherService eventPublisherService = Mockito.mock(EventsPublisherServiceImpl.class,
                Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getEventPublisherService"))
                .toReturn(eventPublisherService);
        Mockito.when(eventPublisherService.publishEvent(Mockito.anyString(), Mockito.anyString(), Mockito.any(),
                Mockito.any(), Mockito.any())).thenThrow(
                new DataPublisherConfigurationException("meta data[0] should have the device Id field"));
        Map<String, Object> payload = new HashMap<>();
        CacheImpl cache = Mockito.mock(CacheImpl.class);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDynamicEventCache"))
                .toReturn(cache);
        Response response = this.deviceAgentService.publishEvents(payload, TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        List<Object> payloadList = new ArrayList<>();
        Response response2 = this.deviceAgentService.publishEvents(payloadList, TEST_DEVICE_TYPE,
                TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response2, "Response should not be null");
        Assert.assertEquals(response2.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
    }

    @Test(description = "Test Publish events with Axis Fault.")
    public void testPublishEventsWithAxisFault() throws DeviceAccessAuthorizationException {
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(this.privilegedCarbonContext);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getDeviceAccessAuthorizationService")).toReturn(this.deviceAccessAuthorizationService);
        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(true);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getEventStreamAdminServiceStub"))
                .toThrow(new AxisFault(""));
        Map<String, Object> payload = new HashMap<>();
        CacheImpl cache = Mockito.mock(CacheImpl.class);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDynamicEventCache"))
                .toReturn(cache);
        Response response = this.deviceAgentService.publishEvents(payload, TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        List<Object> payloadList = new ArrayList<>();
        Response response2 = this.deviceAgentService.publishEvents(payloadList, TEST_DEVICE_TYPE,
                TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response2, "Response should not be null");
        Assert.assertEquals(response2.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
    }

    @Test(description = "Test Publishing events when EventStreamAdminService throws Remote exception.")
    public void testPublishEventsWithRemoteException() throws DeviceAccessAuthorizationException {
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(this.privilegedCarbonContext);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getDeviceAccessAuthorizationService")).toReturn(this.deviceAccessAuthorizationService);
        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(true);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getEventStreamAdminServiceStub"))
                .toThrow(new RemoteException());
        Map<String, Object> payload = new HashMap<>();
        CacheImpl cache = Mockito.mock(CacheImpl.class);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDynamicEventCache"))
                .toReturn(cache);

        Response response = this.deviceAgentService.publishEvents(payload, TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");

        List<Object> payloadList = new ArrayList<>();
        Response response2 = this.deviceAgentService.publishEvents(payloadList, TEST_DEVICE_TYPE,
                TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response2, "Response should not be null");
        Assert.assertEquals(response2.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
    }

    @Test(description = "Test Publishing events when EventStreamAdminService throws JWT exception.")
    public void testPublishEventsWithJWTException() throws DeviceAccessAuthorizationException {
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(this.privilegedCarbonContext);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getDeviceAccessAuthorizationService")).toReturn(this.deviceAccessAuthorizationService);
        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(true);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getEventStreamAdminServiceStub"))
                .toThrow(new JWTClientException());
        Map<String, Object> payload = new HashMap<>();
        CacheImpl cache = Mockito.mock(CacheImpl.class);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDynamicEventCache"))
                .toReturn(cache);

        Response response = this.deviceAgentService.publishEvents(payload, TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");

        List<Object> payloadList = new ArrayList<>();
        Response response2 = this.deviceAgentService.publishEvents(payloadList, TEST_DEVICE_TYPE,
                TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response2, "Response should not be null");
        Assert.assertEquals(response2.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
    }

    @Test(description = "Test Publishing events when EventStreamAdminService throws User Store exception.")
    public void testPublishEventsWithUserStoreException() throws DeviceAccessAuthorizationException {
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(this.privilegedCarbonContext);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getDeviceAccessAuthorizationService")).toReturn(this.deviceAccessAuthorizationService);
        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(true);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getEventStreamAdminServiceStub"))
                .toThrow(new UserStoreException());
        Map<String, Object> payload = new HashMap<>();
        CacheImpl cache = Mockito.mock(CacheImpl.class);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDynamicEventCache"))
                .toReturn(cache);

        Response response = this.deviceAgentService.publishEvents(payload, TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");

        List<Object> payloadList = new ArrayList<>();
        Response response2 = this.deviceAgentService.publishEvents(payloadList, TEST_DEVICE_TYPE,
                TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response2, "Response should not be null");
        Assert.assertEquals(response2.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
    }

    @Test(description = "Test the get pending operation method which return empty device type list.")
    public void testGetPendingOperationsWithNoDeviceType() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes())
                .thenReturn(new ArrayList<String>() {
                });
        Response response = this.deviceAgentService.getPendingOperations(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test the get pending operation method with invalid device identifier.")
    public void testGetPendingOperationsWithInvalidDeviceIdentifier() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isValidDeviceIdentifier"))
                .toReturn(false);
        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add(TEST_DEVICE_TYPE);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes()).thenReturn(deviceTypes);
        Response response = this.deviceAgentService.getPendingOperations(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode(),
                "The response status should be 204");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test the get pending operations success scenario.")
    public void testGetPendingOperationsSuccess() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isValidDeviceIdentifier"))
                .toReturn(true);
        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add(TEST_DEVICE_TYPE);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes()).thenReturn(deviceTypes);
        Response response = this.deviceAgentService.getPendingOperations(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertNotNull(response.getEntity(), "Response entity should not be null.");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "The response status should be 200");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test the scenario when get pending operations throw OperationManagementException.")
    public void testGetPendingOperationsWithOperationManagementException() throws DeviceManagementException,
            OperationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isValidDeviceIdentifier"))
                .toReturn(true);
        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add(TEST_DEVICE_TYPE);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes()).thenReturn(deviceTypes);
        Mockito.when(this.deviceManagementProviderService.getPendingOperations(Mockito.any())).thenThrow(new
                OperationManagementException());
        Response response = this.deviceAgentService.getPendingOperations(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertNotNull(response.getEntity(), "Response entity should not be null.");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test the scenario when getAvailableDeviceTypes throw DeviceManagementException.")
    public void testGetPendingOperationsWithDeviceManagementException() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isValidDeviceIdentifier"))
                .toReturn(true);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes())
                .thenThrow(new DeviceManagementException());
        Response response = this.deviceAgentService.getPendingOperations(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertNotNull(response.getEntity(), "Response entity should not be null.");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test get next pending operation with device type is invalid.")
    public void getNextPendingOperationWithInvalidDeviceType() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes())
                .thenReturn(new ArrayList<String>() {});
        Response response = this.deviceAgentService.getNextPendingOperation(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test get next pending operation with invalid device identifier.")
    public void getNextPendingOperationWithInvalidDeviceIdentifier() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isValidDeviceIdentifier"))
                .toReturn(false);
        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add(TEST_DEVICE_TYPE);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes()).thenReturn(deviceTypes);
        Response response = this.deviceAgentService.getNextPendingOperation(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test the getNextPendingOperation success scenario.")
    public void testGetNextPendingOperationSuccess() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isValidDeviceIdentifier"))
                .toReturn(true);
        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add(TEST_DEVICE_TYPE);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes()).thenReturn(deviceTypes);
        Response response = this.deviceAgentService.getNextPendingOperation(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertNotNull(response.getEntity(), "Response entity should not be null.");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "The response status should be 200");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test get next pending operation with operation management exception.")
    public void getNextPendingOperationWithOperationManagementException() throws DeviceManagementException,
            OperationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isValidDeviceIdentifier"))
                .toReturn(true);
        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add(TEST_DEVICE_TYPE);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes()).thenReturn(deviceTypes);
        Mockito.when(this.deviceManagementProviderService.getNextPendingOperation(Mockito.any())).thenThrow(
                new OperationManagementException());
        Response response = this.deviceAgentService.getNextPendingOperation(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertNotNull(response.getEntity(), "Response entity should not be null.");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test the get next pending operation method with device management exception.")
    public void getNextPendingOperationWithDeviceManagementException() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isValidDeviceIdentifier"))
                .toReturn(true);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes())
                .thenThrow(new DeviceManagementException());
        Response response = this.deviceAgentService.getNextPendingOperation(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertNotNull(response.getEntity(), "Response entity should not be null.");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test update operation method with invalid device type.")
    public void testUpdateOperationWithInvalidDeviceType() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes())
                .thenReturn(new ArrayList<String>() {});
        Operation operation = new Operation();
        Response response = this.deviceAgentService.updateOperation(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER,
                operation);
        Assert.assertNotNull(response, "The response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test update operation when operation is null.")
    public void testUpdateOperationWithNullOperation() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add(TEST_DEVICE_TYPE);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes()).thenReturn(deviceTypes);
        Response response = this.deviceAgentService.updateOperation(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER,
                null);
        Assert.assertNotNull(response, "The response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test update operation method with invalid device identifier.")
    public void testUpdateOperationWithInvalidDeviceIdentifier() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isValidDeviceIdentifier"))
                .toReturn(false);
        Operation operation = new Operation();
        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add(TEST_DEVICE_TYPE);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes()).thenReturn(deviceTypes);
        Response response = this.deviceAgentService.updateOperation(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER,
                operation);
        Assert.assertNotNull(response, "The response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test update operation success scenario.")
    public void testUpdateOperationSuccess() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isValidDeviceIdentifier"))
                .toReturn(true);
        Operation operation = new Operation();
        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add(TEST_DEVICE_TYPE);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes()).thenReturn(deviceTypes);
        Response response = this.deviceAgentService.updateOperation(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER,
                operation);
        Assert.assertNotNull(response, "The response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "The response status should be 200");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test the update Operation method with Policy Monitoring Operation.")
    public void testUpdateOperationSuccessWithPolicyMonitorOperation() throws DeviceManagementException,
            PolicyComplianceException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isValidDeviceIdentifier"))
                .toReturn(true);
        PolicyManagerService policyManagementService = Mockito.mock(PolicyManagerServiceImpl.class, Mockito
                .RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getPolicyManagementService"))
                .toReturn(policyManagementService);
        Mockito.when(policyManagementService.checkCompliance(Mockito.any(), Mockito.any())).thenReturn(true);

        Operation operation = new Operation();
        operation.setCode(MONITOR_OPERATION);
        operation.setStatus(Operation.Status.PENDING);
        operation.setPayLoad(null);
        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add(TEST_DEVICE_TYPE);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes()).thenReturn(deviceTypes);
        Response response = this.deviceAgentService.updateOperation(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER,
                operation);
        Assert.assertNotNull(response, "The response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "The response status should be 200");
    }

    @Test(description = "Test Update Operation with Operation Management Exception.")
    public void testUpdateOperationWithOperationManagementException() throws DeviceManagementException,
            OperationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isValidDeviceIdentifier"))
                .toReturn(true);

        Operation operation = new Operation();
        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add(TEST_DEVICE_TYPE);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes()).thenReturn(deviceTypes);
        Mockito.doThrow(new OperationManagementException()).when(this.deviceManagementProviderService)
                .updateOperation(Mockito.any(), Mockito.any());
        Response response = this.deviceAgentService.updateOperation(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER,
                operation);
        Assert.assertNotNull(response, "The response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test Update operation with Device Management exception.")
    public void testUpdateOperationWithDeviceManagementException() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isValidDeviceIdentifier"))
                .toReturn(true);

        Operation operation = new Operation();
        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add(TEST_DEVICE_TYPE);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes())
                .thenThrow(new DeviceManagementException());
        Response response = this.deviceAgentService.updateOperation(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER,
                operation);
        Assert.assertNotNull(response, "The response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test update operation with Policy Compliance operation.")
    public void testUpdateOperationWithPolicyComplianceException() throws PolicyComplianceException,
            DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isValidDeviceIdentifier"))
                .toReturn(true);
        PolicyManagerService policyManagementService = Mockito.mock(PolicyManagerServiceImpl.class, Mockito
                .RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getPolicyManagementService"))
                .toReturn(policyManagementService);
        Mockito.when(policyManagementService.checkCompliance(Mockito.any(), Mockito.any()))
                .thenThrow(new PolicyComplianceException());

        Operation operation = new Operation();
        operation.setCode(MONITOR_OPERATION);
        operation.setStatus(Operation.Status.PENDING);
        operation.setPayLoad(null);
        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add(TEST_DEVICE_TYPE);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes()).thenReturn(deviceTypes);
        Response response = this.deviceAgentService.updateOperation(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER,
                operation);
        Assert.assertNotNull(response, "The response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
    }

    @Test(description = "Test get operations with null operation status.")
    public void getOperationsWithStatusNull() {
        Response response = this.deviceAgentService.getOperationsByDeviceAndStatus(TEST_DEVICE_TYPE,
                TEST_DEVICE_IDENTIFIER, null);
        Assert.assertNotNull(response, "The response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
    }

    @Test(description = "Test get operations with invalid device types.")
    public void getOperationWithInvalidDeviceType() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes())
                .thenReturn(new ArrayList<String>() {});
        Response response = this.deviceAgentService.getOperationsByDeviceAndStatus(TEST_DEVICE_TYPE,
                TEST_DEVICE_IDENTIFIER, Operation.Status.COMPLETED);
        Assert.assertNotNull(response, "The response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
    }

    @Test(description = "Test get operations success scenario.")
    public void testGetOperationSuccess() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add(TEST_DEVICE_TYPE);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes()).thenReturn(deviceTypes);
        Response response = this.deviceAgentService.getOperationsByDeviceAndStatus(TEST_DEVICE_TYPE,
                TEST_DEVICE_IDENTIFIER, Operation.Status.COMPLETED);
        Assert.assertNotNull(response, "The response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "The response status should be 200");
    }

    @Test(description = "Test the get operation method with operation management exception.")
    public void testGetOperationWithOperationManagementException() throws DeviceManagementException,
            OperationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add(TEST_DEVICE_TYPE);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes()).thenReturn(deviceTypes);
        Mockito.when(this.deviceManagementProviderService.getOperationsByDeviceAndStatus(Mockito.any(), Mockito.any()))
                .thenThrow(new OperationManagementException());
        Response response = this.deviceAgentService.getOperationsByDeviceAndStatus(TEST_DEVICE_TYPE,
                TEST_DEVICE_IDENTIFIER, Operation.Status.COMPLETED);
        Assert.assertNotNull(response, "The response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "Test the get operation method with device management exception.")
    public void testGetOperationsWithDeviceManagementException() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add(TEST_DEVICE_TYPE);
        Mockito.when(this.deviceManagementProviderService.getAvailableDeviceTypes())
                .thenThrow(new DeviceManagementException());
        Response response = this.deviceAgentService.getOperationsByDeviceAndStatus(TEST_DEVICE_TYPE,
                TEST_DEVICE_IDENTIFIER, Operation.Status.COMPLETED);
        Assert.assertNotNull(response, "The response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        Mockito.reset(this.deviceManagementProviderService);
    }
}
