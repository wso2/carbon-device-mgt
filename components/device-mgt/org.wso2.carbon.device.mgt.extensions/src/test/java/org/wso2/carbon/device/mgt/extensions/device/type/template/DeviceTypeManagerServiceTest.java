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

package org.wso2.carbon.device.mgt.extensions.device.type.template;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceStatusTaskPluginConfig;
import org.wso2.carbon.device.mgt.common.InitialOperationConfig;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.ProvisioningConfig;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DeviceStatusTaskConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DeviceTypeConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.PolicyMonitoring;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.PullNotificationSubscriberConfig;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.PushNotificationProvider;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.TaskConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.exception.DeviceTypeConfigurationException;
import org.wso2.carbon.device.mgt.extensions.utils.Utils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.powermock.api.mockito.PowerMockito.when;

/**
 * This is the test class for {@link DeviceTypeManagerService}
 */
public class DeviceTypeManagerServiceTest {
    private DeviceTypeManagerService androidDeviceTypeManagerService;
    private DeviceTypeConfiguration androidDeviceConfiguration;
    private DeviceTypeManagerService rasberrypiDeviceTypeManagerService;
    private DeviceTypeConfiguration rasberrypiDeviceConfiguration;
    private DeviceTypeManagerService arduinoDeviceTypeManagerService;
    private DeviceTypeConfiguration arduinoDeviceTypeConfiguration;
    private Method setProvisioningConfig;
    private Method setOperationMonitoringConfig;
    private Method setDeviceStatusTaskPluginConfig;
    private Method populatePushNotificationConfig;
    private Method setPolicyMonitoringManager;
    private Method setPullNotificationSubscriber;

    @BeforeTest
    public void setup() throws NoSuchMethodException, SAXException, JAXBException, ParserConfigurationException,
            DeviceTypeConfigurationException, IOException, NoSuchFieldException, IllegalAccessException,
            DeviceManagementException, RegistryException {
        ClassLoader classLoader = getClass().getClassLoader();

        setProvisioningConfig = DeviceTypeManagerService.class
                .getDeclaredMethod("setProvisioningConfig", String.class, DeviceTypeConfiguration.class);
        setProvisioningConfig.setAccessible(true);
        setDeviceStatusTaskPluginConfig = DeviceTypeManagerService.class
                .getDeclaredMethod("setDeviceStatusTaskPluginConfig", DeviceStatusTaskConfiguration.class);
        setDeviceStatusTaskPluginConfig.setAccessible(true);
        setOperationMonitoringConfig = DeviceTypeManagerService.class
                .getDeclaredMethod("setOperationMonitoringConfig", DeviceTypeConfiguration.class);
        setOperationMonitoringConfig.setAccessible(true);
        populatePushNotificationConfig = DeviceTypeManagerService.class
                .getDeclaredMethod("populatePushNotificationConfig", PushNotificationProvider.class);
        populatePushNotificationConfig.setAccessible(true);
        setPolicyMonitoringManager = DeviceTypeManagerService.class
                .getDeclaredMethod("setPolicyMonitoringManager", PolicyMonitoring.class);
        setPolicyMonitoringManager.setAccessible(true);
        setPullNotificationSubscriber = DeviceTypeManagerService.class
                .getDeclaredMethod("setPullNotificationSubscriber", PullNotificationSubscriberConfig.class);
        setPullNotificationSubscriber.setAccessible(true);

        Field deviceStatusTaskPluginConfig = DeviceTypeManagerService.class
                .getDeclaredField("deviceStatusTaskPluginConfig");
        deviceStatusTaskPluginConfig.setAccessible(true);

        Field operationMonitoringConfigs = DeviceTypeManagerService.class
                .getDeclaredField("operationMonitoringConfigs");
        operationMonitoringConfigs.setAccessible(true);

        Field initialOperationConfig = DeviceTypeManagerService.class.getDeclaredField("initialOperationConfig");
        initialOperationConfig.setAccessible(true);

        Field deviceManager = DeviceTypeManagerService.class.getDeclaredField("deviceManager");
        deviceManager.setAccessible(true);

        androidDeviceTypeManagerService = Mockito.mock(DeviceTypeManagerService.class, Mockito.CALLS_REAL_METHODS);
        deviceStatusTaskPluginConfig.set(androidDeviceTypeManagerService, new DeviceStatusTaskPluginConfig());
        operationMonitoringConfigs.set(androidDeviceTypeManagerService, new OperationMonitoringTaskConfig());
        initialOperationConfig.set(androidDeviceTypeManagerService, new InitialOperationConfig());

        rasberrypiDeviceTypeManagerService = Mockito.mock(DeviceTypeManagerService.class, Mockito.CALLS_REAL_METHODS);
        deviceStatusTaskPluginConfig.set(rasberrypiDeviceTypeManagerService, new DeviceStatusTaskPluginConfig());
        operationMonitoringConfigs.set(rasberrypiDeviceTypeManagerService, new OperationMonitoringTaskConfig());
        initialOperationConfig.set(rasberrypiDeviceTypeManagerService, new InitialOperationConfig());

        URL resourceUrl = classLoader.getResource(Utils.DEVICE_TYPE_FOLDER + "android.xml");

        File androidConfiguration = null;
        if (resourceUrl != null) {
            androidConfiguration = new File(resourceUrl.getFile());
        }
        androidDeviceConfiguration = Utils.getDeviceTypeConfiguration(androidConfiguration);

        resourceUrl = classLoader.getResource(Utils.DEVICE_TYPE_FOLDER + "raspberrypi.xml");
        File raspberrypiConfiguration = null;
        if (resourceUrl != null) {
            raspberrypiConfiguration = new File(resourceUrl.getFile());
        }
        rasberrypiDeviceConfiguration = Utils.getDeviceTypeConfiguration(raspberrypiConfiguration);

        PlatformConfiguration platformConfiguration = new PlatformConfiguration();
        platformConfiguration.setType("android");
        List<ConfigurationEntry> configurationEntries = new ArrayList<>();
        ConfigurationEntry configurationEntry = new ConfigurationEntry();
        configurationEntry.setValue("10");
        configurationEntry.setName("frequency");
        configurationEntry.setContentType("Integer");

        configurationEntries.add(configurationEntry);
        platformConfiguration.setConfiguration(configurationEntries);
        DeviceTypeManager deviceTypeManager = Mockito.mock(DeviceTypeManager.class);
        when(deviceTypeManager.getConfiguration()).thenReturn(platformConfiguration);
        deviceManager.set(androidDeviceTypeManagerService, deviceTypeManager);
        setupArduinoDeviceType();
    }

