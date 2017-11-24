/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.core.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.exception.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;

import java.sql.SQLException;
import java.util.Optional;

/**
 * This class provides all functionalities for managing devices
 */
public class DeviceManagerImpl implements DeviceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceManagerImpl.class);

    private final DeviceDAO deviceDAO;

    public DeviceManagerImpl(DeviceDAO deviceDAO) {
        this.deviceDAO = deviceDAO;
    }

    @Override
    public Optional<Device> getDevice(String deviceIdentifier, String type) throws DeviceManagementException {
        LOGGER.debug("Get device types");
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDevice(deviceIdentifier, type);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while obtaining the device types";
            LOGGER.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            LOGGER.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }
}
