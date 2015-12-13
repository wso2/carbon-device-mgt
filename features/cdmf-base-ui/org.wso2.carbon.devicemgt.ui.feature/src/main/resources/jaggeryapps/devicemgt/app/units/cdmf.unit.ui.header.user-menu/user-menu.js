/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function onRequest(context) {
    var constants = require("/app/modules/constants.js");
    var user = session.get(constants.USER_SESSION_KEY);
    if (user) {
        var devicemgtProps = require('/app/conf/devicemgt-props.js').config();
        var localLogoutURL = devicemgtProps.appContext + "api/user/logout";
        var ssoLogoutURL = devicemgtProps.appContext + "sso/logout";
        var logoutURL = devicemgtProps.ssoConfiguration.enabled ? ssoLogoutURL : localLogoutURL;
        return {
            userName: user.username,
            subMenus: [{text: "Settings", url: "/settings"}, {text: "Sign out", url: logoutURL}],
            showUserMenu: true
        }
    } else {
        return {
            showUserMenu: false
        };
    }

}