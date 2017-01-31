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
package org.wso2.carbon.device.mgt.oauth.extensions.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.oauth.extensions.internal.OAuthExtensionsDataHolder;
import org.wso2.carbon.identity.oauth.cache.CacheEntry;
import org.wso2.carbon.identity.oauth.cache.OAuthCache;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dao.TokenMgtDAO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.model.ResourceScopeCacheEntry;
import org.wso2.carbon.identity.oauth2.validators.OAuth2ScopeValidator;

import java.util.Map;

public class ScopeValidationHandler extends OAuth2ScopeValidator {

    private final static Log log = LogFactory.getLog(ScopeValidationHandler.class);
    private Map<String, OAuth2ScopeValidator> scopeValidators;

    private final String DEFAULT_PREFIX = "default";

    public ScopeValidationHandler() {
        scopeValidators = OAuthExtensionsDataHolder.getInstance().getScopeValidators();
    }

    public boolean validateScope(AccessTokenDO accessTokenDO, String resource) throws IdentityOAuth2Exception {

        //returns true if scope validators are not defined
        if (scopeValidators == null || scopeValidators.isEmpty()) {
            if(log.isDebugEnabled()){
                log.debug("OAuth2 scope validators are not loaded");
            }
            return true;
        }

        String resourceScope = getResourceScope(resource);

        //returns true if scope does not exist for the resource
        if (resourceScope == null) {
            if(log.isDebugEnabled()){
                log.debug("Resource '" + resource + "' is not protected with a scope");
            }
            return true;
        }

        String scope[] = resourceScope.split(":");
        String scopePrefix = scope[0];

        OAuth2ScopeValidator scopeValidator = scopeValidators.get(scopePrefix);

        if (scopeValidator == null) {
            if(log.isDebugEnabled()){
                log.debug("OAuth2 scope validator cannot be identified for '" + scopePrefix + "' scope prefix");
            }

            // loading default scope validator if matching validator is not found
            scopeValidator = scopeValidators.get(DEFAULT_PREFIX);
            if(log.isDebugEnabled()){
                log.debug("Loading default scope validator");
            }

            if (scopeValidator == null) {
                if(log.isDebugEnabled()){
                    log.debug("Default scope validator is not available");
                }
                return true;
            }
        }

        // validate scope via relevant scope validator that matches with the prefix
        return scopeValidator.validateScope(accessTokenDO, resource);
    }

    private String getResourceScope(String resource) {

        String resourceScope = null;
        boolean cacheHit = false;
        // Check the cache, if caching is enabled.
        if (OAuthServerConfiguration.getInstance().isCacheEnabled()) {
            OAuthCache oauthCache = OAuthCache.getInstance();
            OAuthCacheKey cacheKey = new OAuthCacheKey(resource);
            CacheEntry result = oauthCache.getValueFromCache(cacheKey);

            //Cache hit
            if (result instanceof ResourceScopeCacheEntry) {
                resourceScope = ((ResourceScopeCacheEntry) result).getScope();
                cacheHit = true;
            }
        }

        TokenMgtDAO tokenMgtDAO = new TokenMgtDAO();
        if (!cacheHit) {
            try {
                resourceScope = tokenMgtDAO.findScopeOfResource(resource);
            } catch (IdentityOAuth2Exception e) {
                log.error("Error occurred while retrieving scope for resource '" + resource + "'");
            }

            if (OAuthServerConfiguration.getInstance().isCacheEnabled()) {
                OAuthCache oauthCache = OAuthCache.getInstance();
                OAuthCacheKey cacheKey = new OAuthCacheKey(resource);
                ResourceScopeCacheEntry cacheEntry = new ResourceScopeCacheEntry(resourceScope);
                //Store resourceScope in cache even if it is null (to avoid database calls when accessing resources for
                //which scopes haven't been defined).
                oauthCache.addToCache(cacheKey, cacheEntry);
            }
        }
        return resourceScope;
    }

}
