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
package org.wso2.carbon.device.mgt.common.configuration.mgt;

/**
 * This represents the tenant configuration management functionality which should be implemented by
 * the device type plugins.
 */
public interface PlatformConfigurationManagementService {

	/**
	 * Method to add a operation to a device or a set of devices.
	 *
	 * @param platformConfiguration Operation to be added.
	 * @param resourcePath Registry resource path.
	 * @throws org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException If some unusual behaviour is observed while adding the
	 * configuration.
	 */
	 boolean saveConfiguration(PlatformConfiguration platformConfiguration,
                               String resourcePath) throws ConfigurationManagementException;

	/**
	 * Method to retrieve the list of general tenant configurations.
	 *
	 * @param resourcePath Registry resource path.
	 * @throws org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException If some unusual behaviour is observed while fetching the
	 * operation list.
	 */
	 PlatformConfiguration getConfiguration(String resourcePath) throws ConfigurationManagementException;

}
