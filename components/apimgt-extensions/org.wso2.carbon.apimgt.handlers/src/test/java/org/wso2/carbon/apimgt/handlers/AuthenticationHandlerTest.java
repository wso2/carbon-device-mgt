/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.handlers;

import com.google.gson.Gson;
import junit.framework.Assert;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicStatusLine;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.handlers.beans.ValidationResponce;
import org.wso2.carbon.apimgt.handlers.invoker.RESTInvoker;
import org.wso2.carbon.apimgt.handlers.mock.MockClient;
import org.wso2.carbon.apimgt.handlers.mock.MockHttpResponse;
import org.wso2.carbon.apimgt.handlers.utils.AuthConstants;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import javax.security.cert.X509Certificate;

/**
 * This testcase will focus on covering the methods of {@link AuthenticationHandler}
 */
public class AuthenticationHandlerTest extends BaseAPIHandlerTest {

    private AuthenticationHandler handler;
    private SynapseConfiguration synapseConfiguration;
    private MockClient mockClient;

    @BeforeClass
    public void initTest() {
        TestUtils.setSystemProperties();
        this.handler = new AuthenticationHandler();
        this.synapseConfiguration = new SynapseConfiguration();
    }

    @Test(description = "Handle request with empty transport headers")
    public void testHandleRequestWithEmptyTransportHeader() throws Exception {
        boolean response = this.handler.handleRequest(createSynapseMessageContext("<empty/>", this.synapseConfiguration,
                new HashMap<>(), "https://test.com/testservice"));
        Assert.assertFalse(response);
    }

    @Test(description = "Handle request with without device type",
            dependsOnMethods = "testHandleRequestWithEmptyTransportHeader")
    public void testHandleRequestWithoutDeviceType() throws Exception {
        HashMap<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(AuthConstants.MDM_SIGNATURE, "some cert");
        boolean response = this.handler.handleRequest(createSynapseMessageContext("<empty/>", this.synapseConfiguration,
                transportHeaders, "https://test.com/testservice"));
        Assert.assertFalse(response);
    }

    @Test(description = "Handle request with device type URI with MDM ceritificate",
            dependsOnMethods = "testHandleRequestWithoutDeviceType")
    public void testHandleSuccessfulRequestMDMCertificate() throws Exception {
        HashMap<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(AuthConstants.MDM_SIGNATURE, "some cert");
        setMockClient();
        this.mockClient.setResponse(getDCRResponse());
        this.mockClient.setResponse(getAccessTokenReponse());
        this.mockClient.setResponse(getValidationResponse());
        boolean response = this.handler.handleRequest(createSynapseMessageContext("<empty/>", this.synapseConfiguration,
                transportHeaders, "https://test.com/testservice/api/testdevice"));
        Assert.assertTrue(response);
        this.mockClient.reset();
    }

    @Test(description = "Handle request with device type URI with Proxy Mutual Auth Header",
            dependsOnMethods = "testHandleSuccessfulRequestMDMCertificate")
    public void testHandleSuccessRequestProxyMutualAuthHeader() throws Exception {
        HashMap<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(AuthConstants.PROXY_MUTUAL_AUTH_HEADER, "Test Header");
        setMockClient();
        this.mockClient.setResponse(getAccessTokenReponse());
        this.mockClient.setResponse(getValidationResponse());
        boolean response = this.handler.handleRequest(createSynapseMessageContext("<empty/>", this.synapseConfiguration,
                transportHeaders, "https://test.com/testservice/api/testdevice"));
        Assert.assertTrue(response);
        this.mockClient.reset();
    }

    @Test(description = "Handle request with device type URI with Mutual Auth Header",
            dependsOnMethods = "testHandleSuccessRequestProxyMutualAuthHeader")
    public void testHandleSuccessRequestMutualAuthHeader() throws Exception {
        HashMap<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(AuthConstants.MUTUAL_AUTH_HEADER, "Test Header");
        setMockClient();
        this.mockClient.setResponse(getAccessTokenReponse());
        this.mockClient.setResponse(getValidationResponse());
        MessageContext messageContext = createSynapseMessageContext("<empty/>", this.synapseConfiguration,
                transportHeaders, "https://test.com/testservice/api/testdevice");
        org.apache.axis2.context.MessageContext axisMC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        String certStr = getContent(TestUtils.getAbsolutePathOfConfig("ra_cert.pem"));
        X509Certificate cert = X509Certificate.getInstance(new ByteArrayInputStream(certStr.
                getBytes(StandardCharsets.UTF_8.name())));
        axisMC.setProperty(AuthConstants.CLIENT_CERTIFICATE, new X509Certificate[]{cert});
        boolean response = this.handler.handleRequest(messageContext);
        Assert.assertTrue(response);
        this.mockClient.reset();
    }

