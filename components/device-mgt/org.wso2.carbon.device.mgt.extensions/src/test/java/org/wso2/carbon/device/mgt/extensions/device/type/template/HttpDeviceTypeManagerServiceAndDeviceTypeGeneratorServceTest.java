/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.extensions.device.type.template;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DeviceTypeConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.Feature;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.PushNotificationProvider;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.exception.DeviceTypeConfigurationException;
import org.wso2.carbon.device.mgt.extensions.utils.Utils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This test case contains the tests for {@link HTTPDeviceTypeManagerService} and {@link DeviceTypeGeneratorServiceImpl}
 */
public class HttpDeviceTypeManagerServiceAndDeviceTypeGeneratorServceTest {
    private DeviceTypeMetaDefinition deviceTypeMetaDefinition;
    private HTTPDeviceTypeManagerService httpDeviceTypeManagerService;
    private DeviceTypeGeneratorServiceImpl deviceTypeGeneratorService;
    private String androidSenseDeviceType = "androidsense";
    private DeviceManagementService generatedDeviceManagementService;

    @BeforeTest
    public void setup() throws RegistryException, IOException, SAXException, ParserConfigurationException,
            DeviceTypeConfigurationException, JAXBException {
        createSampleDeviceTypeMetaDefinition();
        httpDeviceTypeManagerService = new HTTPDeviceTypeManagerService(androidSenseDeviceType,
                deviceTypeMetaDefinition);
        deviceTypeGeneratorService = new DeviceTypeGeneratorServiceImpl();

    }

    @Test(description = "This test case tests the get type method of the device type manager")
    public void testGetType() {
        Assert.assertEquals(httpDeviceTypeManagerService.getType(), androidSenseDeviceType,
                "HttpDeviceTypeManagerService returns" + " a different device type than initially provided");
    }

    @Test(description = "This test case tests the enrollment of newly added device type")
    public void testEnrollDevice() throws DeviceManagementException {
        String deviceId = "testdevice1";
        Device sampleDevice1 = new Device(deviceId, androidSenseDeviceType, "test", "testdevice", null, null, null);
        Assert.assertTrue(httpDeviceTypeManagerService.getDeviceManager().enrollDevice(sampleDevice1),
                "Enrollment of " + androidSenseDeviceType + " device failed");
        Assert.assertTrue(httpDeviceTypeManagerService.getDeviceManager()
                        .isEnrolled(new DeviceIdentifier(deviceId, androidSenseDeviceType)),
                "Enrollment of " + androidSenseDeviceType + " device " + "failed");
    }

    @Test(description = "This test case tests the populate device management service method")
    public void testPopulateDeviceManagementService() {
        String sampleDeviceType = "sample";
        generatedDeviceManagementService = deviceTypeGeneratorService
                .populateDeviceManagementService(sampleDeviceType, deviceTypeMetaDefinition);
        Assert.assertEquals(generatedDeviceManagementService.getType(), sampleDeviceType,
                "DeviceTypeGeneration for the " + "sample device type failed");
    }

    @Test(description = "This test case tests the get configuration of the populated device management service though"
            + " DeviceTypeGeneratorService", dependsOnMethods = {"testPopulateDeviceManagementService"})
    public void testGetConfiguration() throws DeviceManagementException, ClassNotFoundException, JAXBException {
        PlatformConfiguration platformConfiguration = generatedDeviceManagementService.getDeviceManager()
                .getConfiguration();
        Assert.assertNotNull(platformConfiguration,
                "Default platform configuration is not added to sample device " + "type from the file system");

        List<ConfigurationEntry> configurationEntries = platformConfiguration.getConfiguration();
        Assert.assertNotNull(configurationEntries,
                "Platform Configuration entries are not parsed and saved " + "correctly for device type sample");
        Assert.assertEquals(configurationEntries.size(), 1,
                "Platform configuration is not saved correctly for " + "device type sample");

        ConfigurationEntry configurationEntry = configurationEntries.get(0);

        Assert.assertEquals(configurationEntry.getName(), "test",
                "Platform Configuration for device type " + "sample is not saved correctly");

        String contentType = configurationEntry.getContentType();
        Assert.assertEquals(contentType, "String",
                "Content type added in default platform configuration is different from the retrieved value");

    }


    @Test(description = "This test case tests the negative scenarios when saving the platform configurations",
            expectedExceptions = {DeviceManagementException.class})
    public void testSaveConfiguration() throws DeviceManagementException {
        httpDeviceTypeManagerService.getDeviceManager().saveConfiguration(null);
    }

    @Test(description = "This test case tests the negative scenarios when getting a device",
            expectedExceptions = {DeviceManagementException.class})
    public void testGetDevice() throws DeviceManagementException {
        httpDeviceTypeManagerService.getDeviceManager().getDevice(null);
    }

