/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.etc.sensormgt;

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