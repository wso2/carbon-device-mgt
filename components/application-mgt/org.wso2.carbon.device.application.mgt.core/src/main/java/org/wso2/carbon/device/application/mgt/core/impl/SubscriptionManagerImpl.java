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

import java.util.List;

public class SubscriptionManagerImpl implements SubscriptionManager {

    private static final Log log = LogFactory.getLog(SubscriptionManagerImpl.class);

    @Override
    public List<DeviceIdentifier> installApplicationForDevices(String applicationUUID, List<DeviceIdentifier> deviceList) throws ApplicationManagementException {
        log.info("Install application: " + applicationUUID + " to: " + deviceList.size() + " devices.");
        for (DeviceIdentifier device : deviceList) {
            String deviceId = device.getId();
            //Todo: implementation, validations
            //Todo: generating one time download link for the application and put install operation to device.
            //Todo: Store the mappings in DB.
        }
        return deviceList;
    }

    @Override
    public List<String> installApplicationForUsers(String applicationUUID, List<String> userList) throws ApplicationManagementException {
        log.info("Install application: " + applicationUUID + " to: " + userList.size() + " users.");
        for (String user : userList) {
            //Todo: implementation
        }
        return userList;
    }

    @Override
    public List<String> installApplicationForRoles(String applicationUUID, List<String> roleList) throws ApplicationManagementException {
        log.info("Install application: " + applicationUUID + " to: " + roleList.size() + " users.");
        for (String role : roleList) {
            //Todo: implementation
        }
        return roleList;
    }

    @Override
    public List<DeviceIdentifier> uninstallApplication(String applicationUUID, List<DeviceIdentifier> deviceList) throws ApplicationManagementException {
        return null;
    }
}
