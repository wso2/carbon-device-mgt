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
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.device.mgt.core.scep.SCEPManager;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.TomcatValveContainer;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticatorFrameworkDataHolder;
import org.wso2.carbon.webapp.authenticator.framework.WebappAuthenticationValve;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.WebappAuthenticator;
import org.wso2.carbon.webapp.authenticator.framework.WebappAuthenticatorRepository;
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
 * @scr.reference name="org.wso2.carbon.certificate.mgt"
 * interface="org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService"
 * policy="dynamic"
 * cardinality="1..n"
 * bind="setCertificateManagementService"
 * unbind="unsetCertificateManagementService"
 * @scr.reference name="org.wso2.carbon.device.mgt.core.scep"
 * interface="org.wso2.carbon.device.mgt.core.scep.SCEPManager"
 * policy="dynamic"
 * cardinality="1..n"
 * bind="setSCEPManagementService"
 * unbind="unsetSCEPManagementService"
 * @scr.reference name="identity.oauth2.validation.service"
 * interface="org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setOAuth2ValidationService"
 * unbind="unsetOAuth2ValidationService"
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
                WebappAuthenticator authenticator = (WebappAuthenticator) Class.forName(config.getClassName()).
                        newInstance();
                repository.addAuthenticator(authenticator);
            }
            AuthenticatorFrameworkDataHolder.getInstance().setWebappAuthenticatorRepository(repository);

            List<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
            valves.add(new WebappAuthenticationValve());
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
        AuthenticatorFrameworkDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        AuthenticatorFrameworkDataHolder.getInstance().setRealmService(null);
    }

    protected void setCertificateManagementService(CertificateManagementService certificateManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting certificate management service");
        }
        AuthenticatorFrameworkDataHolder.getInstance().setCertificateManagementService(certificateManagementService);
    }

    protected void unsetCertificateManagementService(CertificateManagementService certificateManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing certificate management service");
        }

        AuthenticatorFrameworkDataHolder.getInstance().setCertificateManagementService(null);
    }

    protected void setSCEPManagementService(SCEPManager scepManager) {
        if (log.isDebugEnabled()) {
            log.debug("Setting SCEP management service");
        }
        AuthenticatorFrameworkDataHolder.getInstance().setScepManager(scepManager);
    }

    protected void unsetSCEPManagementService(SCEPManager scepManager) {
        if (log.isDebugEnabled()) {
            log.debug("Removing SCEP management service");
        }

        AuthenticatorFrameworkDataHolder.getInstance().setScepManager(null);
    }

    /**
     * Sets OAuth2TokenValidation Service.
     *
     * @param tokenValidationService An instance of OAuth2TokenValidationService
     */
    protected void setOAuth2ValidationService(OAuth2TokenValidationService tokenValidationService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting OAuth2TokenValidationService Service");
        }
        AuthenticatorFrameworkDataHolder.getInstance().setoAuth2TokenValidationService(tokenValidationService);
    }

    /**
     * Unsets OAuth2TokenValidation Service.
     *
     * @param tokenValidationService An instance of OAuth2TokenValidationService
     */
    protected void unsetOAuth2ValidationService(OAuth2TokenValidationService tokenValidationService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting OAuth2TokenValidationService Service");
        }
        AuthenticatorFrameworkDataHolder.getInstance().setoAuth2TokenValidationService(null);
    }
}
