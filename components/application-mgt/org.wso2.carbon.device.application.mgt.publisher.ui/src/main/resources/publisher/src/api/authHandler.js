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
        let login_promise = Axios.post("https://localhost:9443/auth/application-mgt/v1.0/auth/login?userName=admin&password=admin",
            null, {headers: headers});

        login_promise.then(response => {
                console.log(response);
                const userName = response.data.userName;
                const validityPeriod = response.data.expires_in; // In seconds
                const WSO2_IOT_TOKEN = response.data.access_token;
                const refreshToken = response.data.refresh_token;
                const clientId = response.data.application_info[0].consumerKey;
                const clientSecret = response.data.application_info[0].consumerSecret;

                const user = new User(userName, clientId, clientSecret, validityPeriod);
                console.log(user);
                user.setAuthToken(WSO2_IOT_TOKEN, validityPeriod);
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
        localStorage.setItem(Constants.userConstants.WSO2_USER, JSON.stringify(user.toJson()));
        /* TODO: IMHO it's better to get this key (`wso2_user`) from configs */
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

    logout() {

    }

    /**
     * Checks whether the access token is expired.
     * @return boolean: True if expired. False otherwise.
     * */
    static isTokenExpired() {
        const userData = AuthHandler.getUser().getAuthToken();
        return (Date.now() - userData._createdTime) > userData._expires;
    }
}

export default AuthHandler;
