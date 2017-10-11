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
import org.apache.commons.io.FileUtils;
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
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.impl.CertificateGenerator;
import org.wso2.carbon.certificate.mgt.core.util.CertificateManagementConstants;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;

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
    public void testHandleRequestWithURISyntaxError() throws Exception {
        HashMap<String, String> transportHeaders = new HashMap<>();
        List<X509Certificate> certificates = loadCertificates();
        transportHeaders.put(AuthConstants.MDM_SIGNATURE, new String(certificates.get(0).getSignature()));
        boolean response = this.handler.handleRequest(createSynapseMessageContext("<empty/>", this.synapseConfiguration,
                transportHeaders, "https://test.com/testservice"));
        Assert.assertFalse(response);
    }

    @Test(description = "Handle request with device type URI",
            dependsOnMethods = "testHandleRequestWithURISyntaxError")
    public void testHandleRequestWithDeviceTypeURI() throws Exception {
        HashMap<String, String> transportHeaders = new HashMap<>();
        List<X509Certificate> certificates = loadCertificates();
        transportHeaders.put(AuthConstants.MDM_SIGNATURE, new String(certificates.get(0).getSignature()));
        setMockClient();
        this.mockClient.setResponse(getDCRResponse());
        this.mockClient.setResponse(getAccessTokenReponse());
        this.mockClient.setResponse(getValidationResponse());
        boolean response = this.handler.handleRequest(createSynapseMessageContext("<empty/>", this.synapseConfiguration,
                transportHeaders, "https://test.com/testservice/api/testdevice"));
        Assert.assertTrue(response);
        this.mockClient.reset();
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

    private List<X509Certificate> loadCertificates() throws IOException, KeystoreException {
        File caPemFile = new File(TestUtils.getAbsolutePathOfConfig("ca_cert.pem"));
        File raPemFile = new File(TestUtils.getAbsolutePathOfConfig("ra_cert.pem"));
        byte[] ca = FileUtils.readFileToByteArray(caPemFile);
        byte[] ra = FileUtils.readFileToByteArray(raPemFile);
        List<X509Certificate> rootCertificates = new CertificateGenerator().getRootCertificates(ca, ra);
        Assert.assertNotNull("Root certificates retrieved", rootCertificates);
        Assert.assertEquals(rootCertificates.get(0).getType(), CertificateManagementConstants.X_509);
        Assert.assertEquals(rootCertificates.get(1).getType(), CertificateManagementConstants.X_509);
        return rootCertificates;
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
