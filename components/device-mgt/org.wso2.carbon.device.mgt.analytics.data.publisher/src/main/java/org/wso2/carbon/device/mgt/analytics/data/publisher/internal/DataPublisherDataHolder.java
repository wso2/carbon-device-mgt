/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.analytics.data.publisher.internal;

import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;

public class DataPublisherDataHolder {
	private static DataPublisherDataHolder thisInstance = new DataPublisherDataHolder();

	private TenantRegistryLoader tenantRegistryLoader;
	private TenantIndexingLoader indexLoader;
	private RegistryService registryService;
	private DataPublisherDataHolder() {
	}


	public static DataPublisherDataHolder getInstance() {
		return thisInstance;
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

	public RegistryService getRegistryService() {
		return registryService;
	}

	public void setRegistryService(RegistryService registryService) {
		this.registryService = registryService;
	}

}
