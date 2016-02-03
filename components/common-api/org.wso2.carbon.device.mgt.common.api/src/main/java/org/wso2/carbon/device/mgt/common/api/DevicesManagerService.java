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
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.common.AbstractManagerService;
import org.wso2.carbon.device.mgt.common.impl.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.common.impl.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.common.impl.sensormgt.SensorRecord;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@WebService
public class DevicesManagerService extends AbstractManagerService {

    private static final Log log = LogFactory.getLog(DevicesManagerService.class);

    private Device[] getActiveDevices(List<Device> devices) {
        List<Device> activeDevices = new ArrayList<>();
        if (devices != null) {
            for (Device device : devices) {
                if (device.getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.ACTIVE)) {
                    activeDevices.add(device);
                }
            }
        }
        return activeDevices.toArray(new Device[activeDevices.size()]);
    }

    @Path("/devices/users/{userName}")
    @GET
    @Produces("application/json")
    public Response getDevicesOfUser(@PathParam("userName") String userName) {
        try {
            List<Device> devices = this.getServiceProvider(DeviceManagementProviderService.class)
                    .getDevicesOfUser(userName);
            Device[] devicesArr = this.getActiveDevices(devices);
            return Response.status(Response.Status.OK).entity(devicesArr).build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/users/{userName}/ungrouped")
    @GET
    @Produces("application/json")
    public Response getUnGroupedDevices(@PathParam("userName") String userName) {
        try {
            List<Device> devices = this.getServiceProvider(DeviceManagementProviderService.class)
                    .getUnGroupedDevices(userName);
            Device[] devicesArr = this.getActiveDevices(devices);
            return Response.status(Response.Status.OK).entity(devicesArr).build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/users/{userName}/count")
    @GET
    @Produces("application/json")
    public Response getDeviceCount(@PathParam("userName") String userName) {
        try {
            List<Device> devices = this.getServiceProvider(DeviceManagementProviderService.class)
                    .getDevicesOfUser(userName);
            if (devices != null) {
                List<Device> activeDevices = new ArrayList<>();
                for (Device device : devices) {
                    if (device.getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.ACTIVE)) {
                        activeDevices.add(device);
                    }
                }
                return Response.status(Response.Status.OK).entity(activeDevices.size()).build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/{deviceType}/{identifier}")
    @GET
    @Produces("application/json")
    public Response getDevice(@PathParam("deviceType") String deviceType,
                              @PathParam("identifier") String identifier) {

        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(identifier);
            deviceIdentifier.setType(deviceType);
            Device device = this.getServiceProvider(DeviceManagementProviderService.class).getDevice(
                    deviceIdentifier);
            if (device != null) {
                return Response.status(Response.Status.OK).entity(device).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/types")
    @GET
    @Produces("application/json")
    public Response getDeviceTypes() {
        try {
            List<DeviceType> deviceTypes = this.getServiceProvider(DeviceManagementProviderService.class)
                    .getAvailableDeviceTypes();
            DeviceType[] deviceTypesArr = deviceTypes.toArray(new DeviceType[deviceTypes.size()]);
            return Response.status(Response.Status.OK).entity(deviceTypesArr).build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/{deviceType}")
    @GET
    @Produces("application/json")
    public Response getAllDevices(@PathParam("deviceType") String deviceType) {
        try {
            List<Device> devices = this.getServiceProvider(DeviceManagementProviderService.class)
                    .getAllDevices(deviceType);
            Device[] devicesArr = this.getActiveDevices(devices);
            return Response.status(Response.Status.OK).entity(devicesArr).build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices")
    @GET
    @Produces("application/json")
    public Response getAllDevices() {
        try {
            List<Device> devices = this.getServiceProvider(DeviceManagementProviderService.class)
                    .getAllDevices();
            return Response.status(Response.Status.OK).entity(this.getActiveDevices(devices)).build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/enrollments/invite")
    @POST
    @Produces("application/json")
    public Response sendEnrolmentInvitation(@FormParam("messageBody") String messageBody,
                                            @FormParam("mailTo") String[] mailTo,
                                            @FormParam("ccList") String[] ccList,
                                            @FormParam("bccList") String[] bccList,
                                            @FormParam("subject") String subject,
                                            @FormParam("firstName") String firstName,
                                            @FormParam("enrolmentUrl") String enrolmentUrl,
                                            @FormParam("title") String title,
                                            @FormParam("password") String password,
                                            @FormParam("userName") String userName) {
        EmailMessageProperties config = new EmailMessageProperties();
        config.setMessageBody(messageBody);
        config.setMailTo(mailTo);
        config.setCcList(ccList);
        config.setBccList(bccList);
        config.setSubject(subject);
        config.setFirstName(firstName);
        config.setEnrolmentUrl(enrolmentUrl);
        config.setTitle(title);
        config.setUserName(userName);
        config.setPassword(password);
        try {
            this.getServiceProvider(DeviceManagementProviderService.class).sendEnrolmentInvitation(config);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/registrations/invite")
    @POST
    @Produces("application/json")
    public Response sendRegistrationEmail(@FormParam("messageBody") String messageBody,
                                          @FormParam("mailTo") String[] mailTo,
                                          @FormParam("ccList") String[] ccList,
                                          @FormParam("bccList") String[] bccList,
                                          @FormParam("subject") String subject,
                                          @FormParam("firstName") String firstName,
                                          @FormParam("enrolmentUrl") String enrolmentUrl,
                                          @FormParam("title") String title,
                                          @FormParam("password") String password,
                                          @FormParam("userName") String userName) {
        EmailMessageProperties config = new EmailMessageProperties();
        config.setMessageBody(messageBody);
        config.setMailTo(mailTo);
        config.setCcList(ccList);
        config.setBccList(bccList);
        config.setSubject(subject);
        config.setFirstName(firstName);
        config.setEnrolmentUrl(enrolmentUrl);
        config.setTitle(title);
        config.setUserName(userName);
        config.setPassword(password);
        try {
            this.getServiceProvider(DeviceManagementProviderService.class).sendRegistrationEmail(config);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/{deviceType}/config")
    @GET
    @Produces("application/json")
    public Response getConfiguration(@PathParam("deviceType") String deviceType) {
        try {
            TenantConfiguration tenantConfiguration = this.getServiceProvider(
                    DeviceManagementProviderService.class).getConfiguration(deviceType);
            if (tenantConfiguration != null) {
                return Response.status(Response.Status.OK).entity(tenantConfiguration).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/search/role")
    @GET
    @Produces("application/json")
    public Response getAllDevicesOfRole(@QueryParam("roleName") String roleName) {
        try {
            List<Device> devices = this.getServiceProvider(DeviceManagementProviderService.class)
                    .getAllDevicesOfRole(roleName);
            Device[] devicesArr = this.getActiveDevices(devices);
            return Response.status(Response.Status.OK).entity(devicesArr).build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/search/name")
    @GET
    @Produces("application/json")
    public Response getDevicesByName(@PathParam("deviceName") String deviceName) {
        try {
            List<Device> devices = this.getServiceProvider(DeviceManagementProviderService.class)
                    .getDevicesByName(deviceName);
            Device[] devicesArr = this.getActiveDevices(devices);
            return Response.status(Response.Status.OK).entity(devicesArr).build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/{deviceType}/{identifier}/status")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateDeviceEnrolmentInfo(@PathParam("deviceType") String deviceType,
                                              @PathParam("identifier") String identifier,
                                              @FormParam("status") EnrolmentInfo.Status status) {
        try {
            DeviceManagementProviderService providerService = this.getServiceProvider(
                    DeviceManagementProviderService.class);
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setType(deviceType);
            deviceIdentifier.setId(identifier);
            Device device = providerService.getDevice(deviceIdentifier);
            providerService.updateDeviceEnrolmentInfo(device, status);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/search/status")
    @GET
    @Produces("application/json")
    public Response getDevicesByStatus(@QueryParam("status") EnrolmentInfo.Status status) {
        try {
            List<Device> devices = this.getServiceProvider(DeviceManagementProviderService.class)
                    .getDevicesByStatus(status);
            Device[] devicesArr = this.getActiveDevices(devices);
            return Response.status(Response.Status.OK).entity(devicesArr).build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/{deviceType}/license")
    @GET
    @Produces("application/json")
    public Response getLicense(@PathParam("deviceType") String deviceType,
                               @QueryParam("languageCode") String languageCode) {
        try {
            License license = this.getServiceProvider(DeviceManagementProviderService.class).getLicense(
                    deviceType, languageCode);
            if (license != null) {
                return Response.status(Response.Status.OK).entity(license).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/{deviceType}/license")
    @POST
    @Produces("application/json")
    public Response addLicense(@PathParam("deviceType") String deviceType,
                               @FormParam("provider") String provider, @FormParam("name") String name,
                               @FormParam("version") String version, @FormParam("language") String language,
                               @FormParam("validFrom") Date validFrom, @FormParam("validTo") Date validTo,
                               @FormParam("text") String text) {
        try {
            License license = new License();
            license.setProvider(provider);
            license.setName(name);
            license.setVersion(version);
            license.setLanguage(language);
            license.setValidFrom(validFrom);
            license.setValidTo(validTo);
            license.setText(text);
            this.getServiceProvider(DeviceManagementProviderService.class).addLicense(deviceType, license);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/{deviceType}/{identifier}/enrollment")
    @PUT
    @Produces("application/json")
    public Response modifyEnrollment(@PathParam("deviceType") String deviceType,
                                     @PathParam("identifier") String identifier,
                                     @FormParam("name") String name,
                                     @FormParam("description") String description,
                                     @FormParam("groupId") int groupId,
                                     @FormParam("enrollmentId") int enrollmentId,
                                     @FormParam("dateOfEnrolment") long dateOfEnrolment,
                                     @FormParam("dateOfLastUpdate") long dateOfLastUpdate,
                                     @FormParam("ownership") EnrolmentInfo.OwnerShip ownership,
                                     @FormParam("status") EnrolmentInfo.Status status,
                                     @FormParam("owner") String owner) {

        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setId(enrollmentId);
        enrolmentInfo.setDateOfEnrolment(dateOfEnrolment);
        enrolmentInfo.setDateOfLastUpdate(dateOfLastUpdate);
        enrolmentInfo.setOwnership(ownership);
        enrolmentInfo.setStatus(status);
        enrolmentInfo.setOwner(owner);

        Device device = new Device();
        device.setType(deviceType);
        device.setDeviceIdentifier(identifier);
        device.setName(name);
        device.setDescription(description);
        device.setGroupId(groupId);
        device.setEnrolmentInfo(enrolmentInfo);
        try {
            boolean isModified = this.getServiceProvider(DeviceManagementProviderService.class)
                    .modifyEnrollment(device);
            if (isModified) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/{deviceType}/{identifier}/enrollment")
    @POST
    @Produces("application/json")
    public Response enrollDevice(@PathParam("deviceType") String deviceType, @PathParam("identifier") String identifier,
                                 @FormParam("name") String name, @FormParam("description") String description,
                                 @FormParam("groupId") int groupId,
                                 @FormParam("enrollmentId") int enrollmentId,
                                 @FormParam("dateOfEnrolment") long dateOfEnrolment,
                                 @FormParam("dateOfLastUpdate") long dateOfLastUpdate,
                                 @FormParam("ownership") EnrolmentInfo.OwnerShip ownership,
                                 @FormParam("status") EnrolmentInfo.Status status,
                                 @FormParam("owner") String owner) {

        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setId(enrollmentId);
        enrolmentInfo.setDateOfEnrolment(dateOfEnrolment);
        enrolmentInfo.setDateOfLastUpdate(dateOfLastUpdate);
        enrolmentInfo.setOwnership(ownership);
        enrolmentInfo.setStatus(status);
        enrolmentInfo.setOwner(owner);

        Device device = new Device();
        device.setType(deviceType);
        device.setDeviceIdentifier(identifier);
        device.setName(name);
        device.setDescription(description);
        device.setGroupId(groupId);
        device.setEnrolmentInfo(enrolmentInfo);
        try {
            boolean isModified = this.getServiceProvider(DeviceManagementProviderService.class).enrollDevice(
                    device);
            if (isModified) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/tenant/configuration")
    @GET
    @Produces("application/json")
    public Response getTenantConfiguration() {
        try {
            TenantConfiguration tenantConfiguration = this.getServiceProvider(
                    DeviceManagementProviderService.class).getConfiguration();
            if (tenantConfiguration != null) {
                return Response.status(Response.Status.OK).entity(tenantConfiguration).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/tenant/configuration")
    @POST
    @Produces("application/json")
    public Response saveTenantConfiguration(@FormParam("tenantConfiguration") TenantConfiguration tenantConfiguration) {
        try {
            boolean isSaved = this.getServiceProvider(DeviceManagementProviderService.class)
                    .saveConfiguration(tenantConfiguration);
            if (isSaved) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/{deviceType}/{identifier}")
    @DELETE
    @Produces("application/json")
    public Response disenrollDevice(@PathParam("deviceType") String deviceType,
                                    @PathParam("identifier") String identifier) {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(deviceType);
        deviceIdentifier.setId(identifier);
        try {
            boolean isDisEnrolled = this.getServiceProvider(DeviceManagementProviderService.class)
                    .disenrollDevice(deviceIdentifier);
            if (isDisEnrolled) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/{deviceType}/{identifier}/enrollment")
    @GET
    @Produces("application/json")
    public Response isEnrolled(@PathParam("deviceType") String deviceType,
                               @PathParam("identifier") String identifier) {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(deviceType);
        deviceIdentifier.setId(identifier);
        try {
            boolean isEnrolled = this.getServiceProvider(DeviceManagementProviderService.class).isEnrolled(
                    deviceIdentifier);
            if (isEnrolled) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/{deviceType}/{identifier}/status")
    @GET
    @Produces("application/json")
    public Response isActive(@PathParam("deviceType") String deviceType,
                             @PathParam("identifier") String identifier) {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(deviceType);
        deviceIdentifier.setId(identifier);
        try {
            boolean isActive = this.getServiceProvider(DeviceManagementProviderService.class).isActive(
                    deviceIdentifier);
            if (isActive) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/{deviceType}/{identifier}/status")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response changeDeviceStatus(@PathParam("deviceType") String deviceType,
                              @PathParam("identifier") String identifier,
                              @FormParam("status") boolean status) {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(deviceType);
        deviceIdentifier.setId(identifier);
        try {
            boolean isActivated = this.getServiceProvider(DeviceManagementProviderService.class).setActive(
                    deviceIdentifier, status);
            if (isActivated) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/{deviceType}/{identifier}/ownership")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response setOwnership(@PathParam("deviceType") String deviceType,
                                 @PathParam("identifier") String identifier,
                                 @FormParam("ownership") String ownership) {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(deviceType);
        deviceIdentifier.setId(identifier);
        try {
            boolean isOwnershipChanged = this.getServiceProvider(DeviceManagementProviderService.class)
                    .setOwnership(deviceIdentifier, ownership);
            if (isOwnershipChanged) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/{deviceType}/{identifier}/enrollment/status")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response setStatus(@PathParam("deviceType") String deviceType,
                              @PathParam("identifier") String identifier, @FormParam("owner") String owner,
                              @FormParam("status") EnrolmentInfo.Status status) {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(deviceType);
        deviceIdentifier.setId(identifier);
        try {
            boolean isStatusChanged = this.getServiceProvider(DeviceManagementProviderService.class)
                    .setStatus(deviceIdentifier, owner, status);
            if (isStatusChanged) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/{deviceType}/{identifier}/sensors/{sensorName}")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response setSensorValue(@PathParam("deviceType") String deviceType,
                                   @PathParam("identifier") String deviceId,
                                   @PathParam("sensorName") String sensorName,
                                   @FormParam("sensorValue") String sensorValue) {

        try {
            boolean isValueSet = SensorDataManager.getInstance().setSensorRecord(deviceId, sensorName,
                    sensorValue, Calendar.getInstance().getTimeInMillis());
            if (isValueSet) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/devices/{deviceType}/{identifier}/sensors/{sensorName}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getSensorValue(@PathParam("deviceType") String deviceType,
                                   @PathParam("identifier") String deviceId,
                                   @PathParam("sensorName") String sensorName,
                                   @QueryParam("defaultValue") String defaultValue) {

        try {
            SensorRecord sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, sensorName);
            if (sensorRecord != null) {
                return Response.status(Response.Status.OK).entity(sensorRecord).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (DeviceControllerException e) {
            log.error("Error on reading sensor value: " + e.getMessage());
            if (defaultValue != null) {
                SensorRecord sensorRecord = new SensorRecord(defaultValue,
                        Calendar.getInstance().getTimeInMillis());
                return Response.status(Response.Status.OK).entity(sensorRecord).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } finally {
            this.endTenantFlow();
        }
    }

}
