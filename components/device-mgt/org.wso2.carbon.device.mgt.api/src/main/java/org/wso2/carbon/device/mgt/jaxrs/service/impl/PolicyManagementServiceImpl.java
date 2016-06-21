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
package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.PolicyWrapper;
import org.wso2.carbon.device.mgt.jaxrs.service.api.PolicyManagementService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.*;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.NotFoundException;
import org.wso2.carbon.device.mgt.jaxrs.beans.PolicyList;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.FilteringUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtUtil;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyAdministratorPoint;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/policies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PolicyManagementServiceImpl implements PolicyManagementService {

    private static final Log log = LogFactory.getLog(PolicyManagementServiceImpl.class);

    @POST
    @Override
    public Response addPolicy(PolicyWrapper policyWrapper) {
        RequestValidationUtil.validatePolicyDetails(policyWrapper);
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();

        try {
            Policy policy = this.getPolicyFromWrapper(policyWrapper);

            List<Device> devices = policy.getDevices();
            if (devices != null && devices.size() == 1) {
                DeviceAccessAuthorizationService deviceAccessAuthorizationService =
                        DeviceManagementDataHolder.getInstance().getDeviceAccessAuthorizationService();
                DeviceIdentifier deviceIdentifier = new DeviceIdentifier(devices.get(0).getDeviceIdentifier(),
                        devices.get(0).getType());
                PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                String username = threadLocalCarbonContext.getUsername();
                try {
                    if (!deviceAccessAuthorizationService.isUserAuthorized(deviceIdentifier, username)) {
                        throw new UnauthorizedAccessException(
                                new ErrorResponse.ErrorResponseBuilder().setCode(401l).setMessage
                                        ("Current logged in user is not authorized to add policies").build());
                    }
                } catch (DeviceAccessAuthorizationException e) {
                    String msg = "ErrorResponse occurred while checking if the current user is authorized to add a policy";
                    log.error(msg, e);
                    throw new UnexpectedServerErrorException(
                            new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
                }
            }

            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            pap.addPolicy(policy);
            return Response.status(Response.Status.CREATED).entity("Policy has been added successfully").build();
        } catch (PolicyManagementException e) {
            String msg = "ErrorResponse occurred while adding policy";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        } catch (DeviceManagementException e) {
            String msg = "ErrorResponse occurred while retrieving device list.";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
    }

    private Policy getPolicyFromWrapper(PolicyWrapper policyWrapper) throws DeviceManagementException {
        Policy policy = new Policy();
        policy.setPolicyName(policyWrapper.getPolicyName());
        policy.setDescription(policyWrapper.getDescription());
        policy.setProfile(DeviceMgtUtil.convertProfile(policyWrapper.getProfile()));
        policy.setOwnershipType(policyWrapper.getOwnershipType());
        policy.setActive(policyWrapper.isActive());
        policy.setRoles(policyWrapper.getRoles());
        policy.setUsers(policyWrapper.getUsers());
        policy.setCompliance(policyWrapper.getCompliance());
        //TODO iterates the device identifiers to create the object. need to implement a proper DAO layer here.
        List<Device> devices = null;
        List<DeviceIdentifier> deviceIdentifiers = policyWrapper.getDeviceIdentifiers();
        if (deviceIdentifiers != null) {
            for (DeviceIdentifier id : deviceIdentifiers) {
                devices.add(DeviceMgtAPIUtils.getDeviceManagementService().getDevice(id));
            }
        }
        policy.setDevices(devices);
        policy.setTenantId(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
        return policy;
    }

    @GET
    @Override
    public Response getPolicies(
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        List<Policy> policies;
        List<Policy> filteredPolicies;
        PolicyList targetPolicies = new PolicyList();
        try {
            PolicyAdministratorPoint policyAdministratorPoint = policyManagementService.getPAP();
            policies = policyAdministratorPoint.getPolicies();
            if (policies == null || policies.size() == 0) {
                throw new NotFoundException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("No policies found.").build());
            }
            targetPolicies.setCount(policies.size());
            filteredPolicies = FilteringUtil.getFilteredList(policies, offset, limit);
            if (filteredPolicies.size() == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("No policies found.").build();
            }
            targetPolicies.setList(filteredPolicies);
        } catch (PolicyManagementException e) {
            String msg = "ErrorResponse occurred while retrieving all available policies";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }

        return Response.status(Response.Status.OK).entity(targetPolicies).build();
    }

    @GET
    @Path("/{id}")
    @Override
    public Response getPolicy(@PathParam("id") int id, @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        final org.wso2.carbon.policy.mgt.common.Policy policy;
        try {
            PolicyAdministratorPoint policyAdministratorPoint = policyManagementService.getPAP();
            policy = policyAdministratorPoint.getPolicy(id);
            if (policy == null) {
                throw new NotFoundException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("No policy found.").build());
            }
        } catch (PolicyManagementException e) {
            String msg = "ErrorResponse occurred while retrieving policy corresponding to the id '" + id + "'";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity(policy).build();
    }

    @PUT
    @Path("/{id}")
    @Override
    public Response updatePolicy(@PathParam("id") int id, PolicyWrapper policyWrapper) {
        RequestValidationUtil.validatePolicyDetails(policyWrapper);
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        try {
            Policy policy = this.getPolicyFromWrapper(policyWrapper);
            policy.setId(id);
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            Policy exisitingPolicy = pap.getPolicy(id);
            if (exisitingPolicy == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Policy not found.").build();
            }
            pap.updatePolicy(policy);
            return Response.status(Response.Status.OK).entity("Policy has successfully been updated.").build();
        } catch (PolicyManagementException e) {
            String msg = "ErrorResponse occurred while updating the policy";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        } catch (DeviceManagementException e) {
            String msg = "ErrorResponse occurred while retrieving the device list.";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
    }

    @POST
    @Path("/remove-policy")
    @Override
    public Response removePolicies(List<Integer> policyIds) {
        RequestValidationUtil.validatePolicyIds(policyIds);
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        boolean policyDeleted = true;
        try {
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            for (int i : policyIds) {
                Policy policy = pap.getPolicy(i);
                if (policy == null || !pap.deletePolicy(policy)) {
                    policyDeleted = false;
                }
            }
        } catch (PolicyManagementException e) {
            String msg = "ErrorResponse occurred while removing policies";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        if (policyDeleted) {
            return Response.status(Response.Status.OK).entity("Policies have been successfully deleted").build();
        } else {
            //TODO:Check of this logic is correct
            throw new NotFoundException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("Policy doesn't exist").build());
        }
    }

    @PUT
    @Path("/activate-policy")
    @Override
    public Response activatePolicies(List<Integer> policyIds) {
        RequestValidationUtil.validatePolicyIds(policyIds);
        boolean isPolicyActivated = false;
        try {
            PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            for (int i : policyIds) {
                Policy policy = pap.getPolicy(i);
                if (policy != null) {
                    pap.activatePolicy(i);
                    isPolicyActivated = true;
                }
            }
        } catch (PolicyManagementException e) {
            String msg = "ErrorResponse occurred while activating policies";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        if (isPolicyActivated) {
            return Response.status(Response.Status.OK).entity("Selected policies have been successfully activated")
                    .build();
        } else {
            throw new NotFoundException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("Selected policies have " +
                            "not been activated").build());
        }
    }

    @PUT
    @Path("/deactivate-policy")
    @Override
    public Response deactivatePolicies(List<Integer> policyIds) {
        RequestValidationUtil.validatePolicyIds(policyIds);
        boolean isPolicyDeActivated = false;
        try {
            PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            for (int i : policyIds) {
                Policy policy = pap.getPolicy(i);
                if (policy != null) {
                    pap.inactivatePolicy(i);
                    isPolicyDeActivated = true;
                }
            }
        } catch (PolicyManagementException e) {
            String msg = "Exception in inactivating policies.";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        if (isPolicyDeActivated) {
            return Response.status(Response.Status.OK).entity("Selected policies have been successfully " +
                                                                      "deactivated").build();
        } else {
            throw new NotFoundException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("Selected policies have " +
                            "not been deactivated").build());
        }
    }

}
