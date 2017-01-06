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


package org.wso2.carbon.policy.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;

import java.util.List;

/**
 * This interface represents the key operations related to policy profile.
 */
public interface ProfileDAO {

    /**
     * This method is used to add a profile.
     *
     * @param profile profile object.
     * @return returns added profile object.
     * @throws ProfileManagerDAOException
     */
    Profile addProfile(Profile profile) throws ProfileManagerDAOException;

    /**
     * This method is used to update a profile
     * @param profile profile object.
     * @return returns updated profile object.
     * @throws ProfileManagerDAOException
     */
    Profile updateProfile(Profile profile) throws ProfileManagerDAOException;

    /**
     * This method is used to remove a profile.
     * @param profile profile object
     * @return returns true if success.
     * @throws ProfileManagerDAOException
     */
    boolean deleteProfile(Profile profile) throws ProfileManagerDAOException;

    /**
     * This method is used to remove a profile of given policy id.
     * @param policyId policy id.
     * @return returns true if success.
     * @throws ProfileManagerDAOException
     */
    boolean deleteProfile(int policyId) throws ProfileManagerDAOException;

    /**
     * This method is used to retrieve a profile when id is given.
     * @param profileId profile id.
     * @return returns profile object.
     * @throws ProfileManagerDAOException
     */
    Profile getProfile(int profileId) throws ProfileManagerDAOException;

    /**
     * This method is used to retrieve all the profiles.
     *
     * @return returns a list of profile objects.
     * @throws ProfileManagerDAOException
     */
    List<Profile> getAllProfiles() throws ProfileManagerDAOException;

    /**
     * This method is used to retrieve all the profile of given device type.
     *
     * @param deviceType device type object.
     * @return retruns list of profiles.
     * @throws ProfileManagerDAOException
     */
    List<Profile> getProfilesOfDeviceType(String deviceType) throws ProfileManagerDAOException;

}
