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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.coyote.InputBuffer;
import org.apache.tomcat.util.buf.ByteChunk;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationException;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationFrameworkUtil;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationInfo;
import org.wso2.carbon.webapp.authenticator.framework.Utils.Utils;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuth2TokenValidator;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuthTokenValidationException;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuthValidationResponse;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuthValidatorFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class BSTAuthenticator implements WebappAuthenticator {

    private Properties properties;
    private OAuth2TokenValidator tokenValidator;
    private static final List<String> APPLICABLE_CONTENT_TYPES = new ArrayList<>();
    private static final Log log = LogFactory.getLog(BSTAuthenticator.class);

    static {
        APPLICABLE_CONTENT_TYPES.add("application/xml");
        APPLICABLE_CONTENT_TYPES.add("application/soap+xml");
    }

    public void init() {
        if (this.properties == null) {
            throw new IllegalArgumentException("Required properties needed to initialize OAuthAuthenticator " +
                    "are not provided");
        }

        String url = Utils.replaceSystemProperty(this.properties.getProperty("TokenValidationEndpointUrl"));
        if ((url == null) || (url.isEmpty())) {
            throw new IllegalArgumentException("OAuth token validation endpoint url is not provided");
        }
        String adminUsername = this.properties.getProperty("Username");
        if (adminUsername == null) {
            throw new IllegalArgumentException("Username to connect to the OAuth token validation endpoint " +
                    "is not provided");
        }

        String adminPassword = this.properties.getProperty("Password");
        if (adminPassword == null) {
            throw new IllegalArgumentException("Password to connect to the OAuth token validation endpoint " +
                    "is not provided");
        }

        boolean isRemote = Boolean.parseBoolean(this.properties.getProperty("IsRemote"));

        Properties validatorProperties = new Properties();
        validatorProperties.setProperty("MaxTotalConnections", this.properties.getProperty("MaxTotalConnections"));
        validatorProperties.setProperty("MaxConnectionsPerHost", this.properties.getProperty("MaxConnectionsPerHost"));
        this.tokenValidator =
                OAuthValidatorFactory.getValidator(url, adminUsername, adminPassword, isRemote, validatorProperties);
    }

    @Override
    public boolean canHandle(Request request) {
        String contentType = request.getContentType();
        if (contentType != null && (contentType.contains("application/xml") || contentType.contains
                ("application/soap+xml") ||
                contentType.contains("application/text"))) {
            try {
                return isBSTHeaderExists(request);
            } catch (IOException | XMLStreamException e) {
                log.error("Error occurred while checking if BST authenticator can handle the incoming SOAP message");
            }
        }
        return false;
    }

    @Override
    public AuthenticationInfo authenticate(Request request, Response response) {
        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();
        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        if ((requestUri == null) || ("".equals(requestUri))) {
            authenticationInfo.setStatus(WebappAuthenticator.Status.CONTINUE);
            return authenticationInfo;
        }

        StringTokenizer tokenizer = new StringTokenizer(requestUri, "/");
        String context = tokenizer.nextToken();
        if ((context == null) || ("".equals(context))) {
            authenticationInfo.setStatus(WebappAuthenticator.Status.CONTINUE);
        }
        String apiVersion = tokenizer.nextToken();

        String authLevel = "any";
        try {
            if ("noMatchedAuthScheme".equals(authLevel)) {
                AuthenticationFrameworkUtil.handleNoMatchAuthScheme(
                        request, response, requestMethod, apiVersion, context);

                authenticationInfo.setStatus(WebappAuthenticator.Status.CONTINUE);
            } else {
                String bearerToken = new String(
                        Base64.decodeBase64(request.getAttribute("BST").toString().getBytes()));

                String resource = requestUri + ":" + requestMethod;

                OAuthValidationResponse oAuthValidationResponse =
                        this.tokenValidator.validateToken(bearerToken, resource);

                if (oAuthValidationResponse.isValid()) {
                    String username = oAuthValidationResponse.getUserName();
                    String tenantDomain = oAuthValidationResponse.getTenantDomain();

                    authenticationInfo.setUsername(username);
                    authenticationInfo.setTenantDomain(tenantDomain);
                    authenticationInfo.setTenantId(Utils.getTenantIdOFUser(username + "@" + tenantDomain));
                    if (oAuthValidationResponse.isValid())
                        authenticationInfo.setStatus(WebappAuthenticator.Status.CONTINUE);
                } else {
                    authenticationInfo.setMessage(oAuthValidationResponse.getErrorMsg());
                }
            }
        } catch (AuthenticationException e) {
            log.error("Failed to authenticate the incoming request", e);
        } catch (OAuthTokenValidationException e) {
            log.error("Failed to authenticate the incoming request due to oauth token validation error.", e);
        }
        return authenticationInfo;
    }

    @Override
    public String getName() {
        return "BSTAuthenticator";
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    private static byte[] getUTF8Bytes(String soapEnvelope) {
        byte[] bytes;
        try {
            bytes = soapEnvelope.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Unable to extract bytes in UTF-8 encoding. "
                    + "Extracting bytes in the system default encoding"
                    + e.getMessage());
            bytes = soapEnvelope.getBytes();
        }
        return bytes;
    }

    private boolean isBSTHeaderExists(Request request) throws IOException, XMLStreamException {
        String bstHeader = this.getBSTHeader(request);
        if (bstHeader == null || bstHeader.isEmpty()) {
            return false;
        }
        request.setAttribute("BST", bstHeader);
        return true;
    }

    private String getBSTHeader(Request request) throws IOException, XMLStreamException {
        org.apache.coyote.Request coyoteReq = request.getCoyoteRequest();
        InputBuffer buf = coyoteReq.getInputBuffer();
        ByteChunk bc = new ByteChunk();

        buf.doRead(bc, coyoteReq);
        try (InputStream is = new ByteArrayInputStream(getUTF8Bytes(bc.toString()))) {
            XMLStreamReader reader = StAXUtils.createXMLStreamReader(is);
            StAXBuilder builder = new StAXSOAPModelBuilder(reader);
            SOAPEnvelope envelope = (SOAPEnvelope) builder.getDocumentElement();
            envelope.build();

            SOAPHeader header = envelope.getHeader();
            Iterator headerEls = header.getChildrenWithLocalName("Security");
            if (!headerEls.hasNext()) {
                return null;
            }
            OMElement securityHeader = (OMElement) headerEls.next();
            Iterator securityHeaderEls = securityHeader.getChildrenWithLocalName("BinarySecurityToken");
            if (!securityHeaderEls.hasNext()) {
                return null;
            }
            OMElement bstHeader = (OMElement) securityHeaderEls.next();
            bstHeader.build();
            return bstHeader.getText();
        }
    }

}
