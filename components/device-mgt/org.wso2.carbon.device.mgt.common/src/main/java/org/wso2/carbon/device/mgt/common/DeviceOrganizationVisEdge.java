package org.wso2.carbon.device.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "DeviceOrganizationVisEdge", description = "This class contains details used to generate a edge in " +
        "the visualization")
public class DeviceOrganizationVisEdge implements Serializable {
    @ApiModelProperty(name = "from", value = "The origin node index of the link",
            required = true)
    private String from;

    @ApiModelProperty(name = "to", value = "The destination node index of the link",
            required = true)
    private String to;

    public DeviceOrganizationVisEdge(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
