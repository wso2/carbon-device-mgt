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

package org.wso2.carbon.device.mgt.jaxrs.util;

import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.device.mgt.jaxrs.beans.ProfileFeature;
import org.wso2.carbon.policy.mgt.common.Profile;

import java.util.ArrayList;
import java.util.List;

public class DeviceMgtUtil {

    public static Profile convertProfile(org.wso2.carbon.device.mgt.jaxrs.beans.Profile mdmProfile) {
        Profile profile = new Profile();
        profile.setTenantId(mdmProfile.getTenantId());
        profile.setCreatedDate(mdmProfile.getCreatedDate());
        profile.setDeviceType(mdmProfile.getDeviceType());

        List<org.wso2.carbon.policy.mgt.common.ProfileFeature> profileFeatures =
                new ArrayList<org.wso2.carbon.policy.
                        mgt.common.ProfileFeature>(mdmProfile.getProfileFeaturesList().size());
        for (ProfileFeature mdmProfileFeature : mdmProfile.getProfileFeaturesList()) {
            profileFeatures.add(convertProfileFeature(mdmProfileFeature));
        }
        profile.setProfileFeaturesList(profileFeatures);
        profile.setProfileId(mdmProfile.getProfileId());
        profile.setProfileName(mdmProfile.getProfileName());
        profile.setUpdatedDate(mdmProfile.getUpdatedDate());
        return profile;
    }

    public static org.wso2.carbon.policy.mgt.common.ProfileFeature convertProfileFeature(ProfileFeature
                                                                                                 mdmProfileFeature) {

        org.wso2.carbon.policy.mgt.common.ProfileFeature profileFeature =
                new org.wso2.carbon.policy.mgt.common.ProfileFeature();
        profileFeature.setProfileId(mdmProfileFeature.getProfileId());
        profileFeature.setContent(mdmProfileFeature.getPayLoad());
        profileFeature.setDeviceType(mdmProfileFeature.getDeviceTypeId());
        profileFeature.setFeatureCode(mdmProfileFeature.getFeatureCode());
        profileFeature.setId(mdmProfileFeature.getId());
        return profileFeature;

    }

    public static List<Scope> convertScopestoAPIScopes(List<org.wso2.carbon.device.mgt.jaxrs.beans.Scope> scopes) {
        List<Scope> convertedScopes = new ArrayList<>();
        Scope convertedScope;
        for (org.wso2.carbon.device.mgt.jaxrs.beans.Scope scope : scopes) {
            convertedScope = new Scope();
            convertedScope.setKey(scope.getKey());
            convertedScope.setName(scope.getName());
            convertedScope.setDescription(scope.getDescription());
            convertedScope.setRoles(scope.getRoles());
            convertedScopes.add(convertedScope);
        }
        return convertedScopes;
    }

    public static List<org.wso2.carbon.device.mgt.jaxrs.beans.Scope> convertAPIScopestoScopes(List<Scope> scopes) {
        List<org.wso2.carbon.device.mgt.jaxrs.beans.Scope> convertedScopes = new ArrayList<>();
        org.wso2.carbon.device.mgt.jaxrs.beans.Scope convertedScope;
        for (Scope scope : scopes) {
            convertedScope = new org.wso2.carbon.device.mgt.jaxrs.beans.Scope();
            convertedScope.setKey(scope.getKey());
            convertedScope.setName(scope.getName());
            convertedScope.setDescription(scope.getDescription());
            convertedScope.setRoles(scope.getRoles());
            convertedScopes.add(convertedScope);
        }
        return convertedScopes;
    }
}