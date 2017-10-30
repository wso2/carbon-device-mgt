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
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.List;

public class ProfileCreator {

    public static Profile getProfile(List<Feature> features) {
        Profile profile = new Profile();
        profile.setProfileFeaturesList(ProfileFeatureCreator.getProfileFeature(features));
        profile.setProfileName("Test Profile");
        profile.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        profile.setDeviceType("android");

        return profile;
    }

    public static Profile getProfile2(List<Feature> features) {
        Profile profile = new Profile();
        profile.setProfileFeaturesList(ProfileFeatureCreator.getProfileFeature(features));
        profile.setProfileName("Test Profile 2");
        profile.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        profile.setDeviceType("android");

        return profile;
    }

    public static Profile getProfile3(List<Feature> features) {
        Profile profile = new Profile();
        profile.setProfileFeaturesList(ProfileFeatureCreator.getProfileFeature(features));
        profile.setProfileName("Test Profile 3");
        profile.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        profile.setDeviceType("android");

        return profile;
    }

    public static Profile getProfile4(List<Feature> features) {
        Profile profile = new Profile();
        profile.setProfileFeaturesList(ProfileFeatureCreator.getProfileFeature(features));
        profile.setProfileName("Test Profile 4");
        profile.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        profile.setDeviceType("android");

        return profile;
    }


    public static Profile getProfile5(List<Feature> features) {
        Profile profile = new Profile();
        profile.setProfileFeaturesList(ProfileFeatureCreator.getProfileFeature(features));
        profile.setProfileName("Test Profile 5");
        profile.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        profile.setDeviceType("android");

        return profile;
    }
}

