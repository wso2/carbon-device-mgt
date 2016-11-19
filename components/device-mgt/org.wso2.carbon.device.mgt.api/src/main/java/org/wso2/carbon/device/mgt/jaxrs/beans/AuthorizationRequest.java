package org.wso2.carbon.device.mgt.jaxrs.beans;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.util.List;

public class AuthorizationRequest {

    String tenantDomain;
    String username;
    List<DeviceIdentifier> deviceIdentifiers;
    List<String> permissions;

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<DeviceIdentifier> getDeviceIdentifiers() {
        return deviceIdentifiers;
    }

    public void setDeviceIdentifiers(List<DeviceIdentifier> deviceIdentifiers) {
        this.deviceIdentifiers = deviceIdentifiers;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
