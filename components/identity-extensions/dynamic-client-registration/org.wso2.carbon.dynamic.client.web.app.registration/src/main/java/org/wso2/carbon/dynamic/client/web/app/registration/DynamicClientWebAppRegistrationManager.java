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

import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationException;
import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationService;
import org.wso2.carbon.dynamic.client.registration.OAuthApplicationInfo;
import org.wso2.carbon.dynamic.client.registration.profile.RegistrationProfile;
import org.wso2.carbon.dynamic.client.web.app.registration.internal.DynamicClientWebAppRegistrationDataHolder;
import org.wso2.carbon.dynamic.client.web.app.registration.util.DynamicClientWebAppRegistrationConstants;
import org.wso2.carbon.dynamic.client.web.app.registration.util.DynamicClientWebAppRegistrationUtil;

import javax.servlet.ServletContext;

/**
 * This class contains the logic to handle the OAuth application creation process.
 */
public class DynamicClientWebAppRegistrationManager {

    private static DynamicClientWebAppRegistrationManager dynamicClientWebAppRegistrationManager;
    private static final Log log =
            LogFactory.getLog(DynamicClientWebAppRegistrationManager.class);

    private DynamicClientWebAppRegistrationManager() {
    }

    public static DynamicClientWebAppRegistrationManager getInstance() {
        if (dynamicClientWebAppRegistrationManager == null) {
            synchronized (DynamicClientWebAppRegistrationManager.class) {
                if (dynamicClientWebAppRegistrationManager == null) {
                    dynamicClientWebAppRegistrationManager = new DynamicClientWebAppRegistrationManager();
                }
            }
        }
        return dynamicClientWebAppRegistrationManager;
    }

    public OAuthApp registerOAuthApplication(RegistrationProfile registrationProfile) {
        if (log.isDebugEnabled()) {
            log.debug("Registering OAuth application for web app : " + registrationProfile.getClientName());
        }
        if (DynamicClientWebAppRegistrationUtil.validateRegistrationProfile(registrationProfile)) {
            DynamicClientRegistrationService dynamicClientRegistrationService =
                    DynamicClientWebAppRegistrationDataHolder.getInstance()
                                                       .getDynamicClientRegistrationService();
            try {
                OAuthApplicationInfo oAuthApplicationInfo =
                        dynamicClientRegistrationService
                                .registerOAuthApplication(registrationProfile);
                OAuthApp oAuthApp = new OAuthApp();
                oAuthApp.setWebAppName(registrationProfile.getClientName());
                oAuthApp.setClientName(oAuthApplicationInfo.getClientName());
                oAuthApp.setClientKey(oAuthApplicationInfo.getClientId());
                oAuthApp.setClientSecret(oAuthApplicationInfo.getClientSecret());
                //store it in registry
                if (DynamicClientWebAppRegistrationUtil.putOAuthApplicationData(oAuthApp)) {
                    return oAuthApp;
                } else {
                    dynamicClientRegistrationService
                            .unregisterOAuthApplication(registrationProfile.getOwner(),
                                                        oAuthApplicationInfo.getClientName(),
                                                        oAuthApplicationInfo.getClientId());
                    log.warn("Error occurred while persisting the OAuth application data in registry.");
                }
            } catch (DynamicClientRegistrationException e) {
                log.error("Error occurred while registering the OAuth application : " +
                          registrationProfile.getClientName(), e);
            }
        }
        return new OAuthApp();
    }

    public OAuthApp getOAuthApplicationData(String clientName) {
        try {
            return DynamicClientWebAppRegistrationUtil.getOAuthApplicationData(clientName);
        } catch (DynamicClientRegistrationException e) {
            log.error("Error occurred while fetching the OAuth application data for web app : " +
                      clientName, e);
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

    public void initiateDynamicClientRegistrationProcess(StandardContext context) {
        ServletContext servletContext = context.getServletContext();
        String requiredDynamicClientRegistration = servletContext.getInitParameter(
                DynamicClientWebAppRegistrationConstants.DYNAMIC_CLIENT_REQUIRED_FLAG);
        DynamicClientWebAppRegistrationManager dynamicClientWebAppRegistrationManager =
                DynamicClientWebAppRegistrationManager.getInstance();
        //Get the application name from web-context
        String webAppName = context.getBaseName();
        RegistrationProfile registrationProfile;
        OAuthApp oAuthApp = null;
        //Java web-app section
        if ((requiredDynamicClientRegistration != null) &&
            (Boolean.parseBoolean(requiredDynamicClientRegistration))) {
            //Check whether this is an already registered application
            if (!dynamicClientWebAppRegistrationManager.isRegisteredOAuthApplication(webAppName)) {
                //Construct the RegistrationProfile
                registrationProfile = DynamicClientWebAppRegistrationUtil.
                                           constructRegistrationProfile(servletContext, webAppName);
                //Register the OAuth application
                oAuthApp = dynamicClientWebAppRegistrationManager.registerOAuthApplication(
                        registrationProfile);

            } else {
                oAuthApp = dynamicClientWebAppRegistrationManager.getOAuthApplicationData(webAppName);
            }
        } else {
            //Jaggery apps
            OAuthSettings oAuthSettings = DynamicClientWebAppRegistrationUtil
                    .getJaggeryAppOAuthSettings(servletContext);
            if (oAuthSettings.isRequireDynamicClientRegistration()) {
                if (!dynamicClientWebAppRegistrationManager.isRegisteredOAuthApplication(webAppName)) {
                    registrationProfile = DynamicClientWebAppRegistrationUtil
                            .constructRegistrationProfile(oAuthSettings, webAppName);
                    oAuthApp = dynamicClientWebAppRegistrationManager
                            .registerOAuthApplication(registrationProfile);
                } else {
                    oAuthApp = dynamicClientWebAppRegistrationManager.getOAuthApplicationData(webAppName);
                }
            }
        }
        DynamicClientWebAppRegistrationUtil.addClientCredentialsToWebContext(oAuthApp,
                                                                                 servletContext);
    }

}
