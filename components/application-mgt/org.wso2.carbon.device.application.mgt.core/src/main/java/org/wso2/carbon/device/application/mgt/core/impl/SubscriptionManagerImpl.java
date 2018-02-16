/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.device.application.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationInstallResponse;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.application.mgt.core.util.ApplicationManagementUtil;
import org.wso2.carbon.device.application.mgt.core.util.HelperUtil;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is the default implementation for the Subscription Manager.
 */
public class SubscriptionManagerImpl implements SubscriptionManager {

    private static final Log log = LogFactory.getLog(SubscriptionManagerImpl.class);
    private static final String INSTALL_APPLICATION = "INSTALL_APPLICATION";

    @Override
    public ApplicationInstallResponse installApplicationForDevices(String applicationUUID,
            List<DeviceIdentifier> deviceList) throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Install application: " + applicationUUID + " to " + deviceList.size() + "devices.");
        }
        return installApplication(applicationUUID, deviceList);
    }

    @Override
    public ApplicationInstallResponse installApplicationForUsers(String applicationUUID, List<String> userList)
            throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Install application: " + applicationUUID + " to " + userList.size() + " users.");
        }
        List<DeviceIdentifier> deviceList = new ArrayList<>();
        for (String user : userList) {
            try {
                List<Device> devicesOfUser = HelperUtil.getDeviceManagementProviderService().getDevicesOfUser(user);
                for (Device device : devicesOfUser) {
                    deviceList.add(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
                }
                if (log.isDebugEnabled()) {
                    log.debug(devicesOfUser.size() + " found for the provided user list");
                }
            } catch (DeviceManagementException e) {
                throw new ApplicationManagementException("Error when extracting the device list of user[" + user + "].",
                        e);
            }
        }
        return installApplication(applicationUUID, deviceList);
    }

    @Override
    public ApplicationInstallResponse installApplicationForRoles(String applicationUUID, List<String> roleList)
            throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Install application: " + applicationUUID + " to " + roleList.size() + " roles.");
        }
        List<DeviceIdentifier> deviceList = new ArrayList<>();
        for (String role : roleList) {
            try {
                List<Device> devicesOfRole = HelperUtil.getDeviceManagementProviderService().getAllDevicesOfRole(role);
                for (Device device : devicesOfRole) {
                    deviceList.add(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
                }
                if (log.isDebugEnabled()) {
                    log.debug(devicesOfRole.size() + " found for role: " + role);
                }
            } catch (DeviceManagementException e) {
                throw new ApplicationManagementException(
                        "Error when extracting the device list from role[" + role + "].", e);
            }
        }
        return installApplication(applicationUUID, deviceList);
    }

    @Override
    public ApplicationInstallResponse installApplicationForGroups(String applicationUUID, List<String> deviceGroupList)
            throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Install application: " + applicationUUID + " to " + deviceGroupList.size() + " groups.");
        }
        GroupManagementProviderService groupManagementProviderService = HelperUtil.getGroupManagementProviderService();
        List<DeviceIdentifier> deviceList = new ArrayList<>();
        for (String groupName : deviceGroupList) {
            try {
                DeviceGroup deviceGroup = groupManagementProviderService.getGroup(groupName);
                int deviceCount = groupManagementProviderService.getDeviceCount(deviceGroup.getGroupId());
                List<Device> devicesOfGroups = groupManagementProviderService
                        .getDevices(deviceGroup.getGroupId(), 0, deviceCount);
                for (Device device : devicesOfGroups) {
                    deviceList.add(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
                }
            } catch (GroupManagementException e) {
                throw new ApplicationManagementException(
                        "Error when extracting the device list from group[" + groupName + "].", e);
            }
        }
        return installApplication(applicationUUID, deviceList);
    }

    @Override
    public List<DeviceIdentifier> uninstallApplication(String applicationUUID, List<DeviceIdentifier> deviceList)
            throws ApplicationManagementException {
        return null;
    }

    private ApplicationInstallResponse installApplication(String applicationUUID, List<DeviceIdentifier> deviceList)
            throws ApplicationManagementException {
        DeviceManagementProviderService deviceManagementProviderService = HelperUtil
                .getDeviceManagementProviderService();

        ApplicationManager applicationManager = ApplicationManagementUtil.getApplicationManagerInstance();
        Application application = applicationManager.getApplicationByRelease(applicationUUID);
        ApplicationInstallResponse response = validateDevices(deviceList, application.getType());

        Map<String, List<DeviceIdentifier>> deviceTypeIdentifierMap = response.getSuccessfulDevices().stream()
                .collect(Collectors.groupingBy(DeviceIdentifier::getType));

        for(Map.Entry<String, List<DeviceIdentifier>> entry: deviceTypeIdentifierMap.entrySet()) {
            Operation operation = generateOperationPayloadByDeviceType(entry.getKey(), application);
            try {
                Activity activity = deviceManagementProviderService
                        .addOperation(entry.getKey(), operation, entry.getValue());
                response.setActivity(activity);
            } catch (OperationManagementException e) {
                throw new ApplicationManagementException("Error occurred while adding the application install"
                        + " operation to devices" , e);
            } catch (InvalidDeviceException e) {
                //This exception should not occur because the validation has already been done.
                throw new ApplicationManagementException("The list of device identifiers are invalid");
            }
        }

        //todo: add device application mapping

        return response;
    }

    private Operation generateOperationPayloadByDeviceType(String deviceType, Application application) {
        ProfileOperation operation = new ProfileOperation();
        operation.setCode(INSTALL_APPLICATION);
        operation.setType(Operation.Type.PROFILE);

        //todo: generate operation payload correctly for all types of devices.
        operation.setPayLoad(
                "{'type':'enterprise', 'url':'" + application.getApplicationReleases().get(0).getAppStoredLoc()
                        + "', 'app':'" + application.getApplicationReleases().get(0).getUuid() + "'}");
        return operation;
    }

    /**
     * Validates the preconditions which is required to satisfy from the device which is required to install the
     * application.
     *
     * This method check two preconditions whether the application type is compatible to install in the device and
     * whether the device is enrolled in the system.
     *
     * @param deviceIdentifierList List of {@link DeviceIdentifier} which the validation happens
     * @param appPlatform type of the application
     * @return {@link ApplicationInstallResponse} which contains compatible and incompatible device identifiers
     */
    private ApplicationInstallResponse validateDevices(List<DeviceIdentifier> deviceIdentifierList,
            String appPlatform) {
        ApplicationInstallResponse applicationInstallResponse = new ApplicationInstallResponse();
        List<DeviceIdentifier> failedDevices = new ArrayList<>();
        List<DeviceIdentifier> compatibleDevices = new ArrayList<>();

        for (DeviceIdentifier deviceIdentifier : deviceIdentifierList) {
            try {
                if (appPlatform == null || !(appPlatform.equals("WEB_CLIP") || appPlatform
                        .equals(deviceIdentifier.getType()))) {
                    log.error("Device with ID: [" + deviceIdentifier.getId() + "] of type: ["
                            + deviceIdentifier.getType() + "] is not compatible with the application of type: ["
                            + appPlatform + "]");
                    failedDevices.add(deviceIdentifier);
                    continue;
                }

                if (!DeviceManagerUtil.isValidDeviceIdentifier(deviceIdentifier)) {
                    log.error("Device with ID: [" + deviceIdentifier.getId() + "] is not valid to install the "
                            + "application.");
                    applicationInstallResponse.getFailedDevices().add(deviceIdentifier);
                }
            } catch (DeviceManagementException e) {
                log.error("Error occurred while validating the device: [" + deviceIdentifier.getId() + "]", e);
                failedDevices.add(deviceIdentifier);
            }
            compatibleDevices.add(deviceIdentifier);
        }
        applicationInstallResponse.setFailedDevices(failedDevices);
        applicationInstallResponse.setSuccessfulDevices(compatibleDevices);

        return applicationInstallResponse;
    }
}
