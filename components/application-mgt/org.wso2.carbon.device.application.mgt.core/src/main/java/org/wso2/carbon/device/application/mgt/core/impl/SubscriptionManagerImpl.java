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
import org.wso2.carbon.device.application.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.application.mgt.core.dao.common.DAOFactory;
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
        List<DeviceIdentifier> failedDeviceList = new ArrayList<>(deviceList);
        for (DeviceIdentifier device : deviceList) {
            org.wso2.carbon.device.mgt.common.DeviceIdentifier deviceIdentifier = new org.wso2.carbon.device.mgt
                    .common.DeviceIdentifier(device.getId(), device.getType());
            try {
                DeviceManagementDAOFactory.openConnection();
                if (DeviceManagementDAOFactory.getDeviceDAO().getDevice(deviceIdentifier).isEmpty()) {
                    log.error("Device with ID: " + device.getId() + " not found to install the application.");
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Installing application to : " + device.getId());
                    }
                    //Todo: generating one time download link for the application and put install operation to device.
                    DAOFactory.getSubscriptionDAO().addDeviceApplicationMapping(device.getId(), applicationUUID, false);
                    failedDeviceList.remove(device);
                }
            } catch (DeviceManagementDAOException | SQLException e) {
                throw new ApplicationManagementException("Error locating device.", e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }
        }
        return failedDeviceList;
    }

    @Override
    public List<String> installApplicationForUsers(String applicationUUID, List<String> userList)
            throws ApplicationManagementException {
        log.info("Install application: " + applicationUUID + " to: " + userList.size() + " users.");
        for (String user : userList) {
            //Todo: implementation
            //Todo: get the device list and call installApplicationForDevices
        }
        return userList;
    }

    @Override
    public List<String> installApplicationForRoles(String applicationUUID, List<String> roleList)
            throws ApplicationManagementException {
        log.info("Install application: " + applicationUUID + " to: " + roleList.size() + " users.");
        for (String role : roleList) {
            //Todo: implementation
            //Todo: get the device list and call installApplicationForDevices
        }
        return roleList;
    }

    @Override
    public List<DeviceIdentifier> uninstallApplication(String applicationUUID,
                                                       List<DeviceIdentifier> deviceList)
            throws ApplicationManagementException {
        return null;
    }
}
