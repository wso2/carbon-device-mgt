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

package org.wso2.carbon.apimgt.application.extension.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.apimgt.application.extension.api.util.APIUtil;
import org.wso2.carbon.apimgt.application.extension.api.util.RegistrationProfile;
import org.wso2.carbon.apimgt.application.extension.constants.ApiApplicationConstants;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Arrays;


public class ApiApplicationRegistrationServiceImpl implements ApiApplicationRegistrationService {
    private static final Log log = LogFactory.getLog(ApiApplicationRegistrationServiceImpl.class);

    @Path("register/tenants")
    @POST
    public Response register(@QueryParam("tenantDomain") String tenantDomain,
                             @QueryParam("applicationName") String applicationName) {
        String authenticatedTenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(authenticatedTenantDomain)) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            if (PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId() == -1) {
                String msg = "Invalid tenant domain : " + tenantDomain;
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity(msg).build();
            }
            String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                    .getRealmConfiguration().getAdminUserName();
            username = username + "@" + APIUtil.getTenantDomainOftheUser();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
            APIManagementProviderService apiManagementProviderService = APIUtil.getAPIManagementProviderService();
            ApiApplicationKey apiApplicationKey = apiManagementProviderService.generateAndRetrieveApplicationKeys(
                    applicationName, APIUtil.getAllowedApisTags().toArray(new String[APIUtil.getAllowedApisTags().size()]),
                    ApiApplicationConstants.DEFAULT_TOKEN_TYPE, username, false,
                    ApiApplicationConstants.DEFAULT_VALIDITY_PERIOD);
            return Response.status(Response.Status.CREATED).entity(apiApplicationKey.toString()).build();
        } catch (APIManagerException e) {
            String msg = "Error occurred while registering an application '" + applicationName + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (UserStoreException e) {
            String msg = "Failed to retrieve the tenant" + tenantDomain + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Failed to retrieve the device service";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Path("register")
    @POST
    public Response register(RegistrationProfile registrationProfile) {
        try {
            if (registrationProfile.getTags() == null || registrationProfile.getTags().length == 0) {
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Tags should not be empty").build();
            }
            if (!APIUtil.getAllowedApisTags().containsAll(Arrays.asList(registrationProfile.getTags()))) {
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity("APIs(Tags) are not allowed to this user."
                ).build();
            }
            String username = APIUtil.getAuthenticatedUser() + "@" + APIUtil.getTenantDomainOftheUser();
            APIManagementProviderService apiManagementProviderService = APIUtil.getAPIManagementProviderService();
            String validityPeriod;
            if (registrationProfile.getValidityPeriod() == null) {
                validityPeriod  =  ApiApplicationConstants.DEFAULT_VALIDITY_PERIOD;
            } else {
                validityPeriod = registrationProfile.getValidityPeriod();
            }
            if (registrationProfile.isMappingAnExistingOAuthApp()) {
                JSONObject jsonStringObject = new JSONObject();
                jsonStringObject.put(ApiApplicationConstants.JSONSTRING_USERNAME_TAG, username);
                jsonStringObject.put(ApiApplicationConstants.JSONSTRING_KEY_TYPE_TAG,
                                     ApiApplicationConstants.DEFAULT_TOKEN_TYPE);
                jsonStringObject.put(ApiApplicationConstants.OAUTH_CLIENT_ID, registrationProfile.getConsumerKey());
                jsonStringObject.put(ApiApplicationConstants.OAUTH_CLIENT_SECRET,
                                     registrationProfile.getConsumerSecret());
                jsonStringObject.put(ApiApplicationConstants.JSONSTRING_VALIDITY_PERIOD_TAG, validityPeriod);
                apiManagementProviderService.registerExistingOAuthApplicationToAPIApplication(
                        jsonStringObject.toJSONString(), registrationProfile.getApplicationName(),
                        registrationProfile.getConsumerKey(), username, registrationProfile.isAllowedToAllDomains(),
                        ApiApplicationConstants.DEFAULT_TOKEN_TYPE, registrationProfile.getTags());
                return Response.status(Response.Status.ACCEPTED).entity("true").build();
            } else {
                ApiApplicationKey apiApplicationKey = apiManagementProviderService.generateAndRetrieveApplicationKeys(
                        registrationProfile.getApplicationName(), registrationProfile.getTags(),
                        ApiApplicationConstants.DEFAULT_TOKEN_TYPE, username,
                        registrationProfile.isAllowedToAllDomains(), validityPeriod);
                return Response.status(Response.Status.CREATED).entity(apiApplicationKey.toString()).build();
            }
        } catch (APIManagerException e) {
            String msg = "Error occurred while registering an application '"
                    + registrationProfile.getApplicationName() + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("false").build();
        } catch (DeviceManagementException e) {
            String msg = "Failed to retrieve the device service";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Path("unregister")
    @DELETE
    public Response unregister(@QueryParam("applicationName") String applicationName) {
        try {
            String username = APIUtil.getAuthenticatedUser() + "@" + APIUtil.getTenantDomainOftheUser();
            APIManagementProviderService apiManagementProviderService = APIUtil.getAPIManagementProviderService();
            apiManagementProviderService.removeAPIApplication(applicationName, username);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (APIManagerException e) {
            String msg = "Error occurred while removing the application '" + applicationName;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}