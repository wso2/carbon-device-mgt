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

package org.wso2.carbon.policy.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderServiceImpl;
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mgt.FeatureManager;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.policy.mgt.core.mgt.impl.FeatureManagerImpl;
import org.wso2.carbon.policy.mgt.core.mgt.impl.PolicyManagerImpl;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolicyInformationPointImpl implements PolicyInformationPoint {

    private static final Log log = LogFactory.getLog(PolicyInformationPointImpl.class);

    PolicyManager policyManager;
    FeatureManager featureManager;
    DeviceManagementProviderService deviceManagementService;

    public PolicyInformationPointImpl() {
        deviceManagementService =
                PolicyManagementDataHolder.getInstance().getDeviceManagementService();
        policyManager = new PolicyManagerImpl();
        featureManager = new FeatureManagerImpl();
    }

    @Override
    public PIPDevice getDeviceData(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        PIPDevice pipDevice = new PIPDevice();
        Device device;
        DeviceType deviceType = new DeviceType();
        deviceType.setName(deviceIdentifier.getType());
        DeviceManagementProviderService deviceManagementService = new DeviceManagementProviderServiceImpl();
        GroupManagementProviderService groupManagementProviderService = new GroupManagementProviderServiceImpl();

        try {
            device = deviceManagementService.getDevice(deviceIdentifier);

            if (device != null) {
                pipDevice.setDevice(device);
                pipDevice.setRoles(getRoleOfDevice(device));
                pipDevice.setDeviceType(deviceType);
                pipDevice.setDeviceIdentifier(deviceIdentifier);
                pipDevice.setUserId(device.getEnrolmentInfo().getOwner());
                pipDevice.setOwnershipType(device.getEnrolmentInfo().getOwnership().toString());
                pipDevice.setDeviceGroups(groupManagementProviderService.getGroups(pipDevice.getDeviceIdentifier()));

            } else {
                throw new PolicyManagementException("Device details cannot be null.");
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred when retrieving the data related to device from the database.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (GroupManagementException e) {
            String msg = "Error occurred when retrieving the data related to device groups from the database.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return pipDevice;
    }

    @Override
    public List<Policy> getRelatedPolicies(PIPDevice pipDevice) throws PolicyManagementException {

        List<Policy> policies = policyManager.getPoliciesOfDeviceType(pipDevice.getDeviceType().getName());
        PolicyFilter policyFilter = new PolicyFilterImpl();

        if (log.isDebugEnabled()) {
            log.debug("No of policies for the device type : " + pipDevice.getDeviceType().getName() + " : " +
                    policies.size());
            for (Policy policy : policies) {
                log.debug("Names of policy for above device type : " + policy.getPolicyName());
            }
        }

        policies = policyFilter.filterActivePolicies(policies);

        if (pipDevice.getDeviceType() != null) {
            policies = policyFilter.filterDeviceTypeBasedPolicies(pipDevice.getDeviceType().getName(), policies);
        }
        if (pipDevice.getOwnershipType() != null && !pipDevice.getOwnershipType().isEmpty()) {
            policies = policyFilter.filterOwnershipTypeBasedPolicies(pipDevice.getOwnershipType(), policies);
        }
        if (pipDevice.getRoles() != null) {
            policies = policyFilter.filterRolesBasedPolicies(pipDevice.getRoles(), policies);
        }
        if (pipDevice.getUserId() != null && !pipDevice.getUserId().isEmpty()) {
            policies = policyFilter.filterUserBasedPolicies(pipDevice.getUserId(), policies);
        }
        if (pipDevice.getDeviceGroups() != null && !pipDevice.getDeviceGroups().isEmpty()) {

            Map<Integer, DeviceGroup> groupMap = new HashMap<>();
            List<DeviceGroup> groups = pipDevice.getDeviceGroups();
            for(DeviceGroup gr: groups){
                groupMap.put(gr.getGroupId(), gr);
            }
            policies = policyFilter.filterDeviceGroupsPolicies(groupMap, policies);
        }

        if (log.isDebugEnabled()) {
            log.debug("No of policies selected for the device type : " + pipDevice.getDeviceType().getName() + " : " +
                    policies.size());
            for (Policy policy : policies) {
                log.debug("Names of selected policy  for above device type : " + policy.getPolicyName());
            }
        }

        return policies;
    }

    @Override
    public List<Feature> getRelatedFeatures(String deviceType) throws FeatureManagementException {
        return featureManager.getAllFeatures(deviceType);

    }

    private String[] getRoleOfDevice(Device device) throws PolicyManagementException {
        try {
            UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            if (userRealm != null) {
                return userRealm.getUserStoreManager().getRoleListOfUser(device.getEnrolmentInfo().getOwner());
            } else {
                return null;
            }
        } catch (UserStoreException e) {
            throw new PolicyManagementException("Error occurred when retrieving roles related to user name.", e);
        }
    }


    private List<Policy> removeDuplicatePolicies(List<List<Policy>> policies) {

        Map<Integer, Policy> map = new HashMap<Integer, Policy>();
        List<Policy> finalPolicies = new ArrayList<Policy>();
        for (List<Policy> policyList : policies) {
            for (Policy policy : policyList) {
                if (!map.containsKey(policy.getId())) {
                    map.put(policy.getId(), policy);
                    finalPolicies.add(policy);
                }
            }
        }
        return finalPolicies;
    }

    private DeviceManagementProviderService getDeviceManagementService() {
        return new DeviceManagementProviderServiceImpl();
    }

}
