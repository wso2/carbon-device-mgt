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
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleInfo;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleList;
import org.wso2.carbon.device.mgt.jaxrs.service.api.RoleManagementService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.FilteringUtil;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.device.mgt.jaxrs.util.SetReferenceTransformer;
import org.wso2.carbon.user.api.*;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.mgt.UserRealmProxy;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

import static org.wso2.carbon.device.mgt.jaxrs.util.Constants.PRIMARY_USER_STORE;

@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleManagementServiceImpl implements RoleManagementService {

    private static final String API_BASE_PATH = "/roles";
    private static final Log log = LogFactory.getLog(RoleManagementServiceImpl.class);

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
        if (userStore == null || "".equals(userStore)) {
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
    @Path("/filter/{prefix}")
    @Override
    public Response getFilteredRoles(
            @PathParam("prefix") String prefix,
            @QueryParam("filter") String filter,
            @QueryParam("user-store") String userStore,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset, @QueryParam("limit") int limit) {
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        List<String> finalRoleList;
        RoleList targetRoles = new RoleList();

        //if user store is null set it to primary
        if (userStore == null || "".equals(userStore)) {
            userStore = PRIMARY_USER_STORE;
        }

        try {

            //Get the total role count that matches the given filter
            List<String> filteredRoles = getRolesFromUserStore(filter, userStore);
            finalRoleList = new ArrayList<String>();

            filteredRoles = FilteringUtil.getFilteredList(getRolesFromUserStore(filter, userStore), offset, limit);
            for (String rolename : filteredRoles){
                if (rolename.startsWith(prefix)){
                    finalRoleList.add(rolename);
                }
            }
            targetRoles.setCount(finalRoleList.size());
            targetRoles.setList(finalRoleList);

            return Response.ok().entity(targetRoles).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving roles from the underlying user stores";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/{roleName}/permissions")
    @Override
    public Response getPermissionsOfRole(@PathParam("roleName") String roleName,
                                         @QueryParam("user-store") String userStoreName,
                                         @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        if (userStoreName != null && !userStoreName.isEmpty()) {
            roleName = userStoreName + "/" + roleName;
        }
        RequestValidationUtil.validateRoleName(roleName);
        try {
            final UserRealm userRealm = DeviceMgtAPIUtils.getUserRealm();
            if (!userRealm.getUserStoreManager().isExistingRole(roleName)) {
                return Response.status(404).entity(new ErrorResponse.ErrorResponseBuilder().setMessage(
                        "No role exists with the name '" + roleName + "'").build()).build();
            }

            final UIPermissionNode rolePermissions = this.getUIPermissionNode(roleName, userRealm);
            if (rolePermissions == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No permissions found for the role '" + roleName + "'");
                }
            }
            return Response.status(Response.Status.OK).entity(rolePermissions).build();
        } catch (UserAdminException e) {
            String msg = "Error occurred while retrieving the permissions of role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the underlying user realm attached to the " +
                    "current logged in user";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    private UIPermissionNode getAllRolePermissions(String roleName, UserRealm userRealm) throws UserAdminException {
        org.wso2.carbon.user.core.UserRealm userRealmCore = null;
        if (userRealm instanceof org.wso2.carbon.user.core.UserRealm) {
            userRealmCore = (org.wso2.carbon.user.core.UserRealm) userRealm;
        }
        final UserRealmProxy userRealmProxy = new UserRealmProxy(userRealmCore);
        final UIPermissionNode rolePermissions =
                userRealmProxy.getRolePermissions(roleName, MultitenantConstants.SUPER_TENANT_ID);
        return rolePermissions;
    }

    private UIPermissionNode getUIPermissionNode(String roleName, UserRealm userRealm)
            throws UserAdminException {
        org.wso2.carbon.user.core.UserRealm userRealmCore = null;
        if (userRealm instanceof org.wso2.carbon.user.core.UserRealm) {
            userRealmCore = (org.wso2.carbon.user.core.UserRealm) userRealm;
        }
        final UserRealmProxy userRealmProxy = new UserRealmProxy(userRealmCore);
        final UIPermissionNode rolePermissions =
                userRealmProxy.getRolePermissions(roleName, MultitenantConstants.SUPER_TENANT_ID);
        UIPermissionNode[] deviceMgtPermissions = new UIPermissionNode[4];

        for (UIPermissionNode permissionNode : rolePermissions.getNodeList()) {
            if (permissionNode.getResourcePath().equals("/permission/admin")) {
                for (UIPermissionNode node : permissionNode.getNodeList()) {
                    if (node.getResourcePath().equals("/permission/admin/device-mgt")) {
                        deviceMgtPermissions[0] = node;
                    } else if (node.getResourcePath().equals("/permission/admin/login")) {
                        deviceMgtPermissions[1] = node;
                    } else if (node.getResourcePath().equals("/permission/admin/manage")) {
                        // Adding permissions related to app-store in emm-console
                        for (UIPermissionNode subNode : node.getNodeList()) {
                            if (subNode.getResourcePath().equals("/permission/admin/manage/mobileapp")) {
                                deviceMgtPermissions[2] = subNode;
                            } else if (subNode.getResourcePath().equals("/permission/admin/manage/webapp")) {
                                deviceMgtPermissions[3] = subNode;
                            }
                        }
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
    public Response getRole(@PathParam("roleName") String roleName, @QueryParam("user-store") String userStoreName,
                            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        if (log.isDebugEnabled()) {
            log.debug("Getting the list of user roles");
        }
        if (userStoreName != null && !userStoreName.isEmpty()) {
            roleName = userStoreName + "/" + roleName;
        }
        RequestValidationUtil.validateRoleName(roleName);
        RoleInfo roleInfo = new RoleInfo();
        try {
            final UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            final UserRealm userRealm = DeviceMgtAPIUtils.getUserRealm();
            if (!userStoreManager.isExistingRole(roleName)) {
                return Response.status(404).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("No role exists with the name '" +
                                roleName + "'").build()).build();
            }
            roleInfo.setRoleName(roleName);
            roleInfo.setUsers(userStoreManager.getUserListOfRole(roleName));
            // Get the permission nodes and hand picking only device management and login perms
            final UIPermissionNode rolePermissions = this.getUIPermissionNode(roleName, userRealm);
            List<String> permList = new ArrayList<>();
            this.iteratePermissions(rolePermissions, permList);
            roleInfo.setPermissionList(rolePermissions);
            String[] permListAr = new String[permList.size()];
            roleInfo.setPermissions(permList.toArray(permListAr));

            return Response.status(Response.Status.OK).entity(roleInfo).build();
        } catch (UserStoreException | UserAdminException e) {
            String msg = "Error occurred while retrieving the user role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
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


    private List<String> getAuthorizedPermissions(UIPermissionNode uiPermissionNode, List<String> list) {
        for (UIPermissionNode permissionNode : uiPermissionNode.getNodeList()) {
            if (permissionNode.isSelected()) {
                list.add(permissionNode.getResourcePath());
            }
            if (permissionNode.getNodeList() != null && permissionNode.getNodeList().length > 0) {
                getAuthorizedPermissions(permissionNode, list);
            }
        }
        return list;
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
            Permission[] permissions = null;
            if (roleInfo.getPermissions() != null && roleInfo.getPermissions().length > 0) {
                permissions = new Permission[roleInfo.getPermissions().length];
                for (int i = 0; i < permissions.length; i++) {
                    String permission = roleInfo.getPermissions()[i];
                    permissions[i] = new Permission(permission, CarbonConstants.UI_PERMISSION_ACTION);
                }
            }
            userStoreManager.addRole(roleInfo.getRoleName(), roleInfo.getUsers(), permissions);

            //TODO fix what's returned in the entity
            return Response.created(new URI(API_BASE_PATH + "/" + URLEncoder.encode(roleInfo.getRoleName(), "UTF-8"))).
                    entity("Role '" + roleInfo.getRoleName() + "' has " + "successfully been"
                            + " added").build();
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
        } catch (UnsupportedEncodingException e) {
            String msg = "Error occurred while encoding role name";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @POST
    @Path("/create-combined-role/{roleName}")
    @Override
    public Response addCombinedRole(List<String> roles, @PathParam("roleName") String roleName,
                                    @QueryParam("user-store") String userStoreName) {
        if (userStoreName != null && !userStoreName.isEmpty()) {
            roleName = userStoreName + "/" + roleName;
        }
        if (roles.size() < 2) {
            return Response.status(400).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage("Combining Roles requires at least two roles.")
                            .build()
            ).build();
        }
        for (String role : roles) {
            RequestValidationUtil.validateRoleName(role);
        }
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (log.isDebugEnabled()) {
                log.debug("Persisting the role in the underlying user store");
            }

            HashSet<Permission> permsSet = new HashSet<>();
            try {
                for (String role : roles) {
                    mergePermissions(new UIPermissionNode[]{getRolePermissions(role)}, permsSet);
                }
            } catch (IllegalArgumentException e) {
                return Response.status(404).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(e.getMessage()).build()
                ).build();
            }

            Permission[] permissions = permsSet.toArray(new Permission[permsSet.size()]);
            userStoreManager.addRole(roleName, new String[0], permissions);

            //TODO fix what's returned in the entity
            return Response.created(new URI(API_BASE_PATH + "/" + URLEncoder.encode(roleName, "UTF-8"))).
                    entity("Role '" + roleName + "' has " + "successfully been"
                            + " added").build();
        } catch (UserAdminException e) {
            String msg = "Error occurred while retrieving the permissions of role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while adding role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (URISyntaxException e) {
            String msg = "Error occurred while composing the URI at which the information of the newly created role " +
                    "can be retrieved";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (UnsupportedEncodingException e) {
            String msg = "Error occurred while encoding role name";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @PUT
    @Path("/{roleName}")
    @Override
    public Response updateRole(@PathParam("roleName") String roleName, RoleInfo roleInfo,
                               @QueryParam("user-store") String userStoreName) {
        if (userStoreName != null && !userStoreName.isEmpty()) {
            roleName = userStoreName + "/" + roleName;
        }
        RequestValidationUtil.validateRoleName(roleName);
        RequestValidationUtil.validateRoleDetails(roleInfo);
        try {
            final UserRealm userRealm = DeviceMgtAPIUtils.getUserRealm();
            final UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            if (!userStoreManager.isExistingRole(roleName)) {
                return Response.status(404).entity(
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

            if (roleInfo.getPermissions() != null) {
                // Get all role permissions
                final UIPermissionNode rolePermissions = this.getAllRolePermissions(roleName, userRealm);
                List<String> permissions = new ArrayList<String>();
                final UIPermissionNode emmRolePermissions = (UIPermissionNode) this.getRolePermissions(roleName);
                List<String> emmConsolePermissions = new ArrayList<String>();
                this.getAuthorizedPermissions(emmRolePermissions, emmConsolePermissions);
                emmConsolePermissions.removeAll(new ArrayList<String>(Arrays.asList(roleInfo.getPermissions())));
                this.getAuthorizedPermissions(rolePermissions, permissions);
                for (String permission : roleInfo.getPermissions()) {
                    permissions.add(permission);
                }
                permissions.removeAll(emmConsolePermissions);
                String[] allApplicablePerms = new String[permissions.size()];
                allApplicablePerms = permissions.toArray(allApplicablePerms);
                roleInfo.setPermissions(allApplicablePerms);

                // Delete all authorizations for the current role before authorizing the permission tree
                authorizationManager.clearRoleAuthorization(roleName);
                if (roleInfo.getPermissions().length > 0) {
                    for (int i = 0; i < roleInfo.getPermissions().length; i++) {
                        String permission = roleInfo.getPermissions()[i];
                        authorizationManager.authorizeRole(roleName, permission, CarbonConstants.UI_PERMISSION_ACTION);
                    }
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
        } catch (UserAdminException e) {
            String msg = "Error occurred while updating permissions of the role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @DELETE
    @Path("/{roleName}")
    @Override
    public Response deleteRole(@PathParam("roleName") String roleName, @QueryParam("user-store") String userStoreName) {
        if (userStoreName != null && !userStoreName.isEmpty()) {
            roleName = userStoreName + "/" + roleName;
        }
        RequestValidationUtil.validateRoleName(roleName);
        try {
            final UserRealm userRealm = DeviceMgtAPIUtils.getUserRealm();
            final UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            if (!userStoreManager.isExistingRole(roleName)) {
                return Response.status(404).entity(
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

            return Response.status(Response.Status.OK).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while deleting the role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @PUT
    @Path("/{roleName}/users")
    @Override
    public Response updateUsersOfRole(@PathParam("roleName") String roleName,
                                      @QueryParam("user-store") String userStoreName, List<String> users) {
        if (userStoreName != null && !userStoreName.isEmpty()) {
            roleName = userStoreName + "/" + roleName;
        }
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
                    "successfully been updated with the user list")
                    .build();
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
        if (userStore.equals("all")) {
            roles = userStoreManager.getRoleNames("*", -1, false, true, true);
        } else {
            roles = userStoreManager.getRoleNames(userStore + "/*", -1, false, true, true);
        }
        // removing all internal roles, roles created for Service-providers and application related roles.
        List<String> filteredRoles = new ArrayList<>();
        for (String role : roles) {
            if (!(role.startsWith("Internal/") || role.startsWith("Authentication/") || role.startsWith(
                    "Application/"))) {
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

    private Set<Permission> mergePermissions(UIPermissionNode[] permissionNodes, Set<Permission> permissions)
            throws UserStoreException, UserAdminException {
        for (UIPermissionNode permissionNode : permissionNodes) {
            if (permissionNode.getNodeList().length > 0) {
                mergePermissions(permissionNode.getNodeList(), permissions);
            }
            if (permissionNode.isSelected()) {
                permissions.add(new Permission(permissionNode.getResourcePath(), CarbonConstants.UI_PERMISSION_ACTION));
            }
        }
        return permissions;
    }

    private UIPermissionNode getRolePermissions(String roleName) throws UserStoreException, UserAdminException {
        final UserRealm userRealm = DeviceMgtAPIUtils.getUserRealm();
        if (!userRealm.getUserStoreManager().isExistingRole(roleName)) {
            throw new IllegalArgumentException("No role exists with the name '" + roleName + "'");
        }

        final UIPermissionNode rolePermissions = this.getUIPermissionNode(roleName, userRealm);
        if (rolePermissions == null) {
            if (log.isDebugEnabled()) {
                log.debug("No permissions found for the role '" + roleName + "'");
            }
        }
        return rolePermissions;
    }
}
