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

import org.powermock.api.mockito.PowerMockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.TestDeviceManagementService;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the testcase which covers the methods from {@link OperationManager}
 */
public class OperationManagementNoDBSchemaTests extends BaseDeviceManagementTest {

    private static final String DEVICE_TYPE = "NEG_OP_TEST_TYPE";
    private static final String DEVICE_ID_PREFIX = "NEG_OP-TEST-DEVICE-ID-";
    private static final String COMMAND_OPERATON_CODE = "COMMAND-TEST";
    private static final int NO_OF_DEVICES = 5;
    private static final String ADMIN_USER = "admin";

    private List<DeviceIdentifier> deviceIds = new ArrayList<>();
    private OperationManager operationMgtService;

    @BeforeClass
    public void init() throws Exception {
        DataSource datasource = this.getDataSource(this.
                readDataSourceConfig(getDatasourceLocation() + "-no-table" + DATASOURCE_EXT));
        OperationManagementDAOFactory.init(datasource);
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
        DeviceManagementService deviceManagementService
                = new TestDeviceManagementService(DEVICE_TYPE, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        this.operationMgtService = PowerMockito.spy(new OperationManagerImpl(DEVICE_TYPE, deviceManagementService));
        PowerMockito.when(this.operationMgtService, "getNotificationStrategy")
                .thenReturn(new TestNotificationStrategy());
    }

    @Test(description = "add operation", expectedExceptions = OperationManagementException.class)
    public void addCommandOperation() throws DeviceManagementException, OperationManagementException,
            InvalidDeviceException {
        this.operationMgtService.addOperation(
                OperationManagementTests.getOperation(new CommandOperation(),
                        Operation.Type.COMMAND, COMMAND_OPERATON_CODE),
                this.deviceIds);
    }

    @Test(description = "Get operations", expectedExceptions = OperationManagementException.class)
    public void getOperations() throws DeviceManagementException, OperationManagementException,
            InvalidDeviceException {
        for (DeviceIdentifier deviceIdentifier : deviceIds) {
            this.operationMgtService.getOperations(deviceIdentifier);
        }
    }


    @Test(description = "Get Pending operations", expectedExceptions = OperationManagementException.class)
    public void getPendingOperations() throws DeviceManagementException, OperationManagementException,
            InvalidDeviceException {
        for (DeviceIdentifier deviceIdentifier : deviceIds) {
            this.operationMgtService.getPendingOperations(deviceIdentifier);
        }
    }

    @Test(description = "Get paginated request", expectedExceptions = OperationManagementException.class)
    public void getPaginatedRequestAsAdmin() throws OperationManagementException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(ADMIN_USER);
        PaginationRequest request = new PaginationRequest(1, 2);
        request.setDeviceType(DEVICE_TYPE);
        request.setOwner(ADMIN_USER);
        for (DeviceIdentifier deviceIdentifier : deviceIds) {
            this.operationMgtService.getOperations(deviceIdentifier, request);
        }
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test(description = "Update operation", expectedExceptions = OperationManagementException.class)
    public void updateOperation() throws OperationManagementException {
        DeviceIdentifier deviceIdentifier = this.deviceIds.get(0);
        Operation operation = OperationManagementTests.getOperation(new CommandOperation(), Operation.Type.COMMAND,
                COMMAND_OPERATON_CODE);
        operation.setStatus(Operation.Status.COMPLETED);
        operation.setOperationResponse("The operation is successfully completed");
        this.operationMgtService.updateOperation(deviceIdentifier, operation);
    }

    @Test(description = "Get next pending operation", expectedExceptions = OperationManagementException.class)
    public void getNextPendingOperation() throws OperationManagementException {
        DeviceIdentifier deviceIdentifier = this.deviceIds.get(0);
        this.operationMgtService.getNextPendingOperation(deviceIdentifier);
    }


    @Test(description = "get operation by device and operation id",
            expectedExceptions = OperationManagementException.class)
    public void getOperationByDeviceAndOperationId() throws OperationManagementException {
        DeviceIdentifier deviceIdentifier = this.deviceIds.get(0);
        this.operationMgtService.getOperationByDeviceAndOperationId(deviceIdentifier, 1);
    }

    @Test(description = "Get operation by device and status",
            expectedExceptions = OperationManagementException.class)
    public void getOperationsByDeviceAndStatus() throws OperationManagementException, DeviceManagementException {
        DeviceIdentifier deviceIdentifier = this.deviceIds.get(0);
        this.operationMgtService.getOperationsByDeviceAndStatus(deviceIdentifier,
                Operation.Status.PENDING);
    }

    @Test(description = "Get operation by operation id", expectedExceptions = OperationManagementException.class)
    public void getOperation() throws OperationManagementException, DeviceManagementException {
        this.operationMgtService.getOperation(1);
    }

    @Test(description = "Get operation activity", expectedExceptions = OperationManagementException.class)
    public void getOperationActivity() throws OperationManagementException {
        this.operationMgtService.getOperationByActivityId
                (DeviceManagementConstants.OperationAttributes.ACTIVITY + "1");
    }

    @Test(description = "Get operation by activity id and device",
            expectedExceptions = OperationManagementException.class)
    public void getOperationByActivityIdAndDevice() throws OperationManagementException {
        this.operationMgtService.getOperationByActivityIdAndDevice(
                DeviceManagementConstants.OperationAttributes.ACTIVITY + "1", this.deviceIds.get(0));
    }

    @Test(description = "Get activities updated after some time",
            expectedExceptions = OperationManagementException.class)
    public void getOperationUpdatedAfterWithLimitAndOffset() throws OperationManagementException {
        this.operationMgtService.getActivitiesUpdatedAfter(System.currentTimeMillis() / 1000, 10, 0);
    }

    @Test(description = "Get activity count updated after",
            expectedExceptions = OperationManagementException.class)
    public void getActivityCountUpdatedAfter() throws OperationManagementException {
        this.operationMgtService.getActivityCountUpdatedAfter(System.currentTimeMillis() / 1000);
    }

    @AfterClass
    public void resetDatabase() throws Exception {
        OperationManagementDAOFactory.init(this.getDataSource(this.
                readDataSourceConfig(getDatasourceLocation() + DATASOURCE_EXT)));
    }
}
