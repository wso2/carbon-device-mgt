/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.device.mgt.core.device.details.mgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsDAO;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsMgtDAOException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceInformationManagerImpl implements DeviceInformationManager {

    private static Log log = LogFactory.getLog(DeviceInformationManagerImpl.class);

    private DeviceDetailsDAO deviceDetailsDAO;

    public DeviceInformationManagerImpl() {
        this.deviceDetailsDAO = DeviceManagementDAOFactory.getDeviceDetailsDAO();
    }

    @Override
    public void addDeviceInfo(DeviceInfo deviceInfo) throws DeviceDetailsMgtException {

        try {
            Device device = DeviceManagementDataHolder.getInstance().
                    getDeviceManagementProvider().getDevice(deviceInfo.getDeviceIdentifier());
            deviceInfo.setDeviceId(device.getId());

            DeviceManagementDAOFactory.beginTransaction();
            deviceDetailsDAO.deleteDeviceInformation(deviceInfo.getDeviceId());
            deviceDetailsDAO.deleteDeviceProperties(deviceInfo.getDeviceId());
            deviceDetailsDAO.addDeviceInformation(deviceInfo);
            deviceDetailsDAO.addDeviceProperties(deviceInfo.getDeviceDetailsMap(), deviceInfo.getDeviceId());
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Transactional error occurred while adding the device information.");
        } catch (DeviceDetailsMgtDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while adding the device information.");
        } catch (DeviceManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while retrieving the device information.");
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public DeviceInfo getDeviceInfo(DeviceIdentifier deviceIdentifier) throws DeviceDetailsMgtException {

        try {
            Device device = DeviceManagementDataHolder.getInstance().
                    getDeviceManagementProvider().getDevice(deviceIdentifier);

            DeviceManagementDAOFactory.openConnection();
            DeviceInfo deviceInfo = deviceDetailsDAO.getDeviceInformation(device.getId());
            deviceInfo.setDeviceDetailsMap(deviceDetailsDAO.getDeviceProperties(device.getId()));
            return deviceInfo;

        } catch (SQLException e) {
            throw new DeviceDetailsMgtException("SQL error occurred while retrieving device from database.", e);
        } catch (DeviceManagementException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving the device.", e);
        } catch (DeviceDetailsMgtDAOException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving device details.", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<DeviceInfo> getDevicesInfo(List<DeviceIdentifier> deviceIdentifiers) throws DeviceDetailsMgtException {

        List<DeviceInfo> deviceInfos = new ArrayList<>();

        Map<String, DeviceIdentifier> identifierMap = new HashMap<>();
        for (DeviceIdentifier identifier : deviceIdentifiers) {
            identifierMap.put(identifier.getId(), identifier);
        }
        try {
            List<Integer> deviceIds = new ArrayList<>();
            List<Device> devices = DeviceManagementDataHolder.getInstance().
                    getDeviceManagementProvider().getAllDevices();
            for (Device device : devices) {
                if (identifierMap.containsKey(device.getDeviceIdentifier()) &&
                        device.getType().equals(identifierMap.get(device.getDeviceIdentifier()))) {
                    deviceIds.add(device.getId());
                }
            }
            DeviceManagementDAOFactory.openConnection();
            for(Integer id : deviceIds) {
                DeviceInfo deviceInfo = deviceDetailsDAO.getDeviceInformation(id);
                deviceInfo.setDeviceDetailsMap(deviceDetailsDAO.getDeviceProperties(id));
                deviceInfos.add(deviceInfo);
            }
        } catch (SQLException e) {
            throw new DeviceDetailsMgtException("SQL error occurred while retrieving devices from database.", e);
        } catch (DeviceManagementException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving the devices.", e);
        } catch (DeviceDetailsMgtDAOException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving devices details.", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return deviceInfos;
    }

    @Override
    public void addDeviceLocation(DeviceLocation deviceLocation) throws DeviceDetailsMgtException {

        try {
            Device device = DeviceManagementDataHolder.getInstance().
                    getDeviceManagementProvider().getDevice(deviceLocation.getDeviceIdentifier());
            deviceLocation.setDeviceId(device.getId());
            DeviceManagementDAOFactory.beginTransaction();
            deviceDetailsDAO.deleteDeviceLocation(deviceLocation.getDeviceId());
            deviceDetailsDAO.addDeviceLocation(deviceLocation);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Transactional error occurred while adding the device location information.");
        } catch (DeviceDetailsMgtDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while adding the device location information.");
        } catch (DeviceManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while getting the device information.");
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public DeviceLocation getDeviceLocation(DeviceIdentifier deviceIdentifier) throws DeviceDetailsMgtException {
        try {
            Device device = DeviceManagementDataHolder.getInstance().
                    getDeviceManagementProvider().getDevice(deviceIdentifier);

            DeviceManagementDAOFactory.openConnection();
            return deviceDetailsDAO.getDeviceLocation(device.getId());

        } catch (SQLException e) {
            throw new DeviceDetailsMgtException("SQL error occurred while retrieving device from database.", e);
        } catch (DeviceManagementException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving the device.", e);
        } catch (DeviceDetailsMgtDAOException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving device location.", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<DeviceLocation> getDeviceLocations(List<DeviceIdentifier> deviceIdentifiers) throws DeviceDetailsMgtException {

        try {
            List<Device> devices = DeviceManagementDataHolder.getInstance().
                    getDeviceManagementProvider().getAllDevices(deviceIdentifiers.get(0).getType());
            List<DeviceLocation> deviceLocations = new ArrayList<>();
            DeviceManagementDAOFactory.openConnection();
            for (Device device : devices) {
                deviceLocations.add(deviceDetailsDAO.getDeviceLocation(device.getId()));
            }
            return deviceLocations;
        } catch (DeviceManagementException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving the devices.", e);
        } catch (SQLException e) {
            throw new DeviceDetailsMgtException("SQL error occurred while retrieving device from database.", e);
        } catch (DeviceDetailsMgtDAOException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving device locations.", e);
        }
    }

}

