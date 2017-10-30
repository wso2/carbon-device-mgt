/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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


package org.wso2.carbon.policy.mgt.core;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mgt.MonitoringManager;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.policy.mgt.core.mgt.impl.MonitoringManagerImpl;
import org.wso2.carbon.policy.mgt.core.mgt.impl.PolicyManagerImpl;
import org.wso2.carbon.policy.mgt.core.services.PolicyMonitoringManagerTest;

import java.util.List;

public class MonitoringTestCase extends BasePolicyManagementDAOTest {

    private static final Log log = LogFactory.getLog(MonitoringTestCase.class);

    private static final String ANDROID = "android";

    private DeviceIdentifier identifier = new DeviceIdentifier();

    @BeforeClass
    @Override
    public void init() throws Exception {

    }

    @Test
    public void testMonitorDao() {

        DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
        PolicyManagerService policyManagerService = new PolicyManagerServiceImpl();

        List<Policy> policies = null;
        List<Device> devices = null;
        try {
            policies = policyManagerService.getPolicies(ANDROID);
            devices = service.getAllDevices(ANDROID);
        } catch (PolicyManagementException e) {
            log.error("Error occurred while retrieving the list of policies defined against the device type '" +
                    ANDROID + "'", e);
            Assert.fail();
        } catch (DeviceManagementException e) {
            log.error("Error occurred while retrieving the list of devices pertaining to the type '" +
                    ANDROID + "'", e);
            Assert.fail();
        }

        for (Policy policy : policies) {
            log.debug("Policy Name : " + policy.getPolicyName());
        }

        for (Device device : devices) {
            log.debug("Device Name : " + device.getDeviceIdentifier());
        }

        identifier.setType(ANDROID);
        identifier.setId(devices.get(0).getDeviceIdentifier());

//        PolicyAdministratorPoint administratorPoint = new PolicyAdministratorPointImpl();
//
//        administratorPoint.setPolicyUsed(identifier, policies.get(0));

    }

    @Test(dependsOnMethods = ("testMonitorDao"))
    public void getDeviceAppliedPolicy() throws PolicyManagementException {

        PolicyManager manager = new PolicyManagerImpl();
        Policy policy = null;

        policy = manager.getAppliedPolicyToDevice(identifier);


        if (policy != null) {

            log.debug(policy.getId());
            log.debug(policy.getPolicyName());
            log.debug(policy.getCompliance());
        } else {
            log.debug("Applied policy was a null object.");
        }
    }


    @Test(dependsOnMethods = ("getDeviceAppliedPolicy"))
    public void addComplianceOperation() throws PolicyManagementException, DeviceManagementException,
            PolicyComplianceException {

        log.debug("Compliance operations adding started.");

        PolicyManager manager = new PolicyManagerImpl();
        Policy policy = null;

        policy = manager.getAppliedPolicyToDevice(identifier);

        OperationManager operationManager = new OperationManagerImpl();

        DeviceManagementDataHolder.getInstance().setOperationManager(operationManager);

        if (policy != null) {
            log.debug(policy.getId());
            log.debug(policy.getPolicyName());
            log.debug(policy.getCompliance());
        }

        MonitoringManager monitoringManager = new MonitoringManagerImpl();

        DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
        List<Device> devices = service.getAllDevices(ANDROID);

        // monitoringManager.addMonitoringOperation(devices);

        log.debug("Compliance operations adding done.");

    }


    @Test(dependsOnMethods = ("addComplianceOperation"))
    public void checkComplianceFromMonitoringService() throws PolicyManagementException, DeviceManagementException,
            PolicyComplianceException {


        PolicyMonitoringManagerTest monitoringServiceTest = new PolicyMonitoringManagerTest();
        TestDeviceManagementProviderService deviceManagementProviderService = new TestDeviceManagementProviderService();
        deviceManagementProviderService.setPolicyMonitoringManager(monitoringServiceTest);
        PolicyManagementDataHolder.getInstance().setDeviceManagementService(deviceManagementProviderService);

        DeviceManagementProviderService adminService = new DeviceManagementProviderServiceImpl();

        // PolicyManager policyManagerService = new PolicyManagerImpl();

        List<Device> devices = adminService.getAllDevices();

        for (Device device : devices) {
            log.debug(device.getDeviceIdentifier());
            log.debug(device.getType());
            log.debug(device.getName());
        }

        PolicyManager manager = new PolicyManagerImpl();
        Policy policy = null;

        policy = manager.getAppliedPolicyToDevice(identifier);

        if (policy != null) {
            Object ob = new Object();

            monitoringServiceTest.checkPolicyCompliance(identifier, policy, ob);
        }
    }


    @Test(dependsOnMethods = ("checkComplianceFromMonitoringService"))
    public void checkCompliance() throws DeviceManagementException, PolicyComplianceException,
            PolicyManagementException {

        PolicyMonitoringManagerTest monitoringServiceTest = new PolicyMonitoringManagerTest();
        TestDeviceManagementProviderService deviceManagementProviderService = new TestDeviceManagementProviderService();
        deviceManagementProviderService.setPolicyMonitoringManager(monitoringServiceTest);
        PolicyManagementDataHolder.getInstance().setDeviceManagementService(deviceManagementProviderService);

        DeviceManagementProviderService adminService = new DeviceManagementProviderServiceImpl();

        List<Device> devices = adminService.getAllDevices();

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(devices.get(0).getDeviceIdentifier());
        deviceIdentifier.setType(devices.get(0).getType());

        Object ob = new Object();

        MonitoringManager monitoringManager = new MonitoringManagerImpl();

        log.debug(identifier.getId());
        log.debug(identifier.getType());


        monitoringManager.checkPolicyCompliance(identifier, ob);


    }

}
