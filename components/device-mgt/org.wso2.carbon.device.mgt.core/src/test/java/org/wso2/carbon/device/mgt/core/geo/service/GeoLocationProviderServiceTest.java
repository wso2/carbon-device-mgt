/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.device.mgt.core.geo.service;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.geo.service.Alert;
import org.wso2.carbon.device.mgt.common.geo.service.AlertAlreadyExistException;
import org.wso2.carbon.device.mgt.common.geo.service.GeoFence;
import org.wso2.carbon.device.mgt.common.geo.service.GeoLocationBasedServiceException;
import org.wso2.carbon.device.mgt.core.TestDeviceManagementService;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub;
import org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.rmi.RemoteException;
import java.util.List;

public class GeoLocationProviderServiceTest {

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

    private GeoLocationProviderServiceImpl geoLocationProviderServiceImpl;
    private ExecutionPlanConfigurationDto[] mockExecutionPlanConfigurationDto = new ExecutionPlanConfigurationDto[1];
    private Device device;
    
    @BeforeClass
    public void init() throws Exception {
        initMocks();
        device = enrollDevice();
    }

    @Test(description = "Create a sample geo exit-alert with relevant details.")
    public void createGeoExitAlert() throws GeoLocationBasedServiceException, AlertAlreadyExistException {
        Boolean result = geoLocationProviderServiceImpl.
                createGeoAlert(getExitAlert(), getDeviceIdentifier(),
                        DeviceManagementConstants.GeoServices.ALERT_TYPE_EXIT, device.getEnrolmentInfo().getOwner());
        Assert.assertEquals(result, Boolean.TRUE);
    }

    @Test(description = "Create a sample geo within-alert with relevant details.")
    public void createGeoWithinAlert() throws GeoLocationBasedServiceException, AlertAlreadyExistException {
        Boolean result = geoLocationProviderServiceImpl.
                createGeoAlert(getWithinAlert(), getDeviceIdentifier(),
                        DeviceManagementConstants.GeoServices.ALERT_TYPE_WITHIN, device.getEnrolmentInfo().getOwner());
        Assert.assertEquals(result, Boolean.TRUE);
    }

    @Test(description = "Create a sample geo proximity-alert with relevant details.")
    public void createGeoProximityAlert() throws GeoLocationBasedServiceException, AlertAlreadyExistException {
        Boolean result = geoLocationProviderServiceImpl.
                createGeoAlert(getProximityAlert(), getDeviceIdentifier(),
                        DeviceManagementConstants.GeoServices.ALERT_TYPE_PROXIMITY, device.getEnrolmentInfo().getOwner());
        Assert.assertEquals(result, Boolean.TRUE);
    }

    @Test(description = "Create a sample geo speed-alert with relevant details.")
    public void createGeoSpeedAlert() throws GeoLocationBasedServiceException, AlertAlreadyExistException {
        Boolean result = geoLocationProviderServiceImpl.
                createGeoAlert(getSpeedAlert(), getDeviceIdentifier(),
                        DeviceManagementConstants.GeoServices.ALERT_TYPE_SPEED, device.getEnrolmentInfo().getOwner());
        Assert.assertEquals(result, Boolean.TRUE);
    }

    @Test(description = "Create a sample geo stationary-alert with relevant details.")
    public void createGeoStationaryAlert() throws GeoLocationBasedServiceException, AlertAlreadyExistException {
        Boolean result = geoLocationProviderServiceImpl.
                createGeoAlert(getStationaryAlert(), getDeviceIdentifier(),
                        DeviceManagementConstants.GeoServices.ALERT_TYPE_STATIONARY, device.getEnrolmentInfo().getOwner());
        Assert.assertEquals(result, Boolean.TRUE);
    }

