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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.encryption.SymmetricEncryption;
import org.wso2.carbon.device.mgt.common.DeviceStatusTaskPluginConfig;
import org.wso2.carbon.device.mgt.core.config.status.task.DeviceStatusTaskConfig;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This implements the Task service which monitors the device activity periodically & update the device-status if
 * necessary.
 */
public class DeviceStatusMonitoringTask implements Task {

    private static Log log = LogFactory.getLog(DeviceStatusMonitoringTask.class);
    private String deviceType;
    private DeviceStatusTaskPluginConfig deviceStatusTaskPluginConfig;

    @Override
    public void setProperties(Map<String, String> properties) {
        deviceType = properties.get(DeviceStatusTaskManagerServiceImpl.DEVICE_TYPE);
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
        EnrolmentInfo enrolmentInfo;
        try {
            operationEnrolmentMappings = this.getOperationEnrolmentMappings();
        } catch (DeviceStatusTaskException e) {
            log.error("Error occurred while fetching OperationEnrolment mappings of deviceType '" + deviceType + "'", e);
        }
        for (OperationEnrolmentMapping mapping:operationEnrolmentMappings) {
            EnrolmentInfo.Status newStatus = this.determineDeviceStatus(mapping);
            if (newStatus != mapping.getDeviceStatus()) {
                enrolmentInfo = new EnrolmentInfo();
                enrolmentInfo.setId(mapping.getEnrolmentId());
                enrolmentInfo.setStatus(newStatus);
                enrolmentInfoTobeUpdated.add(enrolmentInfo);
            }
        }

        if (enrolmentInfoTobeUpdated.size() > 0) {
            try {
                this.updateDeviceStatus(enrolmentInfoTobeUpdated);
            } catch (DeviceStatusTaskException e) {
                log.error("Error occurred while updating non-responsive device-status of devices of type '" + deviceType + "'",e);
            }
        }
    }

    private EnrolmentInfo.Status determineDeviceStatus(OperationEnrolmentMapping opMapping) {
        long lastContactedBefore = (System.currentTimeMillis()/1000) - opMapping.getCreatedTime();
        EnrolmentInfo.Status status = opMapping.getDeviceStatus();
        if (lastContactedBefore >= this.deviceStatusTaskPluginConfig.getIdleTimeToMarkInactive()) {
            status = EnrolmentInfo.Status.INACTIVE;
        } else if (lastContactedBefore >= this.deviceStatusTaskPluginConfig.getIdleTimeToMarkUnreachable()) {
            status = EnrolmentInfo.Status.UNREACHABLE;
        }
        return status;
    }

    private long getTimeWindow() {
        return (System.currentTimeMillis()/1000) - this.deviceStatusTaskPluginConfig.getIdleTimeToMarkInactive();
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
                    getOperationMappingDAO().getFirstPendingOperationMappingsForActiveEnrolments(this.getTimeWindow());
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
}