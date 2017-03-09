/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.oauth.extensions.validators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.oauth.extensions.internal.OAuthExtensionsDataHolder;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dao.TokenMgtDAO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.validators.OAuth2ScopeValidator;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class PermissionBasedScopeValidator extends OAuth2ScopeValidator {

    private static final Log log = LogFactory.getLog(PermissionBasedScopeValidator.class);
    private static final String UI_EXECUTE = "ui.execute";


    @Override
    public boolean validateScope(AccessTokenDO accessTokenDO, String resourceScope) throws IdentityOAuth2Exception {
        //Get the list of scopes associated with the access token
        String[] scopes = accessTokenDO.getScope();

        //If no scopes are associated with the token
        if (scopes == null || scopes.length == 0) {
            return true;
        }

        TokenMgtDAO tokenMgtDAO = new TokenMgtDAO();

        List<String> scopeList = new ArrayList<>(Arrays.asList(scopes));

        //If the access token does not bear the scope required for accessing the Resource.
        if(!scopeList.contains(resourceScope)){
            if(log.isDebugEnabled()){
                log.debug("Access token '" + accessTokenDO.getAccessToken() + "' does not bear the scope '" +
                        resourceScope + "'");
            }
            return false;
        }

        try {
            //Get the permissions associated with the scope, if any
            Set<String> permissionsOfScope = tokenMgtDAO.getRolesOfScopeByScopeKey(resourceScope);

            //If the scope doesn't have any permissions associated with it.
            if(permissionsOfScope == null || permissionsOfScope.isEmpty()){
                if(log.isDebugEnabled()){
                    log.debug("Did not find any roles associated to the scope " + resourceScope);
                }
                return true;
            }

            if(log.isDebugEnabled()){
                StringBuilder logMessage = new StringBuilder("Found permissions of scope '" + resourceScope + "' ");
                for(String permission : permissionsOfScope){
                    logMessage.append(permission);
                    logMessage.append(", ");
                }
                log.debug(logMessage.toString());
            }

            User authorizedUser = accessTokenDO.getAuthzUser();
            RealmService realmService = OAuthExtensionsDataHolder.getInstance().getRealmService();

            int tenantId = realmService.getTenantManager().getTenantId(authorizedUser.getTenantDomain());

            if (tenantId == 0 || tenantId == -1) {
                tenantId = IdentityTenantUtil.getTenantIdOfUser(authorizedUser.getUserName());
            }

            AuthorizationManager authorizationManager;
            String[] userRoles;
            boolean tenantFlowStarted = false;

            try{
                //If this is a tenant user
                if(tenantId != MultitenantConstants.SUPER_TENANT_ID){
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                            realmService.getTenantManager().getDomain(tenantId),true);
                    tenantFlowStarted = true;
                }

                authorizationManager = realmService.getTenantUserRealm(tenantId).getAuthorizationManager();

            } finally {
                if (tenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
            boolean status = false;
            String username = MultitenantUtils.getTenantAwareUsername(authorizedUser.getUserName());
            for (String permission : permissionsOfScope) {
                if (authorizationManager != null) {
                    String userStore = authorizedUser.getUserStoreDomain();

                    if (userStore != null) {
                        status = authorizationManager
                                .isUserAuthorized(userStore + "/" + username, permission, UI_EXECUTE);
                    } else {
                        status = authorizationManager.isUserAuthorized(username , permission, UI_EXECUTE);
                    }
                    if (status) {
                        break;
                    }
                }
            }

            if (status) {
                if(log.isDebugEnabled()){
                    log.debug("User '" + authorizedUser.getUserName() + "' is authorized");
                }
                return true;
            }

            if(log.isDebugEnabled()){
                log.debug("No permissions associated for the user " + authorizedUser.getUserName());
            }
            return false;

        } catch (UserStoreException e) {
            //Log and return since we do not want to stop issuing the token in case of scope validation failures.
            log.error("Error when getting the tenant's UserStoreManager or when getting roles of user ", e);
            return false;
        }
    }

}
