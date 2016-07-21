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

package org.wso2.carbon.device.mgt.core.scope.mgt;

import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.scope.mgt.ScopeManagementException;
import org.wso2.carbon.device.mgt.common.scope.mgt.ScopeManagementService;
import org.wso2.carbon.device.mgt.core.scope.mgt.dao.ScopeManagementDAO;
import org.wso2.carbon.device.mgt.core.scope.mgt.dao.ScopeManagementDAOException;
import org.wso2.carbon.device.mgt.core.scope.mgt.dao.ScopeManagementDAOFactory;

import java.lang.annotation.Inherited;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of a Scope Management Service.
 */
public class ScopeManagementServiceImpl implements ScopeManagementService {

    private ScopeManagementDAO scopeManagementDAO;

    public ScopeManagementServiceImpl() {
        this.scopeManagementDAO = ScopeManagementDAOFactory.getScopeManagementDAO();
    }

    @Override
    public void updateScopes(List<Scope> scopes) throws ScopeManagementException {
        try{
            ScopeManagementDAOFactory.beginTransaction();
            scopeManagementDAO.updateScopes(scopes);
            ScopeManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            ScopeManagementDAOFactory.rollbackTransaction();
            throw new ScopeManagementException("Transactional error occurred while adding the scopes.", e);
        } catch (ScopeManagementDAOException e) {
            ScopeManagementDAOFactory.rollbackTransaction();
            throw new ScopeManagementException("Error occurred while adding the scopes to database.", e);
        } finally {
            ScopeManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Scope> getAllScopes() throws ScopeManagementException {
        List<Scope> scopes = new ArrayList<>();
        try{
            ScopeManagementDAOFactory.openConnection();
            scopes = scopeManagementDAO.getAllScopes();
        } catch (SQLException e) {
            throw new ScopeManagementException("SQL error occurred while adding scopes to database.", e);
        } catch (ScopeManagementDAOException e) {
            throw new ScopeManagementException("Error occurred while adding scopes to database.", e);
        } finally {
            ScopeManagementDAOFactory.closeConnection();
        }
        return scopes;
    }

}
