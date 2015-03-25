/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.webapp.authenticator.framework;

import org.apache.catalina.connector.Request;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.core.authenticate.APITokenValidator;
import org.wso2.carbon.apimgt.core.gateway.APITokenAuthenticator;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

public class OAuthAuthenticator implements WebappAuthenticator {

    private static final String OAUTH_AUTHENTICATOR = "OAuthAuthenticator";
    private static APITokenAuthenticator authenticator = new APITokenAuthenticator();

    private String bearerToken;
    private static final Log log = LogFactory.getLog(OAuthAuthenticator.class);

    public OAuthAuthenticator(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    @Override
    public boolean isAuthenticated(Request request) {
        return false;
    }

    @Override
    public Status authenticate(Request request) {
        String context = request.getContextPath();
        if (context == null || "".equals(context)) {
            return Status.CONTINUE;
        }

        boolean contextExist;
        Boolean contextValueInCache = null;
        if (APIUtil.getAPIContextCache().get(context) != null) {
            contextValueInCache = Boolean.parseBoolean(APIUtil.getAPIContextCache().get(context).toString());
        }

        if (contextValueInCache != null) {
            contextExist = contextValueInCache;
        } else {
            contextExist = ApiMgtDAO.isContextExist(context);
            APIUtil.getAPIContextCache().put(context, contextExist);
        }

        if (!contextExist) {
            return Status.CONTINUE;
        }

        try {
            String apiVersion = HandlerUtil.getAPIVersion(request);
            String domain = request.getHeader(APITokenValidator.getAPIManagerClientDomainHeader());
            String authLevel = authenticator.getResourceAuthenticationScheme(context,
                    apiVersion,
                    request.getRequestURI(),
                    request.getMethod());
            if (HandlerConstants.NO_MATCHING_AUTH_SCHEME.equals(authLevel)) {
                HandlerUtil.handleNoMatchAuthSchemeCallForRestService(null,
                        request.getMethod(), request.getRequestURI(),
                        apiVersion, context);
                return Status.CONTINUE;
            } else {
                boolean isAuthenticated =
                        HandlerUtil.doAuthenticate(context, apiVersion, bearerToken, authLevel, domain);
                return (isAuthenticated) ? Status.SUCCESS : Status.FAILURE;
            }
        } catch (APIManagementException e) {
            //ignore
        } catch (AuthenticationException e) {
            log.error("Error occurred while key validation", e);
        }
        return Status.CONTINUE;
    }

    @Override
    public String getAuthenticatorName() {
        return OAuthAuthenticator.OAUTH_AUTHENTICATOR;
    }


}
