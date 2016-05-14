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
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.device.mgt.jaxrs.api.common.MDMAPIException;
import org.wso2.carbon.device.mgt.jaxrs.api.util.DeviceMgtAPIUtils;
import org.wso2.carbon.device.mgt.jaxrs.api.util.ResponsePayload;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleWrapper;
import org.wso2.carbon.device.mgt.jaxrs.util.SetReferenceTransformer;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.mgt.UserRealmProxy;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("NonJaxWsWebServices")
public class RoleImpl implements org.wso2.carbon.device.mgt.jaxrs.api.Role {

    private static Log log = LogFactory.getLog(RoleImpl.class);

    /**
     * Get user roles (except all internal roles) from system.
     *
     * @return A list of users
     */
    @Override
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllRoles() {
        List<String> filteredRoles;
        try {
            filteredRoles = getRolesFromUserStore();
        } catch (MDMAPIException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getErrorMessage()).build();
        }
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("All user roles were successfully retrieved.");
        responsePayload.setResponseContent(filteredRoles);
        return Response.status(Response.Status.OK).entity(responsePayload).build();
    }

    /**
     * Get user roles by user store(except all internal roles) from system.
     *
     * @return A list of users
     */
    @Override
    @GET
    @Path("{userStore}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getRolesOfUserStore(@PathParam("userStore") String userStore) {
        String[] roles;
        try {
            AbstractUserStoreManager abstractUserStoreManager =
                    (AbstractUserStoreManager) DeviceMgtAPIUtils.getUserStoreManager();
            if (log.isDebugEnabled()) {
                log.debug("Getting the list of user roles");
            }
            roles = abstractUserStoreManager.getRoleNames(userStore + "/*", -1, false, true, true);

        } catch (UserStoreException | MDMAPIException e) {
            String msg = "Error occurred while retrieving the list of user roles.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        // removing all internal roles and roles created for Service-providers
        List<String> filteredRoles = new ArrayList<>();
        for (String role : roles) {
            if (!(role.startsWith("Internal/") || role.startsWith("Authentication/"))) {
                filteredRoles.add(role);
            }
        }
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("All user roles were successfully retrieved.");
        responsePayload.setResponseContent(filteredRoles);
        return Response.status(Response.Status.OK).entity(responsePayload).build();
    }

    /**
     * Get user roles by providing a filtering criteria(except all internal roles & system roles) from system.
     *
     * @return A list of users
     */
    @Override
    @GET
    @Path("search")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getMatchingRoles(@QueryParam("filter") String filter) {
        String[] roles;
        try {
            AbstractUserStoreManager abstractUserStoreManager =
                    (AbstractUserStoreManager) DeviceMgtAPIUtils.getUserStoreManager();
            if (log.isDebugEnabled()) {
                log.debug("Getting the list of user roles using filter : " + filter);
            }
            roles = abstractUserStoreManager.getRoleNames("*" + filter + "*", -1, true, true, true);

        } catch (UserStoreException | MDMAPIException e) {
            String msg = "Error occurred while retrieving the list of user roles using the filter : " + filter;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        // removing all internal roles and roles created for Service-providers
        List<String> filteredRoles = new ArrayList<>();
        for (String role : roles) {
            if (!(role.startsWith("Internal/") || role.startsWith("Authentication/"))) {
                filteredRoles.add(role);
            }
        }
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("All matching user roles were successfully retrieved.");
        responsePayload.setResponseContent(filteredRoles);
        return Response.status(Response.Status.OK).entity(responsePayload).build();
    }

    /**
     * Get role permissions.
     *
     * @return list of permissions
     */
    @Override
    @GET
    @Path("permissions")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPermissions(@QueryParam("rolename") String roleName) {
        try {
            final UserRealm userRealm = DeviceMgtAPIUtils.getUserRealm();
            org.wso2.carbon.user.core.UserRealm userRealmCore = null;
            final UIPermissionNode rolePermissions;
            if (userRealm instanceof org.wso2.carbon.user.core.UserRealm) {
                userRealmCore = (org.wso2.carbon.user.core.UserRealm) userRealm;
            }
            final UserRealmProxy userRealmProxy = new UserRealmProxy(userRealmCore);
            rolePermissions = getUIPermissionNode(roleName, userRealmProxy);
            ResponsePayload responsePayload = new ResponsePayload();
            responsePayload.setStatusCode(HttpStatus.SC_OK);
            responsePayload.setMessageFromServer("All permissions retrieved");
            responsePayload.setResponseContent(rolePermissions);
            return Response.status(Response.Status.OK).entity(responsePayload).build();
        } catch (UserAdminException | MDMAPIException e) {
            String msg = "Error occurred while retrieving the user role";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * Get user role of the system
     *
     * @return user role
     */
    @Override
    @GET
    @Path("role")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getRole(@QueryParam("rolename") String roleName) {
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
                ArrayList<String> permList = new ArrayList<>();
                iteratePermissions(rolePermissions, permList);
                roleWrapper.setPermissionList(rolePermissions);
                String[] permListAr = new String[permList.size()];
                roleWrapper.setPermissions(permList.toArray(permListAr));
            }
        } catch (UserStoreException | UserAdminException | MDMAPIException e) {
            String msg = "Error occurred while retrieving the user role";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("All user roles were successfully retrieved.");
        responsePayload.setResponseContent(roleWrapper);
        return Response.status(Response.Status.OK).entity(responsePayload).build();
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

    /**
     * API is used to persist a new Role
     *
     * @param roleWrapper for role
     * @return response
     */
    @Override
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    public Response addRole(RoleWrapper roleWrapper) {
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
        } catch (UserStoreException | MDMAPIException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    /**
     * API is used to update a role Role
     *
     * @param roleWrapper for role
     * @return response
     */
    @Override
    @PUT
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateRole(@QueryParam("rolename") String roleName, RoleWrapper roleWrapper) {
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
        } catch (UserStoreException | MDMAPIException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    /**
     * API is used to delete a role and authorizations
     *
     * @param roleName to delete
     * @return response
     */
    @Override
    @DELETE
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteRole(@QueryParam("rolename") String roleName) {
        try {
            final UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            final AuthorizationManager authorizationManager = DeviceMgtAPIUtils.getAuthorizationManager();
            if (log.isDebugEnabled()) {
                log.debug("Deleting the role in user store");
            }
            userStoreManager.deleteRole(roleName);
            // Delete all authorizations for the current role before deleting
            authorizationManager.clearRoleAuthorization(roleName);
        } catch (UserStoreException | MDMAPIException e) {
            String msg = "Error occurred while deleting the role: " + roleName;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    /**
     * API is used to update users of a role
     *
     * @param roleName to update
     * @param userList of the users
     * @return response
     */
    @Override
    @PUT
    @Path("users")
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateUsers(@QueryParam("rolename") String roleName, List<String> userList) {
        try {
            final UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (log.isDebugEnabled()) {
                log.debug("Updating the users of a role");
            }
            SetReferenceTransformer<String> transformer = new SetReferenceTransformer<>();
            transformer.transform(Arrays.asList(userStoreManager.getUserListOfRole(roleName)),
                                  userList);
            final String[] usersToAdd = transformer.getObjectsToAdd().toArray(new String[transformer
                    .getObjectsToAdd().size()]);
            final String[] usersToDelete = transformer.getObjectsToRemove().toArray(new String[transformer
                    .getObjectsToRemove().size()]);

            userStoreManager.updateUserListOfRole(roleName, usersToDelete, usersToAdd);
        } catch (UserStoreException | MDMAPIException e) {
            String msg = "Error occurred while saving the users of the role: " + roleName;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    private ArrayList<String> iteratePermissions(UIPermissionNode uiPermissionNode, ArrayList<String> list) {
        for (UIPermissionNode permissionNode : uiPermissionNode.getNodeList()) {
            list.add(permissionNode.getResourcePath());
            if (permissionNode.getNodeList() != null && permissionNode.getNodeList().length > 0) {
                iteratePermissions(permissionNode, list);
            }
        }
        return list;
    }

    /**
     * This method is used to retrieve the role count of the system.
     *
     * @return returns the count.
     */
    @Override
    @GET
    @Path("count")
    public Response getRoleCount() {
        try {
            List<String> filteredRoles = getRolesFromUserStore();
            Integer count = filteredRoles.size();
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (MDMAPIException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getErrorMessage()).build();
        }
    }

    private List<String> getRolesFromUserStore() throws MDMAPIException {
        UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
        String[] roles;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting the list of user roles");
            }
            roles = userStoreManager.getRoleNames();

        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the list of user roles.";
            throw new MDMAPIException(msg, e);
        }
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
