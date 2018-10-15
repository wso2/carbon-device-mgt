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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsDAO;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsMgtDAOException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceInformationManagerImpl implements DeviceInformationManager {

    private DeviceDetailsDAO deviceDetailsDAO;
    private DeviceDAO deviceDAO;
    private static final Log log = LogFactory.getLog(DeviceInformationManagerImpl.class);
    private static final String LOCATION_EVENT_STREAM_DEFINITION = "org.wso2.iot.LocationStream";
    private static final String DEVICE_INFO_EVENT_STREAM_DEFINITION = "org.wso2.iot.DeviceInfoStream";


    public DeviceInformationManagerImpl() {
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        this.deviceDetailsDAO = DeviceManagementDAOFactory.getDeviceDetailsDAO();
    }

    @Override
    public void addDeviceInfo(DeviceIdentifier deviceId, DeviceInfo deviceInfo) throws DeviceDetailsMgtException {
        try {
            Device device = DeviceManagementDataHolder.getInstance().
                    getDeviceManagementProvider().getDevice(deviceId, false);
            DeviceInfo newDeviceInfo = null;
            DeviceManagementDAOFactory.beginTransaction();
            DeviceInfo previousDeviceInfo = deviceDetailsDAO
                    .getDeviceInformation(device.getId(), device.getEnrolmentInfo().getId());
            Map<String, String> previousDeviceProperties = deviceDetailsDAO
                    .getDeviceProperties(device.getId(), device.getEnrolmentInfo().getId());
            if (previousDeviceInfo != null && previousDeviceProperties != null) {
                previousDeviceInfo.setDeviceDetailsMap(previousDeviceProperties);
                newDeviceInfo = processDeviceInfo(previousDeviceInfo, deviceInfo);
            } else if (previousDeviceInfo == null && previousDeviceProperties != null) {
                previousDeviceInfo = new DeviceInfo();
                previousDeviceInfo.setDeviceDetailsMap(previousDeviceProperties);
                newDeviceInfo = processDeviceInfo(previousDeviceInfo, deviceInfo);
            } else {
                newDeviceInfo = deviceInfo;
            }
            deviceDAO.updateDevice(device, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            deviceDetailsDAO.deleteDeviceInformation(device.getId(), device.getEnrolmentInfo().getId());
            deviceDetailsDAO.deleteDeviceProperties(device.getId(), device.getEnrolmentInfo().getId());
            deviceDetailsDAO.addDeviceInformation(device.getId(), device.getEnrolmentInfo().getId(), newDeviceInfo);
            deviceDetailsDAO.addDeviceProperties(newDeviceInfo.getDeviceDetailsMap(), device.getId(),
                    device.getEnrolmentInfo().getId());
            DeviceManagementDAOFactory.commitTransaction();

            //TODO :: This has to be fixed by adding the enrollment ID.
            if (DeviceManagerUtil.isPublishDeviceInfoResponseEnabled()) {
                Object[] metaData = {device.getDeviceIdentifier(), device.getType()};
                Object[] payload = new Object[]{
                        Calendar.getInstance().getTimeInMillis(),
                        deviceInfo.getDeviceDetailsMap().get("IMEI"),
                        deviceInfo.getDeviceDetailsMap().get("IMSI"),
                        deviceInfo.getDeviceModel(),
                        deviceInfo.getVendor(),
                        deviceInfo.getOsVersion(),
                        deviceInfo.getOsBuildDate(),
                        deviceInfo.getBatteryLevel(),
                        deviceInfo.getInternalTotalMemory(),
                        deviceInfo.getInternalAvailableMemory(),
                        deviceInfo.getExternalTotalMemory(),
                        deviceInfo.getExternalAvailableMemory(),
                        deviceInfo.getOperator(),
                        deviceInfo.getConnectionType(),
                        deviceInfo.getMobileSignalStrength(),
                        deviceInfo.getSsid(),
                        deviceInfo.getCpuUsage(),
                        deviceInfo.getTotalRAMMemory(),
                        deviceInfo.getAvailableRAMMemory(),
                        deviceInfo.isPluggedIn()
                };
                DeviceManagerUtil.getEventPublisherService().publishEvent(
                        DEVICE_INFO_EVENT_STREAM_DEFINITION, "1.0.0", metaData, new Object[0], payload
                );
            }
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Transactional error occurred while adding the device " +
                    "information.", e);
        } catch (DeviceDetailsMgtDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while adding the device information.", e);
        } catch (DeviceManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while retrieving the device information.", e);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while updating the last update timestamp of the " +
                    "device", e);
        } catch (DataPublisherConfigurationException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while publishing the device location information.", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    private DeviceInfo processDeviceInfo(DeviceInfo previousDeviceInfo, DeviceInfo newDeviceInfo) {
        if (newDeviceInfo.getDeviceModel().equals("")) {
            newDeviceInfo.setDeviceModel(previousDeviceInfo.getDeviceModel());
        }
        if (newDeviceInfo.getVendor().equals("")) {
            newDeviceInfo.setVendor(previousDeviceInfo.getVendor());
        }
        if (newDeviceInfo.getOsBuildDate().equals("")) {
            newDeviceInfo.setOsBuildDate(previousDeviceInfo.getOsBuildDate());
        }
        if (newDeviceInfo.getOsVersion().equals("")) {
            newDeviceInfo.setOsVersion(previousDeviceInfo.getOsVersion());
        }
        if (newDeviceInfo.getBatteryLevel() == 0.0) {
            newDeviceInfo.setBatteryLevel(previousDeviceInfo.getBatteryLevel());
        }
        if (newDeviceInfo.getInternalTotalMemory() == 0.0) {
            newDeviceInfo.setInternalTotalMemory(previousDeviceInfo.getInternalTotalMemory());
        }
        if (newDeviceInfo.getInternalAvailableMemory() == 0.0) {
            newDeviceInfo.setInternalAvailableMemory(previousDeviceInfo.getInternalAvailableMemory());
        }
        if (newDeviceInfo.getExternalTotalMemory() == 0.0) {
            newDeviceInfo.setExternalTotalMemory(previousDeviceInfo.getExternalTotalMemory());
        }
        if (newDeviceInfo.getExternalAvailableMemory() == 0.0) {
            newDeviceInfo.setExternalAvailableMemory(previousDeviceInfo.getExternalAvailableMemory());
        }
        if (newDeviceInfo.getOperator().equals("")) {
            newDeviceInfo.setOperator(previousDeviceInfo.getOperator());
        }
        if (newDeviceInfo.getConnectionType().equals("")) {
            newDeviceInfo.setConnectionType(previousDeviceInfo.getConnectionType());
        }
        if (newDeviceInfo.getMobileSignalStrength() == 0.0) {
            newDeviceInfo.setMobileSignalStrength(previousDeviceInfo.getMobileSignalStrength());
        }
        if (newDeviceInfo.getSsid().equals("")) {
            newDeviceInfo.setSsid(previousDeviceInfo.getSsid());
        }
        if (newDeviceInfo.getCpuUsage() == 0.0) {
            newDeviceInfo.setCpuUsage(previousDeviceInfo.getCpuUsage());
        }
        if (newDeviceInfo.getTotalRAMMemory() == 0.0) {
            newDeviceInfo.setTotalRAMMemory(previousDeviceInfo.getTotalRAMMemory());
        }
        if (newDeviceInfo.getAvailableRAMMemory() == 0.0) {
            newDeviceInfo.setAvailableRAMMemory(previousDeviceInfo.getAvailableRAMMemory());
        }
        if (!newDeviceInfo.isPluggedIn()) {
            newDeviceInfo.setPluggedIn(previousDeviceInfo.isPluggedIn());
        }
        Map<String, String> newDeviceDetailsMap = newDeviceInfo.getDeviceDetailsMap();
        Map<String, String> previousDeviceDetailsMap = previousDeviceInfo.getDeviceDetailsMap();
        for (String eachKey : previousDeviceDetailsMap.keySet()) {
            if (!newDeviceDetailsMap.containsKey(eachKey)) {
                newDeviceDetailsMap.put(eachKey, previousDeviceDetailsMap.get(eachKey));
            }
        }
        return newDeviceInfo;
    }

    @Override
    public DeviceInfo getDeviceInfo(DeviceIdentifier deviceId) throws DeviceDetailsMgtException {
        Device device = getDevice(deviceId);
        if (device == null) {
            return null;
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            DeviceInfo deviceInfo = deviceDetailsDAO.getDeviceInformation(device.getId(),
                    device.getEnrolmentInfo().getId());
            deviceInfo.setDeviceDetailsMap(deviceDetailsDAO.getDeviceProperties(device.getId(),
                    device.getEnrolmentInfo().getId()));
            return deviceInfo;

        } catch (SQLException e) {
            throw new DeviceDetailsMgtException("SQL error occurred while retrieving device " + deviceId.toString()
                    + "'s info from database.", e);
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
            List<Device> deviceIds = new ArrayList<>();
            List<Device> devices = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                    getAllDevices(false);
            for (Device device : devices) {
                if (identifierMap.containsKey(device.getDeviceIdentifier()) &&
                        device.getType().equals(identifierMap.get(device.getDeviceIdentifier()).getType())) {
                    deviceIds.add(device);
                }
            }
            DeviceManagementDAOFactory.openConnection();
            for (Device device : deviceIds) {
                DeviceInfo deviceInfo = deviceDetailsDAO.getDeviceInformation(device.getId(),
                        device.getEnrolmentInfo().getId());
                deviceInfo.setDeviceDetailsMap(deviceDetailsDAO.getDeviceProperties(device.getId(),
                        device.getEnrolmentInfo().getId()));
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
                    getDeviceManagementProvider().getDevice(deviceLocation.getDeviceIdentifier(), false);
            deviceLocation.setDeviceId(device.getId());
            DeviceManagementDAOFactory.beginTransaction();
            deviceDAO.updateDevice(device, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            deviceDetailsDAO.deleteDeviceLocation(deviceLocation.getDeviceId(), device.getEnrolmentInfo().getId());
            deviceDetailsDAO.addDeviceLocation(deviceLocation, device.getEnrolmentInfo().getId());
            if (DeviceManagerUtil.isPublishLocationResponseEnabled()) {
                Object[] metaData = {device.getDeviceIdentifier(), device.getEnrolmentInfo().getOwner(), device.getType()};
                Object[] payload = new Object[]{
                        deviceLocation.getUpdatedTime().getTime(),
                        deviceLocation.getLatitude(),
                        deviceLocation.getLongitude()
                };
                DeviceManagerUtil.getEventPublisherService().publishEvent(
                        LOCATION_EVENT_STREAM_DEFINITION, "1.0.0", metaData, new Object[0], payload
                );
            }
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            throw new DeviceDetailsMgtException("Transactional error occurred while adding the device location " +
                    "information.", e);
        } catch (DeviceDetailsMgtDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while adding the device location information.", e);
        } catch (DeviceManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while getting the device information.", e);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while updating the last updated timestamp of " +
                    "the device", e);
        } catch (DataPublisherConfigurationException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while publishing the device location information.", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public DeviceLocation getDeviceLocation(DeviceIdentifier deviceId) throws DeviceDetailsMgtException {
        Device device = getDevice(deviceId);
        if (device == null) {
            return null;
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDetailsDAO.getDeviceLocation(device.getId(), device.getEnrolmentInfo().getId());
        } catch (SQLException e) {
            throw new DeviceDetailsMgtException("SQL error occurred while retrieving device from database.", e);
        } catch (DeviceDetailsMgtDAOException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving device location.", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    private Device getDevice(DeviceIdentifier deviceId) throws DeviceDetailsMgtException {
        Device device;
        try {
            device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceId, false);
            if (device == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No device is found upon the device identifier '" + deviceId.getId() +
                            "' and type '" + deviceId.getType() + "'. Therefore returning null");
                }
                return null;
            }
        } catch (DeviceManagementException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving the device.", e);
        }
        return device;
    }

    @Override
    public List<DeviceLocation> getDeviceLocations(
            List<DeviceIdentifier> deviceIdentifiers) throws DeviceDetailsMgtException {

        try {
            List<Device> devices = DeviceManagementDataHolder.getInstance().
                    getDeviceManagementProvider().getAllDevices(deviceIdentifiers.get(0).getType(), false);
            List<DeviceLocation> deviceLocations = new ArrayList<>();
            DeviceManagementDAOFactory.openConnection();
            for (Device device : devices) {
                deviceLocations.add(deviceDetailsDAO.getDeviceLocation(device.getId(),
                        device.getEnrolmentInfo().getId()));
            }
            return deviceLocations;
        } catch (DeviceManagementException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving the devices.", e);
        } catch (SQLException e) {
            throw new DeviceDetailsMgtException("SQL error occurred while retrieving device from database.", e);
        } catch (DeviceDetailsMgtDAOException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving device locations.", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

}

