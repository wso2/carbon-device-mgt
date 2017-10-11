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
package org.wso2.carbon.device.mgt.analytics.data.publisher;

import junit.framework.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.analytics.data.publisher.config.AnalyticsConfiguration;
import org.wso2.carbon.device.mgt.analytics.data.publisher.config.InvalidConfigurationStateException;
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

/**
 * This test class will validate the Data publisher configuration creation.
 */
public class DataPublisherConfigTest extends BaseAnalyticsDataPublisherTest {

    @Test(description = "Validating the behaviour od getInstance of the config before calling the init",
            expectedExceptions = InvalidConfigurationStateException.class)
    public void testGetInstanceWithoutInit() throws NoSuchFieldException, IllegalAccessException {
        Field configField = AnalyticsConfiguration.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(configField, null);
        AnalyticsConfiguration.getInstance();
    }

    @Test(description = "Validating the behaviour od getInstance of the config before calling the init",
            expectedExceptions = DataPublisherConfigurationException.class,
            dependsOnMethods = "testGetInstanceWithoutInit")
    public void testInitWithInvalidConfig() throws DataPublisherConfigurationException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL invalidConfig = classLoader.getResource("carbon-home/repository/conf/etc/" +
                "device-analytics-config-invalid.xml");
        Assert.assertTrue("No configuration  - device-analytics-config-invalid.xml found in resource dir",
                invalidConfig != null);
        File file = new File(invalidConfig.getFile());
        AnalyticsConfiguration.init(file.getAbsolutePath());
    }


    @Test(description = "Validating the behaviour od getInstance of the config before calling the init",
            expectedExceptions = DataPublisherConfigurationException.class,
            dependsOnMethods = "testInitWithInvalidConfig")
    public void testInitWithInvalidXML() throws DataPublisherConfigurationException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL invalidConfig = classLoader.getResource("carbon-home/repository/conf/etc/" +
                "device-analytics-config-invalid-xml.xml");
        Assert.assertTrue("No configuration  - device-analytics-config-invalid-xml.xml found in resource dir",
                invalidConfig != null);
        File file = new File(invalidConfig.getFile());
        AnalyticsConfiguration.init(file.getAbsolutePath());
    }


    @Test(description = "Validating the init method with all required params",
            dependsOnMethods = "testInitWithInvalidXML")
    public void testInitWithValidConfig() throws DataPublisherConfigurationException {
        AnalyticsConfiguration.init();
        AnalyticsConfiguration analyticsConfiguration = AnalyticsConfiguration.getInstance();
        Assert.assertEquals(analyticsConfiguration.getAdminPassword(), "testuserpwd");
        Assert.assertEquals(analyticsConfiguration.getAdminUsername(), "testuser");
        Assert.assertEquals(analyticsConfiguration.getReceiverServerUrl(), "tcp://localhost:7615");
        Assert.assertTrue(analyticsConfiguration.isEnable());
    }

}
