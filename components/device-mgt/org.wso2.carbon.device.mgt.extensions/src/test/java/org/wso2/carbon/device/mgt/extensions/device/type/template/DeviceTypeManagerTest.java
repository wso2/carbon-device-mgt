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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DeviceTypeConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.exception.DeviceTypeConfigurationException;
import org.wso2.carbon.device.mgt.extensions.device.type.template.dao.DeviceDAODefinition;
import org.wso2.carbon.device.mgt.extensions.device.type.template.dao.DeviceTypeDAOHandler;
import org.wso2.carbon.device.mgt.extensions.device.type.template.dao.DeviceTypePluginDAOImpl;
import org.wso2.carbon.device.mgt.extensions.device.type.template.dao.DeviceTypePluginDAOManager;
import org.wso2.carbon.device.mgt.extensions.internal.DeviceTypeExtensionDataHolder;
import org.wso2.carbon.device.mgt.extensions.utils.Utils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class tests the {@link DeviceTypeManager}
 */
public class DeviceTypeManagerTest {
    private DeviceTypeManager androidDeviceTypeManager;
    private DeviceIdentifier nonExistingDeviceIdentifier;
    private Device sampleDevice1;
    private Device sampleDevice2;
    private String androidDeviceType;

    @BeforeTest(description = "Mocking the classes for testing")
    public void setup() throws NoSuchFieldException, IllegalAccessException, IOException, SQLException, SAXException,
            ParserConfigurationException, DeviceTypeConfigurationException, JAXBException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource("android_h2.sql");
        androidDeviceType = "android";
        File androidDatabaseScript = null;
        javax.sql.DataSource dataSource = null;
        File carbonHome = new File("src/test/resources/carbon-home");

        if (resourceUrl != null) {
            androidDatabaseScript = new File(resourceUrl.getFile());
        }
        if (carbonHome.exists()) {
            System.setProperty("carbon.home", carbonHome.getAbsolutePath());
        }
        resourceUrl = classLoader.getResource("android.xml");
        File androidConfiguration = null;
        if (resourceUrl != null) {
            androidConfiguration = new File(resourceUrl.getFile());
        }
        DeviceTypeConfiguration androidDeviceConfiguration = Utils.getDeviceTypeConfiguration(androidConfiguration);
        androidDeviceTypeManager = Mockito.mock(DeviceTypeManager.class, Mockito.CALLS_REAL_METHODS);

        if (androidDatabaseScript != null) {
            dataSource = Utils.createDataTables("deviceType", androidDatabaseScript.getAbsolutePath());
        }
        DeviceTypePluginDAOManager deviceTypePluginDAOManager = createMockDeviceTypePluginDAOManager(dataSource,
                androidDeviceConfiguration);
        Field deviceTypePluginDAOManagerField = DeviceTypeManager.class.getDeclaredField("deviceTypePluginDAOManager");
        deviceTypePluginDAOManagerField.setAccessible(true);
        deviceTypePluginDAOManagerField.set(androidDeviceTypeManager, deviceTypePluginDAOManager);

