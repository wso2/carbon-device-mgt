/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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


package org.wso2.carbon.device.mgt.core.scep;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.SQLException;
import java.util.HashMap;

public class SCEPManagerImpl implements SCEPManager {

    private DeviceDAO deviceDAO;
    private static final Log log = LogFactory.getLog(SCEPManagerImpl.class);

    public SCEPManagerImpl() {
        deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
    }

    @Override
    public TenantedDeviceWrapper getValidatedDevice(DeviceIdentifier deviceIdentifier) throws SCEPException {

        TenantedDeviceWrapper tenantedDeviceWrapper = new TenantedDeviceWrapper();
        try {
            DeviceManagementDAOFactory.openConnection();
            HashMap<Integer, Device> deviceHashMap = deviceDAO.getDevice(deviceIdentifier);
            Object[] keySet = deviceHashMap.keySet().toArray();

            if(keySet == null || keySet.length == 0) {
                throw new SCEPException("Lookup device not found for the device identifier");
            }

            Integer tenantId = (Integer) keySet[0];
            tenantedDeviceWrapper.setDevice(deviceHashMap.get(tenantId));
            tenantedDeviceWrapper.setTenantId(tenantId);


            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            ctx.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            ctx.setTenantId(MultitenantConstants.SUPER_TENANT_ID);

            RealmService realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
            if (realmService == null) {
                String msg = "RealmService is not initialized";
                log.error(msg);
                throw new SCEPException(msg);
            }

            String tenantDomain = realmService.getTenantManager().getDomain(tenantId);
            tenantedDeviceWrapper.setTenantDomain(tenantDomain);
            
        } catch (SQLException e) {
            throw new SCEPException("Error occurred while getting the datasource connection.", e);
        } catch (DeviceManagementDAOException e) {
            throw new SCEPException("Error occurred while reading the device dao.", e);
        } catch (UserStoreException e) {
            throw new SCEPException("Error occurred while getting the tenant domain.", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
            DeviceManagementDAOFactory.closeConnection();
        }
        return tenantedDeviceWrapper;
    }
}
