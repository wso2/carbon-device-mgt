/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.device.mgt.common.impl.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyAdministratorPoint;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.common.PolicyMonitoringTaskException;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceData;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.policy.mgt.core.task.TaskScheduleService;

import javax.jws.WebService;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.List;

@WebService
public class PolicyManagementService {

	private static Log log = LogFactory.getLog(PolicyManagementService.class);

	@Context  //injected response proxy supporting multiple thread
	private HttpServletResponse response;

	private PrivilegedCarbonContext ctx;

	private PolicyManagerService getPolicyServiceProvider() throws DeviceManagementException {
		String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
		PrivilegedCarbonContext.startTenantFlow();
		ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		ctx.setTenantDomain(tenantDomain, true);

		if (log.isDebugEnabled()) {
			log.debug("Getting thread local carbon context for tenant domain: " + tenantDomain);
		}

		PolicyManagerService policyManagerService = (PolicyManagerService) ctx.getOSGiService(
				PolicyManagerService.class, null);

		if (policyManagerService == null) {
			String msg = "Policy Management service not initialized";
			log.error(msg);
			throw new DeviceManagementException(msg);
		}

		return policyManagerService;
	}

	private void endTenantFlow() {
		PrivilegedCarbonContext.endTenantFlow();
		ctx = null;
		if (log.isDebugEnabled()) {
			log.debug("Tenant flow ended");
		}
	}

	@POST
	@Path("/inactive-policy")
	@Produces("application/json")
	public boolean addPolicy(Policy policy) {

		try {
			PolicyManagerService policyManagerService = getPolicyServiceProvider();
			PolicyAdministratorPoint pap = policyManagerService.getPAP();
			pap.addPolicy(policy);
			response.setStatus(Response.Status.CREATED.getStatusCode());
			if (log.isDebugEnabled()) {
				log.debug("Policy has been added successfully.");
			}
			return true;
		} catch (PolicyManagementException e) {
			String error = "Policy Management related exception.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} catch (DeviceManagementException e) {
			String error = "Error occurred while invoking Policy Management Service.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} finally {
			this.endTenantFlow();
		}
	}

	@POST
	@Path("/active-policy")
	@Produces("application/json")
	public boolean addActivePolicy(Policy policy) {

		policy.setActive(true);

		try {
			PolicyManagerService policyManagerService = getPolicyServiceProvider();
			PolicyAdministratorPoint pap = policyManagerService.getPAP();
			pap.addPolicy(policy);
			response.setStatus(Response.Status.CREATED.getStatusCode());
			if (log.isDebugEnabled()) {
				log.debug("Policy has been added successfully.");
			}
			return true;
		} catch (PolicyManagementException e) {
			String error = "Policy Management related exception.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} catch (DeviceManagementException e) {
			String error = "Error occurred while invoking Policy Management Service.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} finally {
			this.endTenantFlow();
		}
	}

	@GET
	@Produces("application/json")
	public Policy[] getAllPolicies() {
		try {
			PolicyManagerService policyManagerService = getPolicyServiceProvider();
			PolicyAdministratorPoint policyAdministratorPoint = policyManagerService.getPAP();
			List<Policy> policies = policyAdministratorPoint.getPolicies();
			return policyAdministratorPoint.getPolicies().toArray(new Policy[policies.size()]);
		} catch (PolicyManagementException e) {
			String error = "Policy Management related exception";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return null;
		} catch (DeviceManagementException e) {
			String error = "Error occurred while invoking Policy Management Service.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return null;
		} finally {
			this.endTenantFlow();
		}
	}

	@GET
	@Produces("application/json")
	@Path("/{id}")
	public Policy getPolicy(@PathParam("id") int policyId) {
		try {
			PolicyManagerService policyManagerService = getPolicyServiceProvider();
			PolicyAdministratorPoint policyAdministratorPoint = policyManagerService.getPAP();
			Policy policy = policyAdministratorPoint.getPolicy(policyId);
			if (policy != null) {
				if (log.isDebugEnabled()) {
					log.debug("Sending policy for ID " + policyId);
				}
				return policy;
			} else {
				log.error("Policy for ID " + policyId + " not found.");
				response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
				return null;
			}
		} catch (PolicyManagementException e) {
			String error = "Policy Management related exception";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return null;
		} catch (DeviceManagementException e) {
			String error = "Error occurred while invoking Policy Management Service.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return null;
		} finally {
			this.endTenantFlow();
		}
	}

