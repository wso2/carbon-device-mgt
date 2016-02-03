/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.common.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;
import org.wso2.carbon.device.mgt.common.AbstractManagerService;
import org.wso2.carbon.device.mgt.common.impl.analytics.statistics.dto.DeviceUsageDTO;

import javax.jws.WebService;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@WebService
public class StatsManagerService extends AbstractManagerService {

    private static final Log log = LogFactory.getLog(StatsManagerService.class);

    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;

    @Path("/stats/devices/{deviceType}/{deviceIdentifier}")
    @GET
    @Produces("application/json")
    public Response getDeviceStats(@PathParam("deviceType") String deviceType,
                                   @PathParam("deviceIdentifier") String deviceIdentifier,
                                   @QueryParam("table") String table, @QueryParam("column") String column,
                                   @QueryParam("username") String user, @QueryParam("from") long from,
                                   @QueryParam("to") long to) {

        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);

        List<DeviceUsageDTO> deviceUsageDTOs = new ArrayList<>();
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ctx.setTenantDomain("carbon.super", true);
        DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx.getOSGiService(
                DeviceAnalyticsService.class, null);
        String query = "owner:" + user + " AND deviceId:" + deviceIdentifier + " AND deviceType:" + deviceType
                + " AND time : [" + fromDate + " TO " + toDate + "]";
        try {
            List<Record> records = deviceAnalyticsService.getAllSensorEventsForDevice(table, query);

            Collections.sort(records, new Comparator<Record>() {

                @Override
                public int compare(Record o1, Record o2) {
                    long t1 = (Long) o1.getValue("time");
                    long t2 = (Long) o2.getValue("time");
                    if (t1 < t2) {
                        return -1;
                    } else if (t1 > t2) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            for (Record record : records) {
                DeviceUsageDTO deviceUsageDTO = new DeviceUsageDTO();
                deviceUsageDTO.setTime("" + (long) record.getValue("time"));
                deviceUsageDTO.setValue("" + (float) record.getValue(column.toLowerCase()));
                deviceUsageDTOs.add(deviceUsageDTO);
            }
            DeviceUsageDTO[] deviceUsageDTOsArr = deviceUsageDTOs.toArray(
                    new DeviceUsageDTO[deviceUsageDTOs.size()]);
            return Response.status(Response.Status.OK).entity(deviceUsageDTOsArr).build();
        } catch (AnalyticsException e) {
            String errorMsg = "Error on retrieving stats on table " + table + " with query " + query;
            log.error(errorMsg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}