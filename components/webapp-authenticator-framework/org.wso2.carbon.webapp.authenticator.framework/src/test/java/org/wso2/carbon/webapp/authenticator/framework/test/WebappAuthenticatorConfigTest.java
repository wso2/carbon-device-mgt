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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticatorFrameworkException;
import org.wso2.carbon.webapp.authenticator.framework.config.AuthenticatorConfig;
import org.wso2.carbon.webapp.authenticator.framework.config.WebappAuthenticatorConfig;

import java.util.List;

public class WebappAuthenticatorConfigTest {

    @BeforeClass
    public void init() {
        System.setProperty(ServerConstants.CARBON_CONFIG_DIR_PATH, "src/test/resources/config");
    }

    @Test
    public void testConfigInitialization() {
        try {
            WebappAuthenticatorConfig.init();

            WebappAuthenticatorConfig config = WebappAuthenticatorConfig.getInstance();
            Assert.assertNotNull(config);

            List<AuthenticatorConfig> authConfigs = config.getAuthenticators();
            Assert.assertNotNull(authConfigs);
        } catch (AuthenticatorFrameworkException e) {
            Assert.fail("Error occurred while testing webapp authenticator config initialization", e);
        } catch (Throwable e) {
            Assert.fail("Unexpected error has been encountered while testing webapp authenticator config " +
                    "initialization", e);
        }
    }

    @AfterClass
    public void cleanup() {
        System.setProperty(ServerConstants.CARBON_CONFIG_DIR_PATH, "");
    }

}
