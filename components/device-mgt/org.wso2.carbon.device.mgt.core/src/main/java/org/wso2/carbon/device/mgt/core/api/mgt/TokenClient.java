package org.wso2.carbon.device.mgt.core.api.mgt;

import org.wso2.carbon.device.mgt.common.api.mgt.AccessTokenException;
import org.wso2.carbon.device.mgt.common.api.mgt.AccessTokenInfo;

public interface TokenClient {

	/**
	 * This return an access token against the device type api application
	 * @param apiApplicationName - apiApplication to which token is generated, this will create an application if it does not exist.
	 * @param deviceType                    - access token to access specific device type api
	 * @param username                      - username of whom token is created.
	 * @param deviceId                      - deviceId can be empty/not if the token is generated for device
	 * @param scopes                        - scopes that the token related to the token.
	 * @param applicationSubscriberUsername - to whom the api application should be created.
	 * @return
	 * @throws AccessTokenException
	 */
	AccessTokenInfo getAccessToken(String apiApplicationName, String deviceType, String username, String deviceId,
								   String scopes, String applicationSubscriberUsername)
			throws AccessTokenException;

	/**
	 * This return an access token against the device type api application with the existing referesh token
	 * @param apiApplicationName - apiApplication to which token is generated, this will create an application if it does not exist.
	 * @param deviceType                    to find the api application
	 * @param refreshToken                  to generate a new token.
	 * @param username                      to whom the token is genereate
	 * @param scopes                        - scopes that the token related to the token.
	 * @param applicationSubscriberUsername - to whom the api application should be created.
	 * @return
	 * @throws AccessTokenException
	 */
	AccessTokenInfo getAccessTokenFromRefreshToken(String apiApplicationName, String deviceType, String refreshToken,
												   String username, String scopes, String applicationSubscriberUsername)
			throws AccessTokenException;
}
