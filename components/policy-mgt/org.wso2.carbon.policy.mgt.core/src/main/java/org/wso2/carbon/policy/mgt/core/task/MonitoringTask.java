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
import org.wso2.carbon.device.mgt.common.policy.mgt.PolicyMonitoringManager;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.policy.PolicyConfiguration;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.ntask.core.Task;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mgt.MonitoringManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MonitoringTask implements Task {

    private static Log log = LogFactory.getLog(MonitoringTask.class);

    Map<String, String> properties;


    @Override
    public void setProperties(Map<String, String> map) {
        this.properties = map;
    }

    @Override
    public void init() {
    }

    @Override
    public void execute() {

        if (log.isDebugEnabled()) {
            log.debug("Monitoring task started to run.");
        }

        MonitoringManager monitoringManager = PolicyManagementDataHolder.getInstance().getMonitoringManager();
        List<String> deviceTypes = new ArrayList<>();
        List<String> configDeviceTypes = new ArrayList<>();
        try {
            deviceTypes = monitoringManager.getDeviceTypes();
            for (String deviceType : deviceTypes) {
                if (isPlatformExist(deviceType)) {
                    configDeviceTypes.add(deviceType);
                }
            }
        } catch (PolicyComplianceException e) {
            log.error("Error occurred while getting the device types.");
        }
        if (!deviceTypes.isEmpty()) {
            try {
                DeviceManagementProviderService deviceManagementProviderService =
                        PolicyManagementDataHolder.getInstance().getDeviceManagementService();
                for (String deviceType : configDeviceTypes) {
                    if (log.isDebugEnabled()) {
                        log.debug("Running task for device type : " + deviceType);
                    }
                    PolicyMonitoringManager monitoringService =
                            PolicyManagementDataHolder.getInstance().getDeviceManagementService()
                                    .getPolicyMonitoringManager(deviceType);
                    List<Device> devices = deviceManagementProviderService.getAllDevices(deviceType);
                    if (monitoringService != null && !devices.isEmpty()) {
                        List<Device> notifiableDevices = new ArrayList<>();
                        if (log.isDebugEnabled()) {
                            log.debug("Removing inactive and blocked devices from the list for the device type : " +
                                    deviceType);
                        }
                        for (Device device : devices) {

                            EnrolmentInfo.Status status = device.getEnrolmentInfo().getStatus();
                            if (status.equals(EnrolmentInfo.Status.BLOCKED) ||
                                    status.equals(EnrolmentInfo.Status.REMOVED) ||
                                    status.equals(EnrolmentInfo.Status.UNCLAIMED) ||
                                    status.equals(EnrolmentInfo.Status.DISENROLLMENT_REQUESTED) ||
                                    status.equals(EnrolmentInfo.Status.SUSPENDED)) {
                                continue;
                            } else {
                                notifiableDevices.add(device);
                            }
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Following devices selected to send the notification for " + deviceType);
                            for (Device device : notifiableDevices) {
                                log.debug(device.getDeviceIdentifier());
                            }
                        }
                        if (!notifiableDevices.isEmpty()) {
                            monitoringManager.addMonitoringOperation(notifiableDevices);
                        }
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

    /**
     * Check whether Device platform (ex: android) is exist in the cdm-config.xml file before adding a
     * Monitoring operation to a specific device type.
     *
     * @param deviceType available device types.
     * @return return platform is exist(true) or not (false).
     */

    private boolean isPlatformExist(String deviceType) {
        PolicyMonitoringManager policyMonitoringManager = PolicyManagementDataHolder.getInstance()
                .getDeviceManagementService().getPolicyMonitoringManager(deviceType);
        if (policyMonitoringManager != null) {
            return true;
        }
        return false;
    }
}
