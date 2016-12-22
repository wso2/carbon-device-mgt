package org.wso2.carbon.device.mgt.jaxrs.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "EnrollmentInvitation", description = "Holds data to send enrollment invitation to list of recipients.")
public class EnrollmentInvitation {

    @ApiModelProperty(name = "deviceType", value = "Device type name.", required = true)
    private String deviceType;

    @ApiModelProperty(name = "recipients", value = "List of recipients.", required = true)
    private List<String> recipients;

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }
}