    @Test(description = "Create a sample geo traffic-alert with relevant details.")
    public void createGeoTrafficAlert() throws GeoLocationBasedServiceException, AlertAlreadyExistException {
        Boolean result = geoLocationProviderServiceImpl.
                createGeoAlert(getTrafficAlert(), getDeviceIdentifier(),
                        DeviceManagementConstants.GeoServices.ALERT_TYPE_TRAFFIC, device.getEnrolmentInfo().getOwner());
        Assert.assertEquals(result, Boolean.TRUE);
    }

    @Test(dependsOnMethods = "createGeoSpeedAlert", description = "retrieve saved geo speed-alert.")
    public void getGeoSpeedAlerts() throws GeoLocationBasedServiceException {
        String result;
        result = geoLocationProviderServiceImpl.getSpeedAlerts(getDeviceIdentifier(), device.getEnrolmentInfo().getOwner());
        Assert.assertNotNull(result);
        Assert.assertEquals(result, "{'speedLimit':" + SAMPLE_SPEED_ALERT_VALUE + "}");
    }

    @Test(dependsOnMethods = "createGeoTrafficAlert", description = "retrieve saved geo exit-alert.")
    public void getGeoTrafficAlerts() throws GeoLocationBasedServiceException {
        List<GeoFence> geoFences;
        geoFences = geoLocationProviderServiceImpl.getTrafficAlerts(getDeviceIdentifier(), device.getEnrolmentInfo().getOwner());
        Assert.assertNotNull(geoFences);
        GeoFence geoFenceNode = geoFences.get(0);
        Assert.assertEquals(geoFenceNode.getGeoJson(), "{\n" +
                "  \"" + DeviceManagementConstants.GeoServices.GEO_FENCE_GEO_JSON + "\": \"" + SAMPLE_GEO_JSON + "\"\n" +
                "}");
    }

    @Test(dependsOnMethods = "createGeoStationaryAlert", description = "retrieve saved geo stationary-alert.")
    public void getGeoStationaryAlerts() throws GeoLocationBasedServiceException {
        List<GeoFence> geoFences;
        geoFences = geoLocationProviderServiceImpl.getStationaryAlerts(getDeviceIdentifier(), device.getEnrolmentInfo().getOwner());
        Assert.assertNotNull(geoFences);
        GeoFence geoFenceNode = geoFences.get(0);
        Assert.assertEquals(geoFenceNode.getAreaName(), SAMPLE_AREA_NAME);
        Assert.assertEquals(geoFenceNode.getQueryName(), SAMPLE_QUERY_NAME);
        Assert.assertEquals(geoFenceNode.getStationaryTime(), SAMPLE_STATIONARY_TIME);
    }

    private void initMocks() throws Exception {
        EventProcessorAdminServiceStub mockEventProcessorAdminServiceStub = Mockito.mock(EventProcessorAdminServiceStub.class);
        geoLocationProviderServiceImpl = Mockito.mock(GeoLocationProviderServiceImpl.class, Mockito.CALLS_REAL_METHODS);
        mockExecutionPlanConfigurationDto[0] = Mockito.mock(ExecutionPlanConfigurationDto.class);
        Mockito.doReturn(mockEventProcessorAdminServiceStub).
                when(geoLocationProviderServiceImpl).getEventProcessorAdminServiceStub();
        Mockito.doReturn("success").
                when(mockEventProcessorAdminServiceStub).validateExecutionPlan(Mockito.anyString());
        Mockito.when(mockEventProcessorAdminServiceStub.getAllActiveExecutionPlanConfigurations()).
                thenReturn(mockExecutionPlanConfigurationDto);
        Mockito.when(mockExecutionPlanConfigurationDto[0].getExecutionPlan()).thenReturn("EXECUTION_PLAN");
    }

    private DeviceIdentifier getDeviceIdentifier() {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(device.getDeviceIdentifier());
        deviceIdentifier.setType(device.getType());
        return deviceIdentifier;
    }

