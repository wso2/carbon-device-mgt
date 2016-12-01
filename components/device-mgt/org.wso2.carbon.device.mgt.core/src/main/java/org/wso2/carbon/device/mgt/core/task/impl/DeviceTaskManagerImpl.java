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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.TaskOperation;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
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
import java.util.Set;

public class DeviceTaskManagerImpl implements DeviceTaskManager {

    private static Log log = LogFactory.getLog(DeviceTaskManagerImpl.class);

    private static Map<Integer, Map<String, Long>> map = new HashMap<>();


    @Override
    //get device type specific operations
    public List<TaskOperation> getOperationList(String deviceType) throws DeviceMgtTaskException {

        List<TaskOperation> taskOperations = new ArrayList<>();
        Map<String, List<TaskOperation>> deviceTypeSpecificTasks;
        //This Map contains task list against device type
        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder.getInstance().
                getDeviceManagementProvider();

        deviceTypeSpecificTasks = deviceManagementProviderService.getTaskList();//Get task list from each device type
        for(String dti : deviceTypeSpecificTasks.keySet()){
            if (dti.equals(deviceType)) {
                taskOperations = deviceTypeSpecificTasks.get(dti);
            }
        }
        return taskOperations;
    }

    private List<String> getDeviceTypes() {
        List<String> operationPlatforms =  new ArrayList<>();
        Map<String, List<TaskOperation>> deviceTypeSpecificTasks;

        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder.getInstance().
                getDeviceManagementProvider();
        deviceTypeSpecificTasks = deviceManagementProviderService.getTaskList();

        Set<String> platformTypes = deviceTypeSpecificTasks.keySet();
        for(String platformType : platformTypes ){
          operationPlatforms.add(platformType);
        }
        return operationPlatforms;
    }

    @Override
    public int getTaskFrequency() throws DeviceMgtTaskException {
        return DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getTaskConfiguration().
                getFrequency();
    }

    @Override
    public String getTaskImplementedClazz() throws DeviceMgtTaskException {
        return DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getTaskConfiguration().
                getTaskClazz();
    }

    @Override
    public boolean isTaskEnabled() throws DeviceMgtTaskException {
        return DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getTaskConfiguration().
                isEnabled();
    }


    @Override
    public void addOperations() throws DeviceMgtTaskException {
        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder.getInstance().
                getDeviceManagementProvider();
        try {
            List<Device> devices;
            List<String> operations;
            List<String> deviceTypes = this.getDeviceTypes();//list available device types

            for(String deviceType : deviceTypes){
                operations = this.getValidOperationNames(deviceType); //list operations for each device type
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

            }
        } catch (InvalidDeviceException e) {
            throw new DeviceMgtTaskException("Invalid DeviceIdentifiers found.", e);
        } catch (DeviceManagementException e) {
            throw new DeviceMgtTaskException("Error occurred while retrieving the device list.", e);
        } catch (OperationManagementException e) {
            throw new DeviceMgtTaskException("Error occurred while adding the operations to devices", e);
        }
    }

    @Override
    public List<String> getValidOperationNames(String deviceType) throws DeviceMgtTaskException {

        List<TaskOperation> taskOperations = this.getOperationList(deviceType);
        List<String> opNames = new ArrayList<>();
        Long milliseconds = System.currentTimeMillis();
        int frequency = this.getTaskFrequency();
        Map<String, Long> mp = Utils.getTenantedTaskOperationMap(map);

        for (TaskOperation top : taskOperations) {
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



    @Override
    public boolean isTaskOperation(String opName, List<DeviceIdentifier> deviceIds) {

        for(DeviceIdentifier deviceIdentifier : deviceIds){
            String deviceType = deviceIdentifier.getType();
            try {
                List<TaskOperation> taskOperations = this.getOperationList(deviceType);
                for (TaskOperation taop : taskOperations) {
                    if (taop.getTaskName().equalsIgnoreCase(opName)) {
                        return true;
                    }
                }
            } catch (DeviceMgtTaskException e) {
                // ignoring the error, no need to throw, If error occurs, return value will be false.
            }
        }

        return false;

    }

}