    @Test(description = "This test cases tests the retrieval of provisioning config after providing the configurations "
            + "values")
    public void testWithProvisioningConfig() throws Exception {
        boolean isRasberryPiSharedWithTenants =
                (rasberrypiDeviceConfiguration.getProvisioningConfig() != null) && rasberrypiDeviceConfiguration
                        .getProvisioningConfig().isSharedWithAllTenants();
        setProvisioningConfig.invoke(androidDeviceTypeManagerService, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME,
                androidDeviceConfiguration);
        ProvisioningConfig provisioningConfig = androidDeviceTypeManagerService.getProvisioningConfig();
        Assert.assertEquals(provisioningConfig.isSharedWithAllTenants(),
                androidDeviceConfiguration.getProvisioningConfig().isSharedWithAllTenants(),
                "Provisioning configs are not correctly set as per the configuration file provided");

        setProvisioningConfig.invoke(rasberrypiDeviceTypeManagerService, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME,
                rasberrypiDeviceConfiguration);
        provisioningConfig = rasberrypiDeviceTypeManagerService.getProvisioningConfig();
        Assert.assertEquals(provisioningConfig.isSharedWithAllTenants(), isRasberryPiSharedWithTenants,
                "Provisioning configs are not correctly set as per the configuration file provided.");
    }

