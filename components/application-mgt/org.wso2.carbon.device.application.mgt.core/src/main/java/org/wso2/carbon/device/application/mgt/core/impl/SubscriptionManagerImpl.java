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
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.util.HelperUtil;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.app.mgt.DeviceApplicationMapping;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the default implementation for the Subscription Manager.
 */
public class SubscriptionManagerImpl implements SubscriptionManager {

    private static final Log log = LogFactory.getLog(SubscriptionManagerImpl.class);
    private static final String INSTALL_APPLICATION = "INSTALL_APPLICATION";

    @Override
    public List<DeviceIdentifier> installApplicationForDevices(String applicationUUID, String versionName,
                                                               List<DeviceIdentifier> deviceList)
            throws ApplicationManagementException {
        return installApplication(applicationUUID, deviceList, versionName);
    }

    @Override
    public List<DeviceIdentifier> installApplicationForUsers(String applicationUUID, List<String> userList,
                                                             String versionName) throws ApplicationManagementException {
        log.info("Install application: " + applicationUUID + " to: " + userList.size() + " users.");
        List<DeviceIdentifier> deviceList = new ArrayList<>();
        for (String user : userList) {
            try {
                List<Device> devicesOfUser = HelperUtil.getDeviceManagementProviderService().getDevicesOfUser(user);
                for (Device device : devicesOfUser) {
                    deviceList.add(new DeviceIdentifier(device
                            .getDeviceIdentifier(), device.getType()));
                }
            } catch (DeviceManagementException e) {
                log.error("Error when extracting the device list from user[" + user + "].", e);
            }
        }
        return installApplication(applicationUUID, deviceList, versionName);
    }

    @Override
    public List<DeviceIdentifier> installApplicationForRoles(String applicationUUID, List<String> roleList,
                                                             String versionName) throws ApplicationManagementException {
        log.info("Install application: " + applicationUUID + " to: " + roleList.size() + " roles.");
        List<DeviceIdentifier> deviceList = new ArrayList<>();
        for (String role : roleList) {
            try {
                List<Device> devicesOfRole = HelperUtil.getDeviceManagementProviderService().getAllDevicesOfRole(role);
                for (Device device : devicesOfRole) {
                    deviceList.add(new DeviceIdentifier(device
                            .getDeviceIdentifier(), device.getType()));
                }
            } catch (DeviceManagementException e) {
                log.error("Error when extracting the device list from role[" + role + "].", e);
            }
        }
        return installApplication(applicationUUID, deviceList, versionName);
    }

    @Override
    public List<DeviceIdentifier> uninstallApplication(String applicationUUID,
                                                       List<DeviceIdentifier> deviceList)
            throws ApplicationManagementException {
        return null;
    }

    private List<DeviceIdentifier> installApplication(String applicationUUID, List<DeviceIdentifier> deviceList,
                                                      String versionName) throws ApplicationManagementException {
        List<DeviceIdentifier> failedDeviceList = new ArrayList<>(deviceList);
        // Todo: try whether we can optimise this by sending bulk inserts to db
        // Todo: atomicity is not maintained as deveice managment provider service uses separate db connection. fix this??
        log.info("Install application: " + applicationUUID + "[" + versionName + "]" + " to: "
                + deviceList.size() + " devices.");
        for (DeviceIdentifier device : deviceList) {
            org.wso2.carbon.device.mgt.common.DeviceIdentifier deviceIdentifier = new org.wso2.carbon.device.mgt
                    .common.DeviceIdentifier(device.getId(), device.getType());
            try {
                DeviceManagementProviderService dmpService = HelperUtil.getDeviceManagementProviderService();
                if (!dmpService.isEnrolled(deviceIdentifier)) {
                    log.error("Device with ID: [" + device.getId() + "] is not enrolled to install the application.");
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Installing application to : " + device.getId());
                    }
                    //Todo: generating one time download link for the application and put install operation to device.

                    // put app install operation to the device
                    ProfileOperation operation = new ProfileOperation();
                    operation.setCode(INSTALL_APPLICATION);
                    operation.setType(Operation.Type.PROFILE);
                    operation.setPayLoad("{'type':'enterprise', 'url':'http://10.100.5.76:8000/app-debug.apk', 'app':'"
                            + applicationUUID + "'}");
                    List<org.wso2.carbon.device.mgt.common.DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
                    deviceIdentifiers.add(deviceIdentifier);
                    dmpService.addOperation(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID,
                            operation, deviceIdentifiers);

                    DeviceApplicationMapping deviceApp = new DeviceApplicationMapping();
                    deviceApp.setDeviceIdentifier(device.getId());
                    deviceApp.setApplicationUUID(applicationUUID);
                    deviceApp.setVersionName(versionName);
                    deviceApp.setInstalled(false);
                    dmpService.addDeviceApplicationMapping(deviceApp);
//                    DeviceManagementDAOFactory.openConnection();
//                    DAOFactory.getSubscriptionDAO().addDeviceApplicationMapping(device.getId(), applicationUUID, false);
                    failedDeviceList.remove(device);
                }
            } catch (DeviceManagementException | OperationManagementException | InvalidDeviceException e) {
                log.error("Error while installing application to device[" + deviceIdentifier.getId() + "]", e);
            }
//            finally {
//                DeviceManagementDAOFactory.closeConnection();
//            }
        }
        return failedDeviceList;
    }
}
