package org.wso2.carbon.device.mgt.oauth.extensions;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.util.List;

/**
 * This class holds the request format for device for grant type.
 */
public class DeviceRequestDTO {

    private List<DeviceIdentifier> deviceIdentifiers;
    private String scope;

    public List<DeviceIdentifier> getDeviceIdentifiers() {
        return deviceIdentifiers;
    }

    public void setDeviceIdentifiers(List<DeviceIdentifier> deviceIdentifiers) {
        this.deviceIdentifiers = deviceIdentifiers;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
