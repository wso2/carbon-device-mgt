/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfigurationManagementService;
import org.wso2.carbon.device.mgt.jaxrs.service.api.ConfigurationManagementService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a test class for {@link ConfigurationServiceImpl}.
 */
@PowerMockIgnore("javax.ws.rs.*")
@SuppressStaticInitializationFor({"org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils",
        "org.wso2.carbon.context.CarbonContext"})
@PrepareForTest({DeviceMgtAPIUtils.class, PolicyManagerUtil.class})
public class ConfigurationServiceImplTest {
    private ConfigurationManagementService configurationManagementService;
    private PlatformConfigurationManagementService platformConfigurationManagementService;
    private PlatformConfiguration platformConfiguration;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeClass
    public void init() {
        configurationManagementService = new ConfigurationServiceImpl();
        platformConfigurationManagementService = Mockito.mock(PlatformConfigurationManagementService.class);
        platformConfiguration = new PlatformConfiguration();
        platformConfiguration.setType("test");
    }

    @Test(description = "This method tests the getConfiguration method of ConfigurationManagementService under valid "
            + "conditions")
    public void testGetConfigurationWithSuccessConditions() throws ConfigurationManagementException {
        PowerMockito.stub(PowerMockito.method(PolicyManagerUtil.class, "getMonitoringFrequency")).toReturn(60);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getPlatformConfigurationManagementService"))
                .toReturn(platformConfigurationManagementService);
        Mockito.doReturn(platformConfiguration).when(platformConfigurationManagementService)
                .getConfiguration(Mockito.any());
        Response response = configurationManagementService.getConfiguration("test");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "getConfiguration request " + "failed with valid parameters");

        List<ConfigurationEntry> configurationEntryList = new ArrayList<>();
        ConfigurationEntry configurationEntry = new ConfigurationEntry();
        configurationEntry.setContentType("String");
        configurationEntry.setName("test");
        configurationEntry.setValue("test");
        configurationEntryList.add(configurationEntry);
        platformConfiguration.setConfiguration(configurationEntryList);
        Mockito.reset(platformConfigurationManagementService);
        Mockito.doReturn(platformConfiguration).when(platformConfigurationManagementService)
                .getConfiguration(Mockito.any());
        response = configurationManagementService.getConfiguration("test");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "getConfiguration request " + "failed with valid parameters");
    }

    @Test(description = "This method tests the getConfiguration method under negative conditions")
    public void testGetConfigurationUnderNegativeConditions() throws ConfigurationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getPlatformConfigurationManagementService"))
                .toReturn(platformConfigurationManagementService);
        Mockito.reset(platformConfigurationManagementService);
        Mockito.doThrow(new ConfigurationManagementException()).when(platformConfigurationManagementService)
                .getConfiguration(Mockito.any());
        Response response = configurationManagementService.getConfiguration("test");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "getConfiguration request " + "succeeded under negative conditions");
    }

    @Test(description = "This method tests the updateConfiguration method under valid conditions.", dependsOnMethods
            = {"testGetConfigurationWithSuccessConditions"})
    public void testUpdateConfigurationUnderValidConditions() throws ConfigurationManagementException {
        Mockito.reset(platformConfigurationManagementService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getPlatformConfigurationManagementService"))
                .toReturn(platformConfigurationManagementService);
        PowerMockito
                .stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getNotifierFrequency", PlatformConfiguration.class))
                .toReturn(60);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "scheduleTaskService", int.class))
                .toReturn(null);
        Mockito.doReturn(platformConfiguration).when(platformConfigurationManagementService)
                .getConfiguration(Mockito.any());
        Mockito.doReturn(true).when(platformConfigurationManagementService)
                .saveConfiguration(Mockito.any(), Mockito.any());
        Response response = configurationManagementService.updateConfiguration(platformConfiguration);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "updateConfiguration request failed with valid parameters");
    }

    @Test(description = "This method tests the updateConfiguration method under negative conditions.",
            dependsOnMethods = {"testGetConfigurationWithSuccessConditions"})
    public void testUpdateConfigurationUnderNegativeConditions() throws ConfigurationManagementException {
        Mockito.reset(platformConfigurationManagementService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getPlatformConfigurationManagementService"))
                .toReturn(platformConfigurationManagementService);
        Mockito.doThrow(new ConfigurationManagementException()).when(platformConfigurationManagementService)
                .saveConfiguration(Mockito.any(), Mockito.any());
        Response response = configurationManagementService.updateConfiguration(platformConfiguration);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "updateConfiguration request succeeded with in-valid parameters");
    }
}
