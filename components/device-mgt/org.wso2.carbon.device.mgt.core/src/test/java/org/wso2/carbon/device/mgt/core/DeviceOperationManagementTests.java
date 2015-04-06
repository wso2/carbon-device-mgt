/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;

import java.util.ArrayList;
import java.util.List;

public class DeviceOperationManagementTests extends DeviceManagementBaseTest {

    private OperationManager operationManager;
    private static final Log log = LogFactory.getLog(DeviceOperationManagementTests.class);

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception{
        OperationManagementDAOFactory.init(this.getDataSource());
        this.initOperationManager();
        this.setupData();
    }

    private void setupData() throws Exception {
        String deviceSql = "INSERT INTO DM_DEVICE(DESCRIPTION, NAME, DATE_OF_ENROLLMENT, DATE_OF_LAST_UPDATE, " +
                     "OWNERSHIP, STATUS, DEVICE_TYPE_ID, DEVICE_IDENTIFICATION, OWNER, TENANT_ID) " +
                     "VALUES ('Galaxy Tab', 'Samsung', 1425467382, 1425467382, 'BYOD', 'ACTIVE', 1, " +
                     "'4892813d-0b18-4a02-b7b1-61775257400e', 'admin@wso2.com', '-1234');";
        String typeSql = "Insert into DM_DEVICE_TYPE (ID,NAME) VALUES (1, 'android');";
        this.getDataSource().getConnection().createStatement().execute(typeSql);
        this.getDataSource().getConnection().createStatement().execute(deviceSql);
    }

    private void initOperationManager() {
        this.operationManager = new OperationManagerImpl();
    }

    @Test
    public void testAddOperation() throws Exception {
        CommandOperation op = new CommandOperation();
        op.setEnabled(true);
        op.setType(Operation.Type.COMMAND);
        op.setCode("OPCODE1");

        List<DeviceIdentifier> deviceIds = new ArrayList<DeviceIdentifier>();
        DeviceIdentifier deviceId = new DeviceIdentifier();
        deviceId.setId("4892813d-0b18-4a02-b7b1-61775257400e");
        deviceId.setType("android");
        deviceIds.add(deviceId);

        try {
            boolean isAdded = operationManager.addOperation(op, deviceIds);
            Assert.assertTrue(isAdded);
        } catch (OperationManagementException e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    public void testGetOperations() {
        try {
            //TODO:- operationManager.getOperations is not implemented
            DeviceIdentifier deviceId = new DeviceIdentifier();
            deviceId.setId("4892813d-0b18-4a02-b7b1-61775257400e");
            deviceId.setType("android");
            List<? extends Operation> operations = operationManager.getOperations(deviceId);
            Assert.assertNotNull(operations);
            boolean notEmpty = operations.size() > 0;
            Assert.assertTrue(notEmpty);
        } catch (OperationManagementException e) {
            e.printStackTrace();
        }
    }


}
