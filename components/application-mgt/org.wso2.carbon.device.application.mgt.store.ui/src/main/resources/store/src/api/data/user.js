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
"use strict";

import Utils from './utils'
import Constants  from '../../common/constants';
/**
 * Represent an user logged in to the application, There will be allays one user per session and
 * this user details will be persist in browser localstorage.
 */
export default class User {
    constructor(name, clientId, clientSecret, validityPeriod) {
        if (User._instance) {
            return User._instance;
        }

        this._userName = name;
        this._clientId = clientId;
        this._clientSecret = clientSecret;
        this._expires = validityPeriod;
        User._instance = this;
    }

    /**
     * OAuth scopes which are available for use by this user
     * @returns {Array} : An array of scopes
     */
    get scopes() {
        return this._scopes;
    }

    /**
     * Set OAuth scopes available to be used by this user
     * @param {Array} newScopes :  An array of scopes
     */
    set scopes(newScopes) {
        Object.assign(this.scopes, newScopes);
    }

    /**
     * Get the JS accessible access token fragment from cookie storage.
     * @returns {String|null}
     */
    getAuthToken() {
        return Utils.getCookie(Constants.userConstants.PARTIAL_TOKEN);
    }

    getClientId() {
        return this._clientId;
    }

    getClientSecret() {
        return this._clientSecret;
    }

    /**
     * Store the JavaScript accessible access token segment in cookie storage
     * @param {String} newToken : Part of the access token which needs when accessing REST API
     * @param {Number} validityPeriod : Validity period of the cookie in seconds
     */
    setAuthToken(newToken, validityPeriod) {
        Utils.delete_cookie(Constants.userConstants.PARTIAL_TOKEN);
        Utils.setCookie(Constants.userConstants.PARTIAL_TOKEN, newToken, validityPeriod);
    }

    /**
     * Get the user name of logged in user.
     * @return String: User name
     * */
    getUserName() {
        return this._userName;
    }

    /**
     * Provide user data in JSON structure.
     * @returns {JSON} : JSON representation of the user object
     */
    toJson() {
        return {
            name: this._userName,
            clientId: this._clientId,
            clientSecret: this._clientSecret,
            expires: this._expires
        };
    }

    /**
     * User utility method to create an user from JSON object.
     * @param {JSON} userJson : Need to provide user information in JSON structure to create an user object
     * @returns {User} : An instance of User(this) class.
     */
    static fromJson(userJson) {
        const _user = new User(userJson.name);
        _user._clientId = userJson.clientId;
        _user._clientSecret = userJson.clientSecret;
        _user._expires = userJson.expires;

        console.log(_user);
        return _user;
    }
}

User._instance = null; // A private class variable to preserve the single instance of a swaggerClient
