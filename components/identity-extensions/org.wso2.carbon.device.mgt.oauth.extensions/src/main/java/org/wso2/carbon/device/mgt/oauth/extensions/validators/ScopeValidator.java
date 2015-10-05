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

package org.wso2.carbon.device.mgt.oauth.extensions.validators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.permission.mgt.Permission;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagementException;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagerService;
import org.wso2.carbon.device.mgt.oauth.extensions.internal.OAuthExtensionsDataHolder;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.validators.OAuth2ScopeValidator;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.Properties;

/**
 * Custom OAuth2Token Scope validation implementation.
 */
public class ScopeValidator extends OAuth2ScopeValidator {

    private static final String URL_PROPERTY = "URL";
    private static final String HTTP_METHOD_PROPERTY = "HTTP_METHOD";

    public static final class PermissionMethod {
        private PermissionMethod() {
            throw new AssertionError();
        }
        public static final String READ = "read";
        public static final String WRITE = "write";
        public static final String DELETE = "delete";
        public static final String ACTION = "action";
    }

    private static final Log log = LogFactory.getLog(ScopeValidator.class);

    @Override
    public boolean validateScope(AccessTokenDO accessTokenDO, String resource)
            throws IdentityOAuth2Exception {
        boolean status = false;
        //Extract the url & http method
        int idx = resource.lastIndexOf(':');
        String url = resource.substring(0, idx);
        String method = resource.substring(++idx, resource.length());

        Properties properties = new Properties();
        properties.put(ScopeValidator.URL_PROPERTY, url);
        properties.put(ScopeValidator.HTTP_METHOD_PROPERTY, method);
        PermissionManagerService permissionManagerService = OAuthExtensionsDataHolder.getInstance().
                getPermissionManagerService();
        try {
            Permission permission = permissionManagerService.getPermission(properties);
            String username = accessTokenDO.getAuthzUser();
            status = CarbonContext.getThreadLocalCarbonContext().getUserRealm().
                    getAuthorizationManager().isUserAuthorized(username, permission.getPath(),
                                                               ScopeValidator.PermissionMethod.READ);

        } catch (PermissionManagementException e) {
            log.error("Error occurred while validating the resource scope for : " + resource +
                      ", Msg = " + e.getMessage(), e);
        } catch (UserStoreException e) {
            log.error("Error occurred while retrieving user store. " + e.getMessage());
        }
        return status;
    }
}
