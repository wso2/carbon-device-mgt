package org.wso2.carbon.device.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "DeviceOrganizationVisEdge", description = "This class contains details used to generate a edge in " +
        "the visualization")
public class DeviceOrganizationVisEdge implements Serializable {
    @ApiModelProperty(name = "from", value = "The origin node index of the link",
            required = true)
    private int from;

    @ApiModelProperty(name = "to", value = "The destination node index of the link",
            required = true)
    private int to;

    public DeviceOrganizationVisEdge(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }
}
