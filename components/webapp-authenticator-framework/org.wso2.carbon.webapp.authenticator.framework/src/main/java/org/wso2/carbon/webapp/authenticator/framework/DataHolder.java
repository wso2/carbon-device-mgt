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
import org.wso2.carbon.user.core.service.RealmService;

public class DataHolder {

    private WebappAuthenticatorRepository repository;
    private RealmService realmService;
    private CertificateManagementService certificateManagementService;
    private SCEPManager scepManager;
    private static DataHolder thisInstance = new DataHolder();

    private DataHolder() {}

    public static DataHolder getInstance() {
        return thisInstance;
    }

    public void setWebappAuthenticatorRepository (WebappAuthenticatorRepository repository) {
        this.repository = repository;
    }

    public WebappAuthenticatorRepository getWebappAuthenticatorRepository() {
        return repository;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public CertificateManagementService getCertificateManagementService() {
        return certificateManagementService;
    }

    public void setCertificateManagementService(CertificateManagementService certificateManagementService) {
        this.certificateManagementService = certificateManagementService;
    }

    public SCEPManager getScepManager() {
        return scepManager;
    }

    public void setScepManager(SCEPManager scepManager) {
        this.scepManager = scepManager;
    }
}
