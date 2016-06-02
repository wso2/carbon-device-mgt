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
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.service.api.RoleManagementService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.*;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.NotFoundException;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleList;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.FilteringUtil;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.UnexpectedServerErrorException;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleWrapper;
import org.wso2.carbon.device.mgt.jaxrs.util.SetReferenceTransformer;
import org.wso2.carbon.user.api.*;
import org.wso2.carbon.user.mgt.UserRealmProxy;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleManagementServiceImpl implements RoleManagementService {

    private static final Log log = LogFactory.getLog(RoleManagementServiceImpl.class);

    @GET
    @Override
    public Response getRoles(
            @QueryParam("filter") String filter,
            @QueryParam("user-store") String userStoreName,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset, @QueryParam("limit") int limit) {
        List<String> filteredRoles;
        RoleList targetRoles = new RoleList();
        try {
            filteredRoles = getRolesFromUserStore();
            if (filteredRoles == null || filteredRoles.size() == 0) {
                throw new NotFoundException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("No roles found.").build());
            }
            targetRoles.setCount(filteredRoles.size());
            filteredRoles = FilteringUtil.getFilteredList(getRolesFromUserStore(), offset, limit);
            if (filteredRoles.size() == 0) {
                throw new NotFoundException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("No roles found").build());
            }
            targetRoles.setList(filteredRoles);
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving roles from the underlying user stores";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity(targetRoles).build();
    }

    @GET
    @Path("/{roleName}/permissions")
    @Override
    public Response getPermissionsOfRole(
            @PathParam("roleName") String roleName,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        RequestValidationUtil.validateRoleName(roleName);
        try {
            final UserRealm userRealm = DeviceMgtAPIUtils.getUserRealm();
            org.wso2.carbon.user.core.UserRealm userRealmCore = null;
            final UIPermissionNode rolePermissions;
            if (userRealm instanceof org.wso2.carbon.user.core.UserRealm) {
                userRealmCore = (org.wso2.carbon.user.core.UserRealm) userRealm;
            }
            final UserRealmProxy userRealmProxy = new UserRealmProxy(userRealmCore);
            rolePermissions = this.getUIPermissionNode(roleName, userRealmProxy);
            if (rolePermissions == null) {
                throw new NotFoundException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("No permissions found" +
                                " for the role '" + roleName + "'").build());
            }
            return Response.status(Response.Status.OK).entity(rolePermissions).build();
        } catch (UserAdminException e) {
            String msg = "Error occurred while retrieving the permissions of role '" + roleName + "'";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the underlying user realm attached to the " +
                    "current logged in user";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
    }

    private UIPermissionNode getUIPermissionNode(String roleName, UserRealmProxy userRealmProxy)
            throws UserAdminException {
        final UIPermissionNode rolePermissions =
                userRealmProxy.getRolePermissions(roleName, MultitenantConstants.SUPER_TENANT_ID);
        UIPermissionNode[] deviceMgtPermissions = new UIPermissionNode[2];

        for (UIPermissionNode permissionNode : rolePermissions.getNodeList()) {
            if (permissionNode.getResourcePath().equals("/permission/admin")) {
                for (UIPermissionNode node : permissionNode.getNodeList()) {
                    if (node.getResourcePath().equals("/permission/admin/device-mgt")) {
                        deviceMgtPermissions[0] = node;
                    } else if (node.getResourcePath().equals("/permission/admin/login")) {
                        deviceMgtPermissions[1] = node;
                    }
                }
            }
        }
        rolePermissions.setNodeList(deviceMgtPermissions);
        return rolePermissions;
    }

    @GET
    @Path("/{roleName}")
    @Override
    public Response getRole(@PathParam("roleName") String roleName,
                            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        RequestValidationUtil.validateRoleName(roleName);
        RoleWrapper roleWrapper = new RoleWrapper();
        try {
            final UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            final UserRealm userRealm = DeviceMgtAPIUtils.getUserRealm();
            org.wso2.carbon.user.core.UserRealm userRealmCore = null;
            if (userRealm instanceof org.wso2.carbon.user.core.UserRealm) {
                userRealmCore = (org.wso2.carbon.user.core.UserRealm) userRealm;
            }

            final UserRealmProxy userRealmProxy = new UserRealmProxy(userRealmCore);
            if (log.isDebugEnabled()) {
                log.debug("Getting the list of user roles");
            }
            if (userStoreManager.isExistingRole(roleName)) {
                roleWrapper.setRoleName(roleName);
                roleWrapper.setUsers(userStoreManager.getUserListOfRole(roleName));
                // Get the permission nodes and hand picking only device management and login perms
                final UIPermissionNode rolePermissions = getUIPermissionNode(roleName, userRealmProxy);
                List<String> permList = new ArrayList<>();
                this.iteratePermissions(rolePermissions, permList);
                roleWrapper.setPermissionList(rolePermissions);
                String[] permListAr = new String[permList.size()];
                roleWrapper.setPermissions(permList.toArray(permListAr));
            } else {
                throw new NotFoundException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("Role name doesn't exist.")
                                .build());
            }
        } catch (UserStoreException | UserAdminException e) {
            String msg = "Error occurred while retrieving the user role '" + roleName + "'";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity(roleWrapper).build();
    }

    private List<String> iteratePermissions(UIPermissionNode uiPermissionNode, List<String> list) {
        for (UIPermissionNode permissionNode : uiPermissionNode.getNodeList()) {
            list.add(permissionNode.getResourcePath());
            if (permissionNode.getNodeList() != null && permissionNode.getNodeList().length > 0) {
                iteratePermissions(permissionNode, list);
            }
        }
        return list;
    }

    @POST
    @Override
    public Response addRole(RoleWrapper roleWrapper) {
        RequestValidationUtil.validateRoleDetails(roleWrapper);
        RequestValidationUtil.validateRoleName(roleWrapper.getRoleName());
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (log.isDebugEnabled()) {
                log.debug("Persisting the role to user store");
            }
            Permission[] permissions = null;
            if (roleWrapper.getPermissions() != null && roleWrapper.getPermissions().length > 0) {
                permissions = new Permission[roleWrapper.getPermissions().length];

                for (int i = 0; i < permissions.length; i++) {
                    String permission = roleWrapper.getPermissions()[i];
                    permissions[i] = new Permission(permission, CarbonConstants.UI_PERMISSION_ACTION);
                }
            }
            userStoreManager.addRole(roleWrapper.getRoleName(), roleWrapper.getUsers(), permissions);
        } catch (UserStoreException e) {
            String msg = "Error occurred while adding role '" + roleWrapper.getRoleName() + "'";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity("Role '" + roleWrapper.getRoleName() + "' has " +
                "successfully been added").build();
    }

    @PUT
    @Path("/{roleName}")
    @Override
    public Response updateRole(@PathParam("roleName") String roleName, RoleWrapper roleWrapper) {
        RequestValidationUtil.validateRoleName(roleName);
        RequestValidationUtil.validateRoleDetails(roleWrapper);
        String newRoleName = roleWrapper.getRoleName();
        try {
            final UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            final AuthorizationManager authorizationManager = DeviceMgtAPIUtils.getAuthorizationManager();
            if (log.isDebugEnabled()) {
                log.debug("Updating the role to user store");
            }
            if (newRoleName != null && !roleName.equals(newRoleName)) {
                userStoreManager.updateRoleName(roleName, newRoleName);
            }
            if (roleWrapper.getUsers() != null) {
                SetReferenceTransformer<String> transformer = new SetReferenceTransformer<>();
                transformer.transform(Arrays.asList(userStoreManager.getUserListOfRole(newRoleName)),
                        Arrays.asList(roleWrapper.getUsers()));
                final String[] usersToAdd = transformer.getObjectsToAdd().toArray(new String[transformer
                        .getObjectsToAdd().size()]);
                final String[] usersToDelete = transformer.getObjectsToRemove().toArray(new String[transformer
                        .getObjectsToRemove().size()]);
                userStoreManager.updateUserListOfRole(newRoleName, usersToDelete, usersToAdd);
            }
            if (roleWrapper.getPermissions() != null) {
                // Delete all authorizations for the current role before authorizing the permission tree
                authorizationManager.clearRoleAuthorization(roleName);
                if (roleWrapper.getPermissions().length > 0) {
                    for (int i = 0; i < roleWrapper.getPermissions().length; i++) {
                        String permission = roleWrapper.getPermissions()[i];
                        authorizationManager.authorizeRole(roleName, permission, CarbonConstants.UI_PERMISSION_ACTION);
                    }
                }
            }
        } catch (UserStoreException e) {
            String msg = "Error occurred while updating role '" + roleName + "'";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity("Role '" + roleWrapper.getRoleName() + "' has " +
                "successfully been updated").build();
    }

    @DELETE
    @Path("/{roleName}")
    @Override
    public Response deleteRole(@PathParam("roleName") String roleName) {
        RequestValidationUtil.validateRoleName(roleName);
        try {
            final UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            final AuthorizationManager authorizationManager = DeviceMgtAPIUtils.getAuthorizationManager();
            if (log.isDebugEnabled()) {
                log.debug("Deleting the role in user store");
            }
            userStoreManager.deleteRole(roleName);
            // Delete all authorizations for the current role before deleting
            authorizationManager.clearRoleAuthorization(roleName);
        } catch (UserStoreException e) {
            String msg = "Error occurred while deleting the role '" + roleName + "'";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity("Role '" + roleName + "' has " +
                "successfully been deleted").build();
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
        } catch (UserStoreException e) {
            String msg = "Error occurred while updating the users of the role '" + roleName + "'";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity("Role '" + roleName + "' has " +
                "successfully been updated with the user list").build();
    }

    private List<String> getRolesFromUserStore() throws UserStoreException {
        UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
        String[] roles;
        if (log.isDebugEnabled()) {
            log.debug("Getting the list of user roles");
        }
        roles = userStoreManager.getRoleNames();
        // removing all internal roles, roles created for Service-providers and application related roles.
        List<String> filteredRoles = new ArrayList<>();
        for (String role : roles) {
            if (!(role.startsWith("Internal/") || role.startsWith("Authentication/") || role.startsWith("Application/"))) {
                filteredRoles.add(role);
            }
        }
        return filteredRoles;
    }

}
