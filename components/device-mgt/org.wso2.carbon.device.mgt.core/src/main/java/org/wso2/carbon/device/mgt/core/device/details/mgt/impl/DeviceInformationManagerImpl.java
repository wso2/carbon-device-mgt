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
import java.util.Arrays;
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
    private Device newDevice;
    private DeviceInfo newDeviceInfo;

    public DeviceInformationManagerImpl() {
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        this.deviceDetailsDAO = DeviceManagementDAOFactory.getDeviceDetailsDAO();
    }

    @Override
    public void addDeviceInfo(DeviceIdentifier deviceId, DeviceInfo deviceInfo) throws DeviceDetailsMgtException {
        try {
            Device device = DeviceManagementDataHolder.getInstance().
                    getDeviceManagementProvider().getDevice(deviceId, false);

            DeviceManagementDAOFactory.beginTransaction();
            processDeviceInfo(deviceId, deviceInfo, device);
            deviceDAO.updateDevice(newDevice, CarbonContext.getThreadLocalCarbonContext().getTenantId());
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
            throw new DeviceDetailsMgtException("Transactional error occurred while adding the device information.", e);
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

    private void processDeviceInfo(DeviceIdentifier deviceId, DeviceInfo deviceInfo, Device device) throws DeviceDetailsMgtException {

        Map<String, String> previousDeviceInfo = null;
        previousDeviceInfo = deviceDAO.getLatestDeviceInfoMap(deviceId, CarbonContext.getThreadLocalCarbonContext().getTenantId());

        Map<String, String> propertyMap = deviceInfo.getDeviceDetailsMap();
        if (previousDeviceInfo == null) {
            previousDeviceInfo = new HashMap<String, String>();
            previousDeviceInfo.put("NAME", device.getName());
            previousDeviceInfo.put("DESCRIPTION", device.getDescription());
            previousDeviceInfo.put("DEVICE_IDENTIFICATION", device.getDeviceIdentifier());
            previousDeviceInfo.put("DEVICE_ID", String.valueOf(device.getId()));

            previousDeviceInfo.put("DEVICE_MODEL", deviceInfo.getDeviceModel());
            previousDeviceInfo.put("VENDOR", deviceInfo.getVendor());
            previousDeviceInfo.put("OS_VERSION", deviceInfo.getOsVersion());
            previousDeviceInfo.put("OS_BUILD_DATE", deviceInfo.getOsBuildDate());
            previousDeviceInfo.put("BATTERY_LEVEL", String.valueOf(deviceInfo.getBatteryLevel()));
            previousDeviceInfo.put("INTERNAL_TOTAL_MEMORY", String.valueOf(deviceInfo.getInternalTotalMemory()));
            previousDeviceInfo.put("INTERNAL_AVAILABLE_MEMORY", String.valueOf(deviceInfo.getInternalAvailableMemory()));
            previousDeviceInfo.put("EXTERNAL_TOTAL_MEMORY", String.valueOf(deviceInfo.getExternalTotalMemory()));
            previousDeviceInfo.put("EXTERNAL_AVAILABLE_MEMORY", String.valueOf(deviceInfo.getExternalAvailableMemory()));
            previousDeviceInfo.put("CONNECTION_TYPE", deviceInfo.getConnectionType());
            previousDeviceInfo.put("SSID", deviceInfo.getSsid());
            previousDeviceInfo.put("CPU_USAGE", String.valueOf(deviceInfo.getCpuUsage()));
            previousDeviceInfo.put("TOTAL_RAM_MEMORY", String.valueOf(deviceInfo.getTotalRAMMemory()));
            previousDeviceInfo.put("AVAILABLE_RAM_MEMORY", String.valueOf(deviceInfo.getAvailableRAMMemory()));
            previousDeviceInfo.put("PLUGGED_IN", String.valueOf(deviceInfo.isPluggedIn()));
            List<String> deviceDetailsMapKeylist = new ArrayList<>();
            for (Map.Entry<String, String> entry : deviceInfo.getDeviceDetailsMap().entrySet()) {
                previousDeviceInfo.put(entry.getKey(), entry.getValue());
                deviceDetailsMapKeylist.add(entry.getKey());
            }
            previousDeviceInfo.put("DEVICE_DETAILS_KEY", deviceDetailsMapKeylist.toString());
            newDevice = device;
            newDeviceInfo = deviceInfo;
            deviceDAO.setLatestDeviceInfoMap(deviceId, CarbonContext.getThreadLocalCarbonContext().getTenantId(),
                    previousDeviceInfo, false);
        } else {
            newDevice = new Device();
            newDeviceInfo = new DeviceInfo();
            if (device.getName() == null || device.getName().equals("")) {
                newDevice.setName(previousDeviceInfo.get("NAME"));
            } else {
                newDevice.setName(device.getName());
            }
            if (device.getDescription() == null || device.getDescription().equals("")) {
                newDevice.setDescription(previousDeviceInfo.get("DESCRIPTION"));
            } else {
                newDevice.setDescription(device.getDescription());
            }
            if (device.getDeviceIdentifier() == null || device.getDeviceIdentifier().equals("")) {
                newDevice.setDeviceIdentifier(previousDeviceInfo.get("DEVICE_IDENTIFICATION"));
            } else {
                newDevice.setDeviceIdentifier(device.getDeviceIdentifier());
            }
            if (device.getName() == null || device.getName().equals("")) {
                newDevice.setName(previousDeviceInfo.get("NAME"));
            } else {
                newDevice.setName(device.getName());
            }
            if (deviceInfo.getDeviceModel() == null || deviceInfo.getDeviceModel().equals("")) {
                newDeviceInfo.setDeviceModel(previousDeviceInfo.get("DEVICE_MODEL"));
            } else {
                newDeviceInfo.setDeviceModel(deviceInfo.getDeviceModel());
            }
            if (deviceInfo.getVendor() == null || deviceInfo.getDeviceModel().equals("")) {
                newDeviceInfo.setVendor(previousDeviceInfo.get("VENDOR"));
            } else {
                newDeviceInfo.setVendor(deviceInfo.getVendor());
            }
            if (deviceInfo.getOsVersion() == null || deviceInfo.getOsVersion().equals("")) {
                newDeviceInfo.setOsVersion(previousDeviceInfo.get("OS_VERSION"));
            } else {
                newDeviceInfo.setOsVersion(deviceInfo.getOsVersion());
            }
            if (deviceInfo.getOsBuildDate() == null || deviceInfo.getOsBuildDate().equals("")) {
                newDeviceInfo.setOsBuildDate(previousDeviceInfo.get("OS_BUILD_DATE"));
            } else {
                newDeviceInfo.setOsBuildDate(deviceInfo.getOsBuildDate());
            }
            if (deviceInfo.getBatteryLevel() == null || deviceInfo.getBatteryLevel().equals("")) {
                newDeviceInfo.setBatteryLevel(Double.valueOf(previousDeviceInfo.get("BATTERY_LEVEL")));
            } else {
                newDeviceInfo.setBatteryLevel(deviceInfo.getBatteryLevel());
            }
            if (deviceInfo.getInternalTotalMemory() == null || deviceInfo.getInternalTotalMemory().equals("")) {
                newDeviceInfo.setInternalTotalMemory(Double.valueOf(previousDeviceInfo.get("INTERNAL_TOTAL_MEMORY")));
            } else {
                newDeviceInfo.setInternalTotalMemory(deviceInfo.getInternalTotalMemory());
            }
            if (deviceInfo.getInternalAvailableMemory() == null || deviceInfo.getInternalAvailableMemory().equals("")) {
                newDeviceInfo.setInternalAvailableMemory(Double.valueOf(previousDeviceInfo.get("INTERNAL_AVAILABLE_MEMORY")));
            } else {
                newDeviceInfo.setInternalAvailableMemory(deviceInfo.getInternalAvailableMemory());
            }
            if (deviceInfo.getExternalTotalMemory() == null || deviceInfo.getExternalAvailableMemory().equals("")) {
                newDeviceInfo.setExternalTotalMemory(Double.valueOf(previousDeviceInfo.get("EXTERNAL_TOTAL_MEMORY")));
            } else {
                newDeviceInfo.setExternalTotalMemory(deviceInfo.getExternalTotalMemory());
            }
            if (deviceInfo.getExternalAvailableMemory() == null || deviceInfo.getExternalAvailableMemory().equals("")) {
                newDeviceInfo.setExternalAvailableMemory(Double.valueOf(previousDeviceInfo.get("EXTERNAL_AVAILABLE_MEMORY")));
            } else {
                newDeviceInfo.setExternalAvailableMemory(deviceInfo.getExternalAvailableMemory());
            }
            if (deviceInfo.getConnectionType() == null || deviceInfo.getConnectionType().equals("")) {
                newDeviceInfo.setConnectionType(previousDeviceInfo.get("CONNECTION_TYPE"));
            } else {
                newDeviceInfo.setConnectionType(deviceInfo.getConnectionType());
            }
            if (deviceInfo.getSsid() == null || deviceInfo.getSsid().equals("")) {
                newDeviceInfo.setSsid(previousDeviceInfo.get("SSID"));
            } else {
                newDeviceInfo.setSsid(deviceInfo.getSsid());
            }
            if (deviceInfo.getCpuUsage() == null || deviceInfo.getCpuUsage().equals("")) {
                newDeviceInfo.setCpuUsage(Double.valueOf(previousDeviceInfo.get("CPU_USAGE")));
            } else {
                newDeviceInfo.setCpuUsage(deviceInfo.getCpuUsage());
            }
            if (deviceInfo.getTotalRAMMemory() == null || deviceInfo.getTotalRAMMemory().equals("")) {
                newDeviceInfo.setTotalRAMMemory(Double.valueOf(previousDeviceInfo.get("TOTAL_RAM_MEMORY")));
            } else {
                newDeviceInfo.setTotalRAMMemory(deviceInfo.getTotalRAMMemory());
            }
            if (deviceInfo.getAvailableRAMMemory() == null || deviceInfo.getAvailableRAMMemory().equals("")) {
                newDeviceInfo.setAvailableRAMMemory(Double.valueOf(previousDeviceInfo.get("AVAILABLE_RAM_MEMORY")));
            } else {
                newDeviceInfo.setAvailableRAMMemory(deviceInfo.getAvailableRAMMemory());
            }
            if (previousDeviceInfo.get("NAME") != null && !previousDeviceInfo.get("NAME").equals(device.getName())) {
                previousDeviceInfo.put("NAME", device.getName());
            }
            if (previousDeviceInfo.get("DESCRIPTION") != null && !previousDeviceInfo.get("DESCRIPTION").equals(device.getDescription())) {
                previousDeviceInfo.put("DESCRIPTION", device.getDescription());
            }
            if (previousDeviceInfo.get("DEVICE_IDENTIFICATION") != null && !previousDeviceInfo.get("DEVICE_IDENTIFICATION").equals(device.getDeviceIdentifier())) {
                previousDeviceInfo.put("DEVICE_IDENTIFICATION", device.getDeviceIdentifier());
            }
            if (previousDeviceInfo.get("DEVICE_ID") != null && !previousDeviceInfo.get("DEVICE_ID").equals(String.valueOf(device.getId()))) {
                previousDeviceInfo.put("DEVICE_ID", String.valueOf(device.getId()));
            }
            if (previousDeviceInfo.get("DEVICE_MODEL") != null && !previousDeviceInfo.get("DEVICE_MODEL").equals(deviceInfo.getDeviceModel())) {
                previousDeviceInfo.put("DEVICE_MODEL", deviceInfo.getDeviceModel());
            }
            if (previousDeviceInfo.get("VENDOR") != null && !previousDeviceInfo.get("VENDOR").equals(deviceInfo.getVendor())) {
                previousDeviceInfo.put("VENDOR", deviceInfo.getVendor());
            }
            if (previousDeviceInfo.get("OS_VERSION") != null && !previousDeviceInfo.get("OS_VERSION").equals(deviceInfo.getOsVersion())) {
                previousDeviceInfo.put("OS_VERSION", deviceInfo.getOsVersion());
            }
            if (previousDeviceInfo.get("OS_BUILD_DATE") != null && !previousDeviceInfo.get("OS_BUILD_DATE").equals(deviceInfo.getOsBuildDate())) {
                previousDeviceInfo.put("OS_BUILD_DATE", deviceInfo.getOsBuildDate());
            }
            if (previousDeviceInfo.get("BATTERY_LEVEL") != null && !previousDeviceInfo.get("BATTERY_LEVEL").equals(String.valueOf(deviceInfo.getBatteryLevel()))) {
                previousDeviceInfo.put("BATTERY_LEVEL", String.valueOf(deviceInfo.getBatteryLevel()));
            }
            if (previousDeviceInfo.get("INTERNAL_TOTAL_MEMORY") != null && !previousDeviceInfo.get("INTERNAL_TOTAL_MEMORY").equals(String.valueOf(deviceInfo.getInternalTotalMemory()))) {
                previousDeviceInfo.put("INTERNAL_TOTAL_MEMORY", String.valueOf(deviceInfo.getInternalTotalMemory()));
            }
            if (previousDeviceInfo.get("INTERNAL_TOTAL_MEMORY") != null && !previousDeviceInfo.get("INTERNAL_TOTAL_MEMORY").equals(String.valueOf(deviceInfo.getInternalTotalMemory()))) {
                previousDeviceInfo.put("INTERNAL_TOTAL_MEMORY", String.valueOf(deviceInfo.getInternalTotalMemory()));
            }
            if (previousDeviceInfo.get("INTERNAL_AVAILABLE_MEMORY") != null && !previousDeviceInfo.get("INTERNAL_AVAILABLE_MEMORY").equals(String.valueOf(deviceInfo.getInternalAvailableMemory()))) {
                previousDeviceInfo.put("INTERNAL_AVAILABLE_MEMORY", String.valueOf(deviceInfo.getInternalAvailableMemory()));
            }
            if (previousDeviceInfo.get("EXTERNAL_TOTAL_MEMORY") != null && !previousDeviceInfo.get("EXTERNAL_TOTAL_MEMORY").equals(String.valueOf(deviceInfo.getExternalTotalMemory()))) {
                previousDeviceInfo.put("EXTERNAL_TOTAL_MEMORY", String.valueOf(deviceInfo.getExternalTotalMemory()));
            }
            if (previousDeviceInfo.get("EXTERNAL_AVAILABLE_MEMORY") != null && previousDeviceInfo.get("NAME") != null && !previousDeviceInfo.get("EXTERNAL_AVAILABLE_MEMORY").equals(String.valueOf(deviceInfo.getExternalAvailableMemory()))) {
                previousDeviceInfo.put("EXTERNAL_AVAILABLE_MEMORY", String.valueOf(deviceInfo.getExternalAvailableMemory()));
            }
            if (previousDeviceInfo.get("CONNECTION_TYPE") != null && !previousDeviceInfo.get("CONNECTION_TYPE").equals(deviceInfo.getConnectionType())) {
                previousDeviceInfo.put("CONNECTION_TYPE", deviceInfo.getConnectionType());
            }
            if (previousDeviceInfo.get("SSID") != null && !previousDeviceInfo.get("SSID").equals(deviceInfo.getSsid())) {
                previousDeviceInfo.put("SSID", deviceInfo.getSsid());
            }
            if (previousDeviceInfo.get("CPU_USAGE") != null && !previousDeviceInfo.get("CPU_USAGE").equals(String.valueOf(deviceInfo.getCpuUsage()))) {
                previousDeviceInfo.put("CPU_USAGE", String.valueOf(deviceInfo.getCpuUsage()));
            }
            if (previousDeviceInfo.get("TOTAL_RAM_MEMORY") != null && !previousDeviceInfo.get("TOTAL_RAM_MEMORY").equals(String.valueOf(deviceInfo.getTotalRAMMemory()))) {
                previousDeviceInfo.put("TOTAL_RAM_MEMORY", String.valueOf(deviceInfo.getTotalRAMMemory()));
            }
            if (previousDeviceInfo.get("AVAILABLE_RAM_MEMORY") != null && !previousDeviceInfo.get("AVAILABLE_RAM_MEMORY").equals(String.valueOf(deviceInfo.getAvailableRAMMemory()))) {
                previousDeviceInfo.put("AVAILABLE_RAM_MEMORY", String.valueOf(deviceInfo.getAvailableRAMMemory()));
            }
            if (previousDeviceInfo.get("PLUGGED_IN") != null && !previousDeviceInfo.get("PLUGGED_IN").equals(String.valueOf(deviceInfo.isPluggedIn()))) {
                previousDeviceInfo.put("PLUGGED_IN", String.valueOf(deviceInfo.isPluggedIn()));
            }
            Map<String, String> tempDetailsMap = new HashMap<>();
            Map<String, String> oldDetailsMap = deviceInfo.getDeviceDetailsMap();
            String tempDetailsMapKeys = previousDeviceInfo.get("DEVICE_DETAILS_KEY");
            tempDetailsMapKeys = tempDetailsMapKeys.substring(1, (tempDetailsMapKeys.length() - 1));
            List<String> tempDetailsMapKeyList = Arrays.asList(tempDetailsMapKeys.split(","));
            List<String> newDetailsMapKeys = new ArrayList<>();
            for (String eachKey : tempDetailsMapKeyList) {
                eachKey = eachKey.replaceAll(" ", "");
                if (oldDetailsMap.get(eachKey) == null) {
                    tempDetailsMap.put(eachKey, previousDeviceInfo.get(eachKey));
                } else if (!oldDetailsMap.get(eachKey).equals(previousDeviceInfo.get(eachKey))) {
                    tempDetailsMap.put(eachKey, oldDetailsMap.get(eachKey));
                    previousDeviceInfo.put(eachKey, oldDetailsMap.get(eachKey));
                }
            }
            for (String eachKey : oldDetailsMap.keySet()) {
                if (!previousDeviceInfo.containsKey(eachKey)) {
                    tempDetailsMap.put(eachKey, previousDeviceInfo.get(eachKey));
                    previousDeviceInfo.put(eachKey, oldDetailsMap.get(eachKey));
                    newDetailsMapKeys.add(eachKey);
                }
            }
            for (String eachKey : tempDetailsMapKeyList) {
                newDetailsMapKeys.add(eachKey);
            }
            previousDeviceInfo.put("DEVICE_DETAILS_KEY", newDetailsMapKeys.toString());
            newDeviceInfo.setDeviceDetailsMap(tempDetailsMap);
            deviceDAO.setLatestDeviceInfoMap(deviceId, CarbonContext.getThreadLocalCarbonContext().getTenantId(),
                    previousDeviceInfo, true);
        }
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
        } finally{
            DeviceManagementDAOFactory.closeConnection();
        }
    }

}

