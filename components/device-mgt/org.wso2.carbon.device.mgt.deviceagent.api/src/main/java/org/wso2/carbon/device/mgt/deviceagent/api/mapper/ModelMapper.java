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

package org.wso2.carbon.device.mgt.deviceagent.api.mapper;

import org.wso2.carbon.device.mgt.deviceagent.api.dto.Device;
import org.wso2.carbon.device.mgt.devicetype.api.dto.DeviceType;
import org.wso2.carbon.device.mgt.devicetype.api.dto.DeviceTypeMetaDefinition;
import org.wso2.carbon.device.mgt.devicetype.api.dto.Feature;
import org.wso2.carbon.device.mgt.devicetype.api.dto.MetadataEntry;
import org.wso2.carbon.device.mgt.devicetype.api.dto.PushNotificationConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Use this class for mapping model classes into JAX-RS beans.
 */
public class ModelMapper {

    public static Device map(org.wso2.carbon.device.mgt.common.Device device) {
        Device rv = new Device();
        return rv;
    }

    public static org.wso2.carbon.device.mgt.common.Device map(Device device) {
        org.wso2.carbon.device.mgt.common.Device rv = new org.wso2.carbon.device.mgt.common.Device();
        return rv;
    }
}
