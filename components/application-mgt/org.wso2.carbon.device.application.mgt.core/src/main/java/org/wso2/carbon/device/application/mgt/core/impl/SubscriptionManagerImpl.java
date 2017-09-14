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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.application.mgt.core.dao.common.DAOFactory;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the default implementation for the Subscription Manager.
 */
public class SubscriptionManagerImpl implements SubscriptionManager {

    private static final Log log = LogFactory.getLog(SubscriptionManagerImpl.class);

    @Override
    public List<DeviceIdentifier> installApplicationForDevices(String applicationUUID,
                                                               List<DeviceIdentifier> deviceList)
            throws ApplicationManagementException {
        log.info("Install application: " + applicationUUID + " to: " + deviceList.size() + " devices.");
        List<DeviceIdentifier> failedDevices = installApplication(applicationUUID, deviceList);
        return failedDevices;
    }

    @Override
    public List<DeviceIdentifier> installApplicationForUsers(String applicationUUID, List<String> userList)
            throws ApplicationManagementException {
        log.info("Install application: " + applicationUUID + " to: " + userList.size() + " users.");
        List<DeviceIdentifier> deviceList = new ArrayList<>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        for (String user : userList) {
            try {
                List<Device> devicesOfUser = DeviceManagementDAOFactory.getDeviceDAO().getDevicesOfUser(user, tenantId);
                for (Device device : devicesOfUser) {
                    deviceList.add(new DeviceIdentifier(device
                            .getDeviceIdentifier(), device.getType()));
                }
            } catch (DeviceManagementDAOException e) {
                log.error("Error when extracting the device list from user[" + user + "].", e);
            }
        }
        return installApplication(applicationUUID, deviceList);
    }

    @Override
    public List<DeviceIdentifier> installApplicationForRoles(String applicationUUID, List<String> roleList)
            throws ApplicationManagementException {
        log.info("Install application: " + applicationUUID + " to: " + roleList.size() + " roles.");
        List<DeviceIdentifier> deviceList = new ArrayList<>();
        for (String role : roleList) {
            try {
                List<Device> devicesOfRole = DataHolder.getInstance().getDeviceManagementService().getAllDevicesOfRole(role);
                for (Device device : devicesOfRole) {
                    deviceList.add(new DeviceIdentifier(device
                            .getDeviceIdentifier(), device.getType()));
                }
            } catch (DeviceManagementException e) {
                log.error("Error when extracting the device list from role[" + role + "].", e);
            }
        }
        return installApplication(applicationUUID, deviceList);
    }

    @Override
    public List<DeviceIdentifier> uninstallApplication(String applicationUUID,
                                                       List<DeviceIdentifier> deviceList)
            throws ApplicationManagementException {
        return null;
    }

    private List<DeviceIdentifier> installApplication(String applicationUUID, List<DeviceIdentifier> deviceList)
            throws ApplicationManagementException {
        List<DeviceIdentifier> failedDeviceList = new ArrayList<>(deviceList);
        List<org.wso2.carbon.device.mgt.common.DeviceIdentifier> activeDeviceList = new ArrayList<>();
        for (DeviceIdentifier device : deviceList) {
            org.wso2.carbon.device.mgt.common.DeviceIdentifier deviceIdentifier = new org.wso2.carbon.device.mgt
                    .common.DeviceIdentifier(device.getId(), device.getType());
            try {
                DeviceManagementDAOFactory.openConnection();
                if (DeviceManagementDAOFactory.getDeviceDAO().getDevice(deviceIdentifier).isEmpty()) {
                    log.error("Device with ID: " + device.getId() + " not found to install the application.");
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Prepare application install to : " + device.getId());
                    }
                    activeDeviceList.add(deviceIdentifier);
                    DAOFactory.getSubscriptionDAO().addDeviceApplicationMapping(device.getId(), applicationUUID, false);
                    failedDeviceList.remove(device);
                }
            } catch (DeviceManagementDAOException | SQLException e) {
                throw new ApplicationManagementException("Error locating device.", e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }
        }
        //Todo: generating one time download link for the application and put install operation to devices.
        return failedDeviceList;
    }
}
