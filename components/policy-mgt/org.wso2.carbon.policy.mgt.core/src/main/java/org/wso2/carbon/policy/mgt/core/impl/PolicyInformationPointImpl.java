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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dao.impl.DeviceDAOImpl;
import org.wso2.carbon.device.mgt.core.dao.impl.DeviceTypeDAOImpl;
import org.wso2.carbon.device.mgt.core.dto.Device;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.policy.mgt.core.dao.*;
import org.wso2.carbon.policy.mgt.core.dao.impl.FeatureDAOImpl;
import org.wso2.carbon.policy.mgt.core.dao.impl.PolicyDAOImpl;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolicyInformationPointImpl implements PolicyInformationPoint {

    private static final Log log = LogFactory.getLog(PolicyInformationPointImpl.class);
    DeviceDAOImpl deviceDAO = new DeviceDAOImpl(PolicyManagementDAOFactory.getDataSource());
    DeviceTypeDAO deviceTypeDAO = new DeviceTypeDAOImpl(PolicyManagementDAOFactory.getDataSource());
    FeatureDAO featureDAO = new FeatureDAOImpl();
    PolicyDAO policyDAO = new PolicyDAOImpl();

    public PolicyInformationPointImpl() {
        deviceDAO = new DeviceDAOImpl(DeviceManagementDAOFactory.getDataSource());
    }

    @Override
    public PIPDevice getDeviceData(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        PIPDevice pipDevice = new PIPDevice();
        Device device;
        try {
            device = deviceDAO.getDevice(deviceIdentifier);
            DeviceType deviceType = deviceTypeDAO.getDeviceType(deviceIdentifier.getType());
            pipDevice.setDevice(device);
            pipDevice.setRoles(getRoleOfDevice(device));
            pipDevice.setDeviceType(deviceType);
            pipDevice.setDeviceIdentifier(deviceIdentifier);

            // TODO : Find a way to retrieve the timestamp and location (lat, long) of the device
            // pipDevice.setLongitude();
            // pipDevice.setAltitude();
            // pipDevice.setTimestamp();
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred when retrieving the data related to device from the database.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }

        return pipDevice;
    }

    @Override
    public List<Policy> getRelatedPolicies(PIPDevice pipDevice) throws PolicyManagementException {

        List<List<Policy>> policies = new ArrayList<List<Policy>>();
        try {
            // Get the device type related policies
            policies.add(policyDAO.getPolicy(pipDevice.getDeviceType().getName()));

            // Get the roles related policies
            for (String role : pipDevice.getRoles()) {
                policies.add(policyDAO.getPolicyOfRole(role));
            }
            // Get policy related to the device
            policies.add(policyDAO.getPolicy(pipDevice.getDeviceIdentifier()));

            return removeDuplicatePolicies(policies);
        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred when retrieving related to given device " +
                    pipDevice.getDeviceIdentifier().getId() + " " + pipDevice.getDeviceIdentifier().getType() + ".";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public List<Feature> getRelatedFeatures(String deviceType) throws FeatureManagementException {
        try {
            return featureDAO.getAllFeatures(deviceType);
        } catch (FeatureManagerDAOException e) {
            String msg = "Error occurred when retrieving features related  to device type.";
            log.error(msg, e);
            throw new FeatureManagementException(msg, e);
        }
    }

    private String[] getRoleOfDevice(Device device) throws PolicyManagementException {
        try {
            return CarbonContext.getThreadLocalCarbonContext().getUserRealm().
                    getUserStoreManager().getRoleListOfUser(device.getOwnerId());
        } catch (UserStoreException e) {
            String msg = "Error occurred when retrieving roles related to user name.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
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

}
