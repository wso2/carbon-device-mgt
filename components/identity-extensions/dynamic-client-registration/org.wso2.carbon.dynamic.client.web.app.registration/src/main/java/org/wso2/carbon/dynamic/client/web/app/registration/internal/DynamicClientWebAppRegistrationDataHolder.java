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

package org.wso2.carbon.dynamic.client.web.app.registration.internal;

import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * Dataholder class of DynamicClient Webapp Registration component.
 */
public class DynamicClientWebAppRegistrationDataHolder {

    private RealmService realmService;
    private RegistryService registryService;
    private DynamicClientRegistrationService dynamicClientRegistrationService;
    private ConfigurationContextService configurationContextService;

    private static DynamicClientWebAppRegistrationDataHolder thisInstance =
            new DynamicClientWebAppRegistrationDataHolder();

    private DynamicClientWebAppRegistrationDataHolder() {
    }

    public static DynamicClientWebAppRegistrationDataHolder getInstance() {
        return thisInstance;
    }

    public ConfigurationContextService getConfigurationContextService() {
        if (configurationContextService != null) {
            return configurationContextService;
        } else {
            throw new IllegalStateException(
                    "ConfigurationContext service has not initialized properly");
        }
    }

    public void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public DynamicClientRegistrationService getDynamicClientRegistrationService() {
        if (dynamicClientRegistrationService != null) {
            return dynamicClientRegistrationService;
        } else {
            throw new IllegalStateException(
                    "DynamicClientRegistration service has not initialized properly");
        }
    }

    public void setDynamicClientRegistrationService(
            DynamicClientRegistrationService dynamicClientRegistrationService) {
        this.dynamicClientRegistrationService = dynamicClientRegistrationService;
    }

    public RealmService getRealmService() {
        if (realmService != null) {
            return realmService;
        } else {
            throw new IllegalStateException("RealmService has not initialized properly");
        }
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public RegistryService getRegistryService() {
        if (registryService != null) {
            return registryService;
        } else {
            throw new IllegalStateException("Registry Service has not initialized properly");
        }
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }
}