    @Test(description = "Handle request with device type URI with Encoded Pem",
            dependsOnMethods = "testHandleSuccessRequestMutualAuthHeader")
    public void testHandleSuccessRequestEncodedPem() throws Exception {
        HashMap<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(AuthConstants.ENCODED_PEM, "encoded pem");
        setMockClient();
        this.mockClient.setResponse(getAccessTokenReponse());
        this.mockClient.setResponse(getValidationResponse());
        MessageContext messageContext = createSynapseMessageContext("<empty/>", this.synapseConfiguration,
                transportHeaders, "https://test.com/testservice/api/testdevice");
        boolean response = this.handler.handleRequest(messageContext);
        Assert.assertTrue(response);
        this.mockClient.reset();
    }

    @Test(description = "Handle request with device type URI with Encoded Pem with invalid response",
            dependsOnMethods = "testHandleSuccessRequestEncodedPem")
    public void testHandleSuccessRequestEncodedPemInvalidResponse() throws Exception {
        HashMap<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(AuthConstants.ENCODED_PEM, "encoded pem");
        setMockClient();
        this.mockClient.setResponse(getAccessTokenReponse());
        this.mockClient.setResponse(getInvalidResponse());
        MessageContext messageContext = createSynapseMessageContext("<empty/>", this.synapseConfiguration,
                transportHeaders, "https://test.com/testservice/api/testdevice");
        boolean response = this.handler.handleRequest(messageContext);
        Assert.assertFalse(response);
        this.mockClient.reset();
    }

    @Test(description = "Handle request with cert management exception ",
            dependsOnMethods = "testHandleSuccessRequestEncodedPem")
    public void testHandleRequestWithCertMgmtException() throws Exception {
        HashMap<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(AuthConstants.ENCODED_PEM, "encoded pem");
        setMockClient();
        this.mockClient.setResponse(null);
        MessageContext messageContext = createSynapseMessageContext("<empty/>", this.synapseConfiguration,
                transportHeaders, "https://test.com/testservice/api/testdevice");
        boolean response = this.handler.handleRequest(messageContext);
        Assert.assertFalse(response);
        this.mockClient.reset();
    }

    @Test(description = "Handle request with IO exception",
            dependsOnMethods = "testHandleRequestWithCertMgmtException")
    public void testHandleRequestWithIOException() throws Exception {
        HashMap<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(AuthConstants.ENCODED_PEM, "encoded pem");
        setMockClient();
        this.mockClient.setResponse(getAccessTokenReponse());
        this.mockClient.setResponse(null);
        MessageContext messageContext = createSynapseMessageContext("<empty/>", this.synapseConfiguration,
                transportHeaders, "https://test.com/testservice/api/testdevice");
        boolean response = this.handler.handleRequest(messageContext);
        Assert.assertFalse(response);
        this.mockClient.reset();
    }

    @Test(description = "Handle request with URI exception",
            dependsOnMethods = "testHandleRequestWithIOException")
    public void testHandleRequestWithURIException() throws Exception {
        TestUtils.resetSystemProperties();
        HashMap<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(AuthConstants.MDM_SIGNATURE, "some cert");
        AuthenticationHandler handler = new AuthenticationHandler();
        boolean response = handler.handleRequest(createSynapseMessageContext("<empty/>", this.synapseConfiguration,
                transportHeaders, "https://test.com/testservice/api/testdevice"));
        Assert.assertFalse(response);
        TestUtils.setSystemProperties();
    }

    @Test(description = "Handle response")
    public void testHandleResponse() throws Exception {
        boolean response = this.handler.handleResponse(null);
        Assert.assertTrue(response);
    }


