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

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuth2TokenValidator;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * This is a test class for {@link BSTAuthenticator}.
 */
public class BSTAuthenticatorTest {
    private BSTAuthenticator bstAuthenticator;
    private Properties properties;

    @BeforeTest
    public void init() {
        bstAuthenticator = new BSTAuthenticator();
        properties = new Properties();
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
}
