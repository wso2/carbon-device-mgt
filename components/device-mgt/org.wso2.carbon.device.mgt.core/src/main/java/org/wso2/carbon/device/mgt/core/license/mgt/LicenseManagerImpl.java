/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.core.license.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.core.DeviceManagerImpl;
import org.wso2.carbon.device.mgt.core.config.license.License;
import org.wso2.carbon.device.mgt.core.config.license.LicenseConfig;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class LicenseManagerImpl implements LicenseManager {

	private static Log log = LogFactory.getLog(DeviceManagerImpl.class);
	private static final DateFormat format = new SimpleDateFormat("dd-mm-yyyy", Locale.ENGLISH);

	@Override
	public License getLicense(final String deviceType, final String languageCode)
			throws LicenseManagementException {
		License deviceLicense = new License();
		LicenseConfig licenseConfig = DeviceManagementDataHolder.getInstance().getLicenseConfig();
		for (License license : licenseConfig.getLicenses()) {
			if ((deviceType.equals(license.getName())) &&
			    (languageCode.equals(license.getLanguage()))) {
					deviceLicense = license;
					break;
			}
		}
		return deviceLicense;
	}

	@Override
	public void addLicense(License license) throws LicenseManagementException {

	}

}
