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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.dto.JWTConfig;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.util.JWTClientUtil;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@PrepareForTest(JWTClientUtil.class)
public class JWTClientTest {
    private static final Log log = LogFactory.getLog(JWTClientTest.class);

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    private JWTClient jwtClient;

    @BeforeClass
    public void init() {
        Properties prop = new Properties();
        prop.put("default-jwt-client", "true");
        prop.put("TokenEndpoint", "http://example.com");
        jwtClient = new JWTClient(new JWTConfig(prop));
    }

    @Test(description = "Test get JWT token.")
    public void testGetJwtToken() throws JWTClientException {
        String mockToken = "123456789";
        PowerMockito.mockStatic(JWTClientUtil.class);
        PowerMockito.when(JWTClientUtil.generateSignedJWTAssertion(Mockito.anyString(), Mockito.any(JWTConfig.class),
                Mockito.anyBoolean())).thenReturn(mockToken);
        String token = jwtClient.getJwtToken("admin");
        Assert.assertEquals(token, mockToken);
    }

    @Test(description = "Test get JWT token by claims.")
    public void testGetJwtTokenByClaims() throws JWTClientException {
        Map<String, String> claims = new HashMap<>();
        claims.put("name", "admin");
        String mockToken = "123456789";
        PowerMockito.mockStatic(JWTClientUtil.class);
        PowerMockito.when(JWTClientUtil.generateSignedJWTAssertion(Mockito.anyString(), Mockito.any(JWTConfig.class),
                Mockito.anyBoolean(), Mockito.any())).thenReturn(mockToken);
        String token = jwtClient.getJwtToken("admin", claims);
        Assert.assertEquals(token, mockToken);
    }

    @Test(description = "Test get JWT token by tenant sign true.")
    public void testGetJwtTokenByTenantSignTrue() throws JWTClientException {
        Map<String, String> claims = new HashMap<>();
        claims.put("name", "admin");
        String mockToken = "123456789";
        PowerMockito.mockStatic(JWTClientUtil.class);
        PowerMockito.when(JWTClientUtil.generateSignedJWTAssertion(Mockito.anyString(), Mockito.any(JWTConfig.class),
                Mockito.anyBoolean(), Mockito.any())).thenReturn(mockToken);
        String token = jwtClient.getJwtToken("admin", claims, true);
        Assert.assertEquals(token, mockToken);
    }

    @Test(description = "Test get JWT token by tenant sign false.")
    public void testGetJwtTokenByTenantSignFalse() throws JWTClientException {
        Map<String, String> claims = new HashMap<>();
        claims.put("name", "admin");
        String mockToken = "123456789";
        PowerMockito.mockStatic(JWTClientUtil.class);
        PowerMockito.when(JWTClientUtil.generateSignedJWTAssertion(Mockito.anyString(), Mockito.any(JWTConfig.class),
                Mockito.anyBoolean(), Mockito.any())).thenReturn(mockToken);
        String token = jwtClient.getJwtToken("admin", claims, false);
        Assert.assertEquals(token, mockToken);
    }

    @Test(description = "Test get token info.")
    public void testGetTokenInfo() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
            IOException, JWTClientException {
        mockJWTClientUtil();
        AccessTokenInfo tokenInfo = jwtClient.getAccessToken("key", "secret", "admin", "default");
        Assert.assertEquals(tokenInfo.getAccessToken(), "b7882d23f1f8257f4bc6cf4a20633ab1");
    }

