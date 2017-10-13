/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.jaxrs.service.impl.util;

import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for Device Management API test cases.
 * */
public class DeviceMgtAPITestHelper {

    private static final String DEVICE_TYPE_DESCRIPTION = "Dummy Description";
    private static final String DEVICE_TYPE = "TEST_DEVICE_TYPE";

    /**
     * Creates a Device Type with given name and given id.
     * If the name is null, the TEST_DEVICE_TYPE will be used as the name.
     *
     * @param name         : Name of the device type.
     * @param deviceTypeId : The Id of the device type.
     * @return DeviceType
     */
    public static DeviceType getDummyDeviceType(String name, int deviceTypeId) {
        DeviceType deviceType = new DeviceType();
        deviceType.setId(deviceTypeId);
        deviceType.setName(name != null ? name : DEVICE_TYPE);

        DeviceTypeMetaDefinition deviceTypeMetaDefinition = new DeviceTypeMetaDefinition();
        deviceTypeMetaDefinition.setClaimable(true);
        deviceTypeMetaDefinition.setDescription(DEVICE_TYPE_DESCRIPTION);

        PushNotificationConfig pushNotificationConfig =
                new PushNotificationConfig(name, true, null);
        deviceTypeMetaDefinition.setPushNotificationConfig(pushNotificationConfig);

        deviceType.setDeviceTypeMetaDefinition(deviceTypeMetaDefinition);
        return deviceType;
    }

    /**
     * Generates a list of device types.
     *
     * @param count: The number of device types that is needed.
     * @return List<DeviceType> : A list of device types.
     */
    public static List<DeviceType> getDummyDeviceTypeList(int count) {
        List<DeviceType> deviceTypes = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            DeviceType deviceType = getDummyDeviceType(DEVICE_TYPE + count, count);
            deviceTypes.add(deviceType);
        }

        return deviceTypes;
    }
}
