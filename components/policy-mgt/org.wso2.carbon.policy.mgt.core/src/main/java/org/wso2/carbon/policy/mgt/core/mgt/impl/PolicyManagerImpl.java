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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dto.Device;
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.policy.mgt.core.dao.*;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.policy.mgt.core.mgt.ProfileManager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PolicyManagerImpl implements PolicyManager {

    private PolicyDAO policyDAO;
    private ProfileDAO profileDAO;
    private FeatureDAO featureDAO;
    private DeviceDAO deviceDAO;
    //    private DeviceTypeDAO deviceTypeDAO;
    private ProfileManager profileManager;
    private static Log log = LogFactory.getLog(PolicyManagerImpl.class);

    public PolicyManagerImpl() {
        this.policyDAO = PolicyManagementDAOFactory.getPolicyDAO();
        this.profileDAO = PolicyManagementDAOFactory.getProfileDAO();
        this.featureDAO = PolicyManagementDAOFactory.getFeatureDAO();
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
//        this.deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
        this.profileManager = new ProfileManagerImpl();
    }

    @Override
    public Policy addPolicy(Policy policy) throws PolicyManagementException {

        try {
            PolicyManagementDAOFactory.beginTransaction();
            if (policy.getProfile() != null && policy.getProfile().getProfileId() == 0) {
                Profile   profile = policy.getProfile();

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

//            if (policy.getEndDate() != null & policy.getStartDate() != null) {
//                policyDAO.addDatesToPolicy(policy.getStartDate(), policy.getEndDate(), policy);
//            }
//
//            if (policy.getStartTime() != 0 & policy.getEndTime() != 0) {
//                policyDAO.addTimesToPolicy(policy.getStartTime(), policy.getEndTime(), policy);
//            }
//
//            if (policy.getLatitude() != null && policy.getLongitude() != null) {
//                policyDAO.addLocationToPolicy(policy.getLatitude(), policy.getLongitude(), policy);
//            }
            PolicyManagementDAOFactory.commitTransaction();

        } catch (PolicyManagerDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Error occurred while adding the policy (" +
                    policy.getId() + " - " + policy.getPolicyName() + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);

        } catch (ProfileManagerDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Error occurred while adding the profile related to policy (" +
                    policy.getId() + " - " + policy.getPolicyName() + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (FeatureManagerDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Error occurred while adding the features of profile related to policy (" +
                    policy.getId() + " - " + policy.getPolicyName() + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return policy;
    }

    @Override
    public Policy updatePolicy(Policy policy) throws PolicyManagementException {

        try {
            PolicyManagementDAOFactory.beginTransaction();
            policy = policyDAO.updatePolicy(policy);
            policyDAO.deleteAllPolicyRelatedConfigs(policy.getId());

            if (policy.getUsers() != null) {
                policyDAO.addPolicyToUser(policy.getUsers(), policy);
            }

            if (policy.getRoles() != null) {
                policyDAO.addPolicyToRole(policy.getRoles(), policy);
            }

            if (policy.getDevices() != null) {
                policyDAO.addPolicyToDevice(policy.getDevices(), policy);
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

//            if (policy.getEndDate() != null & policy.getStartDate() != null) {
//                policyDAO.addDatesToPolicy(policy.getStartDate(), policy.getEndDate(), policy);
//            }
//
//            if (policy.getStartTime() != 0 & policy.getEndTime() != 0) {
//                policyDAO.addTimesToPolicy(policy.getStartTime(), policy.getEndTime(), policy);
//            }
//
//            if (policy.getLatitude() != null && policy.getLongitude() != null) {
//                policyDAO.addLocationToPolicy(policy.getLatitude(), policy.getLongitude(), policy);
//            }

            PolicyManagementDAOFactory.commitTransaction();

        } catch (PolicyManagerDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Error occurred while updating the policy ("
                    + policy.getId() + " - " + policy.getPolicyName() + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return policy;
    }

    @Override
    public boolean deletePolicy(Policy policy) throws PolicyManagementException {

        boolean bool;
        try {
            PolicyManagementDAOFactory.beginTransaction();
            bool = policyDAO.deletePolicy(policy);
            PolicyManagementDAOFactory.commitTransaction();

        } catch (PolicyManagerDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Error occurred while deleting the policy ("
                    + policy.getId() + " - " + policy.getPolicyName() + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return bool;
    }

    @Override
    public boolean deletePolicy(int policyId) throws PolicyManagementException {
        boolean bool;
        try {
            PolicyManagementDAOFactory.beginTransaction();
            policyDAO.deleteAllPolicyRelatedConfigs(policyId);
            bool = policyDAO.deletePolicy(policyId);
            PolicyManagementDAOFactory.commitTransaction();

        } catch (PolicyManagerDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Error occurred while deleting the policy ("
                    + policyId +")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return bool;
    }

    @Override
    public Policy addPolicyToDevice(List<DeviceIdentifier> deviceIdentifierList, Policy policy) throws
            PolicyManagementException {

        try {
            PolicyManagementDAOFactory.beginTransaction();
            if (policy.getId() == 0) {
                policyDAO.addPolicy(policy);
            }
            List<Device> deviceList = new ArrayList<Device>();
            for (DeviceIdentifier deviceIdentifier : deviceIdentifierList) {
                deviceList.add(deviceDAO.getDevice(deviceIdentifier));
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
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Error occurred while adding the policy ("
                    + policy.getId() + " - " + policy.getPolicyName() + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Error occurred while adding the policy to device list";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
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
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Error occurred while adding the policy ("
                    + policy.getId() + " - " + policy.getPolicyName() + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
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
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Error occurred while adding the policy ("
                    + policy.getId() + " - " + policy.getPolicyName() + ") to user list.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
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
            policy = policyDAO.getPolicyByProfileID(profileId);
            deviceList = getPolicyAppliedDevicesIds(policy.getId());
            roleNames = policyDAO.getPolicyAppliedRoles(policy.getId());
//            policyDAO.getDatesOfPolicy(policy);
//            policyDAO.getTimesOfPolicy(policy);
//            policyDAO.getLocationsOfPolicy(policy);

            profile = profileDAO.getProfiles(profileId);

            policy.setProfile(profile);
            policy.setRoles(roleNames);
            policy.setDevices(deviceList);

        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while getting the policy related to profile ID (" + profileId + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (ProfileManagerDAOException e) {
            String msg = "Error occurred while getting the profile related to profile ID (" + profileId + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return policy;
    }

    @Override
    public Policy getPolicy(int policyId) throws PolicyManagementException {

        Policy policy;
        List<Device> deviceList;
        List<String> roleNames;

        try {
            policy = policyDAO.getPolicy(policyId);
            deviceList = getPolicyAppliedDevicesIds(policyId);
            roleNames = policyDAO.getPolicyAppliedRoles(policyId);
//            policyDAO.getDatesOfPolicy(policy);
//            policyDAO.getTimesOfPolicy(policy);
//            policyDAO.getLocationsOfPolicy(policy);

            Profile profile = profileDAO.getProfiles(policy.getProfileId());

            policy.setProfile(profile);
            policy.setRoles(roleNames);
            policy.setDevices(deviceList);

        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while getting the policy related to policy ID (" + policyId + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (ProfileManagerDAOException e) {
            String msg = "Error occurred while getting the profile related to policy ID (" + policyId + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return policy;
    }

    @Override
    public List<Policy> getPolicies() throws PolicyManagementException {

        List<Policy> policyList;

        try {
            policyList = policyDAO.getAllPolicies();
//            List<Profile> profileList = profileDAO.getAllProfiles();
            List<Profile> profileList = profileManager.getAllProfiles();

            for (Policy policy : policyList) {
                for (Profile profile : profileList) {
                    if (policy.getProfileId() == profile.getProfileId()) {
                        policy.setProfile(profile);
                    }
                }
                policy.setDevices(getPolicyAppliedDevicesIds(policy.getId()));
                policy.setRoles(policyDAO.getPolicyAppliedRoles(policy.getId()));
                policy.setPolicyCriterias(policyDAO.getPolicyCriteria(policy.getId()));
//                policyDAO.getDatesOfPolicy(policy);
//                policyDAO.getTimesOfPolicy(policy);
//                policyDAO.getLocationsOfPolicy(policy);
            }

        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while getting all the policies.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while getting all the profiles.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return policyList;
    }

    @Override
    public List<Policy> getPoliciesOfDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {

        List<Integer> policyIdList;
        List<Policy> policies = new ArrayList<Policy>();
        try {
            Device device = deviceDAO.getDevice(deviceIdentifier);
            policyIdList = policyDAO.getPolicyIdsOfDevice(device);
            List<Policy> tempPolicyList = this.getPolicies();

            for (Policy policy : tempPolicyList) {
                for (Integer i : policyIdList) {
                    if (policy.getId() == i) {
                        policies.add(policy);
                    }
                }
            }

        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while getting the policies for device identifier (" +
                    deviceIdentifier.getId() + " - " + deviceIdentifier.getType() + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while getting device related to device identifier (" +
                    deviceIdentifier.getId() + " - " + deviceIdentifier.getType() + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return policies;
    }

    @Override
    public List<Policy> getPoliciesOfDeviceType(String deviceTypeName) throws PolicyManagementException {

        List<Policy> policies = new ArrayList<Policy>();

        try {
//            DeviceType deviceType = deviceTypeDAO.getDeviceType(deviceTypeName);

            List<Profile> profileList = profileManager.getProfilesOfDeviceType(deviceTypeName);
            List<Policy> allPolicies = this.getPolicies();


            for (Profile profile : profileList) {
                for (Policy policy : allPolicies) {
                    if (policy.getProfileId() == profile.getProfileId()) {
                        policy.setProfile(profile);
                        policies.add(policy);
                    }
                }
            }

//        } catch (PolicyManagerDAOException e) {
//            String msg = "Error occurred while getting all the policies.";
//            log.error(msg, e);
//            throw new PolicyManagementException(msg, e);
//        } catch (ProfileManagerDAOException e) {
//            String msg = "Error occurred while getting the profiles related to device type (" + deviceTypeName + ")";
//            log.error(msg, e);
//            throw new PolicyManagementException(msg, e);
//        } catch (DeviceManagementDAOException e) {
//            String msg = "Error occurred while getting device type object related to (" + deviceTypeName + ")";
//            log.error(msg, e);
//            throw new PolicyManagementException(msg, e);
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while getting all the profile features.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return policies;
    }

    @Override
    public List<Policy> getPoliciesOfRole(String roleName) throws PolicyManagementException {

        List<Policy> policies = new ArrayList<Policy>();
        List<Integer> policyIdList;

        try {
            policyIdList = policyDAO.getPolicyOfRole(roleName);
            List<Policy> tempPolicyList = this.getPolicies();

            for (Policy policy : tempPolicyList) {
                for (Integer i : policyIdList) {
                    if (policy.getId() == i) {
                        policies.add(policy);
                    }
                }
            }

        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while getting the policies.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return policies;
    }

    @Override
    public List<Policy> getPoliciesOfUser(String username) throws PolicyManagementException {

        List<Policy> policies = new ArrayList<Policy>();
        List<Integer> policyIdList;

        try {
            policyIdList = policyDAO.getPolicyOfUser(username);
            List<Policy> tempPolicyList = this.getPolicies();

            for (Policy policy : tempPolicyList) {
                for (Integer i : policyIdList) {
                    if (policy.getId() == i) {
                        policies.add(policy);
                    }
                }
            }

        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while getting the policies.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return policies;
    }

    @Override
    public List<Device> getPolicyAppliedDevicesIds(int policyId) throws PolicyManagementException {

        List<Device> deviceList = new ArrayList<Device>();
        List<Integer> deviceIdList;

        try {
            deviceIdList = policyDAO.getPolicyAppliedDevicesIds(policyId);
            for (Integer integer : deviceIdList) {
                deviceList.add(deviceDAO.getDevice(integer));
            }

        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while getting the device ids related to policy id (" + policyId + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while getting the devices related to policy id (" + policyId + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return deviceList;
    }

    @Override
    public void addAppliedPolicyToDevice(DeviceIdentifier deviceIdentifier, int policyId, List<ProfileFeature> profileFeatures) throws
            PolicyManagementException {
        int deviceId = -1;
        try {
            Device device = deviceDAO.getDevice(deviceIdentifier);
            deviceId = device.getId();
            boolean exist = policyDAO.checkPolicyAvailable(deviceId);
            PolicyManagementDAOFactory.beginTransaction();
            if (exist) {
                policyDAO.updateEffectivePolicyToDevice(deviceId, policyId, profileFeatures);
            } else {
                policyDAO.addEffectivePolicyToDevice(deviceId, policyId, profileFeatures);
            }
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Error occurred while adding the evaluated policy to device (" +
                    deviceId + " - " + policyId + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while getting the device details (" + deviceIdentifier.getId() + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }

    }

    @Override
    public boolean checkPolicyAvailable(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {

        boolean exist;
        try {
            Device device = deviceDAO.getDevice(deviceIdentifier);
            exist = policyDAO.checkPolicyAvailable(device.getId());
        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while checking whether device has a policy to apply.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while getting the device details (" + deviceIdentifier.getId() + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return exist;
    }

    @Override
    public boolean setPolicyApplied(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {

        try {
            Device device = deviceDAO.getDevice(deviceIdentifier);
            policyDAO.setPolicyApplied(device.getId());
            return true;
        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while setting the policy has applied to device (" +
                    deviceIdentifier.getId() + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while getting the device details (" + deviceIdentifier.getId() + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public int getPolicyCount() throws PolicyManagementException {
        int policyCount = 0;
        try {
            policyCount = policyDAO.getPolicyCount();
            return policyCount;
        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while getting policy count";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }
}
