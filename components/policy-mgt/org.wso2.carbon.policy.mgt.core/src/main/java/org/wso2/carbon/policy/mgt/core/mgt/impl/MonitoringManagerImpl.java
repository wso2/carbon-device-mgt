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
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.common.ProfileFeature;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceData;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceDecisionPoint;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceFeature;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.common.spi.PolicyMonitoringService;
import org.wso2.carbon.policy.mgt.core.dao.*;
import org.wso2.carbon.policy.mgt.core.impl.ComplianceDecisionPointImpl;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mgt.MonitoringManager;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        List<ComplianceFeature> complianceFeatures = new ArrayList<>();
        try {
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            PolicyManager manager = new PolicyManagerImpl();
            Device device = service.getDevice(deviceIdentifier);
            Policy policy = manager.getAppliedPolicyToDevice(deviceIdentifier);
            if (policy != null) {
                PolicyMonitoringService monitoringService = PolicyManagementDataHolder.getInstance().
                        getPolicyMonitoringService(deviceIdentifier.getType());

                ComplianceData complianceData;
                // This was retrieved from database because compliance id must be present for other dao operations to
                // run.
                try {
                    PolicyManagementDAOFactory.openConnection();
                    ComplianceData cmd = monitoringDAO.getCompliance(device.getId());
                    complianceData = monitoringService.checkPolicyCompliance(deviceIdentifier,
                            policy, deviceResponse);

                    complianceData.setId(cmd.getId());
                    complianceData.setPolicy(policy);
                    complianceFeatures = complianceData.getComplianceFeatures();
                    complianceData.setDeviceId(device.getId());
                    complianceData.setPolicyId(policy.getId());
                } catch (SQLException e) {
                    throw new PolicyComplianceException("Error occurred while opening a data source connection", e);
                } finally {
                    PolicyManagementDAOFactory.closeConnection();
                }

                //This was added because update query below that did not return the update table primary key.

                if (complianceFeatures != null && !complianceFeatures.isEmpty()) {
                    try {
                        PolicyManagementDAOFactory.beginTransaction();
                        monitoringDAO.setDeviceAsNoneCompliance(device.getId(), policy.getId());
                        if (log.isDebugEnabled()) {
                            log.debug("Compliance status primary key " + complianceData.getId());
                        }
                        monitoringDAO.addNoneComplianceFeatures(complianceData.getId(), device.getId(),
                                complianceFeatures);

                        PolicyManagementDAOFactory.commitTransaction();
                    } finally {
                        PolicyManagementDAOFactory.closeConnection();
                    }
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
                    try {
                        PolicyManagementDAOFactory.beginTransaction();
                        monitoringDAO.setDeviceAsCompliance(device.getId(), policy.getId());
                        monitoringDAO.deleteNoneComplianceData(complianceData.getId());
                        PolicyManagementDAOFactory.commitTransaction();
                    } finally {
                        PolicyManagementDAOFactory.closeConnection();
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("There is no policy applied to this device, hence compliance monitoring was not called.");
                }
            }
        } catch (DeviceManagementException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyComplianceException("Unable tor retrieve device data from DB for " +
                    deviceIdentifier.getId() + " - " + deviceIdentifier.getType(), e);
        } catch (PolicyManagerDAOException | PolicyManagementException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyComplianceException("Unable tor retrieve policy data from DB for device " +
                    deviceIdentifier.getId() + " - " + deviceIdentifier.getType(), e);
        } catch (MonitoringDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyComplianceException("Unable to add the none compliance features to database for device " +
                    deviceIdentifier.getId() + " - " + deviceIdentifier.getType(), e);
        }
        return complianceFeatures;
    }

    @Override
    public boolean isCompliance(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException {
        try {
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            Device device = service.getDevice(deviceIdentifier);
            PolicyManagementDAOFactory.openConnection();
            ComplianceData complianceData = monitoringDAO.getCompliance(device.getId());
            if (complianceData == null || !complianceData.isStatus()) {
                return false;
            }
        } catch (DeviceManagementException e) {
            throw new PolicyComplianceException("Unable to retrieve device data for " + deviceIdentifier.getId() +
                    " - " + deviceIdentifier.getType(), e);

        } catch (MonitoringDAOException e) {
            throw new PolicyComplianceException("Unable to retrieve compliance status for " + deviceIdentifier.getId() +
                    " - " + deviceIdentifier.getType(), e);
        } catch (SQLException e) {
            throw new PolicyComplianceException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return true;
    }

    @Override
    public ComplianceData getDevicePolicyCompliance(DeviceIdentifier deviceIdentifier) throws
            PolicyComplianceException {

        ComplianceData complianceData;
        try {
            PolicyManagementDAOFactory.openConnection();
            DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
            Device device = service.getDevice(deviceIdentifier);
            complianceData = monitoringDAO.getCompliance(device.getId());
            List<ComplianceFeature> complianceFeatures =
                    monitoringDAO.getNoneComplianceFeatures(complianceData.getId());
            complianceData.setComplianceFeatures(complianceFeatures);

        } catch (DeviceManagementException e) {
            throw new PolicyComplianceException("Unable to retrieve device data for " + deviceIdentifier.getId() +
                    " - " + deviceIdentifier.getType(), e);

        } catch (MonitoringDAOException e) {
            throw new PolicyComplianceException("Unable to retrieve compliance data for " + deviceIdentifier.getId() +
                    " - " + deviceIdentifier.getType(), e);
        } catch (SQLException e) {
            throw new PolicyComplianceException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return complianceData;
    }

    @Override
    public void addMonitoringOperation(List<Device> devices) throws PolicyComplianceException {

        ComplianceDecisionPoint decisionPoint = new ComplianceDecisionPointImpl();

        //int tenantId = PolicyManagerUtil.getTenantId();
        Map<Integer, Device> deviceIds = new HashMap<>();
        List<ComplianceData> complianceDatas;
        HashMap<Integer, Integer> devicePolicyIdMap;

        for (Device device : devices) {
            deviceIds.put(device.getId(), device);
        }

        List<Integer> deviceIDs = new ArrayList<>(deviceIds.keySet());
        try {
            PolicyManagementDAOFactory.openConnection();
            complianceDatas = monitoringDAO.getCompliance(deviceIDs);
            devicePolicyIdMap = policyDAO.getAppliedPolicyIds(deviceIDs);
        } catch (SQLException e) {
            throw new PolicyComplianceException("SQL error occurred while getting monitoring details.", e);
        } catch (MonitoringDAOException e) {
            throw new PolicyComplianceException("SQL error occurred while getting monitoring details.", e);
        } catch (PolicyManagerDAOException e) {
            throw new PolicyComplianceException("SQL error occurred while getting policy details.", e);
        }

        Map<Integer, Device> deviceIdsToAddOperation = new HashMap<>();
        Map<Integer, Device> deviceIdsWithExistingOperation = new HashMap<>();
        Map<Integer, Device> inactiveDeviceIds = new HashMap<>();
        Map<Integer, Integer> firstTimeDeviceIdsWithPolicyIds = new HashMap<>();

        Map<Integer, ComplianceData> tempMap = new HashMap<>();

        try {
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
                if ((!tempMap.containsKey(device.getId())) && (devicePolicyIdMap.containsKey(device.getId()))) {
                    deviceIdsToAddOperation.put(device.getId(), device);
                    firstTimeDeviceIdsWithPolicyIds.put(device.getId(), devicePolicyIdMap.get(device.getId()));
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("These devices are in the system for the first time");
                for (Map.Entry<Integer, Integer> map : firstTimeDeviceIdsWithPolicyIds.entrySet()) {
                    log.debug("First time device primary key : " + map.getKey() + " & policy id " + map.getValue());
                }
            }

            PolicyManagementDAOFactory.beginTransaction();

            if (!deviceIdsToAddOperation.isEmpty()) {
                monitoringDAO.addComplianceDetails(firstTimeDeviceIdsWithPolicyIds);
            }

            if (!deviceIdsWithExistingOperation.isEmpty()) {
                monitoringDAO.updateAttempts(new ArrayList<>(deviceIdsWithExistingOperation.keySet()), false);
                //TODO: Add attempts. This has to be fixed in the get pending operation tables too. This will be
//                decisionPoint.setDevicesAsUnreachable(this.getDeviceIdentifiersFromDevices(
//                        new ArrayList<>(deviceIdsWithExistingOperation.values())));
            }
            PolicyManagementDAOFactory.commitTransaction();

        } catch (MonitoringDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyComplianceException("Error occurred from monitoring dao.", e);
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyComplianceException("Error occurred reading the applied policies to devices.", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }

        if (!deviceIdsToAddOperation.isEmpty()) {
            try {
                this.addMonitoringOperationsToDatabase(new ArrayList<>(deviceIdsToAddOperation.values()));
            } catch (OperationManagementException e) {
                throw new PolicyComplianceException("Error occurred while adding monitoring operation to devices", e);
            }
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
