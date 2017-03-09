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

package org.wso2.carbon.device.mgt.oauth.extensions.internal;

import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.validators.OAuth2ScopeValidator;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This holds the OSGi service references required for oauth extensions bundle.
 */
public class OAuthExtensionsDataHolder {

    private RealmService realmService;
    private OAuth2TokenValidationService oAuth2TokenValidationService;
    private List<String> whitelistedScopes;
    private Map<String, OAuth2ScopeValidator> scopeValidators;

    private static OAuthExtensionsDataHolder thisInstance = new OAuthExtensionsDataHolder();

    private OAuthExtensionsDataHolder() {
        scopeValidators = new HashMap<>();
    }

    public static OAuthExtensionsDataHolder getInstance() {
        return thisInstance;
    }

    public RealmService getRealmService() {
        if (realmService == null) {
            throw new IllegalStateException("Realm service is not initialized properly");
        }
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public OAuth2TokenValidationService getoAuth2TokenValidationService() {
        if (oAuth2TokenValidationService == null) {
            throw new IllegalStateException("OAuth2TokenValidation service is not initialized properly");
        }
        return oAuth2TokenValidationService;
    }

    public void setoAuth2TokenValidationService(
            OAuth2TokenValidationService oAuth2TokenValidationService) {
        this.oAuth2TokenValidationService = oAuth2TokenValidationService;
    }

    public List<String> getWhitelistedScopes() {
        return whitelistedScopes;
    }

    public void setWhitelistedScopes(List<String> whitelistedScopes) {
        this.whitelistedScopes = whitelistedScopes;
    }

    public Map<String, OAuth2ScopeValidator> getScopeValidators() {
        return scopeValidators;
    }

    public void setScopeValidators(Map<String, OAuth2ScopeValidator> scopeValidators) {
        this.scopeValidators = scopeValidators;
    }

    public void addScopeValidator(OAuth2ScopeValidator oAuth2ScopeValidator, String prefix) {
        scopeValidators.put(prefix, oAuth2ScopeValidator);
    }

    public void removeScopeValidator() {
        scopeValidators = null;
    }

}
