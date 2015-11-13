/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.device.mgt.common.api;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.api.config.server.DeviceCloudConfigManager;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;

public class DeviceValidator {

    private static Log log = LogFactory.getLog(DeviceValidator.class);
    private static LRUMap cache;

    // private static Log log = LogFactory.getLog(DeviceValidator.class);
    static {
        int cacheSize = DeviceCloudConfigManager.getInstance().getDeviceCloudMgtConfig().getDeviceUserValidator()
                .getCacheSize();
        cache = new LRUMap(cacheSize);
    }

    private PrivilegedCarbonContext ctx;

    public boolean isExist(String owner, String tenantDomain, DeviceIdentifier deviceId)
            throws DeviceManagementException {
        return true;
        //TODO check cache impl
        //return cacheCheck(owner,tenantDomain, deviceId);
    }

    private boolean cacheCheck(String owner, String tenantDomain, DeviceIdentifier deviceId)
            throws DeviceManagementException {

        String value = (String) cache.get(deviceId);

        if (value != null && !value.isEmpty()) {
            return value.equals(owner);
        } else {
            boolean status = isExist(owner, deviceId);
            if (status) {
                addToCache(owner, deviceId);
            }
            return status;
        }
    }

    private void addToCache(String owner, DeviceIdentifier deviceId) {
        cache.put(deviceId, owner);
    }

    private boolean isExist(String owner, DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        try {
            DeviceManagementProviderService dmService = getServiceProvider();
            if (dmService.isEnrolled(deviceIdentifier)) {
                Device device = dmService.getDevice(deviceIdentifier);
                if (device.getEnrolmentInfo().getOwner().equals(owner)) {
                    return true;
                }
            }

            return false;
        } finally {
            endTenantFlow();
        }
    }

    private DeviceManagementProviderService getServiceProvider() {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        PrivilegedCarbonContext.startTenantFlow();
        ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ctx.setTenantDomain(tenantDomain, true);
        if (log.isDebugEnabled()) {
            log.debug("Getting thread local carbon context for tenant domain: " + tenantDomain);
        }
        return (DeviceManagementProviderService) ctx.getOSGiService(DeviceManagementProviderService.class, null);
    }

    private void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
        ctx = null;
        if (log.isDebugEnabled()) {
            log.debug("Tenant flow ended");
        }
    }

}
