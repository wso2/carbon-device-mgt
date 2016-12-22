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

package org.wso2.carbon.policy.decision.point.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.policy.decision.point.merged.MergedEvaluationPoint;
import org.wso2.carbon.policy.mgt.common.PolicyEvaluationPoint;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.policy.decision.MergedPolicyEvaluationServiceComponent" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="org.wso2.carbon.devicemgt.policy.manager"
 * interface="org.wso2.carbon.policy.mgt.core.PolicyManagerService"
 * cardinality="0..1"
 * policy="dynamic"
 * bind="setPolicyManagerService"
 * unbind="unsetPolicyManagerService"
 */

public class MergedPolicyEvaluationServiceComponent {

    private static Log log = LogFactory.getLog(MergedPolicyEvaluationServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Activating the policy evaluation bundle.");
        }

        try {
            componentContext.getBundleContext().registerService(PolicyEvaluationPoint.class.getName(),
                    new MergedEvaluationPoint(), null);
        } catch (Throwable t) {
            log.error("Error occurred while initializing the policy evaluation bundle");
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating the policy evaluation bundle.");
        }
    }

    /**
     * Sets Realm Service
     *
     * @param realmService An instance of RealmService
     */
    protected void setRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting Realm Service");
        }
        PolicyDecisionPointDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Unsets Realm Service
     *
     * @param realmService An instance of RealmService
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Realm Service");
        }
        PolicyDecisionPointDataHolder.getInstance().setRealmService(null);
    }

    protected void setPolicyManagerService(PolicyManagerService policyManagerService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting PolicyManagerService Service");
        }
        PolicyDecisionPointDataHolder.getInstance().setPolicyManagerService(policyManagerService);
    }

    protected void unsetPolicyManagerService(PolicyManagerService policyManagerService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting PolicyManagerService Service");
        }
        PolicyDecisionPointDataHolder.getInstance().setPolicyManagerService(null);
    }

//    protected String getName() {
//        return MergedPolicyEvaluationServiceComponent.class.getName();
//    }

}

