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
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceTypeManagementService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.device.mgt.jaxrs.util.MDMAppConstants;
import org.wso2.carbon.device.mgt.jaxrs.util.ResponsePayload;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/device-types")
public class DeviceTypeManagementServiceImpl implements DeviceTypeManagementService {

    private static final Log log = LogFactory.getLog(DeviceTypeManagementServiceImpl.class);

    @GET
    @Override
    public Response getTypes() {
        return null;
    }

    @POST
    @Path("/{type}/configuration")
    @Override
    public Response saveConfiguration(@PathParam("type") String type, PlatformConfiguration config) {
        ResponsePayload responseMsg = new ResponsePayload();
        try {
            DeviceMgtAPIUtils.getPlatformConfigurationManagementService().saveConfiguration(config,
                    MDMAppConstants.RegistryConstants.GENERAL_CONFIG_RESOURCE_PATH);
            //Schedule the task service
            DeviceMgtAPIUtils.scheduleTaskService(DeviceMgtAPIUtils.getNotifierFrequency(config));
            return Response.status(Response.Status.OK).entity("Platform configuration of device type '" + type +
                    "' has successfully been saved").build();
        } catch (ConfigurationManagementException e) {
            String msg = "Error occurred while saving the platform configuration of device type '" + type + "'";
            log.error(msg, e);
            return javax.ws.rs.core.Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/{type}/configuration")
    @Override
    public Response getConfiguration(@PathParam("type") String type) {
        String msg;
        try {
            PlatformConfiguration config = DeviceMgtAPIUtils.getPlatformConfigurationManagementService().
                    getConfiguration(MDMAppConstants.RegistryConstants.GENERAL_CONFIG_RESOURCE_PATH);
            ConfigurationEntry configurationEntry = new ConfigurationEntry();
            configurationEntry.setContentType("text");
            configurationEntry.setName("notifierFrequency");
            configurationEntry.setValue(PolicyManagerUtil.getMonitoringFequency());
            List<ConfigurationEntry> configList = config.getConfiguration();
            if (configList == null) {
                configList = new ArrayList<>();
            }
            configList.add(configurationEntry);
            config.setConfiguration(configList);
            return Response.status(Response.Status.OK).entity(config).build();
        } catch (ConfigurationManagementException e) {
            msg = "Error occurred while retrieving the configuration of device type '" + type + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Path("/{type}/configuration")
    @Override
    public Response updateConfiguration(@PathParam("type") String type, PlatformConfiguration config) {
        ResponsePayload responseMsg = new ResponsePayload();
        try {
            DeviceMgtAPIUtils.getPlatformConfigurationManagementService().saveConfiguration(config,
                    MDMAppConstants.RegistryConstants.GENERAL_CONFIG_RESOURCE_PATH);
            //Schedule the task service
            DeviceMgtAPIUtils.scheduleTaskService(DeviceMgtAPIUtils.getNotifierFrequency(config));
            return Response.status(Response.Status.CREATED).entity("Configuration of device type '" + type +
                    "' has successfully been updated").build();
        } catch (ConfigurationManagementException e) {
            String msg = "Error occurred while updating the configuration of device type '" + type + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}
