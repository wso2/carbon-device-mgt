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
import org.wso2.carbon.device.mgt.common.DeviceType;
import org.wso2.carbon.device.mgt.common.exception.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exception.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This class provides all functionalities for managing device types
 */
public class DeviceTypeManagerImpl implements DeviceTypeManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceTypeManagerImpl.class);

    private static final String DEVICETYPE_REGEX_PATTERN = "^[^ /]+$";
    private static final Pattern patternMatcher = Pattern.compile(DEVICETYPE_REGEX_PATTERN);

    private final DeviceTypeDAO deviceTypeDAO;

    public DeviceTypeManagerImpl(DeviceTypeDAO deviceTypeDAO) {
        this.deviceTypeDAO = deviceTypeDAO;
    }

    public List<DeviceType> getDeviceTypes() throws DeviceManagementException {
        LOGGER.debug("Get device types");
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceTypeDAO.getDeviceTypes();
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

    @Override
    public DeviceType addDeviceType(DeviceType type) throws DeviceManagementException {
        LOGGER.debug("Add device type");
        try {
            DeviceManagementDAOFactory.beginTransaction();
            Optional<DeviceType> existingDeviceType = deviceTypeDAO.getDeviceType(type.getName());
            if (!existingDeviceType.isPresent()) {
                DeviceType deviceType = deviceTypeDAO.addDeviceType(type);
                DeviceManagementDAOFactory.commitTransaction();
                return deviceType;
            } else {
                throw new DeviceManagementException("Device type: " + type.getName() + " already exists.", 400);
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while obtaining the device types";
            LOGGER.error(msg, e);
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            LOGGER.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public DeviceType updateDeviceType(DeviceType deviceType) throws DeviceManagementException {
        LOGGER.debug("Update device type");
        try {
            DeviceManagementDAOFactory.beginTransaction();
            Optional<DeviceType> existingDeviceType = deviceTypeDAO.getDeviceType(deviceType.getId());
            if (existingDeviceType.isPresent()) {
                return deviceTypeDAO.updateDeviceType(deviceType);
            } else {
                throw new DeviceManagementException("Device type: " + deviceType.getName() + " does not exist.", 404);
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while obtaining the device types";
            LOGGER.error(msg, e);
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            LOGGER.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

}
