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
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.wso2.carbon.webapp.authenticator.framework.WebappAuthenticator;

public class BasicAuthAuthenticator implements WebappAuthenticator {

    private static final String BASIC_AUTH_AUTHENTICATOR = "BasicAuthAuthenticator";

    @Override
    public boolean isAuthenticated(Request request) {
        return false;  
    }

    @Override
    public Status authenticate(Request request, Response response) {
        return Status.CONTINUE;
    }

    @Override
    public String getAuthenticatorName() {
        return BasicAuthAuthenticator.BASIC_AUTH_AUTHENTICATOR;
    }

    private Credentials getCredentials(Request request) {
        Credentials credentials = null;
        MessageBytes authorization = request.getCoyoteRequest().getMimeHeaders().getValue("authorization");
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
