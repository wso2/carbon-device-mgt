package org.wso2.carbon.device.mgt.etc.util;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

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
