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
package org.wso2.carbon.webapp.authenticator.framework;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.core.authenticate.APITokenValidator;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;

public class AuthenticationFrameworkUtil {

    private static final Log log = LogFactory.getLog(AuthenticationFrameworkUtil.class);

    public static void handleNoMatchAuthScheme(Request request, Response response, String httpVerb, String version,
                                               String context) {
        String msg = "Resource is not matched for HTTP Verb: '" + httpVerb + "', API context: '" + context +
                "', Version: '" + version + "' and RequestURI: '" + request.getRequestURI() + "'";
        handleResponse(request, response, HttpServletResponse.SC_FORBIDDEN, msg);
    }

    public static boolean doAuthenticate(
            String context, String version, String accessToken, String requiredAuthenticationLevel,
            String clientDomain) throws APIManagementException, AuthenticationException {

        if (APIConstants.AUTH_NO_AUTHENTICATION.equals(requiredAuthenticationLevel)) {
            return true;
        }
        APITokenValidator tokenValidator = new APITokenValidator();
        APIKeyValidationInfoDTO apiKeyValidationDTO = tokenValidator.validateKey(context, version, accessToken,
                requiredAuthenticationLevel, clientDomain);
        if (apiKeyValidationDTO.isAuthorized()) {
            String username = apiKeyValidationDTO.getEndUserName();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
            try {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(IdentityUtil.
                                                                                           getTenantIdOFUser(username));
            } catch (IdentityException e) {
                throw new AuthenticationException("Error occurred while retrieving the tenant ID of user '" +
                        username + "'", e);
            }
            return true;
        } else {
            throw new AuthenticationException(apiKeyValidationDTO.getValidationStatus(),
                    "Access failure for API: " + context + ", version: " +
                            version + " with key: " + accessToken);
        }
    }

    public static void handleResponse(Request request, Response response, int statusCode, String payload) {
        response.setStatus(statusCode);
        String targetResponseContentType =
                request.getHeader(Constants.HTTPHeaders.HEADER_HTTP_ACCEPT);
        if (targetResponseContentType != null && !"".equals(targetResponseContentType) &&
                !Constants.ContentTypes.CONTENT_TYPE_ANY.equals(targetResponseContentType)) {
            response.setContentType(targetResponseContentType);
        } else {
            response.setContentType(Constants.ContentTypes.CONTENT_TYPE_APPLICATION_XML);
        }
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().write(payload);
        } catch (IOException e) {
            log.error("Error occurred while sending faulty response back to the client", e);
        }
    }

    public static Document convertToDocument(File file) throws AuthenticatorFrameworkException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new AuthenticatorFrameworkException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document", e);
        }
    }

}
