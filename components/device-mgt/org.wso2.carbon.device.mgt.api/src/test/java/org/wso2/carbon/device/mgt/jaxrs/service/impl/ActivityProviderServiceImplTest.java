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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.jaxrs.common.ActivityIdList;
import org.wso2.carbon.device.mgt.jaxrs.service.api.ActivityInfoProviderService;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceManagementService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This is a test class for {@link ActivityProviderServiceImpl}.
 */
@PowerMockIgnore("javax.ws.rs.*")
@SuppressStaticInitializationFor({ "org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils",
        "org.wso2.carbon.context.CarbonContext", "org.wso2.carbon.context.PrivilegedCarbonContext" })
@PrepareForTest({ DeviceMgtAPIUtils.class, PolicyManagerUtil.class, PrivilegedCarbonContext.class })
public class ActivityProviderServiceImplTest {

    private static final Log log = LogFactory.getLog(ActivityProviderServiceImplTest.class);
    private static final String TEST_ACTIVITY_ID = "ACTIVITY_1";
    private static final String IF_MODIFIED_SINCE = "01Aug2018";
    private static final String DEVICE_TYPE = "android";
    private static final String DEVICE_ID = "1234567";
    private static final String OPERATION_CODE = "111222";
    private static final int OFFSET = 0;
    private static final int LIMIT = 5;
    private static final String TEST_ACTIVITY_ID_LIST = "ACTIVITY_1,ACTIVITY_2";
    private static final List<String> idList = new ArrayList();
    private static final List<Activity> activities = new ArrayList<>();
    private static final ActivityIdList activityList = new ActivityIdList(TEST_ACTIVITY_ID_LIST);
    private static final ActivityIdList activityListEmpty = new ActivityIdList("");

    private List<String> idList1;
    private Activity activity;
    private List<Activity> activities1;

    private DeviceManagementService deviceManagementService;
    private DeviceAccessAuthorizationService deviceAccessAuthorizationService;
    private DeviceManagementProviderService deviceManagementProviderService;
    private ActivityInfoProviderService activityInfoProviderService;
    private DeviceIdentifier deviceIdentifier;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeClass
    public void init() {
        log.info("Initializing ActivityProviderServiceImplTest tests");
        initMocks(this);
        this.deviceManagementProviderService = Mockito.mock(DeviceManagementProviderServiceImpl.class,
                Mockito.RETURNS_MOCKS);
        this.deviceIdentifier = new DeviceIdentifier();
        this.deviceManagementService = new DeviceManagementServiceImpl();
        this.activityInfoProviderService = new ActivityProviderServiceImpl();
        this.deviceAccessAuthorizationService = Mockito.mock(DeviceAccessAuthorizationServiceImpl.class);
        idList.add("ACTIVITY_1");
        idList.add("ACTIVITY_2");
        this.activity = new Activity();
        Activity activity1 = new Activity();
        Activity activity2 = new Activity();
        activity1.setActivityId("ACTIVITY_1");
        activity2.setActivityId("ACTIVITY_2");
        activities.add(activity1);
        activities.add(activity2);
    }

    @Test(description =
            "This method tests  getting details of an activity with an admin user with an existing"
                    + " activity Id")
    public void testGetActivitiesWithValidAdminUserWithValidId() throws OperationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isAdmin")).toReturn(true);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.getOperationByActivityId(TEST_ACTIVITY_ID))
                .thenReturn(activity);
        Response response = this.activityInfoProviderService.getActivity(TEST_ACTIVITY_ID, IF_MODIFIED_SINCE);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "This method tests getting details of an activity with an invalid admin user")
    public void testGetActivitiesWithInvalidAdminUserWithValidId() throws OperationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isAdmin")).toReturn(false);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.getOperationByActivityId(TEST_ACTIVITY_ID))
                .thenReturn(activity);
        Response response = this.activityInfoProviderService.getActivity(TEST_ACTIVITY_ID, IF_MODIFIED_SINCE);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "This method tests getting details of an activity which does not exists")
    public void testGetActivitiesWithNonExistingActivityID() throws OperationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isAdmin")).toReturn(true);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.getOperationByActivityId(TEST_ACTIVITY_ID))
                .thenReturn(null);
        Response response = this.activityInfoProviderService.getActivity(TEST_ACTIVITY_ID, IF_MODIFIED_SINCE);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "This method tests the getActivity method under negative conditions.")
    public void testGetActivitiesWithOperationManagementException() throws OperationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isAdmin")).toReturn(true);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.getOperationByActivityId(Mockito.any())).thenThrow(
                new OperationManagementException());
        Response response = this.activityInfoProviderService.getActivity(TEST_ACTIVITY_ID, IF_MODIFIED_SINCE);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "This method tests getting details of list of given activity IDs")
    public void testGetActivitiesWithActivityIdList() throws OperationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isAdmin")).toReturn(true);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.getOperationByActivityIds(idList)).thenReturn(
                activities);
        Response response = this.activityInfoProviderService.getActivities(activityList);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "This method tests trying to get details activity IDs when call with empty list")
    public void testGetActivitiesWithEmptyActivityIdList() throws OperationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isAdmin")).toReturn(true);
        Response response = this.activityInfoProviderService.getActivities(activityListEmpty);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "This method tests trying to get details of a list activity IDs which does not exists")
    public void testGetActivitiesWithNonExistingActivityIdList() throws OperationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isAdmin")).toReturn(true);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService.getOperationByActivityIds(idList1)).thenReturn(
                activities1);
        Response response = this.activityInfoProviderService.getActivities(activityList);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "This method tests  getting details of an activity for a given device")
    public void testGetActivitiesByDevice() throws OperationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(this.deviceManagementProviderService
                .getOperationByActivityIdAndDevice(TEST_ACTIVITY_ID, deviceIdentifier)).thenReturn(activity);
        Response response = this.activityInfoProviderService.getActivityByDevice(TEST_ACTIVITY_ID,
                DEVICE_TYPE, DEVICE_ID, IF_MODIFIED_SINCE);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "This method tests getting details of an activity for a given device")
    public void testGetActivities() throws OperationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isAdmin")).toReturn(true);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(
                this.deviceManagementProviderService.getFilteredActivities(OPERATION_CODE, OFFSET, LIMIT))
                .thenReturn(activities);
        Response response = this.activityInfoProviderService.getActivities(OPERATION_CODE, OFFSET, LIMIT);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

    @Test(description = "This method tests getting details of an activity for a given device")
    public void testGetActivitiesForInvalidUser() throws OperationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "isAdmin")).toReturn(false);
        PowerMockito.stub(PowerMockito.method(RequestValidationUtil.class, "validateActivityId"));
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(
                this.deviceManagementProviderService.getFilteredActivities(OPERATION_CODE, OFFSET, LIMIT))
                .thenReturn(activities);
        Response response = this.activityInfoProviderService.getActivities(OPERATION_CODE, OFFSET, LIMIT);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
        Mockito.reset(this.deviceManagementProviderService);
    }

}
