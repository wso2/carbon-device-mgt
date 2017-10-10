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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.device.mgt.analytics.data.publisher.config.AnalyticsConfiguration;
import org.wso2.carbon.device.mgt.analytics.data.publisher.config.InvalidConfigurationStateException;
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherService;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherServiceImpl;

import java.lang.reflect.Field;

/**
 * This test class will test the methods that are exposed from {@link EventsPublisherService}
 */
public class EventPublisherServiceTest extends BaseAnalyticsDataPublisherTest {

    private static final String STREAM_NAME = "org.wso2.test.stream";
    private static final String TENANT_DOMAIN = "test.com";

    private EventsPublisherService eventsPublisherService;

    @BeforeClass
    public void initTest() {
        this.eventsPublisherService = new EventsPublisherServiceImpl();
    }

    @Test(description = "Publish the event before initializing",
            expectedExceptions = InvalidConfigurationStateException.class)
    public void publishBeforeInit() throws DataPublisherConfigurationException, NoSuchFieldException,
            IllegalAccessException, InstantiationException {
        Field configField = AnalyticsConfiguration.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(configField, null);
        this.eventsPublisherService.publishEvent(STREAM_NAME, "1.0.0", getEventProps(), getEventProps(),
                getEventProps());
    }

    @Test(description = "Publish with analytics config disabled", dependsOnMethods = "publishBeforeInit")
    public void publishWhenAnalyticsConfigDisabled() throws DataPublisherConfigurationException {
        AnalyticsConfiguration.init();
        AnalyticsConfiguration.getInstance().setEnable(false);
        boolean published = this.eventsPublisherService.publishEvent(STREAM_NAME, "1.0.0", getEventProps(),
                getEventProps(), getEventProps());
        Assert.assertFalse(published);
    }

    @Test(description = "Publish the event after initializing", dependsOnMethods = "publishWhenAnalyticsConfigDisabled")
    public void publishAfterInit() throws DataPublisherConfigurationException {
        AnalyticsConfiguration.getInstance().setEnable(true);
        boolean published = this.eventsPublisherService.publishEvent(STREAM_NAME, "1.0.0", getEventProps(),
                getEventProps(), getEventProps());
        Assert.assertTrue(published);
    }

    @Test(description = "Publish as tenant", dependsOnMethods = "publishAfterInit")
    public void publishAsTenant() throws DataPublisherConfigurationException {
        publishAsTenant(getEventProps());
    }

    @Test(description = "Publish the with no meta data as tenant", dependsOnMethods = "publishAsTenant",
            expectedExceptions = DataPublisherConfigurationException.class)
    public void publishAsTenantWithNoMetaData() throws DataPublisherConfigurationException {
        publishAsTenant(null);
    }

    @Test(description = "Publish the with empty meta data as tenant", dependsOnMethods = "publishAsTenant",
            expectedExceptions = DataPublisherConfigurationException.class)
    public void publishAsTenantWithEmptyMetaData() throws DataPublisherConfigurationException {
        publishAsTenant(new Object[0]);
    }

    @Test(description = "Publishing with invalid data publisher config",
            dependsOnMethods = {"publishAsTenantWithEmptyMetaData", "publishAsTenantWithNoMetaData"},
            expectedExceptions = DataPublisherConfigurationException.class)
    public void publishWithDataEndpointConfigException() throws DataPublisherConfigurationException,
            NoSuchFieldException, IllegalAccessException {
        AnalyticsConfiguration analyticsConfiguration = AnalyticsConfiguration.getInstance();
        analyticsConfiguration.setReceiverServerUrl("");
        Field dataPublisherField = DeviceDataPublisher.class.getDeclaredField("deviceDataPublisher");
        dataPublisherField.setAccessible(true);
        dataPublisherField.set(dataPublisherField, null);
        publishAsTenant(getEventProps());
    }

    @Test(description = "Publishing with invalid data publisher config",
            dependsOnMethods = "publishWithDataEndpointConfigException",
            expectedExceptions = DataPublisherConfigurationException.class)
    public void publishWithDataAgentConfigException() throws DataPublisherConfigurationException,
            NoSuchFieldException, IllegalAccessException, DataEndpointAgentConfigurationException {
        AnalyticsConfiguration.init();
        Field defaultAgentName = AgentHolder.getInstance().getClass().getDeclaredField("defaultDataEndpointAgentName");
        defaultAgentName.setAccessible(true);
        defaultAgentName.set(AgentHolder.getInstance(), "dummyAgent");
        publishAsTenant(getEventProps());
    }

    @Test(description = "Publishing with invalid data publisher config",
            dependsOnMethods = "publishWithDataAgentConfigException")
    public void publishWithDataEndpointException() throws DataPublisherConfigurationException,
            NoSuchFieldException, IllegalAccessException, DataEndpointAgentConfigurationException {
        AnalyticsConfiguration.init();

    }

    private void publishAsTenant(Object[] metaData) throws DataPublisherConfigurationException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(TENANT_DOMAIN, true);
        try {
            boolean published = this.eventsPublisherService.publishEvent(STREAM_NAME, "1.0.0", metaData,
                    getEventProps(), getEventProps());
            Assert.assertTrue(published);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private Object[] getEventProps() {
        return new Object[]{"123"};
    }


}
