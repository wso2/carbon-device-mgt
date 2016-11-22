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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.device.mgt.oauth.extensions.internal.OAuthExtensionsDataHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import javax.cache.Caching;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class holds util methods used by OAuth extension bundle.
 */
public class OAuthExtUtils {

    private static final Log log = LogFactory.getLog(OAuthExtUtils.class);
    private static final String DEFAULT_SCOPE_NAME = "default";
    private static final String UI_EXECUTE = "ui.execute";
    private static final String REST_API_SCOPE_CACHE = "REST_API_SCOPE_CACHE";
    private static final int START_INDEX = 0;
    private static final String DEFAULT_SCOPE_TAG = "device-mgt";

    /**
     * This method is used to get the tenant id when given tenant domain.
     *
     * @param tenantDomain Tenant domain name.
     * @return Returns the tenant id.
     */
    public static int getTenantId(String tenantDomain) {
        int tenantId = 0;
        if (tenantDomain != null) {
            try {
                TenantManager tenantManager =
                        OAuthExtensionsDataHolder.getInstance().getRealmService().getTenantManager();
                tenantId = tenantManager.getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                String errorMsg = "Error when getting the tenant id from the tenant domain : " +
                        tenantDomain;
                log.error(errorMsg, e);
            }
        }
        return tenantId;
    }

