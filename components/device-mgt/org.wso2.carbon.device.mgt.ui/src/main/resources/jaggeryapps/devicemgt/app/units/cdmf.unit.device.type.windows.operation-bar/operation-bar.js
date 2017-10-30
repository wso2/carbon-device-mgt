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
    var log = new Log("cdmf.unit.device.type.windows.operation-bar");
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var viewModel = {};
    var permissions = {};

    // adding android operations related permission checks
    permissions["android"] = [];
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning-device/operations/android/ring")) {
        permissions["android"].push("DEVICE_RING");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning-device/operations/android/lock")) {
        permissions["android"].push("DEVICE_LOCK");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning-device/operations/android/unlock")) {
        permissions["android"].push("DEVICE_UNLOCK");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning-device/operations/android/location")) {
        permissions["android"].push("DEVICE_LOCATION");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning-device/operations/android/clear-password")) {
        permissions["android"].push("CLEAR_PASSWORD");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning-device/operations/android/reboot")) {
        permissions["android"].push("DEVICE_REBOOT");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning-device/operations/android/upgrade-firmware")) {
        permissions["android"].push("UPGRADE_FIRMWARE");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning-device/operations/android/mute")) {
        permissions["android"].push("DEVICE_MUTE");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning-device/operations/android/send-notification")) {
        permissions["android"].push("NOTIFICATION");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning-device/operations/android/change-lock-code")) {
        permissions["android"].push("CHANGE_LOCK_CODE");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning-device/operations/android/enterprise-wipe")) {
        permissions["android"].push("ENTERPRISE_WIPE");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning-device/operations/android/wipe")) {
        permissions["android"].push("WIPE_DATA");
    }

    // adding ios operations related permission checks
    permissions["ios"] = [];
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/ios/lock")) {
        permissions["ios"].push("DEVICE_LOCK");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/ios/location")) {
        permissions["ios"].push("LOCATION");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/ios/enterprise-wipe")) {
        permissions["ios"].push("ENTERPRISE_WIPE");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/ios/notification")) {
        permissions["ios"].push("NOTIFICATION");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/ios/ring")) {
        permissions["ios"].push("RING");
    }

    // adding windows operations related permission checks
    permissions["windows"] = [];
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/windows/lock")) {
        permissions["windows"].push("DEVICE_LOCK");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/disenroll/windows")) {
        permissions["windows"].push("DISENROLL");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/windows/wipe")) {
        permissions["windows"].push("WIPE_DATA");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/windows/ring")) {
        permissions["windows"].push("DEVICE_RING");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/windows/lock-reset")) {
        permissions["windows"].push("LOCK_RESET");
    }

    viewModel["permissions"] = stringify(permissions);

    viewModel["deviceType"] = context.unit.params.deviceType;
    viewModel["ownership"] = context.unit.params.ownership;

    return viewModel;
}