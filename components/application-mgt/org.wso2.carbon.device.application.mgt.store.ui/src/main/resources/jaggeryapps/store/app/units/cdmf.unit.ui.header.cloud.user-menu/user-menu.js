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
    var log = new Log("user-menu.js");

    var sqlDateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

    //get billing info
    var type = {PAID: "PAID", TRIAL: "TRIAL", FREE: "FREE"};
    var status = {
        ACTIVE: "ACTIVE",
        INACTIVE: "INACTIVE",
        EXTENDED: "EXTENDED",
        PENDING_DISABLED: "PENDING_DISABLED",
        DISABLED: "DISABLED"
    };

    var BILLING_INFO_KEY = 'BILLING_INFO_' + context.user.domain;
    var BILLING_INFO_RETRY_COUNT_KEY = 'BILLING_INFO_RETRY_COUNT_' + context.user.domain;

    if (viewModal.Main.Account.billingEnabled) {
        var cookie = getLoginCookie();
        if (!session.get(BILLING_INFO_KEY) || daysAfterLastCheck(session.get(BILLING_INFO_KEY).lastChecked) > 1) {
            session.put(BILLING_INFO_RETRY_COUNT_KEY, 0);
            var serviceUrl = viewModal.Main.Account.cloudMgtHost + "/cloudmgt/site/blocks/admin/admin.jag";
            getBillingData(serviceUrl, cookie, 1);
        }

        var billingInfo = session.get(BILLING_INFO_KEY);

        var isExpired = false;
        var isTrial = false;
        var trialPeriod = 14;
        var cloudMgtIndexPage = viewModal.Main.Account.cloudMgtIndexPage;

        if (!billingInfo) {
            recordFirstLogin(serviceUrl, cookie, 1);
            log.info("Access denied for tenant: " + context.user.domain
                     + " with a NULL subscription. Redirected to CloudMgt");
            response.sendRedirect(cloudMgtIndexPage);
            exit(0);
        } else if (billingInfo.isPaidAccount && (billingInfo.billingPlanStatus === status.ACTIVE
                                                 || billingInfo.billingPlanStatus === status.PENDING_DISABLED)) {
            isExpired = false;
            //change menu item name
            delete viewModal.Main.Account.color;
            delete viewModal.Main.Account["Request Extension"];
        } else if (!billingInfo.isPaidAccount) {
            var accountContent = "Account";
            if (billingInfo.billingPlanStatus === status.ACTIVE || billingInfo.billingPlanStatus === status.EXTENDED) {
                var currDate = new java.util.Date();
                var endDate = sqlDateFormatter.parse(billingInfo.endDate);
                var diff = endDate.getTime() - currDate.getTime();
                isTrial = true;
                if (diff > 0) {
                    noOfDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff);
                    accountContent = "Trial - " + (noOfDays + 1) + " days to upgrade";
                } else {
                    isExpired = true;
                    accountContent = "Trial Expired";
                }
            } else if (billingInfo.billingPlanStatus === status.INACTIVE) {
                isExpired = false;
                isTrial = true;
                accountContent = "Trial " + (trialPeriod) + " days tade";
            }
            //change menu item name
            viewModal.Main.Account.label = accountContent;
            viewModal.Main.Account.isDomain = false;
            viewModal.Main.Account.color = "red";
            viewModal.Main.Account.isExpired = isExpired;
        } else if (billingInfo.billingPlanStatus === status.DISABLED) {
            log.info(
                "Access denied for tenant: " + context.user.domain
                + " with a DISABLED subscription. Redirected to CloudMgt");
            response.sendRedirect(cloudMgtIndexPage);
            exit(0);
        }
    } else {
        // delete viewModal.Main.Account;
        viewModal.Main.Account = {"billingEnabled": false}
    }

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

    function getBillingData(serviceUrl, cookie, attempt) {
        result = post(serviceUrl,
                      'action=getBillingStatusOfTenant&tenantDomain=' + context.user.domain + '&cloudType=device_cloud',
                      {"Cookie": cookie});
        if (result.data) {
            var billing = JSON.parse(result.data).data;
            if (!billing || !billing.isPaidAccount && billing.billingPlanStatus === status.INACTIVE) {
                recordFirstLogin(serviceUrl, cookie, attempt);
            } else {
                session.put(BILLING_INFO_KEY, JSON.parse(result.data).data);
                session.put(BILLING_INFO_RETRY_COUNT_KEY, 0);
            }
        }
    }

    function recordFirstLogin(serviceUrl, cookie, attempt){
        var rv = post(serviceUrl,
                      'action=informFirstLogin&tenantDomain=' + context.user.domain + '&cloudType=device_cloud',
                      {"Cookie": cookie});
        if (!attempt) attempt = 1;
        var failStr = "First login capturing failed";
        var successStr = "First login captured successfully";
        if (rv.data.substring(0, failStr.length) === failStr && attempt < 3) {
            recordFirstLogin(serviceUrl, cookie, ++attempt); //retry
        } else {
            getBillingData(serviceUrl, cookie, ++attempt); //get expiry details
        }
    }

    function getLoginCookie(){
        var retryCount = session.get(BILLING_INFO_RETRY_COUNT_KEY) || 0;
        if (retryCount <= 3) {
            retryCount++;
            session.put(BILLING_INFO_RETRY_COUNT_KEY, retryCount);
            var username = viewModal.Main.Account.billingApi.username;
            var password = viewModal.Main.Account.billingApi.password;
            var serviceUrl = viewModal.Main.Account.cloudMgtHost + "/cloudmgt/site/blocks/user/authenticate/ajax/login.jag";
            var result = post(serviceUrl, 'action=login&userName=' + username + '&password=' + password,
                              {"Content-Type": "application/x-www-form-urlencoded"});
            if (result.data && result.data.trim() === "true") {
                var cookieHeader = result.xhr.getResponseHeader("Set-Cookie");
                if (cookieHeader) {
                    var cookie = cookieHeader.split(";")[0];
                    return cookie;
                }
            }
        } else {
            log.error("Billing info api failed after " + session.get(BILLING_INFO_RETRY_COUNT_KEY) + " attempts!");
        }
    }

    function daysAfterLastCheck(checkedDate){
        if(!checkedDate) return 0;
        var currDate = new java.util.Date();
        var endDate = sqlDateFormatter.parse(checkedDate);
        var diff = endDate.getTime() - currDate.getTime();
        if (diff > 0) {
            return java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff);
        }
        return 0;
    }
}