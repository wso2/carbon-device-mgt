/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.sensor.mgt.DeviceTypeSensor;
import org.wso2.carbon.device.mgt.common.sensor.mgt.dao.DeviceTypeSensorTransactionObject;

import java.util.List;

/**
 * TODO:: Complete all comments....
 */
public interface DeviceTypeSensorDAO {

    void addDeviceTypeSensor(DeviceTypeSensorTransactionObject deviceTypeSensorTransactionObject)
            throws DeviceManagementDAOException;

    void updateDeviceTypeSensor(DeviceTypeSensorTransactionObject deviceTypeSensorTransactionObject)
            throws DeviceManagementDAOException;

    void updateDeviceTypeSensor(int deviceTypeId, DeviceTypeSensor deviceTypeSensor)
            throws DeviceManagementDAOException;

    DeviceTypeSensorTransactionObject getDeviceTypeSensor(int deviceTypeId, String deviceTypeSensorName)
            throws DeviceManagementDAOException;

    DeviceTypeSensorTransactionObject getDeviceTypeSensor(int deviceTypeSensorId) throws DeviceManagementDAOException;

    List<DeviceTypeSensorTransactionObject> getDeviceTypeSensors(int deviceTypeId) throws DeviceManagementDAOException;

    void removeDeviceTypeSensor(int deviceTypeId, String deviceTypeSensorName) throws DeviceManagementDAOException;

    void removeDeviceTypeSensor(int deviceTypeSensorId) throws DeviceManagementDAOException;

    void removeDeviceTypeSensors(int deviceTypeId) throws DeviceManagementDAOException;
}
