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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAuthorizationResult;
import org.wso2.carbon.device.mgt.jaxrs.beans.AuthorizationRequest;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.DeviceAccessAuthorizationAdminService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/admin/authorization")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceAccessAuthorizationAdminServiceImpl implements DeviceAccessAuthorizationAdminService {

    private static final Log log = LogFactory.getLog(DeviceAccessAuthorizationAdminServiceImpl.class);

    @POST
    @Override
    public Response isAuthorized(AuthorizationRequest authorizationRequest) {
        int currentTenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String loggedinUserTenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (authorizationRequest.getTenantDomain() != null) {
            if (!loggedinUserTenantDomain.equals(authorizationRequest.getTenantDomain())) {
                if (MultitenantConstants.SUPER_TENANT_ID != currentTenantId) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage(
                                    "Current logged in user is not authorized to perform this operation").build())
                            .build();
                }
            }
        } else {
            authorizationRequest.setTenantDomain(loggedinUserTenantDomain);
        }
        if (authorizationRequest.getTenantDomain() == null || authorizationRequest.getTenantDomain().isEmpty()) {
            authorizationRequest.setTenantDomain(
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    authorizationRequest.getTenantDomain(), true);
            String[] permissionArr = null;
            if (authorizationRequest.getPermissions() != null && authorizationRequest.getPermissions().size() > 0) {
                permissionArr = new String[authorizationRequest.getPermissions().size()];
                permissionArr = authorizationRequest.getPermissions().toArray(permissionArr);
            }
            DeviceAuthorizationResult deviceAuthorizationResult =
                    DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
                            authorizationRequest.getDeviceIdentifiers(), authorizationRequest.getUsername()
                            , permissionArr);

            return Response.status(Response.Status.OK).entity(deviceAuthorizationResult).build();
        } catch (DeviceAccessAuthorizationException e) {
            String msg = "Error occurred at server side while fetching authorization information.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
