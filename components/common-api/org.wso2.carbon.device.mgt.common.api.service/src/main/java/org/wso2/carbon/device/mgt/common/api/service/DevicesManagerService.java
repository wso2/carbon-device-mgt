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

package org.wso2.carbon.device.mgt.common.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EmailMessageProperties;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.api.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.common.api.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.common.api.sensormgt.SensorRecord;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;

import javax.jws.WebService;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@WebService public class DevicesManagerService {

    private static Log log = LogFactory.getLog(DevicesManagerService.class);

	@Context  //injected response proxy supporting multiple thread
	private HttpServletResponse response;

    private PrivilegedCarbonContext ctx;

    private DeviceManagementProviderService getServiceProvider() {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        PrivilegedCarbonContext.startTenantFlow();
        ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ctx.setTenantDomain(tenantDomain, true);
        if (log.isDebugEnabled()) {
            log.debug("Getting thread local carbon context for tenant domain: " + tenantDomain);
        }
        return (DeviceManagementProviderService) ctx.getOSGiService(DeviceManagementProviderService.class, null);
    }

    private void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
        ctx = null;
        if (log.isDebugEnabled()) {
            log.debug("Tenant flow ended");
        }
    }

    private Device[] getActiveDevices(List<Device> devices){
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

    @Path("/device/user/{username}/all")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	public Device[] getDevicesOfUser(@PathParam("username") String username) {
        try {
            List<Device> devices = this.getServiceProvider().getDevicesOfUser(username);
            return this.getActiveDevices(devices);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            this.endTenantFlow();
        }
    }

	@Path("/device/user/{username}/ungrouped")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	public Device[] getUnGroupedDevices(@PathParam("username") String username){
        try{
    		List<Device> devices = this.getServiceProvider().getUnGroupedDevices(username);
            return this.getActiveDevices(devices);
		} catch (DeviceManagementException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return null;
		} finally {
			this.endTenantFlow();
		}
	}

	@Path("/device/user/{username}/all/count")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	public int getDeviceCount(@PathParam("username") String username){
        try {
            List<Device> devices = this.getServiceProvider().getDevicesOfUser(username);
            if (devices != null) {
                List<Device> activeDevices = new ArrayList<>();
                for (Device device : devices) {
                    if (device.getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.ACTIVE)) {
                        activeDevices.add(device);
                    }
                }
                return activeDevices.size();
            }
            return 0;
		} catch (DeviceManagementException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return 0;
		} finally {
			this.endTenantFlow();
		}
	}

	@Path("/device/type/{type}/identifier/{identifier}")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	public Device getDevice(@PathParam("type") String type, @PathParam("identifier") String identifier){

		try{
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(identifier);
            deviceIdentifier.setType(type);
			return this.getServiceProvider().getDevice(deviceIdentifier);
		} catch (DeviceManagementException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
		} finally {
			this.endTenantFlow();
		}
	}

	@Path("/device/type/all")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	public DeviceType[] getDeviceTypes(){
		try{
			List<DeviceType> deviceTypes = this.getServiceProvider().getAvailableDeviceTypes();
            return deviceTypes.toArray(new DeviceType[deviceTypes.size()]);
		} catch (DeviceManagementException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return null;
		} finally {
            this.endTenantFlow();
		}
	}

    @Path("/device/type/{type}/all")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Device[] getAllDevices(@PathParam("type") String type){
        try{
            List<Device> devices = this.getServiceProvider().getAllDevices(type);
            return this.getActiveDevices(devices);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/all")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Device[] getAllDevices(){
        try{
            List<Device> devices = this.getServiceProvider().getAllDevices();
            return this.getActiveDevices(devices);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/enrollment/invitation")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public void sendEnrolmentInvitation(@FormParam("messageBody") String messageBody,
            @FormParam("mailTo") String[] mailTo, @FormParam("ccList") String[] ccList,
            @FormParam("bccList") String[] bccList, @FormParam("subject") String subject,
            @FormParam("firstName") String firstName, @FormParam("enrolmentUrl") String enrolmentUrl,
            @FormParam("title") String title, @FormParam("password") String password,
            @FormParam("userName") String userName){
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
            this.getServiceProvider().sendEnrolmentInvitation(config);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/registration/invitation")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public void sendRegistrationEmail(@FormParam("messageBody") String messageBody,
            @FormParam("mailTo") String[] mailTo, @FormParam("ccList") String[] ccList,
            @FormParam("bccList") String[] bccList, @FormParam("subject") String subject,
            @FormParam("firstName") String firstName, @FormParam("enrolmentUrl") String enrolmentUrl,
            @FormParam("title") String title, @FormParam("password") String password,
            @FormParam("userName") String userName){
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
            this.getServiceProvider().sendRegistrationEmail(config);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/type/{type}/config")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public TenantConfiguration getConfiguration(@PathParam("type") String type){
        try {
            return this.getServiceProvider().getConfiguration(type);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/group/{groupId}/all")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Device[] getDevices(@PathParam("groupId") int groupId){
        try{
            List<Device> devices = this.getServiceProvider().getDevices(groupId);
            return this.getActiveDevices(devices);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/role/{role}/all")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Device[] getAllDevicesOfRole(@PathParam("role") String roleName){
        try{
            List<Device> devices = this.getServiceProvider().getAllDevicesOfRole(roleName);
            return this.getActiveDevices(devices);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/name/{name}/all")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Device[] getDevicesByName(@PathParam("name") String name) {
        try{
            List<Device> devices = this.getServiceProvider().getDevicesByName(name);
            return this.getActiveDevices(devices);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/type/{type}/identifier/{identifier}/status")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    void updateDeviceEnrolmentInfo(@PathParam("type") String type, @PathParam("identifier") String identifier,
            @FormParam("status") EnrolmentInfo.Status status) {
        DeviceManagementProviderService providerService = this.getServiceProvider();
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(type);
        deviceIdentifier.setId(identifier);
        try {
            Device device = providerService.getDevice(deviceIdentifier);
            providerService.updateDeviceEnrolmentInfo(device, status);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/status/{status}/all")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Device[] getDevicesByStatus(@PathParam("status") EnrolmentInfo.Status status) {
        try{
            List<Device> devices = this.getServiceProvider().getDevicesByStatus(status);
            return this.getActiveDevices(devices);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/type/{type}/license")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public License getLicense(@PathParam("type") String type, @QueryParam("languageCode") String languageCode) {
        try{
            return this.getServiceProvider().getLicense(type, languageCode);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/type/{type}/license")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public void addLicense(@PathParam("type") String type, @FormParam("provider") String provider,
            @FormParam("name") String name, @FormParam("version") String version,
            @FormParam("language") String language, @FormParam("validFrom") Date validFrom,
            @FormParam("validTo") Date validTo, @FormParam("text") String text) {
        try{
            License license = new License();
            license.setProvider(provider);
            license.setName(name);
            license.setVersion(version);
            license.setLanguage(language);
            license.setValidFrom(validFrom);
            license.setValidTo(validTo);
            license.setText(text);
            this.getServiceProvider().addLicense(type, license);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/type/{type}/identifier/{identifier}")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    boolean modifyEnrollment(@PathParam("type") String type, @PathParam("identifier") String identifier,
            @FormParam("name") String name, @FormParam("description") String description,
            @FormParam("groupId") int groupId, @FormParam("enrollmentId") int enrollmentId,
            @FormParam("dateOfEnrolment") long dateOfEnrolment, @FormParam("dateOfLastUpdate") long dateOfLastUpdate,
            @FormParam("ownership") EnrolmentInfo.OwnerShip ownership, @FormParam("status") EnrolmentInfo.Status status,
            @FormParam("owner") String owner){

        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setId(enrollmentId);
        enrolmentInfo.setDateOfEnrolment(dateOfEnrolment);
        enrolmentInfo.setDateOfLastUpdate(dateOfLastUpdate);
        enrolmentInfo.setOwnership(ownership);
        enrolmentInfo.setStatus(status);
        enrolmentInfo.setOwner(owner);

        Device device = new Device();
        device.setType(type);
        device.setDeviceIdentifier(identifier);
        device.setName(name);
        device.setDescription(description);
        device.setGroupId(groupId);
        device.setEnrolmentInfo(enrolmentInfo);
        try {
            return this.getServiceProvider().modifyEnrollment(device);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    boolean enrollDevice(@FormParam("type") String type, @FormParam("identifier") String identifier,
            @FormParam("name") String name, @FormParam("description") String description,
            @FormParam("groupId") int groupId, @FormParam("enrollmentId") int enrollmentId,
            @FormParam("dateOfEnrolment") long dateOfEnrolment, @FormParam("dateOfLastUpdate") long dateOfLastUpdate,
            @FormParam("ownership") EnrolmentInfo.OwnerShip ownership, @FormParam("status") EnrolmentInfo.Status status,
            @FormParam("owner") String owner){

        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setId(enrollmentId);
        enrolmentInfo.setDateOfEnrolment(dateOfEnrolment);
        enrolmentInfo.setDateOfLastUpdate(dateOfLastUpdate);
        enrolmentInfo.setOwnership(ownership);
        enrolmentInfo.setStatus(status);
        enrolmentInfo.setOwner(owner);

        Device device = new Device();
        device.setType(type);
        device.setDeviceIdentifier(identifier);
        device.setName(name);
        device.setDescription(description);
        device.setGroupId(groupId);
        device.setEnrolmentInfo(enrolmentInfo);
        try {
            return this.getServiceProvider().enrollDevice(device);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/tenantconfiguration")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public TenantConfiguration getConfiguration(){
        try {
            return this.getServiceProvider().getConfiguration();
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/tenantconfiguration")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public boolean saveConfiguration(@FormParam("tenantConfiguration") TenantConfiguration tenantConfiguration){
        try {
            return this.getServiceProvider().saveConfiguration(tenantConfiguration);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/type/{type}/identifier/{identifier}")
    @DELETE
    @Consumes("application/json")
    @Produces("application/json")
    public boolean disenrollDevice(@PathParam("type") String type, @PathParam("identifier") String identifier){
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(type);
        deviceIdentifier.setId(identifier);
        try {
            return this.getServiceProvider().disenrollDevice(deviceIdentifier);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/type/{type}/identifier/{identifier}/enrolled")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public boolean isEnrolled(@PathParam("type") String type, @PathParam("identifier") String identifier){
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(type);
        deviceIdentifier.setId(identifier);
        try {
            return this.getServiceProvider().isEnrolled(deviceIdentifier);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } finally {
            this.endTenantFlow();
        }
    }
    @Path("/device/type/{type}/identifier/{identifier}/active")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public boolean isActive(@PathParam("type") String type, @PathParam("identifier") String identifier){
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(type);
        deviceIdentifier.setId(identifier);
        try {
            return this.getServiceProvider().isActive(deviceIdentifier);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/type/{type}/identifier/{identifier}/active")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public boolean setActive(@PathParam("type") String type, @PathParam("identifier") String identifier,
            @FormParam("status") boolean status){
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(type);
        deviceIdentifier.setId(identifier);
        try {
            return this.getServiceProvider().setActive(deviceIdentifier, status);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/type/{type}/identifier/{identifier}/ownership")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public boolean setOwnership(@PathParam("type") String type, @PathParam("identifier") String identifier,
            @FormParam("ownership") String ownership){
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(type);
        deviceIdentifier.setId(identifier);
        try {
            return this.getServiceProvider().setOwnership(deviceIdentifier, ownership);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/type/{type}/identifier/{identifier}/status")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public boolean setStatus(@PathParam("type") String type, @PathParam("identifier") String identifier,
            @FormParam("owner") String owner, @FormParam("status") EnrolmentInfo.Status status){
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(type);
        deviceIdentifier.setId(identifier);
        try {
            return this.getServiceProvider().setStatus(deviceIdentifier, owner, status);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/type/{type}/identifier/{identifier}/sensor/{sensorName}")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public boolean setSensorValue(@PathParam("type") String type, @PathParam("identifier") String deviceId,
                                  @PathParam("sensorName") String sensorName,
                                  @HeaderParam("sensorValue") String sensorValue){

        try {
            return SensorDataManager.getInstance().setSensorRecord(deviceId, sensorName, sensorValue, Calendar
                    .getInstance().getTimeInMillis());
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/device/type/{type}/identifier/{identifier}/sensor/{sensorName}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public SensorRecord getSensorValue(@PathParam("type") String type, @PathParam("identifier") String deviceId,
                                   @PathParam("sensorName") String sensorName, @HeaderParam("defaultValue") String defaultValue){

        try {
            return SensorDataManager.getInstance().getSensorRecord(deviceId, sensorName);
        } catch (DeviceControllerException e) {
            log.error("Error on reading sensor value: " + e.getMessage());
            if(defaultValue != null){
                return new SensorRecord(defaultValue, Calendar.getInstance().getTimeInMillis());
            }else{
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                return null;
            }
        } finally {
            this.endTenantFlow();
        }
    }

}