	@GET
	@Path("/count")
	public int getPolicyCount() {
		try {
			PolicyManagerService policyManagerService = getPolicyServiceProvider();
			PolicyAdministratorPoint policyAdministratorPoint = policyManagerService.getPAP();
			return policyAdministratorPoint.getPolicyCount();
		} catch (PolicyManagementException e) {
			String error = "Policy Management related exception";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return -1;
		} catch (DeviceManagementException e) {
			String error = "Error occurred while invoking Policy Management Service.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return -1;
		} finally {
			this.endTenantFlow();
		}
	}

	@PUT
	@Path("/{id}")
	@Produces("application/json")
	public boolean updatePolicy(Policy policy, @PathParam("id") int policyId) {
		try {
			PolicyManagerService policyManagerService = getPolicyServiceProvider();
			PolicyAdministratorPoint pap = policyManagerService.getPAP();
			org.wso2.carbon.policy.mgt.common.Policy previousPolicy = pap.getPolicy(policyId);
			policy.setProfile(pap.getProfile(previousPolicy.getProfileId()));
			policy.setPolicyName(previousPolicy.getPolicyName());
			pap.updatePolicy(policy);
			if (log.isDebugEnabled()) {
				log.debug("Policy with ID " + policyId + " has been updated successfully.");
			}
			return true;
		} catch (PolicyManagementException e) {
			String error = "Policy Management related exception";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} catch (DeviceManagementException e) {
			String error = "Error occurred while invoking Policy Management Service.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} finally {
			this.endTenantFlow();
		}
	}

