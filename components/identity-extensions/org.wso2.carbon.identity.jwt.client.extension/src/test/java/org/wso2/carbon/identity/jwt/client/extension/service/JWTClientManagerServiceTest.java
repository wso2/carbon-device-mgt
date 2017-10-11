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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientConfigurationException;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.service.common.BaseJWTClientManagerExtTest;

import java.util.Properties;

public class JWTClientManagerServiceTest extends BaseJWTClientManagerExtTest{

    private static final Log log = LogFactory.getLog(JWTClientManagerServiceTest.class);
    private JWTClientManagerService jwtClientManagerService;

    @BeforeClass
    public void init() {
        jwtClientManagerService = new JWTClientManagerServiceImpl();
    }

    @Test(description = "Test for setting default JWT client to null.")
    public void testSetDefaultJWTClientToNull() throws JWTClientConfigurationException {
        try {
            jwtClientManagerService.setDefaultJWTClient(null);
            Assert.fail();
        } catch (JWTClientConfigurationException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test(description = "Test non existent get JWT client.")
    public void testGetJWTClientNotSet() throws JWTClientConfigurationException, JWTClientException {
        try {
            jwtClientManagerService.getJWTClient();
            Assert.fail();
        } catch (NullPointerException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test(description = "Test for setting default JWT client with property null.")
    public void testSetDefaultJWTClientPropertyToNull() throws JWTClientConfigurationException, JWTClientException {
        Properties prop = new Properties();
        prop.put("default-jwt-client", "");
        jwtClientManagerService.setDefaultJWTClient(prop);
        Assert.assertNotNull(jwtClientManagerService.getJWTClient());
    }

    @Test(description = "Test for setting default JWT client.")
    public void testSetDefaultJWTClient() throws JWTClientConfigurationException, JWTClientException {
        Properties prop = new Properties();
        prop.put("default-jwt-client", "true");
        jwtClientManagerService.setDefaultJWTClient(prop);
        Assert.assertNotNull(jwtClientManagerService.getJWTClient());
    }
}
