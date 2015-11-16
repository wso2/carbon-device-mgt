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
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.policy.PolicyConfiguration;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.common.ProfileFeature;
import org.wso2.carbon.policy.mgt.common.monitor.*;
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
    //    private DeviceDAO deviceDAO;
    private DeviceTypeDAO deviceTypeDAO;
    private MonitoringDAO monitoringDAO;
    private ComplianceDecisionPoint complianceDecisionPoint;
    private PolicyConfiguration policyConfiguration;

    private static final Log log = LogFactory.getLog(MonitoringManagerImpl.class);
    private static final String OPERATION_MONITOR = "MONITOR";
    private static final String OPERATION_INFO = "DEVICE_INFO";
    private static final String OPERATION_APP_LIST = "APPLICATION_LIST";

    public MonitoringManagerImpl() {
        this.policyDAO = PolicyManagementDAOFactory.getPolicyDAO();
//        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        this.deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
        this.monitoringDAO = PolicyManagementDAOFactory.getMonitoringDAO();
        this.complianceDecisionPoint = new ComplianceDecisionPointImpl();
        this.policyConfiguration = DeviceConfigurationManager.getInstance().getDeviceManagementConfig().
                getDeviceManagementConfigRepository().getPolicyConfiguration();

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
                    ComplianceData cmd = monitoringDAO.getCompliance(device.getId(), device.getEnrolmentInfo().getId());
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
                        monitoringDAO.setDeviceAsNoneCompliance(device.getId(), device.getEnrolmentInfo().getId(),
                                policy.getId());
                        if (log.isDebugEnabled()) {
                            log.debug("Compliance status primary key " + complianceData.getId());
                        }
                        monitoringDAO.addNonComplianceFeatures(complianceData.getId(), device.getId(),
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
                        monitoringDAO.setDeviceAsCompliance(device.getId(), device.getEnrolmentInfo().getId(), policy
                                .getId());
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
            ComplianceData complianceData = monitoringDAO.getCompliance(device.getId(), device.getEnrolmentInfo()
                    .getId());
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
            complianceData = monitoringDAO.getCompliance(device.getId(), device.getEnrolmentInfo().getId());
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

        //int tenantId = PolicyManagerUtil.getTenantId();
        Map<Integer, Device> deviceIds = new HashMap<>();
        List<ComplianceData> complianceDatas = new ArrayList<>();
        HashMap<Integer, Integer> devicePolicyIdMap = new HashMap<>();

        try {
            PolicyManagementDAOFactory.openConnection();
            List<ComplianceData> cd = monitoringDAO.getCompliance();

            for (Device device : devices) {
                deviceIds.put(device.getId(), device);

                for (ComplianceData data : cd) {
                    if (device.getId() == data.getDeviceId() && device.getEnrolmentInfo().getId() == data
                            .getEnrolmentId()) {
                        complianceDatas.add(data);
                    }
                }
            }
            List<Integer> deviceIDs = new ArrayList<>(deviceIds.keySet());

            HashMap<Integer, Integer> temp = policyDAO.getAppliedPolicyIds();
            for (Integer id : deviceIDs) {
                if (temp != null && !temp.isEmpty() && temp.containsKey(id)) {
                    devicePolicyIdMap.put(id, temp.get(id));
                }
            }

        } catch (SQLException e) {
            throw new PolicyComplianceException("SQL error occurred while getting monitoring details.", e);
        } catch (MonitoringDAOException e) {
            throw new PolicyComplianceException("SQL error occurred while getting monitoring details.", e);
        } catch (PolicyManagerDAOException e) {
            throw new PolicyComplianceException("SQL error occurred while getting policy details.", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }

        Map<Integer, Device> deviceIdsToAddOperation = new HashMap<>();
        Map<Integer, Device> deviceIdsWithExistingOperation = new HashMap<>();
        Map<Integer, Device> inactiveDeviceIds = new HashMap<>();
        Map<Integer, Device> deviceToMarkUnreachable = new HashMap<>();
        //Map<Integer, Integer> firstTimeDeviceIdsWithPolicyIds = new HashMap<>();

        List<PolicyDeviceWrapper> firstTimeDevices = new ArrayList<>();

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
                        if (complianceData.getAttempts() >= policyConfiguration.getMinRetriesToMarkUnreachable()) {
                            deviceToMarkUnreachable.put(complianceData.getDeviceId(),
                                    deviceIds.get(complianceData.getDeviceId()));
                        }
                    }
                    if (complianceData.getAttempts() >= policyConfiguration.getMinRetriesToMarkInactive()) {
                        inactiveDeviceIds.put(complianceData.getDeviceId(),
                                deviceIds.get(complianceData.getDeviceId()));
                    }
                }
            }

            for (Device device : devices) {
                if ((!tempMap.containsKey(device.getId())) && (devicePolicyIdMap.containsKey(device.getId()))) {
                    deviceIdsToAddOperation.put(device.getId(), device);

                    PolicyDeviceWrapper policyDeviceWrapper = new PolicyDeviceWrapper();
                    policyDeviceWrapper.setDeviceId(device.getId());
                    policyDeviceWrapper.setEnrolmentId(device.getEnrolmentInfo().getId());
                    policyDeviceWrapper.setPolicyId(devicePolicyIdMap.get(device.getId()));

                    firstTimeDevices.add(policyDeviceWrapper);

                    // firstTimeDeviceIdsWithPolicyIds.put(device.getId(), devicePolicyIdMap.get(device.getId()));
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("These devices are in the system for the first time");
                for (PolicyDeviceWrapper wrapper : firstTimeDevices) {
                    log.debug("First time device primary key : " + wrapper.getDeviceId() + " & policy id " +
                            wrapper.getPolicyId());
                }
            }

            PolicyManagementDAOFactory.beginTransaction();

            if (!deviceIdsToAddOperation.isEmpty()) {
//                monitoringDAO.addComplianceDetails(firstTimeDeviceIdsWithPolicyIds);
                monitoringDAO.addComplianceDetails(firstTimeDevices);
                monitoringDAO.updateAttempts(new ArrayList<>(deviceIdsToAddOperation.keySet()), false);
            }

            if (!deviceIdsWithExistingOperation.isEmpty()) {
                monitoringDAO.updateAttempts(new ArrayList<>(deviceIdsWithExistingOperation.keySet()), false);
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

        // TODO : This should be uncommented, this is to mark the device as unreachable, But given the current
        // implementation we are not able to do so.

//        if(!deviceToMarkUnreachable.isEmpty()) {
//        ComplianceDecisionPoint decisionPoint = new ComplianceDecisionPointImpl();
//            decisionPoint.setDevicesAsUnreachable(this.getDeviceIdentifiersFromDevices(
//                       new ArrayList<>(deviceToMarkUnreachable.values())));
//        }

    }

    @Override
    public List<DeviceType> getDeviceTypes() throws PolicyComplianceException {

        List<DeviceType> deviceTypes = new ArrayList<>();
        try {
            DeviceManagementDAOFactory.openConnection();
            deviceTypes = deviceTypeDAO.getDeviceTypes();
        } catch (Exception e) {
            log.error("Error occurred while getting the device types.", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return deviceTypes;
    }

    private void addMonitoringOperationsToDatabase(List<Device> devices)
            throws PolicyComplianceException, OperationManagementException {

        List<DeviceIdentifier> deviceIdentifiers = this.getDeviceIdentifiersFromDevices(devices);
        CommandOperation monitoringOperation = new CommandOperation();
        monitoringOperation.setEnabled(true);
        monitoringOperation.setType(Operation.Type.COMMAND);
        monitoringOperation.setCode(OPERATION_MONITOR);
//	    CommandOperation infoOperation = new CommandOperation();
//	    infoOperation.setEnabled(true);
//	    infoOperation.setType(Operation.Type.COMMAND);
//	    infoOperation.setCode(OPERATION_INFO);
//	    CommandOperation appListOperation = new CommandOperation();
//	    appListOperation.setEnabled(true);
//	    appListOperation.setType(Operation.Type.COMMAND);
//	    appListOperation.setCode(OPERATION_APP_LIST);

        DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
        service.addOperation(monitoringOperation, deviceIdentifiers);
//	    service.addOperation(infoOperation, deviceIdentifiers);
//	    service.addOperation(appListOperation, deviceIdentifiers);
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
