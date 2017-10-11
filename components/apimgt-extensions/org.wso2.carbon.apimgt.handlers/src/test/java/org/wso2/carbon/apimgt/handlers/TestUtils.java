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

public class TestUtils {

    static final String IOT_CORE_HOST = "iot.core.wso2.com";
    static final String IOT_CORE_HTTPS_PORT = "9443";
    static final String IOT_KEYMANAGER_HOST = "iot.keymanager.wso2.com";
    static final String IOT_KEYMANAGER_PORT = "9443";
    static final String CONTENT_TYPE = "application/json";

    static String getAbsolutePathOfConfig(String configFilePath) {
        ClassLoader classLoader = TestUtils.class.getClassLoader();
        URL invalidConfig = classLoader.getResource(configFilePath);
        Assert.assertTrue(invalidConfig != null);
        File file = new File(invalidConfig.getFile());
        return file.getAbsolutePath();
    }

    static void setSystemProperties() {
        System.setProperty("iot.core.host", IOT_CORE_HOST);
        System.setProperty("iot.core.https.port", IOT_CORE_HTTPS_PORT);
        System.setProperty("iot.keymanager.host", IOT_KEYMANAGER_HOST);
        System.setProperty("iot.keymanager.https.port", IOT_KEYMANAGER_PORT);
    }

    static void resetSystemProperties() {
        System.clearProperty("iot.core.host");
        System.clearProperty("iot.core.https.port");
        System.clearProperty("iot.keymanager.host");
        System.clearProperty("iot.keymanager.https.port");
    }
}
