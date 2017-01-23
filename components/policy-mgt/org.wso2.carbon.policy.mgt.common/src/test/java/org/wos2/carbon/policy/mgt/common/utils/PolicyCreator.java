/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wos2.carbon.policy.mgt.common.utils;


import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;

import java.util.ArrayList;
import java.util.List;

public class PolicyCreator {

    private static Policy policy = new Policy();

    public static Policy createPolicy() {

        Feature feature = new Feature();
        feature.setName("Camera");
        feature.setCode("502A");
       // feature.setAttribute("disable");

        List<Feature> featureList = new ArrayList<Feature>();
        featureList.add(feature);

        Profile profile = new Profile();
        profile.setProfileId(1);
        profile.setProfileName("Test-01");
        profile.setTenantId(-1234);

        policy.setProfile(profile);
        //profile.setFeaturesList(featureList);

        policy.setPolicyName("Camera_related_policy");

        return policy;
    }


}
