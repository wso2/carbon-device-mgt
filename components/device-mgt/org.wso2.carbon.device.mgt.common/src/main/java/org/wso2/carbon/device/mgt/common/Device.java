/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.common;

import java.io.Serializable;
import java.util.List;

@XmlRootElement
public class Device implements Serializable{

	private static final long serialVersionUID = 1998101711L;

	private int id;
    private String name;
	private String type;
	private String description;
	private String deviceIdentifier;
	private int groupId;
    private EnrolmentInfo enrolmentInfo;
    private List<Feature> features;
    private List<Device.Property> properties;

    public Device() {}

    public Device(String name, String type, String description, String deviceId, EnrolmentInfo enrolmentInfo,
                  List<Feature> features, List<Property> properties) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.deviceIdentifier = deviceId;
        this.enrolmentInfo = enrolmentInfo;
        this.features = features;
        this.properties = properties;
    }

    public Device(String name, String type, String description, String deviceId, int groupId, EnrolmentInfo enrolmentInfo,
                  List<Feature> features, List<Property> properties) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.deviceIdentifier = deviceId;
        this.groupId = groupId;
        this.enrolmentInfo = enrolmentInfo;
        this.features = features;
        this.properties = properties;
    }

	@XmlElement
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	@XmlElement
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

	@XmlElement
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@XmlElement
	public String getDeviceIdentifier() {
		return deviceIdentifier;
	}

	public void setDeviceIdentifier(String deviceIdentifier) {
		this.deviceIdentifier = deviceIdentifier;
	}

	@XmlElement
    public EnrolmentInfo getEnrolmentInfo() {
        return enrolmentInfo;
    }

    public void setEnrolmentInfo(EnrolmentInfo enrolmentInfo) {
        this.enrolmentInfo = enrolmentInfo;
    }

	@XmlElement
	public List<Feature> getFeatures() {
		return features;
	}

	public void setFeatures(List<Feature> features) {
		this.features = features;
	}

	@XmlElement
	public List<Device.Property> getProperties() {
		return properties;
	}

	public void setProperties(List<Device.Property> properties) {
		this.properties = properties;
	}

	@XmlElement
	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public static class Property {

		private String name;
		private String value;

		@XmlElement
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@XmlElement
		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

    @Override
    public String toString() {
        return "Device[" +
                "name=" + name + ";" +
                "type=" + type + ";" +
                "description=" + description + ";" +
                "identifier=" + deviceIdentifier + ";" +
//                "EnrolmentInfo[" +
//                "owner=" + enrolmentInfo.getOwner() + ";" +
//                "ownership=" + enrolmentInfo.getOwnership() + ";" +
//                "status=" + enrolmentInfo.getStatus() + ";" +
//                "]" +
                "]";
    }

}
