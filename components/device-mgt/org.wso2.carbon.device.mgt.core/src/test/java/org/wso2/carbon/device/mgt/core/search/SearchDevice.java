/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.search.Condition;
import org.wso2.carbon.device.mgt.common.search.SearchContext;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchManagerService;
import org.wso2.carbon.device.mgt.core.search.mgt.impl.SearchManagerServiceImpl;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;

import java.util.ArrayList;
import java.util.List;

public class SearchDevice extends BaseDeviceManagementTest {

    private static final Log log = LogFactory.getLog(SearchDevice.class);

    @BeforeClass
    @Override
    public void init() throws Exception {
        DeviceManagementProviderService deviceManagementProviderService = new DeviceManagementProviderServiceImpl();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceManagementProviderService);
    }

    @Test
    public void searchDeviceDetails() throws Exception {

        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();


        Condition cond = new Condition();
        cond.setKey("BATTERY_VOLTAGE");
        cond.setOperator("=");
        cond.setValue("40");
        cond.setState(Condition.State.AND);
        conditions.add(cond);

//        Condition cond2 = new Condition();
//        cond2.setKey("CPU_USAGE");
//        cond2.setOperator(">");
//        cond2.setValue("40");
//        cond2.setState(Condition.State.OR);
//        conditions.add(cond2);
//
//        Condition cond3 = new Condition();
//        cond3.setKey("LOCATION");
//        cond3.setOperator("=");
//        cond3.setValue("Colombo");
//        cond3.setState(Condition.State.AND);
//        conditions.add(cond3);

        context.setConditions(conditions);

        SearchManagerService service = new SearchManagerServiceImpl();
        List<Device> devices = service.search(context);

        Gson gson = new Gson();
        String bbbb = gson.toJson(devices);
        log.info(bbbb);


        for (Device device : devices) {
            log.debug(device.getDescription());
            log.debug(device.getDeviceIdentifier());
        }

    }

    @Test
    public void doValidLocationSearch() throws Exception {

        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition cond = new Condition();
        cond.setKey("LOCATION");
        cond.setOperator("=");
        cond.setValue("Karan");
        cond.setState(Condition.State.AND);
        conditions.add(cond);

        context.setConditions(conditions);

        SearchManagerService service = new SearchManagerServiceImpl();
        List<Device> devices = service.search(context);

        Gson gson = new Gson();
        String bbbb = gson.toJson(devices);
        log.info("Valid Search " + bbbb);


        for (Device device : devices) {
            log.debug(device.getDescription());
            log.debug(device.getDeviceIdentifier());
        }
    }

    @Test
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

        Gson gson = new Gson();
        String bbbb = gson.toJson(devices);
        log.info("Invalid Search " + bbbb);


        for (Device device : devices) {
            log.debug(device.getDescription());
            log.debug(device.getDeviceIdentifier());
        }
    }

    @Test
    public void doStringSearch() throws Exception {

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

        Gson gson = new Gson();
        String bbbb = gson.toJson(devices);
        log.info("Invalid Search " + bbbb);


        for (Device device : devices) {
            log.debug(device.getDescription());
            log.debug(device.getDeviceIdentifier());
        }
    }
}

