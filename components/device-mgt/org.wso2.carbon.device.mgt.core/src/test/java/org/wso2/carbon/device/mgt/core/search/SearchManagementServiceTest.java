/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.search.Condition;
import org.wso2.carbon.device.mgt.common.search.SearchContext;
import org.wso2.carbon.device.mgt.core.TestDeviceManagementService;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.search.mgt.InvalidOperatorException;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchManagerService;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchMgtException;
import org.wso2.carbon.device.mgt.core.search.mgt.impl.SearchManagerServiceImpl;
import org.wso2.carbon.device.mgt.core.search.util.Utils;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * This class contains unit tests for the class SearchManagerService
 * */
public class SearchManagementServiceTest extends BaseDeviceManagementTest {

    private static final Log log = LogFactory.getLog(SearchManagementServiceTest.class);
    private static List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
    private static final String DEVICE_ID_PREFIX = "SEARCH-DEVICE-ID-";
    private static final String DEVICE_TYPE = "SEARCH_TYPE";

    @BeforeClass
    public void init() throws Exception {
        DeviceManagementDataHolder.getInstance().getDeviceAccessAuthorizationService();

        for (int i = 0; i < 5; i++) {
            deviceIdentifiers.add(new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE));
        }
        DeviceManagementProviderService deviceMgtService = new DeviceManagementProviderServiceImpl();
        DeviceManagementServiceComponent.notifyStartupListeners();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceMgtService);
        deviceMgtService.registerDeviceType(new TestDeviceManagementService(DEVICE_TYPE,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));

        List<Device> devices = TestDataHolder.generateDummyDeviceData(deviceIdentifiers);
        for (Device device : devices) {
            device.setDeviceInfo(Utils.getDeviceInfo());
            deviceMgtService.enrollDevice(device);
        }
        List<Device> returnedDevices = deviceMgtService.getAllDevices(DEVICE_TYPE, true);
        for (Device device : returnedDevices) {
            if (!device.getDeviceIdentifier().startsWith(DEVICE_ID_PREFIX)) {
                throw new Exception("Incorrect device with ID - " + device.getDeviceIdentifier() + " returned!");
            }
        }
    }

    @Test(description = "Search for device details.")
    public void searchDeviceDetails() throws Exception {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition cond = new Condition();
        cond.setKey("batteryVoltage");
        cond.setOperator("=");
        cond.setValue("40");
        cond.setState(Condition.State.OR);
        conditions.add(cond);

        context.setConditions(conditions);

        SearchManagerService service = new SearchManagerServiceImpl();
        List<Device> devices = service.search(context);
        Assert.assertTrue(devices != null);
    }

    @Test(description = "Search devices by location")
    public void doValidLocationSearch() throws Exception {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition cond = new Condition();
        cond.setKey("LOCATION");
        cond.setOperator("=");
        cond.setValue("Karandeniya");
        cond.setState(Condition.State.AND);
        conditions.add(cond);

        context.setConditions(conditions);

        SearchManagerService service = new SearchManagerServiceImpl();
        List<Device> devices = service.search(context);
        Assert.assertTrue(devices != null);
    }

    @Test(description = "Search devices by location.")
    public void doInvalidLocationSearch() throws Exception {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition cond = new Condition();
        cond.setKey("LOCATION");
        cond.setOperator("=");
        cond.setValue("Colombo");
        cond.setState(Condition.State.AND);
        conditions.add(cond);

        context.setConditions(conditions);

        SearchManagerService service = new SearchManagerServiceImpl();
        List<Device> devices = service.search(context);
        Assert.assertTrue(devices.size() == 0);
    }

    @Test(description = "Search devices by string parameter.")
    public void testStringSearch() throws Exception {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition cond = new Condition();
        cond.setKey("deviceModel");
        cond.setOperator("=");
        cond.setValue("SM-T520");
        cond.setState(Condition.State.AND);
        conditions.add(cond);

        context.setConditions(conditions);

        SearchManagerService service = new SearchManagerServiceImpl();
        List<Device> devices = service.search(context);

        Assert.assertTrue(devices != null);
    }

    @Test(description = "Search devices by Double parameter.")
    public void testDoubleSearch() throws Exception {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition cond = new Condition();
        cond.setKey("internalAvailableMemory");
        cond.setOperator("=");
        cond.setValue("3.56");
        cond.setState(Condition.State.AND);
        conditions.add(cond);

        context.setConditions(conditions);

        SearchManagerService service = new SearchManagerServiceImpl();
        List<Device> devices = service.search(context);

        Assert.assertTrue(devices != null);
    }

    @Test(expectedExceptions = {SearchMgtException.class})
    public void testInvalidOperator() throws SearchMgtException {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition cond = new Condition();
        cond.setKey("deviceModel");
        cond.setOperator("=/");
        cond.setValue("SM-T520");
        cond.setState(Condition.State.OR);
        conditions.add(cond);

        context.setConditions(conditions);

        SearchManagerService service = new SearchManagerServiceImpl();
        List<Device> devices = service.search(context);

        Assert.assertTrue(devices != null);
    }

    @Test(description = "Test for search updated devices in given time.")
    public void testGetUpdatedDevices() throws SearchMgtException {
        SearchManagerService service = new SearchManagerServiceImpl();
        List<Device> updatedDevices = service.getUpdated(Calendar.getInstance().getTimeInMillis());
        Assert.assertEquals(updatedDevices.size(), 0);
    }

    @Test(description = "Test for invalid number")
    public void testInvalidNumber() throws SearchMgtException {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition cond = new Condition();
        cond.setKey("batteryLevel");
        cond.setOperator("=");
        cond.setValue("bbb");
        cond.setState(Condition.State.OR);
        conditions.add(cond);

        context.setConditions(conditions);

        SearchManagerService service = new SearchManagerServiceImpl();
        try {
            service.search(context);
        } catch (SearchMgtException e) {
            if (!(e.getCause() instanceof InvalidOperatorException)) {
                throw e;
            }
        }
    }

    @Test(description = "Test multiple search conditions")
    public void testMultipleConditions() throws SearchMgtException {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition cond = new Condition();
        cond.setKey("batteryLevel");
        cond.setOperator("%");
        cond.setValue("40");
        cond.setState(Condition.State.OR);
        conditions.add(cond);

        Condition cond2 = new Condition();
        cond2.setKey("availableTotalMemory");
        cond2.setOperator("=");
        cond2.setValue("40.0");
        cond2.setState(Condition.State.OR);
        conditions.add(cond2);

        Condition cond3 = new Condition();
        cond3.setKey("LOCATION");
        cond3.setOperator("=");
        cond3.setValue("Karandeniya");
        cond3.setState(Condition.State.OR);
        conditions.add(cond3);

        Condition cond4 = new Condition();
        cond4.setKey("deviceModel");
        cond4.setOperator("=");
        cond4.setValue("SM-T520");
        cond4.setState(Condition.State.AND);
        conditions.add(cond4);

        Condition cond5 = new Condition();
        cond5.setKey("vendor");
        cond5.setOperator("=");
        cond5.setValue("Samsung");
        cond5.setState(Condition.State.AND);
        conditions.add(cond5);

        Condition cond6 = new Condition();
        cond6.setKey("osVersion");
        cond6.setOperator("=");
        cond6.setValue("Marshmellow");
        cond6.setState(Condition.State.OR);
        conditions.add(cond6);

        context.setConditions(conditions);

        SearchManagerService service = new SearchManagerServiceImpl();
        List<Device> devices = service.search(context);
        Assert.assertTrue(devices != null);
    }

    @Test(description = "Test with wildcard operator")
    public void testWithWildcardOperator() throws SearchMgtException {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition condition = new Condition();
        condition.setKey("batteryLevel");
        condition.setOperator("=");
        condition.setValue("40");
        condition.setState(Condition.State.AND);
        conditions.add(condition);

        Condition condition2 = new Condition();
        condition2.setKey("LOCATION");
        condition2.setOperator("%");
        condition2.setValue("Karandeniya");
        condition2.setState(Condition.State.OR);
        conditions.add(condition2);

        Condition condition3 = new Condition();
        condition3.setKey("internalTotalMemory");
        condition3.setOperator("%");
        condition3.setValue("23.2");
        condition3.setState(Condition.State.OR);
        conditions.add(condition3);

        Condition condition4 = new Condition();
        condition4.setKey("connectionType");
        condition4.setOperator("%");
        condition4.setValue("DIALOG");
        condition4.setState(Condition.State.AND);
        conditions.add(condition4);

        context.setConditions(conditions);

        SearchManagerService service = new SearchManagerServiceImpl();
        List<Device> devices = service.search(context);
        Assert.assertTrue(devices != null);
    }
}
