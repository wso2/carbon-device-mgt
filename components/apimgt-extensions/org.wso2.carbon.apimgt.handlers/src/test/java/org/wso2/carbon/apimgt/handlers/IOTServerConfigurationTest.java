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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.handlers.config.IOTServerConfiguration;
import org.wso2.carbon.apimgt.handlers.utils.Utils;

import java.io.File;

/**
 * This class validates the behaviour of {@link IOTServerConfiguration}
 */
public class IOTServerConfigurationTest extends BaseAPIHandlerTest {
    private static final String CONFIG_DIR = "carbon-home" + File.separator + "repository" + File.separator +
            "conf" + File.separator;

    @BeforeClass
    public void initTest(){
        TestUtils.resetSystemProperties();
    }

    @Test(description = "Validating the IoT Server configuration initialization without system properties")
    public void initConfigWithoutSystemProps() {
        IOTServerConfiguration serverConfiguration = Utils.initConfig();
        Assert.assertTrue(serverConfiguration != null);
        Assert.assertEquals(serverConfiguration.getHostname(), "https://${iot.core.host}:${iot.core.https.port}/");
        Assert.assertEquals(serverConfiguration.getVerificationEndpoint(),
                "https://${iot.core.host}:${iot.core.https.port}/api/certificate-mgt/v1.0/admin/certificates/verify/");
        Assert.assertEquals(serverConfiguration.getUsername(), "testuser");
        Assert.assertEquals(serverConfiguration.getPassword(), "testuserpwd");
        Assert.assertEquals(serverConfiguration.getDynamicClientRegistrationEndpoint(),
                "https://${iot.keymanager.host}:${iot.keymanager.https.port}/client-registration/v0.11/register");
        Assert.assertEquals(serverConfiguration.getOauthTokenEndpoint(),
                "https://${iot.keymanager.host}:${iot.keymanager.https.port}/oauth2/token");
        Assert.assertEquals(serverConfiguration.getApis().size(), 1);
        Assert.assertEquals(serverConfiguration.getApis().get(0).getContextPath(), "/services");
    }

    @Test(description = "Initializing IoT server config with invalid configuration",
            dependsOnMethods = "initConfigWithoutSystemProps")
    public void initConfigWithInvalidConfig() {
        IOTServerConfiguration serverConfig = Utils.initConfig(TestUtils.getAbsolutePathOfConfig(CONFIG_DIR
                + "iot-api-config-invalid.xml"));
        Assert.assertEquals(serverConfig, null);
    }

    @Test(description = "Initializing IoT server config with invalid xml",
            dependsOnMethods = "initConfigWithInvalidConfig")
    public void initConfigWithInvalidXMLConfig() {
        IOTServerConfiguration serverConfig = Utils.initConfig(TestUtils.getAbsolutePathOfConfig(CONFIG_DIR +
                "iot-api-config-invalid-xml.xml"));
        Assert.assertEquals(serverConfig, null);
    }

    @Test(description = "Initializing IoT server config with system configs",
            dependsOnMethods = "initConfigWithInvalidXMLConfig")
    public void initConfigWithSystemProps() {
        TestUtils.setSystemProperties();
        IOTServerConfiguration serverConfiguration = Utils.initConfig();
        Assert.assertTrue(serverConfiguration != null);
        Assert.assertEquals(serverConfiguration.getHostname(), "https://" + TestUtils.IOT_CORE_HOST + ":"
                + TestUtils.IOT_CORE_HTTPS_PORT
                + "/");
        Assert.assertEquals(serverConfiguration.getVerificationEndpoint(),
                "https://" + TestUtils.IOT_CORE_HOST + ":" + TestUtils.IOT_CORE_HTTPS_PORT +
                        "/api/certificate-mgt/v1.0/admin/certificates/" +
                        "verify/");
        Assert.assertEquals(serverConfiguration.getUsername(), "testuser");
        Assert.assertEquals(serverConfiguration.getPassword(), "testuserpwd");
        Assert.assertEquals(serverConfiguration.getDynamicClientRegistrationEndpoint(),
                "https://" + TestUtils.IOT_KEYMANAGER_HOST + ":" + TestUtils.IOT_KEYMANAGER_PORT
                        + "/client-registration/v0.11/register");
        Assert.assertEquals(serverConfiguration.getOauthTokenEndpoint(),
                "https://" + TestUtils.IOT_KEYMANAGER_HOST + ":" + TestUtils.IOT_KEYMANAGER_PORT
                        + "/oauth2/token");
        Assert.assertEquals(serverConfiguration.getApis().size(), 1);
        Assert.assertEquals(serverConfiguration.getApis().get(0).getContextPath(), "/services");
    }
}
