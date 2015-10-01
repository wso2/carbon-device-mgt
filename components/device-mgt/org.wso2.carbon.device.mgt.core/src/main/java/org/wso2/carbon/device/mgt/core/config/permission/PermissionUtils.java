/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.config.permission;

import org.w3c.dom.Document;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * Utility class which holds necessary utility methods required for persisting permissions in
 * registry.
 */
public class PermissionUtils {

	public static String ADMIN_PERMISSION_REGISTRY_PATH = "/permission/admin";
	public static String PERMISSION_PROPERTY_NAME = "name";

	public static Registry getGovernanceRegistry() throws DeviceManagementException {
		try {
			int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
			return DeviceManagementDataHolder.getInstance().getRegistryService()
			                                       .getGovernanceSystemRegistry(
					                                       tenantId);
		} catch (RegistryException e) {
			throw new DeviceManagementException(
					"Error in retrieving governance registry instance: " +
					e.getMessage(), e);
		}
	}

	public static Permission getPermission(String path) throws DeviceManagementException {
		try {
			Resource resource = PermissionUtils.getGovernanceRegistry().get(path);
			Permission permission = new Permission();
			permission.setName(resource.getProperty(PERMISSION_PROPERTY_NAME));
			permission.setPath(resource.getPath());
			return permission;
		} catch (RegistryException e) {
			throw new DeviceManagementException("Error in retrieving registry resource : " +
			                                         e.getMessage(), e);
		}
	}

	public static boolean putPermission(Permission permission)
			throws DeviceManagementException {
		boolean status;
		try {
			Resource resource = PermissionUtils.getGovernanceRegistry().newCollection();
			resource.addProperty(PERMISSION_PROPERTY_NAME, permission.getName());
			PermissionUtils.getGovernanceRegistry().beginTransaction();
			PermissionUtils.getGovernanceRegistry().put(ADMIN_PERMISSION_REGISTRY_PATH +
			                                         permission.getPath(), resource);
			PermissionUtils.getGovernanceRegistry().commitTransaction();
			status = true;
		} catch (RegistryException e) {
			throw new DeviceManagementException(
					"Error occurred while persisting permission : " +
					permission.getName(), e);
		}
		return status;
	}

	public static boolean checkPermissionExistence(Permission permission)
			throws DeviceManagementException,
			       org.wso2.carbon.registry.core.exceptions.RegistryException {
		return PermissionUtils.getGovernanceRegistry().resourceExists(permission.getPath());
	}

	public static Document convertToDocument(File file) throws DeviceManagementException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			return docBuilder.parse(file);
		} catch (Exception e) {
			throw new DeviceManagementException("Error occurred while parsing file, while converting " +
			                                    "to a org.w3c.dom.Document", e);
		}
	}

}
