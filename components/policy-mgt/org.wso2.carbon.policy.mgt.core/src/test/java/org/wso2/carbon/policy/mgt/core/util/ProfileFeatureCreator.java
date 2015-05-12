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

package org.wso2.carbon.policy.mgt.core.util;

import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.policy.mgt.common.ProfileFeature;

import java.util.ArrayList;
import java.util.List;

public class ProfileFeatureCreator {

    public static List<ProfileFeature> getProfileFeature(List<Feature> features) {
        List<ProfileFeature> profileFeatureList = new ArrayList<ProfileFeature>();

        for (Feature feature : features) {

            ProfileFeature profileFeature = new ProfileFeature();
            profileFeature.setContent(feature);
            profileFeature.setDeviceTypeId(1);
            profileFeature.setFeatureCode(feature.getCode());

//            profileFeature.setContent("rrrrrrrrrrrrrrrrrrrrrrrrrrrrr");
          //  profileFeature.setProfileId(1);
//            profileFeature.setFeature(feature);
            profileFeatureList.add(profileFeature);

        }
        return profileFeatureList;
    }
}
