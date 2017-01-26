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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;

import org.wso2.carbon.device.mgt.oauth.extensions.validators.ExtendedJDBCScopeValidator;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.validators.JDBCScopeValidator;
import org.wso2.carbon.identity.oauth2.validators.OAuth2ScopeValidator;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.device.mgt.oauth.extensions" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="identity.oauth2.validation.service"
 * interface="org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setOAuth2ValidationService"
 * unbind="unsetOAuth2ValidationService"
 * * @scr.reference name="scope.validator.service"
 * interface="org.wso2.carbon.identity.oauth2.validators.OAuth2ScopeValidator"
 * cardinality="0..n"
 * policy="dynamic"
 * bind="addScopeValidator"
 * unbind="removeScopeValidator"
 */
public class OAuthExtensionServiceComponent {

    private static final Log log = LogFactory.getLog(OAuthExtensionServiceComponent.class);
    private static final String REPOSITORY = "repository";
    private static final String CONFIGURATION = "conf";
    private static final String APIM_CONF_FILE = "api-manager.xml";
    private static final String PERMISSION_SCOPE_PREFIX = "perm";
    private static final String DEFAULT_PREFIX = "default";


    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Starting OAuthExtensionBundle");
        }

        ExtendedJDBCScopeValidator permissionBasedScopeValidator = new ExtendedJDBCScopeValidator();
        JDBCScopeValidator roleBasedScopeValidator = new JDBCScopeValidator();
        OAuthExtensionsDataHolder.getInstance().addScopeValidator(permissionBasedScopeValidator,
                PERMISSION_SCOPE_PREFIX);
        OAuthExtensionsDataHolder.getInstance().addScopeValidator(roleBasedScopeValidator,
                DEFAULT_PREFIX);

    }

    @SuppressWarnings("unused")
    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Stopping OAuthExtensionBundle");
        }
    }

    /**
     * Sets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Realm Service");
        }
        OAuthExtensionsDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Unsets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Realm Service");
        }
        OAuthExtensionsDataHolder.getInstance().setRealmService(null);
    }

    /**
     * Sets OAuth2TokenValidation Service.
     *
     * @param tokenValidationService An instance of OAuth2TokenValidationService
     */
    protected void setOAuth2ValidationService(OAuth2TokenValidationService tokenValidationService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting OAuth2TokenValidation Service");
        }
        OAuthExtensionsDataHolder.getInstance().setoAuth2TokenValidationService(tokenValidationService);
    }

    /**
     * Unsets OAuth2TokenValidation Service.
     *
     * @param tokenValidationService An instance of OAuth2TokenValidationService
     */
    protected void unsetOAuth2ValidationService(OAuth2TokenValidationService tokenValidationService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting OAuth2TokenValidation Service");
        }
        OAuthExtensionsDataHolder.getInstance().setoAuth2TokenValidationService(null);
    }

    /**
     * Add scope validator to the map.
     * @param scopesValidator
     */
    protected void addScopeValidator(OAuth2ScopeValidator scopesValidator) {
        OAuthExtensionsDataHolder.getInstance().addScopeValidator(scopesValidator, DEFAULT_PREFIX);
    }

    /**
     * unset scope validator.
     * @param scopesValidator
     */
    protected void removeScopeValidator(OAuth2ScopeValidator scopesValidator) {
        OAuthExtensionsDataHolder.getInstance().removeScopeValidator();
    }


}
