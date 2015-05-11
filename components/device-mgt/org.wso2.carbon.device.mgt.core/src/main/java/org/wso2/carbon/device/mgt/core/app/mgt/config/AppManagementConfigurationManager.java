/*
*  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.device.mgt.core.app.mgt.config;

import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.common.app.mgt.AppManagerConnectorException;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class AppManagementConfigurationManager {

	private AppManagementConfig appManagementConfig;
	private static AppManagementConfigurationManager appManagementConfigManager;

	private static final String APP_MANAGER_CONFIG_FILE = "app-management-config.xml";
	private static final String APP_MANAGER_CONFIG_PATH =
			CarbonUtils.getEtcCarbonConfigDirPath() + File.separator + APP_MANAGER_CONFIG_FILE;

	public static AppManagementConfigurationManager getInstance() {
		if (appManagementConfigManager == null) {
			synchronized (AppManagementConfigurationManager.class) {
				if (appManagementConfigManager == null) {
					appManagementConfigManager = new AppManagementConfigurationManager();
				}
			}
		}
		return appManagementConfigManager;
	}

	public synchronized void initConfig() throws AppManagerConnectorException {
		try {
			File appManagementConfig =
					new File(AppManagementConfigurationManager.APP_MANAGER_CONFIG_PATH);
			Document doc = DeviceManagerUtil.convertToDocument(appManagementConfig);

            /* Un-marshaling App Management configuration */
			JAXBContext cdmContext = JAXBContext.newInstance(AppManagementConfig.class);
			Unmarshaller unmarshaller = cdmContext.createUnmarshaller();
			this.appManagementConfig = (AppManagementConfig) unmarshaller.unmarshal(doc);
		} catch (Exception e) {
		    /* Catches generic exception as there's no specific task to be carried out catching a particular
            exception */
			throw new AppManagerConnectorException(
					"Error occurred while initializing application management Configurations", e);
		}
	}

	public AppManagementConfig getAppManagementConfig() {
		return appManagementConfig;
	}

}
