/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.policy.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceNotFoundException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.policy.mgt.DeviceGroupWrapper;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.operation.mgt.PolicyOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderServiceImpl;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.common.PolicyEvaluationException;
import org.wso2.carbon.policy.mgt.common.PolicyEvaluationPoint;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.enforcement.DelegationTask;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mock.TypeADeviceManagementService;
import org.wso2.carbon.policy.mgt.core.services.SimplePolicyEvaluationTest;
import org.wso2.carbon.policy.mgt.core.task.MonitoringTask;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagementConstants;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PolicyManagerServiceImplTest extends BasePolicyManagementDAOTest {

    private static final Log log = LogFactory.getLog(PolicyManagerServiceImpl.class);

    private static final String DEVICE_TYPE_A = "deviceTypeA";
    private static final String DEVICE1 = "device1";
    private static final String GROUP1 = "group1";
    private static final String POLICY1 = "policy1";
    private static final String POLICY1_FEATURE1_CODE = "DISALLOW_ADJUST_VOLUME";
    private static final String ADMIN_USER = "admin";

    private DeviceManagementProviderService deviceMgtService;
    private GroupManagementProviderService groupMgtService;
    private OperationManager operationManager;
    private PolicyManagerService policyManagerService;

    private Policy policy1;

    @BeforeClass
    public void init() throws Exception {
        super.initSQLScript();

        DeviceConfigurationManager.getInstance().initConfig();
        log.info("Initializing policy tests");

        deviceMgtService = new DeviceManagementProviderServiceImpl();
        groupMgtService = new GroupManagementProviderServiceImpl();

        DeviceManagementServiceComponent.notifyStartupListeners();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceMgtService);
        DeviceManagementDataHolder.getInstance().setRegistryService(getRegistryService());
        DeviceManagementDataHolder.getInstance().setDeviceAccessAuthorizationService(new DeviceAccessAuthorizationServiceImpl());
        DeviceManagementDataHolder.getInstance().setGroupManagementProviderService(groupMgtService);
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(null);

        PolicyEvaluationPoint policyEvaluationPoint = new SimplePolicyEvaluationTest();
        PolicyManagementDataHolder.getInstance().setPolicyEvaluationPoint("Simple", policyEvaluationPoint);
        PolicyManagementDataHolder.getInstance().setDeviceManagementService(deviceMgtService);
    }

    private RegistryService getRegistryService() throws RegistryException {
        RealmService realmService = new InMemoryRealmService();
        RegistryDataHolder.getInstance().setRealmService(realmService);
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("carbon-home/repository/conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }

    private boolean enrollDevice(String deviceName, String deviceType) {
        boolean success = false;
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo(
                ADMIN_USER, EnrolmentInfo.OwnerShip.BYOD, EnrolmentInfo.Status.ACTIVE);
        Device device1 = new Device(deviceName, deviceType, deviceName, deviceName, enrolmentInfo, null, null);
        try {
            success = deviceMgtService.enrollDevice(device1);
        } catch (DeviceManagementException e) {
            String msg = "Failed to enroll a device.";
            log.error(msg, e);
            Assert.fail();
        }
        return success;
    }

    private void createDeviceGroup(String groupName) {
        DeviceGroup deviceGroup = new DeviceGroup(groupName);
        deviceGroup.setDescription(groupName);
        deviceGroup.setOwner(ADMIN_USER);
        try {
            groupMgtService.createGroup(deviceGroup, null, null);
        } catch (GroupAlreadyExistException | GroupManagementException e) {
            String msg = "Failed to create group: " + groupName;
            log.error(msg, e);
            Assert.fail(msg);
        }
    }

    private void addDeviceToGroup(DeviceIdentifier deviceIdentifier, String groupName) {
        List<DeviceIdentifier> groupDevices = new ArrayList<>();
        groupDevices.add(deviceIdentifier);
        try {
            DeviceGroup group = groupMgtService.getGroup(groupName);
            groupMgtService.addDevices(group.getGroupId(), groupDevices);
        } catch (DeviceNotFoundException | GroupManagementException e) {
            String msg = "Failed to add device " + deviceIdentifier.getId() + " to group " + groupName;
            log.error(msg, e);
            Assert.fail(msg);
        }
    }

    @Test
    public void addPolicy() throws DeviceManagementException, GroupManagementException, PolicyManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        policyManagerService = new PolicyManagerServiceImpl();

        deviceMgtService.registerDeviceType(new TypeADeviceManagementService());
        operationManager = new OperationManagerImpl(DEVICE_TYPE_A);
        enrollDevice(DEVICE1, DEVICE_TYPE_A);
        createDeviceGroup(GROUP1);
        DeviceGroup group1 = groupMgtService.getGroup(GROUP1);
        addDeviceToGroup(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A), GROUP1);

        Profile profile = new Profile();
        profile.setTenantId(tenantId);
        profile.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        profile.setDeviceType(DEVICE_TYPE_A);

        List<ProfileFeature> profileFeatures = new ArrayList<ProfileFeature>();
        ProfileFeature profileFeature = new ProfileFeature();
        profileFeature.setContent("{'enable':'true'}");
        profileFeature.setDeviceType(DEVICE_TYPE_A);
        profileFeature.setFeatureCode(POLICY1_FEATURE1_CODE);
        profileFeatures.add(profileFeature);
        profile.setProfileFeaturesList(profileFeatures);
        profile.setProfileName("tp_profile1");
        profile.setUpdatedDate(new Timestamp(System.currentTimeMillis()));

        DeviceGroupWrapper deviceGroupWrapper = new DeviceGroupWrapper();
        deviceGroupWrapper.setId(group1.getGroupId());
        deviceGroupWrapper.setName(GROUP1);
        deviceGroupWrapper.setOwner(ADMIN_USER);
        deviceGroupWrapper.setTenantId(tenantId);
        List<DeviceGroupWrapper> deviceGroupWrappers = new ArrayList<>();
        deviceGroupWrappers.add(deviceGroupWrapper);

        policy1 = new Policy();
        policy1.setPolicyName(POLICY1);
        policy1.setDescription(POLICY1);
        policy1.setProfile(profile);
        policy1.setOwnershipType("BYOD");
        policy1.setActive(false);
        policy1.setRoles(new ArrayList<>());
        policy1.setUsers(new ArrayList<>());
        policy1.setCompliance(PolicyManagementConstants.ENFORCE);
        policy1.setDeviceGroups(deviceGroupWrappers);
        List<Device> devices = new ArrayList<Device>();
        policy1.setDevices(devices);
        policy1.setTenantId(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
        policy1 = policyManagerService.addPolicy(policy1);


        int policyCount = policyManagerService.getPolicyCount();
        Assert.assertEquals(policyCount, 1, "Policy count should be 1");

        List<Policy> allPolicies = policyManagerService.getPolicies(DEVICE_TYPE_A);
        boolean policyFound = false;
        for (Policy policy : allPolicies) {
            String policyName = policy.getPolicyName();
            if (POLICY1.equals(policyName)) {
                policyFound = true;
                break;
            }
        }
        Assert.assertTrue(policyFound, POLICY1 + " was not added.");
    }

    @Test(dependsOnMethods = "addPolicy")
    public void activatePolicy() throws PolicyManagementException {
        policyManagerService.getPAP().activatePolicy(policy1.getId());
        Policy effectivePolicy = policyManagerService.getEffectivePolicy(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A));
        Assert.assertEquals(effectivePolicy.getPolicyName(), POLICY1, POLICY1 + " was not activated for " + DEVICE1);


    }

    @Test(dependsOnMethods = "activatePolicy")
    public void applyPolicy() throws PolicyManagementException, OperationManagementException {
        new DelegationTask().execute();
        Policy appliedPolicy = policyManagerService.getAppliedPolicyToDevice(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A));
        Assert.assertEquals(appliedPolicy.getPolicyName(), POLICY1, POLICY1 + " was not applied on " + DEVICE1);

        List<? extends Operation> operations = operationManager.getPendingOperations(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A));
        boolean policyOperationFound = false;
        for (Operation operation : operations) {
            if (policyOperationFound) {
                break;
            }
            if (PolicyOperation.POLICY_OPERATION_CODE.equals(operation.getCode())) {
                Object o = operation.getPayLoad();
                if (o instanceof ArrayList) {
                    ArrayList profileOps = (ArrayList) o;
                    for (Object op : profileOps) {
                        if (op instanceof ProfileOperation) {
                            ProfileOperation profileOperation = (ProfileOperation) op;
                            if (POLICY1_FEATURE1_CODE.equals(profileOperation.getCode())) {
                                policyOperationFound = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        Assert.assertTrue(policyOperationFound, "Policy operation for " + POLICY1 + " was not added to " + DEVICE1);
    }

    @Test(dependsOnMethods = "applyPolicy")
    public void checkCompliance() throws PolicyComplianceException {
        new MonitoringTask().execute();

        List<ComplianceFeature> complianceFeatures = new ArrayList<>();
        ComplianceFeature complianceFeature = new ComplianceFeature();
        ProfileFeature profileFeature = new ProfileFeature();
        profileFeature.setContent("{'enable':'false'}");
        profileFeature.setDeviceType(DEVICE_TYPE_A);
        profileFeature.setFeatureCode(POLICY1_FEATURE1_CODE);
        complianceFeature.setFeature(profileFeature);
        complianceFeature.setFeatureCode(profileFeature.getFeatureCode());
        complianceFeature.setMessage("Test message");
        complianceFeature.setCompliance(true);
        complianceFeatures.add(complianceFeature);
        policyManagerService.checkCompliance(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A), complianceFeatures);
        boolean deviceCompliance = policyManagerService.isCompliant(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A));

        Assert.assertTrue(deviceCompliance, "Policy was not compliant");
    }

    @Test(dependsOnMethods = "checkCompliance")
    public void checkNonCompliance() throws PolicyComplianceException {
        new MonitoringTask().execute();

        List<ComplianceFeature> complianceFeatures = new ArrayList<>();
        ComplianceFeature complianceFeature = new ComplianceFeature();
        ProfileFeature profileFeature = new ProfileFeature();
        profileFeature.setContent("{'enable':'false'}");
        profileFeature.setDeviceType(DEVICE_TYPE_A);
        profileFeature.setFeatureCode(POLICY1_FEATURE1_CODE);
        complianceFeature.setFeature(profileFeature);
        complianceFeature.setFeatureCode(profileFeature.getFeatureCode());
        complianceFeature.setMessage("Test message");
        complianceFeature.setCompliance(false);
        complianceFeatures.add(complianceFeature);
        policyManagerService.checkCompliance(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A), complianceFeatures);
        boolean deviceCompliance = policyManagerService.isCompliant(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A));

        Assert.assertFalse(deviceCompliance, "Policy was compliant even though the response was not compliant");
    }

    @Test(dependsOnMethods = "inactivatePolicy")
    public void updatePolicy() throws PolicyManagementException {
        Policy policy1Temp = policyManagerService.getPAP().getPolicy(policy1.getId());
        policy1Temp.setDescription("Updated policy1");
        policy1Temp = policyManagerService.updatePolicy(policy1Temp);

        Policy policy1Updated = policyManagerService.getPAP().getPolicy(policy1.getId());
        Assert.assertEquals(policy1Updated.getDescription(), policy1Temp.getDescription(), "Policy was not updated successfully.");
    }

    @Test(dependsOnMethods = "checkNonCompliance")
    public void inactivatePolicy() throws PolicyManagementException {
        policyManagerService.getPAP().inactivatePolicy(policy1.getId());
        new DelegationTask().execute();
        Policy effectivePolicy = policyManagerService.getEffectivePolicy(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A));
        Assert.assertNull(effectivePolicy, POLICY1 + " (after inactivation) is still applied for " + DEVICE1);
    }

    @Test(dependsOnMethods = "updatePolicy")
    public void deletePolicyById() throws PolicyManagementException {
        policyManagerService.deletePolicy(policy1.getId());
        Policy tempPolicy = policyManagerService.getPAP().getPolicy(policy1.getId());
        Assert.assertNull(tempPolicy, "Policy was not deleted successfully");
    }

    @Test(dependsOnMethods = "updatePolicy")
    public void deletePolicyByPolicy() throws PolicyManagementException {
        policyManagerService.deletePolicy(policy1);
        Policy tempPolicy = policyManagerService.getPAP().getPolicy(policy1.getId());
        Assert.assertNull(tempPolicy, "Policy was not deleted successfully");
    }

    @Test(dependsOnMethods = "applyPolicy")
    public void getEffectiveFeatures( ) throws Exception {
        List<ProfileFeature> effectiveFeatures = policyManagerService.
                getEffectiveFeatures(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A));
        Assert.assertNotNull(effectiveFeatures);
        Assert.assertEquals(POLICY1_FEATURE1_CODE,effectiveFeatures.get(0).getFeatureCode());
        try{
              policyManagerService.getEffectiveFeatures(null);
        }catch(FeatureManagementException e){
           Assert.assertTrue(e.getCause()instanceof PolicyEvaluationException);
        }

    }


}