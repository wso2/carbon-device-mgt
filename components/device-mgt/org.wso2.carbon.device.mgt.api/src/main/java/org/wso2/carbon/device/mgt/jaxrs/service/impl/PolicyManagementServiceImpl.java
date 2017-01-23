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
import org.wso2.carbon.device.mgt.jaxrs.beans.PolicyList;
import org.wso2.carbon.device.mgt.jaxrs.beans.PolicyWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.PriorityUpdatedPolicyWrapper;
import org.wso2.carbon.device.mgt.jaxrs.service.api.PolicyManagementService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.FilteringUtil;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtUtil;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyAdministratorPoint;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Path("/policies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PolicyManagementServiceImpl implements PolicyManagementService {

    private static final String API_BASE_PATH = "/policies";
    private static final Log log = LogFactory.getLog(PolicyManagementServiceImpl.class);

    @POST
    @Override
    public Response addPolicy(@Valid PolicyWrapper policyWrapper) {
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
                        return Response.status(Response.Status.UNAUTHORIZED).entity(
                                new ErrorResponse.ErrorResponseBuilder().setMessage
                                        ("Current logged in user is not authorized to add policies").build()).build();
                    }
                } catch (DeviceAccessAuthorizationException e) {
                    String msg = "Error occurred while checking if the current user is authorized to add a policy";
                    log.error(msg, e);
                    return Response.serverError().entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
                }
            }

            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            Policy createdPolicy = pap.addPolicy(policy);

            return Response.created(new URI(API_BASE_PATH + "/" + createdPolicy.getId())).entity(createdPolicy).build();
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while adding policy";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving device list.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        } catch (URISyntaxException e) {
            String msg = "Error occurred while composing the location URI, which represents information of the " +
                    "newly created policy";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    private Policy getPolicyFromWrapper(@Valid PolicyWrapper policyWrapper) throws DeviceManagementException {
        Policy policy = new Policy();
        policy.setPolicyName(policyWrapper.getPolicyName());
        policy.setDescription(policyWrapper.getDescription());
        policy.setProfile(DeviceMgtUtil.convertProfile(policyWrapper.getProfile()));
        policy.setOwnershipType(policyWrapper.getOwnershipType());
        policy.setActive(policyWrapper.isActive());
        policy.setRoles(policyWrapper.getRoles());
        policy.setUsers(policyWrapper.getUsers());
        policy.setCompliance(policyWrapper.getCompliance());
        policy.setDeviceGroups(policyWrapper.getDeviceGroups());
        //TODO iterates the device identifiers to create the object. need to implement a proper DAO layer here.
        List<Device> devices = new ArrayList<Device>();
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
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        List<Policy> policies;
        List<Policy> filteredPolicies;
        PolicyList targetPolicies = new PolicyList();
        try {
            PolicyAdministratorPoint policyAdministratorPoint = policyManagementService.getPAP();
            policies = policyAdministratorPoint.getPolicies();
            targetPolicies.setCount(policies.size());
            filteredPolicies = FilteringUtil.getFilteredList(policies, offset, limit);
            targetPolicies.setList(filteredPolicies);
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving all available policies";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }

        return Response.status(Response.Status.OK).entity(targetPolicies).build();
    }

    @GET
    @Path("/{id}")
    @Override
    public Response getPolicy(@PathParam("id") int id, @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        final Policy policy;
        try {
            PolicyAdministratorPoint policyAdministratorPoint = policyManagementService.getPAP();
            policy = policyAdministratorPoint.getPolicy(id);
            if (policy == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(
                                "No policy found with the id '" + id + "'").build()).build();
            }
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving policy corresponding to the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(policy).build();
    }

    @PUT
    @Path("/{id}")
    @Override
    public Response updatePolicy(@PathParam("id") int id, @Valid PolicyWrapper policyWrapper) {
        RequestValidationUtil.validatePolicyDetails(policyWrapper);
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        try {
            Policy policy = this.getPolicyFromWrapper(policyWrapper);
            policy.setId(id);
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            Policy existingPolicy = pap.getPolicy(id);
            if (existingPolicy == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Policy not found.").build();
            }
            pap.updatePolicy(policy);
            return Response.status(Response.Status.OK).entity("Policy has successfully been updated.").build();
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while updating the policy";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the device list.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @POST
    @Path("/remove-policy")
    @Override
    public Response removePolicies(List<Integer> policyIds) {
        RequestValidationUtil.validatePolicyIds(policyIds);
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        boolean policyDeleted = true;
        String invalidPolicyIds = "";
        try {
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            for (int i : policyIds) {
                Policy policy = pap.getPolicy(i);
                if (policy == null) {
                    invalidPolicyIds += i + ",";
                    policyDeleted = false;
                }
            }
            if (policyDeleted) {
                for (int i : policyIds) {
                    Policy policy = pap.getPolicy(i);
                    pap.deletePolicy(policy);
                }
            }
        } catch (PolicyManagementException e) {
            String msg = "ErrorResponse occurred while removing policies";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        if (policyDeleted) {
            return Response.status(Response.Status.OK).entity("Policies have been successfully " +
                                                              "deleted").build();
        } else {
            //TODO:Check of this logic is correct
            String modifiedInvalidPolicyIds =
                    invalidPolicyIds.substring(0, invalidPolicyIds.length() - 1);
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(new ErrorResponse.ErrorResponseBuilder().
                            setMessage("Policies with the policy ID " + modifiedInvalidPolicyIds +
                                       " doesn't exist").build()).build();
        }
    }

    @POST
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
            String msg = "Error occurred while activating policies";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        }
        if (isPolicyActivated) {
            return Response.status(Response.Status.OK).entity("Selected policies have been successfully activated")
                    .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage("Selected policies have " +
                            "not been activated").build()).build();
        }
    }

    @POST
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
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        if (isPolicyDeActivated) {
            return Response.status(Response.Status.OK).entity("Selected policies have been successfully " +
                    "deactivated").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage("Selected policies have " +
                            "not been deactivated").build()).build();
        }
    }

    @Override
    @PUT
    @Produces("application/json")
    @Path("apply-changes")
    public Response applyChanges() {
        try {
            PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            pap.publishChanges();
        } catch (PolicyManagementException e) {
            String msg = "Exception in applying changes.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity("Changes have been successfully updated.").build();
    }

    @PUT
    @Path("/priorities")
    public Response updatePolicyPriorities(List<PriorityUpdatedPolicyWrapper> priorityUpdatedPolicies) {
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        List<Policy> policiesToUpdate = new ArrayList<>(priorityUpdatedPolicies.size());
        int i;
        for (i = 0; i < priorityUpdatedPolicies.size(); i++) {
            Policy policyObj = new Policy();
            policyObj.setId(priorityUpdatedPolicies.get(i).getId());
            policyObj.setPriorityId(priorityUpdatedPolicies.get(i).getPriority());
            policiesToUpdate.add(policyObj);
        }
        boolean policiesUpdated;
        try {
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            policiesUpdated = pap.updatePolicyPriorities(policiesToUpdate);
        } catch (PolicyManagementException e) {
            String error = "Exception in updating policy priorities.";
            log.error(error, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(error).build()).build();
        }
        if (policiesUpdated) {
            return Response.status(Response.Status.OK).entity("Policy Priorities successfully "
                    + "updated.").build();

        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Policy priorities did "
                            + "not update. Bad Request.").build()).build();
        }
    }

    @GET
    @Path("/effective-policy/{deviceType}/{deviceId}")
    @Override
    public Response getEffectivePolicy(@PathParam("deviceType") String deviceType, @PathParam("deviceId") String deviceId) {
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        final Policy policy;
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(deviceType);
            policy = policyManagementService.getAppliedPolicyToDevice(deviceIdentifier);
            if (policy == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(
                                "No policy found for device ID '" + deviceId + "'"+ deviceId).build()).build();
            }
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving policy corresponding to the id '" + deviceType + "'"+ deviceId;
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(policy).build();
    }

}
