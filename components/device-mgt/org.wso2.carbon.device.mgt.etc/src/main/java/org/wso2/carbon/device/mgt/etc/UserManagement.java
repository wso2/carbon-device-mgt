package org.wso2.carbon.device.mgt.etc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.etc.util.Role;
import org.wso2.carbon.device.mgt.etc.util.User;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManagement {
	private static Log log = LogFactory.getLog(UserManagement.class);

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
	public static final String CHALLENGE_QUESTION_URI
			= UserCoreConstants.ClaimTypeURIs.CHALLENGE_QUESTION_URI;
	public static final String IDENTITY_CLAIM_URI
			= UserCoreConstants.ClaimTypeURIs.IDENTITY_CLAIM_URI;
	public static final String TEMPORARY_EMAIL_ADDRESS
			= UserCoreConstants.ClaimTypeURIs.TEMPORARY_EMAIL_ADDRESS;

	private static final String DEVICE_API_ACCESS_ROLE_NAME="deviceRole";
	private static final String DEVICE_USER_API_ACCESS_ROLE_NAME="deviceUser";
	private static RealmService realmService;

	public static RealmService getRealmService() {
		return realmService;
	}

	public static void setRealmService(RealmService realmService) {
		UserManagement.realmService = realmService;
	}

	public int getUserCount() {

		try {
			String[] users = getUserStoreManager().listUsers("", -1);
			if (users == null) {
				return 0;
			}
			return users.length;
		} catch (UserStoreException e) {
			String msg
					=
					"Error occurred while retrieving the list of users that exist within the " +
							"current tenant";
			log.error(msg, e);
			return 0;
		}
	}



	//===========================================================================
	//TODO: Below methods are implemented to support jaggery code upon removal of org.wso2.carbon
	// .device.mgt.user.core from CDMF
	//===========================================================================

	private static UserStoreManager getUserStoreManager() throws UserStoreException {

		UserStoreManager userStoreManager;
		try {

			if (realmService == null) {
				String msg = "Realm service not initialized";
				log.error(msg);
				throw new UserStoreException(msg);
			}
			int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
			userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
		} catch (UserStoreException e) {
			String msg = "Error occurred while retrieving current user store manager";
			log.error(msg, e);
			throw new UserStoreException(msg, e);
		} finally {
			//PrivilegedCarbonContext.endTenantFlow();
		}
		return userStoreManager;


	}

	public List<User> getUsersForTenantAndRole(int tenantId, String roleName)
			throws UserStoreException {

		UserStoreManager userStoreManager = getUserStoreManager();
		String[] userNames;
		ArrayList usersList = new ArrayList();

		userNames = userStoreManager.getUserListOfRole(roleName);
		User newUser;
		for (String userName : userNames) {
			newUser = new User(userName);
			Claim[] claims = userStoreManager.getUserClaimValues(userName, null);
			Map<String, String> claimMap = new HashMap<String, String>();
			for (Claim claim : claims) {
				String claimURI = claim.getClaimUri();
				String value = claim.getValue();
				claimMap.put(claimURI, value);
			}
			setUserClaims(newUser, claimMap);
			usersList.add(newUser);
		}

		return usersList;
	}

	public List<Role> getRolesForTenant(int tenantId) throws UserStoreException {

		String[] roleNames;
		ArrayList<Role> rolesList = new ArrayList<Role>();
		Role newRole;
		UserStoreManager userStoreManager = getUserStoreManager();

		roleNames = userStoreManager.getRoleNames();
		for (String roleName : roleNames) {
			newRole = new Role(roleName);
			rolesList.add(newRole);
		}

		return rolesList;
	}

	public List<User> getUsersForTenant(int tenantId) throws UserStoreException {

		UserStoreManager userStoreManager;
		String[] userNames;
		ArrayList usersList = new ArrayList();

		userStoreManager = getUserStoreManager();

		userNames = userStoreManager.listUsers("", -1);
		User newUser;
		for (String userName : userNames) {
			newUser = new User(userName);
			Claim[] claims = userStoreManager.getUserClaimValues(userName, null);
			Map<String, String> claimMap = new HashMap<String, String>();
			for (Claim claim : claims) {
				String claimURI = claim.getClaimUri();
				String value = claim.getValue();
				claimMap.put(claimURI, value);
			}
			setUserClaims(newUser, claimMap);
			usersList.add(newUser);
		}

		return usersList;
	}

	public User getUser(String username, int tenantId) throws UserStoreException {
		UserStoreManager userStoreManager;
		User user;
		userStoreManager = getUserStoreManager();
		user = new User(username);

		Claim[] claims = userStoreManager.getUserClaimValues(username, null);
		Map<String, String> claimMap = new HashMap<String, String>();
		for (Claim claim : claims) {
			String claimURI = claim.getClaimUri();
			String value = claim.getValue();
			claimMap.put(claimURI, value);
		}

		setUserClaims(user, claimMap);

		return user;
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

	public static void registerApiAccessRoles() {
		UserStoreManager userStoreManager = null;
		try {
			userStoreManager = getUserStoreManager();
			String[] userList = new String[]{"admin"};
			Permission permissions[] = new Permission[]{};
			if (!userStoreManager.isExistingRole(DEVICE_API_ACCESS_ROLE_NAME)) {
				userStoreManager.addRole(DEVICE_API_ACCESS_ROLE_NAME, userList, permissions);
			}
			if (!userStoreManager.isExistingRole(DEVICE_USER_API_ACCESS_ROLE_NAME)) {
				userStoreManager.addRole(DEVICE_USER_API_ACCESS_ROLE_NAME, userList, permissions);
			}
		} catch (UserStoreException e) {
			log.error("error on wso2 user component");
		}


	}

}
