package org.wso2.carbon.device.mgt.common.api.util;

import org.wso2.carbon.device.mgt.common.Feature;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement
public class DeviceTypes implements Serializable{

	//private static final long serialVersionUID = 7526472295622776147L;

	private String name;


	public DeviceTypes() {
	}

	@XmlElement
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
