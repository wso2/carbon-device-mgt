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

package org.wso2.carbon.dynamic.client.web.app.registration.listner;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationException;
import org.wso2.carbon.dynamic.client.registration.profile.RegistrationProfile;
import org.wso2.carbon.dynamic.client.web.app.registration.DynamicRegistrationManager;
import org.wso2.carbon.dynamic.client.web.app.registration.util.DynamicClientRegistrationConstants;
import org.wso2.carbon.dynamic.client.web.app.registration.util.DynamicClientWebAppRegistrationUtil;

import javax.servlet.ServletContext;

/**
 * This class initiates the dynamic client registration flow for Web applications upon on deployment
 * of the web application.
 */
@SuppressWarnings("unused")
public class DynamicClientWebAppDeploymentLifecycleListener implements LifecycleListener {

    private static final Log log =
            LogFactory.getLog(DynamicClientWebAppDeploymentLifecycleListener.class);

    @Override
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        if (Lifecycle.AFTER_START_EVENT.equals(lifecycleEvent.getType())) {
            StandardContext context = (StandardContext) lifecycleEvent.getLifecycle();
            ServletContext servletContext = context.getServletContext();
            String requiredDynamicClientRegistration = servletContext.getInitParameter(
                    DynamicClientRegistrationConstants.DYNAMIC_CLIENT_REQUIRED_FLAG_PARAM);
            if ((requiredDynamicClientRegistration != null) &&
                (Boolean.parseBoolean(requiredDynamicClientRegistration))) {
                DynamicRegistrationManager dynamicRegistrationManager =
                        DynamicRegistrationManager.getInstance();
                //Get the application name from web-context
                String webAppName = context.getBaseName();
                if (!dynamicRegistrationManager.isRegisteredOAuthApplication(webAppName)) {
                    RegistrationProfile registrationProfile = DynamicClientWebAppRegistrationUtil
                            .constructRegistrationProfile(servletContext, webAppName);
                    if(DynamicClientWebAppRegistrationUtil.validateRegistrationProfile(registrationProfile)){
                        dynamicRegistrationManager.registerOAuthApplication(registrationProfile);
                    }
                }
            } else {
                //TODO: Need to have the necessary logic to handle jaggery webapp scenario
            }
        }
    }
}
