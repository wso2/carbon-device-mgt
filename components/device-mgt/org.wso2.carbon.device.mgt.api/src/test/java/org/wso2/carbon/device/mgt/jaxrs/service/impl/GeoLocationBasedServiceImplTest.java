package org.wso2.carbon.device.mgt.jaxrs.service.impl;


import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.geo.GeoCluster;
import org.wso2.carbon.device.mgt.core.geo.geoHash.GeoCoordinate;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.service.api.GeoLocationBasedService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class GeoLocationBasedServiceImplTest {
    private DeviceManagementProviderService deviceManagementProviderService;
    private PrivilegedCarbonContext context;
    private GeoLocationBasedService geoLocationBasedService;

    @BeforeClass
    public void init() {
        deviceManagementProviderService = Mockito.mock(DeviceManagementProviderService.class);
        geoLocationBasedService = new GeoLocationBasedServiceImpl();
        context = Mockito.mock(PrivilegedCarbonContext.class);
        Mockito.doReturn("admin").when(context).getUsername();
    }

    @Test(description = "This method tests the behaviour of getGeoDeviceLocations when there are no devices" +
            "in the given map boundaries")
    public void testGetGeoDeviceLocations1() throws DeviceManagementException {
        Mockito.doReturn(new ArrayList<GeoCluster>()).when(deviceManagementProviderService)
                .findGeoClusters(Mockito.any(GeoCoordinate.class), Mockito.any(GeoCoordinate.class), Mockito.anyInt());
        Response response = geoLocationBasedService.getGeoDeviceLocations(0.4, 15, 75.6,
                90.1, 6);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "getGeoDeviceLocations request failed with valid parameters");
    }

    @Test(description = "This method tests the behaviour of getGeoDeviceLocations when there are devices" +
            "in the given map boundaries")
    public void testGetGeoDeviceLocations2() throws DeviceManagementException {
        List<GeoCluster> geoClusters = new ArrayList<>();
        geoClusters.add(new GeoCluster(new GeoCoordinate(1.5, 80.7),
                new GeoCoordinate(1.1, 79.5), new GeoCoordinate(1.9, 82.1), 3,
                "tb32", "aegtew234", "android", "1234"));
        geoClusters.add(new GeoCluster(new GeoCoordinate(10.2, 86.1),
                new GeoCoordinate(9.8, 84.7), new GeoCoordinate(11.1, 88.1), 4,
                "t1gd", "swerty12s", "android", "1234"));
        Mockito.doReturn(geoClusters).when(deviceManagementProviderService)
                .findGeoClusters(Mockito.any(GeoCoordinate.class), Mockito.any(GeoCoordinate.class), Mockito.anyInt());
        Response response = geoLocationBasedService.getGeoDeviceLocations(0.4, 15, 75.6,
                90.1, 6);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "getGeoDeviceLocations request failed with valid parameters");
    }
}
