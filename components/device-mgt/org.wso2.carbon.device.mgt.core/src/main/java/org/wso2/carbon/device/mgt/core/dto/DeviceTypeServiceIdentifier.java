/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.dto;

import java.io.Serializable;

/**
 * This class holds the information of the device type and its provider tenant.
 */
public class DeviceTypeServiceIdentifier implements Serializable {

	private String deviceType;
	private int tenantId;
	private static final int DEFAULT_SHARE_WITH_ALL_TENANTS_ID = -1;

	public DeviceTypeServiceIdentifier(String deviceType, int tenantId) {
		this.deviceType = deviceType;
		this.tenantId = tenantId;
	}

	public DeviceTypeServiceIdentifier(String deviceType) {
		this.deviceType = deviceType;
		this.tenantId = DEFAULT_SHARE_WITH_ALL_TENANTS_ID;
	}

	public void setTenantId(int tenantId) {
		this.tenantId = tenantId;
	}

	public String getDeviceType() {
		return this.deviceType;
	}

	public int getTenantId() {
		return this.tenantId;
	}

	@Override
	public int hashCode() {
		int result = this.deviceType.hashCode();
		result = 31 * result + ("@" + this.tenantId).hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof DeviceTypeServiceIdentifier) && deviceType.equals(
				((DeviceTypeServiceIdentifier) obj).deviceType) && tenantId == ((DeviceTypeServiceIdentifier) obj).tenantId;
	}

	public boolean isSharedWithAllTenant() {
		return tenantId == DEFAULT_SHARE_WITH_ALL_TENANTS_ID;
	}
}
