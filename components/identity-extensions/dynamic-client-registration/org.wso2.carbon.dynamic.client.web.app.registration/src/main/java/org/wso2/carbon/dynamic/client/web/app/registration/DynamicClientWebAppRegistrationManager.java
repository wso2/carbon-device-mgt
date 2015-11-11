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
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationException;
import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationService;
import org.wso2.carbon.dynamic.client.registration.OAuthApplicationInfo;
import org.wso2.carbon.dynamic.client.registration.profile.RegistrationProfile;
import org.wso2.carbon.dynamic.client.web.app.registration.dto.OAuthAppDetails;
import org.wso2.carbon.dynamic.client.web.app.registration.dto.JaggeryOAuthConfigurationSettings;
import org.wso2.carbon.dynamic.client.web.app.registration.internal.DynamicClientWebAppRegistrationDataHolder;
import org.wso2.carbon.dynamic.client.web.app.registration.util.DynamicClientWebAppRegistrationConstants;
import org.wso2.carbon.dynamic.client.web.app.registration.util.DynamicClientWebAppRegistrationUtil;

import javax.servlet.ServletContext;
import java.util.*;

/**
 * This class contains the logic to handle the OAuth application creation process.
 */
public class DynamicClientWebAppRegistrationManager {

    private static DynamicClientWebAppRegistrationManager dynamicClientWebAppRegistrationManager;
    private static Map<String, ServletContext> webAppContexts = new HashMap<>();

    private static final Log log = LogFactory.getLog(DynamicClientWebAppRegistrationManager.class);

    private DynamicClientWebAppRegistrationManager() {
    }

    public static DynamicClientWebAppRegistrationManager getInstance() {
        if (dynamicClientWebAppRegistrationManager == null) {
            synchronized (DynamicClientWebAppRegistrationManager.class) {
                if (dynamicClientWebAppRegistrationManager == null) {
                    dynamicClientWebAppRegistrationManager =
                            new DynamicClientWebAppRegistrationManager();
                }
            }
        }
        return dynamicClientWebAppRegistrationManager;
    }

    public OAuthAppDetails registerOAuthApplication(RegistrationProfile registrationProfile) {
        if (log.isDebugEnabled()) {
            log.debug("Registering OAuth application for web app : " + registrationProfile.getClientName());
        }
        if (DynamicClientWebAppRegistrationUtil.validateRegistrationProfile(registrationProfile)) {
            DynamicClientRegistrationService dynamicClientRegistrationService =
                    DynamicClientWebAppRegistrationDataHolder.getInstance().getDynamicClientRegistrationService();
            try {
                OAuthApplicationInfo oAuthApplicationInfo =
                        dynamicClientRegistrationService.registerOAuthApplication(registrationProfile);
                OAuthAppDetails oAuthAppDetails = new OAuthAppDetails();
                oAuthAppDetails.setWebAppName(registrationProfile.getClientName());
                oAuthAppDetails.setClientName(oAuthApplicationInfo.getClientName());
                oAuthAppDetails.setClientKey(oAuthApplicationInfo.getClientId());
                oAuthAppDetails.setClientSecret(oAuthApplicationInfo.getClientSecret());
                //store it in registry
                if (DynamicClientWebAppRegistrationUtil.putOAuthApplicationData(oAuthAppDetails)) {
                    return oAuthAppDetails;
                } else {
                    dynamicClientRegistrationService.unregisterOAuthApplication(registrationProfile.getOwner(),
                                                        oAuthApplicationInfo.getClientName(),
                                                        oAuthApplicationInfo.getClientId());
                    log.warn("Error occurred while persisting the OAuth application data in registry.");
                }
            } catch (DynamicClientRegistrationException e) {
                log.error("Error occurred while registering the OAuth application : " +
                          registrationProfile.getClientName(), e);
            }
        }
        return null;
    }

    public OAuthAppDetails getOAuthApplicationData(String clientName) {
        try {
            return DynamicClientWebAppRegistrationUtil.getOAuthApplicationData(clientName);
        } catch (DynamicClientRegistrationException e) {
            log.error("Error occurred while fetching the OAuth application data for web app : " +
                      clientName, e);
        }
        return null;
    }

