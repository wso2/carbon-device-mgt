/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.device.mgt.group.core.common;

import org.wso2.carbon.device.mgt.group.common.DeviceGroup;
import org.wso2.carbon.device.mgt.group.core.internal.DeviceGroupBroker;

import java.util.Date;

public class TestDataHolder {

    public static Integer SUPER_TENANT_ID = -1234;
    public static String OWNER = "admin";

    public static DeviceGroup generateDummyGroupData() {
        DeviceGroup deviceGroup = new DeviceGroup();
        deviceGroup.setName("Test device group");
        deviceGroup.setDescription("Test description");
        deviceGroup.setDateOfCreation(new Date().getTime());
        deviceGroup.setDateOfLastUpdate(new Date().getTime());
        deviceGroup.setOwner(OWNER);
        DeviceGroupBroker broker = new DeviceGroupBroker(deviceGroup);
        return broker.getGroup();
    }

}
