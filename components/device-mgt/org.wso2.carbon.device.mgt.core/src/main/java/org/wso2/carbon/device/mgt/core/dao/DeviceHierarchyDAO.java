/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * This class represents key operations related maintaining edge device related information in a network
 */
public interface DeviceHierarchyDAO {

    /**
     * This method is used to add a new device to the hierarchy
     *
     * @param deviceId unique device identifier
     * @param parent   parent that device is child to in the network
     * @param isParent can identify if device is a gateway or not
     * @param tenantId unique identifier of tenant device was enrolled from
     * @return returns a "true" if device connected successfully. If not will return "false"
     * @throws DeviceHierarchyDAOException
     */
    boolean addDeviceToHierarchy(String deviceId, String deviceParent, int isParent, int tenantId)
            throws DeviceHierarchyDAOException;

    /**
     * This method is used to remove a device from the hierarchy
     *
     * @param deviceId unique device identifier
     * @return returns "true" if device removed successfully
     * @throws DeviceHierarchyDAOException
     */
    boolean removeDeviceFromHierarchy(String deviceId) throws DeviceHierarchyDAOException;

    /**
     * This method is used to update the device hierarchy parent
     *
     * @param deviceId    unique device identifier
     * @param newParentId unique identifier of new parent
     * @return returns true if successful.
     * @throws DeviceHierarchyDAOException
     */
    boolean updateDeviceHierarchyParent(String deviceId, String newParentId) throws DeviceHierarchyDAOException;

    /**
     * This method is used to update the device parency state
     *
     * @param deviceId        unique device identifier
     * @param newParencyState parency state identification flag
     * @return returns true if successful.
     * @throws DeviceHierarchyDAOException
     */
    boolean updateDeviceHierarchyParencyState(String deviceId, int newParencyState)
            throws DeviceHierarchyDAOException;

    /**
     * This method is used to update the tenant ID of enrolled device
     *
     * @param deviceId    unique device identifier
     * @param newTenantId unique identifier of enrolled tenant
     * @return returns true if successful.
     * @throws DeviceHierarchyDAOException
     */
    boolean updateDeviceHierarchyTenantId(String deviceId, int newTenantId)
            throws DeviceHierarchyDAOException;
}
