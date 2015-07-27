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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
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
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import java.util.*;

public class MonitoringManagerImpl implements MonitoringManager {

    private PolicyDAO policyDAO;
    private DeviceDAO deviceDAO;
    private MonitoringDAO monitoringDAO;
    private ComplianceDecisionPoint complianceDecisionPoint;

    private static final Log log = LogFactory.getLog(MonitoringManagerImpl.class);
    private static final String OPERATION_MONITOR = "MONITOR";

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

            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            PolicyManager manager = new PolicyManagerImpl();

            Device device = service.getDevice(deviceIdentifier);
            Policy policy = manager.getAppliedPolicyToDevice(deviceIdentifier); //policyDAO.getAppliedPolicy(device
            // .getId());
            PolicyMonitoringService monitoringService = PolicyManagementDataHolder.getInstance().
                    getPolicyMonitoringService(deviceIdentifier.getType());

            ComplianceData complianceData = monitoringService.checkPolicyCompliance(deviceIdentifier,
                    policy, deviceResponse);
            complianceData.setPolicy(policy);
            complianceFeatures = complianceData.getComplianceFeatures();
            complianceData.setDeviceId(device.getId());
            complianceData.setPolicyId(policy.getId());

            if (!complianceFeatures.isEmpty()) {
                int complianceId = monitoringDAO.setDeviceAsNoneCompliance(device.getId(), policy.getId());
                complianceData.setId(complianceId);
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
                int complianceId = monitoringDAO.setDeviceAsCompliance(device.getId(), policy.getId());
                complianceData.setId(complianceId);
                monitoringDAO.deleteNoneComplianceData(complianceId);
            }
            PolicyManagementDAOFactory.commitTransaction();

        } catch (DeviceManagementException e) {
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
        } catch (PolicyManagementException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Unable tor retrieve policy data from DB for device " + deviceIdentifier.getId() + " - " +
                    deviceIdentifier.getType();
            log.error(msg, e);
            throw new PolicyComplianceException(msg, e);
        }
        return complianceFeatures;
    }


    @Override
    public boolean isCompliance(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException {

        try {
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            Device device = service.getDevice(deviceIdentifier);
            //deviceDAO.getDevice(deviceIdentifier, tenantId);
            ComplianceData complianceData = monitoringDAO.getCompliance(device.getId());
            if (complianceData == null || !complianceData.isStatus()) {
                return false;
            }

        } catch (DeviceManagementException e) {
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
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            Device device = service.getDevice(deviceIdentifier);
            complianceData = monitoringDAO.getCompliance(device.getId());
            List<ComplianceFeature> complianceFeatures =
                    monitoringDAO.getNoneComplianceFeatures(complianceData.getId());
            complianceData.setComplianceFeatures(complianceFeatures);

        } catch (DeviceManagementException e) {
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

    @Override
    public void addMonitoringOperation(List<Device> devices) throws PolicyComplianceException {

        try {
            PolicyManagementDAOFactory.beginTransaction();

            ComplianceDecisionPoint decisionPoint = new ComplianceDecisionPointImpl();

            //int tenantId = PolicyManagerUtil.getTenantId();
            Map<Integer, Device> deviceIds = new HashMap<>();

            for (Device device : devices) {
                deviceIds.put(device.getId(), device);
            }

            List<Integer> deviceIDs = new ArrayList<>(deviceIds.keySet());
            List<ComplianceData> complianceDatas = monitoringDAO.getCompliance(deviceIDs);
            HashMap<Integer, Integer> devicePolicyIdMap = policyDAO.getAppliedPolicyIds(deviceIDs);

            Map<Integer, Device> deviceIdsToAddOperation = new HashMap<>();
            Map<Integer, Device> deviceIdsWithExistingOperation = new HashMap<>();
            Map<Integer, Device> inactiveDeviceIds = new HashMap<>();
            Map<Integer, Integer> firstTimeDeviceId = new HashMap<>();

            Map<Integer, ComplianceData> tempMap = new HashMap<>();


            if (complianceDatas != null || !complianceDatas.isEmpty()) {
                for (ComplianceData complianceData : complianceDatas) {

                    tempMap.put(complianceData.getDeviceId(), complianceData);

                    if (complianceData.getAttempts() == 0) {
                        deviceIdsToAddOperation.put(complianceData.getDeviceId(),
                                deviceIds.get(complianceData.getDeviceId()));
                    } else {
                        deviceIdsWithExistingOperation.put(complianceData.getDeviceId(),
                                deviceIds.get(complianceData.getDeviceId()));
                    }
                    if (complianceData.getAttempts() >= 20) {
                        inactiveDeviceIds.put(complianceData.getDeviceId(),
                                deviceIds.get(complianceData.getDeviceId()));
                    }
                }
            }

            for (Device device : devices) {
                if (!tempMap.containsKey(device.getId())) {
                    deviceIdsToAddOperation.put(device.getId(), device);
                    firstTimeDeviceId.put(device.getId(), devicePolicyIdMap.get(device.getId()));
                }
            }

            if (!deviceIdsToAddOperation.isEmpty()) {
                this.addMonitoringOperationsToDatabase(new ArrayList<>(deviceIdsToAddOperation.values()));
                monitoringDAO.addComplianceDetails(firstTimeDeviceId);
            }

            if (!deviceIdsWithExistingOperation.isEmpty()) {
                monitoringDAO.updateAttempts(new ArrayList<>(deviceIdsWithExistingOperation.keySet()), false);
                decisionPoint.setDevicesAsUnreachable(this.getDeviceIdentifiersFromDevices(
                        new ArrayList<>(deviceIdsWithExistingOperation.values())));
            }

            PolicyManagementDAOFactory.commitTransaction();

        } catch (MonitoringDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Error occurred from monitoring dao.";
            log.error(msg, e);
            throw new PolicyComplianceException(msg, e);
        } catch (OperationManagementException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Error occurred while adding monitoring operation to devices";
            log.error(msg, e);
            throw new PolicyComplianceException(msg, e);
        } catch (PolicyManagerDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Error occurred while roll backing the transaction.");
            }
            String msg = "Error occurred reading the applied policies to devices.";
            log.error(msg, e);
            throw new PolicyComplianceException(msg, e);
        }

    }


    private void addMonitoringOperationsToDatabase(List<Device> devices)
            throws PolicyComplianceException, OperationManagementException {

        List<DeviceIdentifier> deviceIdentifiers = this.getDeviceIdentifiersFromDevices(devices);
        CommandOperation monitoringOperation = new CommandOperation();
        monitoringOperation.setEnabled(true);
        monitoringOperation.setType(Operation.Type.COMMAND);
        monitoringOperation.setCode(OPERATION_MONITOR);

        DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
        service.addOperation(monitoringOperation, deviceIdentifiers);
//        PolicyManagementDataHolder.getInstance().getDeviceManagementService().
//                addOperation(monitoringOperation, deviceIdentifiers);
    }

    private List<DeviceIdentifier> getDeviceIdentifiersFromDevices(List<Device> devices) {

        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        for (Device device : devices) {
            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(device.getDeviceIdentifier());
            identifier.setType(device.getType());

            deviceIdentifiers.add(identifier);
        }
        return deviceIdentifiers;
    }

}
