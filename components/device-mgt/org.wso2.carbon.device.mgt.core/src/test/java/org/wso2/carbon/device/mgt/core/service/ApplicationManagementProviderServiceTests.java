package org.wso2.carbon.device.mgt.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;

public class ApplicationManagementProviderServiceTests extends BaseDeviceManagementTest{

    private static final Log log = LogFactory.getLog(ApplicationManagementProviderServiceTests.class);

    @BeforeClass
    @Override
    public void init() throws Exception {
        this.initDatSource();
    }

    @Test
    public void testAddApplications() {

/*        ArrayList<Application> sourceApplications = new ArrayList<Application>();
        sourceApplications.add(TestDataHolder.generateApplicationDummyData("Test App2"));
        sourceApplications.add(TestDataHolder.generateApplicationDummyData("Test App3"));

        DeviceManagementTests deviceManagementDAOTests = new DeviceManagementTests();
        deviceManagementDAOTests.testAddDeviceTest();

        Device device = TestDataHolder.initialTestDevice;

        try {
            DeviceManagementDAOFactory.openConnection();


        } catch (DeviceManagementDAOException e) {
            log.error("Error occurred while adding application", e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }
        *//* Retrieving the application by its name *//*
        Application target = null;
        try {
            target = this.getApplication(source.getApplicationIdentifier(), -1234);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving application info";
            log.error(msg, e);
            Assert.fail(msg, e);
        }

        Assert.assertEquals(target.getApplicationIdentifier(), source.getApplicationIdentifier(), "Application added is not as same as " +
                "what's " +
                "retrieved");*/
    }


}
