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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.AbstractManagerService;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyAdministratorPoint;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.common.PolicyMonitoringTaskException;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceData;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.policy.mgt.core.task.TaskScheduleService;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@WebService
public class PolicyManagementService extends AbstractManagerService {

	private static final Log log = LogFactory.getLog(PolicyManagementService.class);

	@POST
	@Path("/policies/inactive")
	@Produces("application/json")
	public Response addInactivePolicy(@FormParam("policy") Policy policy) {
		return addPolicy(policy);
	}

	@POST
	@Path("/policies/active")
	@Produces("application/json")
	public Response addActivePolicy(@FormParam("policy") Policy policy) {
		policy.setActive(true);
		return addPolicy(policy);
	}

	private Response addPolicy(Policy policy) {
		try {
			PolicyManagerService policyManagerService = getServiceProvider(PolicyManagerService.class);
			PolicyAdministratorPoint pap = policyManagerService.getPAP();
			pap.addPolicy(policy);
			if (log.isDebugEnabled()) {
				log.debug("Policy has been added successfully.");
			}
			return Response.status(Response.Status.NO_CONTENT).build();
		} catch (PolicyManagementException e) {
			String error = "Policy Management related exception.";
			log.error(error, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			this.endTenantFlow();
		}
	}

	@GET
	@Path("/policies")
	@Produces("application/json")
	public Response getAllPolicies() {

		try {
			PolicyManagerService policyManagerService = getServiceProvider(PolicyManagerService.class);
			PolicyAdministratorPoint policyAdministratorPoint = policyManagerService.getPAP();
			List<Policy> policies = policyAdministratorPoint.getPolicies();
			Policy[] policyArr = policyAdministratorPoint.getPolicies().toArray(new Policy[policies.size()]);
			return Response.status(Response.Status.OK).entity(policyArr).build();
		} catch (PolicyManagementException e) {
			String error = "Policy Management related exception";
			log.error(error, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			this.endTenantFlow();
		}
	}

	@GET
	@Path("/policies/{policyId}")
	@Produces("application/json")
	public Response getPolicy(@PathParam("policyId") int policyId) {

		try {
			PolicyManagerService policyManagerService = getServiceProvider(PolicyManagerService.class);
			PolicyAdministratorPoint policyAdministratorPoint = policyManagerService.getPAP();
			Policy policy = policyAdministratorPoint.getPolicy(policyId);
			if (policy != null) {
				if (log.isDebugEnabled()) {
					log.debug("Sending policy for ID " + policyId);
				}
				return Response.status(Response.Status.OK).entity(policy).build();
			} else {
				log.error("Policy for ID " + policyId + " not found.");
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (PolicyManagementException e) {
			String error = "Policy Management related exception";
			log.error(error, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			this.endTenantFlow();
		}
	}

	@GET
	@Path("/policies/inactive/count")
	@Produces("application/json")
	public Response getPolicyCount() {

		try {
			PolicyManagerService policyManagerService = getServiceProvider(PolicyManagerService.class);
			PolicyAdministratorPoint policyAdministratorPoint = policyManagerService.getPAP();
			int policyCount = policyAdministratorPoint.getPolicyCount();
			return Response.status(Response.Status.OK).entity(policyCount).build();
		} catch (PolicyManagementException e) {
			String error = "Policy Management related exception";
			log.error(error, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			this.endTenantFlow();
		}
	}

	@PUT
	@Path("/policies/{policyId}")
	@Produces("application/json")
	public Response updatePolicy(@FormParam("policy") Policy policy, @PathParam("policyId") int policyId) {

		try {
			PolicyManagerService policyManagerService = getServiceProvider(PolicyManagerService.class);
			PolicyAdministratorPoint pap = policyManagerService.getPAP();
			Policy previousPolicy = pap.getPolicy(policyId);
			policy.setProfile(pap.getProfile(previousPolicy.getProfileId()));
			policy.setPolicyName(previousPolicy.getPolicyName());
			pap.updatePolicy(policy);
			if (log.isDebugEnabled()) {
				log.debug("Policy with ID " + policyId + " has been updated successfully.");
			}
			return Response.status(Response.Status.NO_CONTENT).build();
		} catch (PolicyManagementException e) {
			String error = "Policy Management related exception";
			log.error(error, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			this.endTenantFlow();
		}
	}

	@PUT
	@Path("/policies/priorities")
	@Consumes("application/json")
	@Produces("application/json")
	public Response updatePolicyPriorities(@FormParam("policies") List<Policy> priorityUpdatedPolicies) {

		try {
			PolicyManagerService policyManagerService = getServiceProvider(PolicyManagerService.class);
			PolicyAdministratorPoint pap = policyManagerService.getPAP();
			boolean policiesUpdated = pap.updatePolicyPriorities(priorityUpdatedPolicies);
			if (policiesUpdated) {
				if (log.isDebugEnabled()) {
					log.debug("Policy Priorities successfully updated.");
				}
				return Response.status(Response.Status.NO_CONTENT).build();
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Policy priorities did not update. Bad Request.");
				}
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}
		} catch (PolicyManagementException e) {
			String error = "Exception in updating policy priorities.";
			log.error(error, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			this.endTenantFlow();
		}
	}

	@DELETE
	@Path("/policies/{policyId}")
	@Produces("application/json")
	public Response deletePolicy(@PathParam("policyId") int policyId) {

		try {
			PolicyManagerService policyManagerService = getServiceProvider(PolicyManagerService.class);
			PolicyAdministratorPoint pap = policyManagerService.getPAP();
			Policy policy = pap.getPolicy(policyId);
			boolean policyDeleted = pap.deletePolicy(policy);
			if (policyDeleted) {
				if (log.isDebugEnabled()) {
					log.debug("Policy by id:" + policyId + " has been successfully deleted.");
				}
				return Response.status(Response.Status.OK).build();
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Policy by id:" + policyId + " does not exist.");
				}
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (PolicyManagementException e) {
			String error = "Exception in deleting policy by id:" + policyId;
			log.error(error, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			this.endTenantFlow();
		}
	}

	@PUT
	@Path("/policies/{policyId}/activate")
	@Produces("application/json")
	public Response activatePolicy(@PathParam("policyId") int policyId) {

		try {
			PolicyManagerService policyManagerService = getServiceProvider(PolicyManagerService.class);
			PolicyAdministratorPoint pap = policyManagerService.getPAP();
			pap.activatePolicy(policyId);
			if (log.isDebugEnabled()) {
				log.debug("Policy by id:" + policyId + " has been successfully activated.");
			}
			return Response.status(Response.Status.NO_CONTENT).build();
		} catch (PolicyManagementException e) {
			String error = "Exception in activating policy by id:" + policyId;
			log.error(error, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			this.endTenantFlow();
		}
	}

	@PUT
	@Path("/policies/{policyId}/inactivate")
	@Produces("application/json")
	public Response inactivatePolicy(@PathParam("policyId") int policyId) {

		try {
			PolicyManagerService policyManagerService = getServiceProvider(PolicyManagerService.class);
			PolicyAdministratorPoint pap = policyManagerService.getPAP();
			pap.inactivatePolicy(policyId);
			if (log.isDebugEnabled()) {
				log.debug("Policy by id:" + policyId + " has been successfully inactivated.");
			}
			return Response.status(Response.Status.NO_CONTENT).build();
		} catch (PolicyManagementException e) {
			String error = "Exception in inactivating policy by id:" + policyId;
			log.error(error, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			this.endTenantFlow();
		}
	}

	@PUT
	@Path("/policies/publish-changes")
	@Produces("application/json")
	public Response publishChanges() {

		try {
			PolicyManagerService policyManagerService = getServiceProvider(PolicyManagerService.class);
			PolicyAdministratorPoint pap = policyManagerService.getPAP();
			pap.publishChanges();
			if (log.isDebugEnabled()) {
				log.debug("Changes have been successfully updated.");
			}
			return Response.status(Response.Status.NO_CONTENT).build();
		} catch (PolicyManagementException e) {
			String error = "Exception in applying changes.";
			log.error(error, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			this.endTenantFlow();
		}
	}

	@PUT
	@Path("/policies/monitor-task/start")
	@Produces("application/json")
	public Response startTaskService(@FormParam("milliseconds") int monitoringFrequency) {

		try {
			PolicyManagerService policyManagerService = getServiceProvider(PolicyManagerService.class);
			TaskScheduleService taskScheduleService = policyManagerService.getTaskScheduleService();
			taskScheduleService.startTask(monitoringFrequency);
			if (log.isDebugEnabled()) {
				log.debug("Policy monitoring service started successfully.");
			}
			return Response.status(Response.Status.NO_CONTENT).build();
		} catch (PolicyMonitoringTaskException e) {
			String error = "Policy Management related exception.";
			log.error(error, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			this.endTenantFlow();
		}
	}

	@PUT
	@Path("/policies/monitor-task/update")
	@Produces("application/json")
	public Response updateTaskService(@FormParam("milliseconds") int monitoringFrequency) {

		try {
			PolicyManagerService policyManagerService = getServiceProvider(PolicyManagerService.class);
			TaskScheduleService taskScheduleService = policyManagerService.getTaskScheduleService();
			taskScheduleService.updateTask(monitoringFrequency);
			if (log.isDebugEnabled()) {
				log.debug("Policy monitoring service updated successfully.");
			}
			return Response.status(Response.Status.NO_CONTENT).build();
		} catch (PolicyMonitoringTaskException e) {
			String error = "Policy Management related exception.";
			log.error(error, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			this.endTenantFlow();
		}
	}

	@PUT
	@Path("/policies/monitor-task/stop")
	@Produces("application/json")
	public Response stopTaskService() {

		try {
			PolicyManagerService policyManagerService = getServiceProvider(PolicyManagerService.class);
			TaskScheduleService taskScheduleService = policyManagerService.getTaskScheduleService();
			taskScheduleService.stopTask();
			if (log.isDebugEnabled()) {
				log.debug("Policy monitoring service stopped successfully.");
			}
			return Response.status(Response.Status.NO_CONTENT).build();
		} catch (PolicyMonitoringTaskException e) {
			String error = "Policy Management related exception.";
			log.error(error, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			this.endTenantFlow();
		}
	}

	@GET
	@Path("/policies/compliance/{deviceType}/{deviceId}")
	@Produces("application/json")
	public Response getComplianceDataOfDevice(@PathParam("deviceId") String deviceId,
											  @PathParam("deviceType") String deviceType) {
		try {
			PolicyManagerService policyManagerService = getServiceProvider(PolicyManagerService.class);
			DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
			deviceIdentifier.setType(deviceType);
			deviceIdentifier.setId(deviceId);
			ComplianceData complianceData = policyManagerService.getDeviceCompliance(deviceIdentifier);
			return Response.status(Response.Status.OK).entity(complianceData).build();
		} catch (PolicyComplianceException e) {
			String error = "Error occurred while getting the compliance data.";
			log.error(error, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			this.endTenantFlow();
		}
	}

}
