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
    var log = new Log("cdmf.unit.device.view/view.js");
    var deviceType = context["uriParams"]["deviceType"];
    var deviceId = request.getParameter("id");
    var deviceViewData = {};
    if (deviceType && deviceId) {
        var deviceModule = require("/app/modules/business-controllers/device.js")["deviceModule"];
        var response = deviceModule.viewDevice(deviceType, deviceId);
        if (response["status"] == "success") {
            deviceViewData["deviceFound"] = true;
            deviceViewData["isAuthorized"] = true;

            var filteredDeviceData = response["content"];

            // creating deviceView information model from filtered device data
            var viewModel = {};
            if (filteredDeviceData["type"]) {
                viewModel["deviceType"] = filteredDeviceData["type"];
                viewModel["type"] = filteredDeviceData["type"];
            }
            if (filteredDeviceData["deviceIdentifier"]) {
                viewModel["deviceIdentifier"] = filteredDeviceData["deviceIdentifier"];
            }
            if (filteredDeviceData["name"]) {
                viewModel["name"] = filteredDeviceData["name"];
            }
            if (filteredDeviceData["enrolmentInfo"]) {
                if (filteredDeviceData["enrolmentInfo"]["status"]) {
                    viewModel["status"] = filteredDeviceData["enrolmentInfo"]["status"];
                    viewModel.isActive = false ;
                    viewModel.isNotRemoved = true;
                    if (filteredDeviceData["enrolmentInfo"]["status"]== "ACTIVE") {
                        viewModel.isActive = true ;
                    }
                    if (filteredDeviceData["enrolmentInfo"]["status"]== "REMOVED") {
                        viewModel.isNotRemoved = false ;
                    }
                }
                if (filteredDeviceData["enrolmentInfo"]["owner"]) {
                    viewModel["owner"] = filteredDeviceData["enrolmentInfo"]["owner"];
                }
                if (filteredDeviceData["enrolmentInfo"]["ownership"]) {
                    viewModel["ownership"] = filteredDeviceData["enrolmentInfo"]["ownership"];
                }
            }
            deviceViewData["device"] = viewModel;
        } else if (response["status"] == "unauthorized") {
            deviceViewData["deviceFound"] = true;
            deviceViewData["isAuthorized"] = false;
        } else if (response["status"] == "notFound") {
            deviceViewData["deviceFound"] = false;
        }
    } else {
        deviceViewData["deviceFound"] = false;
    }
    return deviceViewData;
}