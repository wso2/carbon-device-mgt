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


package org.wso2.carbon.policy.mgt.core.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.ntask.core.Task;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.common.spi.PolicyMonitoringService;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mgt.MonitoringManager;
import org.wso2.carbon.policy.mgt.core.mgt.impl.MonitoringManagerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MonitoringTask implements Task {

    private DeviceTypeDAO deviceTypeDAO;
    private static Log log = LogFactory.getLog(MonitoringTask.class);

    Map<String, String> properties;


    @Override
    public void setProperties(Map<String, String> map) {
        this.properties = map;
    }

    @Override
    public void init() {
        deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
    }

    @Override
    public void execute() {

        if (log.isDebugEnabled()) {
            log.debug("Monitoring task started to run.");
        }

        MonitoringManager monitoringManager = new MonitoringManagerImpl();

        List<DeviceType> deviceTypes = new ArrayList<>();
        try {
            deviceTypes = monitoringManager.getDeviceTypes();
        } catch (PolicyComplianceException e) {
            log.error("Error occurred while getting the device types.");
        }

        if (!deviceTypes.isEmpty()) {
            try {



                DeviceManagementProviderService deviceManagementProviderService =
                        PolicyManagementDataHolder.getInstance().getDeviceManagementService();

                for (DeviceType deviceType : deviceTypes) {

                    if(log.isDebugEnabled()){
                        log.debug("Running task for device type : " + deviceType.getName() );
                    }

                    PolicyMonitoringService monitoringService =
                            PolicyManagementDataHolder.getInstance().getPolicyMonitoringService(deviceType.getName());
                    List<Device> devices = deviceManagementProviderService.getAllDevices(deviceType.getName());
                    if (monitoringService != null && !devices.isEmpty()) {
                        monitoringManager.addMonitoringOperation(devices);

                        List<Device> notifiableDevices = new ArrayList<>();

                        if (log.isDebugEnabled()) {
                            log.debug("Removing inactive and blocked devices from the list for the device type : " +
                                    deviceType.getName());
                        }
                        for (Device device : devices) {
                            if (device.getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.INACTIVE) ||
                                    device.getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.BLOCKED)) {
                                continue;
                            } else {
                                notifiableDevices.add(device);
                            }
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Following devices selected to send the notification for " + deviceType.getName());
                            for (Device device : notifiableDevices) {
                                log.debug(device.getDeviceIdentifier());
                            }
                        }
                        monitoringService.notifyDevices(notifiableDevices);
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Monitoring task running completed.");
                }
            } catch (Exception e) {
                log.error("Error occurred while trying to run a task.", e);
            }
        } else {
            log.info("No device types registered currently. So did not run the monitoring task.");
        }

    }
}
