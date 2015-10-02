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

package org.wso2.carbon.device.mgt.oauth.extensions;

import org.wso2.carbon.device.mgt.oauth.extensions.internal.OAuthExtensionsDataHolder;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Created by harshan on 10/2/15.
 */
public class OAuthExtensionsUtils {

    public static void getRolePermissions(String role){
        RealmService realmService = OAuthExtensionsDataHolder.getInstance().getRealmService();
        try {
           int tenantId = realmService.getTenantManager().getTenantId("tenant-domain");
           AuthorizationManager
                   authorizationManager = realmService.getTenantUserRealm(tenantId).getAuthorizationManager();
          // authorizationManager.is
        } catch (UserStoreException e) {
            e.printStackTrace();
        }
    }

    public static void getUserPermissions(String userName){

    }

    public static String[] getUserRoles(String userName){
        RealmService realmService = OAuthExtensionsDataHolder.getInstance().getRealmService();
        try {
            int tenantId = realmService.getTenantManager().getTenantId("tenant-domain");
            UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            return userStoreManager.getRoleListOfUser(userName);
        } catch (UserStoreException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    public static void getScopePermissions(String scopeKey){

    }
}
