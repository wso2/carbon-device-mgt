/*
*  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.policy.decision.point.merged;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.policy.decision.point.internal.PolicyDecisionPointDataHolder;

import java.sql.Timestamp;
import java.util.*;

/**
 * This class helps to merge related policies and return as a effective policy.
 */
public class MergedEvaluationPoint implements PolicyEvaluationPoint {

    private static final Log log = LogFactory.getLog(MergedEvaluationPoint.class);
    private PolicyManagerService policyManagerService;
    private static final String effectivePolicyName = "Effective-Policy";
    private static final String policyEvaluationPoint = "Merged";

    @Override
    public List<ProfileFeature> getEffectiveFeatures(DeviceIdentifier deviceIdentifier)
            throws PolicyEvaluationException {
        return this.getEffectivePolicy(deviceIdentifier).getProfile().getProfileFeaturesList();
    }

    @Override
    public String getName() {
        return policyEvaluationPoint;
    }

    @Override
    public Policy getEffectivePolicy(DeviceIdentifier deviceIdentifier) throws PolicyEvaluationException {
        PIPDevice pipDevice;
        List<Policy> policyList;
        Policy policy;
        try {
            policyManagerService = getPolicyManagerService();
            if (policyManagerService == null) {
                return null;
            }
            PolicyInformationPoint policyInformationPoint = policyManagerService.getPIP();
            pipDevice = policyInformationPoint.getDeviceData(deviceIdentifier);
            policyList = policyInformationPoint.getRelatedPolicies(pipDevice);

            if (policyList.size() == 0) {
                return null;
            }

            // Set effective-policy information
            Profile profile = new Profile();
            policy = policyResolve(policyList);
            profile.setProfileFeaturesList(policy.getProfile().getProfileFeaturesList());
            policy.setProfile(profile);
            Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            profile.setCreatedDate(currentTimestamp);
            profile.setUpdatedDate(currentTimestamp);
            profile.setDeviceType(deviceIdentifier.getType());
            profile.setTenantId(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            // Set effective policy name
            policy.setPolicyName(effectivePolicyName);
            policy.setOwnershipType(pipDevice.getOwnershipType());
            // Set effective policy Active and Updated
            policy.setActive(true);
            policy.setUpdated(true);
            policy.setTenantId(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            String policyIds = "";
            Collections.sort(policyList);
            for (Policy appliedPolicy : policyList) {
                policyIds += appliedPolicy.getId() + ", ";
            }
            policyIds = policyIds.substring(0, policyIds.length() - 2);
            policy.setDescription("This is a system generated effective policy by merging Policy Id : " + policyIds);
            // Need to set compliance of the effective policy. Get compliance of first policy using priority order
            policy.setCompliance(policyList.get(0).getCompliance());
            // Change default 0 effective policy id to (-1)
            policy.setId(-1);
            return policy;
        } catch (PolicyManagementException e) {
            String msg = "Error occurred when retrieving the policy related data from policy management service.";
            log.error(msg, e);
            throw new PolicyEvaluationException(msg, e);
        }
    }

    private Policy policyResolve(List<Policy> policyList) throws PolicyEvaluationException, PolicyManagementException {
        Collections.sort(policyList, Collections.reverseOrder());

        // Iterate through all policies
        Map<String, ProfileFeature> featureMap = new HashMap<>();
        for (Policy policy : policyList) {
            List<ProfileFeature> profileFeaturesList = policy.getProfile().getProfileFeaturesList();
            if (profileFeaturesList != null) {
                for (ProfileFeature feature : profileFeaturesList) {
                    featureMap.put(feature.getFeatureCode(), feature);
                }
            }
        }

        // Get prioritized features list
        List<ProfileFeature> newFeaturesList = new ArrayList<>(featureMap.values());
        Profile profile = new Profile();
        profile.setProfileFeaturesList(newFeaturesList);
        Policy effectivePolicy = new Policy();
        effectivePolicy.setProfile(profile);
        return effectivePolicy;
    }

    private PolicyManagerService getPolicyManagerService() {
        return PolicyDecisionPointDataHolder.getInstance().getPolicyManagerService();
    }

}
