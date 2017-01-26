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
package org.wso2.carbon.apimgt.webapp.publisher.internal;

import org.wso2.carbon.apimgt.integration.client.service.IntegrationClientService;
import org.wso2.carbon.apimgt.webapp.publisher.APIConfig;
import org.wso2.carbon.apimgt.webapp.publisher.APIPublisherService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.Stack;

public class APIPublisherDataHolder {

    private APIPublisherService apiPublisherService;
    private ConfigurationContextService configurationContextService;
    private RealmService realmService;
    private TenantManager tenantManager;
    private RegistryService registryService;
    private boolean isServerStarted;
    private Stack<APIConfig> unpublishedApis = new Stack<>();
    private IntegrationClientService integrationClientService;

    private static APIPublisherDataHolder thisInstance = new APIPublisherDataHolder();

    private APIPublisherDataHolder() {
    }

    public static APIPublisherDataHolder getInstance() {
        return thisInstance;
    }

    public APIPublisherService getApiPublisherService() {
        if (apiPublisherService == null) {
            throw new IllegalStateException("APIPublisher service is not initialized properly");
        }
        return apiPublisherService;
    }

    public void setApiPublisherService(APIPublisherService apiPublisherService) {
        this.apiPublisherService = apiPublisherService;
    }

    public void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        if (configurationContextService == null) {
            throw new IllegalStateException("ConfigurationContext service is not initialized properly");
        }
        return configurationContextService;
    }

    public RealmService getRealmService() {
        if (realmService == null) {
            throw new IllegalStateException("Realm service is not initialized properly");
        }
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
        this.setTenantManager(realmService);
    }

    private void setTenantManager(RealmService realmService) {
        if (realmService == null) {
            throw new IllegalStateException("Realm service is not initialized properly");
        }
        this.tenantManager = realmService.getTenantManager();
    }

    public TenantManager getTenantManager() {
        return tenantManager;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public boolean isServerStarted() {
        return isServerStarted;
    }

    public void setServerStarted(boolean serverStarted) {
        isServerStarted = serverStarted;
    }

    public Stack<APIConfig> getUnpublishedApis() {
        return unpublishedApis;
    }

    public void setUnpublishedApis(Stack<APIConfig> unpublishedApis) {
        this.unpublishedApis = unpublishedApis;
    }

    public IntegrationClientService getIntegrationClientService() {
        return integrationClientService;
    }

    public void setIntegrationClientService(
            IntegrationClientService integrationClientService) {
        this.integrationClientService = integrationClientService;
    }
}
