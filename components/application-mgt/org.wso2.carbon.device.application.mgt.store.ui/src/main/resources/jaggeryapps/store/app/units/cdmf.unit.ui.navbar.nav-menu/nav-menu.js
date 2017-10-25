/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function onRequest(context) {
    context.handlebars.registerHelper('equal', function (lvalue, rvalue, options) {
        if (arguments.length < 3) {
            throw new Error("Handlebars Helper equal needs 2 parameters");
        }
        if (lvalue != rvalue) {
            return options.inverse(this);
        } else {
            return options.fn(this);
        }
    });
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var mdmProps = require("/app/modules/conf-reader/main.js")["conf"];
    var constants = require("/app/modules/constants.js");
    var uiPermissions = userModule.getUIPermissions();
    context["permissions"] = uiPermissions;
    context["userMgtEnabled"] = (uiPermissions["LIST_USERS"] || uiPermissions["LIST_ROLES"]);

    var links = {
        "user-mgt": [],
        "role-mgt": [],
        "policy-mgt": [],
        "device-mgt": []
    };

    // following context.link value comes here based on the value passed at the point
    // where units are attached to a page zone.
    // eg: {{unit "appbar" pageLink="users" title="User Management"}}
    context["currentActions"] = links[context["pageLink"]];
    context["enrollmentURL"] = mdmProps["generalConfig"]["host"] + mdmProps["enrollmentDir"];
    var  isAuthorizedForNotifications =
        userModule.isAuthorized("/permission/admin/device-mgt/emm-admin/notifications/view");
    var currentUser = session.get(constants["USER_SESSION_KEY"]);
    context["isAuthorizedForNotifications"] = isAuthorizedForNotifications;
    context["currentUser"] = currentUser;
    context["appContext"] = mdmProps["appContext"];
    context["isCloud"] = mdmProps["isCloud"];

    return context;
}