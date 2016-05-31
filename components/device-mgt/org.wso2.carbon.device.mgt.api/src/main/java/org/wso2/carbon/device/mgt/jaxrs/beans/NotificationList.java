/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.jaxrs.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;

import java.util.List;

@ApiModel(value = "notificationList")
public class NotificationList extends BasePaginatedResult {

    private List<Notification> notifications;

    @JsonProperty("notifications")
    @ApiModelProperty("notifications")
    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("  count: ").append(getCount()).append(",");
        sb.append("  next: ").append(getNext()).append(",");
        sb.append("  previous: ").append(getPrevious()).append(",");
        sb.append("  notifications: [").append(notifications).append("");
        sb.append("]}");
        return sb.toString();
    }


}
