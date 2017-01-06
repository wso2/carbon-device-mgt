/*
*  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/


package org.wso2.carbon.policy.mgt.common;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;

import java.util.List;

/**
 * This will be used retrieve data database. This interface has to be implemented by PIP. PIP will be plug-able.
 */
public interface PolicyInformationPoint {

    /**
     * This method will return the data related Device, Some of the device data will provided in the initial pipDeviceData object such as
     * device id, device time and location, Other data such as roles, owned users and ownership type will be filled by this method.
     * @param  deviceIdentifier device data.
     * @return   PIPDevice
     */
    PIPDevice getDeviceData(DeviceIdentifier deviceIdentifier) throws PolicyManagementException;

    /**
     * This method will retrieve the policies related given device Data.
     * @param pipDevice
     * @return
     */
    List<Policy> getRelatedPolicies(PIPDevice pipDevice) throws PolicyManagementException;

    /**
     * This is will retrieve the features related to device type. This feature list will be used for dynamically
     * merging the policies.
     * @param deviceType
     * @return
     */
    List<Feature> getRelatedFeatures(String deviceType) throws FeatureManagementException;
}
