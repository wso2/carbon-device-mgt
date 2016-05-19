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

function onRequest(context) {
    var userModule = require("/app/modules/user.js").userModule;
    var userName = request.getParameter("username");
    var user = userModule.getUser(userName)["content"];
    var devicemgtProps = require('/app/conf/devicemgt-props.js').config();
    if (user) {
        var title;
        if (user.firstname || user.lastname) {
            title = user.firstname + " " + user.lastname;
        } else {
            title = user.username;
        }
        var page = {"user": user, "title": title};

        var userStore = "PRIMARY";
        if (userName.indexOf("/") > -1) {
            userStore = userName.substr(0, userName.indexOf('/'));
        }
        var response = userModule.getUser(userName);

        if (response["status"] == "success") {
            page["editUser"] = response["content"];
        }

        response = userModule.getRolesByUsername(userName);
        if (response["status"] == "success") {
            page["usersRoles"] = response["content"];
        }
        response = userModule.getRolesByUserStore(userStore);
        if (response["status"] == "success") {
            var roleVals = response["content"];
            var filteredRoles = [];
            var prefix  = "Application";
            for (i = 0; i < roleVals.length; i++) {
                if(roleVals[i].indexOf(prefix) < 0){
                    filteredRoles.push(roleVals[i]);
                }
            }
            page["userRoles"] = filteredRoles;
        }

    }
    page["usernameJSRegEx"] = devicemgtProps.userValidationConfig.usernameJSRegEx;
    page["usernameRegExViolationErrorMsg"] = devicemgtProps.userValidationConfig.usernameRegExViolationErrorMsg;
    page["firstnameJSRegEx"] = devicemgtProps.userValidationConfig.firstnameJSRegEx;
    page["firstnameRegExViolationErrorMsg"] = devicemgtProps.userValidationConfig.firstnameRegExViolationErrorMsg;
    page["lastnameJSRegEx"] = devicemgtProps.userValidationConfig.lastnameJSRegEx;
    page["lastnameRegExViolationErrorMsg"] = devicemgtProps.userValidationConfig.lastnameRegExViolationErrorMsg;
    return page;
}