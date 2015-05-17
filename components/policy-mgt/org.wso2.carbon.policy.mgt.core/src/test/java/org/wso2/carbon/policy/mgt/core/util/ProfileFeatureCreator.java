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
import java.util.Objects;

public class ProfileFeatureCreator {

    public static List<ProfileFeature> getProfileFeature(List<Feature> features) {
        List<ProfileFeature> profileFeatureList = new ArrayList<ProfileFeature>();

        int i = 0;
        for (Feature feature : features) {

            ProfileFeature profileFeature = new ProfileFeature();
            if (i % 2 == 0) {
                profileFeature.setContent( getJSON());
            } else {
                profileFeature.setContent(getJSON2());
            }
            profileFeature.setDeviceTypeId(1);
            profileFeature.setFeatureCode(feature.getCode());

//            profileFeature.setContent("rrrrrrrrrrrrrrrrrrrrrrrrrrrrr");
            //  profileFeature.setProfileId(1);
//            profileFeature.setFeature(feature);
            profileFeatureList.add(profileFeature);

            i++;

        }
        return profileFeatureList;
    }


    private static String getJSON() {
        return "[\n" +
                "\t{\n" +
                "\t\tcolor: \"red\",\n" +
                "\t\tvalue: \"#f00\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\tcolor: \"green\",\n" +
                "\t\tvalue: \"#0f0\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\tcolor: \"blue\",\n" +
                "\t\tvalue: \"#00f\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\tcolor: \"cyan\",\n" +
                "\t\tvalue: \"#0ff\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\tcolor: \"magenta\",\n" +
                "\t\tvalue: \"#f0f\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\tcolor: \"yellow\",\n" +
                "\t\tvalue: \"#ff0\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\tcolor: \"black\",\n" +
                "\t\tvalue: \"#000\"\n" +
                "\t}\n" +
                "]";
    }

    private static String getJSON2() {
        return "{\n" +
                "   \"odata.metadata\":\"http://services.odata.org/V3/OData/OData.svc/$metadata#Products\",\n" +
                "   \"value\":[\n" +
                "      {\n" +
                "         \"ID\":0,\n" +
                "         \"Name\":\"Bread\",\n" +
                "         \"Description\":\"Whole grain bread\",\n" +
                "         \"ReleaseDate\":\"1992-01-01T00:00:00\",\n" +
                "         \"DiscontinuedDate\":null,\n" +
                "         \"Rating\":4,\n" +
                "         \"Price\":\"2.5\"\n" +
                "      },\n" +
                "      {\n" +
                "         \"ID\":1,\n" +
                "         \"Name\":\"Milk\",\n" +
                "         \"Description\":\"Low fat milk\",\n" +
                "         \"ReleaseDate\":\"1995-10-01T00:00:00\",\n" +
                "         \"DiscontinuedDate\":null,\n" +
                "         \"Rating\":3,\n" +
                "         \"Price\":\"3.5\"\n" +
                "      },\n" +
                "      {\n" +
                "         \"ID\":2,\n" +
                "         \"Name\":\"Vint soda\",\n" +
                "         \"Description\":\"Americana Variety - Mix of 6 flavors\",\n" +
                "         \"ReleaseDate\":\"2000-10-01T00:00:00\",\n" +
                "         \"DiscontinuedDate\":null,\n" +
                "         \"Rating\":3,\n" +
                "         \"Price\":\"20.9\"\n" +
                "      },\n" +
                "   …\n" +
                "   ]\n" +
                "}";
    }
}