    @Test (description = "This test case tests the Device task config retrieval")
    public void testDeviceStatusTaskConfig () throws InvocationTargetException, IllegalAccessException {
        setDeviceStatusTaskPluginConfig
                .invoke(androidDeviceTypeManagerService, androidDeviceConfiguration.getDeviceStatusTaskConfiguration());
        DeviceStatusTaskPluginConfig deviceStatusTaskPuginConfig = androidDeviceTypeManagerService
                .getDeviceStatusTaskPluginConfig();
        DeviceStatusTaskConfiguration deviceStatusTaskConfiguration = androidDeviceConfiguration
                .getDeviceStatusTaskConfiguration();

        Assert.assertEquals(deviceStatusTaskPuginConfig.getFrequency(), deviceStatusTaskConfiguration.getFrequency(),
                "Frequency provided in the device task configuration is not set properly.");
        Assert.assertEquals(deviceStatusTaskPuginConfig.getIdleTimeToMarkInactive(),
                deviceStatusTaskConfiguration.getIdleTimeToMarkInactive(),
                "Idle time to mark inactive provided in " + "the device task configuration is not set properly.");
        Assert.assertEquals(deviceStatusTaskPuginConfig.getIdleTimeToMarkUnreachable(),
                deviceStatusTaskConfiguration.getIdleTimeToMarkUnreachable(),
                "Idle time to mark un-reachable " + "provided in the device task configuration is not set properly.");
        Assert.assertEquals(deviceStatusTaskPuginConfig.isRequireStatusMonitoring(),
                deviceStatusTaskConfiguration.isEnabled(),
                "Enabled status provided in the device task configuration" + " is not set properly");

        setDeviceStatusTaskPluginConfig.invoke(rasberrypiDeviceTypeManagerService,
                rasberrypiDeviceConfiguration.getDeviceStatusTaskConfiguration());
        deviceStatusTaskPuginConfig = rasberrypiDeviceTypeManagerService.getDeviceStatusTaskPluginConfig();
        Assert.assertEquals(deviceStatusTaskPuginConfig.getFrequency(), 0);
    }

    @Test(description = "This test case aims to test whether correct operations are listed as per the configuration "
            + "of device types")
    public void testOperationConfig() throws InvocationTargetException, IllegalAccessException {
        setOperationMonitoringConfig.invoke(androidDeviceTypeManagerService, androidDeviceConfiguration);
        OperationMonitoringTaskConfig operationMonitoringTaskConfig = androidDeviceTypeManagerService
                .getOperationMonitoringConfig();
        TaskConfiguration taskConfiguration = androidDeviceConfiguration.getTaskConfiguration();
        Assert.assertEquals(operationMonitoringTaskConfig.getFrequency(), taskConfiguration.getFrequency(),
                "Policy " + "Monitoring frequency does not match with the frequency in the configuration");
        Assert.assertEquals(operationMonitoringTaskConfig.getMonitoringOperation().size(),
                taskConfiguration.getOperations().size(),
                "Number of task configuration operations does not match with the task "
                        + "configuration operations provided in the configuration file");

        setOperationMonitoringConfig.invoke(rasberrypiDeviceTypeManagerService, rasberrypiDeviceConfiguration);
        operationMonitoringTaskConfig = rasberrypiDeviceTypeManagerService.getOperationMonitoringConfig();
        Assert.assertEquals(operationMonitoringTaskConfig.getFrequency(), 0,
                "Frequency is set for a non-existing " + "operation task configuration");
    }

    @Test(description = "This test case tests the populateNotificationConfig method and retrieval of the same.")
    public void testPopulatePushNotificationConfig() throws InvocationTargetException, IllegalAccessException {
        populatePushNotificationConfig
                .invoke(androidDeviceTypeManagerService, androidDeviceConfiguration.getPushNotificationProvider());
        PushNotificationConfig pushNotificationConfig = androidDeviceTypeManagerService.getPushNotificationConfig();
        Assert.assertNotEquals(pushNotificationConfig, null, "Push notification configuration is set even though "
                + "Push notfication configuration was not mentioned.");

        populatePushNotificationConfig.invoke(rasberrypiDeviceTypeManagerService,
                rasberrypiDeviceConfiguration.getPushNotificationProvider());
        pushNotificationConfig = rasberrypiDeviceTypeManagerService.getPushNotificationConfig();
        PushNotificationProvider pushNotificationProvider = rasberrypiDeviceConfiguration.getPushNotificationProvider();
        Assert.assertEquals(pushNotificationConfig.getType(), pushNotificationProvider.getType());
        Assert.assertEquals(pushNotificationConfig.isScheduled(), pushNotificationProvider.isScheduled());
    }

    @Test(description = "This test case tests the initial operation configuration setting and retrieval of the same.")
    public void testSetInitialOperationConfig() throws InvocationTargetException, IllegalAccessException {
        androidDeviceTypeManagerService.setInitialOperationConfig(androidDeviceConfiguration);
        InitialOperationConfig initialOperationConfig = androidDeviceTypeManagerService.getInitialOperationConfig();

        Assert.assertEquals(initialOperationConfig.getOperations().size(),androidDeviceConfiguration.getOperations()
                .size());
    }

