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


package org.wso2.carbon.webapp.authenticator.framework.Utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationException;

public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    public static int getTenantIdOFUser(String username) throws AuthenticationException {
        int tenantId = 0;
        String domainName = MultitenantUtils.getTenantDomain(username);
        if (domainName != null) {
            try {
                TenantManager tenantManager = IdentityTenantUtil.getRealmService().getTenantManager();
                tenantId = tenantManager.getTenantId(domainName);
            } catch (UserStoreException e) {
                String errorMsg = "Error when getting the tenant id from the tenant domain : " +
                        domainName;
                log.error(errorMsg, e);
                throw new AuthenticationException(errorMsg, e);
            }
        }
        return tenantId;
    }
}
