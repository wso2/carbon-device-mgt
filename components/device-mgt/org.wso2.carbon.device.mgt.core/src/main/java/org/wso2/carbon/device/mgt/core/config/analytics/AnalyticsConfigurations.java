/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.config.analytics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configurations related to DAS data publisher and DAL.
 */
@XmlRootElement(name = "AnalyticsConfiguration")
public class AnalyticsConfigurations {
	private String receiverServerUrl;
	private String adminUsername;
	private String adminPassword;
	private boolean enable;

	@XmlElement(name = "AdminUsername", required = true)
	public String getAdminUsername() {
		return adminUsername;
	}

	public void setAdminUsername(String adminUsername) {
		this.adminUsername = adminUsername;
	}

	@XmlElement(name = "AdminPassword", required = true)
	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	@XmlElement(name = "ReceiverServerUrl", required = true)
	public String getReceiverServerUrl() {
		return receiverServerUrl;
	}

	public void setReceiverServerUrl(String receiverServerUrl) {
		this.receiverServerUrl = receiverServerUrl;
	}

	@XmlElement(name = "Enabled", required = true)
	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean status) {
		this.enable = status;
	}

}
