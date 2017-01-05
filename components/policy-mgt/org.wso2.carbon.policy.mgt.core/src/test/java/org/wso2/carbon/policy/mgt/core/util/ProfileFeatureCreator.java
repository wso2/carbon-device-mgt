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
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;

import java.util.ArrayList;
import java.util.List;

public class ProfileFeatureCreator {

    public static List<ProfileFeature> getProfileFeature(List<Feature> features) {
        List<ProfileFeature> profileFeatureList = new ArrayList<ProfileFeature>();

        int i = 0;
        for (Feature feature : features) {

            ProfileFeature profileFeature = new ProfileFeature();
            if (i % 2 == 0) {
                profileFeature.setContent(getJSON());
            } else {
                profileFeature.setContent(getJSON2());
            }
            //TODO why assigning a random number below?
            //profileFeature.setDeviceTypeId(1);
            profileFeature.setDeviceType("android");
            profileFeature.setFeatureCode(feature.getCode());

//            profileFeature.setContent("mm");
//            profileFeature.setProfileId(1);
//            profileFeature.setFeature(feature);
            profileFeatureList.add(profileFeature);

            i++;

        }
        return profileFeatureList;
    }


    private static String getJSON() {
        return "{\n" +
                "    \"userNameList\": [\n" +
                "        \"admin\"\n" +
                "    ],\n" +
                "    \"roleNameList\": [\n" +
                "        \"admin\"\n" +
                "    ],\n" +
                "    \"deviceIdentifiers\": [\n" +
                "        {\n" +
                "            \"id\": \"08:00:27:fe:27:7b\",\n" +
                "            \"type\": \"ios\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"application\": {\n" +
                "        \"id\": \"id\",\n" +
                "        \"name\": \"test\",\n" +
                "        \"type\": \"ENTERPRISE\",\n" +
                "        \"platform\": \"android\",\n" +
                "        \"version\": \"1.0\",\n" +
                "        \"identifier\": \"sdfsdfldfs\",\n" +
                "        \"iconImage\": \"http://gogle.com\",\n" +
                "        \"packageName\": \"com.google.mail\",\n" +
                "        \"appIdentifier\": \"asdf\",\n" +
                "        \"location\": \"location\",\n" +
                "        \"properties\": {\n" +
                "            \"isRemoveApp\": true,\n" +
                "            \"isPreventBackup\": true\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }

    private static String getJSON2() {
        return "{\n" +
                "    \"userNameList\": [\n" +
                "        \"admin\"\n" +
                "    ],\n" +
                "    \"roleNameList\": [\n" +
                "        \"admin\"\n" +
                "    ],\n" +
                "    \"deviceIdentifiers\": [\n" +
                "        {\n" +
                "            \"id\": \"11:11:11:12\",\n" +
                "            \"type\": \"ios\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"application\": {\n" +
                "        \"id\": \"1d548206-14ee-4672-91f6-9c230626a056\",\n" +
                "        \"platform\": \"ios\",\n" +
                "        \"packageName\": \"com.imangi.templerun2\",\n" +
                "        \"name\": \"Temle Run\",\n" +
                "        \"appIdentifier\": \"572395608\",\n" +
                "        \"iconImage\": \"http://10.100.5.6:9763/publisher/api/mobileapp/getfile/FHmJReGEV3cExtf.png\",\n" +
                "        \"type\": \"PUBLIC\",\n" +
                "        \"identifier\": \"572395608\",\n" +
                "        \"version\": \"1\",\n" +
                "        \"properties\": {\n" +
                "            \"isRemoveApp\": true,\n" +
                "            \"isPreventBackup\": true,\n" +
                "            \"iTunesId\": 572395608\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }
}
