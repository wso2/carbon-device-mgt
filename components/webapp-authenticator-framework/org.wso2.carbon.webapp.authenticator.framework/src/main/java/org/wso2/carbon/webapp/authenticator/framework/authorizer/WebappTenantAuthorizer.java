/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.webapp.authenticator.framework.authorizer;

import org.apache.catalina.connector.Request;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationInfo;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.WebappAuthenticator;

/**
 * This class represents the methods that are used to authorize requests based on the tenant subscription.
 */
public class WebappTenantAuthorizer {
	private static final String SHARED_WITH_ALL_TENANTS_PARAM_NAME = "isSharedWithAllTenants";
	private static final String PROVIDER_TENANT_DOMAIN_PARAM_NAME = "providerTenantDomain";

	public static WebappAuthenticator.Status authorize(Request request, AuthenticationInfo authenticationInfo) {
		String tenantDomain = authenticationInfo.getTenantDomain();
		if (tenantDomain != null && isSharedWithAllTenants(request) || isProviderTenant(request, tenantDomain)) {
			return WebappAuthenticator.Status.CONTINUE;
		}
		return WebappAuthenticator.Status.FAILURE;
	}

	private static boolean isSharedWithAllTenants(Request request) {
		String param = request.getContext().findParameter(SHARED_WITH_ALL_TENANTS_PARAM_NAME);
		return (param == null || Boolean.parseBoolean(param));
	}

	private static boolean isProviderTenant(Request request, String requestTenantDomain) {
        Object tenantDoamin = request.getServletContext().getAttribute(PROVIDER_TENANT_DOMAIN_PARAM_NAME);
		String param = null;
        if (tenantDoamin != null) {
            param = (String)request.getServletContext().getAttribute(PROVIDER_TENANT_DOMAIN_PARAM_NAME);
        }
		return (param == null || requestTenantDomain.equals(param));
	}
}
