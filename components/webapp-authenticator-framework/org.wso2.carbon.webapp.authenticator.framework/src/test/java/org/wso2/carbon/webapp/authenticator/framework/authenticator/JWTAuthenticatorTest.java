/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.webapp.authenticator.framework.authenticator;

import org.apache.catalina.connector.Request;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.jwt.client.extension.dto.JWTConfig;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.util.JWTClientUtil;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JWTAuthenticatorTest {
    private JWTAuthenticator jwtAuthenticator;
    private Field headersField;
    private final String JWT_HEADER = "X-JWT-Assertion";
    private String jwtToken;
    private String wrongJwtToken;
    private String jwtTokenWithWrongUser;
    private static final String SIGNED_JWT_AUTH_USERNAME = "http://wso2.org/claims/enduser";
    private static final String SIGNED_JWT_AUTH_TENANT_ID = "http://wso2.org/claims/enduserTenantId";
    private Properties properties;
    private final String ISSUER = "wso2.org/products/iot";
    private final String ALIAS = "wso2carbon";

    @BeforeClass
    public void setup() throws NoSuchFieldException, IOException, JWTClientException {
        jwtAuthenticator = new JWTAuthenticator();
        headersField = org.apache.coyote.Request.class.getDeclaredField("headers");
        headersField.setAccessible(true);
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource("jwt.properties");
        File jwtPropertyFile;
        JWTConfig jwtConfig = null;

        if (resourceUrl != null) {
            jwtPropertyFile = new File(resourceUrl.getFile());
            Properties jwtConfigProperties = new Properties();
            jwtConfigProperties.load(new FileInputStream(jwtPropertyFile));
            jwtConfig = new JWTConfig(jwtConfigProperties);
        }

        Map<String, String> customClaims = new HashMap<>();
        customClaims.put(SIGNED_JWT_AUTH_USERNAME, "admin");
        customClaims.put(SIGNED_JWT_AUTH_TENANT_ID, String.valueOf(MultitenantConstants.SUPER_TENANT_ID));
        jwtToken = JWTClientUtil.generateSignedJWTAssertion("admin", jwtConfig, false, customClaims);
        customClaims = new HashMap<>();
        customClaims.put(SIGNED_JWT_AUTH_USERNAME, "admin");
        customClaims.put(SIGNED_JWT_AUTH_TENANT_ID, "-1");
        wrongJwtToken = JWTClientUtil.generateSignedJWTAssertion("admin", jwtConfig, false, customClaims);
        customClaims = new HashMap<>();
        customClaims.put(SIGNED_JWT_AUTH_USERNAME, "notexisting");
        customClaims.put(SIGNED_JWT_AUTH_TENANT_ID, String.valueOf(MultitenantConstants.SUPER_TENANT_ID));
        jwtTokenWithWrongUser = JWTClientUtil.generateSignedJWTAssertion("notexisting", jwtConfig, false, customClaims);
    }

    @Test(description = "This method tests the get methods in the JWTAuthenticator", dependsOnMethods = "testAuthenticate")
    public void testGetMethods() {
        Assert.assertEquals(jwtAuthenticator.getName(), "JWT", "GetName method returns wrong value");
        Assert.assertNotNull(jwtAuthenticator.getProperties(), "Properties are not properly added to JWT "
                + "Authenticator");
        Assert.assertEquals(jwtAuthenticator.getProperties().size(), properties.size(),
                "Added properties do not match with retrieved properties");
        Assert.assertNull(jwtAuthenticator.getProperty("test"), "Retrieved a propety that was never added");
        Assert.assertNotNull(jwtAuthenticator.getProperty(ISSUER), ALIAS);
    }

    @Test(description = "This method tests the canHandle method under different conditions of request")
    public void testHandle() throws IllegalAccessException, NoSuchFieldException {
        Request request = new Request();
        org.apache.coyote.Request coyoteRequest = new org.apache.coyote.Request();
        request.setCoyoteRequest(coyoteRequest);
        Assert.assertFalse(jwtAuthenticator.canHandle(request));
        MimeHeaders mimeHeaders = new MimeHeaders();
        MessageBytes bytes = mimeHeaders.addValue(JWT_HEADER);
        bytes.setString("test");
        headersField.set(coyoteRequest, mimeHeaders);
        request.setCoyoteRequest(coyoteRequest);
        Assert.assertTrue(jwtAuthenticator.canHandle(request));
    }

    @Test(description = "This method tests authenticate method under the successful condition", dependsOnMethods =
            { "testAuthenticateFailureScenarios" })
    public void testAuthenticate() throws IllegalAccessException, NoSuchFieldException {
        Request request = createJWTRequest(jwtToken, "test");
        AuthenticationInfo authenticationInfo = jwtAuthenticator.authenticate(request, null);
        Assert.assertNotNull(authenticationInfo.getUsername(), "Proper authentication request is not properly "
                + "authenticated by the JWTAuthenticator");
    }

    @Test(description = "This method tests the authenticate method under failure conditions")
    public void testAuthenticateFailureScenarios() throws NoSuchFieldException, IllegalAccessException {
        Request request = createJWTRequest("test", "");
        AuthenticationInfo authenticationInfo = jwtAuthenticator.authenticate(request, null);
        Assert.assertNotNull(authenticationInfo, "Returned authentication info was null");
        Assert.assertNull(authenticationInfo.getUsername(), "Un-authenticated request contain username");

        request = createJWTRequest(jwtToken, "");
        authenticationInfo = jwtAuthenticator.authenticate(request, null);
        Assert.assertNotNull(authenticationInfo, "Returned authentication info was null");
        Assert.assertNull(authenticationInfo.getUsername(), "Un-authenticated request contain username");

        properties = new Properties();
        properties.setProperty(ISSUER, "test");
        jwtAuthenticator.setProperties(properties);
        request = createJWTRequest(jwtToken, "");
        authenticationInfo = jwtAuthenticator.authenticate(request, null);
        Assert.assertNotNull(authenticationInfo, "Returned authentication info was null");
        Assert.assertEquals(authenticationInfo.getStatus(), WebappAuthenticator.Status.FAILURE,
                "Un authenticated request does not contain status as failure");

        properties = new Properties();
        properties.setProperty(ISSUER, ALIAS);
        jwtAuthenticator.setProperties(properties);

        request = createJWTRequest(wrongJwtToken, "");
        authenticationInfo = jwtAuthenticator.authenticate(request, null);
        Assert.assertNotNull(authenticationInfo, "Returned authentication info was null");
        Assert.assertEquals(authenticationInfo.getStatus(), WebappAuthenticator.Status.FAILURE,
                "Un authenticated request does not contain status as failure");

        request = createJWTRequest(jwtTokenWithWrongUser, "");
        authenticationInfo = jwtAuthenticator.authenticate(request, null);
        Assert.assertNotNull(authenticationInfo, "Returned authentication info was null");
        Assert.assertEquals(authenticationInfo.getStatus(), WebappAuthenticator.Status.FAILURE,
                "Un authenticated request does not contain status as failure");
    }


    /**
     * To create a JWT request with the given jwt header.
     *  @param jwtToken JWT token to be added to the header
     * @param requestUri Request URI to be added to the request.
     */
    private Request createJWTRequest(String jwtToken, String requestUri)
            throws IllegalAccessException, NoSuchFieldException {
        Request request = new Request();
        org.apache.coyote.Request coyoteRequest = new org.apache.coyote.Request();
        MimeHeaders mimeHeaders = new MimeHeaders();
        MessageBytes bytes = mimeHeaders.addValue(JWT_HEADER);
        bytes.setString(jwtToken);
        headersField.set(coyoteRequest, mimeHeaders);
        Field uriMB = org.apache.coyote.Request.class.getDeclaredField("uriMB");
        uriMB.setAccessible(true);
        bytes = MessageBytes.newInstance();
        bytes.setString(requestUri);
        uriMB.set(coyoteRequest, bytes);
        request.setCoyoteRequest(coyoteRequest);

        return request;
    }
}
