/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagerStartupListener;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceManagementPluginRepository implements DeviceManagerStartupListener {

    private Map<ProviderKey, DeviceManagementService> providers;
    private boolean isInited;
    private static final Log log = LogFactory.getLog(DeviceManagementPluginRepository.class);

    public DeviceManagementPluginRepository() {
        providers = Collections.synchronizedMap(new HashMap<ProviderKey, DeviceManagementService>());
        DeviceManagementServiceComponent.registerStartupListener(this);
    }

    public void addDeviceManagementProvider(DeviceManagementService provider) throws DeviceManagementException {
        String deviceType = provider.getType();
        String tenantDomain = provider.getProviderTenantDomain();
        boolean isSharedWithAllTenants= provider.isSharedWithAllTenants();
        String[] sharedTenants= provider.getSharedTenantsDomain();
        int tenantId=DeviceManagerUtil.getTenantId(tenantDomain);

        synchronized (providers) {
            try {
                if (isInited) {
                    /* Initializing Device Management Service Provider */
                    provider.init();
                    DeviceManagerUtil.registerDeviceType(deviceType,tenantId,isSharedWithAllTenants,sharedTenants);
                }
            } catch (DeviceManagementException e) {
                throw new DeviceManagementException("Error occurred while adding device management provider '" +
                        deviceType + "'", e);
            }

            if(isSharedWithAllTenants){
                ProviderKey providerKey=new ProviderKey(deviceType,ProviderKey.SHARE_WITH_ALL_TENANTS);
                providers.put(providerKey, provider);

            }else{
                ProviderKey providerKey=new ProviderKey(deviceType,tenantId);
                providers.put(providerKey, provider);

                if(sharedTenants!=null) {
                    for (int i = 0; i < sharedTenants.length; i++) {
                        providerKey = new ProviderKey(deviceType, DeviceManagerUtil.getTenantId(
                                sharedTenants[i]));
                        providers.put(providerKey,provider);

                    }
                }
            }

        }
    }

    public void removeDeviceManagementProvider(DeviceManagementService provider) throws DeviceManagementException {
        String deviceTypeName=provider.getType();
        ProviderKey providerKey=new ProviderKey(deviceTypeName,ProviderKey.SHARE_WITH_ALL_TENANTS);
        if(provider.isSharedWithAllTenants()){
            providers.remove(providerKey);
        }else{
            int providerTenantId=DeviceManagerUtil.getTenantId(provider.getProviderTenantDomain());
            try {
                DeviceTypeDAO deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
                List<Integer> sharedTenants = deviceTypeDAO.getSharedTenantId(deviceTypeName,providerTenantId);
                for(Integer tenantId : sharedTenants){
					providerKey.setTenantId(tenantId);
                    providers.remove(providerKey);
                }

            } catch (DeviceManagementDAOException e) {
                throw new DeviceManagementException("Error occurred while removing tenants provider for device type '" +
                                                            deviceTypeName + "'", e);
            }
        }
    }

    public DeviceManagementService getDeviceManagementService(String type,int tenantId) {
        ProviderKey providerKey=new ProviderKey(type,tenantId);
        DeviceManagementService provider= providers.get(providerKey);
        if(provider == null){
            providerKey.setTenantId(ProviderKey.SHARE_WITH_ALL_TENANTS);
            provider= providers.get(providerKey);

        }
        return provider;
    }

    @Override
    public void notifyObserver() {
        synchronized (providers) {
            for (DeviceManagementService provider : providers.values()) {
                try {
                    provider.init();
                    int tenantId=DeviceManagerUtil.getTenantId(provider.getProviderTenantDomain());
                    boolean isSharedwithAllTenants= provider.isSharedWithAllTenants();
                    String[] sharedTenants= provider.getSharedTenantsDomain();
                    DeviceManagerUtil.registerDeviceType(provider.getType(), tenantId,
                                                         isSharedwithAllTenants, sharedTenants);
                } catch (Throwable e) {
                    /* Throwable is caught intentionally as failure of one plugin - due to invalid start up parameters,
                        etc - should not block the initialization of other device management providers */
                    log.error("Error occurred while initializing device management provider '" +
                            provider.getType() + "'", e);
                }
            }
            this.isInited = true;
        }
    }


	private class ProviderKey {
		public static final int SHARE_WITH_ALL_TENANTS = -1;
		private String deviceType = "";
		private int tenantId;
		boolean shareWithAlltenants;

		ProviderKey(String deviceType, int tenantId) {
			this.deviceType = deviceType;
			this.tenantId = tenantId;
		}

		ProviderKey(String deviceType, int tenantId, boolean shareWithAllTenants) {
			this.deviceType = deviceType;
			this.tenantId = tenantId;
			if (shareWithAllTenants) {
				this.tenantId = SHARE_WITH_ALL_TENANTS;
				this.shareWithAlltenants = true;
			}

		}

		public void setTenantId(int tenantId) {
			this.tenantId = tenantId;
		}


		@Override
		public int hashCode() {
			return (this.deviceType + "@" + this.tenantId).hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof ProviderKey) && deviceType.equals(
					((ProviderKey) obj).deviceType) && tenantId == ((ProviderKey) obj).tenantId;
		}
	}

}
