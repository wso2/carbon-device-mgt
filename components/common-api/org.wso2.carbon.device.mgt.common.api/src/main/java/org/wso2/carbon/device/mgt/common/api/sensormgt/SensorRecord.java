package org.wso2.carbon.device.mgt.common.api.sensormgt;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class SensorRecord implements Serializable{
	//sensor value float, int, boolean all should be converted into string
	private String sensorValue;
	private long time;

	public SensorRecord(String sensorValue, long time) {
		this.sensorValue = sensorValue;
		this.time = time;
	}

	@XmlElement
	public String getSensorValue() {
		return sensorValue;
	}

	@XmlElement
	public long getTime() {
		return time;
	}

}