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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.jaxrs.beans.UserCredentialWrapper;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;

/**
 * This class builds Credential modification related Responses
 */
public class CredentialManagementResponseBuilder {

    private static Log log = LogFactory.getLog(CredentialManagementResponseBuilder.class);

    /**
     * Builds the response to change the password of a user
     * @param credentials - User credentials
     * @return Response Object
     */
    public static Response buildChangePasswordResponse(UserCredentialWrapper credentials) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            byte[] decodedNewPassword = Base64.decodeBase64(credentials.getNewPassword());
            byte[] decodedOldPassword = Base64.decodeBase64(credentials.getOldPassword());
            userStoreManager.updateCredential(credentials.getUsername(), new String(
                    decodedNewPassword, "UTF-8"), new String(decodedOldPassword, "UTF-8"));
            return Response.status(Response.Status.OK).entity("UserImpl password by username: " +
                    credentials.getUsername() + " was successfully changed.").build();
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).entity("Old password does not match.").build();
        } catch (UnsupportedEncodingException e) {
            String errorMsg = "Could not change the password of the user: " + credentials.getUsername() +
                    ". The Character Encoding is not supported.";
            log.error(errorMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
        }
    }

    /**
     * Builds the response to reset the password of a user
     * @param credentials - User credentials
     * @return Response Object
     */
    public static Response buildResetPasswordResponse(UserCredentialWrapper credentials) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            byte[] decodedNewPassword = Base64.decodeBase64(credentials.getNewPassword());
            userStoreManager.updateCredentialByAdmin(credentials.getUsername(), new String(
                    decodedNewPassword, "UTF-8"));
            return Response.status(Response.Status.CREATED).entity("UserImpl password by username: " +
                    credentials.getUsername() + " was successfully changed.").build();
        } catch (UserStoreException e) {
            String msg = "ErrorResponse occurred while updating the credentials of user '" + credentials.getUsername() + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (UnsupportedEncodingException e) {
            String msg = "Could not change the password of the user: " + credentials.getUsername() +
                    ". The Character Encoding is not supported.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}
