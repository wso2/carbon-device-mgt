/*
*  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
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
*/
package org.wso2.carbon.policy.mgt.core;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.PolicyCriterion;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.device.mgt.core.dao.*;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.policy.mgt.core.impl.PolicyAdministratorPointImpl;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PolicyDAOTestCase extends BasePolicyManagementDAOTest {

    private List<Feature> featureList;
    private List<ProfileFeature> profileFeatureList;
    private Profile profile;
    private Policy policy;
    private List<Device> devices;
    private static final Log log = LogFactory.getLog(PolicyDAOTestCase.class);

    @BeforeClass
    @Override
    public void init() throws Exception {
        initDatSource();
        // System.setProperty("GetTenantIDForTest", "Super");
        initiatePrivilegedCaronContext();
    }


    @Test
    public void addDeviceType() throws DeviceManagementDAOException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            DeviceTypeDAO deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
            deviceTypeDAO.addDeviceType(DeviceTypeCreator.getDeviceType(), -1234, true);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            log.error("Error occurred while adding dummy device type", e);
            Assert.fail();
        } catch (TransactionManagementException e) {
            log.error("Error occurred while initiating a transaction to add dummy device type", e);
            Assert.fail();
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }


    @Test(dependsOnMethods = ("addDeviceType"))
    public void addDevice() throws DeviceManagementException, PolicyManagementException {

        DeviceDAO deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        EnrollmentDAO enrollmentDAO = DeviceManagementDAOFactory.getEnrollmentDAO();

        DeviceType type = DeviceTypeCreator.getDeviceType();
        devices = DeviceCreator.getDeviceList(type);
        devices.addAll(DeviceCreator.getDeviceList2(type));
        devices.addAll(DeviceCreator.getDeviceList3(type));
        devices.addAll(DeviceCreator.getDeviceList4(type));
        devices.addAll(DeviceCreator.getDeviceList5(type));
        devices.addAll(DeviceCreator.getDeviceList6(type));

        try {
            DeviceManagementDAOFactory.beginTransaction();
            for (Device device : devices) {
                int id = deviceDAO.addDevice(type.getId(), device, -1234);
                enrollmentDAO.addEnrollment(id, device.getEnrolmentInfo(), -1234);
            }
        } catch (TransactionManagementException e) {
            log.error("Error occurred while adding device enrolment", e);
            Assert.fail();
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            log.error("Error occurred while adding device information", e);
            Assert.fail();
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }


        DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();

        PolicyManagementDataHolder.getInstance().setDeviceManagementService(service);

        if (log.isDebugEnabled()) {
            log.debug("Printing device taken by calling the service layer with device type.");
        }
        List<Device> devices3 = service.getAllDevices("android");

        log.debug("Device list size ...! " + devices3.size());
        for (Device device : devices3) {
            log.debug(device.getDeviceIdentifier());
        }
    }


    @Test(dependsOnMethods = ("addDevice"))
    public void addFeatures() throws FeatureManagementException {

        //This test case was removed because architecture was changed

//        FeatureManager featureManager = new FeatureManagerImpl();
        featureList = FeatureCreator.getFeatureList();
//        featureManager.addFeatures(featureList);
//        for (Feature feature : featureList) {
//           featureManager.addFeature(feature);
//        }

    }

    @Test(dependsOnMethods = ("addFeatures"))
    public void addProfileFeatures() throws ProfileManagementException {

//        ProfileManager profileManager = new ProfileManagerImpl();
//        Profile profile = ProfileCreator.getProfile2(FeatureCreator.getFeatureList2());
//        profileManager.addProfile(profile);
//        profileFeatureList = profile.getProfileFeaturesList();
    }

    @Test(dependsOnMethods = ("addProfileFeatures"))
    public void addPolicy() {
//        ProfileManager profileManager = new ProfileManagerImpl();
        try {
            Profile profile = ProfileCreator.getProfile5(FeatureCreator.getFeatureList5());
//        profileManager.addProfile(profile);
            PolicyAdministratorPoint pap = new PolicyAdministratorPointImpl();
            policy = PolicyCreator.createPolicy(profile);
            policy = pap.addPolicy(policy);
            pap.activatePolicy(policy.getId());
        } catch (PolicyManagementException e) {
            log.error("Error occurred while adding the policy", e);
            Assert.fail();
        }
    }

    @Test(dependsOnMethods = ("addPolicy"))
    public void addPolicyToRole() throws PolicyManagementException {
        PolicyAdministratorPoint pap = new PolicyAdministratorPointImpl();

        List<String> roles = new ArrayList<String>();
        roles.add("Test_ROLE_01");
        roles.add("Test_ROLE_02");
        roles.add("Test_ROLE_03");

        policy = pap.getPolicy(policy.getId());

        pap.addPolicyToRole(roles, policy);

    }

    @Test(dependsOnMethods = ("addPolicyToRole"))
    public void addPolicyToDevice() throws PolicyManagementException {
        PolicyAdministratorPoint pap = new PolicyAdministratorPointImpl();
        Device device = DeviceCreator.getSingleDevice();

        List<DeviceIdentifier> deviceIdentifierList = new ArrayList<DeviceIdentifier>();
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(device.getDeviceIdentifier());
        deviceIdentifier.setType("android");

        deviceIdentifierList.add(deviceIdentifier);
        pap.addPolicyToDevice(deviceIdentifierList, policy);

    }

    @Test(dependsOnMethods = ("addPolicyToDevice"))
    public void addNewPolicy() throws PolicyManagementException, ProfileManagementException {
        PolicyAdministratorPoint pap = new PolicyAdministratorPointImpl();
        Profile profile = ProfileCreator.getProfile3(FeatureCreator.getFeatureList3());
        //pap.addProfile(profile);
        // PolicyManager policyManager = new PolicyManagerImpl();
        Policy policy = PolicyCreator.createPolicy2(profile);
        policy = pap.addPolicy(policy);
        pap.activatePolicy(policy.getId());
    }


    @Test(dependsOnMethods = ("addPolicyToDevice"))
    public void addThirdPolicy() throws PolicyManagementException, ProfileManagementException {
        //ProfileManager profileManager = new ProfileManagerImpl();
        Profile profile = ProfileCreator.getProfile4(FeatureCreator.getFeatureList4());
        //profileManager.addProfile(profile);
        PolicyAdministratorPoint pap = new PolicyAdministratorPointImpl();
        Policy policy = PolicyCreator.createPolicy4(profile);
        policy = pap.addPolicy(policy);
        pap.activatePolicy(policy.getId());
    }

    @Test(dependsOnMethods = ("addNewPolicy"))
    public void getPolicies() {
        PolicyAdministratorPoint policyAdministratorPoint = new PolicyAdministratorPointImpl();
        List<Policy> policyList = null;
        try {
            policyList = policyAdministratorPoint.getPolicies();
        } catch (PolicyManagementException e) {
            log.error("Error occurred while retrieving all the policies registered in the system", e);
            Assert.fail();
        }

        log.debug("----------All policies---------");

        for (Policy policy : policyList) {
            log.debug("Policy Id : " + policy.getId() + " Policy Name : " + policy.getPolicyName());
            log.debug("Policy Ownership type :" + policy.getOwnershipType());

            List<String> users = policy.getUsers();
            for (String user : users) {
                log.debug("User of the policy : " + user);
            }

            List<String> roles = policy.getRoles();
            for (String role : roles) {
                log.debug("User of the policy : " + role);
            }
        }
    }

    @Test(dependsOnMethods = ("getPolicies"))
    public void getDeviceTypeRelatedPolicy() {

        PolicyAdministratorPoint policyAdministratorPoint = new PolicyAdministratorPointImpl();
        List<Policy> policyList = null;
        try {
            policyList = policyAdministratorPoint.getPoliciesOfDeviceType("android");
        } catch (PolicyManagementException e) {
            log.error("Error occurred while retrieving the list of policies configured upon the platform 'android'", e);
            Assert.fail();
        }

        log.debug("----------Device type related policy---------");

        for (Policy policy : policyList) {
            log.debug("Policy Id : " + policy.getId() + " Policy Name : " + policy.getPolicyName());
            log.debug("Policy Ownership type :" + policy.getOwnershipType());

            List<String> users = policy.getUsers();
            for (String user : users) {
                log.debug("User of the policy : " + user);
            }

            List<String> roles = policy.getRoles();
            for (String role : roles) {
                log.debug("User of the policy : " + role);
            }
        }
    }


    @Test(dependsOnMethods = ("getDeviceTypeRelatedPolicy"))
    public void getUserRelatedPolicy() {
        String targetUser = "Dilshan";
        PolicyAdministratorPoint policyAdministratorPoint = new PolicyAdministratorPointImpl();
        List<Policy> policyList = null;
        try {
            policyList = policyAdministratorPoint.getPoliciesOfUser(targetUser);
        } catch (PolicyManagementException e) {
            log.error("Error occurred while retrieving the list of policies assigned to the user '" +
                    targetUser + "'", e);
            Assert.fail();
        }

        log.debug("----------User related policy---------");

        for (Policy policy : policyList) {
            log.debug("Policy Id : " + policy.getId() + " Policy Name : " + policy.getPolicyName());
            log.debug("Policy Ownership type :" + policy.getOwnershipType());

            List<String> users = policy.getUsers();
            for (String user : users) {
                log.debug("User of the policy : " + user);
            }

            List<String> roles = policy.getRoles();
            for (String role : roles) {
                log.debug("User of the policy : " + role);
            }
        }
    }

    @Test(dependsOnMethods = ("getDeviceTypeRelatedPolicy"))
    public void getRoleRelatedPolicy() {
        String targetRole = "Test_ROLE_01";
        PolicyAdministratorPoint policyAdministratorPoint = new PolicyAdministratorPointImpl();
        List<Policy> policyList = null;
        try {
            policyList = policyAdministratorPoint.getPoliciesOfRole(targetRole);
        } catch (PolicyManagementException e) {
            log.error("Error occurred while retrieving the list of policies defined against the role '" +
                    targetRole + "'", e);
            Assert.fail();
        }

        log.debug("----------Roles related policy---------");

        for (Policy policy : policyList) {
            log.debug("Policy Id : " + policy.getId() + " Policy Name : " + policy.getPolicyName());

            log.debug("Policy Ownership type :" + policy.getOwnershipType());
        }
    }

    @Test(dependsOnMethods = ("getRoleRelatedPolicy"))
    public void addSecondPolicy() throws PolicyManagementException, ProfileManagementException {
        // ProfileManager profileManager = new ProfileManagerImpl();
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList());
        //profileManager.addProfile(profile);
        PolicyAdministratorPoint pap = new PolicyAdministratorPointImpl();
        Policy policy = PolicyCreator.createPolicy3(profile);
        policy = pap.addPolicy(policy);
        pap.activatePolicy(policy.getId());
    }

    @Test(dependsOnMethods = ("addSecondPolicy"))
    public void updatedPolicy() throws PolicyManagementException {
        PolicyAdministratorPoint pap = new PolicyAdministratorPointImpl();
        Profile profile = ProfileCreator.getProfile3(FeatureCreator.getFeatureList3());
        Policy policy = PolicyCreator.createPolicy3(profile);
        policy.setPolicyName("Policy_05");
        policy = pap.addPolicy(policy);
        pap.activatePolicy(policy.getId());
        List<String> users = new ArrayList<>();
        users.add("Udara");
        users.add("Dileesha");
        policy.setUsers(users);

        Profile profile2 = ProfileCreator.getProfile3(FeatureCreator.getFeatureList4());

        Profile pf = new Profile();

        pap.updatePolicy(policy);
        pap.activatePolicy(policy.getId());
    }

    @Test(dependsOnMethods = ("updatedPolicy"))
    public void getRoleRelatedPolicySecondTime() throws PolicyManagementException {

        PolicyAdministratorPoint policyAdministratorPoint = new PolicyAdministratorPointImpl();
        List<Policy> policyList = policyAdministratorPoint.getPoliciesOfRole("Role_01");

        log.debug("----------Roles related policy second time ---------");

        for (Policy policy : policyList) {
            log.debug("Policy Id : " + policy.getId() + " Policy Name : " + policy.getPolicyName());

            log.debug("Policy Ownership type :" + policy.getOwnershipType());

            List<ProfileFeature> profileFeatures = policy.getProfile().getProfileFeaturesList();

            for (ProfileFeature profileFeature : profileFeatures) {
                log.debug("Feature Content" + profileFeature.getId() + " - " + profileFeature.getContent());
            }

        }
    }

    @Test(dependsOnMethods = ("getRoleRelatedPolicySecondTime"))
    public void getRoleRelatedPolicyThirdTime() throws PolicyManagementException {

        PolicyAdministratorPoint policyAdministratorPoint = new PolicyAdministratorPointImpl();
        List<Policy> policyList = policyAdministratorPoint.getPoliciesOfRole("Role_02");


        log.debug("----------Roles related policy third time ---------");

        for (Policy policy : policyList) {
            log.debug("Policy Id : " + policy.getId() + " Policy Name : " + policy.getPolicyName());

            List<ProfileFeature> profileFeatures = policy.getProfile().getProfileFeaturesList();

//            for (ProfileFeature profileFeature : profileFeatures) {
//                log.debug("Feature Content" + profileFeature.getId() + " - " + profileFeature.getContent());
//            }

            List<PolicyCriterion> criteria = policy.getPolicyCriterias();

            for (PolicyCriterion criterion : criteria) {
                log.debug("Criterias " + criterion.getName() + " -- " + criterion.getCriteriaId() + " -- "
                        + criterion.getId());

                Properties prop = criterion.getProperties();

                for (String key : prop.stringPropertyNames()) {
                    log.debug("Property Names : " + key + " -- " + prop.getProperty(key));
                }
            }

        }
    }


    @Test(dependsOnMethods = ("getRoleRelatedPolicyThirdTime"))
    public void deletPolicy() throws PolicyManagementException {
        PolicyAdministratorPoint policyAdministratorPoint = new PolicyAdministratorPointImpl();
        policyAdministratorPoint.deletePolicy(1);

        log.debug("First policy deleted..!");
    }


    @Test(dependsOnMethods = ("deletPolicy"))
    public void testMonitorDao() throws PolicyManagementException, DeviceManagementException {

        DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
        PolicyManagerService policyManagerService = new PolicyManagerServiceImpl();

        List<Policy> policies = policyManagerService.getPolicies("android");
        List<Device> devices = service.getAllDevices("android");

        for (Policy policy : policies) {
            log.debug("Policy Name : " + policy.getPolicyName());
        }

        for (Device device : devices) {
            log.debug("Device Name : " + device.getDeviceIdentifier());
        }
    }

}
