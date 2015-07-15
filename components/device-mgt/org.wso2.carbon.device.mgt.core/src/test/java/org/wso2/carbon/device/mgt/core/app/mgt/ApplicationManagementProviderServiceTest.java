package org.wso2.carbon.device.mgt.core.app.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.core.DeviceManagementPluginRepository;
import org.wso2.carbon.device.mgt.core.TestDeviceManagementService;
import org.wso2.carbon.device.mgt.core.api.mgt.ApplicationManagementProviderService;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagerProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.app.mgt.config.AppManagementConfig;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;

import java.util.ArrayList;
import java.util.List;

public class ApplicationManagementProviderServiceTest {

    private ApplicationManagementProviderService appMgtProvider;
    private static final Log log = LogFactory.getLog(ApplicationManagementProviderServiceTest.class);
    private DeviceManagementPluginRepository deviceManagementPluginRepository = null;

    @BeforeClass
    public void init() {
        deviceManagementPluginRepository = new DeviceManagementPluginRepository();
        TestDeviceManagementService testDeviceManagementService = new TestDeviceManagementService(TestDataHolder.TEST_DEVICE_TYPE);
        try {
            deviceManagementPluginRepository.addDeviceManagementProvider(testDeviceManagementService);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while initiate plugins '" + TestDataHolder.TEST_DEVICE_TYPE + "'";
            log.error(msg, e);
            Assert.fail(msg, e);
        }
    }

    @Test
    public void updateApplicationTest(){

        List<Application> applications = new ArrayList<Application>();

        Application application1 =  TestDataHolder.generateApplicationDummyData("org.wso2.app1");
        Application application2 = TestDataHolder.generateApplicationDummyData("org.wso2.app2");
        Application application3 = TestDataHolder.generateApplicationDummyData("org.wso2.app3");

        applications.add(application1);
        applications.add(application2);
        applications.add(application3);

        Device device =  TestDataHolder.initialTestDevice;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(TestDataHolder.initialDeviceIdentifier);
        deviceIdentifier.setType(device.getType());

        AppManagementConfig appManagementConfig = new AppManagementConfig();
        appMgtProvider = new ApplicationManagerProviderServiceImpl(deviceManagementPluginRepository, true);

        try {
            appMgtProvider.updateApplicationListInstalledInDevice(deviceIdentifier, applications);
        } catch (ApplicationManagementException appMgtEx){
            String msg = "Error occurred while updating app list '" + TestDataHolder.TEST_DEVICE_TYPE + "'";
            log.error(msg, appMgtEx);
            Assert.fail(msg, appMgtEx);
        }

        Application application4 = TestDataHolder.generateApplicationDummyData("org.wso2.app3");
        applications = new ArrayList<Application>();
        applications.add(application4);
        applications.add(application3);

        try {
            appMgtProvider.updateApplicationListInstalledInDevice(deviceIdentifier, applications);
            List<Application> installedApps = appMgtProvider.getApplicationListForDevice(deviceIdentifier);
            Assert.assertEquals(installedApps.size(),2,"Num of installed applications should be two");
        } catch (ApplicationManagementException appMgtEx){
            String msg = "Error occurred while updating app list '" + TestDataHolder.TEST_DEVICE_TYPE + "'";
            log.error(msg, appMgtEx);
            Assert.fail(msg, appMgtEx);
        }

    }

}
