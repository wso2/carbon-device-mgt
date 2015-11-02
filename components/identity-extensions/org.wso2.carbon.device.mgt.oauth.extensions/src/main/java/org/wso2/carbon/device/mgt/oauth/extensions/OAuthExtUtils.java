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

package org.wso2.carbon.device.mgt.oauth.extensions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.oauth.extensions.internal.OAuthExtensionsDataHolder;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;

/**
 * This class holds util methods used by OAuth extension bundle.
 */
public class OAuthExtUtils {

    private static final Log log = LogFactory.getLog(OAuthExtUtils.class);

    public static int getTenantId(String tenantDomain) {
        int tenantId = 0;
        if (tenantDomain != null) {
            try {
                TenantManager tenantManager = OAuthExtensionsDataHolder.getInstance().getRealmService().getTenantManager();
                tenantId = tenantManager.getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                String errorMsg = "Error when getting the tenant id from the tenant domain : " +
                                  tenantDomain;
                log.error(errorMsg, e);
            }
        }
        return tenantId;
    }
}
