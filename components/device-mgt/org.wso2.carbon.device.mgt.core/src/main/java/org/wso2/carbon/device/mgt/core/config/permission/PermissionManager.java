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

import org.wso2.carbon.device.mgt.common.DeviceManagementException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.List;

/**
 * This class will add, update custom permissions defined in permission.xml in webapps.
 */
public class PermissionManager {

	private static PermissionManager permissionManager;

	public static PermissionManager getInstance() {
		if (permissionManager == null) {
			synchronized (PermissionManager.class) {
				if (permissionManager == null) {
					permissionManager = new PermissionManager();
				}
			}
		}
		return permissionManager;
	}

	public boolean addPermission(Permission permission) throws DeviceManagementException {
		try {
			return PermissionUtils.putPermission(permission);
		} catch (DeviceManagementException e) {
			throw new DeviceManagementException("Error occurred while adding the permission : " +
			                                    permission.getName(), e);
		}
	}

	public boolean addPermissions(List<Permission> permissions) throws DeviceManagementException{
		for(Permission permission:permissions){
			this.addPermission(permission);
		}
		return true;
	}

	public void initializePermissions(InputStream permissionStream) throws DeviceManagementException {
		try {
			if(permissionStream != null){
				/* Un-marshaling Device Management configuration */
				JAXBContext cdmContext = JAXBContext.newInstance(PermissionConfiguration.class);
				Unmarshaller unmarshaller = cdmContext.createUnmarshaller();
				PermissionConfiguration permissionConfiguration = (PermissionConfiguration)
						unmarshaller.unmarshal(permissionStream);
				if((permissionConfiguration != null) && (permissionConfiguration.getPermissions() != null)){
					this.addPermissions(permissionConfiguration.getPermissions());
				}
			}
		} catch (JAXBException e) {
			throw new DeviceManagementException("Error occurred while initializing Data Source config", e);
		}
	}
}