    @Test(description = "This test case tests the policy monitoring configuration setting.")
    public void testSetPolicyMonitoring() throws InvocationTargetException, IllegalAccessException {
        setPolicyMonitoringManager
                .invoke(androidDeviceTypeManagerService, androidDeviceConfiguration.getPolicyMonitoring());
        Assert.assertEquals(androidDeviceTypeManagerService.getPolicyMonitoringManager() != null,
                (androidDeviceConfiguration.getPolicyMonitoring() != null && androidDeviceConfiguration
                        .getPolicyMonitoring().isEnabled()),
                "Policy Management configurations are added as per the " + "configuration file");
        setPolicyMonitoringManager
                .invoke(rasberrypiDeviceTypeManagerService, rasberrypiDeviceConfiguration.getPolicyMonitoring());
        Assert.assertEquals(rasberrypiDeviceTypeManagerService.getPolicyMonitoringManager() != null,
                (rasberrypiDeviceConfiguration.getPolicyMonitoring() != null && rasberrypiDeviceConfiguration
                        .getPolicyMonitoring().isEnabled()),
                "Policy Management configurations are added as " + "per the " + "configuration file");
    }

    @Test (description = "This test case tests whether the Pull Notification Subscriber is set correctly.")
    public void testSetPullNotificationSubscriberConfig() throws InvocationTargetException, IllegalAccessException {
        setPullNotificationSubscriber.invoke(androidDeviceTypeManagerService, androidDeviceConfiguration
                .getPullNotificationSubscriberConfig());
        Assert.assertEquals(androidDeviceTypeManagerService.getPullNotificationSubscriber() != null,
                androidDeviceConfiguration.getPullNotificationSubscriberConfig() != null);
        setPullNotificationSubscriber.invoke(rasberrypiDeviceTypeManagerService, rasberrypiDeviceConfiguration
                .getPullNotificationSubscriberConfig());
        Assert.assertEquals(rasberrypiDeviceTypeManagerService.getPullNotificationSubscriber() != null,
                rasberrypiDeviceConfiguration.getPullNotificationSubscriberConfig() != null);

    }

    @Test (description = "This test case tests the addition and retrieval of the license")
    public void testGetLicense () throws LicenseManagementException {
        License license = arduinoDeviceTypeManagerService.getDeviceManager().getLicense("en_Us");
        Assert.assertEquals(license.getText(), arduinoDeviceTypeConfiguration.getLicense().getText(),
                "The retrieved" + " license is different from added license");
        license.setLanguage("eu");
        license.setText("This is a EU License");
        arduinoDeviceTypeManagerService.getDeviceManager().addLicense(license);
        License newLicense = arduinoDeviceTypeManagerService.getDeviceManager().getLicense("eu");
        Assert.assertEquals(newLicense.getText(), license.getText(),
                "The retrieved license is different from added license");
        Assert.assertNull(arduinoDeviceTypeManagerService.getDeviceManager().getLicense("tn"),
                "License is retrieved for a non-existing language code");
    }

    /**
     * Setting the Arduino Device Type
     * @throws RegistryException Registry Exception
     * @throws IOException IO Exception
     * @throws SAXException SAX Exception
     * @throws ParserConfigurationException Parser Configuration Exception
     * @throws DeviceTypeConfigurationException Device Type Configuration Exception
     * @throws JAXBException JAXB Exception
     */
    private void setupArduinoDeviceType()
            throws RegistryException, IOException, SAXException, ParserConfigurationException,
            DeviceTypeConfigurationException, JAXBException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource(Utils.DEVICE_TYPE_FOLDER + "arduino.xml");
        File arduinoConfiguration = null;
        if (resourceUrl != null) {
            arduinoConfiguration = new File(resourceUrl.getFile());
        }
        arduinoDeviceTypeConfiguration = Utils.getDeviceTypeConfiguration(arduinoConfiguration);
        arduinoDeviceTypeManagerService = new DeviceTypeManagerService(
                new DeviceTypeConfigIdentifier("arduino", "carbon.super"), arduinoDeviceTypeConfiguration);
    }
}