    private Alert getWithinAlert() {
        Alert alert = new Alert();
        alert.setDeviceId(DEVICE_ID);
        alert.setCepAction("CEP_ACTION");
        alert.setParseData("{\n" +
                "  \" " + DeviceManagementConstants.GeoServices.GEO_FENCE_GEO_JSON + "\": \"" + SAMPLE_GEO_JSON + "\"\n" +
                "}");
        alert.setCustomName(SAMPLE_AREA_NAME);
        alert.setExecutionPlan("EXECUTION_PLAN");
        alert.setQueryName(SAMPLE_QUERY_NAME);
        return alert;
    }

    private Alert getExitAlert() {
        Alert alert = new Alert();
        alert.setDeviceId(DEVICE_ID);
        alert.setQueryName(SAMPLE_QUERY_NAME);
        alert.setCustomName(SAMPLE_AREA_NAME);
        alert.setStationeryTime(SAMPLE_STATIONARY_TIME);
        alert.setFluctuationRadius(SAMPLE_FLUCTUATION_RADIUS);
        alert.setParseData("{\n" +
                "  \" " + DeviceManagementConstants.GeoServices.GEO_FENCE_GEO_JSON + "\": \"" + SAMPLE_GEO_JSON + "\"\n" +
                "}");
        alert.setExecutionPlan("EXECUTION_PLAN");
        return alert;
    }

    private Alert getProximityAlert() {
        Alert alert = new Alert();
        alert.setDeviceId(DEVICE_ID);
        alert.setProximityTime(SAMPLE_PROXIMITY_TIME);
        alert.setProximityDistance(SAMPLE_PROXIMITY_DISATANCE);
        alert.setParseData("{\n" +
                "  \" " + DeviceManagementConstants.GeoServices.GEO_FENCE_GEO_JSON + "\": \"" + SAMPLE_GEO_JSON + "\"\n" +
                "}");
        return alert;
    }

    private Alert getSpeedAlert() {
        Alert alert = new Alert();
        alert.setDeviceId(DEVICE_ID);
        alert.setParseData("{\n" +
                "  \"" + DeviceManagementConstants.GeoServices.GEO_FENCE_GEO_JSON + "\": \"" + SAMPLE_GEO_JSON + "\",\n" +
                "  \"" + DeviceManagementConstants.GeoServices.SPEED_ALERT_VALUE + "\": \"" + SAMPLE_SPEED_ALERT_VALUE + "\"\n" +
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
                "  \"" + DeviceManagementConstants.GeoServices.GEO_FENCE_GEO_JSON + "\": \"" + SAMPLE_GEO_JSON + "\"\n" +
                "}");
        return alert;
    }

    private Alert getTrafficAlert() {
        Alert alert = new Alert();
        alert.setDeviceId(DEVICE_ID);
        alert.setParseData("{\n" +
                "  \"" + DeviceManagementConstants.GeoServices.GEO_FENCE_GEO_JSON + "\": \"" + SAMPLE_GEO_JSON + "\"\n" +
                "}");
        alert.setCustomName(SAMPLE_AREA_NAME);
        alert.setExecutionPlan("EXECUTION_PLAN");
        alert.setQueryName(SAMPLE_QUERY_NAME);
        return alert;
    }

    private Device enrollDevice() throws Exception {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(DEVICE_ID, DEVICE_TYPE);
        Device device = TestDataHolder.generateDummyDeviceData(deviceIdentifier);
        DeviceManagementProviderService deviceMgtService = DeviceManagementDataHolder.getInstance().
                getDeviceManagementProvider();
        deviceMgtService.registerDeviceType(new TestDeviceManagementService(DEVICE_TYPE,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
        deviceMgtService.enrollDevice(device);

        Device returnedDevice = deviceMgtService.getDevice(deviceIdentifier);

        if (!returnedDevice.getDeviceIdentifier().equals(deviceIdentifier.getId())) {
            throw new Exception("Incorrect device with ID - " + device.getDeviceIdentifier() + " returned!");
        }
        
        return returnedDevice;
    }

}
