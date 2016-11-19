package org.wso2.carbon.device.mgt.jaxrs.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.util.List;

@ApiModel(value = "AuthorizationRequest", description = "Authorization details together with deviceIdentifier and permission")
public class AuthorizationRequest {

    @ApiModelProperty(name = "tenantDomain", value = "tenant domain.", required = false)
    String tenantDomain;
    @ApiModelProperty(name = "username", value = "username of the user, to whom the device identifiers needs to be verified", required = true)
    String username;
    @ApiModelProperty(name = "deviceIdentifiers", value = "list of devices that needs to be verified against the user", required = true)
    List<DeviceIdentifier> deviceIdentifiers;
    @ApiModelProperty(name = "permission", value = "if null then checks against the owner else it could be grouping permission", required = false)
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