	@PUT
	@Path("/priorities")
	@Consumes("application/json")
	@Produces("application/json")
	public boolean updatePolicyPriorities(List<Policy> priorityUpdatedPolicies) {
		try {
			PolicyManagerService policyManagerService = getPolicyServiceProvider();
			PolicyAdministratorPoint pap = policyManagerService.getPAP();
			boolean policiesUpdated = pap.updatePolicyPriorities(priorityUpdatedPolicies);
			if (policiesUpdated) {
				if (log.isDebugEnabled()) {
					log.debug("Policy Priorities successfully updated.");
				}
				return true;
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Policy priorities did not update. Bad Request.");
				}
				response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
				return false;
			}
		} catch (PolicyManagementException e) {
			String error = "Exception in updating policy priorities.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} catch (DeviceManagementException e) {
			String error = "Error occurred while invoking Policy Management Service.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} finally {
			this.endTenantFlow();
		}
	}

	@DELETE
	@Path("/{id}")
	@Produces("application/json")
	public boolean deletePolicy(@PathParam("id") int policyId) {
		try {
			PolicyManagerService policyManagerService = getPolicyServiceProvider();
			PolicyAdministratorPoint pap = policyManagerService.getPAP();
			org.wso2.carbon.policy.mgt.common.Policy policy = pap.getPolicy(policyId);
			boolean policyDeleted = pap.deletePolicy(policy);
			if (policyDeleted) {
				if (log.isDebugEnabled()) {
					log.debug("Policy by id:" + policyId + " has been successfully deleted.");
				}
				return true;
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Policy by id:" + policyId + " does not exist.");
				}
				response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
				return false;
			}
		} catch (PolicyManagementException e) {
			String error = "Exception in deleting policy by id:" + policyId;
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} catch (DeviceManagementException e) {
			String error = "Error occurred while invoking Policy Management Service.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} finally {
			this.endTenantFlow();
		}
	}

	@PUT
	@Produces("application/json")
	@Path("/activate/{id}")
	public boolean activatePolicy(@PathParam("id") int policyId) {
		try {
			PolicyManagerService policyManagerService = getPolicyServiceProvider();
			PolicyAdministratorPoint pap = policyManagerService.getPAP();
			pap.activatePolicy(policyId);
			if (log.isDebugEnabled()) {
				log.debug("Policy by id:" + policyId + " has been successfully activated.");
			}
			return true;
		} catch (PolicyManagementException e) {
			String error = "Exception in activating policy by id:" + policyId;
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} catch (DeviceManagementException e) {
			String error = "Error occurred while invoking Policy Management Service.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} finally {
			this.endTenantFlow();
		}
	}

	@PUT
	@Produces("application/json")
	@Path("/inactivate/{id}")
	public boolean inactivatePolicy(@PathParam("id") int policyId) {

		try {
			PolicyManagerService policyManagerService = getPolicyServiceProvider();
			PolicyAdministratorPoint pap = policyManagerService.getPAP();
			pap.inactivatePolicy(policyId);
			if (log.isDebugEnabled()) {
				log.debug("Policy by id:" + policyId + " has been successfully inactivated.");
			}
			return true;
		} catch (PolicyManagementException e) {
			String error = "Exception in inactivating policy by id:" + policyId;
			log.error(error, e);
			return false;
		} catch (DeviceManagementException e) {
			String error = "Error occurred while invoking Policy Management Service.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} finally {
			this.endTenantFlow();
		}
	}

	@PUT
	@Produces("application/json")
	@Path("/apply-changes")
	public boolean applyChanges() {

		try {
			PolicyManagerService policyManagerService = getPolicyServiceProvider();
			PolicyAdministratorPoint pap = policyManagerService.getPAP();
			pap.publishChanges();
			if (log.isDebugEnabled()) {
				log.debug("Changes have been successfully updated.");
			}
			return true;
		} catch (PolicyManagementException e) {
			String error = "Exception in applying changes.";
			log.error(error, e);
			return false;
		} catch (DeviceManagementException e) {
			String error = "Error occurred while invoking Policy Management Service.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} finally {
			this.endTenantFlow();
		}
	}

	@GET
	@Path("/start-task/{milliseconds}")
	public boolean startTaskService(@PathParam("milliseconds") int monitoringFrequency) {

		try {
			PolicyManagerService policyManagerService = getPolicyServiceProvider();
			TaskScheduleService taskScheduleService = policyManagerService.getTaskScheduleService();
			taskScheduleService.startTask(monitoringFrequency);
			if (log.isDebugEnabled()) {
				log.debug("Policy monitoring service started successfully.");
			}
			return true;
		} catch (PolicyMonitoringTaskException e) {
			String error = "Policy Management related exception.";
			log.error(error, e);
			return false;
		} catch (DeviceManagementException e) {
			String error = "Error occurred while invoking Policy Management Service.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} finally {
			this.endTenantFlow();
		}
	}

	@GET
	@Path("/update-task/{milliseconds}")
	public boolean updateTaskService(@PathParam("milliseconds") int monitoringFrequency) {

		try {
			PolicyManagerService policyManagerService = getPolicyServiceProvider();
			TaskScheduleService taskScheduleService = policyManagerService.getTaskScheduleService();
			taskScheduleService.updateTask(monitoringFrequency);
			if (log.isDebugEnabled()) {
				log.debug("Policy monitoring service updated successfully.");
			}
			return true;
		} catch (PolicyMonitoringTaskException e) {
			String error = "Policy Management related exception.";
			log.error(error, e);
			return false;
		} catch (DeviceManagementException e) {
			String error = "Error occurred while invoking Policy Management Service.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} finally {
			this.endTenantFlow();
		}
	}

	@GET
	@Path("/stop-task")
	public boolean stopTaskService() {

		try {
			PolicyManagerService policyManagerService = getPolicyServiceProvider();
			TaskScheduleService taskScheduleService = policyManagerService.getTaskScheduleService();
			taskScheduleService.stopTask();
			if (log.isDebugEnabled()) {
				log.debug("Policy monitoring service stopped successfully.");
			}
			return true;
		} catch (PolicyMonitoringTaskException e) {
			String error = "Policy Management related exception.";
			log.error(error, e);
			return false;
		} catch (DeviceManagementException e) {
			String error = "Error occurred while invoking Policy Management Service.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		} finally {
			this.endTenantFlow();
		}
	}

	@GET
	@Path("/{type}/{id}")
	public ComplianceData getComplianceDataOfDevice(@PathParam("id") String deviceId,
											 @PathParam("type") String deviceType) {
		try {
			PolicyManagerService policyManagerService = getPolicyServiceProvider();
			DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
			deviceIdentifier.setType(deviceType);
			deviceIdentifier.setId(deviceId);
			return policyManagerService.getDeviceCompliance(deviceIdentifier);
		} catch (PolicyComplianceException e) {
			String error = "Error occurred while getting the compliance data.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return null;
		} catch (DeviceManagementException e) {
			String error = "Error occurred while invoking Policy Management Service.";
			log.error(error, e);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return null;
		} finally {
			this.endTenantFlow();
		}
	}

}
