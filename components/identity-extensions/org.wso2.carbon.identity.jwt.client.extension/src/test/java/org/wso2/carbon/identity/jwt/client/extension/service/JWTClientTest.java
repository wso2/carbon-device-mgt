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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.JWTConfig;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JWTClientTest {
    private static final Log log = LogFactory.getLog(JWTClientManagerServiceTest.class);

    private JWTClient jwtClient;

    @BeforeClass
    public void init() {
        Properties prop = new Properties();
        prop.put("default-jwt-client", "true");
        jwtClient = new JWTClient(new JWTConfig(prop));
    }

    @Test(description = "Test get JWT token.")
    public void testGetJwtToken() throws JWTClientException {
        jwtClient.getJwtToken("admin");
    }

    @Test(description = "Test get JWT token by claims.")
    public void testGetJwtTokenByClaims() throws JWTClientException {
        Map<String, String> claims = new HashMap<>();
        claims.put("name", "admin");
        jwtClient.getJwtToken("admin", claims);
    }

    @Test(description = "Test get JWT token by tenant sign.")
    public void testGetJwtTokenByTenantSign() throws JWTClientException {
        Map<String, String> claims = new HashMap<>();
        claims.put("name", "admin");
        jwtClient.getJwtToken("admin", claims, true);
    }
}
