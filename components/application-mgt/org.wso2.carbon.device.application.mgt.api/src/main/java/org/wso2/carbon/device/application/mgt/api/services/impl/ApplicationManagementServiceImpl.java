/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.core.components.ApplicationManager;
import org.wso2.carbon.device.application.mgt.core.dto.lists.ApplicationList;
import org.wso2.carbon.device.application.mgt.core.dto.Filter;
import org.wso2.carbon.device.application.mgt.core.util.ApplicationManagementUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Produces({ "application/json"})
@Consumes({ "application/json"})
public class ApplicationManagementServiceImpl {

    public static final int DEFAULT_LIMIT = 20;

    private static Log log = LogFactory.getLog(ApplicationManagementServiceImpl.class);


    @GET
    @Consumes("application/json")
    @Path("applications")
    public Response getApplications(@QueryParam("offset") int offset, @QueryParam("limit") int limit,
                                    @QueryParam("q") String searchQuery) {
        ApplicationManager applicationManager = ApplicationManagementUtil.getApplicationManager();
        try {

            if(limit == 0){
                limit = DEFAULT_LIMIT;
            }

            Filter filter = new Filter();
            filter.setOffset(offset);
            filter.setLimit(limit);
            filter.setSearchQuery(searchQuery);

            ApplicationList applications = applicationManager.getApplications(filter);
            return Response.status(Response.Status.OK).entity(applications).build();

        } catch (Exception e) {
            String msg = "Error occurred while getting the application list";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
