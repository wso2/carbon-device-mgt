/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.dynamic.client.web.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationException;
import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationService;
import org.wso2.carbon.dynamic.client.registration.OAuthApplicationInfo;
import org.wso2.carbon.dynamic.client.registration.profile.RegistrationProfile;
import org.wso2.carbon.dynamic.client.web.DynamicClientUtil;
import org.wso2.carbon.dynamic.client.web.FaultResponse;
import org.wso2.carbon.dynamic.client.web.RegistrationService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.QueryParam;

public class RegistrationServiceImpl implements RegistrationService {

    private static final Log log = LogFactory.getLog(RegistrationServiceImpl.class);

    @POST
    @Override
    public Response register(RegistrationProfile profile) {
        Response response;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().
                    setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            DynamicClientRegistrationService dynamicClientRegistrationService = DynamicClientUtil.
                                                                                  getDynamicClientRegistrationService();
            if (dynamicClientRegistrationService != null) {
                OAuthApplicationInfo info = dynamicClientRegistrationService.registerOAuthApplication(profile);
                return Response.status(Response.Status.CREATED).entity(info.toString()).build();
            }
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity("Dynamic Client Registration Service not available.").build();
        } catch (DynamicClientRegistrationException e) {
            String msg = "Error occurred while registering client '" + profile.getClientName() + "'";
            log.error(msg, e);
            response = Response.status(Response.Status.BAD_REQUEST).entity(
                    new FaultResponse(ErrorCode.INVALID_CLIENT_METADATA, msg)).build();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return response;
    }

    @DELETE
    @Override
    public Response unregister(@QueryParam("applicationName") String applicationName,
                               @QueryParam("userId") String userId,
                               @QueryParam("consumerKey") String consumerKey) {
        Response response;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            DynamicClientRegistrationService dynamicClientRegistrationService = DynamicClientUtil.
                                                                                  getDynamicClientRegistrationService();
            if (dynamicClientRegistrationService != null) {
                boolean status = dynamicClientRegistrationService.unregisterOAuthApplication(userId, applicationName,
                                                                                             consumerKey);
                if (status) {
                    return Response.status(Response.Status.OK).build();
                }
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity("Dynamic Client Registration Service not available.").build();
        } catch (DynamicClientRegistrationException e) {
            String msg = "Error occurred while un-registering client '" + applicationName + "'";
            log.error(msg, e);
            response = Response.serverError().entity(new FaultResponse(ErrorCode.INVALID_CLIENT_METADATA, msg)).build();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return response;
    }

}
