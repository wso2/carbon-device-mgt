/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.device.mgt.core.task.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.MonitoringOperation;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.task.DeviceMgtTaskException;
import org.wso2.carbon.device.mgt.core.task.DeviceTaskManager;
import org.wso2.carbon.device.mgt.core.task.Utils;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceTaskManagerImpl implements DeviceTaskManager {

    private static Log log = LogFactory.getLog(DeviceTaskManagerImpl.class);
    private String deviceType;
    private static Map<Integer, Map<String, Long>> map = new HashMap<>();
    private OperationMonitoringTaskConfig operationMonitoringTaskConfig;

    public DeviceTaskManagerImpl(String deviceType,
                                 OperationMonitoringTaskConfig operationMonitoringTaskConfig) {
        this.operationMonitoringTaskConfig = operationMonitoringTaskConfig;
        this.deviceType = deviceType;
    }

    public DeviceTaskManagerImpl(String deviceType) {
        this.deviceType = deviceType;
    }

    //get device type specific operations
    private List<MonitoringOperation> getOperationList() throws DeviceMgtTaskException {
        return operationMonitoringTaskConfig.getMonitoringOperation();
    }

    @Override
    public int getTaskFrequency() throws DeviceMgtTaskException {
        return operationMonitoringTaskConfig.getFrequency();
    }

//    @Override
//    public String getTaskImplementedClazz() throws DeviceMgtTaskException {
//        return DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getTaskConfiguration().
//                getTaskClazz();
//    }

    @Override
    public boolean isTaskEnabled() throws DeviceMgtTaskException {
        return operationMonitoringTaskConfig.isEnabled();
    }


    @Override
    public void addOperations() throws DeviceMgtTaskException {
        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder.getInstance().
                getDeviceManagementProvider();
        try {
            List<Device> devices;
            List<String> operations;

            operations = this.getValidOperationNames(); //list operations for each device type
            devices = deviceManagementProviderService.getAllDevices(deviceType);//list devices for each type
            if (!devices.isEmpty()) {
                for (String str : operations) {
                    CommandOperation operation = new CommandOperation();
                    operation.setEnabled(true);
                    operation.setType(Operation.Type.COMMAND);
                    operation.setCode(str);
                    deviceManagementProviderService.addOperation(deviceType, operation,
                            DeviceManagerUtil.getValidDeviceIdentifiers(devices));
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No devices are available to perform the operations.");
                }
            }
        } catch (InvalidDeviceException e) {
            throw new DeviceMgtTaskException("Invalid DeviceIdentifiers found.", e);
        } catch (DeviceManagementException e) {
            throw new DeviceMgtTaskException("Error occurred while retrieving the device list.", e);
        } catch (OperationManagementException e) {
            throw new DeviceMgtTaskException("Error occurred while adding the operations to devices", e);
        }
    }

    private List<String> getValidOperationNames() throws DeviceMgtTaskException {

        List<MonitoringOperation> monitoringOperations = this.getOperationList();
        List<String> opNames = new ArrayList<>();
        Long milliseconds = System.currentTimeMillis();
        int frequency = this.getTaskFrequency();
        Map<String, Long> mp = Utils.getTenantedTaskOperationMap(map);

        for (MonitoringOperation top : monitoringOperations) {
            if (!mp.containsKey(top.getTaskName())) {
                opNames.add(top.getTaskName());
                mp.put(top.getTaskName(), milliseconds);
            } else {
                Long lastExecutedTime = mp.get(top.getTaskName());
                Long evalTime = lastExecutedTime + (frequency * top.getRecurrentTimes());
                if (evalTime <= milliseconds) {
                    opNames.add(top.getTaskName());
                    mp.put(top.getTaskName(), milliseconds);
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Valid operation names are : " + Arrays.toString(opNames.toArray()));
        }
        return opNames;
    }

    private List<MonitoringOperation> getOperationListforTask() throws DeviceMgtTaskException {

        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder
                .getInstance().
                        getDeviceManagementProvider();

        return deviceManagementProviderService.getMonitoringOperationList(
                deviceType);//Get task list from each device type
    }


    @Override
    public boolean isTaskOperation(String opName) {

        try {
            List<MonitoringOperation> monitoringOperations = this.getOperationListforTask();
            for (MonitoringOperation taop : monitoringOperations) {
                if (taop.getTaskName().equalsIgnoreCase(opName)) {
                    return true;
                }
            }
        } catch (DeviceMgtTaskException e) {
            // ignoring the error, no need to throw, If error occurs, return value will be false.
        }

        return false;

    }

}

