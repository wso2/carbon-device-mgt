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
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.cache.CacheManager;
import javax.ws.rs.core.Response;
import java.rmi.RemoteException;
import java.util.HashMap;
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
    private static final String TENANT_DOMAIN = "carbon.super";
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
    public void testEnrollDeviceWithDeviceIsNULL() {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Response response = this.deviceAgentService.enrollDevice(null);

        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
    }

    @Test(description = "Test device enrollment when device type is null.")
    public void testEnrollDeviceWithDeviceTypeNull() {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Device device = DeviceMgtAPITestHelper.generateDummyDevice(null, TEST_DEVICE_IDENTIFIER);
        Response response = this.deviceAgentService.enrollDevice(device);

        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
    }

    @Test(description = "Test device enrollment of a device with null device identifier.")
    public void testEnrollNewDeviceWithDeviceIdentifierIsNull() {
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
    public void testEnrollDeviceWithException() throws DeviceManagementException {
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
    public void testDisEnrollNonExistingDevice() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Response response = deviceAgentService.disEnrollDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode(),
                "The response status should be 204");
    }

    @Test(description = "Test dis-enrolling device error")
    public void testDisEnrollingDeviceError() throws DeviceManagementException {
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
    public void testUpdateDeviceDMException() throws DeviceManagementException {
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
    public void testUpdatingNonExistingDevice() throws DeviceManagementException {
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
    public void testEnrollDeviceWithDeviceAccessAuthException() throws DeviceManagementException,
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
    public void testUpdateDeviceNOTModify() throws DeviceManagementException, DeviceAccessAuthorizationException {
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
    public void testUpdateDeviceWithModifyEnrollmentFailure() throws DeviceManagementException, DeviceAccessAuthorizationException {
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
        Mockito.when(this.deviceManagementProviderService.modifyEnrollment(Mockito.any())).thenThrow(new DeviceManagementException());
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
        Mockito.when(this.privilegedCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

        Map<String, Object> payload = null;
        Response response = this.deviceAgentService.publishEvents(payload, TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400");
    }

    @Test(description = "Test publish events with no device access authorization.")
    public void testPublishEventsWithOutAuthorization() throws DeviceAccessAuthorizationException {
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(this.privilegedCarbonContext);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class,
                "getDeviceAccessAuthorizationService")).toReturn(this.deviceAccessAuthorizationService);

        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenReturn(false);
        Mockito.when(this.privilegedCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        Map<String, Object> payload = new HashMap<>();
        Response response = this.deviceAgentService.publishEvents(payload, TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(),
                "The response status should be 401");
        Mockito.reset(this.deviceAccessAuthorizationService);
    }

    @Test
    public void testPublishEventsWithDeviceAccessAuthException() throws DeviceAccessAuthorizationException {
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(this.privilegedCarbonContext);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceAccessAuthorizationService"))
                .toReturn(this.deviceAccessAuthorizationService);
        Mockito.when(this.deviceAccessAuthorizationService.isUserAuthorized(Mockito.any(DeviceIdentifier.class)))
                .thenThrow(new DeviceAccessAuthorizationException());
        Mockito.when(this.privilegedCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        Map<String, Object> payload = new HashMap<>();
        Response response = this.deviceAgentService.publishEvents(payload, TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "The response status should be 500");
        Mockito.reset(this.deviceAccessAuthorizationService);
    }

    @Test(description = "Test event publishing when the event stream dao is null.")
    public void testEventPublishWithNullEventAttributesAndNullEventStreamDefDAO() throws DeviceAccessAuthorizationException, RemoteException {
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(this.privilegedCarbonContext);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceAccessAuthorizationService"))
                .toReturn(this.deviceAccessAuthorizationService);
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
        Mockito.reset(eventStreamAdminServiceStub);
    }

    @Test(description ="Test the error scenario of Publishing Events with null event attributes.")
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
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getEventPublisherService")).toReturn
                (eventPublisherService);

        Map<String, Object> payload = new HashMap<>();
        CacheImpl cache = Mockito.mock(CacheImpl.class);

        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDynamicEventCache"))
                .toReturn(cache);

        Response response = this.deviceAgentService.publishEvents(payload, TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
        Assert.assertNotNull(response, "Response should not be null");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
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
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getEventPublisherService")).toReturn
                (eventPublisherService);
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
    }

    @Test(description = "Test event publishing when PublishEvents throws DataPublisherConfigurationException.")
    public void testPublishEventsDataPublisherConfig() throws DeviceAccessAuthorizationException, RemoteException, DataPublisherConfigurationException {
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
    }
}
