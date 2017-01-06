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

import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.PolicyCriterion;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PolicyCreator {

    public static Policy createPolicy(Profile profile) {
        Policy policy = new Policy();

        policy.setPolicyName("Test_Policy_01");
        policy.setGeneric(true);
        policy.setProfile(profile);
        List<String> users = new ArrayList<String>();
        users.add("Dilshan");
        policy.setUsers(users);
        policy.setCompliance("NOTIFY");
        policy.setOwnershipType("COPE");
        policy.setDescription("This is the first policy.");

        return policy;
    }


    public static Policy createPolicy2(Profile profile) {
        Policy policy = new Policy();

        policy.setPolicyName("Test_Policy_02");
        policy.setGeneric(true);
        policy.setProfile(profile);
        policy.setDevices(DeviceCreator.getDeviceList2(DeviceTypeCreator.getDeviceType()));

        policy.setCompliance("ENFORCE");

        List<String> roles = new ArrayList<String>();
        roles.add("Role_01");
        roles.add("Role_02");
        roles.add("Role_03");

        policy.setRoles(roles);

        List<String> users = new ArrayList<String>();
        users.add("Geeth");
        users.add("Manoj");
        users.add("Milan");
        users.add("Dulitha");

        policy.setUsers(users);


        PolicyCriterion criterion = new PolicyCriterion();

        Properties prop = new Properties();
        prop.put("Start_time", "10.00 AM");
        prop.put("End_time", "4.00 PM");

        criterion.setProperties(prop);
        criterion.setName("Time");


        List<PolicyCriterion> criteria = new ArrayList<PolicyCriterion>();

        criteria.add(criterion);

        policy.setOwnershipType("COPE");

        policy.setPolicyCriterias(criteria);
        policy.setDescription("This is the second policy.");


        return policy;
    }


    public static Policy createPolicy3(Profile profile) {
        Policy policy = new Policy();

        policy.setPolicyName("Test_Policy_03");
        policy.setGeneric(true);
        policy.setProfile(profile);
        policy.setDevices(DeviceCreator.getDeviceList3(DeviceTypeCreator.getDeviceType()));

        List<String> roles = new ArrayList<String>();
        roles.add("Role_01");
        roles.add("Role_02");

        policy.setRoles(roles);
        policy.setCompliance("ENFORCE");
        policy.setOwnershipType("BYOD");



        PolicyCriterion criterion = new PolicyCriterion();

        Properties prop = new Properties();
        prop.put("Start_time", "10.00 AM");
        prop.put("End_time", "4.00 PM");

        criterion.setProperties(prop);
        criterion.setName("Location");


        List<PolicyCriterion> criteria = new ArrayList<PolicyCriterion>();

        criteria.add(criterion);

        policy.setPolicyCriterias(criteria);
        policy.setDescription("This is the third policy.");

        return policy;
    }


    public static Policy createPolicy4(Profile profile) {
        Policy policy = new Policy();

        policy.setPolicyName("Test_Policy_04");
        policy.setGeneric(true);
        policy.setProfile(profile);
        policy.setDevices(DeviceCreator.getDeviceList4(DeviceTypeCreator.getDeviceType()));

        policy.setCompliance("MONITOR");
        policy.setOwnershipType("BYOD");

        List<String> roles = new ArrayList<String>();
        roles.add("Role_04");
        roles.add("Role_05");
        roles.add("Role_02");

        policy.setRoles(roles);

        List<String> users = new ArrayList<String>();
        users.add("Geeth");
        users.add("Manoj");
        users.add("Milan");
        users.add("Dulitha");

        policy.setUsers(users);


        PolicyCriterion criterion = new PolicyCriterion();

        Properties prop = new Properties();
        prop.put("Start_time", "10.00 AM");
        prop.put("End_time", "4.00 PM");

        criterion.setProperties(prop);
        criterion.setName("LOCATION");


        List<PolicyCriterion> criteria = new ArrayList<PolicyCriterion>();

        criteria.add(criterion);

        policy.setPolicyCriterias(criteria);

        policy.setDescription("This is the fourth policy.");


        return policy;
    }


}
