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
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.policy.decision.point.internal.PolicyDecisionPointDataHolder;

import java.sql.Timestamp;
import java.util.*;

public class MergedEvaluationPoint implements PolicyEvaluationPoint {

    private static final Log log = LogFactory.getLog(MergedEvaluationPoint.class);
    private PolicyManagerService policyManagerService;
    private List<Policy> policyList;
    PIPDevice pipDevice;

    @Override
    public List<ProfileFeature> getEffectiveFeatures(DeviceIdentifier deviceIdentifier) throws PolicyEvaluationException {
        PolicyAdministratorPoint policyAdministratorPoint;
        PolicyInformationPoint policyInformationPoint;
        policyManagerService = getPolicyManagerService();

        try {
            if (policyManagerService != null) {

                policyInformationPoint = policyManagerService.getPIP();
                PIPDevice pipDevice = policyInformationPoint.getDeviceData(deviceIdentifier);
                policyList = policyInformationPoint.getRelatedPolicies(pipDevice);

                if (!policyList.isEmpty()) {
                    //policy = policyList.get(0);
                    //policyList = new ArrayList<Policy>();
                    Policy effectivePolicy = policyResolve(policyList);
                    effectivePolicy.setActive(true);
                    //TODO : UNCOMMENT THE FOLLOWING CASE
                    policyAdministratorPoint = policyManagerService.getPAP();
                    policyAdministratorPoint.setPolicyUsed(deviceIdentifier, effectivePolicy);
                    return effectivePolicy.getProfile().getProfileFeaturesList();
                }
            }
            return null;
        } catch (PolicyManagementException e) {
            String msg = "Error occurred when retrieving the policy related data from policy management service.";
            log.error(msg, e);
            throw new PolicyEvaluationException(msg, e);
        }
    }

    @Override
    public Policy getEffectivePolicy(DeviceIdentifier deviceIdentifier) throws PolicyEvaluationException {

        try {
            policyManagerService = getPolicyManagerService();
            PolicyInformationPoint policyInformationPoint = policyManagerService.getPIP();
            pipDevice = policyInformationPoint.getDeviceData(deviceIdentifier);
            policyList = policyInformationPoint.getRelatedPolicies(pipDevice);

            if (policyManagerService == null || policyList.size() == 0) {
                return null;
            }

            Policy policy = new Policy();
            Profile profile = new Profile();
            profile.setProfileFeaturesList(getEffectiveFeatures(deviceIdentifier));
            policy.setProfile(profile);
            Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            profile.setCreatedDate(currentTimestamp);
            profile.setUpdatedDate(currentTimestamp);
            profile.setDeviceType(deviceIdentifier.getType());
            profile.setTenantId(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            policy.setPolicyName("Effective-Policy");
            policy.setOwnershipType(pipDevice.getOwnershipType());
            policy.setRoles(null);
            policy.setDevices(null);
            policy.setUsers(null);
            policy.setActive(true);
            policy.setUpdated(true);
            policy.setTenantId(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            policy.setDescription("This is a system generated effective policy by merging relevant policies.");
            policy.setCompliance(policyList.get(0).getCompliance());
            return policy;
        } catch (PolicyManagementException e) {
            String msg = "Error occurred when retrieving the policy related data from policy management service.";
            log.error(msg, e);
            throw new PolicyEvaluationException(msg, e);
        }
    }

    private Policy policyResolve(List<Policy> policyList) throws PolicyEvaluationException, PolicyManagementException {
        sortPolicies();

        // Iterate through all policies
        Map<String, ProfileFeature> featureMap = new HashMap<>();
        Iterator<Policy> policyIterator = policyList.iterator();
        while (policyIterator.hasNext()) {
            Policy policy = policyIterator.next();
            List<ProfileFeature> profileFeaturesList = policy.getProfile().getProfileFeaturesList();
            if (profileFeaturesList != null) {
                Iterator<ProfileFeature> featureIterator = profileFeaturesList.iterator();
                while (featureIterator.hasNext()) {
                    ProfileFeature feature = featureIterator.next();
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

    public void sortPolicies() throws PolicyEvaluationException {
        Collections.sort(policyList, Collections.reverseOrder());
    }

    private PolicyManagerService getPolicyManagerService() {
        return PolicyDecisionPointDataHolder.getInstance().getPolicyManagerService();
    }
}
