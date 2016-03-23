package org.wso2.carbon.device.mgt.common;

import java.io.Serializable;

public class DeviceTypeIdentifier implements Serializable {

	private String deviceType;
	private int tenantId;
	private static final int SHARE_WITH_ALL_TENANTS_ID = -1;

	public DeviceTypeIdentifier(String deviceType, int tenantId) {
		this.deviceType = deviceType;
		this.tenantId = tenantId;
	}

	public DeviceTypeIdentifier(String deviceType) {
		this.deviceType = deviceType;
		this.tenantId = SHARE_WITH_ALL_TENANTS_ID;
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
		return (obj instanceof DeviceTypeIdentifier) && deviceType.equals(
				((DeviceTypeIdentifier) obj).deviceType) && tenantId == ((DeviceTypeIdentifier) obj).tenantId;
	}

	public boolean isSharedWithAllTenant() {
		return tenantId == SHARE_WITH_ALL_TENANTS_ID;
	}
}
