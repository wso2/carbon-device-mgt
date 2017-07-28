/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.status.task.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceStatusTaskPluginConfig;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationEnrolmentMapping;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.status.task.DeviceStatusTaskException;
import org.wso2.carbon.ntask.core.Task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This implements the Task service which monitors the device activity periodically & update the device-status if
 * necessary.
 */
public class DeviceStatusMonitoringTask implements Task {

    private static final Log log = LogFactory.getLog(DeviceStatusMonitoringTask.class);
    private String deviceType;
    private DeviceStatusTaskPluginConfig deviceStatusTaskPluginConfig;
    private int deviceTypeId = -1;

    @Override
    public void setProperties(Map<String, String> properties) {
        deviceType = properties.get(DeviceStatusTaskManagerServiceImpl.DEVICE_TYPE);
        deviceTypeId = Integer.parseInt(properties.get(DeviceStatusTaskManagerServiceImpl.DEVICE_TYPE_ID));
        String deviceStatusTaskConfigStr = properties.get(DeviceStatusTaskManagerServiceImpl.DEVICE_STATUS_TASK_CONFIG);
        Gson gson = new Gson();
        deviceStatusTaskPluginConfig = gson.fromJson(deviceStatusTaskConfigStr, DeviceStatusTaskPluginConfig.class);
    }

    @Override
    public void init() {

    }

    @Override
    public void execute() {
        List<OperationEnrolmentMapping> operationEnrolmentMappings = null;
        List<EnrolmentInfo> enrolmentInfoTobeUpdated = new ArrayList<>();
        List<DeviceIdentifier> identifiers = new ArrayList<>();
        Map<Integer, Long> lastActivities = null;
        EnrolmentInfo enrolmentInfo;
        DeviceIdentifier deviceIdentifier;
        try {
            operationEnrolmentMappings = this.getOperationEnrolmentMappings();
            if (operationEnrolmentMappings != null && operationEnrolmentMappings.size() > 0) {
                lastActivities = this.getLastDeviceActivities();
            }
        } catch (DeviceStatusTaskException e) {
            log.error("Error occurred while fetching OperationEnrolment mappings of deviceType '" + deviceType + "'", e);
        }
        for (OperationEnrolmentMapping mapping:operationEnrolmentMappings) {
            long lastActivity = -1;
            if (lastActivities != null && lastActivities.containsKey(mapping.getEnrolmentId())) {
                lastActivity = lastActivities.get(mapping.getEnrolmentId());
            }
            EnrolmentInfo.Status newStatus = this.determineDeviceStatus(mapping, lastActivity);
            if (newStatus != mapping.getDeviceStatus()) {
                enrolmentInfo = new EnrolmentInfo();
                enrolmentInfo.setId(mapping.getEnrolmentId());
                enrolmentInfo.setStatus(newStatus);
                enrolmentInfoTobeUpdated.add(enrolmentInfo);

                deviceIdentifier = new DeviceIdentifier();
                deviceIdentifier.setId(mapping.getDeviceId());
                deviceIdentifier.setId(mapping.getDeviceType());
                identifiers.add(deviceIdentifier);
            }
        }

        if (enrolmentInfoTobeUpdated.size() > 0) {
            try {
                this.updateDeviceStatus(enrolmentInfoTobeUpdated);
                //Remove updated entries from cache
                //DeviceCacheManagerImpl.getInstance().removeDevicesFromCache(identifiers);
            } catch (DeviceStatusTaskException e) {
                log.error("Error occurred while updating non-responsive device-status of devices of type '" + deviceType + "'",e);
            }
        }
    }

    private EnrolmentInfo.Status determineDeviceStatus(OperationEnrolmentMapping opMapping, long lastActivityTime) {
        long lastPendingOpBefore = (System.currentTimeMillis()/1000) - opMapping.getCreatedTime();
        EnrolmentInfo.Status newStatus = null;
        if (lastPendingOpBefore >= this.deviceStatusTaskPluginConfig.getIdleTimeToMarkInactive()) {
            newStatus = EnrolmentInfo.Status.INACTIVE;
        } else if (lastPendingOpBefore >= this.deviceStatusTaskPluginConfig.getIdleTimeToMarkUnreachable()) {
            newStatus = EnrolmentInfo.Status.UNREACHABLE;
        }
        if (lastActivityTime != -1) {
            long lastActivityBefore = (System.currentTimeMillis()/1000) - lastActivityTime;
            if (lastActivityBefore < lastPendingOpBefore) {
                return opMapping.getDeviceStatus();
            }
        }
        return newStatus;
    }

    private long getMinTimeWindow() {
        return (System.currentTimeMillis()/1000) - this.deviceStatusTaskPluginConfig.getIdleTimeToMarkUnreachable();
    }

    private long getMaxTimeWindow() {
        //Need to consider the frequency of the task as well
        return (System.currentTimeMillis()/1000) - this.deviceStatusTaskPluginConfig.getIdleTimeToMarkInactive() -
                this.deviceStatusTaskPluginConfig.getFrequency();
    }

    private boolean updateDeviceStatus(List<EnrolmentInfo> enrolmentInfos) throws
            DeviceStatusTaskException {
        boolean updateStatus;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            updateStatus = DeviceManagementDAOFactory.getEnrollmentDAO().updateEnrollmentStatus(enrolmentInfos);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceStatusTaskException("Error occurred while updating enrollment status of devices of type '"
                    + deviceType + "'", e);
        } catch (TransactionManagementException e) {
            throw new DeviceStatusTaskException("Error occurred while initiating a transaction for updating the device " +
                    "status of type '" + deviceType +"'", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return updateStatus;
    }

    private List<OperationEnrolmentMapping> getOperationEnrolmentMappings() throws DeviceStatusTaskException {
        List<OperationEnrolmentMapping> operationEnrolmentMappings = null;
        try {
            OperationManagementDAOFactory.openConnection();
            operationEnrolmentMappings = OperationManagementDAOFactory.
                    getOperationMappingDAO().getFirstPendingOperationMappingsForActiveEnrolments(this.getMinTimeWindow(),
                    this.getMaxTimeWindow(), this.deviceTypeId);
        } catch (SQLException e) {
            throw new DeviceStatusTaskException("Error occurred while getting Enrolment operation mappings for " +
                    "determining device status of deviceType '" + deviceType + "'", e);
        } catch (OperationManagementDAOException e) {
            throw new DeviceStatusTaskException("Error occurred obtaining a DB connection for fetching " +
                    "operation-enrolment mappings for status monitoring of deviceType '" + deviceType + "'", e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
        return operationEnrolmentMappings;
    }

    private Map<Integer, Long> getLastDeviceActivities() throws DeviceStatusTaskException {
        Map<Integer, Long> lastActivities = null;
        try {
            OperationManagementDAOFactory.openConnection();
            lastActivities = OperationManagementDAOFactory.
                    getOperationMappingDAO().getLastConnectedTimeForActiveEnrolments(this.getMaxTimeWindow(),
                    this.deviceTypeId);
        } catch (SQLException e) {
            throw new DeviceStatusTaskException("Error occurred while getting last activities for " +
                    "determining device status of deviceType '" + deviceType + "'", e);
        } catch (OperationManagementDAOException e) {
            throw new DeviceStatusTaskException("Error occurred obtaining a DB connection for fetching " +
                    "last activities for status monitoring of deviceType '" + deviceType + "'", e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
        return lastActivities;
    }
}