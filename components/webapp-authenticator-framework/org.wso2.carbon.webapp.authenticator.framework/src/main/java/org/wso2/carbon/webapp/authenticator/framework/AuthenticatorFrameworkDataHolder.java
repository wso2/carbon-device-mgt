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
package org.wso2.carbon.webapp.authenticator.framework;

import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.device.mgt.core.scep.SCEPManager;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.user.core.service.RealmService;

public class AuthenticatorFrameworkDataHolder {

    private WebappAuthenticatorRepository repository;
    private RealmService realmService;
    private CertificateManagementService certificateManagementService;
    private SCEPManager scepManager;
    private OAuth2TokenValidationService oAuth2TokenValidationService;

    private static AuthenticatorFrameworkDataHolder
            thisInstance = new AuthenticatorFrameworkDataHolder();

    private AuthenticatorFrameworkDataHolder() {}

    public static AuthenticatorFrameworkDataHolder getInstance() {
        return thisInstance;
    }

    public void setWebappAuthenticatorRepository (WebappAuthenticatorRepository repository) {
        this.repository = repository;
    }

    public WebappAuthenticatorRepository getWebappAuthenticatorRepository() {
        return repository;
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

    public CertificateManagementService getCertificateManagementService() {
        if (certificateManagementService == null) {
            throw new IllegalStateException("CertificateManagement service is not initialized properly");
        }
        return certificateManagementService;
    }

    public void setCertificateManagementService(CertificateManagementService certificateManagementService) {
        this.certificateManagementService = certificateManagementService;
    }

    public SCEPManager getScepManager() {
        if (scepManager == null) {
            throw new IllegalStateException("SCEPManager service is not initialized properly");
        }
        return scepManager;
    }

    public void setScepManager(SCEPManager scepManager) {
        this.scepManager = scepManager;
    }

    public OAuth2TokenValidationService getOAuth2TokenValidationService() {
        if (oAuth2TokenValidationService == null) {
            throw new IllegalStateException("OAuth2TokenValidation service is not initialized properly");
        }
        return oAuth2TokenValidationService;
    }

    public void setOAuth2TokenValidationService(
            OAuth2TokenValidationService oAuth2TokenValidationService) {
        this.oAuth2TokenValidationService = oAuth2TokenValidationService;
    }
}
