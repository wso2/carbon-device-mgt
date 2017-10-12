/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationInfo;
import org.wso2.carbon.webapp.authenticator.framework.BaseWebAppAuthenticatorFrameworkTest;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.impl.RemoteOAuthValidator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * This is a test case for {@link OAuthAuthenticator}
 */
public class OauthAuthenticatorTest {
    private OAuthAuthenticator oAuthAuthenticator;
    private final String BEARER_HEADER = "bearer ";
    private Field headersField;
    private Properties properties;

    @BeforeClass
    public void setup()
            throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Field instance = org.wso2.carbon.core.security.AuthenticatorsConfiguration.class.getDeclaredField("instance");
        instance.setAccessible(true);

        AuthenticatorsConfiguration authenticatorsConfiguration = Mockito
                .mock(AuthenticatorsConfiguration.class, Mockito.CALLS_REAL_METHODS);
        Method initialize = AuthenticatorsConfiguration.class.getDeclaredMethod("initialize");
        initialize.setAccessible(true);
        initialize.invoke(authenticatorsConfiguration);
        instance.set(null, authenticatorsConfiguration);
        oAuthAuthenticator = new OAuthAuthenticator();
        headersField = org.apache.coyote.Request.class.getDeclaredField("headers");
        headersField.setAccessible(true);
    }

    @Test(description = "This method tests the successful execution of init method")
    public void testInit() throws NoSuchFieldException, IllegalAccessException {
        properties = new Properties();
        properties.setProperty("TokenValidationEndpointUrl", "test");
        properties.setProperty("Username", "admin");
        properties.setProperty("Password", "admin");
        properties.setProperty("IsRemote", "true");
        properties.setProperty("MaxConnectionsPerHost", "100");
        properties.setProperty("MaxTotalConnections", "1000");
        Assert.assertNull(oAuthAuthenticator.getProperty("test"),
                "OAuth authenticator is returning the properties that were never set");
        oAuthAuthenticator.setProperties(properties);
        oAuthAuthenticator.init();
        Field tokenValidator = OAuthAuthenticator.class.getDeclaredField("tokenValidator");
        tokenValidator.setAccessible(true);
        Assert.assertNotNull(tokenValidator.get(oAuthAuthenticator), "OauthAuthenticator initialization failed");
        Assert.assertEquals(oAuthAuthenticator.getName(), "OAuth", "Name of the OauthAuthenticator does not match");
    }

    @Test(description = "This method tests the canHandle method of OAuthAuthenticator")
    public void testCanHandle() throws IllegalAccessException {
        Request request = createOauthRequest(BEARER_HEADER);
        Assert.assertTrue(oAuthAuthenticator.canHandle(request),
                "The request with the bearer authorization header cannot be handled by OauthAuthenticator");

        request = createOauthRequest("test");
        Assert.assertFalse(oAuthAuthenticator.canHandle(request),
                "The request without bearer authorization header can be handled by OauthAuthenticator");
    }

    @Test(description = "This method tests the authenticate under different parameters",
            dependsOnMethods = {"testInit"})
    public void testAuthenticate() throws Exception {
        Request request = createOauthRequest(BEARER_HEADER);
        Assert.assertEquals(oAuthAuthenticator.authenticate(request, null).getStatus(),
                WebappAuthenticator.Status.CONTINUE, "Authentication status mismatched");
        request = createOauthRequest(BEARER_HEADER + "abc");
        org.apache.coyote.Request coyoteRequest = request.getCoyoteRequest();
        Field uriMB = org.apache.coyote.Request.class.getDeclaredField("uriMB");
        uriMB.setAccessible(true);
        MessageBytes bytes = MessageBytes.newInstance();
        bytes.setString("test");
        uriMB.set(coyoteRequest, bytes);
        request.setCoyoteRequest(coyoteRequest);
        Field tokenValidator = OAuthAuthenticator.class.getDeclaredField("tokenValidator");
        tokenValidator.setAccessible(true);

        GenericObjectPool genericObjectPool = Mockito.mock(GenericObjectPool.class, Mockito.CALLS_REAL_METHODS);
        RemoteOAuthValidator remoteOAuthValidator = Mockito
                .mock(RemoteOAuthValidator.class, Mockito.CALLS_REAL_METHODS);
        tokenValidator.set(oAuthAuthenticator, remoteOAuthValidator);
        Field stubs = RemoteOAuthValidator.class.getDeclaredField("stubs");
        stubs.setAccessible(true);
        stubs.set(remoteOAuthValidator, genericObjectPool);
        OAuth2TokenValidationResponseDTO oAuth2TokenValidationResponseDTO = new OAuth2TokenValidationResponseDTO();
        oAuth2TokenValidationResponseDTO.setValid(true);
        oAuth2TokenValidationResponseDTO.setAuthorizedUser("admin@carbon.super");
        OAuth2ClientApplicationDTO oAuth2ClientApplicationDTO = Mockito
                .mock(OAuth2ClientApplicationDTO.class, Mockito.CALLS_REAL_METHODS);
        Mockito.doReturn(oAuth2TokenValidationResponseDTO).when(oAuth2ClientApplicationDTO)
                .getAccessTokenValidationResponse();
        OAuth2TokenValidationServiceStub oAuth2TokenValidationServiceStub = Mockito
                .mock(OAuth2TokenValidationServiceStub.class, Mockito.CALLS_REAL_METHODS);
        Mockito.doReturn(oAuth2ClientApplicationDTO).when(oAuth2TokenValidationServiceStub)
                .findOAuthConsumerIfTokenIsValid(Mockito.any());
        Mockito.doReturn(oAuth2TokenValidationServiceStub).when(genericObjectPool).borrowObject();
        oAuthAuthenticator.canHandle(request);
        AuthenticationInfo authenticationInfo = oAuthAuthenticator.authenticate(request, null);
        Assert.assertEquals(authenticationInfo.getUsername(), "admin");

    }

    @Test(description = "This method is used to test getProperty method of the OAuthAuthenticator",
            dependsOnMethods = {"testInit"})
    public void testGetProperty() {
        Assert.assertEquals(oAuthAuthenticator.getProperty("Username"), "admin",
                "Username property of " + "OauthAuthenticator is not matching with the assigned one.");
        Assert.assertEquals(oAuthAuthenticator.getProperties().size(), properties.size(),
                "Property list assigned " + "does not match with retrieved list");
    }

    /**
     * This will create an OAuth request.
     *
     * @param authorizationHeader Authorization Header
     */
    private Request createOauthRequest(String authorizationHeader) throws IllegalAccessException {
        Request request = new Request();
        org.apache.coyote.Request coyoteRequest = new org.apache.coyote.Request();
        MimeHeaders mimeHeaders = new MimeHeaders();
        MessageBytes bytes = mimeHeaders.addValue(BaseWebAppAuthenticatorFrameworkTest.AUTHORIZATION_HEADER);
        bytes.setString(authorizationHeader);
        headersField.set(coyoteRequest, mimeHeaders);
        request.setCoyoteRequest(coyoteRequest);
        return request;
    }
}