    public boolean isRegisteredOAuthApplication(String clientName) {
        OAuthAppDetails oAuthAppDetails = this.getOAuthApplicationData(clientName);
        if (oAuthAppDetails != null && (oAuthAppDetails.getClientKey() != null && oAuthAppDetails.getClientSecret() !=
                                                                                  null)) {
            return true;
        }
        return false;
    }

    public void saveServletContextToCache(StandardContext context) {
        DynamicClientWebAppRegistrationManager.webAppContexts.put(context.getBaseName(),
                                                                  context.getServletContext());
    }

    public void initiateDynamicClientRegistration() {
        String requiredDynamicClientRegistration, webAppName, serviceProviderName;
        ServletContext servletContext;
        RegistrationProfile registrationProfile;
        OAuthAppDetails oAuthAppDetails;
        DynamicClientWebAppRegistrationManager dynamicClientWebAppRegistrationManager =
                DynamicClientWebAppRegistrationManager.getInstance();
        Enumeration enumeration = new IteratorEnumeration(DynamicClientWebAppRegistrationManager.
                webAppContexts.keySet().iterator());
        if (log.isDebugEnabled()) {
            log.debug("Initiating the DynamicClientRegistration service for web-apps");
        }
        while (enumeration.hasMoreElements()) {
            oAuthAppDetails = new OAuthAppDetails();
            webAppName = (String) enumeration.nextElement();
            serviceProviderName = DynamicClientWebAppRegistrationUtil.replaceInvalidChars(DynamicClientWebAppRegistrationUtil.getUserName())
                                  + "_" + webAppName;
            servletContext = DynamicClientWebAppRegistrationManager.webAppContexts.get(webAppName);
            requiredDynamicClientRegistration = servletContext.getInitParameter(
                    DynamicClientWebAppRegistrationConstants.DYNAMIC_CLIENT_REQUIRED_FLAG);
            //Java web-app section
            if ((requiredDynamicClientRegistration != null) && (Boolean.parseBoolean(
                    requiredDynamicClientRegistration))) {
                //Check whether this is an already registered application
                if (!dynamicClientWebAppRegistrationManager.isRegisteredOAuthApplication(serviceProviderName)) {
                    //Construct the RegistrationProfile
                    registrationProfile = DynamicClientWebAppRegistrationUtil.
                                                        constructRegistrationProfile(servletContext, webAppName);
                    //Register the OAuth application
                    oAuthAppDetails =
                            dynamicClientWebAppRegistrationManager.registerOAuthApplication(registrationProfile);

                } else {
                    oAuthAppDetails = dynamicClientWebAppRegistrationManager.getOAuthApplicationData(webAppName);
                }
            } else if (requiredDynamicClientRegistration == null) {
                //Jaggery apps
                JaggeryOAuthConfigurationSettings jaggeryOAuthConfigurationSettings =
                        DynamicClientWebAppRegistrationUtil.getJaggeryAppOAuthSettings(servletContext);
                if (jaggeryOAuthConfigurationSettings.isRequireDynamicClientRegistration()) {
                    if (!dynamicClientWebAppRegistrationManager.isRegisteredOAuthApplication(serviceProviderName)) {
                        registrationProfile = DynamicClientWebAppRegistrationUtil.
                                                      constructRegistrationProfile(jaggeryOAuthConfigurationSettings,
                                                                                   webAppName);
                        oAuthAppDetails = dynamicClientWebAppRegistrationManager.
                                                                          registerOAuthApplication(registrationProfile);
                    } else {
                        oAuthAppDetails = dynamicClientWebAppRegistrationManager.getOAuthApplicationData(webAppName);
                    }
                }
            }
            //Add client credentials to the web-context
            if ((oAuthAppDetails != null && oAuthAppDetails.getClientKey() != null) && !oAuthAppDetails.getClientKey().isEmpty()) {
                DynamicClientWebAppRegistrationUtil.addClientCredentialsToWebContext(oAuthAppDetails,
                                                          servletContext);
                log.info("Added OAuth application credentials to webapp context of webapp : " +
                         webAppName);
            }
        }
    }
}