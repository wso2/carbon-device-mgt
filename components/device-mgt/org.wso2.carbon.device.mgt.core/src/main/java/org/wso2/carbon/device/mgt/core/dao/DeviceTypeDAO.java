/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.device.mgt.common.DeviceType;

import java.util.List;

/**
 * This class represents the key operations associated with persisting device type related information.
 */
public interface DeviceTypeDAO {

    /**
     * @param deviceType             device that needs to be added
     * @throws DeviceManagementDAOException
     */
    DeviceType addDeviceType(DeviceType deviceType) throws DeviceManagementDAOException;

    /**
     * @param deviceType       deviceType that needs to be updated.
     * @throws DeviceManagementDAOException
     */
    DeviceType updateDeviceType(DeviceType deviceType) throws DeviceManagementDAOException;

    /**
     * @return list of all device types that are associated with the tenant this includes the shared device types.
     * @throws DeviceManagementDAOException
     */
    List<DeviceType> getDeviceTypes() throws DeviceManagementDAOException;

    /**
     * @param id retrieve the device type with its id.
     * @return the device type associated with the id.
     * @throws DeviceManagementDAOException
     */
    DeviceType getDeviceType(int id) throws DeviceManagementDAOException;

    /**
     * @param name retreive the device type with it name.
     * @return the device type associated with its name and tenant id.
     * @throws DeviceManagementDAOException
     */
    DeviceType getDeviceType(String name) throws DeviceManagementDAOException;

    /**
     * remove the device type from tenant.
     *
     * @param name remove the device type with it name.
     * @throws DeviceManagementDAOException
     */
    void removeDeviceType(String name) throws DeviceManagementDAOException;

}
