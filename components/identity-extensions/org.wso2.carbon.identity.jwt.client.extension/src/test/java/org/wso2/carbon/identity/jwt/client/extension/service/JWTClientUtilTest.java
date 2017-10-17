/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.identity.jwt.client.extension.service;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.jwt.client.extension.dto.JWTConfig;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.util.JWTClientUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@PrepareForTest({PrivilegedCarbonContext.class, JWTClientUtil.class})
public class JWTClientUtilTest {

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeClass
    public void init() throws Exception {

    }

    @Test(description = "Test get response string.")
    public void testGetResponseString() throws IOException {
        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null);
        BasicHttpEntity httpEntity = new BasicHttpEntity();
        httpEntity.setContent(new ByteArrayInputStream("test message".getBytes(StandardCharsets.UTF_8.name())));
        response.setEntity(httpEntity);
        String result = JWTClientUtil.getResponseString(response);
        Assert.assertEquals(result, "test message");
    }

    @Test(description = "Test initialize.")
    public void testInitialize() throws Exception {
        JWTClientManagerServiceImpl jwtManagerService = new JWTClientManagerServiceImpl();
        Properties prop = new Properties();
        prop.put("default-jwt-client", "true");
        prop.put("TokenEndpoint", "http://example.com");
        jwtManagerService.setDefaultJWTClient(prop);
        try {
            JWTClientUtil.initialize(jwtManagerService);
        } catch (RegistryException e) {
            Assert.fail("Test failed", e);
        }
    }

    @Test(description = "Test generate signed JWT assertion.")
    public void testGenerateSignedJWTAssertion() {
        Properties prop = new Properties();
        prop.put("default-jwt-client", "true");
        prop.put("TokenEndpoint", "http://example.com");
        try {
            JWTClientUtil.generateSignedJWTAssertion("admin", new JWTConfig(prop), true);
        } catch (JWTClientException e) {
            Assert.fail("Test failed", e);
        }
    }

    @Test(description = "Test generate signed JWT assertion with claims.")
    public void testGenerateSignedJWTAssertionWithClaims() {
        Properties prop = new Properties();
        prop.put("default-jwt-client", "true");
        prop.put("TokenEndpoint", "http://example.com");
        try {
            Map<String, String> customClaims = new HashMap<>();
            JWTClientUtil.generateSignedJWTAssertion("admin", new JWTConfig(prop), true, customClaims);
        } catch (JWTClientException e) {
            Assert.fail("Test failed", e);
        }
    }
}
