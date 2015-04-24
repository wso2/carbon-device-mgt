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

import org.wso2.carbon.policy.mgt.common.Feature;

import java.util.ArrayList;
import java.util.List;

public class FeatureCreator {

    public static List<Feature> getFeatureList() {

        Feature feature1 = new Feature();
        feature1.setName("Camera");
        feature1.setCode("C001");
        feature1.setDescription("Camera");
        feature1.setRuleValue("permit_override");
        feature1.setDeviceTypeId(1);


        Feature feature2 = new Feature();
        feature2.setName("LOCK");
        feature2.setCode("L001");
        feature2.setDescription("Lock the phone");
        feature2.setRuleValue("deny_override");
        feature2.setDeviceTypeId(1);


        Feature feature3 = new Feature();
        feature3.setName("WIFI");
        feature3.setCode("W001");
        feature3.setDescription("Wifi configuration for the device");
        feature3.setRuleValue("all_available");
        feature3.setDeviceTypeId(1);

        Feature feature4 = new Feature();
        feature4.setName("RING");
        feature4.setCode("R001");
        feature4.setDescription("Ring the mobile");
        feature4.setRuleValue("first_applicable");
        feature4.setDeviceTypeId(1);

        Feature feature5 = new Feature();
        feature5.setName("LDAP");
        feature5.setCode("L002");
        feature5.setDescription("LDAP Configurations");
        feature5.setRuleValue("all_available");
        feature5.setDeviceTypeId(1);


        Feature feature6 = new Feature();
        feature6.setName("VPN");
        feature6.setCode("V001");
        feature6.setDescription("VPN config for accessing the company network from out side");
        feature6.setRuleValue("all_available");
        feature6.setDeviceTypeId(1);

        Feature feature7 = new Feature();
        feature7.setName("PASSWORD");
        feature7.setCode("P001");
        feature7.setDescription("Setting the password for the mobile");
        feature7.setRuleValue("first_applicable");
        feature7.setDeviceTypeId(1);

        Feature feature8 = new Feature();
        feature8.setName("WIPE");
        feature8.setCode("W002");
        feature8.setDescription("Wiping the company profile created to access the company secure data");
        feature8.setRuleValue("permit_override");
        feature8.setDeviceTypeId(1);

        Feature feature9 = new Feature();
        feature9.setName("ENCRYPTION");
        feature9.setCode("E001");
        feature9.setDescription("Adding the encryption for the phone and SD card.");
        feature9.setRuleValue("permit_override");
        feature9.setDeviceTypeId(1);

        Feature feature10 = new Feature();
        feature10.setName("APP");
        feature10.setCode("A001");
        feature10.setDescription("Installing an application to the phone");
        feature10.setRuleValue("permit_override");
        feature10.setDeviceTypeId(1);

        Feature feature11 = new Feature();
        feature11.setName("EMAIL");
        feature11.setCode("E002");
        feature11.setDescription("Email configurations of the phone.");
        feature11.setRuleValue("all_applicable");
        feature11.setDeviceTypeId(1);


        List<Feature> featureList = new ArrayList<Feature>();
        featureList.add(feature1);
        featureList.add(feature2);
        featureList.add(feature3);
        featureList.add(feature4);
        featureList.add(feature5);
        featureList.add(feature6);
        featureList.add(feature7);
        featureList.add(feature8);
        featureList.add(feature9);
        featureList.add(feature10);
        featureList.add(feature11);

        return featureList;
    }
}