        Field propertiesExist = DeviceTypeManager.class.getDeclaredField("propertiesExist");
        propertiesExist.setAccessible(true);
        Field deviceType = DeviceTypeManager.class.getDeclaredField("deviceType");
        deviceType.setAccessible(true);
        deviceType.set(androidDeviceTypeManager, androidDeviceType);
        propertiesExist.set(androidDeviceTypeManager, true);
        createDevice();
    }

    @Test(description = "This test case tests IsEnrolled method of the DeviceTypeManager",
            dependsOnMethods = {"testEnrollDevice"})
    public void testIsEnrolled() throws DeviceManagementException {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(sampleDevice2.getDeviceIdentifier(),
                sampleDevice2.getType());
        Assert.assertTrue(!androidDeviceTypeManager.isEnrolled(nonExistingDeviceIdentifier),
                "Device with " + "NON-Existing ID is not enrolled, but this shows as enrolled");
        Assert.assertTrue(androidDeviceTypeManager.isEnrolled(deviceIdentifier),
                "Enrolled device is shown as un-enrolled");
    }

    @Test(description = "This test case tests the getDevcie method of the DeviceTypeManager", dependsOnMethods =
            {"testEnrollDevice"})
    public void testGetDevice() throws DeviceManagementException {
        DeviceIdentifier existingDeviceIdntifier = new DeviceIdentifier(sampleDevice2.getDeviceIdentifier(),
                androidDeviceType);
        Assert.assertNull(androidDeviceTypeManager.getDevice(nonExistingDeviceIdentifier),
                "Non existing sampleDevice was retrieved");
        Assert.assertNotNull(androidDeviceTypeManager.getDevice(existingDeviceIdntifier),
                "Existing sampleDevice was retrieved");
    }

    @Test(description = "This test case tests the enrollment of the device")
    public void testEnrollDevice() throws DeviceManagementException {
        Assert.assertTrue(androidDeviceTypeManager.enrollDevice(sampleDevice1));
        Assert.assertTrue(!androidDeviceTypeManager.enrollDevice(sampleDevice2));
    }

    @Test(description = "This test case tests the get all devices method of the DeviceTypeManager", dependsOnMethods
            = {"testEnrollDevice"})
    public void testGetAllDevices() throws DeviceManagementException {
        Assert.assertEquals(androidDeviceTypeManager.getAllDevices().size(), 1,
                "All the added devices are not fetched " + "from the database");
    }

    @Test(description = "This test case tests the addition of platform configuration and retrieval of the same")
    public void testAddPlatformConfiguration() throws RegistryException, DeviceManagementException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        PlatformConfiguration platformConfiguration = new PlatformConfiguration();
        platformConfiguration.setType(androidDeviceType);
        RegistryService registryService = Utils.getRegistryService();
        Registry governanceSystemRegistry = registryService.getConfigSystemRegistry();
        DeviceTypeExtensionDataHolder.getInstance().setRegistryService(registryService);
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setRegistry(RegistryType.SYSTEM_CONFIGURATION, governanceSystemRegistry);
        androidDeviceTypeManager.saveConfiguration(platformConfiguration);
        androidDeviceTypeManager.getConfiguration();
        PlatformConfiguration actualPlatformConfiguration = androidDeviceTypeManager.getConfiguration();
        Assert.assertNotNull(actualPlatformConfiguration,
                "Platform Configuration saved and retrieved correctly in " + "DeviceType Manager");
        Assert.assertEquals(actualPlatformConfiguration.getType(), androidDeviceType,
                "Platform Configuration saved and " + "retrieved correctly in DeviceType Manager");
    }

    @Test (description = "This test case tests the getDefaultConfiguration method")
    public void testGetDefaultConfiguration()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getDefaultConfiguration = DeviceTypeManager.class.getDeclaredMethod("getDefaultConfiguration");
        getDefaultConfiguration.setAccessible(true);
        Assert.assertNull(getDefaultConfiguration.invoke(androidDeviceTypeManager), "Default configuration file "
                + "retrieved even without adding the configuration for the device type android");
    }

    @Test (description = "This test case tests the updateDeviceInfo method")
    public void testUpdateDeviceInfo() throws DeviceManagementException {
        DeviceIdentifier existingDeviceIdentifier = new DeviceIdentifier(sampleDevice2.getDeviceIdentifier(),
                androidDeviceType);
        Assert.assertFalse(androidDeviceTypeManager.updateDeviceInfo(nonExistingDeviceIdentifier, sampleDevice1),
                "Non-existing device was updated");
        Assert.assertTrue(androidDeviceTypeManager.updateDeviceInfo(existingDeviceIdentifier, sampleDevice1),
                "Existing device update failed");
    }

    /**
     * To create a sample sampleDevice to add to DAO Layer.
     */
    private void createDevice() {
        nonExistingDeviceIdentifier = new DeviceIdentifier("NON-EXISTING", androidDeviceType);
        List<Device.Property> list = new ArrayList<>();

        String[] deviceTypeAttributes = { "FCM_TOKEN", "DEVICE_INFO", "IMEI", "IMSI", "OS_VERSION", "DEVICE_MODEL",
                "VENDOR", "LATITUDE", "LONGITUDE", "SERIAL", "MAC_ADDRESS", "DEVICE_NAME", "DEVICE_NAME",
                "OS_BUILD_DATE" };

        for (String deviceTypeAttribute : deviceTypeAttributes) {
            Device.Property property = new Device.Property();
            property.setName(deviceTypeAttribute);
            property.setValue(deviceTypeAttribute + "T");
            list.add(property);
        }

        sampleDevice1 = new Device("testdevice", androidDeviceType, "test", "testdevice", null, null, list);
        sampleDevice2 = new Device("testdevice1", androidDeviceType, "test", "testdevice", null, null, list);
    }

    /*
     * To create a mock sampleDevice type plugin dao manager.
     * @param dataSource DataSource for the DAO layer
     * @param androidDeviceConfiguration Android Device Configuration
     * @return Mock Device Type Plugin DAO Manager
     * @throws NoSuchFieldException No Such Field Exception
     * @throws IllegalAccessException Illegal Access Exception
     */
    private DeviceTypePluginDAOManager createMockDeviceTypePluginDAOManager(javax.sql.DataSource dataSource,
            DeviceTypeConfiguration androidDeviceConfiguration) throws NoSuchFieldException, IllegalAccessException {
        Field datasourceField = DeviceTypeDAOHandler.class.getDeclaredField("dataSource");
        datasourceField.setAccessible(true);
        Field currentConnection = DeviceTypeDAOHandler.class.getDeclaredField("currentConnection");
        currentConnection.setAccessible(true);

        DeviceTypeDAOHandler deviceTypeDAOHandler = Mockito
                .mock(DeviceTypeDAOHandler.class, Mockito.CALLS_REAL_METHODS);
        datasourceField.set(deviceTypeDAOHandler, dataSource);
        currentConnection.set(deviceTypeDAOHandler, new ThreadLocal<Connection>());

        DeviceDAODefinition deviceDAODefinition = Utils.getDeviceDAODefinition(androidDeviceConfiguration);
        DeviceTypePluginDAOImpl deviceTypePluginDAO = new DeviceTypePluginDAOImpl(deviceDAODefinition,
                deviceTypeDAOHandler);

        DeviceTypePluginDAOManager deviceTypePluginDAOManager = Mockito
                .mock(DeviceTypePluginDAOManager.class, Mockito.CALLS_REAL_METHODS);
        Field deviceTypePluginDAOField = DeviceTypePluginDAOManager.class.getDeclaredField("deviceTypePluginDAO");
        deviceTypePluginDAOField.setAccessible(true);
        Field deviceTypeDAOHandlerField = DeviceTypePluginDAOManager.class.getDeclaredField("deviceTypeDAOHandler");
        deviceTypeDAOHandlerField.setAccessible(true);
        deviceTypePluginDAOField.set(deviceTypePluginDAOManager, deviceTypePluginDAO);
        deviceTypeDAOHandlerField.set(deviceTypePluginDAOManager, deviceTypeDAOHandler);

        return deviceTypePluginDAOManager;
    }
}
