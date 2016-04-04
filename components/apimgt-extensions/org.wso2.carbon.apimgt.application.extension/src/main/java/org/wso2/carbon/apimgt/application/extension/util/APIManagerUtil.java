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
package org.wso2.carbon.apimgt.application.extension.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.apimgt.application.extension.internal.APIApplicationManagerExtensionDataHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;

public final class APIManagerUtil {

    private static final Log log = LogFactory.getLog(APIManagerUtil.class);

    /**
     * returns the tenant Id of the specific tenant Domain
     */
    public static int getTenantId(String tenantDomain) throws APIManagerException {
        try {
            if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                return MultitenantConstants.SUPER_TENANT_ID;
            }
            TenantManager tenantManager = APIApplicationManagerExtensionDataHolder.getInstance().getTenantManager();
            int tenantId = tenantManager.getTenantId(tenantDomain);
            if (tenantId == -1) {
                throw new APIManagerException("invalid tenant Domain :" + tenantDomain);
            }
            return tenantId;
        } catch (UserStoreException e) {
            throw new APIManagerException("invalid tenant Domain :" + tenantDomain);
        }
    }

    public static String getTenantDomain() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }
}
