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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
 */
public class OAuthExtensionServiceComponent {

    private static final Log log = LogFactory.getLog(OAuthExtensionServiceComponent.class);
    private static final String REPOSITORY = "repository";
    private static final String CONFIGURATION = "conf";
    private static final String APIM_CONF_FILE = "api-manager.xml";


    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Starting OAuthExtensionBundle");
        }
        try {

            APIManagerConfiguration configuration = new APIManagerConfiguration();
            String filePath = new StringBuilder().
                    append(CarbonUtils.getCarbonHome()).
                    append(File.separator).
                    append(REPOSITORY).
                    append(File.separator).
                    append(CONFIGURATION).
                    append(File.separator).
                    append(APIM_CONF_FILE).toString();

            configuration.load(filePath);
            // loading white listed scopes
            List<String> whiteList;

            // Read scope whitelist from Configuration.
            whiteList = configuration.getProperty(APIConstants.WHITELISTED_SCOPES);

            // If whitelist is null, default scopes will be put.
            if (whiteList == null) {
                whiteList = new ArrayList<String>();
                whiteList.add(APIConstants.OPEN_ID_SCOPE_NAME);
                whiteList.add(APIConstants.DEVICE_SCOPE_PATTERN);
            }

            OAuthExtensionsDataHolder.getInstance().setWhitelistedScopes(whiteList);
        } catch (APIManagementException e) {
            log.error("Error occurred while loading DeviceMgtConfig configurations", e);
        }
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


}
