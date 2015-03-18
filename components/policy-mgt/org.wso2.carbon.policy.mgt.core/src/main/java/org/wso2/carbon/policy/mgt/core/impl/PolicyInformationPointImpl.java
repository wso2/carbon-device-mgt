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

package org.wso2.carbon.policy.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.impl.DeviceDAOImpl;
import org.wso2.carbon.device.mgt.core.dto.Device;
import org.wso2.carbon.policy.mgt.common.*;

import java.util.List;

public class PolicyInformationPointImpl implements PolicyInformationPoint {

    private static final Log log = LogFactory.getLog(PolicyInformationPointImpl.class);
    DeviceDAOImpl deviceDAO;

    public PolicyInformationPointImpl() {
        deviceDAO = new DeviceDAOImpl(DeviceManagementDAOFactory.getDataSource());
    }

    @Override
    public PIPDeviceData getDeviceData(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        PIPDeviceData pipDeviceData = new PIPDeviceData();
        Device device;
        try {
            device = deviceDAO.getDevice(deviceIdentifier);
            pipDeviceData.setDevice(device);

        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred when retrieving the data related to device from the database.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }

        return pipDeviceData;
    }

    @Override
    public List<Policy> getRelatedPolicies(PIPDeviceData pipDeviceData) throws PolicyManagementException {
        return null;
    }

    @Override
    public List<Feature> getRelatedFeatures(String deviceType) throws FeatureManagementException {
        return null;
    }
}
