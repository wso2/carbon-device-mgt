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
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceAgentService;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.DeviceTypeManagementAdminService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.DeviceMgtAPITestHelper;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.ws.rs.core.Response;

import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class holds the unit tests for the class {@link DeviceAgentServiceImpl}
 */
@PowerMockIgnore("javax.ws.rs.*")
@SuppressStaticInitializationFor({"org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils"})
@PrepareForTest({DeviceMgtAPIUtils.class, DeviceManagementProviderService.class})
public class DeviceAgentServiceTest {

    private static final Log log = LogFactory.getLog(DeviceTypeManagementAdminService.class);
    private DeviceManagementProviderService deviceManagementProviderService;
    private DeviceAgentService deviceAgentService;
    private static final String TEST_DEVICE_TYPE = "TEST-DEVICE-TYPE";
    private static final String TEST_DEVICE_IDENTIFIER = "11222334455";
    private static final String AUTHENTICATED_USER = "admin";
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
        demoDevice = DeviceMgtAPITestHelper.generateDummyDevice(TEST_DEVICE_TYPE, TEST_DEVICE_IDENTIFIER);
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

}
