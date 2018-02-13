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
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationReleaseManager;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.application.mgt.core.util.ApplicationManagementUtil;
import org.wso2.carbon.device.application.mgt.core.util.HelperUtil;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.app.mgt.DeviceApplicationMapping;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the default implementation for the Subscription Manager.
 */
public class SubscriptionManagerImpl implements SubscriptionManager {

    private static final Log log = LogFactory.getLog(SubscriptionManagerImpl.class);
    private static final String INSTALL_APPLICATION = "INSTALL_APPLICATION";

    @Override
    public List<DeviceIdentifier> installApplicationForDevices(String applicationUUID,
            List<DeviceIdentifier> deviceList) throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Install application: " + applicationUUID + " to " + deviceList.size() + "devices.");
        }
        return installApplication(applicationUUID, deviceList);
    }

    @Override
    public List<DeviceIdentifier> installApplicationForUsers(String applicationUUID, List<String> userList)
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
    public List<DeviceIdentifier> installApplicationForRoles(String applicationUUID, List<String> roleList)
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
    public List<DeviceIdentifier> installApplicationForGroups(String applicationUUID, List<String> deviceGroupList)
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

    private List<DeviceIdentifier> installApplication(String applicationUUID, List<DeviceIdentifier> deviceList)
            throws ApplicationManagementException {
        //todo: 1. get application and release.
        ApplicationReleaseManager applicationReleaseManager = ApplicationManagementUtil
                .getApplicationReleaseManagerInstance();
        ApplicationRelease applicationRelease = applicationReleaseManager.getReleaseByUuid(applicationUUID);
        //todo: 2. check type and filter devices.
        //todo: 3. generate url based on application attributes and app release attributes

        //Todo: check if app type is installable for all the device types: apk -> android, ipa -> ios, webclip -> both
        return null;
    }
}
