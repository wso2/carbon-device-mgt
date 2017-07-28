/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.extensions.pull.notification;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.device.mgt.common.pull.notification.PullNotificationExecutionFailedException;
import org.wso2.carbon.device.mgt.common.pull.notification.PullNotificationSubscriber;
import org.wso2.carbon.device.mgt.extensions.pull.notification.internal.PullNotificationDataHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PullNotificationSubscriberImpl implements PullNotificationSubscriber {

    public final class OperationCodes {
        private OperationCodes() {
            throw new AssertionError();
        }
        public static final String POLICY_MONITOR = "POLICY_MONITOR";
    }


    private static final Log log = LogFactory.getLog(PullNotificationSubscriberImpl.class);

    public void init(Map<String, String> properties) {

    }

    @Override
    public void execute(DeviceIdentifier deviceIdentifier, Operation operation) throws PullNotificationExecutionFailedException {
        try {
            if (!Operation.Status.ERROR.equals(operation.getStatus()) && operation.getCode() != null &&
            OperationCodes.POLICY_MONITOR.equals(operation.getCode())) {
                if (log.isDebugEnabled()) {
                    log.info("Received compliance status from POLICY_MONITOR operation ID: " + operation.getId());
                }
                List<ComplianceFeature> features = getComplianceFeatures(operation.getPayLoad());
                PullNotificationDataHolder.getInstance().getPolicyManagerService()
                        .checkCompliance(deviceIdentifier, features);

            } else {
                PullNotificationDataHolder.getInstance().getDeviceManagementProviderService().updateOperation(
                        deviceIdentifier, operation);
            }
        } catch (OperationManagementException e) {
            throw new PullNotificationExecutionFailedException(e);
        } catch (PolicyComplianceException e) {
            throw new PullNotificationExecutionFailedException("Invalid payload format compliant feature", e);
        }
    }

    public void clean() {

    }

    private static List<ComplianceFeature> getComplianceFeatures(Object compliancePayload) throws
                                                                                           PolicyComplianceException {
        String compliancePayloadString = new Gson().toJson(compliancePayload);
        if (compliancePayload == null) {
            return null;
        }
        // Parsing json string to get compliance features.
        JsonElement jsonElement = new JsonParser().parse(compliancePayloadString);
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        Gson gson = new Gson();
        ComplianceFeature complianceFeature;
        List<ComplianceFeature> complianceFeatures = new ArrayList<ComplianceFeature>(jsonArray.size());

        for (JsonElement element : jsonArray) {
            complianceFeature = gson.fromJson(element, ComplianceFeature.class);
            complianceFeatures.add(complianceFeature);
        }
        return complianceFeatures;
    }
}
