/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.policy.mgt.core.mgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.powermock.api.mockito.PowerMockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.IllegalTransactionStateException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.core.BasePolicyManagementDAOTest;
import org.wso2.carbon.policy.mgt.core.PolicyManagerServiceImpl;
import org.wso2.carbon.policy.mgt.core.dao.FeatureDAO;
import org.wso2.carbon.policy.mgt.core.dao.FeatureManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.ProfileDAO;
import org.wso2.carbon.policy.mgt.core.dao.ProfileManagerDAOException;
import org.wso2.carbon.policy.mgt.core.mgt.FeatureManager;
import org.wso2.carbon.policy.mgt.core.mock.TypeXDeviceManagementService;
import org.wso2.carbon.policy.mgt.core.util.FeatureCreator;
import org.wso2.carbon.policy.mgt.core.util.ProfileCreator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeatureManagerImplTest extends BasePolicyManagementDAOTest {

    private static final Log log = LogFactory.getLog(PolicyManagerServiceImpl.class);

    private static final String DEVICE4 = "device4";
    private static final String GROUP4 = "group4";
    private static final String POLICY4 = "policy4";
    private static final String DEVICE_TYPE_D = "deviceTypeD";

    private OperationManager operationManager;
    private FeatureManager featureManager;
    private Profile profile1;
    private List<ProfileFeature> profileFeaturesList1;

    @BeforeClass
    public void initialize() throws Exception {
        log.info("Initializing feature manager tests");
        super.initializeServices();

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        deviceMgtService.registerDeviceType(new TypeXDeviceManagementService(DEVICE_TYPE_D));
        operationManager = new OperationManagerImpl(DEVICE_TYPE_D);
        featureManager = new FeatureManagerImpl();

        enrollDevice(DEVICE4, DEVICE_TYPE_D);
        createDeviceGroup(GROUP4);
        DeviceGroup group4 = groupMgtService.getGroup(GROUP4);
        addDeviceToGroup(new DeviceIdentifier(DEVICE4, DEVICE_TYPE_D), GROUP4);

        Profile profile = new Profile();
        profile.setTenantId(tenantId);
        profile.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        profile.setDeviceType(DEVICE_TYPE_D);
    }

    @Test(description = "This test case tests handling UnsupportedOperationException when adding new profile feature",
          expectedExceptions = {UnsupportedOperationException.class})
    public void testAddProfileFeature() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        profile1 = profileManager.addProfile(profile);
        ProfileFeature profileFeature = profile.getProfileFeaturesList().get(0);
        featureManager.addProfileFeature(profileFeature, profile1.getProfileId());
    }


    @Test(description = "This test case tests adding new profile feature to a non existent profile",
          dependsOnMethods = "testAddProfileFeature",
          expectedExceptions = {FeatureManagementException.class})
    public void testAddProfileFeatureThrowingFeatureManagementException() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        int nonExistentProfileId = 9999;
        ProfileFeature profileFeature = profile.getProfileFeaturesList().get(0);
        //Adding profile
        featureManager.addProfileFeature(profileFeature, nonExistentProfileId);
    }

    @Test(description = "This test case tests handling ProfileManagerDAOException when adding new profile feature",
          dependsOnMethods = "testAddProfileFeature")
    public void testAddProfileFeatureThrowingProfileManagerDAOException() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        profile1 = profileManager.addProfile(profile);

        ProfileDAO profileDAO = mock(ProfileDAO.class);
        when(profileDAO.getProfile(anyInt())).thenThrow(new ProfileManagerDAOException());

        ProfileFeature profileFeature = profile.getProfileFeaturesList().get(0);
        testThrowingException(featureManager,
                              profileFeature,
                              p -> featureManager.addProfileFeature((ProfileFeature) p, profile1.getProfileId()),
                              "profileDAO", profileDAO,
                              ProfileManagerDAOException.class);
    }

    @Test(description = "This test case tests handling FeatureManagerDAOException when adding new profile feature",
          dependsOnMethods = "testAddProfileFeatureThrowingProfileManagerDAOException")
    public void testAddProfileFeatureThrowingFeatureManagerDAOException() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        profile1 = profileManager.addProfile(profile);

        FeatureDAO featureDAO = mock(FeatureDAO.class);
        when(featureDAO.addProfileFeature(any(ProfileFeature.class), anyInt())).thenThrow(
                new FeatureManagerDAOException());

        ProfileFeature profileFeature = profile.getProfileFeaturesList().get(0);
        testThrowingException(featureManager,
                              profileFeature,
                              p -> featureManager.addProfileFeature((ProfileFeature) p, profile1.getProfileId()),
                              "featureDAO", featureDAO,
                              FeatureManagerDAOException.class);
    }

    @Test(description = "This test case tests handling SQLException when adding new profile feature",
          dependsOnMethods = "testAddProfileFeatureThrowingFeatureManagerDAOException",
          expectedExceptions = IllegalTransactionStateException.class)
    public void testAddProfileThrowingIllegalTransactionStateException() throws Exception {
        //Creating profile object
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        ProfileFeature profileFeature = profile.getProfileFeaturesList().get(0);

        Pair<Connection, Pair<DataSource, DataSource>> pair = mockConnection();
        PowerMockito.doThrow(new SQLException()).when(pair.first()).setAutoCommit(anyBoolean());
        try {
            featureManager.addProfileFeature(profileFeature, profile.getProfileId());
        } finally {
            PolicyManagementDAOFactory.init(pair.second().first());
        }
    }

    @Test(description = "This test case tests adding new profile features",
          dependsOnMethods = "testAddProfileThrowingIllegalTransactionStateException")
    public void testAddProfileFeatures() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        //Adding profile
        profile1 = profileManager.addProfile(profile);
        profileFeaturesList1 = featureManager.addProfileFeatures(profile.getProfileFeaturesList(),
                                                                 profile1.getProfileId());
        Assert.assertEquals(profileFeaturesList1.size(), profile.getProfileFeaturesList().size());
    }

    @Test(description = "This test case tests adding new profile features to a non existent profile",
          dependsOnMethods = "testAddProfileFeatures",
          expectedExceptions = {FeatureManagementException.class})
    public void testAddProfileFeaturesThrowingFeatureManagementException() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        int nonExistentProfileId = 9999;
        //Adding profile
        featureManager.addProfileFeatures(profile.getProfileFeaturesList(), nonExistentProfileId);
    }

    @Test(description = "This test case tests handling ProfileManagerDAOException when adding new profile feature",
          dependsOnMethods = "testAddProfileFeatures")
    public void testAddProfileFeaturesThrowingProfileManagerDAOException() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        profile1 = profileManager.addProfile(profile);

        ProfileDAO profileDAO = mock(ProfileDAO.class);
        when(profileDAO.getProfile(anyInt())).thenThrow(new ProfileManagerDAOException());

        List<ProfileFeature> profileFeaturesList = profile.getProfileFeaturesList();
        testThrowingException(featureManager,
                              profileFeaturesList,
                              p -> featureManager.addProfileFeatures((List<ProfileFeature>) p, profile1.getProfileId()),
                              "profileDAO", profileDAO,
                              ProfileManagerDAOException.class);
    }

    @Test(description = "This test case tests handling FeatureManagerDAOException when adding new profile feature",
          dependsOnMethods = "testAddProfileFeaturesThrowingProfileManagerDAOException")
    public void testAddProfileFeaturesThrowingFeatureManagerDAOException() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        profile1 = profileManager.addProfile(profile);

        FeatureDAO featureDAO = mock(FeatureDAO.class);
        when(featureDAO.addProfileFeature(any(ProfileFeature.class), anyInt())).thenThrow(
                new FeatureManagerDAOException());

        List<ProfileFeature> profileFeaturesList = profile.getProfileFeaturesList();
        testThrowingException(featureManager,
                              profileFeaturesList,
                              p -> featureManager.addProfileFeatures((List<ProfileFeature>) p, profile1.getProfileId()),
                              "featureDAO", featureDAO,
                              FeatureManagerDAOException.class);
    }

    @Test(description = "This test case tests handling SQLException when adding new profile feature",
          dependsOnMethods = "testAddProfileFeaturesThrowingFeatureManagerDAOException",
          expectedExceptions = IllegalTransactionStateException.class)
    public void testAddProfileFeaturesThrowingIllegalTransactionStateException() throws Exception {
        //Creating profile object
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        List<ProfileFeature> profileFeaturesList = profile.getProfileFeaturesList();

        Pair<Connection, Pair<DataSource, DataSource>> pair = mockConnection();
        PowerMockito.doThrow(new SQLException()).when(pair.first()).setAutoCommit(anyBoolean());
        try {
            featureManager.addProfileFeatures(profileFeaturesList, profile.getProfileId());
        } finally {
            PolicyManagementDAOFactory.init(pair.second().first());
        }
    }

    @Test(description = "This test case tests handling UnsupportedOperationException when updating a profile feature",
          expectedExceptions = {UnsupportedOperationException.class})
    public void testUpdateProfileFeature() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        profile1 = profileManager.addProfile(profile);
        ProfileFeature profileFeature = profile.getProfileFeaturesList().get(0);
        featureManager.updateProfileFeature(profileFeature, profile1.getProfileId());
    }

    @Test(description = "This test case tests updating a non existent profile feature",
          expectedExceptions = {FeatureManagementException.class},
          dependsOnMethods = "testUpdateProfileFeature")
    public void testUpdateProfileFeatureThrowingFeatureManagementException() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        int nonExistentProfileId = 9999;
        ProfileFeature profileFeature = profile.getProfileFeaturesList().get(0);
        featureManager.updateProfileFeature(profileFeature, nonExistentProfileId);
    }

    @Test(description = "This test case tests handling ProfileManagerDAOException when adding new profile feature",
          dependsOnMethods = "testAddProfileFeature")
    public void testUpdateProfileFeatureThrowingProfileManagerDAOException() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        profile1 = profileManager.addProfile(profile);

        ProfileDAO profileDAO = mock(ProfileDAO.class);
        when(profileDAO.getProfile(anyInt())).thenThrow(new ProfileManagerDAOException());

        ProfileFeature profileFeature = profile.getProfileFeaturesList().get(0);
        testThrowingException(featureManager,
                              profileFeature,
                              p -> featureManager.updateProfileFeature((ProfileFeature) p, profile1.getProfileId()),
                              "profileDAO", profileDAO,
                              ProfileManagerDAOException.class);
    }

    @Test(description = "This test case tests handling FeatureManagerDAOException when adding new profile feature",
          dependsOnMethods = "testUpdateProfileFeatureThrowingProfileManagerDAOException")
    public void testUpdateProfileFeatureThrowingFeatureManagerDAOException() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        profile1 = profileManager.addProfile(profile);

        FeatureDAO featureDAO = mock(FeatureDAO.class);
        when(featureDAO.updateProfileFeature(any(ProfileFeature.class), anyInt())).thenThrow(
                new FeatureManagerDAOException());

        ProfileFeature profileFeature = profile.getProfileFeaturesList().get(0);
        testThrowingException(featureManager,
                              profileFeature,
                              p -> featureManager.updateProfileFeature((ProfileFeature) p, profile1.getProfileId()),
                              "featureDAO", featureDAO,
                              FeatureManagerDAOException.class);
    }

    @Test(description = "This test case tests updating profile features",
          dependsOnMethods = "testAddProfileFeatures")
    public void testUpdateProfileFeatures() throws Exception {
        String newFeatureCode = "C002";
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        int createdProfileId = profileManager.addProfile(profile).getProfileId();
        profileFeaturesList1.get(0).setFeatureCode(newFeatureCode);
        List<ProfileFeature> updatedProfileFeatures = featureManager.updateProfileFeatures(profileFeaturesList1,
                                                                                           createdProfileId);
        Assert.assertEquals(updatedProfileFeatures.get(0).getFeatureCode(), newFeatureCode);
    }

    @Test(description = "This test case tests handling FeatureManagementException when updating profile features",
          dependsOnMethods = "testUpdateProfileFeatures",
          expectedExceptions = {FeatureManagementException.class})
    public void testUpdateProfileFeaturesThrowingFeatureManagementException() throws Exception {
        String newFeatureCode = "C002";
        int nonExistentProfileId = 9999;
        profileFeaturesList1.get(0).setFeatureCode(newFeatureCode);
        List<ProfileFeature> updatedProfileFeatures = featureManager.updateProfileFeatures(profileFeaturesList1,
                                                                                           nonExistentProfileId);
        Assert.assertEquals(updatedProfileFeatures.get(0).getFeatureCode(), newFeatureCode);
    }

    @Test(description = "This test case tests handling ProfileManagerDAOException when adding new profile feature",
          dependsOnMethods = "testUpdateProfileFeaturesThrowingFeatureManagementException")
    public void testUpdateProfileFeaturesThrowingProfileManagerDAOException() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        profile1 = profileManager.addProfile(profile);

        ProfileDAO profileDAO = mock(ProfileDAO.class);
        when(profileDAO.getProfile(anyInt())).thenThrow(new ProfileManagerDAOException());

        List<ProfileFeature> profileFeaturesList = profile.getProfileFeaturesList();
        testThrowingException(featureManager,
                              profileFeaturesList,
                              p -> featureManager
                                      .updateProfileFeatures((List<ProfileFeature>) p, profile1.getProfileId()),
                              "profileDAO", profileDAO,
                              ProfileManagerDAOException.class);
    }

    @Test(description = "This test case tests handling FeatureManagerDAOException when adding new profile feature",
          dependsOnMethods = "testUpdateProfileFeaturesThrowingProfileManagerDAOException")
    public void testUpdateProfileFeaturesThrowingFeatureManagerDAOException() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        profile1 = profileManager.addProfile(profile);

        FeatureDAO featureDAO = mock(FeatureDAO.class);
        when(featureDAO.addProfileFeature(any(ProfileFeature.class), anyInt())).thenThrow(
                new FeatureManagerDAOException());

        List<ProfileFeature> profileFeaturesList = profile.getProfileFeaturesList();
        testThrowingException(featureManager,
                              profileFeaturesList,
                              p -> featureManager
                                      .updateProfileFeatures((List<ProfileFeature>) p, profile1.getProfileId()),
                              "featureDAO", featureDAO,
                              FeatureManagerDAOException.class);
    }

    @Test(description = "This test case tests handling SQLException when adding new profile feature",
          dependsOnMethods = "testUpdateProfileFeaturesThrowingFeatureManagerDAOException",
          expectedExceptions = IllegalTransactionStateException.class)
    public void testUpdateProfileFeaturesThrowingIllegalTransactionStateException() throws Exception {
        //Creating profile object
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        List<ProfileFeature> profileFeaturesList = profile.getProfileFeaturesList();

        Pair<Connection, Pair<DataSource, DataSource>> pair = mockConnection();
        PowerMockito.doThrow(new SQLException()).when(pair.first()).setAutoCommit(anyBoolean());
        try {
            featureManager.updateProfileFeatures(profileFeaturesList, profile.getProfileId());
        } finally {
            PolicyManagementDAOFactory.init(pair.second().first());
        }
    }

    @Test(description = "This test case tests retrieving all features of a device type",
          dependsOnMethods = "testUpdateProfileFeaturesThrowingIllegalTransactionStateException",
          expectedExceptions = FeatureManagementException.class)
    public void testGetAllFeatures() throws Exception {
        featureManager.getAllFeatures(DEVICE_TYPE_D);
    }

    @Test(description = "This test case tests handling SQLException when all features of a device type",
          dependsOnMethods = "testGetAllFeatures",
          expectedExceptions = {IllegalTransactionStateException.class, FeatureManagementException.class})
    public void testGetAllFeaturesThrowingIllegalTransactionStateException() throws Exception {
        Pair<Connection, Pair<DataSource, DataSource>> pair = mockConnection();
        PowerMockito.doThrow(new SQLException()).when(pair.second().second()).getConnection();
        try {
            featureManager.getAllFeatures(DEVICE_TYPE_D);
        } finally {
            PolicyManagementDAOFactory.init(pair.second().first());
        }
    }

    @Test(description = "This test case tests retrieving features of a profile",
          dependsOnMethods = "testGetAllFeaturesThrowingIllegalTransactionStateException")
    public void testGetFeaturesForProfile() throws Exception {
        featureManager.getFeaturesForProfile(profile1.getProfileId());
    }

    @Test(description = "This test case tests retrieving features to a non existent profile",
          dependsOnMethods = "testGetFeaturesForProfile",
          expectedExceptions = {FeatureManagementException.class})
    public void testGetFeaturesForProfileThrowingFeatureManagementException() throws Exception {
        int nonExistentProfileId = 9999;
        featureManager.getFeaturesForProfile(nonExistentProfileId);
    }

    @Test(description = "This test case tests handling ProfileManagerDAOException when retrieving features of a " +
            "profile",
          dependsOnMethods = "testGetFeaturesForProfile")
    public void testGetFeaturesForProfileThrowingProfileManagerDAOException() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        profile1 = profileManager.addProfile(profile);

        ProfileDAO profileDAO = mock(ProfileDAO.class);
        when(profileDAO.getProfile(anyInt())).thenThrow(new ProfileManagerDAOException());

        testThrowingException(featureManager,
                              null,
                              p -> featureManager.getFeaturesForProfile(profile1.getProfileId()),
                              "profileDAO", profileDAO,
                              ProfileManagerDAOException.class);
    }

    @Test(description = "This test case tests handling FeatureManagerDAOException when retrieving features of a " +
            "profile",
          dependsOnMethods = "testGetFeaturesForProfileThrowingProfileManagerDAOException")
    public void testGetFeaturesForProfileThrowingFeatureManagerDAOException() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        profile1 = profileManager.addProfile(profile);

        FeatureDAO featureDAO = mock(FeatureDAO.class);
        when(featureDAO.addProfileFeature(any(ProfileFeature.class), anyInt())).thenThrow(
                new FeatureManagerDAOException());

        testThrowingException(featureManager,
                              null,
                              p -> featureManager.getFeaturesForProfile(profile1.getProfileId()),
                              "featureDAO", featureDAO,
                              FeatureManagerDAOException.class);
    }

    @Test(description = "This test case tests handling SQLException when retrieving features of a profile",
          dependsOnMethods = "testGetFeaturesForProfileThrowingFeatureManagerDAOException",
          expectedExceptions = IllegalTransactionStateException.class)
    public void testGetFeaturesForProfileThrowingIllegalTransactionStateException() throws Exception {
        Pair<Connection, Pair<DataSource, DataSource>> pair = mockConnection();
        PowerMockito.doThrow(new SQLException()).when(pair.second().second()).getConnection();
        try {
            featureManager.getFeaturesForProfile(profile1.getProfileId());
        } finally {
            PolicyManagementDAOFactory.init(pair.second().first());
        }
    }

    @Test(description = "This test case tests handling FeatureManagerDAOException when deleting features of a profile",
          dependsOnMethods = "testGetFeaturesForProfile")
    public void testDeleteFeaturesOfProfileThrowingFeatureManagerDAOException() throws Exception {
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_D);
        profile1 = profileManager.addProfile(profile);

        FeatureDAO featureDAO = mock(FeatureDAO.class);
        when(featureDAO.deleteFeaturesOfProfile(any(Profile.class))).thenThrow(new FeatureManagerDAOException());

        testThrowingException(featureManager,
                              profile1,
                              p -> featureManager.deleteFeaturesOfProfile(profile),
                              "featureDAO", featureDAO,
                              FeatureManagerDAOException.class);
    }

    @Test(description = "This test case tests handling SQLException when deleting features of a profile",
          dependsOnMethods = "testDeleteFeaturesOfProfileThrowingFeatureManagerDAOException",
          expectedExceptions = IllegalTransactionStateException.class)
    public void testDeleteFeaturesOfProfileThrowingIllegalTransactionStateException() throws Exception {
        Pair<Connection, Pair<DataSource, DataSource>> pair = mockConnection();
        PowerMockito.doThrow(new SQLException()).when(pair.second().second()).getConnection();
        try {
            featureManager.deleteFeaturesOfProfile(profile1);
        } finally {
            PolicyManagementDAOFactory.init(pair.second().first());
        }
    }

    @Test(description = "This test case tests deleting features of a profile",
          dependsOnMethods = "testDeleteFeaturesOfProfileThrowingIllegalTransactionStateException")
    public void testDeleteFeaturesOfProfile() throws Exception {
        featureManager.deleteFeaturesOfProfile(profile1);
    }
}