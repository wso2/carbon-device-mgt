/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.config.pagination;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the information related to Pagination configuration.
 */
@XmlRootElement(name = "PaginationConfiguration")
public class PaginationConfiguration {

    private int deviceListPageSize;
    private int groupListPageSize;
    private int operationListPageSize;
    private int notificationListPageSize;
    private int activityListPageSize;

    public int getDeviceListPageSize() {
        return deviceListPageSize;
    }

    @XmlElement(name = "DeviceListPageSize", required = true)
    public void setDeviceListPageSize(int deviceListPageSize) {
        this.deviceListPageSize = deviceListPageSize;
    }

    public int getGroupListPageSize() {
        return groupListPageSize;
    }

    @XmlElement(name = "GroupListPageSize", required = true)
    public void setGroupListPageSize(int groupListPageSize) {
        this.groupListPageSize = groupListPageSize;
    }

    public int getOperationListPageSize() {
        return operationListPageSize;
    }

    @XmlElement(name = "OperationListPageSize", required = true)
    public void setOperationListPageSize(int operationListPageSize) {
        this.operationListPageSize = operationListPageSize;
    }

    public int getNotificationListPageSize() {
        return notificationListPageSize;
    }

    @XmlElement(name = "NotificationListPageSize", required = true)
    public void setNotificationListPageSize(int notificationListPageSize) {
        this.notificationListPageSize = notificationListPageSize;
    }

    public int getActivityListPageSize() {
        return activityListPageSize;
    }

    @XmlElement(name = "ActivityListPageSize", required = true)
    public void setActivityListPageSize(int activityListPageSize) {
        this.activityListPageSize = activityListPageSize;
    }

}
