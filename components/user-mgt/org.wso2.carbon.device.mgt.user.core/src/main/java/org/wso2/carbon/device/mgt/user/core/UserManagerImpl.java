/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
*/
package org.wso2.carbon.device.mgt.user.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.user.common.Claims;
import org.wso2.carbon.device.mgt.user.common.Role;
import org.wso2.carbon.device.mgt.user.common.User;
import org.wso2.carbon.device.mgt.user.common.UserManagementException;
import org.wso2.carbon.device.mgt.user.core.internal.DeviceMgtUserDataHolder;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserManagerImpl implements UserManager {

    private static Log log = LogFactory.getLog(UserManagerImpl.class);

    public static final String GIVEN_NAME = UserCoreConstants.ClaimTypeURIs.GIVEN_NAME;
    public static final String EMAIL_ADDRESS = UserCoreConstants.ClaimTypeURIs.EMAIL_ADDRESS;
    public static final String SURNAME = UserCoreConstants.ClaimTypeURIs.SURNAME;
    public static final String STREET_ADDRESS = UserCoreConstants.ClaimTypeURIs.STREET_ADDRESS;
    public static final String LOCALITY = UserCoreConstants.ClaimTypeURIs.LOCALITY;
    public static final String REGION = UserCoreConstants.ClaimTypeURIs.REGION;
    public static final String POSTAL_CODE = UserCoreConstants.ClaimTypeURIs.POSTAL_CODE;
    public static final String COUNTRY = UserCoreConstants.ClaimTypeURIs.COUNTRY;
    public static final String HONE = UserCoreConstants.ClaimTypeURIs.HONE;
    public static final String IM = UserCoreConstants.ClaimTypeURIs.IM;
    public static final String ORGANIZATION = UserCoreConstants.ClaimTypeURIs.ORGANIZATION;
    public static final String URL = UserCoreConstants.ClaimTypeURIs.URL;
    public static final String TITLE = UserCoreConstants.ClaimTypeURIs.TITLE;
    public static final String ROLE = UserCoreConstants.ClaimTypeURIs.ROLE;
    public static final String MOBILE = UserCoreConstants.ClaimTypeURIs.MOBILE;
    public static final String NICKNAME = UserCoreConstants.ClaimTypeURIs.NICKNAME;
    public static final String DATE_OF_BIRTH = UserCoreConstants.ClaimTypeURIs.DATE_OF_BIRTH;
    public static final String GENDER = UserCoreConstants.ClaimTypeURIs.GENDER;
    public static final String ACCOUNT_STATUS = UserCoreConstants.ClaimTypeURIs.ACCOUNT_STATUS;
    public static final String CHALLENGE_QUESTION_URI = UserCoreConstants.ClaimTypeURIs.CHALLENGE_QUESTION_URI;
    public static final String IDENTITY_CLAIM_URI = UserCoreConstants.ClaimTypeURIs.IDENTITY_CLAIM_URI;
    public static final String TEMPORARY_EMAIL_ADDRESS = UserCoreConstants.ClaimTypeURIs.TEMPORARY_EMAIL_ADDRESS;

    public static final String[] DEFAULT_CLAIM_ARR = new String[]{GIVEN_NAME,EMAIL_ADDRESS,SURNAME,STREET_ADDRESS,
            LOCALITY,REGION,REGION,POSTAL_CODE,COUNTRY,HONE,IM,ORGANIZATION,URL,TITLE,ROLE,MOBILE,NICKNAME,
            DATE_OF_BIRTH,GENDER,ACCOUNT_STATUS,CHALLENGE_QUESTION_URI,IDENTITY_CLAIM_URI,TEMPORARY_EMAIL_ADDRESS};

  //  private static final String CLAIM_URL_

    @Override
    public List<User> getUsersForTenantAndRole(int tenantId, String roleName) throws UserManagementException {

        UserStoreManager userStoreManager;
        String[] userNames;
        ArrayList usersList = new ArrayList();

        try {
            userStoreManager = DeviceMgtUserDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();

            userNames = userStoreManager.getUserListOfRole(roleName);
            User newUser;
            for (String userName : userNames) {
                newUser = new User(userName);
                setUserClaims(newUser, userStoreManager.getUserClaimValues(userName, DEFAULT_CLAIM_ARR,
                        UserCoreConstants.DEFAULT_PROFILE));
                usersList.add(newUser);
            }
        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in fetching user list for role and tenant tenant id:" + tenantId
                    + " role name:" + roleName;
            log.error(errorMsg, userStoreEx);
            throw new UserManagementException(errorMsg, userStoreEx);
        }
        return usersList;
    }

    @Override
    public List<Role> getRolesForTenant(int tenantId) throws UserManagementException {

        String[] roleNames;
        ArrayList<Role> rolesList = new ArrayList<Role>();
        Role newRole;
        try {
            UserStoreManager userStoreManager = DeviceMgtUserDataHolder.getInstance().getRealmService()
                    .getTenantUserRealm(tenantId)
                    .getUserStoreManager();

            roleNames = userStoreManager.getRoleNames();
            for (String roleName : roleNames) {
                newRole = new Role(roleName);
                rolesList.add(newRole);
            }

        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in fetching user list for role and tenant tenant id:" + tenantId;
            log.error(errorMsg, userStoreEx);
            throw new UserManagementException(errorMsg, userStoreEx);
        }
        return rolesList;
    }

    @Override
    public List<User> getUsersForTenant(int tenantId) throws UserManagementException {

        UserStoreManager userStoreManager;
        String[] userNames;
        ArrayList usersList = new ArrayList();

        try {
            userStoreManager = DeviceMgtUserDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();

            userNames = userStoreManager.listUsers("",-1);
            User newUser;
            for (String userName : userNames) {
                newUser = new User(userName);
                setUserClaims(newUser, userStoreManager.getUserClaimValues(userName, DEFAULT_CLAIM_ARR,
                        UserCoreConstants.DEFAULT_PROFILE));
                usersList.add(newUser);
            }
        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in fetching user list for tenant id:" + tenantId;
            log.error(errorMsg, userStoreEx);
            throw new UserManagementException(errorMsg, userStoreEx);
        }

        return usersList;
    }

    private void setUserClaims(User newUser, Map<String, String> claimMap) {

        newUser.setRoleName(UserCoreConstants.ClaimTypeURIs.ROLE);
        newUser.setAccountStatus(claimMap.get(ACCOUNT_STATUS));
        newUser.setChallengeQuestion(claimMap.get(CHALLENGE_QUESTION_URI));
        newUser.setCountry(claimMap.get(COUNTRY));
        newUser.setDateOfBirth(claimMap.get(DATE_OF_BIRTH));
        newUser.setEmail(claimMap.get(EMAIL_ADDRESS));
        newUser.setFirstName(claimMap.get(GIVEN_NAME));
        newUser.setGender(claimMap.get(GENDER));
        newUser.setHone(claimMap.get(HONE));
        newUser.setIm(claimMap.get(IM));
        newUser.setIdentityClaimUri(claimMap.get(IDENTITY_CLAIM_URI));
        newUser.setLastName(claimMap.get(SURNAME));
        newUser.setLocality(claimMap.get(LOCALITY));
        newUser.setEmail(claimMap.get(EMAIL_ADDRESS));
        newUser.setMobile(claimMap.get(MOBILE));
        newUser.setNickName(claimMap.get(NICKNAME));
        newUser.setOrganization(claimMap.get(ORGANIZATION));
        newUser.setPostalCode(claimMap.get(POSTAL_CODE));
        newUser.setRegion(claimMap.get(REGION));
        newUser.setStreatAddress(claimMap.get(STREET_ADDRESS));
        newUser.setTitle(claimMap.get(TITLE));
        newUser.setTempEmailAddress(claimMap.get(TEMPORARY_EMAIL_ADDRESS));
    }

}
