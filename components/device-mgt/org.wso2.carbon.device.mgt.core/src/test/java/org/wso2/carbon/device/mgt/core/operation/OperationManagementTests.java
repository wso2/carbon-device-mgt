/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/

package org.wso2.carbon.device.mgt.core.operation;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.ActivityStatus;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.TestDeviceManagementService;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ConfigOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.operation.mgt.PolicyOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * This is the testcase which covers the methods from {@link OperationManager}
 */
public class OperationManagementTests extends BaseDeviceManagementTest {

    private static final String DEVICE_TYPE = "OP_TEST_TYPE";
    private static final String DEVICE_ID_PREFIX = "OP-TEST-DEVICE-ID-";
    private static final String COMMAND_OPERATON_CODE = "COMMAND-TEST";
    private static final String POLICY_OPERATION_CODE = "POLICY-TEST";
    private static final String CONFIG_OPERATION_CODE = "CONFIG-TEST";
    private static final String PROFILE_OPERATION_CODE = "PROFILE-TEST";
    private static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    private static final int NO_OF_DEVICES = 5;
    private static final String ADMIN_USER = "admin";
    private static final String NON_ADMIN_USER = "test";
    private static final String INVALID_DEVICE = "ThisIsInvalid";

    private List<DeviceIdentifier> deviceIds = new ArrayList<>();
    private OperationManager operationMgtService;
    private DeviceManagementProviderService deviceMgmtProvider;
    private Activity commandActivity;
    private long commandActivityBeforeUpdatedTimestamp;

