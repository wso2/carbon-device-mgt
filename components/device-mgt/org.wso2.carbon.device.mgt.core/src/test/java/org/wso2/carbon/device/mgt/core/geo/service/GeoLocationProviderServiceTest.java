package org.wso2.carbon.device.mgt.core.geo.service;

import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.powermock.core.classloader.annotations.PrepareForTest;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.geo.service.Alert;
import org.wso2.carbon.device.mgt.common.geo.service.GeoFence;
import org.wso2.carbon.device.mgt.core.TestDeviceManagementService;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderServiceImpl;
import org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.List;

import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest(GeoLocationProviderServiceImpl.class)
public class GeoLocationProviderServiceTest {

    private static final Log log = LogFactory.getLog(GeoLocationProviderServiceTest.class);
    private static final String DEVICE_TYPE = "GL_TEST_TYPE";
    private static final String DEVICE_ID = "GL-TEST-DEVICE-ID-1";
    private static final String SAMPLE_GEO_JSON = "12121";
    private static final String SAMPLE_AREA_NAME = "CUSTOM_NAME";
    private static final String SAMPLE_QUERY_NAME = "QUERY_NAME";
    private static final String SAMPLE_PROXIMITY_DISATANCE = "100";
    private static final String SAMPLE_PROXIMITY_TIME = "50";
    private static final String SAMPLE_SPEED_ALERT_VALUE = "120";
    private static final String SAMPLE_STATIONARY_TIME = "1500";
    private static final String SAMPLE_FLUCTUATION_RADIUS = "2000";

    private EventProcessorAdminServiceStub mockEventProcessorAdminServiceStub;
    private GeoLocationProviderServiceImpl geoLocationProviderServiceImpl;

    @BeforeClass
    public void init() throws Exception {
        initMocks();
        enrollTestDevice();
    }

    @Test
    public void createGeoExitAlert() throws Exception {
         Boolean result = geoLocationProviderServiceImpl.
                createGeoAlert(getTestAlert(), getDeviceIdentifier(), DeviceManagementConstants.GeoServices.ALERT_TYPE_EXIT);
        Assert.assertEquals(result, Boolean.TRUE);
        verifyPrivate(geoLocationProviderServiceImpl).invoke("getEventProcessorAdminServiceStub");
    }

    @Test
    public void createGeoWithinAlert() throws Exception {
         Boolean result = geoLocationProviderServiceImpl.
                createGeoAlert(getTestAlert(), getDeviceIdentifier(), DeviceManagementConstants.GeoServices.ALERT_TYPE_WITHIN);
        Assert.assertEquals(result, Boolean.TRUE);
    }

    @Test
    public void createGeoProximityAlert() throws Exception {
        Boolean result = geoLocationProviderServiceImpl.
                createGeoAlert(getProximityAlert(), getDeviceIdentifier(), DeviceManagementConstants.GeoServices.ALERT_TYPE_PROXIMITY);
        Assert.assertEquals(result, Boolean.TRUE);
    }

    @Test
    public void createGeoSpeedAlert() throws Exception {
        Boolean result = geoLocationProviderServiceImpl.
                createGeoAlert(getSpeedAlert(), getDeviceIdentifier(), DeviceManagementConstants.GeoServices.ALERT_TYPE_SPEED);
        Assert.assertEquals(result, Boolean.TRUE);
    }

    @Test
    public void createGeoStationaryAlert() throws Exception {
        Boolean result = geoLocationProviderServiceImpl.
                createGeoAlert(getStationaryAlert(), getDeviceIdentifier(), DeviceManagementConstants.GeoServices.ALERT_TYPE_STATIONARY);
        Assert.assertEquals(result, Boolean.TRUE);
    }

    @Test
    public void createGeoTrafficAlert() throws Exception {
        Boolean result = geoLocationProviderServiceImpl.
                createGeoAlert(getTrafficAlert(), getDeviceIdentifier(), DeviceManagementConstants.GeoServices.ALERT_TYPE_TRAFFIC);
        Assert.assertEquals(result, Boolean.TRUE);
    }

    //Test methods to retrieve saved Data.
    @Test(dependsOnMethods = "createGeoExitAlert")
    public void getGeoExitAlerts() throws Exception {
        List<GeoFence> geoFences;
        geoFences = geoLocationProviderServiceImpl.getExitAlerts(getDeviceIdentifier());
        Assert.assertNotNull(geoFences);
        GeoFence geoFenceNode = geoFences.get(0);
        Assert.assertEquals(geoFenceNode.getGeoJson(), SAMPLE_GEO_JSON);
        Assert.assertEquals(geoFenceNode.getAreaName(), SAMPLE_AREA_NAME);
        Assert.assertEquals(geoFenceNode.getQueryName(), SAMPLE_QUERY_NAME);
    }

    @Test(dependsOnMethods = "createGeoWithinAlert")
    public void getGeoWithinAlerts() throws Exception {
        List<GeoFence> geoFences;
        geoFences = geoLocationProviderServiceImpl.getWithinAlerts(getDeviceIdentifier());
        Assert.assertNotNull(geoFences);
        GeoFence geoFenceNode = geoFences.get(0);
        Assert.assertEquals(geoFenceNode.getAreaName(), SAMPLE_AREA_NAME);
        Assert.assertEquals(geoFenceNode.getQueryName(), SAMPLE_QUERY_NAME);
    }

    @Test(dependsOnMethods = "createGeoSpeedAlert")
    public void getGeoSpeedAlerts() throws Exception {
        String result;
        result = geoLocationProviderServiceImpl.getSpeedAlerts(getDeviceIdentifier());
        Assert.assertNotNull(result);
        Assert.assertEquals(result, "{'speedLimit':"+SAMPLE_SPEED_ALERT_VALUE+"}");
    }

