/*
*  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.scope.mgt.dao;

import org.wso2.carbon.apimgt.api.model.Scope;

import java.util.List;

/**
 * This interface contains the basic database operations related to scope management.
 */
public interface ScopeManagementDAO {

    /**
     * This method is used to update the list of scopes.
     *
     * @param scopes List of scopes to be updated.
     * @throws ScopeManagementDAOException
     */
    void updateScopes(List<Scope> scopes) throws ScopeManagementDAOException;

    /**
     * This method is used to retrieve all the scopes.
     *
     * @return List of scopes.
     * @throws ScopeManagementDAOException
     */
    List<Scope> getAllScopes() throws ScopeManagementDAOException;

    /**
     * This method is to retrieve the roles of the given scope
     * @param scopeKey key of the scope
     * @return List of roles
     * @throws ScopeManagementDAOException
     */
    String getRolesOfScope(String scopeKey) throws ScopeManagementDAOException;

}
