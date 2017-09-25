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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.core.TestDeviceManagementService;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagementServiceProvider;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.InMemoryEmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OperationManagementTests extends BaseDeviceManagementTest {
    private static final Log log = LogFactory.getLog(OperationManagementTests.class);

    private static final String DEVICE_TYPE = "OP_TEST_TYPE";
    private static final String DEVICE_ID_PREFIX = "OP-TEST-DEVICE-ID-";
    private static final String COMMAND_OPERATON_CODE = "COMMAND-TEST";

    private List<DeviceIdentifier> deviceIds = new ArrayList<>();
    private DeviceManagementProviderService deviceMgtService;
    private OperationManager operationMgtService;

    @BeforeClass
    @Override
    public void init() throws Exception {
        DeviceConfigurationManager.getInstance().initConfig();
        initDataSource();
        log.info("Initializing");
        for (int i = 0; i < 5; i++) {
            deviceIds.add(new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE));
        }
        List<Device> devices = TestDataHolder.generateDummyDeviceData(this.deviceIds);
        this.deviceMgtService = new DeviceManagementProviderServiceImpl();
        DeviceManagementServiceComponent.notifyStartupListeners();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceMgtService);
        DeviceManagementDataHolder.getInstance().setRegistryService(getRegistryService());
        DeviceManagementDataHolder.getInstance().setDeviceAccessAuthorizationService(new DeviceAccessAuthorizationServiceImpl());
        this.deviceMgtService.registerDeviceType(new TestDeviceManagementService(DEVICE_TYPE,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
        for (Device device : devices) {
            this.deviceMgtService.enrollDevice(device);
        }
        List<Device> returnedDevices = this.deviceMgtService.getAllDevices(DEVICE_TYPE);
        for (Device device : returnedDevices) {
            if (!device.getDeviceIdentifier().startsWith(DEVICE_ID_PREFIX)) {
                throw new Exception("Incorrect device with ID - " + device.getDeviceIdentifier() + " returned!");
            }
        }

        this.operationMgtService = new OperationManagerImpl(DEVICE_TYPE);
    }

    private RegistryService getRegistryService() throws RegistryException {
        RealmService realmService = new InMemoryRealmService();
        RegistryDataHolder.getInstance().setRealmService(realmService);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("carbon-home/repository/conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }

    @Test
    public void addCommandOperation() throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        CommandOperation operation = new CommandOperation();
        operation.setType(Operation.Type.PROFILE);
        operation.setCode(COMMAND_OPERATON_CODE);
        this.operationMgtService.addOperation(operation, this.deviceIds);
    }
}