    @Test(dependsOnMethods = "createGeoTrafficAlert")
    public void getGeoTrafficAlerts() throws Exception {
        List<GeoFence> geoFences;
        geoFences = geoLocationProviderServiceImpl.getTrafficAlerts(getDeviceIdentifier());
        Assert.assertNotNull(geoFences);
        GeoFence geoFenceNode = geoFences.get(0);
        Assert.assertEquals(geoFenceNode.getGeoJson(), "{\n" +
                "  \"geoFenceGeoJSON\": \"" + SAMPLE_GEO_JSON + "\"\n" +
                "}");
    }

    @Test(dependsOnMethods = "createGeoStationaryAlert")
    public void getGeoStationaryAlerts() throws Exception {
        List<GeoFence> geoFences;
        geoFences = geoLocationProviderServiceImpl.getStationaryAlerts(getDeviceIdentifier());
        Assert.assertNotNull(geoFences);
        GeoFence geoFenceNode = geoFences.get(0);
        Assert.assertEquals(geoFenceNode.getAreaName(), SAMPLE_AREA_NAME);
        Assert.assertEquals(geoFenceNode.getQueryName(), SAMPLE_QUERY_NAME);
        Assert.assertEquals(geoFenceNode.getStationaryTime(), SAMPLE_STATIONARY_TIME);
    }

    private void initMocks() throws JWTClientException, RemoteException {
        mockEventProcessorAdminServiceStub = Mockito.mock(EventProcessorAdminServiceStub.class);
        geoLocationProviderServiceImpl = Mockito.mock(GeoLocationProviderServiceImpl.class, Mockito.CALLS_REAL_METHODS);
        doReturn(mockEventProcessorAdminServiceStub).when(geoLocationProviderServiceImpl).getEventProcessorAdminServiceStub();
        doReturn("success").when(mockEventProcessorAdminServiceStub).validateExecutionPlan(Mockito.anyString());
    }

    private DeviceIdentifier getDeviceIdentifier() {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId("1234");
        deviceIdentifier.setType("TEST");
        return deviceIdentifier;
    }
    private RegistryService getRegistryService() throws RegistryException {
        RealmService realmService = new InMemoryRealmService();
        RegistryDataHolder.getInstance().setRealmService(realmService);
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("carbon-home/repository/conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }

    private void enrollTestDevice() throws Exception {
        Device device = TestDataHolder.generateDummyDeviceData(DEVICE_ID);
        DeviceManagementProviderService deviceMgtService = new DeviceManagementProviderServiceImpl();
        DeviceManagementServiceComponent.notifyStartupListeners();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceMgtService);
        DeviceManagementDataHolder.getInstance().setRegistryService(getRegistryService());
        DeviceManagementDataHolder.getInstance().setDeviceAccessAuthorizationService(new DeviceAccessAuthorizationServiceImpl());
        DeviceManagementDataHolder.getInstance().setGroupManagementProviderService(new GroupManagementProviderServiceImpl());
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(null);
        deviceMgtService.registerDeviceType(new TestDeviceManagementService(DEVICE_TYPE,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
            deviceMgtService.enrollDevice(device);
    }

    private Alert getTestAlert() {
        Alert alert = new Alert();
        alert.setDeviceId(DEVICE_ID);
        alert.setCepAction("CEP_ACTION");
        alert.setParseData("{\n" +
                "  \"geoFenceGeoJSON\": \""+SAMPLE_GEO_JSON+"\"\n" +
                "}");
        alert.setCustomName(SAMPLE_AREA_NAME);
        alert.setExecutionPlan("EXECUTION_PLAN");
        alert.setQueryName(SAMPLE_QUERY_NAME);
        return alert;
    }

    private Alert getProximityAlert() {
        Alert alert = new Alert();
        alert.setDeviceId(DEVICE_ID);
        alert.setProximityTime(SAMPLE_PROXIMITY_TIME);
        alert.setProximityDistance(SAMPLE_PROXIMITY_DISATANCE);
        alert.setParseData("{\n" +
                "  \"geoFenceGeoJSON\": \""+SAMPLE_GEO_JSON+"\"\n" +
                "}");
        return alert;
    }

    private Alert getSpeedAlert() {
        Alert alert =  getTestAlert();
        alert.setParseData("{\n" +
                "  \"geoFenceGeoJSON\": \"" + SAMPLE_GEO_JSON + "\",\n" +
                "  \"speedAlertValue\": \"" + SAMPLE_SPEED_ALERT_VALUE + "\"\n" +
                "}");
        return alert;
    }

    private Alert getStationaryAlert() {
        Alert alert = new Alert();
        alert.setDeviceId(DEVICE_ID);
        alert.setQueryName(SAMPLE_QUERY_NAME);
        alert.setCustomName(SAMPLE_AREA_NAME);
        alert.setStationeryTime(SAMPLE_STATIONARY_TIME);
        alert.setFluctuationRadius(SAMPLE_FLUCTUATION_RADIUS);
        alert.setParseData("{\n" +
                "  \"geoFenceGeoJSON\": \"" + SAMPLE_GEO_JSON + "\"\n" +
                "}");
        return alert;
    }

    private Alert getTrafficAlert() {
        Alert alert = new Alert();
        alert.setDeviceId(DEVICE_ID);
        alert.setParseData("{\n" +
                "  \"geoFenceGeoJSON\": \"" + SAMPLE_GEO_JSON + "\"\n" +
                "}");
        alert.setCustomName(SAMPLE_AREA_NAME);
        alert.setExecutionPlan("EXECUTION_PLAN");
        alert.setQueryName(SAMPLE_QUERY_NAME);
        return alert;
    }
}
