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

package org.wso2.carbon.device.mgt.jaxrs.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.jaxrs.beans.PasswordResetWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.OldPasswordResetWrapper;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

/**
 * This class builds Credential modification related Responses
 */
public class CredentialManagementResponseBuilder {

    private static Log log = LogFactory.getLog(CredentialManagementResponseBuilder.class);
    private static String PASSWORD_VALIDATION_REGEX_TAG = "PasswordJavaRegEx";
    private static String PASSWORD_VALIDATION_ERROR_MSG_TAG = "PasswordJavaRegExViolationErrorMsg";

    /**
     * Builds the response to change the password of a user
     * @param username - Username of the user.
     * @param credentials - User credentials
     * @return Response Object
     */
    public static Response buildChangePasswordResponse(String username, OldPasswordResetWrapper credentials) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (!userStoreManager.isExistingUser(username)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("No user found with the username "
                                                                                   + username).build();
            }
            if (credentials == null || credentials.getNewPassword() == null || credentials.getOldPassword() == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Password cannot be empty.")
                        .build();
            }
            if (!validateCredential(credentials.getNewPassword())) {
                String errorMsg = DeviceMgtAPIUtils.getRealmService().getBootstrapRealmConfiguration()
                        .getUserStoreProperty(PASSWORD_VALIDATION_ERROR_MSG_TAG);
                return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
            }
            userStoreManager.updateCredential(username, credentials.getNewPassword(),
                                              credentials.getOldPassword());
            return Response.status(Response.Status.OK).entity("UserImpl password by username: " +
                    username + " was successfully changed.").build();
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).entity("Old password does not match.").build();
        } catch (UnsupportedEncodingException e) {
            String errorMsg = "Could not change the password of the user: " + username +
                    ". The Character Encoding is not supported.";
            log.error(errorMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
        }
    }

    /**
     * Builds the response to reset the password of a user
     * @param username - Username of the user.
     * @param credentials - User credentials
     * @return Response Object
     */
    public static Response buildResetPasswordResponse(String username, PasswordResetWrapper credentials) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (!userStoreManager.isExistingUser(username)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("No user found with the username "
                                                                                   + username).build();
            }
            if (credentials == null || credentials.getNewPassword() == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Password cannot be empty.")
                        .build();
            }
            if (!validateCredential(credentials.getNewPassword())) {
                String errorMsg = DeviceMgtAPIUtils.getRealmService().getBootstrapRealmConfiguration()
                        .getUserStoreProperty(PASSWORD_VALIDATION_ERROR_MSG_TAG);
                return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
            }
            userStoreManager.updateCredentialByAdmin(username, credentials.getNewPassword());
            return Response.status(Response.Status.OK).entity("UserImpl password by username: " +
                    username + " was successfully changed.").build();
        } catch (UserStoreException e) {
            String msg = "ErrorResponse occurred while updating the credentials of user '" + username + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (UnsupportedEncodingException e) {
            String msg = "Could not change the password of the user: " + username +
                    ". The Character Encoding is not supported.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    private static boolean validateCredential(String password)
            throws UserStoreException, UnsupportedEncodingException {
        String passwordValidationRegex = DeviceMgtAPIUtils.getRealmService().getBootstrapRealmConfiguration()
                .getUserStoreProperty(PASSWORD_VALIDATION_REGEX_TAG);
        if (passwordValidationRegex != null) {
            Pattern pattern = Pattern.compile(passwordValidationRegex);
            if (pattern.matcher(password).matches()) {
                return true;
            }
        }
        return false;
    }

}
