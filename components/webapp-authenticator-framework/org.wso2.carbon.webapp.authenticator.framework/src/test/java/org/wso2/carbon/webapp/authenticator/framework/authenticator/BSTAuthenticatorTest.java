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

import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationInfo;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuth2TokenValidator;
import org.wso2.carbon.webapp.authenticator.framework.internal.AuthenticatorFrameworkDataHolder;
import org.wso2.carbon.webapp.authenticator.framework.util.TestInputBuffer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Properties;

/**
 * This is a test class for {@link BSTAuthenticator}.
 */
public class BSTAuthenticatorTest {
    private BSTAuthenticator bstAuthenticator;
    private Properties properties;
    private Field headersField;
    private OAuth2TokenValidationService oAuth2TokenValidationService;
    private OAuth2ClientApplicationDTO oAuth2ClientApplicationDTO;

    @BeforeClass
    public void init() throws NoSuchFieldException {
        bstAuthenticator = new BSTAuthenticator();
        properties = new Properties();
        headersField = org.apache.coyote.Request.class.getDeclaredField("headers");
        headersField.setAccessible(true);
        oAuth2TokenValidationService = Mockito
                .mock(OAuth2TokenValidationService.class, Mockito.CALLS_REAL_METHODS);
        oAuth2ClientApplicationDTO = Mockito
                .mock(OAuth2ClientApplicationDTO.class, Mockito.CALLS_REAL_METHODS);

        OAuth2TokenValidationResponseDTO authorizedValidationResponse = new OAuth2TokenValidationResponseDTO();
        authorizedValidationResponse.setValid(true);
        authorizedValidationResponse.setAuthorizedUser("admin@" + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

        Mockito.doReturn(oAuth2ClientApplicationDTO).when(oAuth2TokenValidationService)
                .findOAuthConsumerIfTokenIsValid(Mockito.any());
        oAuth2ClientApplicationDTO.setAccessTokenValidationResponse(authorizedValidationResponse);
        AuthenticatorFrameworkDataHolder.getInstance().setOAuth2TokenValidationService(oAuth2TokenValidationService);
    }

    @Test(description = "This test case is used to test the behaviour of BST Authenticator when the properties are "
            + "null", expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Required properties needed to initialize OAuthAuthenticator are "
                    + "not provided")
    public void testInitWithoutProperties() {
        bstAuthenticator.init();
    }

    @Test(description = "This test case is used to test the behaviour of BST Authenticator when the token validation "
            + "urlproperty is not set ", expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "OAuth token validation endpoint url is not provided",
            dependsOnMethods = {"testInitWithoutProperties"})
    public void testInitWithoutTokenValidationUrl() {
        bstAuthenticator.setProperties(properties);
        bstAuthenticator.init();
    }

    @Test(description = "This test case is used to test the behaviour of BST Authenticator when the user "
            + "name is not set", expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Username to connect to the OAuth token validation "
                    + "endpoint is not provided", dependsOnMethods = {"testInitWithoutTokenValidationUrl"})
    public void testInitWithoutUserName() {
        properties.setProperty("TokenValidationEndpointUrl", "test");
        bstAuthenticator.setProperties(properties);
        bstAuthenticator.init();
    }

    @Test(description = "This test case is used to test the behaviour of BST Authenticator when the password "
            + "name is not set", expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = "Password to connect to the OAuth token validation endpoint is "
                    + "not provided", dependsOnMethods = {"testInitWithoutUserName"})
    public void testInitWithoutPassword() {
        properties.setProperty("Username", "admin");
        bstAuthenticator.setProperties(properties);
        bstAuthenticator.init();
    }

    @Test(description = "This test case is used to test the behaviour of BST Authenticator when all the required "
            + "properties are set correctly", dependsOnMethods = {"testInitWithoutPassword"})
    public void testInitWithRemote() throws NoSuchFieldException, IllegalAccessException {
        properties.setProperty("Password", "admin");
        bstAuthenticator.setProperties(properties);
        bstAuthenticator.init();
        Field tokenValidator = BSTAuthenticator.class.getDeclaredField("tokenValidator");
        tokenValidator.setAccessible(true);
        OAuth2TokenValidator oAuth2TokenValidator = (OAuth2TokenValidator) tokenValidator.get(bstAuthenticator);
        Assert.assertNotNull(oAuth2TokenValidator, "Token validation creation failed even with the required "
                + "parameters.");
    }

    @Test(description = "This method tests the get methods of the BST Authenticator",
            dependsOnMethods = {"testInitWithRemote"})
    public void testGetterMethods() {
        Assert.assertNotNull(bstAuthenticator.getProperties(), "Retrieval of properties from BSTAuthenticator failed");
        Assert.assertNotNull(bstAuthenticator.getProperty("Password"),
                "Retrieval of added property failed in " + "BSTAuthenticator");
        Assert.assertNull(bstAuthenticator.getProperty("test"),
                "Retrieval of property test is successful, which is " + "never added");
        Assert.assertEquals(bstAuthenticator.getName(), "BSTAuthenticator",
                "Name returned by BSTAuthenticator does" + " not match.");
    }

    @Test(description = "This test case tests the canHandle method of the BSTAuthenticator under faulty conditions")
    public void testCanHandleWithFalseConditions() throws IllegalAccessException {
        Request request = new Request();
        org.apache.coyote.Request coyoteRequest = new org.apache.coyote.Request();
        request.setCoyoteRequest(coyoteRequest);
        Assert.assertFalse(bstAuthenticator.canHandle(request),
                "BST Authenticator can handle a request without content type");

        MimeHeaders mimeHeaders = new MimeHeaders();
        MessageBytes bytes = mimeHeaders.addValue("content-type");
        bytes.setString("test");
        headersField.set(coyoteRequest, mimeHeaders);
        request.setCoyoteRequest(coyoteRequest);
        Assert.assertFalse(bstAuthenticator.canHandle(request),
                "BST Authenticator can handle a request with content type test");
    }


    @Test(description = "This test case tests the canHandle method of the BSTAuthenticator under valid conditions")
    public void testCanHandleWithValidRequest() throws IOException, IllegalAccessException {
        Request request = createSoapRequest("CorrectBST.xml");
        Assert.assertTrue(bstAuthenticator.canHandle(request), "BST Authenticator cannot handle a valid "
                + "authentication request");
    }

    @Test(description = "This test case tests the canHandle method of the BSTAuthenticator under missing soap headers")
    public void testCanHandleWithMissingHeaders() throws IOException, IllegalAccessException {
        Request request = createSoapRequest("WrongBST1.xml");
        Assert.assertFalse(bstAuthenticator.canHandle(request),
                "BST Authenticator can handle a request with missing headers ");
        request = createSoapRequest("WrongBST2.xml");
        Assert.assertFalse(bstAuthenticator.canHandle(request),
                "BST Authenticator can handle a request with missing headers ");
    }

    @Test(description = "This method tests the authenticate method of BST Authenticator when only minimal information"
            + " is provided")
    public void testAuthenticateWithMinimalConditions() throws NoSuchFieldException, IllegalAccessException {
        Request request = new Request();
        org.apache.coyote.Request coyoteRequest = new org.apache.coyote.Request();
        request.setCoyoteRequest(coyoteRequest);
        AuthenticationInfo authenticationInfo = bstAuthenticator.authenticate(request, null);
        Assert.assertEquals(authenticationInfo.getStatus(), WebappAuthenticator.Status.CONTINUE,
                "Authentication status of authentication info is wrong");
    }

    @Test(description = "This method tests the authenticate method of BST Authenticator when all the relevant "
            + "details", dependsOnMethods = "testInitWithRemote")
    public void testAuthenticate() throws NoSuchFieldException, IllegalAccessException, IOException {
        Request request = createSoapRequest("CorrectBST.xml");
        org.apache.coyote.Request coyoteRequest = request.getCoyoteRequest();
        Field uriMB = org.apache.coyote.Request.class.getDeclaredField("uriMB");
        uriMB.setAccessible(true);
        MessageBytes bytes = MessageBytes.newInstance();
        bytes.setString("test");
        uriMB.set(coyoteRequest, bytes);
        request.setCoyoteRequest(coyoteRequest);
        bstAuthenticator.canHandle(request);
        AuthenticationInfo authenticationInfo = bstAuthenticator.authenticate(request, null);
        Assert.assertEquals(authenticationInfo.getStatus(), WebappAuthenticator.Status.CONTINUE,
                "Authentication status of authentication info is wrong");
        Assert.assertEquals(authenticationInfo.getUsername(), "admin",
                "User name in the authentication info is different than original user");
        OAuth2TokenValidationResponseDTO unAuthorizedValidationRespose = new OAuth2TokenValidationResponseDTO();
        unAuthorizedValidationRespose.setValid(false);
        unAuthorizedValidationRespose.setErrorMsg("User is not authorized");
        Mockito.doReturn(oAuth2ClientApplicationDTO).when(oAuth2TokenValidationService)
                .findOAuthConsumerIfTokenIsValid(Mockito.any());
        oAuth2ClientApplicationDTO.setAccessTokenValidationResponse(unAuthorizedValidationRespose);
        AuthenticatorFrameworkDataHolder.getInstance().setOAuth2TokenValidationService(oAuth2TokenValidationService);
        authenticationInfo = bstAuthenticator.authenticate(request, null);
        Assert.assertEquals(authenticationInfo.getStatus(), WebappAuthenticator.Status.FAILURE,
                "Un-authorized user got authenticated with BST");
    }

    /**
     * To create a soap request by reading the request from given file.
     *
     * @param fileName Name of the file that has the soap request content.
     * @return Request created with soap content.
     * @throws IllegalAccessException Illegal Access Exception.
     * @throws IOException            IO Exception.
     */
    private Request createSoapRequest(String fileName) throws IllegalAccessException, IOException {
        Request request = new Request();
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader
                .getResource("requests" + File.separator + "BST" + File.separator + fileName);
        String bstRequestContent = null;
        if (resourceUrl != null) {
            File bst = new File(resourceUrl.getFile());
            bstRequestContent = FileUtils.readFileToString(bst);
        }
        org.apache.coyote.Request coyoteRequest = new org.apache.coyote.Request();
        MimeHeaders mimeHeaders = new MimeHeaders();
        MessageBytes bytes = mimeHeaders.addValue("content-type");
        bytes.setString("application/xml");
        bytes = mimeHeaders.addValue("custom");
        bytes.setString(bstRequestContent);
        headersField.set(coyoteRequest, mimeHeaders);
        TestInputBuffer inputBuffer = new TestInputBuffer();
        coyoteRequest.setInputBuffer(inputBuffer);
        Context context = new StandardContext();
        request.setContext(context);
        request.setCoyoteRequest(coyoteRequest);
        return request;
    }

}
