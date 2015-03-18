/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
*/
package org.wso2.carbon.device.mgt.user.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.user.common.Claims;
import org.wso2.carbon.device.mgt.user.common.Role;
import org.wso2.carbon.device.mgt.user.common.User;
import org.wso2.carbon.device.mgt.user.common.UserManagementException;
import org.wso2.carbon.device.mgt.user.core.internal.DeviceMgtUserDataHolder;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.util.ArrayList;
import java.util.List;

public class UserManagerImpl implements UserManager {

    private static Log log = LogFactory.getLog(UserManagerImpl.class);

    @Override
    public List<User> getUsersForTenantAndRole(int tenantId, String roleName) throws UserManagementException {

        UserStoreManager userStoreManager;
        String[] userNames;
        ArrayList usersList = new ArrayList();

        try {
            userStoreManager = DeviceMgtUserDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();

            userNames = userStoreManager.getUserListOfRole(roleName);
            User newUser;
            for (String userName : userNames) {
                newUser = new User(userName);
                setUserClaims(newUser, userStoreManager.getUserClaimValues(userName, null));
                usersList.add(newUser);
            }
        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in fetching user list for role and tenant tenant id:" + tenantId
                    + " role name:" + roleName;
            log.error(errorMsg, userStoreEx);
            throw new UserManagementException(errorMsg, userStoreEx);
        }
        return usersList;
    }

    @Override
    public List<Role> getRolesForTenant(int tenantId) throws UserManagementException {

        String[] roleNames;
        ArrayList<Role> rolesList = new ArrayList<Role>();
        Role newRole;
        try {
            UserStoreManager userStoreManager = DeviceMgtUserDataHolder.getInstance().getRealmService()
                    .getTenantUserRealm(tenantId)
                    .getUserStoreManager();

            roleNames = userStoreManager.getRoleNames();
            for (String roleName : roleNames) {
                newRole = new Role(roleName);
                rolesList.add(newRole);
            }

        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in fetching user list for role and tenant tenant id:" + tenantId;
            log.error(errorMsg, userStoreEx);
            throw new UserManagementException(errorMsg, userStoreEx);
        }
        return rolesList;
    }

    @Override
    public List<User> getUsersForTenant(int tenantId) throws UserManagementException {

        UserStoreManager userStoreManager;
        String[] userNames;
        ArrayList usersList = new ArrayList();

        try {
            userStoreManager = DeviceMgtUserDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();

            userNames = userStoreManager.listUsers("",-1);
            User newUser;
            for (String userName : userNames) {
                newUser = new User(userName);
                setUserClaims(newUser, userStoreManager.getUserClaimValues(userName, null));
                usersList.add(newUser);
            }
        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in fetching user list for tenant id:" + tenantId;
            log.error(errorMsg, userStoreEx);
            throw new UserManagementException(errorMsg, userStoreEx);
        }

        return usersList;
    }

    private void setUserClaims(User newUser, Claim[] userClaimValues) {

        Claims userClaims;
        ArrayList<Claims> claimsList = new ArrayList<Claims>();
        for (Claim claim : userClaimValues) {
            userClaims = new Claims();
            userClaims.setClaimUrl(claim.getClaimUri());
            userClaims.setDescription(claim.getDescription());
            userClaims.setDialectUrl(claim.getDialectURI());
            userClaims.setValue(claim.getValue());
            claimsList.add(userClaims);
        }
        newUser.setClaimList(claimsList);
    }

}
