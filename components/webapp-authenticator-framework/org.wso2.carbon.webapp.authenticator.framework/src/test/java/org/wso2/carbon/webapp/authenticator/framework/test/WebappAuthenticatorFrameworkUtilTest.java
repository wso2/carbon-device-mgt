/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.webapp.authenticator.framework.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.webapp.authenticator.framework.Utils.OAuthTokenValidationStubFactory;

import java.util.Properties;

public class WebappAuthenticatorFrameworkUtilTest {

    private static final Log log = LogFactory.getLog(WebappAuthenticatorFrameworkUtilTest.class);

    private static final String TOKEN_VALIDATION_SERVICE_URL = "https://localhost:9443";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final Properties PROPERTIES = new Properties();

    static {
        PROPERTIES.setProperty("MaxTotalConnections", "100");
        PROPERTIES.setProperty("MaxConnectionsPerHost", "100");
    }

    @Test
    public void testOAuthTokenValidatorStubPool() {
        ObjectPool stubs = null;
        OAuth2TokenValidationServiceStub stub = null;

        try {
            stubs = new GenericObjectPool(
                    new OAuthTokenValidationStubFactory(
                            TOKEN_VALIDATION_SERVICE_URL, ADMIN_USERNAME, ADMIN_PASSWORD, PROPERTIES));

            stub = (OAuth2TokenValidationServiceStub) stubs.borrowObject();
            Assert.assertNotNull(stub);
        } catch (Exception e) {
            String msg = "Error occurred while borrowing an oauth validator service stub instance from the pool";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            if (stubs != null) {
                try {
                    if (stub != null) {
                        stubs.returnObject(stub);
                    }
                } catch (Exception e) {
                    log.warn("Error occurred while returning oauth validator service stub instance to the pool", e);
                }

                /* Checks if the stub instance used above has been properly returned to the pool */
                Assert.assertEquals(stubs.getNumIdle(), 1);
                /* Verifies that there's no hanging connections after the operation performed above */
                Assert.assertEquals(stubs.getNumActive(), 0);

                try {
                    stubs.close();
                } catch (Exception e) {
                    log.warn("Error occurred while closing the object pool", e);
                }
            }
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStubFactoryInitWithInvalidHttpClientProperties() {
        new OAuthTokenValidationStubFactory(TOKEN_VALIDATION_SERVICE_URL, null, ADMIN_PASSWORD, PROPERTIES);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStubFactoryInitWithInvalidUsername() {
        new OAuthTokenValidationStubFactory(TOKEN_VALIDATION_SERVICE_URL, null, ADMIN_PASSWORD, PROPERTIES);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStubFactoryInitWithInvalidPassword() {
        new OAuthTokenValidationStubFactory(TOKEN_VALIDATION_SERVICE_URL, ADMIN_USERNAME, null, PROPERTIES);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStubFactoryInitWithInvalidUrl() {
        new OAuthTokenValidationStubFactory(null, ADMIN_USERNAME, ADMIN_PASSWORD, PROPERTIES);
    }

}
