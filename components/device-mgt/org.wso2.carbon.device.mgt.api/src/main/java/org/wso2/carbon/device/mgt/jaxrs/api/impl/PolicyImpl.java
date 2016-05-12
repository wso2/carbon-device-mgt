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

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.jaxrs.api.common.MDMAPIException;
import org.wso2.carbon.device.mgt.jaxrs.api.util.DeviceMgtAPIUtils;
import org.wso2.carbon.device.mgt.jaxrs.beans.PriorityUpdatedPolicyWrapper;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtUtil;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.jaxrs.api.util.ResponsePayload;
import org.wso2.carbon.device.mgt.jaxrs.beans.PolicyWrapper;
import org.wso2.carbon.policy.mgt.common.PolicyAdministratorPoint;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.common.PolicyMonitoringTaskException;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceData;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.policy.mgt.core.task.TaskScheduleService;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("NonJaxWsWebServices")
public class PolicyImpl implements org.wso2.carbon.device.mgt.jaxrs.api.Policy {
    private static Log log = LogFactory.getLog(PolicyImpl.class);

    @Override
    @POST
    @Path("inactive-policy")
    public Response addPolicy(PolicyWrapper policyWrapper) {

        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        ResponsePayload responseMsg = new ResponsePayload();
        org.wso2.carbon.policy.mgt.common.Policy policy = new org.wso2.carbon.policy.mgt.common.Policy();
        policy.setPolicyName(policyWrapper.getPolicyName());
        policy.setProfileId(policyWrapper.getProfileId());
	    policy.setDescription(policyWrapper.getDescription());
        policy.setProfile(DeviceMgtUtil.convertProfile(policyWrapper.getProfile()));
        policy.setOwnershipType(policyWrapper.getOwnershipType());
        policy.setRoles(policyWrapper.getRoles());
        policy.setUsers(policyWrapper.getUsers());
        policy.setTenantId(policyWrapper.getTenantId());
        policy.setCompliance(policyWrapper.getCompliance());

        return addPolicy(policyManagementService, responseMsg, policy);
    }

    @Override
    @POST
    @Path("active-policy")
    public Response addActivePolicy(PolicyWrapper policyWrapper) {

        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        ResponsePayload responseMsg = new ResponsePayload();
        org.wso2.carbon.policy.mgt.common.Policy policy = new org.wso2.carbon.policy.mgt.common.Policy();
        policy.setPolicyName(policyWrapper.getPolicyName());
        policy.setProfileId(policyWrapper.getProfileId());
	    policy.setDescription(policyWrapper.getDescription());
        policy.setProfile(DeviceMgtUtil.convertProfile(policyWrapper.getProfile()));
        policy.setOwnershipType(policyWrapper.getOwnershipType());
        policy.setRoles(policyWrapper.getRoles());
        policy.setUsers(policyWrapper.getUsers());
        policy.setTenantId(policyWrapper.getTenantId());
        policy.setCompliance(policyWrapper.getCompliance());
        policy.setActive(true);

        return addPolicy(policyManagementService, responseMsg, policy);
    }

