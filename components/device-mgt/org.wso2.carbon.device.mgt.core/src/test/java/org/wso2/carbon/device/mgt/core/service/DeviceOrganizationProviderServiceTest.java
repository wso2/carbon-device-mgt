package org.wso2.carbon.device.mgt.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.DeviceOrganizationException;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.dao.DeviceOrganizationDAOException;

public class DeviceOrganizationProviderServiceTest extends BaseDeviceManagementTest {

    private static final Log log = LogFactory.getLog(DeviceOrganizationProviderServiceTest.class);

    DeviceOrganizationProviderServiceImpl deviceOrganizationProviderService;

    @BeforeClass
    @Override
    public void init() throws Exception {
        log.info("Initializing");

        deviceOrganizationProviderService = new DeviceOrganizationProviderServiceImpl();
    }

    @Test
    public void testGenerateHierarchy() throws DeviceOrganizationException {
        dummyDeviceOrgData();
        deviceOrganizationProviderService.generateHierarchy();
    }

    private void dummyDeviceOrgData() throws DeviceOrganizationException {
        deviceOrganizationProviderService.addDeviceOrganization("d1","device0", "d0",1, 1, 1);
        deviceOrganizationProviderService.addDeviceOrganization("d2","device2", "d0",1, 1, 1);
        deviceOrganizationProviderService.addDeviceOrganization("d3","device3", "d1",1, 1, 1);
        deviceOrganizationProviderService.addDeviceOrganization("d4","device4", "d1",1, 1, 1);
        deviceOrganizationProviderService.addDeviceOrganization("d5","device5", "d2",1, 1, 1);
        deviceOrganizationProviderService.addDeviceOrganization("d0","device0", "dx",1, 1, 1);
    }
}
