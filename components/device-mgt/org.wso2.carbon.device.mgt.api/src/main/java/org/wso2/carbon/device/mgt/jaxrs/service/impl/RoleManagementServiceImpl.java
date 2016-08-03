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
import org.wso2.carbon.device.mgt.common.scope.mgt.ScopeManagementException;
import org.wso2.carbon.device.mgt.common.scope.mgt.ScopeManagementService;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleInfo;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleList;
import org.wso2.carbon.device.mgt.jaxrs.beans.Scope;
import org.wso2.carbon.device.mgt.jaxrs.service.api.RoleManagementService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.FilteringUtil;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.SetReferenceTransformer;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleManagementServiceImpl implements RoleManagementService {

    private static final String API_BASE_PATH = "/roles";
    private static final Log log = LogFactory.getLog(RoleManagementServiceImpl.class);
    private static final String PRIMARY_USER_STORE = "PRIMARY";

    @GET
    @Override
    public Response getRoles(
            @QueryParam("filter") String filter,
            @QueryParam("user-store") String userStore,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset, @QueryParam("limit") int limit) {
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        List<String> filteredRoles;
        RoleList targetRoles = new RoleList();

        //if user store is null set it to primary
        if(userStore == null || "".equals(userStore)){
            userStore = PRIMARY_USER_STORE;
        }

        try {
            //Get the total role count that matches the given filter
            filteredRoles = getRolesFromUserStore(filter, userStore);
            targetRoles.setCount(filteredRoles.size());

            filteredRoles = FilteringUtil.getFilteredList(getRolesFromUserStore(filter, userStore), offset, limit);
            targetRoles.setList(filteredRoles);

            return Response.ok().entity(targetRoles).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving roles from the underlying user stores";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/scopes")
    @Override
    public Response getScopes(
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {

        List<Scope> scopes = new ArrayList<>();
        try {
            ScopeManagementService scopeManagementService = DeviceMgtAPIUtils.getScopeManagementService();
            if (scopeManagementService == null) {
                log.error("Scope management service initialization is failed, hence scopes will not be retrieved");
            } else {
                scopes = DeviceMgtUtil.convertAPIScopestoScopes(scopeManagementService.getAllScopes());
            }
            return Response.status(Response.Status.OK).entity(scopes).build();
        } catch (ScopeManagementException e) {
            String msg = "Error occurred while retrieving the scopes";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @PUT
    @Path("/scopes")
    @Override
    public Response updateScopes(List<Scope> scopes) {
        RequestValidationUtil.validateScopes(scopes);
        try {
            ScopeManagementService scopeManagementService = DeviceMgtAPIUtils.getScopeManagementService();
            if (scopeManagementService == null) {
                log.error("Scope management service initialization is failed, hence scopes will not be retrieved");
            } else {
                scopeManagementService.updateScopes(DeviceMgtUtil.convertScopestoAPIScopes(scopes));
            }
            return Response.status(Response.Status.OK).entity("Scopes has been successfully updated").build();
        } catch (ScopeManagementException e) {
            String msg = "Error occurred while updating the scopes";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/{roleName}")
    @Override
    public Response getRole(@PathParam("roleName") String roleName,
                            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        if (log.isDebugEnabled()) {
            log.debug("Getting the list of user roles");
        }
        RequestValidationUtil.validateRoleName(roleName);
        RoleInfo roleInfo = new RoleInfo();
        try {
            final UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (!userStoreManager.isExistingRole(roleName)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("No role exists with the name '" +
                                roleName + "'").build()).build();
            }
            roleInfo.setRoleName(roleName);
            roleInfo.setUsers(userStoreManager.getUserListOfRole(roleName));

            return Response.status(Response.Status.OK).entity(roleInfo).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the user role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @POST
    @Override
    public Response addRole(RoleInfo roleInfo) {
        RequestValidationUtil.validateRoleDetails(roleInfo);
        RequestValidationUtil.validateRoleName(roleInfo.getRoleName());

        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (log.isDebugEnabled()) {
                log.debug("Persisting the role in the underlying user store");
            }
            userStoreManager.addRole(roleInfo.getRoleName(), roleInfo.getUsers(), null);

            //TODO fix what's returned in the entity
            return Response.created(new URI(API_BASE_PATH + "/" + roleInfo.getRoleName())).entity(
                    "Role '" + roleInfo.getRoleName() + "' has " +
                            "successfully been added").build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while adding role '" + roleInfo.getRoleName() + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (URISyntaxException e) {
            String msg = "Error occurred while composing the URI at which the information of the newly created role " +
                    "can be retrieved";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @PUT
    @Path("/{roleName}")
    @Override
    public Response updateRole(@PathParam("roleName") String roleName, RoleInfo roleInfo) {
        RequestValidationUtil.validateRoleName(roleName);
        RequestValidationUtil.validateRoleDetails(roleInfo);
        try {
            final UserRealm userRealm = DeviceMgtAPIUtils.getUserRealm();
            final UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            if (!userStoreManager.isExistingRole(roleName)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("No role exists with the name '" +
                                roleName + "'").build()).build();
            }

            final AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
            if (log.isDebugEnabled()) {
                log.debug("Updating the role to user store");
            }

            String newRoleName = roleInfo.getRoleName();
            if (newRoleName != null && !roleName.equals(newRoleName)) {
                userStoreManager.updateRoleName(roleName, newRoleName);
            }

            if (roleInfo.getUsers() != null) {
                SetReferenceTransformer<String> transformer = new SetReferenceTransformer<>();
                transformer.transform(Arrays.asList(userStoreManager.getUserListOfRole(newRoleName)),
                        Arrays.asList(roleInfo.getUsers()));
                final String[] usersToAdd = transformer.getObjectsToAdd().toArray(new String[transformer
                        .getObjectsToAdd().size()]);
                final String[] usersToDelete = transformer.getObjectsToRemove().toArray(new String[transformer
                        .getObjectsToRemove().size()]);
                userStoreManager.updateUserListOfRole(newRoleName, usersToDelete, usersToAdd);
            }

            if (roleInfo.getScopes() != null) {
                ScopeManagementService scopeManagementService = DeviceMgtAPIUtils.getScopeManagementService();
                if (scopeManagementService == null) {
                    log.error("Scope management service initialization is failed, hence scopes will not be updated");
                } else {
                    scopeManagementService.updateScopes(DeviceMgtUtil.convertScopestoAPIScopes(roleInfo.getScopes()));
                }
            }
            //TODO: Need to send the updated role information in the entity back to the client
            return Response.status(Response.Status.OK).entity("Role '" + roleInfo.getRoleName() + "' has " +
                    "successfully been updated").build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while updating role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (ScopeManagementException e) {
            String msg = "Error occurred while updating scopes of role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @DELETE
    @Path("/{roleName}")
    @Override
    public Response deleteRole(@PathParam("roleName") String roleName, RoleInfo roleInfo) {
        RequestValidationUtil.validateRoleName(roleName);
        RequestValidationUtil.validateScopes(roleInfo.getScopes());

        try {
            final UserRealm userRealm = DeviceMgtAPIUtils.getUserRealm();
            final UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            if (!userStoreManager.isExistingRole(roleName)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("No role exists with the name '" +
                                roleName + "'").build()).build();
            }

            final AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
            if (log.isDebugEnabled()) {
                log.debug("Deleting the role in user store");
            }
            userStoreManager.deleteRole(roleName);
            // Delete all authorizations for the current role before deleting
            authorizationManager.clearRoleAuthorization(roleName);

            //updating scopes
            ScopeManagementService scopeManagementService = DeviceMgtAPIUtils.getScopeManagementService();
            if (scopeManagementService == null) {
                log.error("Scope management service initialization is failed, hence scopes will not be updated");
            } else {
                scopeManagementService.updateScopes(DeviceMgtUtil.convertScopestoAPIScopes(roleInfo.getScopes()));
            }

            return Response.status(Response.Status.OK).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while deleting the role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (ScopeManagementException e) {
            String msg = "Error occurred while updating scopes of role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @PUT
    @Path("/{roleName}/users")
    @Override
    public Response updateUsersOfRole(@PathParam("roleName") String roleName, List<String> users) {
        RequestValidationUtil.validateRoleName(roleName);
        RequestValidationUtil.validateUsers(users);
        try {
            final UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (log.isDebugEnabled()) {
                log.debug("Updating the users of a role");
            }
            SetReferenceTransformer<String> transformer = new SetReferenceTransformer<>();
            transformer.transform(Arrays.asList(userStoreManager.getUserListOfRole(roleName)),
                    users);
            final String[] usersToAdd = transformer.getObjectsToAdd().toArray(new String[transformer
                    .getObjectsToAdd().size()]);
            final String[] usersToDelete = transformer.getObjectsToRemove().toArray(new String[transformer
                    .getObjectsToRemove().size()]);

            userStoreManager.updateUserListOfRole(roleName, usersToDelete, usersToAdd);

            return Response.status(Response.Status.OK).entity("Role '" + roleName + "' has " +
                    "successfully been updated with the user list").build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while updating the users of the role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    private List<String> getRolesFromUserStore(String filter, String userStore) throws UserStoreException {
        AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) DeviceMgtAPIUtils.getUserStoreManager();
        String[] roles;
        boolean filterRolesByName = (!((filter == null) || filter.isEmpty()));
        if (log.isDebugEnabled()) {
            log.debug("Getting the list of user roles");
        }
        roles = userStoreManager.getRoleNames(userStore+"/*", -1, false, true, true);
        // removing all internal roles, roles created for Service-providers and application related roles.
        List<String> filteredRoles = new ArrayList<>();
        for (String role : roles) {
            if (!(role.startsWith("Internal/") || role.startsWith("Authentication/") || role.startsWith("Application/"))) {
                if (!filterRolesByName) {
                    filteredRoles.add(role);
                } else {
                    if (role.contains(filter)) {
                        filteredRoles.add(role);
                    }
                }
            }
        }
        return filteredRoles;
    }

}
