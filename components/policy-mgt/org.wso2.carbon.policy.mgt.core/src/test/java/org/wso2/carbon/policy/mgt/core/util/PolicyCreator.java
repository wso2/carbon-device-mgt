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

import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.Profile;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PolicyCreator {

    public static Policy createPolicy(Profile profile) {
        Policy policy = new Policy();

        policy.setPolicyName("Test_Policy_01");
        policy.setGeneric(true);
        policy.setProfile(profile);
        List<String> users = new ArrayList<String>();
        users.add("Dilshan");
        policy.setUsers(users);

        return policy;
    }


    public static Policy createPolicy2(Profile profile) {
        Policy policy = new Policy();

        policy.setPolicyName("New test Policy");
        policy.setGeneric(true);
        policy.setProfile(profile);
        policy.setDeviceList(DeviceCreator.getDeviceList(DeviceTypeCreator.getDeviceType()));

        List<String> roles = new ArrayList<String>();
        roles.add("Role_01");
        roles.add("Role_02");
        roles.add("Role_03");

        policy.setRoleList(roles);

        List<String> users = new ArrayList<String>();
        users.add("Geeth");
        users.add("Manoj");
        users.add("Milan");
        users.add("Dulitha");

        policy.setUsers(users);

        policy.setLatitude("6.927079");
        policy.setLongitude("79.861243");

/*        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd ");
        java.util.Date date = new java.util.Date();

        policy.setStartDate(new java.sql.Timestamp(date.getDate()));*/

        return policy;
    }


}
