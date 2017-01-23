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

package org.wso2.carbon.policy.mgt.core.mgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.DeviceGroupWrapper;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.PolicyCriterion;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderServiceImpl;
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.policy.mgt.core.cache.impl.PolicyCacheManagerImpl;
import org.wso2.carbon.policy.mgt.core.dao.*;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.policy.mgt.core.mgt.ProfileManager;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class PolicyManagerImpl implements PolicyManager {

    private PolicyDAO policyDAO;
    private ProfileDAO profileDAO;
    private FeatureDAO featureDAO;
    private ProfileManager profileManager;
    private static Log log = LogFactory.getLog(PolicyManagerImpl.class);

    public PolicyManagerImpl() {
        this.policyDAO = PolicyManagementDAOFactory.getPolicyDAO();
        this.profileDAO = PolicyManagementDAOFactory.getProfileDAO();
        this.featureDAO = PolicyManagementDAOFactory.getFeatureDAO();
        this.profileManager = new ProfileManagerImpl();
    }

    @Override
    public Policy addPolicy(Policy policy) throws PolicyManagementException {

        try {
            PolicyManagementDAOFactory.beginTransaction();
            if (policy.getProfile() != null && policy.getProfile().getProfileId() == 0) {
                Profile profile = policy.getProfile();

                Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                profile.setCreatedDate(currentTimestamp);
                profile.setUpdatedDate(currentTimestamp);

                profileDAO.addProfile(profile);
                featureDAO.addProfileFeatures(profile.getProfileFeaturesList(), profile.getProfileId());
            }
            policy = policyDAO.addPolicy(policy);

            if (policy.getUsers() != null) {
                policyDAO.addPolicyToUser(policy.getUsers(), policy);
            }

            if (policy.getRoles() != null) {
                policyDAO.addPolicyToRole(policy.getRoles(), policy);
            }

            if (policy.getDevices() != null) {
                policyDAO.addPolicyToDevice(policy.getDevices(), policy);
            }

            if (policy.getDeviceGroups() != null && !policy.getDeviceGroups().isEmpty()) {
                policyDAO.addDeviceGroupsToPolicy(policy);
            }

            if (policy.getPolicyCriterias() != null) {
                List<PolicyCriterion> criteria = policy.getPolicyCriterias();
                for (PolicyCriterion criterion : criteria) {

                    Criterion cr = policyDAO.getCriterion(criterion.getName());

                    if (cr.getId() == 0) {
                        Criterion criteriaObj = new Criterion();
                        criteriaObj.setName(criterion.getName());
                        policyDAO.addCriterion(criteriaObj);
                        criterion.setCriteriaId(criteriaObj.getId());
                    } else {
                        criterion.setCriteriaId(cr.getId());
                    }
                }

                policyDAO.addPolicyCriteria(policy);
                policyDAO.addPolicyCriteriaProperties(policy.getPolicyCriterias());
            }

            if (policy.isActive()) {
                policyDAO.activatePolicy(policy.getId());
            }
            PolicyManagementDAOFactory.commitTransaction();

        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the policy (" +
                    policy.getId() + " - " + policy.getPolicyName() + ")", e);

        } catch (ProfileManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the profile related to policy (" +
                    policy.getId() + " - " + policy.getPolicyName() + ")", e);
        } catch (FeatureManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the features of profile related to " +
                    "policy (" + policy.getId() + " - " + policy.getPolicyName() + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policy;
    }

    @Override
    public Policy updatePolicy(Policy policy) throws PolicyManagementException {

        try {
            // Previous policy needs to be obtained before beginning the transaction
            Policy previousPolicy = this.getPolicy(policy.getId());

            PolicyManagementDAOFactory.beginTransaction();
            // This will keep track of the policies updated.
            policyDAO.recordUpdatedPolicy(policy);


            List<ProfileFeature> existingFeaturesList = new ArrayList<>();
            List<ProfileFeature> newFeaturesList = new ArrayList<>();
            List<ProfileFeature> featuresToDelete = new ArrayList<>();
            List<String> temp = new ArrayList<>();
            List<String> updateDFes = new ArrayList<>();

            List<ProfileFeature> updatedFeatureList = policy.getProfile().getProfileFeaturesList();

            List<ProfileFeature> existingProfileFeaturesList = previousPolicy.getProfile().getProfileFeaturesList();

            // Checks for the existing features
            for (ProfileFeature feature : updatedFeatureList) {
                for (ProfileFeature fe : existingProfileFeaturesList) {
                    if (feature.getFeatureCode().equalsIgnoreCase(fe.getFeatureCode())) {
                        existingFeaturesList.add(feature);
                        temp.add(feature.getFeatureCode());
                    }
                }
                updateDFes.add(feature.getFeatureCode());
            }

            // Check for the features to delete
            for (ProfileFeature feature : existingProfileFeaturesList) {
                if (!updateDFes.contains(feature.getFeatureCode())) {
                    featuresToDelete.add(feature);
                }
            }

            // Checks for the new features
            for (ProfileFeature feature : updatedFeatureList) {
                if (!temp.contains(feature.getFeatureCode())) {
                    newFeaturesList.add(feature);
                }
            }

            int profileId = previousPolicy.getProfile().getProfileId();
            policy.getProfile().setProfileId(profileId);
            policy.setProfileId(profileId);
            Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            policy.getProfile().setUpdatedDate(currentTimestamp);

            policyDAO.updatePolicy(policy);
            profileDAO.updateProfile(policy.getProfile());

            featureDAO.updateProfileFeatures(existingFeaturesList, profileId);
            if (!newFeaturesList.isEmpty()) {
                featureDAO.addProfileFeatures(newFeaturesList, profileId);
            }

            if (!featuresToDelete.isEmpty()) {
                for (ProfileFeature pf : featuresToDelete)
                    featureDAO.deleteProfileFeatures(pf.getId());
            }

            policyDAO.deleteCriteriaAndDeviceRelatedConfigs(policy.getId());


            if (policy.getUsers() != null) {
                policyDAO.updateUserOfPolicy(policy.getUsers(), previousPolicy);
            }

            if (policy.getRoles() != null) {
                policyDAO.updateRolesOfPolicy(policy.getRoles(), previousPolicy);
            }

            if (policy.getDevices() != null) {
                policyDAO.addPolicyToDevice(policy.getDevices(), previousPolicy);
            }

            if (policy.getDeviceGroups() != null && !policy.getDeviceGroups().isEmpty()) {
                policyDAO.addDeviceGroupsToPolicy(policy);
            }

            if (policy.getPolicyCriterias() != null) {
                List<PolicyCriterion> criteria = policy.getPolicyCriterias();
                for (PolicyCriterion criterion : criteria) {
                    if (!policyDAO.checkCriterionExists(criterion.getName())) {
                        Criterion criteriaObj = new Criterion();
                        criteriaObj.setName(criterion.getName());
                        policyDAO.addCriterion(criteriaObj);
                        criterion.setCriteriaId(criteriaObj.getId());
                    }
                }

                policyDAO.addPolicyCriteria(policy);
                policyDAO.addPolicyCriteriaProperties(policy.getPolicyCriterias());
            }

            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while updating the policy ("
                    + policy.getId() + " - " + policy.getPolicyName() + ")", e);
        } catch (ProfileManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while updating the profile (" +
                    policy.getProfile().getProfileName() + ")", e);
        } catch (FeatureManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while updating the profile features (" +
                    policy.getProfile().getProfileName() + ")", e);

        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policy;
    }

    @Override
    public boolean updatePolicyPriorities(List<Policy> policies) throws PolicyManagementException {
        boolean bool;
        try {
//            List<Policy> existingPolicies = this.getPolicies();
            List<Policy> existingPolicies = PolicyCacheManagerImpl.getInstance().getAllPolicies();
            PolicyManagementDAOFactory.beginTransaction();
            bool = policyDAO.updatePolicyPriorities(policies);

            // This logic is added because ui sends only policy id and priority to update priorities.

            for (Policy policy : policies) {
                for (Policy exPolicy : existingPolicies) {
                    if (policy.getId() == exPolicy.getId()) {
                        policy.setProfile(exPolicy.getProfile());
                    }
                }
            }
            policyDAO.recordUpdatedPolicies(policies);
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while updating the policy priorities", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return bool;
    }

    @Override
    public boolean deletePolicy(Policy policy) throws PolicyManagementException {
        try {
            PolicyManagementDAOFactory.beginTransaction();
            policyDAO.deleteAllPolicyRelatedConfigs(policy.getId());
            policyDAO.deletePolicy(policy.getId());
            featureDAO.deleteFeaturesOfProfile(policy.getProfileId());
            profileDAO.deleteProfile(policy.getProfileId());
            PolicyManagementDAOFactory.commitTransaction();
            return true;
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while deleting the policy ("
                    + policy.getId() + " - " + policy.getPolicyName() + ")", e);
        } catch (ProfileManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while deleting the profile for policy ("
                    + policy.getId() + ")", e);
        } catch (FeatureManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while deleting the profile features for policy ("
                    + policy.getId() + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean deletePolicy(int policyId) throws PolicyManagementException {
        boolean bool;
        try {
            PolicyManagementDAOFactory.beginTransaction();

            Policy policy = policyDAO.getPolicy(policyId);
            policyDAO.deleteAllPolicyRelatedConfigs(policyId);
            bool = policyDAO.deletePolicy(policyId);

            if (log.isDebugEnabled()) {
                log.debug("Profile ID: " + policy.getProfileId());
            }

            featureDAO.deleteFeaturesOfProfile(policy.getProfileId());

            profileDAO.deleteProfile(policy.getProfileId());
            PolicyManagementDAOFactory.commitTransaction();
            return bool;
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while deleting the policy (" + policyId + ")", e);
        } catch (ProfileManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while deleting the profile for policy ("
                    + policyId + ")", e);
        } catch (FeatureManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while deleting the profile features for policy ("
                    + policyId + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void activatePolicy(int policyId) throws PolicyManagementException {
        try {
            Policy policy = this.getPolicy(policyId);
            PolicyManagementDAOFactory.beginTransaction();
            policyDAO.activatePolicy(policyId);
            policyDAO.recordUpdatedPolicy(policy);
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while activating the policy. (Id : " + policyId + ")" +
                    "", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void inactivatePolicy(int policyId) throws PolicyManagementException {
        try {
            Policy policy = this.getPolicy(policyId);
            PolicyManagementDAOFactory.beginTransaction();
            policyDAO.inactivatePolicy(policyId);
            policyDAO.recordUpdatedPolicy(policy);
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while inactivating the policy. (Id : " + policyId +
                    ")" +
                    "", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public Policy addPolicyToDevice(List<DeviceIdentifier> deviceIdentifierList,
                                    Policy policy) throws PolicyManagementException {

        List<Device> deviceList = new ArrayList<>();
        DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
        for (DeviceIdentifier deviceIdentifier : deviceIdentifierList) {
            try {
                Device device = service.getDevice(deviceIdentifier);
                deviceList.add(device);
            } catch (DeviceManagementException e) {
                throw new PolicyManagementException("Error occurred while retrieving device information", e);
            }
        }
        try {
            PolicyManagementDAOFactory.beginTransaction();
            if (policy.getId() == 0) {
                policyDAO.addPolicy(policy);
            }

            policy = policyDAO.addPolicyToDevice(deviceList, policy);
            PolicyManagementDAOFactory.commitTransaction();

            if (policy.getDevices() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device list of policy is not null.");
                }
                policy.getDevices().addAll(deviceList);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Device list of policy is null. So added the first device to the list.");
                }
                policy.setDevices(deviceList);
            }
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the policy ("
                    + policy.getId() + " - " + policy.getPolicyName() + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policy;
    }

    @Override
    public Policy addPolicyToRole(List<String> roleNames, Policy policy) throws PolicyManagementException {
        try {
            PolicyManagementDAOFactory.beginTransaction();
            if (policy.getId() == 0) {
                policyDAO.addPolicy(policy);
            }
            policy = policyDAO.addPolicyToRole(roleNames, policy);
            PolicyManagementDAOFactory.commitTransaction();

            if (policy.getRoles() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("New roles list is added to the policy ");
                }
                policy.getRoles().addAll(roleNames);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Roles list was null, new roles are added.");
                }
                policy.setRoles(roleNames);
            }
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the policy ("
                    + policy.getId() + " - " + policy.getPolicyName() + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policy;
    }

    @Override
    public Policy addPolicyToUser(List<String> usernameList, Policy policy) throws PolicyManagementException {

        try {
            PolicyManagementDAOFactory.beginTransaction();
            if (policy.getId() == 0) {
                policyDAO.addPolicy(policy);
            }
            policy = policyDAO.addPolicyToUser(usernameList, policy);
            PolicyManagementDAOFactory.commitTransaction();

            if (policy.getRoles() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("New users list is added to the policy ");
                }
                policy.getRoles().addAll(usernameList);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Users list was null, new users list is added.");
                }
                policy.setRoles(usernameList);
            }
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the policy ("
                    + policy.getId() + " - " + policy.getPolicyName() + ") to user list.", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policy;
    }

    @Override
    public Policy getPolicyByProfileID(int profileId) throws PolicyManagementException {

        Policy policy;
        Profile profile;
        List<Device> deviceList;
        List<String> roleNames;

        try {
            PolicyManagementDAOFactory.openConnection();
            policy = policyDAO.getPolicyByProfileID(profileId);

            roleNames = policyDAO.getPolicyAppliedRoles(policy.getId());
            profile = profileDAO.getProfile(profileId);
            policy.setProfile(profile);
            policy.setRoles(roleNames);


        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the policy related to profile ID (" +
                    profileId + ")", e);
        } catch (ProfileManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the profile related to profile ID (" +
                    profileId + ")", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }

        // This is due to connection close in following method too.
        deviceList = getPolicyAppliedDevicesIds(policy.getId());
        policy.setDevices(deviceList);
        return policy;
    }

    @Override
    public Policy getPolicy(int policyId) throws PolicyManagementException {

        Policy policy;
        List<Device> deviceList;
        List<String> roleNames;
        List<String> userNames;
        try {
            PolicyManagementDAOFactory.openConnection();
            policy = policyDAO.getPolicy(policyId);

            roleNames = policyDAO.getPolicyAppliedRoles(policyId);
            userNames = policyDAO.getPolicyAppliedUsers(policyId);

            //Profile profile = profileDAO.getProfile(policy.getProfileId());


            policy.setRoles(roleNames);
            policy.setUsers(userNames);

        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the policy related to policy ID (" +
                    policyId + ")", e);
//        } catch (ProfileManagerDAOException e) {
//            throw new PolicyManagementException("Error occurred while getting the profile related to policy ID (" +
//                    policyId + ")", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
//        } catch (ProfileManagementException e) {
//            throw new PolicyManagementException("Error occurred while getting the profile related to policy ID (" +
//                    policyId + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }

        // This is done because connection close in below method too.
        deviceList = this.getPolicyAppliedDevicesIds(policyId);
        policy.setDevices(deviceList);

        try {
            //   PolicyManagementDAOFactory.openConnection();
            Profile profile = profileManager.getProfile(policy.getProfileId());
            policy.setProfile(profile);
        } catch (ProfileManagementException e) {
            throw new PolicyManagementException("Error occurred while getting the profile related to policy ID (" +
                    policyId + ")", e);
//        } catch (SQLException e) {
//            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
//        } finally {
//            PolicyManagementDAOFactory.closeConnection();
        }

        return policy;
    }

    @Override
    public List<Policy> getPolicies() throws PolicyManagementException {

        List<Policy> policyList;
        List<Profile> profileList;
        try {
            profileList = profileManager.getAllProfiles();
        } catch (ProfileManagementException e) {
            throw new PolicyManagementException("Error occurred while getting all the profiles.", e);
        }
        try {
            PolicyManagementDAOFactory.openConnection();
            policyList = policyDAO.getAllPolicies();

            for (Policy policy : policyList) {
                for (Profile profile : profileList) {
                    if (policy.getProfileId() == profile.getProfileId()) {
                        policy.setProfile(profile);
                    }
                }
                policy.setRoles(policyDAO.getPolicyAppliedRoles(policy.getId()));
                policy.setUsers(policyDAO.getPolicyAppliedUsers(policy.getId()));
                policy.setPolicyCriterias(policyDAO.getPolicyCriteria(policy.getId()));

                List<DeviceGroupWrapper> deviceGroupWrappers = policyDAO.getDeviceGroupsOfPolicy(policy.getId());
                if (!deviceGroupWrappers.isEmpty()) {
                    deviceGroupWrappers = this.getDeviceGroupNames(deviceGroupWrappers);
                }
                policy.setDeviceGroups(deviceGroupWrappers);

            }
            Collections.sort(policyList);
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting all the policies.", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } catch (GroupManagementException e) {
            throw new PolicyManagementException("Error occurred while getting device groups.", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }

        // Following is done because connection close has been implemented in every method.

        for (Policy policy : policyList) {
            policy.setDevices(this.getPolicyAppliedDevicesIds(policy.getId()));
        }

        return policyList;
    }

    @Override
    public List<Policy> getPoliciesOfDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {

        List<Integer> policyIdList;
        List<Policy> policies = new ArrayList<>();
        try {

            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            Device device = service.getDevice(deviceIdentifier);

            PolicyManagementDAOFactory.openConnection();
            policyIdList = policyDAO.getPolicyIdsOfDevice(device);
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the policies for device identifier (" +
                    deviceIdentifier.getId() + " - " + deviceIdentifier.getType() + ")", e);
        } catch (DeviceManagementException e) {
            throw new PolicyManagementException("Error occurred while getting device related to device identifier (" +
                    deviceIdentifier.getId() + " - " + deviceIdentifier.getType() + ")", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while open a data source connection", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }

//        List<Policy> tempPolicyList = this.getPolicies();
        List<Policy> tempPolicyList = PolicyCacheManagerImpl.getInstance().getAllPolicies();

        for (Policy policy : tempPolicyList) {
            for (Integer i : policyIdList) {
                if (policy.getId() == i) {
                    policies.add(policy);
                }
            }
        }

        Collections.sort(policies);
        return policies;
    }

    @Override
    public List<Policy> getPoliciesOfDeviceType(String deviceTypeName) throws PolicyManagementException {
        List<Policy> policies = new ArrayList<>();
//        try {
        // List<Profile> profileList = profileManager.getProfilesOfDeviceType(deviceTypeName);
//            List<Policy> allPolicies = this.getPolicies();
        List<Policy> allPolicies = PolicyCacheManagerImpl.getInstance().getAllPolicies();

        for (Policy policy : allPolicies) {
            if (policy.getProfile().getDeviceType().equalsIgnoreCase(deviceTypeName)) {
                policies.add(policy);
            }
        }

//            for (Profile profile : profileList) {
//                for (Policy policy : allPolicies) {
//                    if (policy.getProfileId() == profile.getProfileId()) {
//                        policy.setProfile(profile);
//                        policies.add(policy);
//                    }
//                }
//            }
        Collections.sort(policies);
//        } catch (ProfileManagementException e) {
//            throw new PolicyManagementException("Error occurred while getting all the profile features.", e);
//        }
        return policies;
    }

    @Override
    public List<Policy> getPoliciesOfRole(String roleName) throws PolicyManagementException {

        List<Policy> policies = new ArrayList<>();
        List<Integer> policyIdList;

        try {
            PolicyManagementDAOFactory.openConnection();
            policyIdList = policyDAO.getPolicyOfRole(roleName);

        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the policies.", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while open a data source connection", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }

//        List<Policy> tempPolicyList = this.getPolicies();
        List<Policy> tempPolicyList = PolicyCacheManagerImpl.getInstance().getAllPolicies();

        for (Policy policy : tempPolicyList) {
            for (Integer i : policyIdList) {
                if (policy.getId() == i) {
                    policies.add(policy);
                }
            }
        }
        Collections.sort(policies);
        return policies;
    }

    @Override
    public List<Policy> getPoliciesOfUser(String username) throws PolicyManagementException {

        List<Policy> policies = new ArrayList<>();
        List<Integer> policyIdList;

        try {
            PolicyManagementDAOFactory.openConnection();
            policyIdList = policyDAO.getPolicyOfUser(username);
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the policies.", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while open a data source connection", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
//        List<Policy> tempPolicyList = this.getPolicies();
        List<Policy> tempPolicyList = PolicyCacheManagerImpl.getInstance().getAllPolicies();

        for (Policy policy : tempPolicyList) {
            for (Integer i : policyIdList) {
                if (policy.getId() == i) {
                    policies.add(policy);
                }
            }
        }
        Collections.sort(policies);
        return policies;
    }

    @Override
    public List<Device> getPolicyAppliedDevicesIds(int policyId) throws PolicyManagementException {

        List<Device> deviceList = new ArrayList<>();
        List<Integer> deviceIds;
        try {
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            List<Device> allDevices = service.getAllDevices();
            PolicyManagementDAOFactory.openConnection();
            deviceIds = policyDAO.getPolicyAppliedDevicesIds(policyId);
            HashMap<Integer, Device> allDeviceMap = new HashMap<>();
            if (!allDevices.isEmpty()) {
                allDeviceMap = PolicyManagerUtil.covertDeviceListToMap(allDevices);
            }
            for (int deviceId : deviceIds) {
                if (allDeviceMap.containsKey(deviceId)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Policy Applied device ids .............: " + deviceId + " - Policy Id " + policyId);
                    }
                    deviceList.add(allDeviceMap.get(deviceId));
                }
                //TODO FIX ME -- This is wrong, Device id is not  device identifier, so converting is wrong.
                //deviceList.add(deviceDAO.getDevice(new DeviceIdentifier(Integer.toString(deviceId), ""), tenantId));
            }
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the device ids related to policy id (" +
                    policyId + ")", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } catch (DeviceManagementException e) {
            throw new PolicyManagementException("Error occurred while getting the devices related to policy id (" +
                    policyId + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return deviceList;
    }

    @Override
    public void addAppliedPolicyFeaturesToDevice(DeviceIdentifier deviceIdentifier,
                                                 Policy policy) throws PolicyManagementException {
        int deviceId = -1;
        try {
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            Device device = service.getDevice(deviceIdentifier);
            deviceId = device.getId();

            PolicyManagementDAOFactory.beginTransaction();
            boolean exist = policyDAO.checkPolicyAvailable(deviceId, device.getEnrolmentInfo().getId());
            if (exist) {
                policyDAO.updateEffectivePolicyToDevice(deviceId, device.getEnrolmentInfo().getId(), policy);
            } else {
                policyDAO.addEffectivePolicyToDevice(deviceId, device.getEnrolmentInfo().getId(), policy);
            }
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the evaluated policy to device (" +
                    deviceId + " - " + policy.getId() + ")", e);
        } catch (DeviceManagementException e) {
            throw new PolicyManagementException("Error occurred while getting the device details (" +
                    deviceIdentifier.getId() + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<String> applyChangesMadeToPolicies() throws PolicyManagementException {

        List<String> changedDeviceTypes = new ArrayList<>();
        try {
            //HashMap<Integer, Integer> map = policyDAO.getUpdatedPolicyIdandDeviceTypeId();
            List<Policy> updatedPolicies = new ArrayList<>();
//            List<Policy> activePolicies = new ArrayList<>();
//            List<Policy> inactivePolicies = new ArrayList<>();
            List<Integer> updatedPolicyIds = new ArrayList<>();

//            List<Policy> allPolicies = this.getPolicies();
            List<Policy> allPolicies = PolicyCacheManagerImpl.getInstance().getAllPolicies();

            for (Policy policy : allPolicies) {
                if (policy.isUpdated()) {
                    updatedPolicies.add(policy);
                    updatedPolicyIds.add(policy.getId());
                    if (!changedDeviceTypes.contains(policy.getProfile().getDeviceType())) {
                        changedDeviceTypes.add(policy.getProfile().getDeviceType());
                    }
                }
//                if (policy.isActive()) {
//                    activePolicies.add(policy);
//                } else {
//                    inactivePolicies.add(policy);
//                }
            }
            PolicyManagementDAOFactory.beginTransaction();
            policyDAO.markPoliciesAsUpdated(updatedPolicyIds);
            policyDAO.removeRecordsAboutUpdatedPolicies();
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while applying the changes to policy operations.", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return changedDeviceTypes;
    }


    @Override
    public void addAppliedPolicyToDevice(DeviceIdentifier deviceIdentifier, Policy policy)
            throws PolicyManagementException {

        int deviceId = -1;
        try {
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            Device device = service.getDevice(deviceIdentifier);
            deviceId = device.getId();
            PolicyManagementDAOFactory.beginTransaction();

            Policy policySaved = policyDAO.getAppliedPolicy(deviceId, device.getEnrolmentInfo().getId());
            if (policySaved != null && policySaved.getId() != 0) {
                policyDAO.updateEffectivePolicyToDevice(deviceId, device.getEnrolmentInfo().getId(), policy);
            } else {
                policyDAO.addEffectivePolicyToDevice(deviceId, device.getEnrolmentInfo().getId(), policy);
            }
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the evaluated policy to device (" +
                    deviceId + " - " + policy.getId() + ")", e);
        } catch (DeviceManagementException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while getting the device details (" +
                    deviceIdentifier.getId() + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void removeAppliedPolicyToDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {

        int deviceId = -1;
        try {
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            Device device = service.getDevice(deviceIdentifier);
            deviceId = device.getId();
            PolicyManagementDAOFactory.beginTransaction();

            Policy policySaved = policyDAO.getAppliedPolicy(deviceId, device.getEnrolmentInfo().getId());
            if (policySaved != null) {
                policyDAO.deleteEffectivePolicyToDevice(deviceId, device.getEnrolmentInfo().getId());
            }
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while removing the applied policy to device (" +
                    deviceId + ")", e);
        } catch (DeviceManagementException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while getting the device details (" +
                    deviceIdentifier.getId() + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean checkPolicyAvailable(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {

        boolean exist;
        try {
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            Device device = service.getDevice(deviceIdentifier);
            PolicyManagementDAOFactory.openConnection();
            exist = policyDAO.checkPolicyAvailable(device.getId(), device.getEnrolmentInfo().getId());
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while checking whether device has a policy " +
                    "to apply.", e);
        } catch (DeviceManagementException e) {
            throw new PolicyManagementException("Error occurred while getting the device details (" +
                    deviceIdentifier.getId() + ")", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return exist;
    }

    @Override
    public boolean setPolicyApplied(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        try {
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            Device device = service.getDevice(deviceIdentifier);

            PolicyManagementDAOFactory.openConnection();
            policyDAO.setPolicyApplied(device.getId(), device.getEnrolmentInfo().getId());
            return true;
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while setting the policy has applied to device (" +
                    deviceIdentifier.getId() + ")", e);
        } catch (DeviceManagementException e) {
            throw new PolicyManagementException("Error occurred while getting the device details (" +
                    deviceIdentifier.getId() + ")", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getPolicyCount() throws PolicyManagementException {
        try {
            PolicyManagementDAOFactory.openConnection();
            return policyDAO.getPolicyCount();
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting policy count", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public Policy getAppliedPolicyToDevice(DeviceIdentifier deviceId) throws PolicyManagementException {
        Policy policy;
        DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
        Device device;
        try {
            device = service.getDevice(deviceId);
            if (device == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No device is found upon the device identifier '" + deviceId.getId() +
                            "' and type '" + deviceId.getType() + "'. Therefore returning null");
                }
                return null;
            }
        } catch (DeviceManagementException e) {
            throw new PolicyManagementException("Error occurred while getting device id.", e);
        }
        try {
            //int policyId = policyDAO.getAppliedPolicyId(device.getId());
            PolicyManagementDAOFactory.openConnection();
            policy = policyDAO.getAppliedPolicy(device.getId(), device.getEnrolmentInfo().getId());
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting policy id or policy.", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policy;
    }

    @Override
    public HashMap<Integer, Integer> getAppliedPolicyIdsDeviceIds() throws PolicyManagementException {
        try {
            PolicyManagementDAOFactory.openConnection();
            return policyDAO.getAppliedPolicyIdsDeviceIds();
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while reading the policy applied database.", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while reading the policy applied database.", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    private List<DeviceGroupWrapper> getDeviceGroupNames(List<DeviceGroupWrapper> groupWrappers) throws GroupManagementException {
        GroupManagementProviderService groupManagementProviderService = new GroupManagementProviderServiceImpl();
        for (DeviceGroupWrapper wrapper : groupWrappers) {
            DeviceGroup deviceGroup = groupManagementProviderService.getGroup(wrapper.getId());
            wrapper.setName(deviceGroup.getName());
            wrapper.setOwner(deviceGroup.getOwner());
        }
        return groupWrappers;
    }

}
