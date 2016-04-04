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


package org.wso2.carbon.device.mgt.core.device.details.mgt;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;

/**
 * This class will manage the storing of device details related generic information such as cpu/memory utilization, battery level,
 * plugged in to a power source or operation on battery.
 * In CDM framework, we only keep the snapshot of the device information. So previous details are deleted as soon as new
 * data is arrived.
 */

public interface DeviceInformationManager {

    /**
     * This method will manage the storing of the device information as key value pairs.
     * @param deviceInfo - Device info object.
     * @throws DeviceDetailsMgtException
     */
    void addDeviceInfo(DeviceInfo deviceInfo) throws DeviceDetailsMgtException;

    /**
     * This method will return the device information.
     * @param deviceIdentifier - Device identifier, device type.
     * @return - Device information object.
     * @throws DeviceDetailsMgtException
     */
    DeviceInfo getDeviceInfo(DeviceIdentifier deviceIdentifier) throws DeviceDetailsMgtException;

    /**
     * This method will manage storing the device location as latitude, longitude, address, zip, country etc..
     * @param deviceLocation - Device location object.
     * @throws DeviceDetailsMgtException
     */
    void addDeviceLocation(DeviceLocation deviceLocation) throws DeviceDetailsMgtException;

    /**
     * This method will return the device location with latitude, longitude, address etc..
     * @param deviceIdentifier  - Device identifier, device type.
     * @return Device location object.
     * @throws DeviceDetailsMgtException
     */
    DeviceLocation getDeviceLocation(DeviceIdentifier deviceIdentifier) throws DeviceDetailsMgtException;

//    /**
//     * This method will manage the storing of device application list.
//     * @param deviceApplication - Device application list.
//     * @throws DeviceDetailsMgtException
//     */
//    void addDeviceApplications(DeviceApplication deviceApplication) throws DeviceDetailsMgtException;
//
//    /**
//     * This method will return the application list of the device.
//     * @param deviceIdentifier - Device identifier, device type.
//     * @return - Device application list with device identifier.
//     * @throws DeviceDetailsMgtException
//     */
//    DeviceApplication getDeviceApplication(DeviceIdentifier deviceIdentifier) throws DeviceDetailsMgtException;
}
