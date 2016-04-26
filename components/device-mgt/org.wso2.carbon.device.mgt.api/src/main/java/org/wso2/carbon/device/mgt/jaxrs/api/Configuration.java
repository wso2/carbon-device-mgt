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

package org.wso2.carbon.device.mgt.jaxrs.api;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.device.mgt.jaxrs.api.util.DeviceMgtAPIUtils;
import org.wso2.carbon.device.mgt.jaxrs.api.util.MDMAppConstants;
import org.wso2.carbon.device.mgt.jaxrs.api.util.ResponsePayload;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * General Tenant Configuration REST-API implementation.
 * All end points support JSON, XMl with content negotiation.
 */

@SuppressWarnings("NonJaxWsWebServices")
@Produces({"application/json", "application/xml"})
@Consumes({ "application/json", "application/xml" })
public class Configuration {

	private static Log log = LogFactory.getLog(Configuration.class);

	@POST
    public Response saveTenantConfiguration(TenantConfiguration configuration) {
        ResponsePayload responseMsg = new ResponsePayload();
        try {
            DeviceMgtAPIUtils.getTenantConfigurationManagementService().saveConfiguration(configuration,
                                                                                          MDMAppConstants.RegistryConstants.GENERAL_CONFIG_RESOURCE_PATH);
            //Schedule the task service
            DeviceMgtAPIUtils.scheduleTaskService(DeviceMgtAPIUtils.getNotifierFrequency(configuration));
            responseMsg.setMessageFromServer("Tenant configuration saved successfully.");
			responseMsg.setStatusCode(HttpStatus.SC_CREATED);
            return Response.status(Response.Status.CREATED).entity(responseMsg).build();
        } catch (ConfigurationManagementException e) {
            String msg = "Error occurred while saving the tenant configuration.";
			log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

	@GET
    public Response getConfiguration() {
        String msg;
        try {
            TenantConfiguration tenantConfiguration = DeviceMgtAPIUtils.getTenantConfigurationManagementService().
                    getConfiguration(MDMAppConstants.RegistryConstants.GENERAL_CONFIG_RESOURCE_PATH);
			ConfigurationEntry configurationEntry = new ConfigurationEntry();
			configurationEntry.setContentType("text");
			configurationEntry.setName("notifierFrequency");
			configurationEntry.setValue(PolicyManagerUtil.getMonitoringFequency());
			List<ConfigurationEntry> configList = tenantConfiguration.getConfiguration();
			if (configList == null) {
                configList = new ArrayList<>();
            }
            configList.add(configurationEntry);
            tenantConfiguration.setConfiguration(configList);
            return Response.status(Response.Status.OK).entity(tenantConfiguration).build();
        } catch (ConfigurationManagementException e) {
            msg = "Error occurred while retrieving the tenant configuration.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

	@PUT
    public Response updateConfiguration(TenantConfiguration configuration) {
        ResponsePayload responseMsg = new ResponsePayload();
        try {
            DeviceMgtAPIUtils.getTenantConfigurationManagementService().saveConfiguration(configuration,
                                                                                          MDMAppConstants.RegistryConstants.GENERAL_CONFIG_RESOURCE_PATH);
            //Schedule the task service
            DeviceMgtAPIUtils.scheduleTaskService(DeviceMgtAPIUtils.getNotifierFrequency(configuration));
            responseMsg.setMessageFromServer("Tenant configuration updated successfully.");
			responseMsg.setStatusCode(HttpStatus.SC_CREATED);
            return Response.status(Response.Status.CREATED).entity(responseMsg).build();
        } catch (ConfigurationManagementException e) {
            String msg = "Error occurred while updating the tenant configuration.";
			log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}
