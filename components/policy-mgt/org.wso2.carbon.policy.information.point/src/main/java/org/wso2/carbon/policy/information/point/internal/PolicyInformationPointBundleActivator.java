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

package org.wso2.carbon.policy.information.point.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wso2.carbon.policy.information.point.PolicyInformationServiceImpl;
import org.wso2.carbon.policy.mgt.common.PolicyInformationPoint;

public class PolicyInformationPointBundleActivator implements BundleActivator {

    private ServiceRegistration pipServiceRegRef;
    private static final Log log = LogFactory.getLog(PolicyInformationPointBundleActivator.class);

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Activating Policy information Point bundle.");
            }

            pipServiceRegRef = bundleContext.registerService(PolicyInformationPoint.class.getName(),
                    new PolicyInformationServiceImpl(), null);

            if (log.isDebugEnabled()) {
                log.debug("Policy information Point bundle is activated.");
            }

        } catch (Exception ex) {
            log.error("Error occurred while activating the Policy Information Point bundle.", ex);
        }

    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Policy information Point bundle is deactivated.");
            }
            pipServiceRegRef.unregister();
        } catch (Exception ex) {
            log.error("Error occurred while de-activating the Policy Information Point bundle.", ex);
        }

    }
}
