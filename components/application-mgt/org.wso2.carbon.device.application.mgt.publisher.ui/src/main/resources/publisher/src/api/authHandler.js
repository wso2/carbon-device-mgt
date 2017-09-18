/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
'use strict';

import Axios from 'axios';
import User from './data/user';
import Utils from './data/utils';
import Constants from "../common/constants";

/**
 * Handles all tasks related to Authentication and Authorization.
 * Generate access tokens, verify the user has necessary permissions etc.
 * */
class AuthHandler {

    /**
     * Sends a request to the auth handler endpoint (auth/application-mgt/v1.0/auth/login) and generate token pair.
     * @param userName: The user name of the user.
     * @param password: The user password.
     * @return Object: The response object from the axios post.
     * */
    static login(userName, password) {
        const headers = {"Content-type": "application/json"};
        let login_promise =
                Axios.post(Constants.userConstants.LOGIN_URL+"?userName=" + userName+ "&password=" + password,
                null, {headers: headers});

        login_promise.then(response => {
                console.log(response);
                const userName = response.data.userName;
                const validityPeriod = response.data.expiresIn; // In seconds
                const WSO2_IOT_TOKEN = response.data.accessToken;
                const refreshToken = response.data.refreshToken;
                const clientId = response.data.application_info[0].consumerKey;
                const clientSecret = response.data.application_info[0].consumerSecret;

                const user = new User(userName, clientId, clientSecret, validityPeriod);
                console.log(user);
                user.setAuthToken(WSO2_IOT_TOKEN, validityPeriod);
                let expiresIn = Date.now() + (validityPeriod * 1000);
                localStorage.setItem("expiresIn", expiresIn);
                AuthHandler.setUser(user);
            }
        );
        return login_promise;
    };

    /**
     * Persists the user object in browser's local storage.
     * @param user: The user object.
     * */
    static setUser(user) {
        if (!user instanceof User) {
            throw "Invalid user object";
        }
        user.created = Date.now();
        localStorage.setItem(Constants.userConstants.WSO2_USER, JSON.stringify(user.toJson()));
        /* TODO: IMHO it's better to get this key (`wso2_user`) from configs */
    }

    static unauthorizedErrorHandler(error_response) {
        if (error_response.status !== 401) { /* Skip unrelated response code to handle in unauthorizedErrorHandler*/
            throw error_response;
            /* re throwing the error since we don't handle it here and propagate to downstream error handlers in catch chain*/
        }
        let message = "The session has expired" + ".<br/> You will be redirect to the login page ...";
        if (true) {
            alert(message);
        } else {
            throw error_response;
        }
    }

    /**
     * Get the logged in user.
     * @return User: The logged in user object.
     * */
    static getUser() {
        const userData = localStorage.getItem(Constants.userConstants.WSO2_USER);
        const partialToken = Utils.getCookie(Constants.userConstants.PARTIAL_TOKEN);

        if (!(userData && partialToken)) {
            return null;
        }
        return User.fromJson(JSON.parse(userData));
    }

    isLoggedIn() {

    }

    static logout() {
        const user = AuthHandler.getUser();
        const clientId = user.getClientId();
        const clientSecret = user.getClientSecret();
        const token = user.getAuthToken();
        const headers = {"Content-type": "application/json"};

        let login_promise = Axios.post(Constants.userConstants.LOGOUT_URL+"?token=" + token + "&clientId=" + clientId
            + "&clientSecret=" + clientSecret,
            null, {headers: headers});
        login_promise.then(
            (response) => {
                Utils.delete_cookie(Constants.userConstants.PARTIAL_TOKEN);
                localStorage.removeItem(Constants.userConstants.WSO2_USER);
                window.location = "/";
            }
        ).catch(
            (err) => {
                AuthHandler.unauthorizedErrorHandler(err);
            }
        )
    }

    /**
     * Checks whether the access token is expired.
     * @return boolean: True if expired. False otherwise.
     * */
    static isTokenExpired() {
        const expiresIn = localStorage.getItem("expiresIn");
        return (expiresIn < Date.now());
    }

    static createAuthenticationHeaders(contentType) {
        return {
            "Authorization": "Bearer " + AuthHandler.getUser().getAuthToken(),
            "Content-Type": contentType,
        };

    };
}

export default AuthHandler;
