/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.application.extension.internal;

import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.apimgt.integration.client.service.IntegrationClientService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

public class APIApplicationManagerExtensionDataHolder {
    private static APIApplicationManagerExtensionDataHolder thisInstance = new APIApplicationManagerExtensionDataHolder();
    private APIManagementProviderService apiManagementProviderService;
    private RealmService realmService;
    private TenantManager tenantManager;
    private TenantRegistryLoader tenantRegistryLoader;
    private TenantIndexingLoader indexLoader;
    private IntegrationClientService integrationClientService;

    private APIApplicationManagerExtensionDataHolder() {
    }


    public static APIApplicationManagerExtensionDataHolder getInstance() {
        return thisInstance;
    }

    public APIManagementProviderService getAPIManagementProviderService() {
        return apiManagementProviderService;
    }

    public void setAPIManagementProviderService(
            APIManagementProviderService apiManagementProviderService) {
        this.apiManagementProviderService = apiManagementProviderService;
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

    public void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader){
        this.tenantRegistryLoader = tenantRegistryLoader;
    }

    public TenantRegistryLoader getTenantRegistryLoader(){
        return tenantRegistryLoader;
    }

    public void setIndexLoaderService(TenantIndexingLoader indexLoader) {
        this.indexLoader = indexLoader;
    }

    public TenantIndexingLoader getIndexLoaderService(){
        return indexLoader;
    }

    public IntegrationClientService getIntegrationClientService() {
        return integrationClientService;
    }

    public void setIntegrationClientService(
            IntegrationClientService integrationClientService) {
        this.integrationClientService = integrationClientService;
    }
}
