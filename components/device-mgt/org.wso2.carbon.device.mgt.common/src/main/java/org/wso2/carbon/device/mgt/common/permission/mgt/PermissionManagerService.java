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

package org.wso2.carbon.device.mgt.common.permission.mgt;

import java.util.List;

/**
 * This represents the Permission management functionality which should be implemented by
 * required PermissionManagers.
 */
public interface PermissionManagerService {

    /**
     *
     * @param permission - Permission to be added
     * @return The status of the operation.
     * @throws PermissionManagementException If some unusual behaviour is observed while adding the
     * permission.
     */
    boolean addPermission(Permission permission) throws PermissionManagementException;

    /**
     *
     * @param url - url of the permission to be fetched.
     * @return The matched Permission list.
     * @throws PermissionManagementException If some unusual behaviour is observed while fetching the
     * permission.
     */
    List<Permission> getPermissions(String url) throws PermissionManagementException;

}
