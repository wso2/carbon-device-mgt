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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.jaxrs.api.Profile;
import org.wso2.carbon.device.mgt.jaxrs.api.util.DeviceMgtAPIUtils;
import org.wso2.carbon.device.mgt.jaxrs.api.util.ResponsePayload;
import org.wso2.carbon.policy.mgt.common.PolicyAdministratorPoint;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@SuppressWarnings("NonJaxWsWebServices")
public class ProfileImpl implements Profile{
	private static Log log = LogFactory.getLog(ProfileImpl.class);

	@POST
    public Response addProfile(org.wso2.carbon.policy.mgt.common.Profile profile) {
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        try {
			PolicyAdministratorPoint pap = policyManagementService.getPAP();
			profile = pap.addProfile(profile);
            return Response.status(Response.Status.OK).entity(profile).build();
        } catch (PolicyManagementException e) {
            String msg = "Policy Management related exception";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
	@POST
	@Path("{id}")
    public Response updateProfile(org.wso2.carbon.policy.mgt.common.Profile profile,
                                  @PathParam("id") String profileId) {
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        ResponsePayload responseMsg = new ResponsePayload();
		try {
			PolicyAdministratorPoint pap = policyManagementService.getPAP();
			pap.updateProfile(profile);
			responseMsg.setMessageFromServer("Profile has been updated successfully.");
            return Response.status(Response.Status.OK).entity(responseMsg).build();
        } catch (PolicyManagementException e) {
            String msg = "Policy Management related exception";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
	@DELETE
	@Path("{id}")
    public Response deleteProfile(@PathParam("id") int profileId) {
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        ResponsePayload responseMsg = new ResponsePayload();
		try {
			PolicyAdministratorPoint pap = policyManagementService.getPAP();
			org.wso2.carbon.policy.mgt.common.Profile profile = pap.getProfile(profileId);
			pap.deleteProfile(profile);
			responseMsg.setMessageFromServer("Profile has been deleted successfully.");
            return Response.status(Response.Status.OK).entity(responseMsg).build();
        } catch (PolicyManagementException e) {
            String msg = "Policy Management related exception";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