    @BeforeClass
    public void init() throws Exception {
        for (int i = 0; i < NO_OF_DEVICES; i++) {
            deviceIds.add(new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE));
        }
        List<Device> devices = TestDataHolder.generateDummyDeviceData(this.deviceIds);
        DeviceManagementProviderService deviceMgtService = DeviceManagementDataHolder.getInstance().
                getDeviceManagementProvider();
        deviceMgtService.registerDeviceType(new TestDeviceManagementService(DEVICE_TYPE,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
        for (Device device : devices) {
            deviceMgtService.enrollDevice(device);
        }
        List<Device> returnedDevices = deviceMgtService.getAllDevices(DEVICE_TYPE);
        for (Device device : returnedDevices) {
            if (!device.getDeviceIdentifier().startsWith(DEVICE_ID_PREFIX)) {
                throw new Exception("Incorrect device with ID - " + device.getDeviceIdentifier() + " returned!");
            }
        }
        this.deviceMgmtProvider = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider();
        NotificationStrategy notificationStrategy = new TestNotificationStrategy();
        this.operationMgtService = new OperationManagerImpl(DEVICE_TYPE, notificationStrategy);
    }

    @Test
    public void addCommandOperation() throws DeviceManagementException, OperationManagementException,
            InvalidDeviceException {
        this.commandActivity = this.operationMgtService.addOperation(
                getOperation(new CommandOperation(), Operation.Type.COMMAND, COMMAND_OPERATON_CODE),
                this.deviceIds);
        validateOperationResponse(this.commandActivity, ActivityStatus.Status.PENDING);
    }

    @Test
    public void addCommandOperationInvalidDeviceIds() throws DeviceManagementException, OperationManagementException,
            InvalidDeviceException {
        startTenantFlowAsNonAdmin();
        try {
            ArrayList<DeviceIdentifier> invalidDevices = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                invalidDevices.add(new DeviceIdentifier(INVALID_DEVICE + i, DEVICE_TYPE));
            }
            invalidDevices.addAll(this.deviceIds);
            Activity activity = this.operationMgtService.addOperation(getOperation(new CommandOperation(),
                    Operation.Type.COMMAND, COMMAND_OPERATON_CODE), invalidDevices);
            Assert.assertEquals(activity.getActivityStatus().size(), invalidDevices.size(),
                    "The operation response for add operation only have - " + activity.getActivityStatus().size());
            for (int i = 0; i < activity.getActivityStatus().size(); i++) {
                ActivityStatus status = activity.getActivityStatus().get(i);
                if (i < 3) {
                    Assert.assertEquals(status.getStatus(), ActivityStatus.Status.INVALID);
                } else {
                    Assert.assertEquals(status.getStatus(), ActivityStatus.Status.UNAUTHORIZED);
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }


    @Test(expectedExceptions = InvalidDeviceException.class)
    public void addEmptyDevicesCommandOperation() throws DeviceManagementException, OperationManagementException,
            InvalidDeviceException {
        this.operationMgtService.addOperation(getOperation(new CommandOperation(), Operation.Type.COMMAND,
                COMMAND_OPERATON_CODE),
                new ArrayList<>());
    }

    @Test(expectedExceptions = InvalidDeviceException.class)
    public void addNonInitializedDevicesCommandOperation() throws DeviceManagementException,
            OperationManagementException,
            InvalidDeviceException {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        deviceIdentifiers.add(deviceIdentifier);
        this.operationMgtService.addOperation(getOperation(new CommandOperation(), Operation.Type.COMMAND,
                COMMAND_OPERATON_CODE),
                deviceIdentifiers);
    }

    @Test
    public void addNonAdminUserDevicesCommandOperation() throws DeviceManagementException, OperationManagementException,
            InvalidDeviceException {
        startTenantFlowAsNonAdmin();
        Activity activity = this.operationMgtService.addOperation(getOperation(new CommandOperation(),
                Operation.Type.COMMAND, COMMAND_OPERATON_CODE),
                deviceIds);
        PrivilegedCarbonContext.endTenantFlow();
        validateOperationResponse(activity, ActivityStatus.Status.UNAUTHORIZED);
    }

    private void startTenantFlowAsNonAdmin() {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NON_ADMIN_USER);
    }

    @Test(dependsOnMethods = "addCommandOperation")
    public void addPolicyOperation() throws DeviceManagementException, OperationManagementException,
            InvalidDeviceException {
        Activity activity = this.operationMgtService.addOperation(getOperation(new PolicyOperation(),
                Operation.Type.POLICY, POLICY_OPERATION_CODE),
                this.deviceIds);
        validateOperationResponse(activity, ActivityStatus.Status.PENDING);
    }

    @Test(dependsOnMethods = "addPolicyOperation")
    public void addConfigOperation() throws DeviceManagementException, OperationManagementException,
            InvalidDeviceException {
        Activity activity = this.operationMgtService.addOperation(getOperation(new ConfigOperation(),
                Operation.Type.CONFIG, CONFIG_OPERATION_CODE),
                this.deviceIds);
        validateOperationResponse(activity, ActivityStatus.Status.PENDING);
    }

    @Test(dependsOnMethods = "addConfigOperation")
    public void addProfileOperation() throws DeviceManagementException, OperationManagementException,
            InvalidDeviceException {
        Activity activity = this.operationMgtService.addOperation(getOperation(new ProfileOperation(),
                Operation.Type.PROFILE, PROFILE_OPERATION_CODE),
                this.deviceIds);
        validateOperationResponse(activity, ActivityStatus.Status.PENDING);
    }

    static Operation getOperation(Operation operation, Operation.Type type, String code) {
        String date = new SimpleDateFormat(DATE_FORMAT_NOW).format(new Date());
        operation.setCreatedTimeStamp(date);
        operation.setType(type);
        operation.setCode(code);
        return operation;
    }

    private void validateOperationResponse(Activity activity, ActivityStatus.Status expectedStatus) {
        Assert.assertEquals(activity.getActivityStatus().size(), NO_OF_DEVICES, "The operation response for add " +
                "operation only have - " + activity.getActivityStatus().size());
        for (ActivityStatus status : activity.getActivityStatus()) {
            Assert.assertEquals(status.getStatus(), expectedStatus);
        }
    }

    @Test(dependsOnMethods = "addProfileOperation")
    public void getOperations() throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        for (DeviceIdentifier deviceIdentifier : deviceIds) {
            List operations = this.operationMgtService.getOperations(deviceIdentifier);
            Assert.assertEquals(operations.size(), 4, "The operations should be 4, but found only " + operations.size());
        }
    }

    @Test(dependsOnMethods = "addProfileOperation", expectedExceptions = OperationManagementException.class)
    public void getOperationsAsNonAdmin() throws DeviceManagementException, OperationManagementException,
            InvalidDeviceException {
        try {
            startTenantFlowAsNonAdmin();
            for (DeviceIdentifier deviceIdentifier : deviceIds) {
                this.operationMgtService.getOperations(deviceIdentifier);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test(dependsOnMethods = "getOperations")
    public void getPendingOperations() throws DeviceManagementException, OperationManagementException,
            InvalidDeviceException {
        for (DeviceIdentifier deviceIdentifier : deviceIds) {
            List operations = this.operationMgtService.getPendingOperations(deviceIdentifier);
            Assert.assertEquals(operations.size(), 4, "The pending operations should be 4, but found only "
                    + operations.size());
        }
    }

    @Test(dependsOnMethods = "getOperations", expectedExceptions = OperationManagementException.class)
    public void getPendingOperationsAsNonAdmin() throws DeviceManagementException, OperationManagementException,
            InvalidDeviceException {
        try {
            startTenantFlowAsNonAdmin();
            for (DeviceIdentifier deviceIdentifier : deviceIds) {
                this.operationMgtService.getPendingOperations(deviceIdentifier);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test(dependsOnMethods = "getPendingOperations")
    public void getPaginatedRequestAsAdmin() throws OperationManagementException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(ADMIN_USER);
        PaginationRequest request = new PaginationRequest(1, 2);
        request.setDeviceType(DEVICE_TYPE);
        request.setOwner(ADMIN_USER);
        for (DeviceIdentifier deviceIdentifier : deviceIds) {
            PaginationResult result = this.operationMgtService.getOperations(deviceIdentifier, request);
            Assert.assertEquals(result.getRecordsFiltered(), 4);
            Assert.assertEquals(result.getData().size(), 2);
            Assert.assertEquals(result.getRecordsTotal(), 4);
        }
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test(dependsOnMethods = "getPendingOperations", expectedExceptions = OperationManagementException.class)
    public void getPaginatedRequestAsNonAdmin() throws OperationManagementException {
        try {
            startTenantFlowAsNonAdmin();
            PaginationRequest request = new PaginationRequest(1, 2);
            request.setDeviceType(DEVICE_TYPE);
            request.setOwner(ADMIN_USER);
            for (DeviceIdentifier deviceIdentifier : deviceIds) {
                try {
                    this.operationMgtService.getOperations(deviceIdentifier, request);
                } catch (OperationManagementException ex) {
                    if (ex.getMessage() == null) {
                        Assert.assertTrue(ex.getMessage().contains("User '" + NON_ADMIN_USER + "' is not authorized"));
                    }
                    throw ex;
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test(dependsOnMethods = "getPaginatedRequestAsAdmin")
    public void updateOperation() throws OperationManagementException {
        //This is required to introduce a delay for the update operation of the device.
        this.commandActivityBeforeUpdatedTimestamp = System.currentTimeMillis();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        DeviceIdentifier deviceIdentifier = this.deviceIds.get(0);
        List operations = this.operationMgtService.getPendingOperations(deviceIdentifier);
        Assert.assertTrue(operations != null && operations.size() == 4);
        Operation operation = (Operation) operations.get(0);
        operation.setStatus(Operation.Status.COMPLETED);
        operation.setOperationResponse("The operation is successfully completed");
        this.operationMgtService.updateOperation(deviceIdentifier, operation);
        List pendingOperations = this.operationMgtService.getPendingOperations(deviceIdentifier);
        Assert.assertEquals(pendingOperations.size(), 3);
    }

    @Test(dependsOnMethods = "updateOperation", expectedExceptions = OperationManagementException.class)
    public void updateOperationAsNonAdmin() throws OperationManagementException {
        //This is required to introduce a delay for the update operation of the device.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        try {
            DeviceIdentifier deviceIdentifier = this.deviceIds.get(0);
            List operations = this.operationMgtService.getPendingOperations(deviceIdentifier);
            Assert.assertTrue(operations != null && operations.size() == 3);
            startTenantFlowAsNonAdmin();
            Operation operation = (Operation) operations.get(0);
            operation.setStatus(Operation.Status.COMPLETED);
            operation.setOperationResponse("The operation is successfully completed, and updated by non admin!");
            this.operationMgtService.updateOperation(deviceIdentifier, operation);
            List pendingOperations = this.operationMgtService.getPendingOperations(deviceIdentifier);
            Assert.assertEquals(pendingOperations.size(), 3);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }

    @Test(dependsOnMethods = "updateOperation")
    public void getNextPendingOperation() throws OperationManagementException {
        DeviceIdentifier deviceIdentifier = this.deviceIds.get(0);
        Operation operation = this.operationMgtService.getNextPendingOperation(deviceIdentifier);
        Assert.assertTrue(operation.getType().equals(Operation.Type.POLICY));
    }

    @Test(dependsOnMethods = "updateOperation", expectedExceptions = OperationManagementException.class)
    public void getNextPendingOperationAsNonAdmin() throws OperationManagementException {
        startTenantFlowAsNonAdmin();
        try {
            DeviceIdentifier deviceIdentifier = this.deviceIds.get(0);
            this.operationMgtService.getNextPendingOperation(deviceIdentifier);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test(dependsOnMethods = "getNextPendingOperation")
    public void getOperationByDeviceAndOperationId() throws OperationManagementException {
        DeviceIdentifier deviceIdentifier = this.deviceIds.get(0);
        Operation operation = this.operationMgtService.getOperationByDeviceAndOperationId(deviceIdentifier,
                getOperationId(this.commandActivity.getActivityId()));
        Assert.assertTrue(operation.getStatus().equals(Operation.Status.COMPLETED));
        Assert.assertTrue(operation.getType().equals(Operation.Type.COMMAND));
    }

    private int getOperationId(String activityId) {
        return Integer.parseInt(activityId.replace(DeviceManagementConstants.OperationAttributes.ACTIVITY, ""));
    }

    @Test(dependsOnMethods = "getNextPendingOperation", expectedExceptions = OperationManagementException.class)
    public void getOperationByDeviceAndOperationIdNonAdmin() throws OperationManagementException {
        startTenantFlowAsNonAdmin();
        try {
            DeviceIdentifier deviceIdentifier = this.deviceIds.get(0);
            String operationId = this.commandActivity.getActivityId().
                    replace(DeviceManagementConstants.OperationAttributes.ACTIVITY, "");
            this.operationMgtService.getOperationByDeviceAndOperationId(deviceIdentifier,
                    Integer.parseInt(operationId));
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test(dependsOnMethods = "getOperationByDeviceAndOperationId")
    public void getOperationsByDeviceAndStatus() throws OperationManagementException, DeviceManagementException {
        DeviceIdentifier deviceIdentifier = this.deviceIds.get(0);
        List operation = this.operationMgtService.getOperationsByDeviceAndStatus(deviceIdentifier,
                Operation.Status.PENDING);
        Assert.assertEquals(operation.size(), 3);
    }

    @Test(dependsOnMethods = "getOperationByDeviceAndOperationId", expectedExceptions = OperationManagementException.class)
    public void getOperationsByDeviceAndStatusByNonAdmin() throws OperationManagementException,
            DeviceManagementException {
        startTenantFlowAsNonAdmin();
        try {
            DeviceIdentifier deviceIdentifier = this.deviceIds.get(0);
            this.operationMgtService.getOperationsByDeviceAndStatus(deviceIdentifier, Operation.Status.PENDING);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test(dependsOnMethods = "getOperationsByDeviceAndStatus")
    public void getOperation() throws OperationManagementException, DeviceManagementException {
        String operationId = this.commandActivity.getActivityId().
                replace(DeviceManagementConstants.OperationAttributes.ACTIVITY, "");
        Operation operation = this.operationMgtService.getOperation(Integer.parseInt(operationId));
        Assert.assertEquals(operation.getType(), Operation.Type.COMMAND);
    }

    @Test(dependsOnMethods = "getOperation")
    public void getOperationActivity() throws OperationManagementException {
        Activity activity = this.operationMgtService.getOperationByActivityId(commandActivity.getActivityId());
        Assert.assertEquals(activity.getType(), Activity.Type.COMMAND);
        Assert.assertEquals(activity.getActivityStatus().size(), this.deviceIds.size());
        Assert.assertEquals(activity.getActivityStatus().get(0).getStatus(), ActivityStatus.Status.COMPLETED);
        for (int i = 1; i < this.deviceIds.size(); i++) {
            Assert.assertEquals(activity.getActivityStatus().get(i).getStatus(), ActivityStatus.Status.PENDING);
        }
    }

    @Test(dependsOnMethods = "getOperationActivity")
    public void getOperationByActivityIdAndDevice() throws OperationManagementException {
        Activity activity = this.operationMgtService.
                getOperationByActivityIdAndDevice(this.commandActivity.getActivityId(), this.deviceIds.get(0));
        Assert.assertEquals(activity.getType(), Activity.Type.COMMAND);
        Assert.assertEquals(activity.getActivityStatus().size(), 1);
        Assert.assertEquals(activity.getActivityStatus().get(0).getStatus(), ActivityStatus.Status.COMPLETED);
    }

    @Test(dependsOnMethods = "getOperationActivity", expectedExceptions = OperationManagementException.class)
    public void getOperationByActivityIdAndDeviceAsNonAdmin() throws OperationManagementException {
        startTenantFlowAsNonAdmin();
        try {
            this.operationMgtService.
                    getOperationByActivityIdAndDevice(this.commandActivity.getActivityId(), this.deviceIds.get(0));
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test(dependsOnMethods = "updateOperation")
    public void getOperationUpdatedAfterWithLimitAndOffset() throws OperationManagementException, ParseException {
        List<Activity> operations = this.operationMgtService.getActivitiesUpdatedAfter
                (this.commandActivityBeforeUpdatedTimestamp / 1000, 10, 0);
        Assert.assertTrue(operations != null && operations.size() == 1,
                "The operations updated after the created should be 1");
        Activity operation = operations.get(0);
        Assert.assertTrue(operation.getActivityStatus() != null && operation.getActivityStatus().size() == 1,
                "The operation should be having the activity status of atleast one device");
        Assert.assertEquals(operation.getActivityStatus().get(0).getDeviceIdentifier().getId(),
                deviceIds.get(0).getId());
        Assert.assertEquals(operation.getActivityStatus().get(0).getDeviceIdentifier().getType(),
                deviceIds.get(0).getType());
    }

    @Test(dependsOnMethods = "getOperationUpdatedAfterWithLimitAndOffset")
    public void getActivityCountUpdatedAfter() throws OperationManagementException, ParseException {
        int activityCount = this.operationMgtService.getActivityCountUpdatedAfter
                (this.commandActivityBeforeUpdatedTimestamp / 1000);
        Assert.assertTrue(activityCount == 1,
                "The activities updated after the created should be 1");
    }

    @Test
    public void getNotificationStrategy() {
        Assert.assertTrue(this.operationMgtService.getNotificationStrategy() != null);
    }

    @Test(dependsOnMethods = {"getOperationByActivityIdAndDevice", "getOperationByActivityIdAndDeviceAsNonAdmin"})
    public void getOperationForInactiveDevice() throws DeviceManagementException, OperationManagementException {
        boolean disEnrolled = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                disenrollDevice(deviceIds.get(0));
        Assert.assertTrue(disEnrolled);
        List operations = this.operationMgtService.getOperations(deviceIds.get(0));
        Assert.assertTrue(operations == null);
    }

    @Test(dependsOnMethods = "getOperationForInactiveDevice", expectedExceptions = OperationManagementException.class)
    public void getPaginatedOperationDeviceForInvalidDevice() throws DeviceManagementException,
            OperationManagementException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(ADMIN_USER);
        try {
            PaginationRequest request = new PaginationRequest(1, 2);
            request.setDeviceType(DEVICE_TYPE);
            request.setOwner(ADMIN_USER);
            PaginationResult result = this.operationMgtService.getOperations
                    (new DeviceIdentifier(INVALID_DEVICE, DEVICE_TYPE), request);
            Assert.assertEquals(result.getRecordsFiltered(), 4);
            Assert.assertEquals(result.getData().size(), 2);
            Assert.assertEquals(result.getRecordsTotal(), 4);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test(dependsOnMethods = "getOperationForInactiveDevice", expectedExceptions = OperationManagementException.class)
    public void getPendingOperationDeviceForInvalidDevice() throws DeviceManagementException,
            OperationManagementException {
        this.operationMgtService.getPendingOperations(new DeviceIdentifier(INVALID_DEVICE, DEVICE_TYPE));
    }

    @Test(dependsOnMethods = "getPendingOperationDeviceForInvalidDevice",
            expectedExceptions = OperationManagementException.class)
    public void getNextPendingOperationDeviceForInvalidDevice() throws DeviceManagementException,
            OperationManagementException {
        this.operationMgtService.getNextPendingOperation(new DeviceIdentifier(INVALID_DEVICE, DEVICE_TYPE));
    }

    @Test(dependsOnMethods = "getNextPendingOperationDeviceForInvalidDevice",
            expectedExceptions = OperationManagementException.class)
    public void getUpdateOperationForInvalidDevice() throws DeviceManagementException, OperationManagementException {
        this.operationMgtService.updateOperation(new DeviceIdentifier(INVALID_DEVICE, DEVICE_TYPE),
                getOperation(new CommandOperation(), Operation.Type.COMMAND, COMMAND_OPERATON_CODE));
    }

    @Test(dependsOnMethods = "getUpdateOperationForInvalidDevice",
            expectedExceptions = OperationManagementException.class)
    public void getOperationByDeviceAndOperationIdInvalidDevice() throws DeviceManagementException,
            OperationManagementException {
        this.operationMgtService.getOperationByDeviceAndOperationId(new DeviceIdentifier(INVALID_DEVICE, DEVICE_TYPE),
                getOperationId(this.commandActivity.getActivityId()));
    }

    @Test(dependsOnMethods = "getOperationByDeviceAndOperationIdInvalidDevice",
            expectedExceptions = OperationManagementException.class)
    public void getOperationsByDeviceAndStatusInvalidDevice() throws DeviceManagementException,
            OperationManagementException {
        this.operationMgtService.getOperationsByDeviceAndStatus(new DeviceIdentifier(INVALID_DEVICE, DEVICE_TYPE),
                Operation.Status.PENDING);
    }

    @Test(dependsOnMethods = "getOperationsByDeviceAndStatusInvalidDevice",
            expectedExceptions = OperationManagementException.class)
    public void getOperationsInvalidOperationId() throws DeviceManagementException,
            OperationManagementException {
        this.operationMgtService.getOperation(123445);
    }

    @Test(dependsOnMethods = "getOperationsInvalidOperationId", expectedExceptions = IllegalArgumentException.class)
    public void getOperationsByActivityIdInvalidActivityId() throws DeviceManagementException,
            OperationManagementException {
        this.operationMgtService.getOperationByActivityId(DeviceManagementConstants.OperationAttributes.ACTIVITY + 0);
    }

    @Test(dependsOnMethods = "getOperationsByActivityIdInvalidActivityId",
            expectedExceptions = IllegalArgumentException.class)
    public void getOperationByActivityIdAndDeviceInvalidActivityId() throws DeviceManagementException,
            OperationManagementException {
        this.operationMgtService.getOperationByActivityIdAndDevice(
                DeviceManagementConstants.OperationAttributes.ACTIVITY + 0,
                new DeviceIdentifier(INVALID_DEVICE, DEVICE_TYPE));
    }

    @Test(dependsOnMethods = "getOperationByActivityIdAndDeviceInvalidActivityId")
    public void getPendingOperationsInactiveEnrollment() throws DeviceManagementException,
            OperationManagementException {
        changeStatus(EnrolmentInfo.Status.INACTIVE);
        List operations = this.operationMgtService.getPendingOperations(this.deviceIds.get(1));
        Assert.assertTrue(operations != null);
        Assert.assertEquals(operations.size(), 4);
        changeStatus(EnrolmentInfo.Status.ACTIVE);
    }

    private void changeStatus(EnrolmentInfo.Status status) throws DeviceManagementException,
            OperationManagementException {
        Device device = this.deviceMgmtProvider.getDevice(this.deviceIds.get(1));
        Assert.assertTrue(device != null);
        Assert.assertEquals(device.getType(), DEVICE_TYPE);
        Assert.assertTrue(device.getEnrolmentInfo() != null);
        device.getEnrolmentInfo().setStatus(status);
        boolean modified = this.deviceMgmtProvider.changeDeviceStatus(this.deviceIds.get(1), status);
        Assert.assertTrue(modified);
        device = this.deviceMgmtProvider.getDevice(this.deviceIds.get(1));
        Assert.assertEquals(device.getEnrolmentInfo().getStatus(), status);
    }

    @Test(dependsOnMethods = "getPendingOperationsInactiveEnrollment")
    public void getNextPendingOperationInactiveEnrollment() throws DeviceManagementException,
            OperationManagementException {
        changeStatus(EnrolmentInfo.Status.INACTIVE);
        Operation operation = this.operationMgtService.getNextPendingOperation(this.deviceIds.get(1));
        Assert.assertTrue(operation != null);
        changeStatus(EnrolmentInfo.Status.ACTIVE);
    }

    @Test(dependsOnMethods = "getNextPendingOperationInactiveEnrollment")
    public void getNextPendingOperationForAllOperations() throws DeviceManagementException,
            OperationManagementException {
        for (int i = 0; i < 4; i++) {
            Operation operation = this.operationMgtService.getNextPendingOperation(this.deviceIds.get(1));
            operation.setStatus(Operation.Status.COMPLETED);
            this.operationMgtService.updateOperation(deviceIds.get(1), operation);
        }
        Assert.assertTrue(this.operationMgtService.getNextPendingOperation(this.deviceIds.get(1)) == null);
    }

    @Test(dependsOnMethods = "getNextPendingOperationForAllOperations")
    public void getOperationByDeviceAndOperationIdForAllOperations() throws DeviceManagementException,
            OperationManagementException {
        for (int i = 1; i <= 4; i++) {
            Operation operation = this.operationMgtService.getOperationByDeviceAndOperationId(this.deviceIds.get(1), i);
            Assert.assertEquals(operation.getStatus(), Operation.Status.COMPLETED);
        }
    }

    @Test(dependsOnMethods = "getOperationByDeviceAndOperationIdForAllOperations")
    public void getOperationForAllOperations() throws DeviceManagementException,
            OperationManagementException {
        for (int i = 1; i <= 4; i++) {
            Operation operation = this.operationMgtService.getOperation(i);
            Assert.assertTrue(operation != null);
        }
    }

    @Test(dependsOnMethods = "getOperationForAllOperations")
    public void addCustomPolicyOperation() throws OperationManagementException, InvalidDeviceException {
        this.addCustomOperation(Operation.Type.POLICY, DeviceManagementConstants.AuthorizationSkippedOperationCodes.
                POLICY_OPERATION_CODE);
    }

    @Test(dependsOnMethods = "getOperationForAllOperations")
    public void addCustomMonitorOperation() throws OperationManagementException, InvalidDeviceException {
        this.addCustomOperation(Operation.Type.COMMAND, DeviceManagementConstants.AuthorizationSkippedOperationCodes.
                MONITOR_OPERATION_CODE);
    }

    @Test(dependsOnMethods = "getOperationForAllOperations")
    public void addCustomPolicyRevokeOperation() throws OperationManagementException, InvalidDeviceException {
        this.addCustomOperation(Operation.Type.POLICY, DeviceManagementConstants.AuthorizationSkippedOperationCodes.
                POLICY_REVOKE_OPERATION_CODE);
    }

    private void addCustomOperation(Operation.Type type, String operationCode) throws OperationManagementException, InvalidDeviceException {
        Operation operation = new Operation();
        operation.setCode(operationCode);
        operation.setType(type);
        Activity activity = this.operationMgtService.addOperation(operation, Collections.singletonList(this.deviceIds.get(2)));
        Assert.assertEquals(activity.getActivityStatus().size(), 1);
        for (ActivityStatus status : activity.getActivityStatus()) {
            Assert.assertEquals(status.getStatus(), ActivityStatus.Status.PENDING);
        }
    }
}
