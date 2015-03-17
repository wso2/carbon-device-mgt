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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.core.dto.Device;
import org.wso2.carbon.device.mgt.core.operation.mgt.*;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;

import java.util.ArrayList;
import java.util.List;

public class DeviceOperationManagementTests extends DeviceManagementBaseTest {

    private OperationManager operationManager;

    @BeforeClass(alwaysRun = true)
    public void init() {
        super.init();
        this.initOperationManager();
        OperationManagementDAOFactory.init(this.getDataSource());
    }

    public void initOperationManager() {
        this.operationManager = new OperationManagerImpl();
    }

    @Test
    public void testAddOperation() throws Exception {

        CommandOperation op = new CommandOperation();
        op.setEnabled(true);
        op.setType(Operation.Type.COMMAND);

        List<DeviceIdentifier> deviceIds = new ArrayList<DeviceIdentifier>();
        DeviceIdentifier deviceId = new DeviceIdentifier();
        deviceId.setId("Test");
        deviceId.setType("Android");
        deviceIds.add(deviceId);

        try {
            operationManager.addOperation(op, deviceIds);
        } catch (OperationManagementException e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    public void testGetOperations() {
        try {
            operationManager.getOperations(null);
        } catch (OperationManagementException e) {
            e.printStackTrace();
        }
    }


}
