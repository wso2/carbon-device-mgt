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
                viewModel.isNotWindows = true;
                if (viewModel["deviceType"] == "windows") {
                    viewModel.isNotWindows = false;
                }
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
            if (filteredDeviceData["initialDeviceInfo"]) {
                viewModel["deviceInfoAvailable"] = true;
                if (filteredDeviceData["initialDeviceInfo"]["IMEI"]) {
                    viewModel["imei"] = filteredDeviceData["initialDeviceInfo"]["IMEI"];
                }
                if (!filteredDeviceData["latestDeviceInfo"]) {
                    if (filteredDeviceData["initialDeviceInfo"]["OS_BUILD_DATE"]) {
                        if (filteredDeviceData["initialDeviceInfo"]["OS_BUILD_DATE"] != "0") {
                            viewModel["osBuildDate"] = new Date(filteredDeviceData["initialDeviceInfo"]["OS_BUILD_DATE"] * 1000);
                        }
                    }
                    if (filteredDeviceData["initialDeviceInfo"]["LATITUDE"] && filteredDeviceData["initialDeviceInfo"]["LONGITUDE"]) {
                        viewModel["location"] = {};
                        viewModel["location"]["latitude"] = filteredDeviceData["initialDeviceInfo"]["LATITUDE"];
                        viewModel["location"]["longitude"] = filteredDeviceData["initialDeviceInfo"]["LONGITUDE"];
                    }
                    if (filteredDeviceData["initialDeviceInfo"]["VENDOR"] && filteredDeviceData["initialDeviceInfo"]["DEVICE_MODEL"]) {
                        viewModel["vendor"] = filteredDeviceData["initialDeviceInfo"]["VENDOR"];
                        viewModel["model"] = filteredDeviceData["initialDeviceInfo"]["DEVICE_MODEL"];
                    }
                    if (filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]) {
                        if (deviceType == "android") {
                            viewModel["BatteryLevel"] = {};
                            viewModel["BatteryLevel"]["value"] = filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["BATTERY_LEVEL"];

                            viewModel["internalMemory"] = {};
                            viewModel["internalMemory"]["total"] = Math.
                                round(filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["INTERNAL_TOTAL_MEMORY"] * 100) / 100;
                            if (filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["INTERNAL_TOTAL_MEMORY"] != 0) {
                                viewModel["internalMemory"]["usage"] = Math.
                                    round((filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["INTERNAL_TOTAL_MEMORY"] -
                                        filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["INTERNAL_AVAILABLE_MEMORY"])
                                        / filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["INTERNAL_TOTAL_MEMORY"] * 10000) / 100;
                            } else {
                                viewModel["internalMemory"]["usage"] = 0;
                            }

                            viewModel["externalMemory"] = {};
                            viewModel["externalMemory"]["total"] = Math.
                                round(filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["EXTERNAL_TOTAL_MEMORY"] * 100) / 100;
                            if (filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["EXTERNAL_TOTAL_MEMORY"] != 0) {
                                viewModel["externalMemory"]["usage"] = Math.
                                    round((filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["EXTERNAL_TOTAL_MEMORY"] -
                                        filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["EXTERNAL_AVAILABLE_MEMORY"])
                                        / filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["EXTERNAL_TOTAL_MEMORY"] * 10000) / 100;
                            } else {
                                viewModel["externalMemory"]["usage"] = 0;
                            }
                        } else if (deviceType == "ios") {
                            viewModel["BatteryLevel"] = {};
                            viewModel["BatteryLevel"]["value"] = filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["BatteryLevel"];

                            viewModel["internalMemory"] = {};
                            viewModel["internalMemory"]["total"] = Math.
                                round(filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["DeviceCapacity"] * 100) / 100;
                            if (filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["DeviceCapacity"] != 0) {
                                viewModel["internalMemory"]["usage"] = Math.
                                    round((filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["DeviceCapacity"] -
                                        filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["AvailableDeviceCapacity"])
                                        / filteredDeviceData["initialDeviceInfo"]["DEVICE_INFO"]["DeviceCapacity"] * 10000) / 100;
                            } else {
                                viewModel["internalMemory"]["usage"] = 0;
                            }
                        }
                    }
                }
            }
            if (filteredDeviceData["latestDeviceInfo"]) {
                viewModel["deviceInfoAvailable"] = true;
                if (filteredDeviceData["latestDeviceInfo"]["osBuildDate"]) {
                    if (filteredDeviceData["latestDeviceInfo"]["osBuildDate"] != "0") {
                        viewModel["osBuildDate"] = new Date(filteredDeviceData["latestDeviceInfo"]["osBuildDate"] * 1000);
                    }
                }
                if (filteredDeviceData["latestDeviceInfo"]["location"]["latitude"] &&
                    filteredDeviceData["latestDeviceInfo"]["location"]["longitude"]) {
                    viewModel["location"] = {};
                    viewModel["location"]["latitude"] = filteredDeviceData["latestDeviceInfo"]["location"]["latitude"];
                    viewModel["location"]["longitude"] = filteredDeviceData["latestDeviceInfo"]["location"]["longitude"];
                }
                if (filteredDeviceData["latestDeviceInfo"]["vendor"] && filteredDeviceData["latestDeviceInfo"]["deviceModel"]) {
                    viewModel["vendor"] = filteredDeviceData["latestDeviceInfo"]["vendor"];
                    viewModel["model"] = filteredDeviceData["latestDeviceInfo"]["deviceModel"];
                }
                if (filteredDeviceData["latestDeviceInfo"]["updatedTime"]) {
                    viewModel["lastUpdatedTime"] = filteredDeviceData["latestDeviceInfo"]["updatedTime"].
                    substr(0, filteredDeviceData["latestDeviceInfo"]["updatedTime"].indexOf("+"));
                }
                viewModel["BatteryLevel"] = {};
                viewModel["BatteryLevel"]["value"] = filteredDeviceData["latestDeviceInfo"]["batteryLevel"];

                viewModel["cpuUsage"] = {};
                viewModel["cpuUsage"]["value"] = filteredDeviceData["latestDeviceInfo"]["cpuUsage"];

                viewModel["ramUsage"] = {};
                if (filteredDeviceData["latestDeviceInfo"]["totalRAMMemory"] != 0) {
                    viewModel["ramUsage"]["value"] = Math.
                        round((filteredDeviceData["latestDeviceInfo"]["totalRAMMemory"] -
                            filteredDeviceData["latestDeviceInfo"]["availableRAMMemory"])
                            / filteredDeviceData["latestDeviceInfo"]["totalRAMMemory"] * 10000) / 100;
                } else {
                    viewModel["ramUsage"]["value"] = 0;
                }

                viewModel["internalMemory"] = {};
                viewModel["internalMemory"]["total"] = Math.
                    round(filteredDeviceData["latestDeviceInfo"]["internalTotalMemory"] * 100) / 100;
                if (filteredDeviceData["latestDeviceInfo"]["internalTotalMemory"] != 0) {
                    viewModel["internalMemory"]["usage"] = Math.
                        round((filteredDeviceData["latestDeviceInfo"]["internalTotalMemory"] -
                            filteredDeviceData["latestDeviceInfo"]["internalAvailableMemory"])
                            / filteredDeviceData["latestDeviceInfo"]["internalTotalMemory"] * 10000) / 100;
                } else {
                    viewModel["internalMemory"]["usage"] = 0;
                }

                viewModel["externalMemory"] = {};
                viewModel["externalMemory"]["total"] = Math.
                    round(filteredDeviceData["latestDeviceInfo"]["externalTotalMemory"] * 100) / 100;
                if (filteredDeviceData["latestDeviceInfo"]["externalTotalMemory"] != 0) {
                    viewModel["externalMemory"]["usage"] = Math.
                        round((filteredDeviceData["latestDeviceInfo"]["externalTotalMemory"] -
                            filteredDeviceData["latestDeviceInfo"]["externalAvailableMemory"])
                            / filteredDeviceData["latestDeviceInfo"]["externalTotalMemory"] * 10000) / 100;
                } else {
                    viewModel["externalMemory"]["usage"] = 0;
                }
            }
            if (!filteredDeviceData["initialDeviceInfo"] && !filteredDeviceData["latestDeviceInfo"]) {
                viewModel["deviceInfoAvailable"] = false;
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