    /**
     * This method is used to set scopes that are authorized to the OAuth token request message context.
     *
     * @param tokReqMsgCtx OAuth token request message context
     * @return Returns true if success.
     */
    public static boolean setScopes(OAuthTokenReqMessageContext tokReqMsgCtx) {
        String[] requestedScopes = tokReqMsgCtx.getScope();
        String[] defaultScope = new String[]{DEFAULT_SCOPE_NAME};

        //If no scopes were requested.
        if (requestedScopes == null || requestedScopes.length == 0) {
            tokReqMsgCtx.setScope(defaultScope);
            return true;
        }

        String consumerKey = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
        List<String> reqScopeList = Arrays.asList(requestedScopes);
        Map<String, String> restAPIScopesOfCurrentTenant;

        try {

            Map<String, String> appScopes;
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

            //Get all the scopes and permissions against the scopes defined for the APIs subscribed to the application.
            appScopes = apiMgtDAO.getScopeRolesOfApplication(consumerKey);

            //Add API Manager rest API scopes set. This list should be loaded at server start up and keep
            //in memory and add it to each and every request coming.
            String tenantDomain = tokReqMsgCtx.getAuthorizedUser().getTenantDomain();
            restAPIScopesOfCurrentTenant = (Map) Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                    .getCache(REST_API_SCOPE_CACHE)
                    .get(tenantDomain);
            if (restAPIScopesOfCurrentTenant != null) {
                appScopes.putAll(restAPIScopesOfCurrentTenant);
            } else {
                restAPIScopesOfCurrentTenant = APIUtil.
                        getRESTAPIScopesFromConfig(APIUtil.getTenantRESTAPIScopesConfig(tenantDomain));

                //then put cache
                appScopes.putAll(restAPIScopesOfCurrentTenant);
                Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                        .getCache(REST_API_SCOPE_CACHE)
                        .put(tenantDomain, restAPIScopesOfCurrentTenant);
            }
            //If no scopes can be found in the context of the application
            if (appScopes.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("No scopes defined for the Application " +
                                      tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId());
                }

                String[] allowedScopes = getAllowedScopes(reqScopeList);
                tokReqMsgCtx.setScope(allowedScopes);
                return true;
            }

            // check for authorized scopes
            List<String> authorizedScopes = getAuthorizedScopes(tokReqMsgCtx, reqScopeList, appScopes);

            if (!authorizedScopes.isEmpty()) {
                String[] authScopesArr = authorizedScopes.toArray(new String[authorizedScopes.size()]);
                tokReqMsgCtx.setScope(authScopesArr);
            } else {
                tokReqMsgCtx.setScope(defaultScope);
            }
        } catch (APIManagementException e) {
            log.error("Error while getting scopes of application " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Determines if the scope is specified in the white list.
     *
     * @param scope - The scope key to check
     * @return - 'true' if the scope is white listed. 'false' if not.
     */
    private static boolean isWhiteListedScope(String scope) {
        // load white listed scopes
        List<String> scopeSkipList = OAuthExtensionsDataHolder.getInstance().getWhitelistedScopes();
        for (String scopeTobeSkipped : scopeSkipList) {
            if (scope.matches(scopeTobeSkipped)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the set of default scopes. If a requested scope is matches with the patterns specified in the white list,
     * then such scopes will be issued without further validation. If the scope list is empty,
     * token will be issued for default scope.
     *
     * @param requestedScopes - The set of requested scopes
     * @return - The subset of scopes that are allowed
     */
    private static String[] getAllowedScopes(List<String> requestedScopes) {
        List<String> authorizedScopes = new ArrayList<>();

        //Iterate the requested scopes list.
        for (String scope : requestedScopes) {
            if (isWhiteListedScope(scope)) {
                authorizedScopes.add(scope);
            }
        }
        if (authorizedScopes.isEmpty()) {
            authorizedScopes.add(DEFAULT_SCOPE_NAME);
        }
        return authorizedScopes.toArray(new String[authorizedScopes.size()]);
    }

    /**
     * This method is used to get the authorized scopes out of requested scopes. It checks requested scopes with app
     * scopes whether user has permissions to take actions for the requested scopes.
     *
     * @param tokReqMsgCtx OAuth token request message context.
     * @param reqScopeList Requested scope list.
     * @param appScopes    App scopes.
     * @return Returns a list of scopes.
     */
    private static List<String> getAuthorizedScopes(OAuthTokenReqMessageContext tokReqMsgCtx, List<String> reqScopeList,
                                                    Map<String, String> appScopes) {

        boolean status;
        List<String> authorizedScopes = new ArrayList<>();

        int tenantId;
        String username = tokReqMsgCtx.getAuthorizedUser().getUserName();
        String tenantDomain = tokReqMsgCtx.getAuthorizedUser().getTenantDomain();
        RealmService realmService = OAuthExtensionsDataHolder.getInstance().getRealmService();

        try {
            tenantId = realmService.getTenantManager().getTenantId(tenantDomain);

            // If tenant Id is not set in the tokenReqContext, deriving it from username.
            if (tenantId == 0 || tenantId == -1) {
                tenantId = IdentityTenantUtil.getTenantIdOfUser(username);
            }

            UserRealm userRealm = OAuthExtensionsDataHolder.getInstance().getRealmService().getTenantUserRealm(
                    tenantId);

            //Iterate the requested scopes list.
            for (String scope : reqScopeList) {
                status = false;

                //Get the set of roles associated with the requested scope.
                String appPermissions = appScopes.get(scope);

                //If the scope has been defined in the context of the App and if permissions have been defined for
                // the scope
                if (appPermissions != null && appPermissions.length() != 0) {
                    List<String> permissions = new ArrayList<>(Arrays.asList(appPermissions.replaceAll(" ", "").split(
                            ",")));

                    //Check if user has at least one of the permission associated with the scope
                    if (!permissions.isEmpty()) {
                        for (String permission : permissions) {
                            if (userRealm != null && userRealm.getAuthorizationManager() != null) {
                                String userStore = tokReqMsgCtx.getAuthorizedUser().getUserStoreDomain();

                                if (userStore != null) {
                                    status = userRealm.getAuthorizationManager()
                                            .isUserAuthorized(userStore + "/" + username, permission, UI_EXECUTE);
                                } else {
                                    status = userRealm.getAuthorizationManager()
                                            .isUserAuthorized(username, permission, UI_EXECUTE);
                                }
                                if (status) {
                                    break;
                                }
                            }
                        }
                        if (status) {
                            authorizedScopes.add(scope);
                        }
                    }
                }

                //The scope string starts with 'device_'.
                else if (appScopes.containsKey(scope) || isWhiteListedScope(scope)) {
                    authorizedScopes.add(scope);
                }
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while initializing user store.", e);
        }
        return authorizedScopes;
    }

    public static String extractUserName(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        String trimmedName = username.trim();
        return trimmedName.substring(START_INDEX, trimmedName.lastIndexOf('@'));
    }

}
