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

import org.wso2.carbon.device.mgt.jaxrs.beans.PasswordResetWrapper;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.UserManagementAdminService;
import org.wso2.carbon.device.mgt.jaxrs.util.CredentialManagementResponseBuilder;

import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserManagementAdminServiceImpl implements UserManagementAdminService {

    @POST
    @Path("/{username}/credentials")
    @Override
    public Response resetUserPassword(@PathParam("username")
                                      @Size(max = 45)
                                          String user, @QueryParam("domain") String domain, PasswordResetWrapper credentials) {
        if (domain != null) {
            user = domain + '/' + user;
        }
        return CredentialManagementResponseBuilder.buildResetPasswordResponse(user, credentials);
    }

}
