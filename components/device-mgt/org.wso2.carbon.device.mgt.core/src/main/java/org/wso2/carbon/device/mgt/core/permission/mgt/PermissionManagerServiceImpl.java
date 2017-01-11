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

package org.wso2.carbon.device.mgt.core.permission.mgt;

import org.wso2.carbon.device.mgt.common.permission.mgt.Permission;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagementException;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagerService;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * This class will add, update custom permissions defined in permission.xml in webapps and it will
 * use Registry as the persistence storage.
 */
public class PermissionManagerServiceImpl implements PermissionManagerService {

    public static final String URL_PROPERTY = "URL";
    public static final String HTTP_METHOD_PROPERTY = "HTTP_METHOD";
    private static PermissionManagerServiceImpl registryBasedPermissionManager;
    private static PermissionTree permissionTree; // holds the permissions at runtime.

    private PermissionManagerServiceImpl() {
    }

    public static PermissionManagerServiceImpl getInstance() {
        if (registryBasedPermissionManager == null) {
            synchronized (PermissionManagerServiceImpl.class) {
                if (registryBasedPermissionManager == null) {
                    registryBasedPermissionManager = new PermissionManagerServiceImpl();
                    permissionTree = new PermissionTree();
                }
            }
        }
        return registryBasedPermissionManager;
    }

    @Override
    public boolean addPermission(Permission permission) throws PermissionManagementException {
        // adding a permission to the tree
        permission.setPath(permission.getPath());
        permissionTree.addPermission(permission);
        return PermissionUtils.putPermission(permission);
    }

    @Override
    public Permission getPermission(Properties properties) throws PermissionManagementException {
        String url = (String) properties.get(URL_PROPERTY);
        String httpMethod = (String) properties.get(HTTP_METHOD_PROPERTY);

        if (url == null || url.isEmpty() || httpMethod == null || httpMethod.isEmpty()) {
            throw new PermissionManagementException("Resource URI/HTTP method is empty");
        }
        return permissionTree.getPermission(url, httpMethod);
    }
}
