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


package org.wso2.carbon.policy.mgt.core.mgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceData;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceDecisionPoint;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceFeature;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.ProfileFeature;
import org.wso2.carbon.policy.mgt.common.spi.PolicyMonitoringService;
import org.wso2.carbon.policy.mgt.core.dao.MonitoringDAO;
import org.wso2.carbon.policy.mgt.core.dao.MonitoringDAOException;
import org.wso2.carbon.policy.mgt.core.dao.PolicyDAO;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagerDAOException;
import org.wso2.carbon.policy.mgt.core.impl.ComplianceDecisionPointImpl;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mgt.MonitoringManager;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import java.util.List;

public class MonitoringManagerImpl implements MonitoringManager {

    private PolicyDAO policyDAO;
    private DeviceDAO deviceDAO;
    private MonitoringDAO monitoringDAO;
    private ComplianceDecisionPoint complianceDecisionPoint;

    private static final Log log = LogFactory.getLog(MonitoringManagerImpl.class);

    public MonitoringManagerImpl() {
        this.policyDAO = PolicyManagementDAOFactory.getPolicyDAO();
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        this.monitoringDAO = PolicyManagementDAOFactory.getMonitoringDAO();
        this.complianceDecisionPoint = new ComplianceDecisionPointImpl();
    }

    @Override
    public List<ComplianceFeature> checkPolicyCompliance(DeviceIdentifier deviceIdentifier,
                                                         Object deviceResponse) throws PolicyComplianceException {

        List<ComplianceFeature> complianceFeatures;
        try {
            PolicyManagementDAOFactory.beginTransaction();
            int tenantId = PolicyManagerUtil.getTenantId();

            Device device = deviceDAO.getDevice(deviceIdentifier, tenantId);
            Policy policy = policyDAO.getAppliedPolicy(device.getId());
            PolicyMonitoringService monitoringService = PolicyManagementDataHolder.getInstance().
                    getPolicyMonitoringService(deviceIdentifier.getType());

            ComplianceData complianceData = monitoringService.checkPolicyCompliance(deviceIdentifier,
                    policy, deviceResponse);
            complianceData.setPolicy(policy);
            complianceFeatures = complianceData.getComplianceFeatures();

            if (!complianceFeatures.isEmpty()) {
                int complianceId = monitoringDAO.setDeviceAsNoneCompliance(device.getId(), policy.getId());
                monitoringDAO.addNoneComplianceFeatures(complianceId, device.getId(), complianceFeatures);
                complianceDecisionPoint.validateDevicePolicyCompliance(deviceIdentifier, complianceData);
                List<ProfileFeature> profileFeatures = policy.getProfile().getProfileFeaturesList();
                for (ComplianceFeature compFeature : complianceFeatures) {
                    for (ProfileFeature profFeature : profileFeatures) {
                        if (profFeature.getFeatureCode().equalsIgnoreCase(compFeature.getFeatureCode())) {
                            compFeature.setFeature(profFeature);
                        }
                    }
                }

            } else {
                monitoringDAO.setDeviceAsCompliance(device.getId(), policy.getId());
            }
            PolicyManagementDAOFactory.commitTransaction();

        } catch (DeviceManagementDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Unable tor retrieve device data from DB for " + deviceIdentifier.getId() + " - " +
                    deviceIdentifier.getType();
            log.error(msg, e);
            throw new PolicyComplianceException(msg, e);
        } catch (PolicyManagerDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Unable tor retrieve policy data from DB for device " + deviceIdentifier.getId() + " - " +
                    deviceIdentifier.getType();
            log.error(msg, e);
            throw new PolicyComplianceException(msg, e);
        } catch (MonitoringDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Unable to add the none compliance features to database for device " + deviceIdentifier.
                    getId() + " - " + deviceIdentifier.getType();
            log.error(msg, e);
            throw new PolicyComplianceException(msg, e);
        }
        return complianceFeatures;
    }


    @Override
    public boolean isCompliance(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException {

        try {
            int tenantId = PolicyManagerUtil.getTenantId();
            Device device = deviceDAO.getDevice(deviceIdentifier, tenantId);
            ComplianceData complianceData = monitoringDAO.getCompliance(device.getId());
            if (complianceData == null || !complianceData.isStatus()) {
                return false;
            }

        } catch (DeviceManagementDAOException e) {
            String msg = "Unable to retrieve device data for " + deviceIdentifier.getId() + " - " +
                    deviceIdentifier.getType();
            log.error(msg, e);
            throw new PolicyComplianceException(msg, e);

        } catch (MonitoringDAOException e) {
            String msg = "Unable to retrieve compliance status for " + deviceIdentifier.getId() + " - " +
                    deviceIdentifier.getType();
            log.error(msg, e);
            throw new PolicyComplianceException(msg, e);
        }
        return true;
    }

    @Override
    public ComplianceData getDevicePolicyCompliance(DeviceIdentifier deviceIdentifier) throws
            PolicyComplianceException {

        ComplianceData complianceData;
        try {
            int tenantId = PolicyManagerUtil.getTenantId();
            Device device = deviceDAO.getDevice(deviceIdentifier, tenantId);
            complianceData = monitoringDAO.getCompliance(device.getId());
            List<ComplianceFeature> complianceFeatures =
                    monitoringDAO.getNoneComplianceFeatures(complianceData.getId());
            complianceData.setComplianceFeatures(complianceFeatures);

        } catch (DeviceManagementDAOException e) {
            String msg = "Unable to retrieve device data for " + deviceIdentifier.getId() + " - " +
                    deviceIdentifier.getType();
            log.error(msg, e);
            throw new PolicyComplianceException(msg, e);

        } catch (MonitoringDAOException e) {
            String msg = "Unable to retrieve compliance data for " + deviceIdentifier.getId() + " - " +
                    deviceIdentifier.getType();
            log.error(msg, e);
            throw new PolicyComplianceException(msg, e);
        }
        return complianceData;
    }
}
