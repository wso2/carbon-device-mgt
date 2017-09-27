package org.wso2.carbon.device.mgt.extensions.device.type.template;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DeviceTypeConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.Feature;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.Operation;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.PushNotificationProvider;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.exception.DeviceTypeConfigurationException;
import org.wso2.carbon.device.mgt.extensions.internal.DeviceTypeExtensionDataHolder;
import org.wso2.carbon.device.mgt.extensions.license.mgt.registry.RegistryBasedLicenseManager;
import org.wso2.carbon.device.mgt.extensions.utils.Utils;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.FileUtil;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.governance.api.util.GovernanceUtils.getGovernanceArtifactConfiguration;

/**
 * This test case contains the tests for {@link HTTPDeviceTypeManagerService}
 */
public class HttpDeviceTypeManagerServiceTest {
    private DeviceTypeMetaDefinition deviceTypeMetaDefinition;
    private HTTPDeviceTypeManagerService httpDeviceTypeManagerService;
    private String androidSenseDeviceType = "androidsense";

    @BeforeTest
    public void setup() throws RegistryException, IOException, SAXException, ParserConfigurationException,
            DeviceTypeConfigurationException, JAXBException {
        createSampleDeviceTypeMetaDefinition();
        httpDeviceTypeManagerService = new HTTPDeviceTypeManagerService(androidSenseDeviceType,
                deviceTypeMetaDefinition);

    }

    private void createSampleDeviceTypeMetaDefinition()
            throws SAXException, JAXBException, ParserConfigurationException, DeviceTypeConfigurationException,
            IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource("android_sense.xml");
        File androidSenseConfiguration = null;

        if (resourceUrl != null) {
            androidSenseConfiguration = new File(resourceUrl.getFile());
        }
        DeviceTypeConfiguration androidSenseDeviceTypeConfiguration = Utils
                .getDeviceTypeConfiguration(androidSenseConfiguration);
        PushNotificationProvider pushNotificationProvider = androidSenseDeviceTypeConfiguration
                .getPushNotificationProvider();
        PushNotificationConfig pushNotificationConfig = new PushNotificationConfig(pushNotificationProvider.getType()
                , pushNotificationProvider.isScheduled(), null);
        org.wso2.carbon.device.mgt.extensions.device.type.template.config.License license = androidSenseDeviceTypeConfiguration.getLicense();
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
            features.add(commonFeature);
        }

        deviceTypeMetaDefinition = new DeviceTypeMetaDefinition();
        deviceTypeMetaDefinition.setPushNotificationConfig(pushNotificationConfig);
        deviceTypeMetaDefinition.setDescription("This is android_sense");
        deviceTypeMetaDefinition.setClaimable(true);
        deviceTypeMetaDefinition.setLicense(androidSenseLicense);
        deviceTypeMetaDefinition.setFeatures(features);
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
}

