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
    var user = context.user;
    var isSuperTenant = false;
    if (user.tenantId == -1234){
        isSuperTenant = true;
    }
    var viewModal = {};
    viewModal.isSuperTenant = isSuperTenant;
    viewModal.USER_SESSION_KEY = session.get(constants["USER_SESSION_KEY"]);
    viewModal.isCloud = mdmProps.isCloud;
    viewModal.contactUsURL = mdmProps.cloudConfig.contactUsURL;
    viewModal.apiCloudDocURL = mdmProps.cloudConfig.apiCloudDocURL;
    viewModal.appCloudDocURL = mdmProps.cloudConfig.appCloudDocURL;
    viewModal.deviceCloudDocURL = mdmProps.cloudConfig.deviceCloudDocURL;
    viewModal.apiCloudWalkthroughURL = mdmProps.cloudConfig.apiCloudWalkthroughURL;
    viewModal.profileURL = mdmProps.cloudConfig.profileURL;
    viewModal.changePasswordURL = mdmProps.cloudConfig.changePasswordURL;
    viewModal.logoutURL = mdmProps.cloudConfig.logoutURL;
    viewModal.apiCloudURL = mdmProps.cloudConfig.apiCloudURL;
    viewModal.appCloudURL = mdmProps.cloudConfig.appCloudURL;
    viewModal.deviceCloudURL = mdmProps.cloudConfig.deviceCloudURL;
    viewModal.oraganizationURL = mdmProps.cloudConfig.oraganizationURL;
    viewModal.membersURL = mdmProps.cloudConfig.membersURL;
    return viewModal;
}
