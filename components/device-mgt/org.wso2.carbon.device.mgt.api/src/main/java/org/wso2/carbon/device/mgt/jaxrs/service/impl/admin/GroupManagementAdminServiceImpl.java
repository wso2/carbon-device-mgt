/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.jaxrs.service.impl.admin;

import io.swagger.annotations.ApiParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.GroupManagementAdminService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;

@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupManagementAdminServiceImpl implements GroupManagementAdminService {

    private static final Log log = LogFactory.getLog(GroupManagementAdminServiceImpl.class);

    @Override
    public Response getGroupsOfUser(
            @QueryParam("username") String username,
            @HeaderParam("If-Modified-Since") Date timestamp,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        try {
            PaginationResult result =
                    DeviceMgtAPIUtils.getGroupManagementProviderService().getGroups(username, offset, limit);
            if (result != null && result.getRecordsTotal() > 0) {
                return Response.status(Response.Status.OK).entity(result).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (GroupManagementException e) {
            String msg = "Error occurred while retrieving the groups of user '" + username + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}
