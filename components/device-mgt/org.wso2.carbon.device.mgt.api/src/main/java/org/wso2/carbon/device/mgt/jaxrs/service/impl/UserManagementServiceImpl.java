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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.EmailMetaInfo;
import org.wso2.carbon.device.mgt.jaxrs.beans.*;
import org.wso2.carbon.device.mgt.jaxrs.service.api.UserManagementService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;
import org.wso2.carbon.device.mgt.jaxrs.util.CredentialManagementResponseBuilder;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.*;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserManagementServiceImpl implements UserManagementService {

    private static final String ROLE_EVERYONE = "Internal/everyone";
    private static final String API_BASE_PATH = "/users";
    private static final Log log = LogFactory.getLog(UserManagementServiceImpl.class);

    @POST
    @Override
    public Response addUser(UserInfo userInfo) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (userStoreManager.isExistingUser(userInfo.getUsername())) {
                // if user already exists
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + userInfo.getUsername() +
                            " already exists. Therefore, request made to add user was refused.");
                }
                // returning response with bad request state
                return Response.status(Response.Status.CONFLICT).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("User by username: " +
                                userInfo.getUsername() + " already exists. Therefore, request made to add user " +
                                "was refused.").build()).build();
            }

            String initialUserPassword = this.generateInitialUserPassword();
            Map<String, String> defaultUserClaims =
                    this.buildDefaultUserClaims(userInfo.getFirstname(), userInfo.getLastname(),
                            userInfo.getEmailAddress());
            // calling addUser method of carbon user api
            userStoreManager.addUser(userInfo.getUsername(), initialUserPassword,
                    userInfo.getRoles(), defaultUserClaims, null);
            // Outputting debug message upon successful addition of user
            if (log.isDebugEnabled()) {
                log.debug("User '" + userInfo.getUsername() + "' has successfully been added.");
            }

            BasicUserInfo createdUserInfo = this.getBasicUserInfo(userInfo.getUsername());
            // Outputting debug message upon successful retrieval of user
            if (log.isDebugEnabled()) {
                log.debug("User by username: " + userInfo.getUsername() + " was found.");
            }
            return Response.created(new URI(API_BASE_PATH + "/" + userInfo.getUsername())).entity(
                    createdUserInfo).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while trying to add user '" + userInfo.getUsername() + "' to the " +
                    "underlying user management system";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (URISyntaxException e) {
            String msg = "Error occurred while composing the location URI, which represents information of the " +
                    "newly created user '" + userInfo.getUsername() + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/{username}")
    @Override
    public Response getUser(@PathParam("username") String username,
                            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (!userStoreManager.isExistingUser(username)) {
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + username + " does not exist.");
                }
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(
                                "User doesn't exist.").build()).build();
            }

            BasicUserInfo user = this.getBasicUserInfo(username);
            return Response.status(Response.Status.OK).entity(user).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving information of the user '" + username + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @PUT
    @Path("/{username}")
    @Override
    public Response updateUser(@PathParam("username") String username, UserInfo userInfo) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (!userStoreManager.isExistingUser(userInfo.getUsername())) {
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + userInfo.getUsername() +
                            " doesn't exists. Therefore, request made to update user was refused.");
                }
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("User by username: " +
                                userInfo.getUsername() + " doesn't  exist.").build()).build();
            }

            Map<String, String> defaultUserClaims =
                    this.buildDefaultUserClaims(userInfo.getFirstname(), userInfo.getLastname(),
                            userInfo.getEmailAddress());
            if (StringUtils.isNotEmpty(userInfo.getPassword())) {
                // Decoding Base64 encoded password
                userStoreManager.updateCredentialByAdmin(userInfo.getUsername(),
                        userInfo.getPassword());
                log.debug("User credential of username: " + userInfo.getUsername() + " has been changed");
            }
            List<String> currentRoles = this.getFilteredRoles(userStoreManager, userInfo.getUsername());
            List<String> newRoles = Arrays.asList(userInfo.getRoles());

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
            userStoreManager.updateRoleListOfUser(userInfo.getUsername(),
                    rolesToDelete.toArray(new String[rolesToDelete.size()]),
                    rolesToAdd.toArray(new String[rolesToAdd.size()]));
            userStoreManager.setUserClaimValues(userInfo.getUsername(), defaultUserClaims, null);
            // Outputting debug message upon successful addition of user
            if (log.isDebugEnabled()) {
                log.debug("User by username: " + userInfo.getUsername() + " was successfully updated.");
            }

            BasicUserInfo updatedUserInfo = this.getBasicUserInfo(username);
            return Response.ok().entity(updatedUserInfo).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while trying to update user '" + userInfo.getUsername() + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
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
            if (!userStoreManager.isExistingUser(username)) {
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + username + " does not exist for removal.");
                }
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("User '" +
                                username + "' does not exist for removal.").build()).build();
            }

            userStoreManager.deleteUser(username);
            if (log.isDebugEnabled()) {
                log.debug("User '" + username + "' was successfully removed.");
            }
            return Response.status(Response.Status.OK).build();
        } catch (UserStoreException e) {
            String msg = "Exception in trying to remove user by username: " + username;
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/{username}/roles")
    @Override
    public Response getRolesOfUser(@PathParam("username") String username) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (!userStoreManager.isExistingUser(username)) {
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + username + " does not exist for role retrieval.");
                }
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("User by username: " + username +
                                " does not exist for role retrieval.").build()).build();
            }

            RoleList result = new RoleList();
            result.setList(getFilteredRoles(userStoreManager, username));
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while trying to retrieve roles of the user '" + username + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
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
        List<BasicUserInfo> userList, offsetList;
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        String appliedFilter = ((filter == null) || filter.isEmpty() ? "*" : filter);
        int appliedLimit = (limit <= 0) ? -1 : (limit + offset);

        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();

            //As the listUsers function accepts limit only to accommodate offset we are passing offset + limit
            String[] users = userStoreManager.listUsers(appliedFilter + "*", appliedLimit);
            userList = new ArrayList<>(users.length);
            BasicUserInfo user;
            for (String username : users) {
                user = new BasicUserInfo();
                user.setUsername(username);
                user.setEmailAddress(getClaimValue(username, Constants.USER_CLAIM_EMAIL_ADDRESS));
                user.setFirstname(getClaimValue(username, Constants.USER_CLAIM_FIRST_NAME));
                user.setLastname(getClaimValue(username, Constants.USER_CLAIM_LAST_NAME));
                userList.add(user);
            }

            if (offset <= userList.size()) {
                offsetList = userList.subList(offset, userList.size());
            } else {
                offsetList = new ArrayList<>();
            }
            BasicUserInfoList result = new BasicUserInfoList();
            result.setList(offsetList);
            result.setCount(offsetList.size());

            return Response.status(Response.Status.OK).entity(result).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the list of users.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/search/usernames")
    @Override
    public Response getUserNames(@QueryParam("filter") String filter, @HeaderParam("If-Modified-Since") String timestamp,
                                 @QueryParam("offset") int offset, @QueryParam("limit") int limit) {
        if (log.isDebugEnabled()) {
            log.debug("Getting the list of users with all user-related information using the filter : " + filter);
        }
        List<UserInfo> userList;
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            String[] users = userStoreManager.listUsers(filter + "*", -1);
            userList = new ArrayList<>(users.length);
            UserInfo user;
            for (String username : users) {
                user = new UserInfo();
                user.setUsername(username);
                user.setEmailAddress(getClaimValue(username, Constants.USER_CLAIM_EMAIL_ADDRESS));
                user.setFirstname(getClaimValue(username, Constants.USER_CLAIM_FIRST_NAME));
                user.setLastname(getClaimValue(username, Constants.USER_CLAIM_LAST_NAME));
                userList.add(user);
            }
//            if (userList.size() <= 0) {
//                return Response.status(Response.Status.NOT_FOUND).entity("No user is available to be retrieved").build();
//            }
            return Response.status(Response.Status.OK).entity(userList).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the list of users using the filter : " + filter;
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @PUT
    @Path("/{username}/credentials")
    @Override
    public Response resetPassword(@PathParam("username") String username, OldPasswordResetWrapper credentials) {
        return CredentialManagementResponseBuilder.buildChangePasswordResponse(username, credentials);
    }

    /**
     * Method used to send an invitation email to a existing user to enroll a device.
     *
     * @param usernames Username list of the users to be invited
     */
    @POST
    @Path("send-invitation")
    @Produces({MediaType.APPLICATION_JSON})
    public Response inviteExistingUsersToEnrollDevice(List<String> usernames) {
        if (log.isDebugEnabled()) {
            log.debug("Sending enrollment invitation mail to existing user.");
        }
        DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            for (String username : usernames) {
                String recipient = getClaimValue(username, Constants.USER_CLAIM_EMAIL_ADDRESS);

                Properties props = new Properties();
                props.setProperty("first-name", getClaimValue(username, Constants.USER_CLAIM_FIRST_NAME));
                props.setProperty("username", username);

                EmailMetaInfo metaInfo = new EmailMetaInfo(recipient, props);
                dms.sendEnrolmentInvitation(metaInfo);
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while inviting user to enrol their device";
            log.error(msg, e);
        } catch (UserStoreException e) {
            String msg = "Error occurred while getting claim values to invite user";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity("Invitation mails have been sent.").build();
    }


    private Map<String, String> buildDefaultUserClaims(String firstName, String lastName, String emailAddress) {
        Map<String, String> defaultUserClaims = new HashMap<>();
        defaultUserClaims.put(Constants.USER_CLAIM_FIRST_NAME, firstName);
        defaultUserClaims.put(Constants.USER_CLAIM_LAST_NAME, lastName);
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
        SecureRandom randomGenerator = new SecureRandom();
        String totalCharset = lowerCaseCharset + upperCaseCharset + numericCharset;
        int totalCharsetLength = totalCharset.length();
        StringBuilder initialUserPassword = new StringBuilder();
        for (int i = 0; i < passwordLength; i++) {
            initialUserPassword.append(
                    totalCharset.charAt(randomGenerator.nextInt(totalCharsetLength)));
        }
        if (log.isDebugEnabled()) {
            log.debug("Initial user password is created for new user: " + initialUserPassword);
        }
        return initialUserPassword.toString();
    }

    private BasicUserInfo getBasicUserInfo(String username) throws UserStoreException {
        BasicUserInfo userInfo = new BasicUserInfo();
        userInfo.setUsername(username);
        userInfo.setEmailAddress(getClaimValue(username, Constants.USER_CLAIM_EMAIL_ADDRESS));
        userInfo.setFirstname(getClaimValue(username, Constants.USER_CLAIM_FIRST_NAME));
        userInfo.setLastname(getClaimValue(username, Constants.USER_CLAIM_LAST_NAME));
        return userInfo;
    }

    private String getClaimValue(String username, String claimUri) throws UserStoreException {
        UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
        return userStoreManager.getUserClaimValue(username, claimUri, null);
    }

}
