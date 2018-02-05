package org.wso2.carbon.device.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "DeviceOrganizationMetadataHolder", description = "This class carries all metadata related to " +
        "device organization")
public class DeviceOrganizationMetadataHolder implements Serializable {

    @ApiModelProperty(name = "deviceId", value = "ID of the device as shown in the device organization.",
            required = true)
    private String deviceId;

    @ApiModelProperty(name = "deviceName", value = "Device name as suggested by the user.",
            required = false)
    private String deviceName;

    @ApiModelProperty(name = "parent", value = "Parent of the device as it is located in the organization.",
            required = true)
    private String parent;

    @ApiModelProperty(name = "pingMins", value = "No. of minutes since device last pinged the server.",
            required = true)
    private int pingMins;

    @ApiModelProperty(name = "state", value = "State of connectivity of the device.",
            required = true)
    private int state;

    @ApiModelProperty(name = "isGateway", value = "Defines if the device is a Gateway.",
            required = true)
    private int isGateway;

    public DeviceOrganizationMetadataHolder() {
    }

    public DeviceOrganizationMetadataHolder(String deviceId, String deviceName, String parent, int pingMins, int state, int isGateway) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.parent = parent;
        this.pingMins = pingMins;
        this.state = state;
        this.isGateway = isGateway;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public int getPingMins() {
        return pingMins;
    }

    public void setPingMins(int pingMins) {
        this.pingMins = pingMins;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getIsGateway() {
        return isGateway;
    }

    public void setIsGateway(int isGateway) {
        this.isGateway = isGateway;
    }

    @Override
    public String toString() {
        return "DeviceOrganizationMetadataHolder{" +
                "deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", parent='" + parent + '\'' +
                ", pingMins=" + pingMins +
                ", state=" + state +
                ", isGateway=" + isGateway +
                '}';
    }

}
