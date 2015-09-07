/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.webapp.authenticator.framework.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.TomcatValveContainer;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.webapp.authenticator.framework.DataHolder;
import org.wso2.carbon.webapp.authenticator.framework.WebappAuthenticationHandler;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.WebappAuthenticator;
import org.wso2.carbon.webapp.authenticator.framework.WebappAuthenticatorRepository;
import org.wso2.carbon.webapp.authenticator.framework.authorizer.PermissionAuthorizerValve;
import org.wso2.carbon.webapp.authenticator.framework.config.AuthenticatorConfig;
import org.wso2.carbon.webapp.authenticator.framework.config.WebappAuthenticatorConfig;

import java.util.ArrayList;
import java.util.List;


/**
 * @scr.component name="org.wso2.carbon.webapp.authenticator" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 */
public class WebappAuthenticatorFrameworkServiceComponent {

    private static final Log log = LogFactory.getLog(WebappAuthenticatorFrameworkServiceComponent.class);

    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Starting Web Application Authenticator Framework Bundle");
        }
        try {
            WebappAuthenticatorConfig.init();
            WebappAuthenticatorRepository repository = new WebappAuthenticatorRepository();
            for (AuthenticatorConfig config : WebappAuthenticatorConfig.getInstance().getAuthenticators()) {
                WebappAuthenticator authenticator =
                        (WebappAuthenticator) Class.forName(config.getClassName()).newInstance();
                repository.addAuthenticator(authenticator);
            }
            DataHolder.getInstance().setWebappAuthenticatorRepository(repository);

            // Adding permissions to registry
//            PermissionAuthorizerConfig.init();
//            for (Permission permission : PermissionAuthorizerConfig.getInstance().getPermissions()) {
//                PermissionAuthorizerUtil.addPermission(permission);
//            }

            List<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
            valves.add(new WebappAuthenticationHandler());
            valves.add(new PermissionAuthorizerValve());
            TomcatValveContainer.addValves(valves);

            if (log.isDebugEnabled()) {
                log.debug("Web Application Authenticator Framework Bundle has been started successfully");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing the bundle", e);
        }
    }

    @SuppressWarnings("unused")
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("RealmService acquired");
        }
        DataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        DataHolder.getInstance().setRealmService(null);
    }
}
