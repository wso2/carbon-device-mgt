/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.device.mgt.jaxrs.api.impl;

import io.swagger.annotations.Api;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.device.details.DeviceWrapper;
import org.wso2.carbon.device.mgt.common.search.SearchContext;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchManagerService;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchMgtException;
import org.wso2.carbon.device.mgt.jaxrs.api.DeviceSearch;
import org.wso2.carbon.device.mgt.jaxrs.api.util.DeviceMgtAPIUtils;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/search")
@Api(value = "DeviceSearch", description = "Device searching related operations can be found here.")
@SuppressWarnings("NonJaxWsWebServices")
public class DeviceSearchImpl implements DeviceSearch {

    private static Log log = LogFactory.getLog(DeviceSearchImpl.class);

    @GET
    public Response getDeviceInfo(SearchContext searchContext) {

        SearchManagerService searchManagerService;
        List<DeviceWrapper> devices;
        try {
            searchManagerService = DeviceMgtAPIUtils.getSearchManagerService();
            devices = searchManagerService.search(searchContext);

        } catch (SearchMgtException e) {
            String msg = "Error occurred while searching the device information.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(devices).build();
    }

    @POST
    @Path("after/{time}")
    public Response getUpdatedDevices(@PathParam("time") String time){

        SearchManagerService searchManagerService;
        List<DeviceWrapper> devices;
        try {
            searchManagerService = DeviceMgtAPIUtils.getSearchManagerService();
            devices = searchManagerService.getUpdated(Long.parseLong(time));

        } catch (SearchMgtException e) {
            String msg = "Error occurred while retrieving the updated device information after the given time.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(devices).build();

    }

}

