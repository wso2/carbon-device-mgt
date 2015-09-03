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

package org.wso2.carbon.device.mgt.common.notification.mgt;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

/**
 * DTO of Notification object which is used to communicate Operation notifications to MDM core.
 */
public class Notification {

	public enum Status{
		NEW, CHECKED
	}

	public enum Type{
		ALERT,
	}

	private int notificationId;
	private DeviceIdentifier deviceIdentifier;
	private String description;
	private int operationId;
	private Status status;

	public Status getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = Status.valueOf(status);
	}

	public int getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(int notificationId) {
		this.notificationId = notificationId;
	}

	public DeviceIdentifier getDeviceIdentifier() {
		return deviceIdentifier;
	}

	public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
		this.deviceIdentifier = deviceIdentifier;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getOperationId() {
		return operationId;
	}

	public void setOperationId(int operationId) {
		this.operationId = operationId;
	}

	@Override
	public String toString() {
		return "Notification{" +
		       "notificationId='" + notificationId + '\'' +
		       ", deviceId=" + deviceIdentifier.getId() +
		       ", deviceType=" + deviceIdentifier.getType() +
		       ", status=" + status +
		       ", description='" + description + '\'' +
		       ", operationId='" + operationId + '\'' +
		       '}';
	}
}
