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

package org.wso2.carbon.dynamic.client.web.app.registration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationException;
import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationService;
import org.wso2.carbon.dynamic.client.registration.OAuthApplicationInfo;
import org.wso2.carbon.dynamic.client.registration.profile.RegistrationProfile;
import org.wso2.carbon.dynamic.client.web.app.registration.internal.DynamicClientRegistrationDataHolder;
import org.wso2.carbon.dynamic.client.web.app.registration.util.DynamicClientWebAppRegistrationUtil;

/**
 * This class contains the logic to handle the OAuth application creation process.
 */
public class DynamicRegistrationManager {

    private static DynamicRegistrationManager dynamicRegistrationManager;
    private static final Log log =
            LogFactory.getLog(DynamicRegistrationManager.class);

    private DynamicRegistrationManager() {
    }

    public static DynamicRegistrationManager getInstance() {
        if (dynamicRegistrationManager == null) {
            synchronized (DynamicRegistrationManager.class) {
                if (dynamicRegistrationManager == null) {
                    dynamicRegistrationManager = new DynamicRegistrationManager();
                }
            }
        }
        return dynamicRegistrationManager;
    }

    public boolean registerOAuthApplication(RegistrationProfile registrationProfile) {
        DynamicClientRegistrationService dynamicClientRegistrationService =
                DynamicClientRegistrationDataHolder.getInstance()
                                                   .getDynamicClientRegistrationService();
        try {
            OAuthApplicationInfo oAuthApplicationInfo =
                    dynamicClientRegistrationService.registerOAuthApplication(registrationProfile);
            OAuthApp oAuthApp = new OAuthApp();
            oAuthApp.setWebAppName(registrationProfile.getClientName());
            oAuthApp.setClientName(oAuthApplicationInfo.getClientName());
            oAuthApp.setClientKey(oAuthApplicationInfo.getClientId());
            oAuthApp.setClientSecret(oAuthApplicationInfo.getClientSecret());
            //store it in registry
            return DynamicClientWebAppRegistrationUtil.putOAuthApplicationData(oAuthApp);
        } catch (DynamicClientRegistrationException e) {
           log.error("Error occurred while registering the OAuth application.",e);
        }
        return false;
    }

    public OAuthApp getOAuthApplicationData(String clientName) {
        try {
            return DynamicClientWebAppRegistrationUtil.getOAuthApplicationData(clientName);
        } catch (DynamicClientRegistrationException e) {
            log.error("Error occurred while fetching the OAuth application data for web app : " + clientName, e);
        }
        return new OAuthApp();
    }

    public boolean isRegisteredOAuthApplication(String clientName) {
        OAuthApp oAuthApp = this.getOAuthApplicationData(clientName);
        if (oAuthApp.getClientKey() != null && oAuthApp.getClientSecret() != null) {
            return true;
        }
        return false;
    }

}
