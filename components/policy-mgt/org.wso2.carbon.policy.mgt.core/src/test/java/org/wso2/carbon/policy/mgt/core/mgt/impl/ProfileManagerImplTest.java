package org.wso2.carbon.policy.mgt.core.mgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.powermock.api.mockito.PowerMockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.IllegalTransactionStateException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderServiceImpl;
import org.wso2.carbon.policy.mgt.common.PolicyEvaluationPoint;
import org.wso2.carbon.policy.mgt.common.ProfileManagementException;
import org.wso2.carbon.policy.mgt.core.BasePolicyManagementDAOTest;
import org.wso2.carbon.policy.mgt.core.PolicyManagerServiceImpl;
import org.wso2.carbon.policy.mgt.core.dao.FeatureDAO;
import org.wso2.carbon.policy.mgt.core.dao.FeatureManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.ProfileDAO;
import org.wso2.carbon.policy.mgt.core.dao.ProfileManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.impl.ProfileDAOImpl;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mock.TypeXDeviceManagementService;
import org.wso2.carbon.policy.mgt.core.services.SimplePolicyEvaluationTest;
import org.wso2.carbon.policy.mgt.core.util.FeatureCreator;
import org.wso2.carbon.policy.mgt.core.util.ProfileCreator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProfileManagerImplTest extends BasePolicyManagementDAOTest {
    private static final Log log = LogFactory.getLog(PolicyManagerServiceImpl.class);

    private static final String DEVICE3 = "device3";
    private static final String GROUP3 = "group3";
    private static final String POLICY3 = "policy3";
    private static final String DEVICE_TYPE_C = "deviceTypeC";

    private Profile profile1;
    private OperationManager operationManager;

    @BeforeClass
    public void initialize() throws Exception {
        log.info("Initializing policy manager tests");
        super.initializeServices();
        deviceMgtService.registerDeviceType(new TypeXDeviceManagementService(DEVICE_TYPE_C));
        operationManager = new OperationManagerImpl(DEVICE_TYPE_C);
        enrollDevice(DEVICE3, DEVICE_TYPE_C);
        createDeviceGroup(GROUP3);
        DeviceGroup group1 = groupMgtService.getGroup(GROUP3);
        addDeviceToGroup(new DeviceIdentifier(DEVICE3, DEVICE_TYPE_C), GROUP3);
    }

    @Test(description = "This test case tests adding new profile")
    public void testAddProfile() throws Exception {
        //Creating profile object
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_C);
        //Adding profile
        profile1 = profileManager.addProfile(profile);
        Assert.assertEquals(profile1.getProfileName(), profile.getProfileName());
        Assert.assertEquals(profile1.getTenantId(), profile.getTenantId());
        Assert.assertEquals(profile1.getDeviceType(), profile.getDeviceType());
    }

    @Test(description = "This test case tests handling ProfileManagerDAOException when adding new profile",
          dependsOnMethods = "testAddProfile")
    public void testAddProfileThrowingProfileManagerDAOException() throws Exception {
        ProfileDAO profileDAO = mock(ProfileDAOImpl.class);
        when(profileDAO.addProfile(any(Profile.class))).thenThrow(new ProfileManagerDAOException());
        //Creating profile object
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_C);
        testThrowingException(profile, p -> profileManager.addProfile(p), "profileDAO", profileDAO,
                              ProfileManagerDAOException.class);
    }

    @Test(description = "This test case tests handling FeatureManagerDAOException when adding new profile",
          dependsOnMethods = "testAddProfileThrowingProfileManagerDAOException")
    public void testAddProfileThrowingFeatureManagerDAOException() throws Exception {
        FeatureDAO featureDAO = mock(FeatureDAO.class);
        when(featureDAO.addProfileFeatures(anyListOf(ProfileFeature.class), anyInt())).thenThrow(
                new FeatureManagerDAOException());
        //Creating profile object
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_C);
        testThrowingException(profile, p -> profileManager.addProfile(p), "featureDAO", featureDAO,
                              FeatureManagerDAOException.class);
    }

    @Test(description = "This test case tests handling SQLException when adding new profile",
          dependsOnMethods = "testAddProfileThrowingFeatureManagerDAOException",
          expectedExceptions = IllegalTransactionStateException.class)
    public void testAddProfileThrowingIllegalTransactionStateException() throws Exception {
        //Creating profile object
        Profile profile = ProfileCreator.getProfile(FeatureCreator.getFeatureList(), DEVICE_TYPE_C);
        Pair<Connection, Pair<DataSource, DataSource>> pair = mockConnection();
        PowerMockito.doThrow(new SQLException()).when(pair.first()).setAutoCommit(anyBoolean());
        try {
            profileManager.addProfile(profile);
        } finally {
            PolicyManagementDAOFactory.init(pair.second().first());
        }
    }

    @Test(description = "This test case tests updating profile",
          dependsOnMethods = "testAddProfile")
    public void testUpdateProfile() throws Exception {
        String newProfileName = "Updated Test Profile";
        Profile savedProfile = profileManager.getProfile(profile1.getProfileId());
        savedProfile.setProfileName(newProfileName);
        Profile updateProfile = profileManager.updateProfile(savedProfile);
        Assert.assertEquals(updateProfile.getProfileName(), newProfileName);
    }

    @Test(description = "This test case tests handling ProfileManagerDAOException when updating profile",
          dependsOnMethods = "testUpdateProfile")
    public void testUpdateProfileThrowingProfileManagerDAOException() throws Exception {
        ProfileDAO profileDAO = mock(ProfileDAOImpl.class);
        when(profileDAO.updateProfile(any(Profile.class))).thenThrow(new ProfileManagerDAOException());

        String newProfileName = "Updated Test Profile";
        Profile savedProfile = profileManager.getProfile(profile1.getProfileId());
        savedProfile.setProfileName(newProfileName);
        testThrowingException(savedProfile, p -> profileManager.updateProfile(p), "profileDAO", profileDAO,
                              ProfileManagerDAOException.class);
    }

    @Test(description = "This test case tests handling FeatureManagerDAOException when updating profile",
          dependsOnMethods = "testUpdateProfileThrowingProfileManagerDAOException")
    public void testUpdateProfileThrowingFeatureManagerDAOException() throws Exception {
        FeatureDAO featureDAO = mock(FeatureDAO.class);
        when(featureDAO.updateProfileFeatures(anyListOf(ProfileFeature.class), anyInt())).thenThrow(
                new FeatureManagerDAOException());

        String newProfileName = "Updated Test Profile";
        Profile savedProfile = profileManager.getProfile(profile1.getProfileId());
        savedProfile.setProfileName(newProfileName);
        testThrowingException(savedProfile, p -> profileManager.updateProfile(p), "featureDAO", featureDAO,
                              FeatureManagerDAOException.class);
    }

    @Test(description = "This test case tests handling SQLException when updating profile",
          dependsOnMethods = {"testUpdateProfileThrowingFeatureManagerDAOException"},
          expectedExceptions = IllegalTransactionStateException.class)
    public void testUpdateProfileThrowingIllegalTransactionStateException() throws Exception {
        //Retrieving profile object
        Profile savedProfile = profileManager.getProfile(profile1.getProfileId());

        Pair<Connection, Pair<DataSource, DataSource>> pair = mockConnection();
        PowerMockito.doThrow(new SQLException()).when(pair.first()).setAutoCommit(anyBoolean());

        String newProfileName = "Updated Test Profile";
        savedProfile.setProfileName(newProfileName);
        try {
            profileManager.updateProfile(savedProfile);
        } finally {
            PolicyManagementDAOFactory.init(pair.second().first());
        }
    }

    @Test(description = "This test case tests retrieving profile", dependsOnMethods = "testAddProfile")
    public void testGetProfile() throws Exception {
        Profile savedProfile = profileManager.getProfile(profile1.getProfileId());
        Assert.assertEquals(profile1.getProfileName(), savedProfile.getProfileName());
        Assert.assertEquals(profile1.getTenantId(), savedProfile.getTenantId());
        Assert.assertEquals(profile1.getDeviceType(), savedProfile.getDeviceType());
    }

    @Test(description = "This test case tests retrieving non existent profile", dependsOnMethods = "testGetProfile",
          expectedExceptions = ProfileManagementException.class)
    public void testGetProfileThrowingProfileManagementException() throws Exception {
        int nonExistentProfileId = 9999;
        profileManager.getProfile(nonExistentProfileId);
    }

    @Test(description = "This test case tests handling ProfileManagerDAOException when retrieving profile",
          dependsOnMethods = "testGetProfile")
    public void testGetProfileThrowingProfileManagerDAOException() throws Exception {
        ProfileDAO profileDAO = mock(ProfileDAOImpl.class);
        when(profileDAO.getProfile(anyInt())).thenThrow(new ProfileManagerDAOException());
        testThrowingException(profile1, p -> profileManager.getProfile(p.getProfileId()), "profileDAO", profileDAO,
                              ProfileManagerDAOException.class);
    }

    @Test(description = "This test case tests handling FeatureManagerDAOException when retrieving profile",
          dependsOnMethods = "testGetProfileThrowingProfileManagerDAOException")
    public void testGetProfileThrowingFeatureManagerDAOException() throws Exception {
        FeatureDAO featureDAO = mock(FeatureDAO.class);
        when(featureDAO.getFeaturesForProfile(anyInt())).thenThrow(new FeatureManagerDAOException());
        testThrowingException(profile1, p -> profileManager.getProfile(p.getProfileId()), "featureDAO", featureDAO,
                              FeatureManagerDAOException.class);
    }

    @Test(description = "This test case tests handling SQLException when retrieving profile",
          dependsOnMethods = "testGetProfileThrowingFeatureManagerDAOException",
          expectedExceptions = IllegalTransactionStateException.class)
    public void testGetProfileThrowingIllegalTransactionStateException() throws Exception {
        //Creating profile object
        Pair<Connection, Pair<DataSource, DataSource>> pair = mockConnection();
        PowerMockito.doThrow(new SQLException()).when(pair.second().second()).getConnection();

        try {
            profileManager.getProfile(profile1.getProfileId());
        } finally {
            PolicyManagementDAOFactory.init(pair.second().first());
        }
    }

    @Test(description = "This test case tests retrieving all profiles",
          dependsOnMethods = "testAddProfile")
    public void testGetAllProfiles() throws Exception {
        profileManager.getAllProfiles();
    }

    @Test(description = "This test case tests handling ProfileManagerDAOException when retrieving all profiles",
          dependsOnMethods = "testGetAllProfiles")
    public void testGetAllProfilesThrowingProfileManagerDAOException() throws Exception {
        ProfileDAO profileDAO = mock(ProfileDAOImpl.class);
        when(profileDAO.getAllProfiles()).thenThrow(new ProfileManagerDAOException());
        testThrowingException(profile1, p -> profileManager.getAllProfiles(), "profileDAO", profileDAO,
                              ProfileManagerDAOException.class);
    }

    @Test(description = "This test case tests handling FeatureManagerDAOException when retrieving all profiles",
          dependsOnMethods = "testGetAllProfilesThrowingProfileManagerDAOException")
    public void testGetAllProfilesThrowingFeatureManagerDAOException() throws Exception {
        FeatureDAO featureDAO = mock(FeatureDAO.class);
        when(featureDAO.getAllProfileFeatures()).thenThrow(new FeatureManagerDAOException());
        testThrowingException(profile1, p -> profileManager.getAllProfiles(), "featureDAO", featureDAO,
                              FeatureManagerDAOException.class);
    }

    @Test(description = "This test case tests handling SQLException when retrieving all profiles",
          dependsOnMethods = "testGetAllProfilesThrowingFeatureManagerDAOException",
          expectedExceptions = IllegalTransactionStateException.class)
    public void testGetAllProfilesThrowingIllegalTransactionStateException() throws Exception {
        //Creating profile object
        Pair<Connection, Pair<DataSource, DataSource>> pair = mockConnection();
        PowerMockito.doThrow(new SQLException()).when(pair.second().second()).getConnection();

        try {
            profileManager.getAllProfiles();
        } finally {
            PolicyManagementDAOFactory.init(pair.second().first());
        }
    }

    @Test(description = "This test case tests retrieving profiles of a device type",
          dependsOnMethods = "testAddProfile")
    public void testGetProfilesOfDeviceType() throws Exception {
        profileManager.getProfilesOfDeviceType(DEVICE_TYPE_C);
    }

    @Test(description = "This test case tests handling ProfileManagerDAOException when retrieving all profiles of a " +
            "device type",
          dependsOnMethods = "testGetProfilesOfDeviceType")
    public void testGetProfilesOfDeviceTypeThrowingProfileManagerDAOException() throws Exception {
        ProfileDAO profileDAO = mock(ProfileDAOImpl.class);
        when(profileDAO.getProfilesOfDeviceType(anyString())).thenThrow(new ProfileManagerDAOException());
        testThrowingException(profile1, p -> profileManager.getProfilesOfDeviceType(DEVICE_TYPE_C), "profileDAO",
                              profileDAO,
                              ProfileManagerDAOException.class);
    }

    @Test(description = "This test case tests handling FeatureManagerDAOException when retrieving all profiles of a " +
            "device type",
          dependsOnMethods = "testGetProfilesOfDeviceTypeThrowingProfileManagerDAOException")
    public void testGetProfilesOfDeviceTypeThrowingFeatureManagerDAOException() throws Exception {
        FeatureDAO featureDAO = mock(FeatureDAO.class);
        when(featureDAO.getAllProfileFeatures()).thenThrow(new FeatureManagerDAOException());
        testThrowingException(profile1, p -> profileManager.getProfilesOfDeviceType(DEVICE_TYPE_C), "featureDAO",
                              featureDAO,
                              FeatureManagerDAOException.class);
    }

    @Test(description = "This test case tests handling SQLException when retrieving all profiles of a device type",
          dependsOnMethods = "testGetProfilesOfDeviceTypeThrowingFeatureManagerDAOException",
          expectedExceptions = IllegalTransactionStateException.class)
    public void testGetProfilesOfDeviceTypeThrowingIllegalTransactionStateException() throws Exception {
        //Creating profile object
        Pair<Connection, Pair<DataSource, DataSource>> pair = mockConnection();
        PowerMockito.doThrow(new SQLException()).when(pair.second().second()).getConnection();

        try {
            profileManager.getProfilesOfDeviceType(DEVICE_TYPE_C);
        } finally {
            PolicyManagementDAOFactory.init(pair.second().first());
        }
    }

    @Test(description = "This test case tests handling ProfileManagerDAOException when deleting a profile",
          dependsOnMethods = "testGetProfilesOfDeviceTypeThrowingIllegalTransactionStateException")
    public void testDeleteProfileThrowingProfileManagerDAOException() throws Exception {
        ProfileDAO profileDAO = mock(ProfileDAOImpl.class);
        when(profileDAO.deleteProfile(any(Profile.class))).thenThrow(new ProfileManagerDAOException());
        testThrowingException(profile1, p -> profileManager.deleteProfile(profile1), "profileDAO", profileDAO,
                              ProfileManagerDAOException.class);
    }

    @Test(description = "This test case tests handling FeatureManagerDAOException when deleting a profile",
          dependsOnMethods = "testDeleteProfileThrowingProfileManagerDAOException")
    public void testDeleteProfileThrowingFeatureManagerDAOException() throws Exception {
        FeatureDAO featureDAO = mock(FeatureDAO.class);
        when(featureDAO.deleteFeaturesOfProfile(any(Profile.class))).thenThrow(new FeatureManagerDAOException());
        testThrowingException(profile1, p -> profileManager.deleteProfile(profile1), "featureDAO", featureDAO,
                              FeatureManagerDAOException.class);
    }

    @Test(description = "This test case tests handling SQLException when deleting a profile",
          dependsOnMethods = "testDeleteProfileThrowingFeatureManagerDAOException",
          expectedExceptions = IllegalTransactionStateException.class)
    public void testDeleteProfileThrowingIllegalTransactionStateException() throws Exception {
        //Creating profile object
        Pair<Connection, Pair<DataSource, DataSource>> pair = mockConnection();
        PowerMockito.doThrow(new SQLException()).when(pair.second().second()).getConnection();

        try {
            profileManager.deleteProfile(profile1);
        } finally {
            PolicyManagementDAOFactory.init(pair.second().first());
        }
    }

    @Test(description = "This test case tests deleting a profile",
          dependsOnMethods = "testDeleteProfileThrowingIllegalTransactionStateException",
          expectedExceptions = {ProfileManagementException.class})
    public void testDeleteProfile() throws Exception {
        profileManager.deleteProfile(profile1);
        Profile savedProfile = profileManager.getProfile(profile1.getProfileId());
    }
}