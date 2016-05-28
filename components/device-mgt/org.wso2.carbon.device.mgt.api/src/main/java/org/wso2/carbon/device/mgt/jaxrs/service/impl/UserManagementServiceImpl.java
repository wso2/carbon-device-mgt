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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.EmailMetaInfo;
import org.wso2.carbon.device.mgt.jaxrs.beans.UserCredentialWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.UserWrapper;
import org.wso2.carbon.device.mgt.jaxrs.service.api.UserManagementService;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;
import org.wso2.carbon.device.mgt.jaxrs.util.CredentialManagementResponseBuilder;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserManagementServiceImpl implements UserManagementService {

    private static final String ROLE_EVERYONE = "Internal/everyone";
    private static final Log log = LogFactory.getLog(UserManagementServiceImpl.class);

    @POST
    @Override
    public Response addUser(UserWrapper userWrapper) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (userStoreManager.isExistingUser(userWrapper.getUsername())) {
                // if user already exists
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + userWrapper.getUsername() +
                            " already exists. Therefore, request made to add user was refused.");
                }
                // returning response with bad request state
                return Response.status(Response.Status.CONFLICT).entity("User by username: " + userWrapper.getUsername() +
                        " already exists. Therefore, request made to add user was refused.").build();
            } else {
                String initialUserPassword = this.generateInitialUserPassword();
                Map<String, String> defaultUserClaims =
                        this.buildDefaultUserClaims(userWrapper.getFirstname(), userWrapper.getLastname(),
                                userWrapper.getEmailAddress());
                // calling addUser method of carbon user api
                userStoreManager.addUser(userWrapper.getUsername(), initialUserPassword,
                        userWrapper.getRoles(), defaultUserClaims, null);
                // invite newly added user to enroll device
                this.inviteNewlyAddedUserToEnrollDevice(userWrapper.getUsername(), initialUserPassword);
                // Outputting debug message upon successful addition of user
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + userWrapper.getUsername() + " was successfully added.");
                }
                // returning response with success state
                return Response.status(Response.Status.OK).entity("User by username: " + userWrapper.getUsername() +
                        " was successfully added.").build();
            }
        } catch (UserStoreException e) {
            String msg = "Exception in trying to add user '" + userWrapper.getUsername() + "' to the user store";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while inviting user to enroll the device";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    private Map<String, String> buildDefaultUserClaims(String firstname, String lastname, String emailAddress) {
        Map<String, String> defaultUserClaims = new HashMap<>();
        defaultUserClaims.put(Constants.USER_CLAIM_FIRST_NAME, firstname);
        defaultUserClaims.put(Constants.USER_CLAIM_LAST_NAME, lastname);
        defaultUserClaims.put(Constants.USER_CLAIM_EMAIL_ADDRESS, emailAddress);
        if (log.isDebugEnabled()) {
            log.debug("Default claim map is created for new user: " + defaultUserClaims.toString());
        }
        return defaultUserClaims;
    }

    private String generateInitialUserPassword() {
        int passwordLength = 6;
        //defining the pool of characters to be used for initial password generation
        String lowerCaseCharset = "abcdefghijklmnopqrstuvwxyz";
        String upperCaseCharset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numericCharset = "0123456789";
        Random randomGenerator = new Random();
        String totalCharset = lowerCaseCharset + upperCaseCharset + numericCharset;
        int totalCharsetLength = totalCharset.length();
        StringBuilder initialUserPassword = new StringBuilder();
        for (int i = 0; i < passwordLength; i++) {
            initialUserPassword
                    .append(totalCharset.charAt(randomGenerator.nextInt(totalCharsetLength)));
        }
        if (log.isDebugEnabled()) {
            log.debug("Initial user password is created for new user: " + initialUserPassword);
        }
        return initialUserPassword.toString();
    }

    private void inviteNewlyAddedUserToEnrollDevice(String username,
                                                    String password) throws DeviceManagementException, UserStoreException {
        if (log.isDebugEnabled()) {
            log.debug("Sending invitation mail to user by username: " + username);
        }
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
            tenantDomain = "";
        }
        if (!username.contains("/")) {
            username = "/" + username;
        }
        String[] usernameBits = username.split("/");
        DeviceManagementProviderService deviceManagementProviderService = DeviceMgtAPIUtils.getDeviceManagementService();

        Properties props = new Properties();
        props.setProperty("username", usernameBits[1]);
        props.setProperty("domain-name", tenantDomain);
        props.setProperty("first-name", getClaimValue(username, Constants.USER_CLAIM_FIRST_NAME));
        props.setProperty("password", password);

        String recipient = getClaimValue(username, Constants.USER_CLAIM_EMAIL_ADDRESS);

        EmailMetaInfo metaInfo = new EmailMetaInfo(recipient, props);

        deviceManagementProviderService.sendRegistrationEmail(metaInfo);
    }

    private String getClaimValue(String username, String claimUri) throws UserStoreException {
        UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
        return userStoreManager.getUserClaimValue(username, claimUri, null);
    }

    @GET
    @Path("/{username}")
    @Override
    public Response getUser(@PathParam("username") String username,
                            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (userStoreManager.isExistingUser(username)) {
                UserWrapper user = new UserWrapper();
                user.setUsername(username);
                user.setEmailAddress(getClaimValue(username, Constants.USER_CLAIM_EMAIL_ADDRESS));
                user.setFirstname(getClaimValue(username, Constants.USER_CLAIM_FIRST_NAME));
                user.setLastname(getClaimValue(username, Constants.USER_CLAIM_LAST_NAME));
                // Outputting debug message upon successful retrieval of user
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + username + " was found.");
                }
                return Response.status(Response.Status.OK).entity(user).build();
            } else {
                // Outputting debug message upon trying to remove non-existing user
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + username + " does not exist.");
                }
                // returning response with bad request state
                return Response.status(Response.Status.NOT_FOUND).entity(
                        "User by username: " + username + " does not exist").build();
            }
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving information of the user '" + username + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Path("/{username}")
    @Override
    public Response updateUser(@PathParam("username") String username, UserWrapper userWrapper) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (userStoreManager.isExistingUser(userWrapper.getUsername())) {
                Map<String, String> defaultUserClaims =
                        this.buildDefaultUserClaims(userWrapper.getFirstname(), userWrapper.getLastname(),
                                userWrapper.getEmailAddress());
                if (StringUtils.isNotEmpty(userWrapper.getPassword())) {
                    // Decoding Base64 encoded password
                    byte[] decodedBytes = Base64.decodeBase64(userWrapper.getPassword());
                    userStoreManager.updateCredentialByAdmin(userWrapper.getUsername(),
                            new String(decodedBytes, "UTF-8"));
                    log.debug("User credential of username: " + userWrapper.getUsername() + " has been changed");
                }
                List<String> currentRoles = this.getFilteredRoles(userStoreManager, userWrapper.getUsername());
                List<String> newRoles = Arrays.asList(userWrapper.getRoles());

                List<String> rolesToAdd = new ArrayList<>(newRoles);
                List<String> rolesToDelete = new ArrayList<>();

                for (String role : currentRoles) {
                    if (newRoles.contains(role)) {
                        rolesToAdd.remove(role);
                    } else {
                        rolesToDelete.add(role);
                    }
                }
                rolesToDelete.remove(ROLE_EVERYONE);
                userStoreManager.updateRoleListOfUser(userWrapper.getUsername(),
                        rolesToDelete.toArray(new String[rolesToDelete.size()]),
                        rolesToAdd.toArray(new String[rolesToAdd.size()]));
                userStoreManager.setUserClaimValues(userWrapper.getUsername(), defaultUserClaims, null);
                // Outputting debug message upon successful addition of user
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + userWrapper.getUsername() + " was successfully updated.");
                }
                // returning response with success state
                return Response.status(Response.Status.CREATED).entity("User by username '" + userWrapper.getUsername() +
                        "' was successfully updated.").build();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + userWrapper.getUsername() +
                            " doesn't exists. Therefore, request made to update user was refused.");
                }
                return Response.status(Response.Status.CONFLICT).entity("User by username: " +
                        userWrapper.getUsername() + " doesn't  exists. Therefore, request made to update user was " +
                        "refused.").build();
            }
        } catch (UserStoreException | UnsupportedEncodingException e) {
            String msg = "Exception in trying to update user by username: " + userWrapper.getUsername();
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    private List<String> getFilteredRoles(UserStoreManager userStoreManager, String username) {
        String[] roleListOfUser = new String[0];
        try {
            roleListOfUser = userStoreManager.getRoleListOfUser(username);
        } catch (UserStoreException e) {
            e.printStackTrace();
        }
        List<String> filteredRoles = new ArrayList<>();
        for (String role : roleListOfUser) {
            if (!(role.startsWith("Internal/") || role.startsWith("Authentication/"))) {
                filteredRoles.add(role);
            }
        }
        return filteredRoles;
    }

    @DELETE
    @Path("/{username}")
    @Override
    public Response removeUser(@PathParam("username") String username) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (userStoreManager.isExistingUser(username)) {
                // if user already exists, trying to remove user
                userStoreManager.deleteUser(username);
                // Outputting debug message upon successful removal of user
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + username + " was successfully removed.");
                }
                // returning response with success state
                return Response.status(Response.Status.OK).entity("User by username: " + username +
                        " was successfully removed.").build();
            } else {
                // Outputting debug message upon trying to remove non-existing user
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + username + " does not exist for removal.");
                }
                // returning response with bad request state
                return Response.status(Response.Status.NOT_FOUND).entity("User by username: " + username +
                        " does not exist for removal.").build();
            }
        } catch (UserStoreException e) {
            String msg = "Exception in trying to remove user by username: " + username;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Path("/{username}/roles")
    @Override
    public Response getRolesOfUser(@PathParam("username") String username) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (userStoreManager.isExistingUser(username)) {
                return Response.status(Response.Status.OK).entity(Collections.singletonList(
                        getFilteredRoles(userStoreManager, username))).build();
            } else {
                // Outputting debug message upon trying to remove non-existing user
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + username + " does not exist for role retrieval.");
                }
                return Response.status(Response.Status.NOT_FOUND).entity("User by username: " + username +
                        " does not exist for role retrieval.").build();
            }
        } catch (UserStoreException e) {
            String msg = "Exception in trying to retrieve roles for user by username: " + username;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    public Response getUsers(@QueryParam("filter") String filter, @HeaderParam("If-Modified-Since") String timestamp,
                             @QueryParam("offset") int offset,
                             @QueryParam("limit") int limit) {
        if (log.isDebugEnabled()) {
            log.debug("Getting the list of users with all user-related information");
        }
        List<UserWrapper> userList;
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            String[] users = userStoreManager.listUsers("*", -1);
            userList = new ArrayList<>(users.length);
            UserWrapper user;
            for (String username : users) {
                user = new UserWrapper();
                user.setUsername(username);
                user.setEmailAddress(getClaimValue(username, Constants.USER_CLAIM_EMAIL_ADDRESS));
                user.setFirstname(getClaimValue(username, Constants.USER_CLAIM_FIRST_NAME));
                user.setLastname(getClaimValue(username, Constants.USER_CLAIM_LAST_NAME));
                userList.add(user);
            }
            if (userList.size() <= 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("No user is available to be retrieved").build();
            }
            return Response.status(Response.Status.OK).entity(userList).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the list of users";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/usernames")
    @Override
    public Response getUserNames(@QueryParam("filter") String filter, @HeaderParam("If-Modified-Since") String timestamp,
                                 @QueryParam("offset") int offset, @QueryParam("limit") int limit) {
        if (log.isDebugEnabled()) {
            log.debug("Getting the list of users with all user-related information using the filter : " + filter);
        }
        List<UserWrapper> userList;
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            String[] users = userStoreManager.listUsers(filter + "*", -1);
            userList = new ArrayList<>(users.length);
            UserWrapper user;
            for (String username : users) {
                user = new UserWrapper();
                user.setUsername(username);
                user.setEmailAddress(getClaimValue(username, Constants.USER_CLAIM_EMAIL_ADDRESS));
                user.setFirstname(getClaimValue(username, Constants.USER_CLAIM_FIRST_NAME));
                user.setLastname(getClaimValue(username, Constants.USER_CLAIM_LAST_NAME));
                userList.add(user);
            }
            if (userList.size() <= 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("No user is available to be retrieved").build();
            }
            return Response.status(Response.Status.OK).entity(userList).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the list of users using the filter : " + filter;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Path("/{username}/credentials")
    @Override
    public Response resetPassword(@PathParam("username") String username, UserCredentialWrapper credentials) {
        return CredentialManagementResponseBuilder.buildChangePasswordResponse(credentials);
    }

}
