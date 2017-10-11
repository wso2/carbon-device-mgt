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

import java.io.File;
import java.net.URL;

/**
 * Utils class which provides utility methods for other testcases.
 */
public class TestUtils {
    static final String IOT_CORE_HOST = "iot.core.wso2.com";
    static final String IOT_CORE_HTTPS_PORT = "9443";
    static final String IOT_KEYMANAGER_HOST = "iot.keymanager.wso2.com";
    static final String IOT_KEYMANAGER_PORT = "9443";
    static final String CONTENT_TYPE = "application/json";

    private static final String IOT_HOST_PROPERTY = "iot.core.host";
    private static final String IOT_PORT_PROPERTY = "iot.core.https.port";
    private static final String IOT_KEY_MANAGER_HOST_PROPERTY = "iot.keymanager.host";
    private static final String IOT_KEY_MANAGER_PORT_PROPERTY = "iot.keymanager.https.port";

    static String getAbsolutePathOfConfig(String configFilePath) {
        ClassLoader classLoader = TestUtils.class.getClassLoader();
        URL invalidConfig = classLoader.getResource(configFilePath);
        Assert.assertTrue(invalidConfig != null);
        File file = new File(invalidConfig.getFile());
        return file.getAbsolutePath();
    }

    static void setSystemProperties() {
        System.setProperty(IOT_HOST_PROPERTY, IOT_CORE_HOST);
        System.setProperty(IOT_PORT_PROPERTY, IOT_CORE_HTTPS_PORT);
        System.setProperty(IOT_KEY_MANAGER_HOST_PROPERTY, IOT_KEYMANAGER_HOST);
        System.setProperty(IOT_KEY_MANAGER_PORT_PROPERTY, IOT_KEYMANAGER_PORT);
    }

    static void resetSystemProperties() {
        System.clearProperty(IOT_HOST_PROPERTY);
        System.clearProperty(IOT_PORT_PROPERTY);
        System.clearProperty(IOT_KEY_MANAGER_HOST_PROPERTY);
        System.clearProperty(IOT_KEY_MANAGER_PORT_PROPERTY);
    }
}
