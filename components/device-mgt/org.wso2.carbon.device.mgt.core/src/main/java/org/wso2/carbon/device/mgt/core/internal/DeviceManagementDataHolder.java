/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.device.mgt.core.internal;

import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManager;
import org.wso2.carbon.device.mgt.core.api.mgt.APIPublisherService;
import org.wso2.carbon.device.mgt.core.app.mgt.AppManagerConnector;
import org.wso2.carbon.device.mgt.core.app.mgt.config.AppManagementConfig;
import org.wso2.carbon.device.mgt.core.config.license.LicenseConfig;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

public class DeviceManagementDataHolder {

    private RealmService realmService;
    private TenantManager tenantManager;
    private DeviceManagementService deviceManagerProvider;
    private LicenseManager licenseManager;
    private RegistryService registryService;
    private LicenseConfig licenseConfig;
    private APIPublisherService apiPublisherService;
	private AppManagerConnector appManager;
	private AppManagementConfig appManagerConfig;

    private static DeviceManagementDataHolder thisInstance = new DeviceManagementDataHolder();

    private DeviceManagementDataHolder() {
    }

    public static DeviceManagementDataHolder getInstance() {
        return thisInstance;
    }

    public RealmService getRealmService() {
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

    public DeviceManagementService getDeviceManagementProvider() {
        return deviceManagerProvider;
    }

    public void setDeviceManagementProvider(DeviceManagementService deviceManagerProvider) {
        this.deviceManagerProvider = deviceManagerProvider;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public LicenseManager getLicenseManager() {
        return licenseManager;
    }

    public void setLicenseManager(LicenseManager licenseManager) {
        this.licenseManager = licenseManager;
    }

    public LicenseConfig getLicenseConfig() {
        return licenseConfig;
    }

    public void setLicenseConfig(LicenseConfig licenseConfig) {
        this.licenseConfig = licenseConfig;
    }

    public APIPublisherService getApiPublisherService() {
        return apiPublisherService;
    }

    public void setApiPublisherService(APIPublisherService apiPublisherService) {
        this.apiPublisherService = apiPublisherService;
    }

	public AppManagerConnector getAppManager() {
		return appManager;
	}

	public void setAppManager(AppManagerConnector appManager) {
		this.appManager = appManager;
	}

	public AppManagementConfig getAppManagerConfig() {
		return appManagerConfig;
	}

	public void setAppManagerConfig(AppManagementConfig appManagerConfig) {
		this.appManagerConfig = appManagerConfig;
	}

}
