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
package org.wso2.carbon.webapp.authenticator.framework.authenticator;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationException;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticatorFrameworkDataHolder;
import org.wso2.carbon.webapp.authenticator.framework.Constants;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationInfo;
import org.wso2.carbon.webapp.authenticator.framework.Utils.Utils;

import java.util.Properties;

public class BasicAuthAuthenticator implements WebappAuthenticator {

    private static final String BASIC_AUTH_AUTHENTICATOR = "BasicAuth";
    private static final Log log = LogFactory.getLog(BasicAuthAuthenticator.class);

    @Override
    public void init() {

    }

    @Override
    public boolean canHandle(Request request) {
        MessageBytes authorization =
                request.getCoyoteRequest().getMimeHeaders().getValue(Constants.HTTPHeaders.HEADER_HTTP_AUTHORIZATION);
        if (authorization != null) {
            authorization.toBytes();
            ByteChunk authBC = authorization.getByteChunk();
            if (authBC.startsWithIgnoreCase("basic ", 0)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AuthenticationInfo authenticate(Request request, Response response) {
        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        Credentials credentials = getCredentials(request);
        try {
            int tenantId = Utils.getTenantIdOFUser(credentials.getUsername());
            UserStoreManager userStore = AuthenticatorFrameworkDataHolder.getInstance().getRealmService().
                    getTenantUserRealm(tenantId).getUserStoreManager();
            boolean authenticated = userStore.authenticate(credentials.getUsername(), credentials.getPassword());
            if (authenticated) {
                authenticationInfo.setStatus(Status.CONTINUE);
                authenticationInfo.setUsername(credentials.getUsername());
                authenticationInfo.setTenantDomain(Utils.getTenantDomain(tenantId));
                authenticationInfo.setTenantId(tenantId);
            } else {
                authenticationInfo.setStatus(Status.FAILURE);
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while authenticating the user." + credentials.getUsername(), e);
        } catch (AuthenticationException e) {
            log.error("Error occurred while obtaining the tenant Id for user." + credentials.getUsername(), e);
        }
        return authenticationInfo;
    }

    @Override
    public String getName() {
        return BasicAuthAuthenticator.BASIC_AUTH_AUTHENTICATOR;
    }

    @Override
    public void setProperties(Properties properties) {

    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public String getProperty(String name) {
        return null;
    }

    private Credentials getCredentials(Request request) {
        Credentials credentials = null;
        MessageBytes authorization =
                request.getCoyoteRequest().getMimeHeaders().getValue(Constants.HTTPHeaders.HEADER_HTTP_AUTHORIZATION);
        if (authorization != null) {
            authorization.toBytes();
            ByteChunk authBC = authorization.getByteChunk();
            if (authBC.startsWithIgnoreCase("basic ", 0)) {
                authBC.setOffset(authBC.getOffset() + 6);

                CharChunk authCC = authorization.getCharChunk();
                Base64.decode(authBC, authCC);

                String username;
                String password = null;

                int colon = authCC.indexOf(':');
                if (colon < 0) {
                    username = authCC.toString();
                } else {
                    char[] buf = authCC.getBuffer();
                    username = new String(buf, 0, colon);
                    password = new String(buf, colon + 1, authCC.getEnd() - colon - 1);
                }
                authBC.setOffset(authBC.getOffset() - 6);
                credentials = new Credentials(username, password);
            }
        }
        return credentials;
    }

    public static class Credentials {
        private String username;
        private String password;

        public Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

}
