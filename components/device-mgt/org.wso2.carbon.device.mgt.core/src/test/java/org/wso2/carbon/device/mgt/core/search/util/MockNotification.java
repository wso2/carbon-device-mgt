/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.search.util;

import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;

public class MockNotification {

    public  Notification getNotification(int notificationId,String status,String deviceId,String deviceName,
                                         int operationId, String deviceType){
        Notification notification = new Notification();
        notification.setNotificationId(notificationId);
        notification.setStatus(status);
        notification.setDeviceIdentifier(deviceId);
        notification.setDescription("test description");
        notification.setDeviceName(deviceName);
        notification.setOperationId(operationId);
        notification.setDeviceType(deviceType);
        return notification;
    }
}

