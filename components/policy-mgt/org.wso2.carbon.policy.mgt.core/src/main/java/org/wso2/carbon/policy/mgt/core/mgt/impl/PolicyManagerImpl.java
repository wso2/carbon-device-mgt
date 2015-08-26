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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.policy.mgt.core.dao.*;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.policy.mgt.core.mgt.ProfileManager;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while updating the policy ("
                    + policy.getId() + " - " + policy.getPolicyName() + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policy;
    }

    @Override
    public boolean updatePolicyPriorities(List<Policy> policies) throws PolicyManagementException {
        boolean bool;
        try {
            PolicyManagementDAOFactory.beginTransaction();
            bool = policyDAO.updatePolicyPriorities(policies);
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
        try {
            PolicyManagementDAOFactory.beginTransaction();

            Policy policy = policyDAO.getPolicy(policyId);
            policyDAO.deleteAllPolicyRelatedConfigs(policyId);
            policyDAO.deletePolicy(policyId);

            if (log.isDebugEnabled()) {
                log.debug("Profile ID: " + policy.getProfileId());
            }

            featureDAO.deleteFeaturesOfProfile(policy.getProfileId());

            profileDAO.deleteProfile(policy.getProfileId());
            PolicyManagementDAOFactory.commitTransaction();
            return true;
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
    public Policy addPolicyToDevice(List<DeviceIdentifier> deviceIdentifierList,
                                    Policy policy) throws PolicyManagementException {
        try {
            PolicyManagementDAOFactory.beginTransaction();
            if (policy.getId() == 0) {
                policyDAO.addPolicy(policy);
            }
            List<Device> deviceList = new ArrayList<>();
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            for (DeviceIdentifier deviceIdentifier : deviceIdentifierList) {
                deviceList.add(service.getDevice(deviceIdentifier));
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
        } catch (DeviceManagementException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the policy to device list", e);
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
        return policy;
    }

    @Override
    public Policy getPolicy(int policyId) throws PolicyManagementException {

        Policy policy;
        List<Device> deviceList;
        List<String> roleNames;

        try {
            PolicyManagementDAOFactory.openConnection();
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
            throw new PolicyManagementException("Error occurred while getting the policy related to policy ID (" +
                    policyId + ")", e);
        } catch (ProfileManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the profile related to policy ID (" +
                    policyId + ")", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
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
//            List<Profile> profileList = profileDAO.getAllProfiles();

            for (Policy policy : policyList) {
                for (Profile profile : profileList) {
                    if (policy.getProfileId() == profile.getProfileId()) {
                        policy.setProfile(profile);
                    }
                }
                policy.setDevices(getPolicyAppliedDevicesIds(policy.getId()));
                policy.setRoles(policyDAO.getPolicyAppliedRoles(policy.getId()));
                policy.setUsers(policyDAO.getPolicyAppliedUsers(policy.getId()));
                policy.setPolicyCriterias(policyDAO.getPolicyCriteria(policy.getId()));
//                policyDAO.getDatesOfPolicy(policy);
//                policyDAO.getTimesOfPolicy(policy);
//                policyDAO.getLocationsOfPolicy(policy);
            }
            Collections.sort(policyList);
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting all the policies.", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policyList;
    }

    @Override
    public List<Policy> getPoliciesOfDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {

        List<Integer> policyIdList;
        List<Policy> policies = new ArrayList<>();
        try {
            PolicyManagementDAOFactory.openConnection();
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            Device device = service.getDevice(deviceIdentifier);
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

        List<Policy> tempPolicyList = this.getPolicies();

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
        try {
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
            Collections.sort(policies);
        } catch (ProfileManagementException e) {
            throw new PolicyManagementException("Error occurred while getting all the profile features.", e);
        }
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

        List<Policy> tempPolicyList = this.getPolicies();

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
        List<Policy> tempPolicyList = this.getPolicies();

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
            PolicyManagementDAOFactory.openConnection();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            deviceIds = policyDAO.getPolicyAppliedDevicesIds(policyId);
            for (int deviceId : deviceIds) {
                //TODO FIX ME
                deviceList.add(deviceDAO.getDevice(new DeviceIdentifier(Integer.toString(deviceId), ""), tenantId));
            }
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the device ids related to policy id (" +
                    policyId + ")", e);
        } catch (DeviceManagementDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the devices related to policy id (" +
                    policyId + ")", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
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
            boolean exist = policyDAO.checkPolicyAvailable(deviceId);
            if (exist) {
                policyDAO.updateEffectivePolicyToDevice(deviceId, policy);
            } else {
                policyDAO.addEffectivePolicyToDevice(deviceId, policy);
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
    public void addAppliedPolicyToDevice(DeviceIdentifier deviceIdentifier, Policy policy)
            throws PolicyManagementException {

        int deviceId = -1;
        try {
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            Device device = service.getDevice(deviceIdentifier);
            deviceId = device.getId();
            // boolean exist = policyDAO.checkPolicyAvailable(deviceId);

            PolicyManagementDAOFactory.beginTransaction();

            Policy policySaved = policyDAO.getAppliedPolicy(deviceId);
            if (policySaved != null && policySaved.getId() != 0) {
                if (policy.getId() != policySaved.getId()){
                    policyDAO.updateEffectivePolicyToDevice(deviceId, policy);
                }
            } else {
                policyDAO.addEffectivePolicyToDevice(deviceId, policy);
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
    public boolean checkPolicyAvailable(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {

        boolean exist;
        try {
            PolicyManagementDAOFactory.openConnection();
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            Device device = service.getDevice(deviceIdentifier);
            exist = policyDAO.checkPolicyAvailable(device.getId());
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
            PolicyManagementDAOFactory.openConnection();
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            Device device = service.getDevice(deviceIdentifier);
            policyDAO.setPolicyApplied(device.getId());
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
    public Policy getAppliedPolicyToDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        Policy policy;
        try {
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            Device device = service.getDevice(deviceIdentifier);
            //int policyId = policyDAO.getAppliedPolicyId(device.getId());
            PolicyManagementDAOFactory.openConnection();
            policy = policyDAO.getAppliedPolicy(device.getId());
        } catch (DeviceManagementException e) {
            throw new PolicyManagementException("Error occurred while getting device id.", e);
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting policy id or policy.", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policy;
    }

}
