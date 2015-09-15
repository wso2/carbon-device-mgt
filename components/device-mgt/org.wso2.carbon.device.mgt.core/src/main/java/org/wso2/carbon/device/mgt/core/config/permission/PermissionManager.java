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
import java.util.StringTokenizer;

/**
 * This class will add, update custom permissions defined in permission.xml in webapps.
 */
public class PermissionManager {

	private static PermissionManager permissionManager;
    private static PermissionHolder rootNode;

	private PermissionManager(){};

	public static PermissionManager getInstance() {
		if (permissionManager == null) {
			synchronized (PermissionManager.class) {
				if (permissionManager == null) {
					permissionManager = new PermissionManager();
                    rootNode = new PermissionHolder("/"); // initializing the root node.
				}
			}
		}
		return permissionManager;
	}

	public boolean addPermission(Permission permission) throws DeviceManagementException {
        StringTokenizer st = new StringTokenizer(permission.getUrl(), "/");
        PermissionHolder tempRoot = rootNode;
        PermissionHolder tempChild;
        while(st.hasMoreTokens()) {
            tempChild = new PermissionHolder(st.nextToken());
            tempRoot = addPermissionNode(tempRoot, tempChild);
        }
        tempRoot.addPermission(permission.getMethod(), permission); //setting permission to the vertex
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

    private PermissionHolder addPermissionNode(PermissionHolder parent, PermissionHolder child) {
        PermissionHolder existChild = parent.getChild(child.getPathName());
        if (existChild == null) {
            parent.addChild(child);
            return child;
        }
        return existChild;
    }

    public Permission getPermission(String url, String httpMethod) {
        StringTokenizer st = new StringTokenizer(url, "/");
        PermissionHolder tempRoot = rootNode;
        PermissionHolder previousRoot;
        while (st.hasMoreTokens()) {
            String currentToken = st.nextToken();
            previousRoot = tempRoot;
            tempRoot = tempRoot.getChild(currentToken);
            if (tempRoot == null) {
                tempRoot = previousRoot;
                int leftTokens = st.countTokens();
                for (int i = 0; i <= leftTokens; i++) {
                    if (tempRoot == null) {
                        return null;
                    }
                    tempRoot = tempRoot.getChild("*");
                }
                break;
            }
        }
        if (tempRoot == null) {
            return null;
        }
        return tempRoot.getPermission(httpMethod);
    }
}
