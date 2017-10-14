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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.policy.mgt.DeviceGroupWrapper;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.operation.mgt.PolicyOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.common.PolicyEvaluationException;
import org.wso2.carbon.policy.mgt.common.PolicyEvaluationPoint;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.enforcement.DelegationTask;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mock.TypeXDeviceManagementService;
import org.wso2.carbon.policy.mgt.core.task.MonitoringTask;
import org.wso2.carbon.policy.mgt.core.task.TaskScheduleService;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagementConstants;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class  PolicyManagerServiceImplTest extends BasePolicyManagementDAOTest {

    private static final Log log = LogFactory.getLog(PolicyManagerServiceImpl.class);

    private static final String DEVICE_TYPE_A = "deviceTypeA";
    private static final String DEVICE1 = "device1";
    private static final String GROUP1 = "group1";
    private static final String POLICY1 = "policy1";
    private static final String POLICY1_FEATURE1_CODE = "DISALLOW_ADJUST_VOLUME";
    private static final String POLICY1_CAM_FEATURE1_CODE = "DISALLOW_OPEN_CAM";
    private static final String ADMIN_USER = "admin";
    public static final String DEVICE_WITHOUT_POLICY = "device2";
    public static final String DEVICE_TYPE_B = "deviceTypeB";

    private OperationManager operationManager;
    private PolicyManagerService policyManagerService;
    private Profile profile;

    private Policy policy1;

    @BeforeClass
    public void init() throws Exception {
        log.info("Initializing policy tests");
        super.initializeServices();
    }

    @Test
    public void addPolicy() throws DeviceManagementException, GroupManagementException, PolicyManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        policyManagerService = new PolicyManagerServiceImpl();

        deviceMgtService.registerDeviceType(new TypeXDeviceManagementService(DEVICE_TYPE_A));
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
    public void activatePolicy() throws Exception {
        policyManagerService.getPAP().activatePolicy(policy1.getId());
        Policy effectivePolicy = policyManagerService.getEffectivePolicy(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A));
        Assert.assertEquals(effectivePolicy.getPolicyName(), POLICY1, POLICY1 + " was not activated for " + DEVICE1);
    }

    @Test(description = "Get active policy but there is no EvaluationPoint define for device yet should be return PolicyManagement exception" +
            " caused by PolicyEvaluationException",dependsOnMethods = "addPolicy",expectedExceptions = PolicyManagementException.class)
    public void getActivePolicyForDeviceWithNoEvaluationEPDefine() throws Exception {
        PolicyEvaluationPoint policyEvaluationPoint = null;
        try{
            policyEvaluationPoint = PolicyManagementDataHolder.getInstance().getPolicyEvaluationPoint();
            PolicyManagementDataHolder.getInstance().setPolicyEvaluationPoint("Simple", null);
            policyManagerService.getEffectivePolicy(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A));
        }catch (Exception e){
            Assert.assertTrue(e.getCause() instanceof PolicyEvaluationException);
            throw e;
        }finally {
            PolicyManagementDataHolder.getInstance().setPolicyEvaluationPoint("Simple", policyEvaluationPoint);
        }
    }


    @Test(description = "Get active policy but no any policy applied for device yet should be return PolicyManagement exception" +
            " caused by PolicyEvaluationException",dependsOnMethods = "addPolicy",expectedExceptions = PolicyManagementException.class)
    public void getActivePolicyForDeviceWithNoAppliedPolicy() throws Exception {
        try{
            policyManagerService.getEffectivePolicy(new DeviceIdentifier(DEVICE_WITHOUT_POLICY, DEVICE_TYPE_B));
        }catch (Exception e){
            Assert.assertTrue(e.getCause() instanceof PolicyEvaluationException);
            throw e;
        }
    }

    @Test(description = "Get active policy if enrollment status removed should be return PolicyManagement exception" +
            " caused by InvalidDeviceException",dependsOnMethods = "addPolicy",expectedExceptions = PolicyManagementException.class)
    public void getActivatePolicyForEnrolmentStatusAsRemoved() throws Exception {
        EnrolmentInfo.Status status =null;
        try{
            Device device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                    getDevice(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A), false);
            status = device.getEnrolmentInfo().getStatus();
            device.getEnrolmentInfo().setStatus(EnrolmentInfo.Status.REMOVED);
            policyManagerService.getEffectivePolicy(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A));
        }catch (Exception e){
            Assert.assertTrue(e.getCause() instanceof InvalidDeviceException);
            throw e;
        }finally {
            if(status != null){
                DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                        getDevice(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A), false).getEnrolmentInfo().setStatus(status);
            }
        }
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
        List<ComplianceFeature> complianceFeatureList = policyManagerService.
                checkPolicyCompliance(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A), complianceFeatures);
        Assert.assertNotNull(complianceFeature);
        Assert.assertEquals(POLICY1_FEATURE1_CODE,complianceFeatureList.get(0).getFeatureCode());
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
    public void getEffectiveFeatures() throws Exception {
        List<ProfileFeature> effectiveFeatures = policyManagerService.
                getEffectiveFeatures(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A));
        Assert.assertNotNull(effectiveFeatures);
        Assert.assertEquals(POLICY1_FEATURE1_CODE, effectiveFeatures.get(0).getFeatureCode());
    }

    @Test(description = "Get active policy but there is no EvaluationPoint define for device yet should be return FeatureManagement exception" +
            " caused by PolicyEvaluationException",dependsOnMethods = "addPolicy",expectedExceptions = FeatureManagementException.class)
    public void getEffectiveFeaturesForDeviceWithNoEvaluationEPDefine() throws Exception {
        PolicyEvaluationPoint policyEvaluationPoint = null;
        try{
            policyEvaluationPoint = PolicyManagementDataHolder.getInstance().getPolicyEvaluationPoint();
            PolicyManagementDataHolder.getInstance().setPolicyEvaluationPoint("Simple", null);
            policyManagerService.getEffectiveFeatures(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A));
        } finally {
            PolicyManagementDataHolder.getInstance().setPolicyEvaluationPoint("Simple", policyEvaluationPoint);
        }
    }


    @Test(description = "Get effective features but no any policy applied for device yet should be return FeatureManagement exception" +
            " caused by PolicyEvaluationException",dependsOnMethods = "addPolicy",expectedExceptions = FeatureManagementException.class)
    public void getEffectiveFeaturesForDeviceWithNoAppliedPolicy() throws Exception {
        try{
            policyManagerService.getEffectiveFeatures(new DeviceIdentifier(DEVICE_WITHOUT_POLICY, DEVICE_TYPE_B));
        }catch (Exception e){
            Assert.assertTrue(e.getCause() instanceof PolicyEvaluationException);
            throw e;
        }
    }

    @Test(dependsOnMethods = "applyPolicy")
    public void getDeviceCompliance() throws Exception{
        NonComplianceData deviceCompliance = policyManagerService.
                getDeviceCompliance(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A));
        Assert.assertNotNull(deviceCompliance);
    }

    @Test(dependsOnMethods = "applyPolicy")
    public void getTaskScheduleService() throws Exception{
        TaskScheduleService taskScheduleService = policyManagerService.getTaskScheduleService();
        Assert.assertNotNull(taskScheduleService);
    }

    @Test(dependsOnMethods = "applyPolicy")
    public void addProfile() throws Exception{
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        profile = new Profile();
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
        profile.setProfileName("tp_profile2");
        profile.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
        Profile profile1 = policyManagerService.addProfile(profile);
        Assert.assertNotNull(profile1);
        Assert.assertEquals("tp_profile2",profile1.getProfileName());
    }

    @Test(dependsOnMethods = "addProfile")
    public void updateProfile() throws Exception{
        Policy effectivePolicy = policyManagerService.getEffectivePolicy(new DeviceIdentifier(DEVICE1, DEVICE_TYPE_A));
        Profile currentProfile = effectivePolicy.getProfile();
        List<ProfileFeature> profileFeatures = new ArrayList<>();
        ProfileFeature profileFeature = new ProfileFeature();
        profileFeature.setContent("{'enable':'true'}");
        profileFeature.setDeviceType(DEVICE_TYPE_A);
        profileFeature.setFeatureCode(POLICY1_CAM_FEATURE1_CODE);
        profileFeatures.add(profileFeature);
        profile.setProfileFeaturesList(profileFeatures);
        profile.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
        Profile updatedProfile = policyManagerService.updateProfile(this.profile);
        Assert.assertNotNull(profile);
        Assert.assertNotNull(currentProfile.getProfileFeaturesList().get(0).getFeatureCode(),
                updatedProfile.getProfileFeaturesList().get(0).getFeatureCode());
    }
}