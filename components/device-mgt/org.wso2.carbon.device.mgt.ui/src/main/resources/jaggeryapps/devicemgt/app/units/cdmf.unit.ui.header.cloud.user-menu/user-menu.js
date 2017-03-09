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
    var constants = require("/app/modules/constants.js");
    var mdmProps = require("/app/modules/conf-reader/main.js")["conf"];
    var cloudProps = require("/app/modules/conf-reader/cloud.js")["conf"];
    var user = context.user;
    var isSuperTenant = false;
    if (user.tenantId == -1234) {
        isSuperTenant = true;
    }
    var viewModal = {};
    viewModal.Main = cloudProps.Main;
    viewModal.UserMenu = cloudProps.User;
    viewModal.Expand = cloudProps.Expand;
    viewModal.Main.Domain.isDomain = true;
    viewModal.Main.Support.isSupport = true;

    for (var key in viewModal.Main) {
        var tempDropDownCheck = false;

        for (var sub_key in viewModal.Main[key].dropDown) {
            if (viewModal.Main[key].dropDown[sub_key].dropDown == null ||
                viewModal.Main[key].dropDown[sub_key].dropDown == true ||
                viewModal.Main[key].dropDown[sub_key].dropDown == "true") {
                viewModal.Main[key].dropDown[sub_key].dropDown = true;
                tempDropDownCheck = true;
            } else {
                viewModal.Main[key].dropDown[sub_key].dropDown = false;
            }
        }

        viewModal.Main[key].dropDownVisible = tempDropDownCheck;
    }
    var tempDropDownCheck = false;
    for (var key in viewModal.UserMenu.dropDown) {

        if (viewModal.UserMenu.dropDown[key].dropDown == null ||
            viewModal.UserMenu.dropDown[key].dropDown == true ||
            viewModal.UserMenu.dropDown[key].dropDown == "true") {
            viewModal.UserMenu.dropDown[key].dropDown = true;
            tempDropDownCheck = true;
        } else {
            viewModal.UserMenu.dropDown[key].dropDown = false;

        }
    }
    viewModal.UserMenu.dropDownVisible = tempDropDownCheck;
    tempDropDownCheck = false;
    for (var key in viewModal.Expand) {
        for (var sub_key in viewModal.Expand[key]) {
            if (viewModal.Expand[key][sub_key].dropDown == null ||
                viewModal.Expand[key][sub_key].dropDown == true ||
                viewModal.Expand[key][sub_key].dropDown == "true") {
                viewModal.Expand[key][sub_key].dropDown = true;
                tempDropDownCheck = true;
            } else {
                viewModal.Expand[key][sub_key].dropDown = false;
            }
        }
    }

    viewModal.isSuperTenant = isSuperTenant;
    viewModal.USER_SESSION_KEY = session.get(constants["USER_SESSION_KEY"]);
    viewModal.isCloud = mdmProps.isCloud;
    return viewModal;
}
