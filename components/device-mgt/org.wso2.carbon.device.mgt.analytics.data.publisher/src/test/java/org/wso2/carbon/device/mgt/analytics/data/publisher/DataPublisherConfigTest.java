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

public class DataPublisherConfigTest extends BaseAnalyticsDataPublisherTest {

    @Test(description = "Validating the behaviour od getInstance of the config before calling the init",
            expectedExceptions = InvalidConfigurationStateException.class)
    public void testGetInstanceWithoutInit(){
        AnalyticsConfiguration.getInstance();
    }

    public void testInitWithInvalidConfig() throws DataPublisherConfigurationException {
        AnalyticsConfiguration.init();
    }

    @Test (description = "Validating the init method with all required params", dependsOnMethods = "testGetInstanceWithoutInit")
    public void testInit() throws DataPublisherConfigurationException {
        AnalyticsConfiguration.init();
        AnalyticsConfiguration analyticsConfiguration = AnalyticsConfiguration.getInstance();
        Assert.assertEquals(analyticsConfiguration.getAdminPassword(), "testuserpwd");
        Assert.assertEquals(analyticsConfiguration.getAdminUsername(), "testuser");
        Assert.assertEquals(analyticsConfiguration.getReceiverServerUrl(), "tcp://localhost:7615");
        Assert.assertTrue(analyticsConfiguration.isEnable());
    }
}