    @Test(description = "This test case tests the negative scenario when checking whether a device has enrolled",
            expectedExceptions = {DeviceManagementException.class})
    public void testIsEnrolled() throws DeviceManagementException {
        httpDeviceTypeManagerService.getDeviceManager().isEnrolled(null);
    }

    @Test(description = "This test case tests the negative scenario when enrolling a device",
            expectedExceptions = {DeviceManagementException.class})
    public void testEnroll() throws DeviceManagementException {
        httpDeviceTypeManagerService.getDeviceManager().enrollDevice(null);
    }

    @Test(description = "This test case tests the getDeviceTypeConfiguration method",
            dependsOnMethods = {"testPopulateDeviceManagementService"})
    public void testGetDeviceTypeConfiguration()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getDeviceTypeConfiguration = HTTPDeviceTypeManagerService.class
                .getDeclaredMethod("getDeviceTypeConfiguration", String.class, DeviceTypeMetaDefinition.class);
        getDeviceTypeConfiguration.setAccessible(true);
        List<String> properties = new ArrayList<>();
        properties.add("test");
        deviceTypeMetaDefinition.setProperties(properties);
        Map<String, String> mapProperties = new HashMap<>();
        mapProperties.put("test", "test");
        PushNotificationConfig pushNotificationConfig = new PushNotificationConfig("push", true, mapProperties);
        deviceTypeMetaDefinition.setPushNotificationConfig(pushNotificationConfig);
        DeviceTypeConfiguration deviceTypeConfiguration = (DeviceTypeConfiguration) getDeviceTypeConfiguration
                .invoke(httpDeviceTypeManagerService, "android", deviceTypeMetaDefinition);
        Assert.assertEquals(deviceTypeMetaDefinition.getProperties().size(),
                deviceTypeConfiguration.getDeviceDetails().getProperties().getProperty().size(), "Number of "
                        + "properties added in device-type meta definition is not equal to the properties added in "
                        + "the DeviceType Configuration");
    }

    /**
     * To create a sample device type meta defintion.
     * @throws SAXException SAX Exception.
     * @throws JAXBException JAXB Exception.
     * @throws ParserConfigurationException ParserConfiguration Exception.
     * @throws DeviceTypeConfigurationException DeviceTypeConfiguration Exception.
     * @throws IOException IO Exception.
     */
    private void createSampleDeviceTypeMetaDefinition()
            throws SAXException, JAXBException, ParserConfigurationException, DeviceTypeConfigurationException,
            IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource(Utils.DEVICE_TYPE_FOLDER + "android_sense.xml");
        File androidSenseConfiguration = null;

        if (resourceUrl != null) {
            androidSenseConfiguration = new File(resourceUrl.getFile());
        }
        DeviceTypeConfiguration androidSenseDeviceTypeConfiguration = Utils
                .getDeviceTypeConfiguration(androidSenseConfiguration);
        PushNotificationProvider pushNotificationProvider = androidSenseDeviceTypeConfiguration
                .getPushNotificationProvider();
        PushNotificationConfig pushNotificationConfig = new PushNotificationConfig(pushNotificationProvider.getType(),
                pushNotificationProvider.isScheduled(), null);
        org.wso2.carbon.device.mgt.extensions.device.type.template.config.License license =
                androidSenseDeviceTypeConfiguration.getLicense();
        License androidSenseLicense = new License();
        androidSenseLicense.setText(license.getText());
        androidSenseLicense.setLanguage(license.getLanguage());

        List<Feature> configurationFeatues = androidSenseDeviceTypeConfiguration.getFeatures().getFeature();
        List<org.wso2.carbon.device.mgt.common.Feature> features = new ArrayList<>();

        for (Feature feature : configurationFeatues) {
            org.wso2.carbon.device.mgt.common.Feature commonFeature = new org.wso2.carbon.device.mgt.common.Feature();
            commonFeature.setCode(feature.getCode());
            commonFeature.setDescription(feature.getDescription());
            commonFeature.setName(feature.getName());
            org.wso2.carbon.device.mgt.common.Feature.MetadataEntry metadataEntry = new org.wso2.carbon.device.mgt
                    .common.Feature.MetadataEntry();
            metadataEntry.setId(1);
            metadataEntry.setValue("test");
            List<org.wso2.carbon.device.mgt.common.Feature.MetadataEntry> metadataEntries = new ArrayList<>();
            metadataEntries.add(metadataEntry);
            commonFeature.setMetadataEntries(metadataEntries);
            features.add(commonFeature);
        }

        deviceTypeMetaDefinition = new DeviceTypeMetaDefinition();
        deviceTypeMetaDefinition.setPushNotificationConfig(pushNotificationConfig);
        deviceTypeMetaDefinition.setDescription("This is android_sense");
        deviceTypeMetaDefinition.setClaimable(true);
        deviceTypeMetaDefinition.setLicense(androidSenseLicense);
        deviceTypeMetaDefinition.setFeatures(features);
    }
}