    private Response addPolicy(PolicyManagerService policyManagementService, ResponsePayload responseMsg,
                               org.wso2.carbon.policy.mgt.common.Policy policy) {
        try {
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            pap.addPolicy(policy);
            responseMsg.setStatusCode(HttpStatus.SC_CREATED);
            responseMsg.setMessageFromServer("PolicyImpl has been added successfully.");
            return Response.status(Response.Status.CREATED).entity(responseMsg).build();
        } catch (PolicyManagementException e) {
            String msg = "PolicyImpl Management related exception";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllPolicies() {
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        List<org.wso2.carbon.policy.mgt.common.Policy> policies;
        try {
            PolicyAdministratorPoint policyAdministratorPoint = policyManagementService.getPAP();
            policies = policyAdministratorPoint.getPolicies();
        } catch (PolicyManagementException e) {
            String msg = "PolicyImpl Management related exception";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("Sending all retrieved device policies.");
        responsePayload.setResponseContent(policies);
        return Response.status(Response.Status.OK).entity(responsePayload).build();
    }

    @Override
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("{id}")
    public Response getPolicy(@PathParam("id") int policyId) {
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        final org.wso2.carbon.policy.mgt.common.Policy policy;
        try {
            PolicyAdministratorPoint policyAdministratorPoint = policyManagementService.getPAP();
            policy = policyAdministratorPoint.getPolicy(policyId);
        } catch (PolicyManagementException e) {
            String msg = "PolicyImpl Management related exception";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        if (policy == null){
            ResponsePayload responsePayload = new ResponsePayload();
            responsePayload.setStatusCode(HttpStatus.SC_NOT_FOUND);
            responsePayload.setMessageFromServer("PolicyImpl for ID " + policyId + " not found.");
            return Response.status(Response.Status.NOT_FOUND).entity(responsePayload).build();
        }
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("Sending all retrieved device policies.");
        responsePayload.setResponseContent(policy);
        return Response.status(Response.Status.OK).entity(responsePayload).build();
    }

    @Override
    @GET
    @Path("count")
    public Response getPolicyCount() {
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        try {
            PolicyAdministratorPoint policyAdministratorPoint = policyManagementService.getPAP();
            Integer count = policyAdministratorPoint.getPolicyCount();
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (PolicyManagementException e) {
            String msg = "PolicyImpl Management related exception";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @PUT
    @Path("{id}")
    public Response updatePolicy(PolicyWrapper policyWrapper, @PathParam("id") int policyId) {

        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        ResponsePayload responseMsg = new ResponsePayload();
        org.wso2.carbon.policy.mgt.common.Policy policy = new org.wso2.carbon.policy.mgt.common.Policy();
        policy.setPolicyName(policyWrapper.getPolicyName());
        policy.setId(policyId);
        policy.setProfileId(policyWrapper.getProfileId());
        policy.setDescription(policyWrapper.getDescription());
        policy.setProfile(DeviceMgtUtil.convertProfile(policyWrapper.getProfile()));
        policy.setOwnershipType(policyWrapper.getOwnershipType());
        policy.setRoles(policyWrapper.getRoles());
        policy.setUsers(policyWrapper.getUsers());
        policy.setTenantId(policyWrapper.getTenantId());
        policy.setCompliance(policyWrapper.getCompliance());

        try {
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            pap.updatePolicy(policy);
            responseMsg.setStatusCode(HttpStatus.SC_CREATED);
            responseMsg.setMessageFromServer("PolicyImpl has been updated successfully.");
            return Response.status(Response.Status.CREATED).entity(responseMsg).build();
        } catch (PolicyManagementException e) {
            String msg = "PolicyImpl Management related exception in policy update.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @PUT
    @Path("priorities")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response updatePolicyPriorities(List<PriorityUpdatedPolicyWrapper> priorityUpdatedPolicies) {
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        List<org.wso2.carbon.policy.mgt.common.Policy> policiesToUpdate =
                new ArrayList<>(priorityUpdatedPolicies.size());
        int i;
        for (i = 0; i < priorityUpdatedPolicies.size(); i++) {
            org.wso2.carbon.policy.mgt.common.Policy policyObj = new org.wso2.carbon.policy.mgt.common.Policy();
            policyObj.setId(priorityUpdatedPolicies.get(i).getId());
            policyObj.setPriorityId(priorityUpdatedPolicies.get(i).getPriority());
            policiesToUpdate.add(policyObj);
        }
        boolean policiesUpdated;
        try {
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            policiesUpdated = pap.updatePolicyPriorities(policiesToUpdate);
        } catch (PolicyManagementException e) {
            String msg = "Exception in updating policy priorities.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        ResponsePayload responsePayload = new ResponsePayload();
        if (policiesUpdated) {
            responsePayload.setStatusCode(HttpStatus.SC_OK);
            responsePayload.setMessageFromServer("PolicyImpl Priorities successfully updated.");
            return Response.status(Response.Status.OK).entity(responsePayload).build();
        } else {
            responsePayload.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            responsePayload.setMessageFromServer("PolicyImpl priorities did not update. Bad Request.");
            return Response.status(Response.Status.BAD_REQUEST).entity(responsePayload).build();
        }
    }

    @Override
    @POST
    @Path("bulk-remove")
    @Consumes("application/json")
    @Produces("application/json")
    public Response bulkRemovePolicy(List<Integer> policyIds) {
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        boolean policyDeleted = true;
        try {
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
	        for(int i : policyIds) {
		        org.wso2.carbon.policy.mgt.common.Policy policy = pap.getPolicy(i);
		        if(!pap.deletePolicy(policy)){
			        policyDeleted = false;
		        }
	        }
        } catch (PolicyManagementException e) {
            String msg = "Exception in deleting policies.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        ResponsePayload responsePayload = new ResponsePayload();
        if (policyDeleted) {
            responsePayload.setStatusCode(HttpStatus.SC_OK);
            responsePayload.setMessageFromServer("Policies have been successfully deleted.");
            return Response.status(Response.Status.OK).entity(responsePayload).build();
        } else {
            responsePayload.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            responsePayload.setMessageFromServer("PolicyImpl does not exist.");
            return Response.status(Response.Status.BAD_REQUEST).entity(responsePayload).build();
        }
    }

    @Override
    @PUT
    @Produces("application/json")
    @Path("activate")
    public Response activatePolicy(List<Integer> policyIds) {
        try {
            PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
	        for(int i : policyIds) {
		        pap.activatePolicy(i);
	        }
        } catch (PolicyManagementException e) {
            String msg = "Exception in activating policies.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }

        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("Selected policies have been successfully activated.");
        return Response.status(Response.Status.OK).entity(responsePayload).build();
    }

    @Override
    @PUT
    @Produces("application/json")
    @Path("inactivate")
    public Response inactivatePolicy(List<Integer> policyIds) throws MDMAPIException {

        try {
            PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
	        for(int i : policyIds) {
		        pap.inactivatePolicy(i);
	        }
        } catch (PolicyManagementException e) {
            String msg = "Exception in inactivating policies.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("Selected policies have been successfully inactivated.");
        return Response.status(Response.Status.OK).entity(responsePayload).build();
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
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("Changes have been successfully updated.");
        return Response.status(Response.Status.OK).entity(responsePayload).build();
    }

    @Override
    @GET
    @Path("start-task/{milliseconds}")
    public Response startTaskService(@PathParam("milliseconds") int monitoringFrequency) {

        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        try {
            TaskScheduleService taskScheduleService = policyManagementService.getTaskScheduleService();
            taskScheduleService.startTask(monitoringFrequency);


        } catch (PolicyMonitoringTaskException e) {
            String msg = "PolicyImpl Management related exception.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("PolicyImpl monitoring service started successfully.");
        return Response.status(Response.Status.OK).entity(responsePayload).build();
    }

    @Override
    @GET
    @Path("update-task/{milliseconds}")
    public Response updateTaskService(@PathParam("milliseconds") int monitoringFrequency) {

        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        try {
            TaskScheduleService taskScheduleService = policyManagementService.getTaskScheduleService();
            taskScheduleService.updateTask(monitoringFrequency);

        } catch (PolicyMonitoringTaskException e) {
            String msg = "PolicyImpl Management related exception.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("PolicyImpl monitoring service updated successfully.");
        return Response.status(Response.Status.OK).entity(responsePayload).build();
    }

    @Override
    @GET
    @Path("stop-task")
    public Response stopTaskService() {

        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        try {
            TaskScheduleService taskScheduleService = policyManagementService.getTaskScheduleService();
            taskScheduleService.stopTask();

        } catch (PolicyMonitoringTaskException e) {
            String msg = "PolicyImpl Management related exception.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("PolicyImpl monitoring service stopped successfully.");
        return Response.status(Response.Status.OK).entity(responsePayload).build();
    }

    @Override
    @GET
    @Path("{type}/{id}")
    public Response getComplianceDataOfDevice(@PathParam("type") String type, @PathParam("id") String id) {
        try {
            DeviceIdentifier deviceIdentifier = DeviceMgtAPIUtils.instantiateDeviceIdentifier(type, id);
            PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
            ComplianceData complianceData = policyManagementService.getDeviceCompliance(deviceIdentifier);
            return Response.status(Response.Status.OK).entity(complianceData).build();
        } catch (PolicyComplianceException e) {
            String msg = "Error occurred while getting the compliance data.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

	@Override
    @GET
	@Path("{type}/{id}/active-policy")
    public Response getDeviceActivePolicy(@PathParam("type") String type, @PathParam("id") String id) {
        try {
            DeviceIdentifier deviceIdentifier = DeviceMgtAPIUtils.instantiateDeviceIdentifier(type, id);
            PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
            org.wso2.carbon.policy.mgt.common.Policy policy = policyManagementService
                    .getAppliedPolicyToDevice(deviceIdentifier);
            return Response.status(Response.Status.OK).entity(policy).build();
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while getting the current policy.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