    private static MessageContext createSynapseMessageContext(
            String payload, SynapseConfiguration config, HashMap<String, String> transportHeaders,
            String address) throws Exception {
        org.apache.axis2.context.MessageContext mc =
                new org.apache.axis2.context.MessageContext();
        AxisConfiguration axisConfig = config.getAxisConfiguration();
        if (axisConfig == null) {
            axisConfig = new AxisConfiguration();
            config.setAxisConfiguration(axisConfig);
        }
        ConfigurationContext cfgCtx = new ConfigurationContext(axisConfig);
        SynapseEnvironment env = new Axis2SynapseEnvironment(cfgCtx, config);
        MessageContext synMc = new Axis2MessageContext(mc, config, env);
        SOAPEnvelope envelope =
                OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        OMDocument omDoc =
                OMAbstractFactory.getSOAP11Factory().createOMDocument();
        omDoc.addChild(envelope);
        envelope.getBody().addChild(SynapseConfigUtils.stringToOM(payload));
        synMc.setEnvelope(envelope);
        synMc.setTo(new EndpointReference(address));
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) synMc).getAxis2MessageContext();
        axis2MessageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, transportHeaders);
        return synMc;
    }

    private void setMockClient() throws NoSuchFieldException, IllegalAccessException {
        Field restInvokerField = this.handler.getClass().getDeclaredField("restInvoker");
        restInvokerField.setAccessible(true);
        RESTInvoker restInvoker = (RESTInvoker) restInvokerField.get(this.handler);
        Field clientField = restInvoker.getClass().getDeclaredField("client");
        clientField.setAccessible(true);
        this.mockClient = new MockClient();
        clientField.set(restInvoker, this.mockClient);
    }

    private CloseableHttpResponse getDCRResponse() throws IOException {
        CloseableHttpResponse mockDCRResponse = new MockHttpResponse();
        String dcrResponseFile = TestUtils.getAbsolutePathOfConfig("dcr-response.json");
        BasicHttpEntity responseEntity = new BasicHttpEntity();
        responseEntity.setContent(new ByteArrayInputStream(getContent(dcrResponseFile).
                getBytes(StandardCharsets.UTF_8.name())));
        responseEntity.setContentType(TestUtils.CONTENT_TYPE);
        mockDCRResponse.setEntity(responseEntity);
        mockDCRResponse.setStatusLine(new BasicStatusLine(new ProtocolVersion("http", 1, 0), 200, "OK"));
        return mockDCRResponse;
    }

    private CloseableHttpResponse getAccessTokenReponse() throws IOException {
        CloseableHttpResponse mockDCRResponse = new MockHttpResponse();
        String dcrResponseFile = TestUtils.getAbsolutePathOfConfig("accesstoken-response.json");
        BasicHttpEntity responseEntity = new BasicHttpEntity();
        responseEntity.setContent(new ByteArrayInputStream(getContent(dcrResponseFile).
                getBytes(StandardCharsets.UTF_8.name())));
        responseEntity.setContentType(TestUtils.CONTENT_TYPE);
        mockDCRResponse.setEntity(responseEntity);
        mockDCRResponse.setStatusLine(new BasicStatusLine(new ProtocolVersion("http", 1, 0), 200, "OK"));
        return mockDCRResponse;
    }

    private CloseableHttpResponse getValidationResponse() throws UnsupportedEncodingException {
        ValidationResponce response = new ValidationResponce();
        response.setDeviceId("1234");
        response.setDeviceType("testdevice");
        response.setJWTToken("1234567788888888");
        response.setTenantId(-1234);
        Gson gson = new Gson();
        String jsonReponse = gson.toJson(response);
        CloseableHttpResponse mockDCRResponse = new MockHttpResponse();
        BasicHttpEntity responseEntity = new BasicHttpEntity();
        responseEntity.setContent(new ByteArrayInputStream(jsonReponse.getBytes(StandardCharsets.UTF_8.name())));
        responseEntity.setContentType(TestUtils.CONTENT_TYPE);
        mockDCRResponse.setEntity(responseEntity);
        mockDCRResponse.setStatusLine(new BasicStatusLine(new ProtocolVersion("http", 1, 0), 200, "OK"));
        return mockDCRResponse;
    }

    private CloseableHttpResponse getInvalidResponse() throws UnsupportedEncodingException {
        CloseableHttpResponse mockDCRResponse = new MockHttpResponse();
        BasicHttpEntity responseEntity = new BasicHttpEntity();
        responseEntity.setContent(new ByteArrayInputStream("invalid response".getBytes(StandardCharsets.UTF_8.name())));
        responseEntity.setContentType(TestUtils.CONTENT_TYPE);
        mockDCRResponse.setEntity(responseEntity);
        mockDCRResponse.setStatusLine(new BasicStatusLine(new ProtocolVersion("http", 1, 0), 400, "Bad Request"));
        return mockDCRResponse;
    }

    private String getContent(String filePath) throws IOException {
        FileReader fileReader = new FileReader(filePath);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String content = "";
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            content += line + "\n";
        }
        bufferedReader.close();
        return content;
    }
}
