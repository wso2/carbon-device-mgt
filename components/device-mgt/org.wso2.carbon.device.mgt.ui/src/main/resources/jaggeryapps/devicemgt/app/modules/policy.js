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

/*
 @Refactored
 */
var policyModule;
policyModule = function () {
    var log = new Log("/app/modules/policy.js");

    var constants = require('/app/modules/constants.js');
    var utility = require("/app/modules/utility.js")["utility"];
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var serviceInvokers = require("/app/modules/oauth/token-protected-service-invokers.js")["invokers"];

    var publicMethods = {};
    var privateMethods = {};

    privateMethods.handleGetAllPoliciesError = function (responsePayload) {
        var response = {};
        response.status = "error";
        /* responsePayload == "Scope validation failed"
         Here the response.context("Scope validation failed") is used other then response.status(401).
         Reason for this is IDP return 401 as the status in 4 different situations such as,
         1. UnAuthorized.
         2. Scope Validation Failed.
         3. Permission Denied.
         4. Access Token Expired.
         5. Access Token Invalid.
         In these cases in order to identify the correct situation we have to compare the unique value from status and
         context which is context.
         */
        if (responsePayload == "Scope validation failed") {
            response.content = "Permission Denied";
        } else {
            response.content = responsePayload;
        }
        return response;
    };

    privateMethods.handleGetAllPoliciesSuccess = function (responsePayload) {
        var isUpdated = false;
        var policyListFromRestEndpoint = responsePayload["responseContent"];
        var policyListToView = [];
        var i, policyObjectFromRestEndpoint, policyObjectToView;
        for (i = 0; i < policyListFromRestEndpoint.length; i++) {
            // get list object
            policyObjectFromRestEndpoint = policyListFromRestEndpoint[i];
            // populate list object values to view-object
            policyObjectToView = {};
            policyObjectToView["id"] = policyObjectFromRestEndpoint["id"];
            policyObjectToView["priorityId"] = policyObjectFromRestEndpoint["priorityId"];
            policyObjectToView["name"] = policyObjectFromRestEndpoint["policyName"];
            policyObjectToView["platform"] = policyObjectFromRestEndpoint["profile"]["deviceType"]["name"];
            policyObjectToView["icon"] = utility.getDeviceThumb(policyObjectToView["platform"]);
            policyObjectToView["ownershipType"] = policyObjectFromRestEndpoint["ownershipType"];
            policyObjectToView["roles"] = privateMethods.
            getElementsInAString(policyObjectFromRestEndpoint["roles"]);
            policyObjectToView["users"] = privateMethods.
            getElementsInAString(policyObjectFromRestEndpoint["users"]);
            policyObjectToView["compliance"] = policyObjectFromRestEndpoint["compliance"];

            if (policyObjectFromRestEndpoint["active"] == true && policyObjectFromRestEndpoint["updated"] == true) {
                policyObjectToView["status"] = "Active/Updated";
                isUpdated = true;
            } else if (policyObjectFromRestEndpoint["active"] == true &&
                       policyObjectFromRestEndpoint["updated"] == false) {
                policyObjectToView["status"] = "Active";
            } else if (policyObjectFromRestEndpoint["active"] == false &&
                       policyObjectFromRestEndpoint["updated"] == true) {
                policyObjectToView["status"] = "Inactive/Updated";
                isUpdated = true;
            } else if (policyObjectFromRestEndpoint["active"] == false &&
                       policyObjectFromRestEndpoint["updated"] == false) {
                policyObjectToView["status"] = "Inactive";
            }
            // push view-objects to list
            policyListToView.push(policyObjectToView);
        }
        // generate response
        var response = {};
        response.updated = isUpdated;
        response.status = "success";
        response.content = policyListToView;
        return response;
    };

    publicMethods.addPolicy = function (policyName, deviceType, policyDefinition, policyDescription,
                                        deviceId) {
        var carbonUser = session.get(constants["USER_SESSION_KEY"]);
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants["ERRORS"]["USER_NOT_FOUND"];
        }
        if (policyName && deviceType) {
            var queName = "WSO2IoTServer/" + carbonUser.username + "/" + deviceType;
            var deviceQueName;
            if (deviceId) {
                deviceQueName = queName + "/" + deviceId;
                privateMethods.publish(deviceQueName, policyName, deviceType, policyDefinition);
            } else {
                var deviceManagementService = utility.getDeviceManagementService();
                var devices = deviceManagementService.getDevicesOfUser(carbonUser.username);
                var device;
                for (var i = 0; i < devices.size(); i++) {
                    device = devices.get(i);
                    deviceId = device.getDeviceIdentifier();
                    deviceQueName = queName + "/" + deviceId;
                    privateMethods.publish(deviceQueName, policyName, deviceType, policyDefinition);
                }
            }
            return true;
        }
        return false;
    };

    privateMethods.publish = function (queName, policyName, deviceType, policyDefinition) {
        var configurationService = utility.getConfigurationService();
        var mqttEndPointDeviceConfig = configurationService.getControlQueue(constants.MQTT_QUEUE_CONFIG_NAME);
        var mqttBrokerURL = mqttEndPointDeviceConfig.getServerURL();
        var mqttBrokerPort = mqttEndPointDeviceConfig.getPort();
        var mqttQueueEndpoint = mqttBrokerURL + ":" + mqttBrokerPort;

        var mqttsenderClass = Packages.org.wso2.carbon.device.mgt.iot.mqtt.PolicyPush;
        var mqttsender = new mqttsenderClass();

        var policyPayload = "POLICY:" + policyDefinition;
        var result = mqttsender.pushToMQTT(queName, policyPayload, mqttQueueEndpoint, "MQTT_Agent");
        mqttsender = null;
        return result;
    };

    /*
     @Updated
     */
    publicMethods.getAllPolicies = function () {
        var carbonUser = session.get(constants["USER_SESSION_KEY"]);
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants["ERRORS"]["USER_NOT_FOUND"];
        }
        try {
            var url = devicemgtProps["httpsURL"] + constants.ADMIN_SERVICE_CONTEXT + "/policies";
            return serviceInvokers.XMLHttp.
            get(url, privateMethods.handleGetAllPoliciesSuccess, privateMethods.handleGetAllPoliciesError);
        } catch (e) {
            throw e;
        }
    };

    /*
     @Updated - used by getAllPolicies
     */
    privateMethods.getElementsInAString = function (elementList) {
        var i, elementsInAString = "";
        for (i = 0; i < elementList.length; i++) {
            if (i == elementList.length - 1) {
                elementsInAString += elementList[i];
            } else {
                elementsInAString += elementList[i] + ", ";
            }
        }
        return elementsInAString;
    };

    /*
     @Deprecated
     */
    publicMethods.getProfiles = function () {
        var carbonUser = session.get(constants.USER_SESSION_KEY);
        var utility = require('/app/modules/utility.js').utility;
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants.ERRORS.USER_NOT_FOUND;
        }
        try {
            utility.startTenantFlow(carbonUser);
            var policyManagementService = utility.getPolicyManagementService();
            var policyAdminPoint = policyManagementService.getPAP();
            var profiles = policyAdminPoint.getProfiles();
            var profileList = [];
            var i, profile, profileObject;
            for (i = 0; i < profiles.size(); i++) {
                profile = profiles.get(i);
                profileObject = {};
                profileObject.name = profile.getProfileName();
                profileObject.id = profile.getProfileId();
                profileList.push(profileObject);
            }
            return profileList;
        } catch (e) {
            throw e;
        } finally {
            utility.endTenantFlow();
        }
    };

    /*
     @Deprecated
     */
    publicMethods.updatePolicyPriorities = function (payload) {
        var carbonUser = session.get(constants.USER_SESSION_KEY);
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants.ERRORS.USER_NOT_FOUND;
        }
        try {
            utility.startTenantFlow(carbonUser);
            var policyManagementService = utility.getPolicyManagementService();
            var policyAdminPoint = policyManagementService.getPAP();
            var policyCount = payload.length;
            var policyList = new java.util.ArrayList();
            var i, policyObject;
            for (i = 0; i < policyCount; i++) {
                policyObject = new Policy();
                policyObject.setId(payload[i].id);
                policyObject.setPriorityId(payload[i].priority);
                policyList.add(policyObject);
            }
            policyAdminPoint.updatePolicyPriorities(policyList);
        } catch (e) {
            throw e;
        } finally {
            utility.endTenantFlow();
        }
    };

    /*
     @Deprecated
     */
    publicMethods.deletePolicy = function (policyId) {
        var isDeleted;
        var carbonUser = session.get(constants.USER_SESSION_KEY);
        var utility = require('/app/modules/utility.js').utility;
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants.ERRORS.USER_NOT_FOUND;
        }
        try {
            utility.startTenantFlow(carbonUser);
            var policyManagementService = utility.getPolicyManagementService();
            var policyAdminPoint = policyManagementService.getPAP();
            isDeleted = policyAdminPoint.deletePolicy(policyId);
            if (isDeleted) {
                // http status code 200 refers to - success.
                return 200;
            } else {
                // http status code 409 refers to - conflict.
                return 409;
            }
        } catch (e) {
            throw e;
        } finally {
            utility.endTenantFlow();
        }
    };

    return publicMethods;
}();