    @Test(description = "Test get token info assertion null.")
    public void testGetTokenInfoAssertionNull() throws KeyManagementException, NoSuchAlgorithmException,
            KeyStoreException,
            IOException {
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(httpClient.execute(Mockito.any(HttpPost.class))).thenReturn(httpResponse);

        PowerMockito.mockStatic(JWTClientUtil.class);
        try {
            PowerMockito.when(JWTClientUtil.generateSignedJWTAssertion(Mockito.anyString(), Mockito.any(JWTConfig
                            .class),

                    Mockito.anyBoolean())).thenReturn(null);
            PowerMockito.when(JWTClientUtil.getHttpClient(Mockito.anyString())).thenReturn(httpClient);
            PowerMockito.when(JWTClientUtil.getResponseString(Mockito.any(HttpResponse.class))).thenReturn
                    ("\n" +
                            "{\n" +
                            "    \"scope\":\"default\",\n" +
                            "    \"token_type\":\"Bearer\",\n" +
                            "    \"expires_in\":3600,\n" +
                            "    \"refresh_token\":\"7ed6bae2b1d36c041787e8c8e2d6cbf8\",\n" +
                            "    \"access_token\":\"b7882d23f1f8257f4bc6cf4a20633ab1\"\n" +
                            "}");
            jwtClient.getAccessToken("key", "secret", "admin", "default");
            Assert.fail();
        } catch (JWTClientException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test(description = "Test get token info with encoded app credentials.")
    public void testGetTokenInfoWithEncodeCredentials() throws KeyManagementException, NoSuchAlgorithmException,
            KeyStoreException,
            IOException, JWTClientException {
        mockJWTClientUtil();
        AccessTokenInfo tokenInfo = jwtClient.getAccessToken("a2V5OnNlY3JldA==", "admin", "default");
        Assert.assertEquals(tokenInfo.getAccessToken(), "b7882d23f1f8257f4bc6cf4a20633ab1");
    }

    @Test(description = "Test get token info with invalid encoded app credentials.")
    public void testGetTokenInfoWithInvalidEncodeCredentials() throws KeyManagementException,
            NoSuchAlgorithmException, KeyStoreException,
            IOException {
        try {
            mockJWTClientUtil();
            jwtClient.getAccessToken("8s7d6fgh4j3", "admin", "default");
            Assert.fail();
        } catch (JWTClientException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test(description = "Test get token info with name value pair.")
    public void testGetTokenInfoWithNameValue() throws KeyManagementException, NoSuchAlgorithmException,
            KeyStoreException,
            IOException, JWTClientException {
        mockJWTClientUtil();
        Map<String, String> map = new HashMap();
        map.put("admin", "admin");
        AccessTokenInfo tokenInfo = jwtClient.getAccessToken("key", "secret", "admin", "default", map);
        Assert.assertEquals(tokenInfo.getAccessToken(), "b7882d23f1f8257f4bc6cf4a20633ab1");
    }

    @Test(description = "Test get token from refresh token.")
    public void testGetAccessTokenFromRefreshToken() throws KeyManagementException, NoSuchAlgorithmException,
            KeyStoreException,
            IOException, JWTClientException {
        mockJWTClientUtil();
        Map<String, String> map = new HashMap();
        map.put("admin", "admin");
        AccessTokenInfo tokenInfo = jwtClient.getAccessTokenFromRefreshToken("7ed6bae2b1d36c041787e8c8e2d6cbf8",
                "admin", "default", "key", "secret");
        Assert.assertEquals(tokenInfo.getAccessToken(), "b7882d23f1f8257f4bc6cf4a20633ab1");
    }

    private void mockJWTClientUtil() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
            IOException, JWTClientException {
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(httpClient.execute(Mockito.any(HttpPost.class))).thenReturn(httpResponse);

        PowerMockito.mockStatic(JWTClientUtil.class);
        PowerMockito.when(JWTClientUtil.generateSignedJWTAssertion(Mockito.anyString(), Mockito.any(JWTConfig.class),
                Mockito.anyBoolean())).thenReturn("b7882d23f1f8257f4bc6cf4a20633ab1");
        PowerMockito.when(JWTClientUtil.getHttpClient(Mockito.anyString())).thenReturn(httpClient);
        PowerMockito.when(JWTClientUtil.getResponseString(Mockito.any(HttpResponse.class))).thenReturn
                ("\n" +
                        "{\n" +
                        "    \"scope\":\"default\",\n" +
                        "    \"token_type\":\"Bearer\",\n" +
                        "    \"expires_in\":3600,\n" +
                        "    \"refresh_token\":\"7ed6bae2b1d36c041787e8c8e2d6cbf8\",\n" +
                        "    \"access_token\":\"b7882d23f1f8257f4bc6cf4a20633ab1\"\n" +
                        "}");
    }
}
