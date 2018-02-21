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

import org.wso2.carbon.device.mgt.common.DeviceOrganizationMetadataHolder;
import org.wso2.carbon.device.mgt.common.DeviceOrganizationNode;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents key operations related maintaining edge device related information in a network
 */
public interface DeviceOrganizationDAO {

    /**
     * This method is used to add a new device
     *
     * @param deviceId   unique device identifier
     * @param deviceName identifier name given to device
     * @param parent     parent that device is child to in the network
     * @param pingMins   number of minutes since last ping from device
     * @param state      state of activity of device
     * @param isGateway  can identify if device is a gateway or not
     * @return returns a "true" if device connected successfully. If not will return "false"
     * @throws DeviceOrganizationDAOException
     */
    boolean addDeviceOrganization(String deviceId, String deviceName, String parent, int pingMins, int state,
                                  int isGateway)
            throws DeviceOrganizationDAOException;

    /**
     * This method is used to remove a device
     *
     * @param deviceId unique device identifier
     * @return returns "true" if device removed successfully
     * @throws DeviceOrganizationDAOException
     */
    boolean removeDeviceOrganization(String deviceId) throws DeviceOrganizationDAOException;

    /**
     * This method enables to retrieve the device state by entering the device ID
     *
     * @param deviceId unique device identifier
     * @return returns integer for different states of connectivity
     * @throws DeviceOrganizationDAOException
     */
    int getDeviceOrganizationStateById(String deviceId) throws DeviceOrganizationDAOException;

    /**
     * This method allows us to retrieve the parent of a device based on device ID
     *
     * @param deviceId unique device identifier
     * @return returns parent of device
     * @throws DeviceOrganizationDAOException
     */
    String getDeviceOrganizationParent(String deviceId) throws DeviceOrganizationDAOException;

    /**
     * This method allows to check whether any device in the organization is a gateway
     *
     * @param deviceId unique device identifier
     * @return returns 1 if device is a gateway
     * @throws DeviceOrganizationDAOException
     */
    int getDeviceOrganizationIsGateway(String deviceId) throws DeviceOrganizationDAOException;

    /**
     * This method allows to retrieve the list of devices connected to a parent
     *
     * @param parentId unique device identifier, in this case the parents' ID
     * @return ArrayList with the IDs of children
     * @throws DeviceOrganizationDAOException
     */
    List<DeviceOrganizationMetadataHolder> getChildrenByParentId(String parentId) throws DeviceOrganizationDAOException;

    /**
     * This method allows to get Device name in Organization by ID
     *
     * @param deviceId
     * @return
     * @throws DeviceOrganizationDAOException
     */
    String getDeviceOrganizationNameById(String deviceId) throws DeviceOrganizationDAOException;

    /**
     * This method retrieves all devices in the organization table
     *
     * @return array with devices list
     * @throws DeviceOrganizationDAOException
     */
    List<DeviceOrganizationMetadataHolder> getDevicesInOrganization() throws DeviceOrganizationDAOException;

    /**
     * This method is used to update the device organization name
     *
     * @param deviceId   unique device identifier
     * @param deviceName identifier name given to device
     * @return returns the updated name string value if successful. Returns null if unsuccessful
     * @throws DeviceOrganizationDAOException
     */
    String updateDeviceOrganizationName(String deviceId, String deviceName) throws DeviceOrganizationDAOException;

    /**
     * This method is used to update the device organization parent
     *
     * @param deviceId unique device identifier
     * @param path     parent that device is child to in the network
     * @return returns the updated parent name string value if successful. Returns null if unsuccessful
     * @throws DeviceOrganizationDAOException
     */
    String updateDeviceOrganizationParent(String deviceId, String newParent) throws DeviceOrganizationDAOException;

    /**
     * This method allows to update the no. of minutes since last contact with device
     *
     * @param deviceId    unique device identifier
     * @param newPingMins number of minutes since last ping from device
     * @return will return -1 if there's an error. Else the new value
     * @throws DeviceOrganizationDAOException
     */
    int updateDevicePingMins(String deviceId, int newPingMins) throws DeviceOrganizationDAOException;

    /**
     * This method allows to update the Device Organization state
     *
     * @param deviceId unique device identifier
     * @param newState tate of activity of device
     * @return will return -1 if there's an error. Else the new value
     * @throws DeviceOrganizationDAOException
     */
    int updateDeviceOrganizationState(String deviceId, int newState) throws DeviceOrganizationDAOException;

}

