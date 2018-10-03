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
            DeviceInfo newDeviceInfo = processDeviceInfo(deviceId, deviceInfo, device);
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

    private DeviceInfo processDeviceInfo(DeviceIdentifier deviceId, DeviceInfo deviceInfo, Device device) throws DeviceDetailsMgtException {

        Map<String, String> previousDeviceInfo = null;
        previousDeviceInfo = deviceDAO.getLatestDeviceInfoMap(deviceId, CarbonContext.getThreadLocalCarbonContext().getTenantId());
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
            deviceDAO.setLatestDeviceInfoMap(deviceId, CarbonContext.getThreadLocalCarbonContext().getTenantId(),
                    previousDeviceInfo, false);
            return deviceInfo;
        } else {
            DeviceInfo newDeviceInfo = new DeviceInfo();
            if (device.getName() != null && !previousDeviceInfo.get("NAME").equals(device.getName())) {
                previousDeviceInfo.put("NAME", device.getName());
            }
            if (device.getDescription() != null && !previousDeviceInfo.get("DESCRIPTION").equals(device.getDescription())) {
                previousDeviceInfo.put("DESCRIPTION", device.getDescription());
            }
            if (device.getDeviceIdentifier() != null && !previousDeviceInfo.get("DEVICE_IDENTIFICATION").equals(device.getDeviceIdentifier())) {
                previousDeviceInfo.put("DEVICE_IDENTIFICATION", device.getDeviceIdentifier());
            }
            if (String.valueOf(device.getId()) != null && !previousDeviceInfo.get("DEVICE_ID").equals(String.valueOf(device.getId()))) {
                previousDeviceInfo.put("DEVICE_ID", String.valueOf(device.getId()));
            }
            if (deviceInfo.getDeviceModel() != null && !previousDeviceInfo.get("DEVICE_MODEL").equals(deviceInfo.getDeviceModel())) {
                previousDeviceInfo.put("DEVICE_MODEL", deviceInfo.getDeviceModel());
            }
            if (deviceInfo.getVendor() != null && !previousDeviceInfo.get("VENDOR").equals(deviceInfo.getVendor())) {
                previousDeviceInfo.put("VENDOR", deviceInfo.getVendor());
            }
            if (deviceInfo.getOsVersion() != null && !previousDeviceInfo.get("OS_VERSION").equals(deviceInfo.getOsVersion())) {
                previousDeviceInfo.put("OS_VERSION", deviceInfo.getOsVersion());
            }
            if (deviceInfo.getOsBuildDate() != null && !previousDeviceInfo.get("OS_BUILD_DATE").equals(deviceInfo.getOsBuildDate())) {
                previousDeviceInfo.put("OS_BUILD_DATE", deviceInfo.getOsBuildDate());
            }
            if (String.valueOf(deviceInfo.getBatteryLevel()) != null && !previousDeviceInfo.get("BATTERY_LEVEL").equals(String.valueOf(deviceInfo.getBatteryLevel()))) {
                previousDeviceInfo.put("BATTERY_LEVEL", String.valueOf(deviceInfo.getBatteryLevel()));
            }
            if (String.valueOf(deviceInfo.getInternalTotalMemory()) != null && !previousDeviceInfo.get("INTERNAL_TOTAL_MEMORY").equals(String.valueOf(deviceInfo.getInternalTotalMemory()))) {
                previousDeviceInfo.put("INTERNAL_TOTAL_MEMORY", String.valueOf(deviceInfo.getInternalTotalMemory()));
            }
            if (String.valueOf(deviceInfo.getInternalTotalMemory()) != null && !previousDeviceInfo.get("INTERNAL_TOTAL_MEMORY").equals(String.valueOf(deviceInfo.getInternalTotalMemory()))) {
                previousDeviceInfo.put("INTERNAL_TOTAL_MEMORY", String.valueOf(deviceInfo.getInternalTotalMemory()));
            }
            if (String.valueOf(deviceInfo.getInternalAvailableMemory()) != null && !previousDeviceInfo.get("INTERNAL_AVAILABLE_MEMORY").equals(String.valueOf(deviceInfo.getInternalAvailableMemory()))) {
                previousDeviceInfo.put("INTERNAL_AVAILABLE_MEMORY", String.valueOf(deviceInfo.getInternalAvailableMemory()));
            }
            if (String.valueOf(deviceInfo.getExternalTotalMemory()) != null && !previousDeviceInfo.get("EXTERNAL_TOTAL_MEMORY").equals(String.valueOf(deviceInfo.getExternalTotalMemory()))) {
                previousDeviceInfo.put("EXTERNAL_TOTAL_MEMORY", String.valueOf(deviceInfo.getExternalTotalMemory()));
            }
            if (String.valueOf(deviceInfo.getExternalAvailableMemory()) != null && previousDeviceInfo.get("NAME") != null && !previousDeviceInfo.get("EXTERNAL_AVAILABLE_MEMORY").equals(String.valueOf(deviceInfo.getExternalAvailableMemory()))) {
                previousDeviceInfo.put("EXTERNAL_AVAILABLE_MEMORY", String.valueOf(deviceInfo.getExternalAvailableMemory()));
            }
            if (deviceInfo.getConnectionType() != null && !previousDeviceInfo.get("CONNECTION_TYPE").equals(deviceInfo.getConnectionType())) {
                previousDeviceInfo.put("CONNECTION_TYPE", deviceInfo.getConnectionType());
            }
            if (deviceInfo.getSsid() != null && !previousDeviceInfo.get("SSID").equals(deviceInfo.getSsid())) {
                previousDeviceInfo.put("SSID", deviceInfo.getSsid());
            }
            if (String.valueOf(deviceInfo.getCpuUsage()) != null && !previousDeviceInfo.get("CPU_USAGE").equals(String.valueOf(deviceInfo.getCpuUsage()))) {
                previousDeviceInfo.put("CPU_USAGE", String.valueOf(deviceInfo.getCpuUsage()));
            }
            if (String.valueOf(deviceInfo.getTotalRAMMemory()) != null && !previousDeviceInfo.get("TOTAL_RAM_MEMORY").equals(String.valueOf(deviceInfo.getTotalRAMMemory()))) {
                previousDeviceInfo.put("TOTAL_RAM_MEMORY", String.valueOf(deviceInfo.getTotalRAMMemory()));
            }
            if (String.valueOf(deviceInfo.getAvailableRAMMemory()) != null && !previousDeviceInfo.get("AVAILABLE_RAM_MEMORY").equals(String.valueOf(deviceInfo.getAvailableRAMMemory()))) {
                previousDeviceInfo.put("AVAILABLE_RAM_MEMORY", String.valueOf(deviceInfo.getAvailableRAMMemory()));
            }
            if (String.valueOf(deviceInfo.isPluggedIn()) != null && !previousDeviceInfo.get("PLUGGED_IN").equals(String.valueOf(deviceInfo.isPluggedIn()))) {
                previousDeviceInfo.put("PLUGGED_IN", String.valueOf(deviceInfo.isPluggedIn()));
            }
            newDeviceInfo.setDeviceModel(previousDeviceInfo.get("DEVICE_MODEL"));
            newDeviceInfo.setVendor(previousDeviceInfo.get("VENDOR"));
            newDeviceInfo.setOsVersion(previousDeviceInfo.get("OS_VERSION"));
            newDeviceInfo.setOsBuildDate(previousDeviceInfo.get("OS_BUILD_DATE"));
            newDeviceInfo.setBatteryLevel(Double.valueOf(previousDeviceInfo.get("BATTERY_LEVEL")));
            newDeviceInfo.setInternalTotalMemory(Double.valueOf(previousDeviceInfo.get("INTERNAL_TOTAL_MEMORY")));
            newDeviceInfo.setInternalAvailableMemory(Double.valueOf(previousDeviceInfo.get("INTERNAL_AVAILABLE_MEMORY")));
            newDeviceInfo.setExternalTotalMemory(Double.valueOf(previousDeviceInfo.get("EXTERNAL_TOTAL_MEMORY")));
            newDeviceInfo.setExternalAvailableMemory(Double.valueOf(previousDeviceInfo.get("EXTERNAL_AVAILABLE_MEMORY")));
            newDeviceInfo.setConnectionType(previousDeviceInfo.get("CONNECTION_TYPE"));
            newDeviceInfo.setSsid(previousDeviceInfo.get("SSID"));
            newDeviceInfo.setCpuUsage(Double.valueOf(previousDeviceInfo.get("CPU_USAGE")));
            newDeviceInfo.setTotalRAMMemory(Double.valueOf(previousDeviceInfo.get("TOTAL_RAM_MEMORY")));
            newDeviceInfo.setAvailableRAMMemory(Double.valueOf(previousDeviceInfo.get("AVAILABLE_RAM_MEMORY")));

            Map<String, String> tempDetailsMap = new HashMap<>();

            Map<String, String> agentDetailsMap = deviceInfo.getDeviceDetailsMap();

            String tempDetailsMapKeys = previousDeviceInfo.get("DEVICE_DETAILS_KEY");

            tempDetailsMapKeys = tempDetailsMapKeys.substring(1, (tempDetailsMapKeys.length() - 1));

            List<String> tempDetailsMapKeyList = Arrays.asList(tempDetailsMapKeys.split(","));

            List<String> newDetailsMapKeys = new ArrayList<>();

            for (String eachKey : tempDetailsMapKeyList) {
                eachKey = eachKey.replaceAll(" ", "");
                if (agentDetailsMap.get(eachKey) == null) {
                    tempDetailsMap.put(eachKey, previousDeviceInfo.get(eachKey));
                } else if (!agentDetailsMap.get(eachKey).equals(previousDeviceInfo.get(eachKey))) {
                    tempDetailsMap.put(eachKey, agentDetailsMap.get(eachKey));
                    previousDeviceInfo.put(eachKey, agentDetailsMap.get(eachKey));
                } else {
                    tempDetailsMap.put(eachKey, agentDetailsMap.get(eachKey));
                }
                newDetailsMapKeys.add(eachKey);
            }
            for (String eachKey : agentDetailsMap.keySet()) {
                if(!newDetailsMapKeys.contains(eachKey)){
                    tempDetailsMap.put(eachKey, agentDetailsMap.get(eachKey));
                    previousDeviceInfo.put(eachKey, agentDetailsMap.get(eachKey));
                    newDetailsMapKeys.add(eachKey);
                }
            }
            previousDeviceInfo.put("DEVICE_DETAILS_KEY", newDetailsMapKeys.toString());
            newDeviceInfo.setDeviceDetailsMap(tempDetailsMap);
            deviceDAO.setLatestDeviceInfoMap(deviceId, CarbonContext.getThreadLocalCarbonContext().getTenantId(),
                    previousDeviceInfo, true);
            return newDeviceInfo;
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

