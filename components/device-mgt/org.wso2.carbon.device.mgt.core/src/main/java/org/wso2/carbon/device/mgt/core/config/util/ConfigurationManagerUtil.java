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
package org.wso2.carbon.device.mgt.core.config.util;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;

public class ConfigurationManagerUtil {

	public static Registry getConfigurationRegistry() throws ConfigurationManagementException {
		try {
			int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
			return DeviceManagementDataHolder.getInstance().getRegistryService()
			                                 .getConfigSystemRegistry(
					                                 tenantId);
		} catch (RegistryException e) {
			throw new ConfigurationManagementException(
					"Error in retrieving governance registry instance: " +
					e.getMessage(), e);
		}
	}

	public static Resource getRegistryResource(String path) throws ConfigurationManagementException {
		try {
			if(ConfigurationManagerUtil.getConfigurationRegistry().resourceExists(path)){
				return ConfigurationManagerUtil.getConfigurationRegistry().get(path);
			}
			return null;
		} catch (RegistryException e) {
			throw new ConfigurationManagementException("Error in retrieving registry resource : " +
			                                         e.getMessage(), e);
		}
	}

	public static boolean putRegistryResource(String path,
	                                          Resource resource)
			throws ConfigurationManagementException {
		boolean status;
		try {
			ConfigurationManagerUtil.getConfigurationRegistry().beginTransaction();
			ConfigurationManagerUtil.getConfigurationRegistry().put(path, resource);
			ConfigurationManagerUtil.getConfigurationRegistry().commitTransaction();
			status = true;
		} catch (RegistryException e) {
			throw new ConfigurationManagementException(
					"Error occurred while persisting registry resource : " +
					e.getMessage(), e);
		}
		return status;
	}
}
