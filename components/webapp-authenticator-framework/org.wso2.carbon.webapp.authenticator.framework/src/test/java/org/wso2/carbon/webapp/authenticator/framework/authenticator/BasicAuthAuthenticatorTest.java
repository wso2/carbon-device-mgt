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
import org.apache.commons.codec.EncoderException;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationInfo;
import org.wso2.carbon.webapp.authenticator.framework.BaseWebAppAuthenticatorFrameworkTest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;

import static org.wso2.carbon.security.SecurityConstants.ADMIN_USER;

/**
 * This is a test case for {@link BasicAuthAuthenticator}.
 */
public class BasicAuthAuthenticatorTest {
    private BasicAuthAuthenticator basicAuthAuthenticator;
    private Field headersField;
    private Context context;
    private Request request;
    private MimeHeaders mimeHeaders;
    private org.apache.coyote.Request coyoteRequest;
    private MessageBytes bytes;

    @BeforeTest
    public void init() throws NoSuchFieldException {
        basicAuthAuthenticator = new BasicAuthAuthenticator();
        headersField = org.apache.coyote.Request.class.getDeclaredField("headers");
        headersField.setAccessible(true);
    }

    @Test(description = "This method tests the behaviour of canHandle method when different wrong values given for a "
            + "request")
    public void testCanHandleWithoutRequireParameters()
            throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, InstantiationException {
        request = new Request();
        context = new StandardContext();
        request.setContext(context);
        Assert.assertFalse(basicAuthAuthenticator.canHandle(request),
                "Without proper headers and parameters, the request can be handled by BasicAuthAuthenticator.");
        context.addParameter("basicAuth", "true");
        request.setContext(context);
        Assert.assertFalse(basicAuthAuthenticator.canHandle(request),
                "Without proper Authentication headers request can be handled by BasicAuthAuthenticator.");
        coyoteRequest = new org.apache.coyote.Request();
        mimeHeaders = new MimeHeaders();
        bytes = mimeHeaders.addValue("Authorization");
        bytes.setString("test");
        headersField.set(coyoteRequest, mimeHeaders);
        request.setCoyoteRequest(coyoteRequest);
        Assert.assertFalse(basicAuthAuthenticator.canHandle(request),
                "With a different authorization header Basic Authenticator can handle the request");

    }

    @Test(description = "This method tests the canHandle method when all the required parameters are given with the "
            + "request", dependsOnMethods = {"testCanHandleWithoutRequireParameters"})
    public void testCanHandleWithRequireParameters() throws IllegalAccessException {
        request = new Request();
        context = new StandardContext();
        context.addParameter("basicAuth", "true");
        request.setContext(context);
        mimeHeaders = new MimeHeaders();
        bytes = mimeHeaders.addValue("Authorization");
        bytes.setString("basic ");
        headersField.set(coyoteRequest, mimeHeaders);
        request.setCoyoteRequest(coyoteRequest);
        Assert.assertTrue(basicAuthAuthenticator.canHandle(request),
                "Basic Authenticator cannot handle a request with all the required headers and parameters.");
    }

    @Test(description = "This method tests the behaviour of the authenticate method in BasicAuthenticator with valid "
            + "credentials", dependsOnMethods = "testCanHandleWithRequireParameters")
    public void testAuthenticateWithValidCredentials() throws EncoderException, IllegalAccessException {
        String encodedString = new String(Base64.getEncoder().encode((ADMIN_USER + ":" + ADMIN_USER).getBytes()));
        request = new Request();
        context = new StandardContext();
        context.addParameter("basicAuth", "true");
        request.setContext(context);
        mimeHeaders = new MimeHeaders();
        bytes = mimeHeaders.addValue(BaseWebAppAuthenticatorFrameworkTest.AUTHORIZATION_HEADER);
        bytes.setString("basic " + encodedString);
        coyoteRequest = new org.apache.coyote.Request();
        headersField.set(coyoteRequest, mimeHeaders);
        request.setCoyoteRequest(coyoteRequest);
        AuthenticationInfo authenticationInfo = basicAuthAuthenticator.authenticate(request, null);
        Assert.assertEquals(authenticationInfo.getStatus(), WebappAuthenticator.Status.CONTINUE,
                "For a valid user authentication failed.");
        Assert.assertEquals(authenticationInfo.getUsername(), ADMIN_USER,
                "Authenticated username for from BasicAuthenticator is not matching with the original user.");
        Assert.assertEquals(authenticationInfo.getTenantDomain(), MultitenantConstants.SUPER_TENANT_DOMAIN_NAME,
                "Authenticated user's tenant domain from BasicAuthenticator is not matching with the "
                        + "original user's tenant domain");
        Assert.assertEquals(authenticationInfo.getTenantId(), MultitenantConstants.SUPER_TENANT_ID,
                "Authenticated user's tenant ID from BasicAuthenticator is not matching with the "
                        + "original user's tenant ID");
    }

    @Test(description = "This method tests the behaviour of the authenticate method in BasicAuthenticator with "
            + "in-valid credentials", dependsOnMethods = {"testAuthenticateWithValidCredentials"})
    public void testAuthenticateWithWrongCredentials() throws IllegalAccessException {
        String encodedString = new String(Base64.getEncoder().encode((ADMIN_USER + ":test" + ADMIN_USER).getBytes()));
        mimeHeaders = new MimeHeaders();
        bytes = mimeHeaders.addValue(BaseWebAppAuthenticatorFrameworkTest.AUTHORIZATION_HEADER);
        bytes.setString("basic " + encodedString);
        coyoteRequest = new org.apache.coyote.Request();
        headersField.set(coyoteRequest, mimeHeaders);
        request.setCoyoteRequest(coyoteRequest);
        AuthenticationInfo authenticationInfo = basicAuthAuthenticator.authenticate(request, null);
        Assert.assertEquals(authenticationInfo.getStatus(), WebappAuthenticator.Status.FAILURE,
                "For a wrong credentials authentication succeeded.");

        encodedString = new String(Base64.getEncoder().encode((ADMIN_USER).getBytes()));
        mimeHeaders = new MimeHeaders();
        bytes = mimeHeaders.addValue(BaseWebAppAuthenticatorFrameworkTest.AUTHORIZATION_HEADER);
        bytes.setString("basic " + encodedString);
        coyoteRequest = new org.apache.coyote.Request();
        headersField.set(coyoteRequest, mimeHeaders);
        request.setCoyoteRequest(coyoteRequest);
        authenticationInfo = basicAuthAuthenticator.authenticate(request, null);
        Assert.assertEquals(authenticationInfo.getStatus(), WebappAuthenticator.Status.FAILURE,
                "For a request with missing password authentication succeeded.");

    }
}
