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

package org.wso2.carbon.webapp.authenticator.framework;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;
import org.wso2.carbon.webapp.authenticator.framework.util.TestRequest;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.Base64;

import static org.wso2.carbon.security.SecurityConstants.ADMIN_USER;

/**
 * This is a test class for {@link WebappAuthenticationValve}.
 */
public class WebappAuthenticationValveTest {
    private WebappAuthenticationValve webappAuthenticationValve;
    private CompositeValve compositeValve;

    @BeforeClass()
    public void setup() {
        webappAuthenticationValve = new WebappAuthenticationValve();
        compositeValve = Mockito.mock(CompositeValve.class);
        Mockito.doNothing().when(compositeValve).continueInvocation(Mockito.any(), Mockito.any());
    }

    @Test(description = "This method tests the invoke method of the WebAppAuthenticationValve with the context path "
            + "starting with carbon")
    public void testInvokeWithContextSkippedScenario1() {
        Request request = new Request();
        Context context = new StandardContext();
        context.setPath("carbon");
        CompositeValve compositeValve = Mockito.mock(CompositeValve.class);
        Mockito.doNothing().when(compositeValve).continueInvocation(Mockito.any(), Mockito.any());
        request.setContext(context);
        webappAuthenticationValve.invoke(request, null, compositeValve);
        request = new TestRequest("", "test");
        context = new StandardContext();
        compositeValve = Mockito.mock(CompositeValve.class);
        Mockito.doNothing().when(compositeValve).continueInvocation(Mockito.any(), Mockito.any());
        request.setContext(context);
        webappAuthenticationValve.invoke(request, null, compositeValve);
    }

    @Test(description = "This method tests the behaviour of the invoke method of WebAuthenticationValve when "
            + "un-secured endpoints are invoked.")
    public void testInvokeUnSecuredEndpoints() {
        Request request = new TestRequest("", "test");
        Context context = new StandardContext();
        context.setPath("carbon1");
        context.addParameter("doAuthentication", String.valueOf(true));
        context.addParameter("nonSecuredEndPoints", "test, test1");
        CompositeValve compositeValve = Mockito.mock(CompositeValve.class);
        Mockito.doNothing().when(compositeValve).continueInvocation(Mockito.any(), Mockito.any());
        request.setContext(context);
        webappAuthenticationValve.invoke(request, null, compositeValve);
    }

    @Test(description = "This method tests the behaviour of the invoke method of WebAuthenticationValve when "
            + "secured endpoints are invoked.")
    public void testInvokeSecuredEndpoints() throws NoSuchFieldException, IllegalAccessException {
        String encodedString = new String(Base64.getEncoder().encode((ADMIN_USER + ":" + ADMIN_USER).getBytes()));
        Request request = createRequest("basic " + encodedString);
        webappAuthenticationValve.invoke(request, null, compositeValve);
        encodedString = new String(Base64.getEncoder().encode((ADMIN_USER + ":" + ADMIN_USER + "test").getBytes()));
        request = createRequest("basic " + encodedString);
        Response response = new Response();
        org.apache.coyote.Response coyoteResponse = new org.apache.coyote.Response();
        Connector connector = new Connector();
        response.setConnector(connector);
        response.setCoyoteResponse(coyoteResponse);
        webappAuthenticationValve.invoke(request, response, compositeValve);
        Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_UNAUTHORIZED,
                "Response of un-authorized request is not updated");
    }

    @Test(description = "This method tests the behaviour of invoke method when the request does not satisfy any "
            + "authenticator requirements")
    public void testInvokeWithoutProperAuthenticator() throws NoSuchFieldException, IllegalAccessException {
        Request request = createRequest("basic");
        Response response = new Response();
        org.apache.coyote.Response coyoteResponse = new org.apache.coyote.Response();
        Connector connector = new Connector();
        response.setConnector(connector);
        response.setCoyoteResponse(coyoteResponse);
        webappAuthenticationValve.invoke(request, response, compositeValve);
        Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_UNAUTHORIZED,
                "Response of un-authorized request is not updated");
    }

    /**
     * To create a request with the given authorization header
     *
     * @param authorizationHeader Authorization header
     * @return the relevant request.
     * @throws IllegalAccessException Illegal Access Exception.
     * @throws NoSuchFieldException   No Such Field Exception.
     */
    private Request createRequest(String authorizationHeader) throws IllegalAccessException, NoSuchFieldException {
        Request request = new TestRequest("", "");
        Context context = new StandardContext();
        context.addParameter("basicAuth", "true");
        context.addParameter("managed-api-enabled", "true");
        context.setPath("carbon1");
        context.addParameter("doAuthentication", String.valueOf(true));
        request.setContext(context);
        MimeHeaders mimeHeaders = new MimeHeaders();
        MessageBytes bytes = mimeHeaders.addValue(BaseWebAppAuthenticatorFrameworkTest.AUTHORIZATION_HEADER);
        bytes.setString(authorizationHeader);
        Field headersField = org.apache.coyote.Request.class.getDeclaredField("headers");
        headersField.setAccessible(true);
        org.apache.coyote.Request coyoteRequest = new org.apache.coyote.Request();
        headersField.set(coyoteRequest, mimeHeaders);
        request.setCoyoteRequest(coyoteRequest);
        return request;
    }
}
