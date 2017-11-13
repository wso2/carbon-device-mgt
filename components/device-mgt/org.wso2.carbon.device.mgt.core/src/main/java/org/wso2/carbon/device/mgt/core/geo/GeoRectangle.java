/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.device.mgt.core.geo;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.device.details.mgt.impl.DeviceInformationManagerImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoRectangle {

    private static int count = 0;
    private static Log log = LogFactory.getLog(GeoRectangle.class);
    DeviceInformationManager deviceInformationManagerService = new DeviceInformationManagerImpl();
    private double minLat;
    private double maxLat;
    private double minLong;
    private double maxLong;
    private double centerLat;
    private double centerLong;
    private double rectangleLat;
    private double rectangleLong;
    private double deviantLatValue = 1000;
    private double deviantLongValue = 1000;
    private int id;
    private ArrayList<Device> devices = new ArrayList<>();

    public GeoRectangle(double minLat, double maxLat, double minLong, double maxLong) {
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLong = minLong;
        this.maxLong = maxLong;
        this.centerLat = (this.minLat + this.maxLat) / 2;
        this.centerLong = (this.minLong + this.maxLong) / 2;
        this.rectangleLat = this.centerLat;
        this.rectangleLong = this.centerLong;
        count++;
        this.id = count;
    }

    public Map<String, Double> getCoordinates() {
        Map<String, Double> rectangleCoordinates = new HashMap<String, Double>();

        rectangleCoordinates.put("Lat", rectangleLat);
        rectangleCoordinates.put("Long", rectangleLong);
        return rectangleCoordinates;
    }

    public boolean isDeviceInGeoRectangle(Device device) {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
        try {
            DeviceLocation location = deviceInformationManagerService.getDeviceLocation(deviceIdentifier);
            double locationLat = location.getLatitude();
            double locationLong = location.getLongitude();
            if (locationLat >= minLat && locationLat < maxLat && locationLong >= minLong && locationLong < maxLong) {
                checkLocationDeviants(locationLat, locationLong);
                return true;
            } else {
                return false;
            }
        } catch (DeviceDetailsMgtException e) {
            String msg = "Exception occurred while retrieving device location." + deviceIdentifier;
            log.error(msg, e);
            return false;
        }


    }

    private void checkLocationDeviants(double locationLat, double locationLong) {
        double latDeviant = Math.abs(centerLat - locationLat);
        double longDeviant = Math.abs(centerLong - locationLong);
        if (latDeviant <= deviantLatValue) {
            deviantLatValue = latDeviant;
            rectangleLat = locationLat;
        }
        if (longDeviant <= deviantLongValue) {
            deviantLongValue = longDeviant;
            rectangleLong = locationLong;
        }
    }

    public void addDevice(Device device) {
        devices.add(device);
    }


    public List<Device> getDevices() {
        return devices;
    }

    public int getDeviceCount() {
        return devices.size();
    }

    public Double getMinLat() {
        return minLat;
    }

    public void setMinLat(double minLat) {
        this.minLat = minLat;
    }

    public Double getMaxLat() {
        return maxLat;
    }

    public void setMaxLat(double maxLat) {
        this.maxLat = maxLat;
    }

    public Double getMinLong() {
        return minLong;
    }

    public void setMinLong(double minLong) {
        this.minLong = minLong;
    }

    public Double getMaxLong() {
        return maxLong;
    }

    public void setMaxLong(double maxLong) {
        this.maxLong = maxLong;
    }

    public Integer getId() {
        return id;
    }

}