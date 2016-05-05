package org.wso2.carbon.identity.jwt.client.extension.dto;

import org.wso2.carbon.core.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JWTConfig {

	private static final String JWT_ISSUER = "iss";
	private static final String JWT_EXPIRATION_TIME = "exp";
	private static final String JWT_AUDIENCE = "aud";
	private static final String VALIDITY_PERIOD = "nbf";
	private static final String JWT_TOKEN_ID = "jti";
	private static final String JWT_ISSUED_AT = "iat";
	private static final String SERVER_TIME_SKEW="skew";
	private static final String JKS_PATH ="KeyStore";
	private static final String JKS_PRIVATE_KEY_ALIAS ="PrivateKeyAlias";
	private static final String JKS_PASSWORD ="KeyStorePassword";
	private static final String JKA_PRIVATE_KEY_PASSWORD = "PrivateKeyPassword";
	private static final String TOKEN_ENDPOINT = "TokenEndpoint";

	/**
	 * issuer of the JWT
	 */
	private String issuer;

	/**
	 * skew between IDP and issuer(milliseconds)
	 */
	private int skew;

	/**
	 * Audience of JWT claim
	 */
	private List<String> audiences;

	/**
	 * expiration time of JWT (number of minutes from the current time).
	 */
	private int expirationTime;

	/**
	 * issued Interval from current time of JWT (number of minutes from the current time).
	 */
	private int issuedInternal;

	/**
	 * nbf time of JWT (number of minutes from current time).
	 */
	private int validityPeriodInterval;

	/**
	 * JWT Id.
	 */
	private String jti;

	/**
	 * Token Endpoint;
	 */
	private String tokenEndpoint;

	/**
	 * Configuration for keystore.
	 */
	private String keyStorePath;
	private String keyStorePassword;
	private String privateKeyAlias;
	private String privateKeyPassword;

	/**
	 * @param properties load the config from the properties file.
	 */
	public JWTConfig(Properties properties) {
		issuer = properties.getProperty(JWT_ISSUER, null);
		skew = Integer.parseInt(properties.getProperty(SERVER_TIME_SKEW, "0"));
		issuedInternal = Integer.parseInt(properties.getProperty(JWT_ISSUED_AT,"0"));
		expirationTime = Integer.parseInt(properties.getProperty(JWT_EXPIRATION_TIME,"15"));
		validityPeriodInterval = Integer.parseInt(properties.getProperty(VALIDITY_PERIOD,"0"));
		jti = properties.getProperty(JWT_TOKEN_ID, null);
		String audience = properties.getProperty(JWT_AUDIENCE, null);
		if(audience != null) {
			audiences = getAudience(audience);
		}
		//get Keystore params
		keyStorePath = properties.getProperty(JKS_PATH);
		keyStorePassword = properties.getProperty(JKS_PASSWORD);
		privateKeyAlias = properties.getProperty(JKS_PRIVATE_KEY_ALIAS);
		privateKeyPassword = properties.getProperty(JKA_PRIVATE_KEY_PASSWORD);
		tokenEndpoint = properties.getProperty(TOKEN_ENDPOINT, "");
	}

	private static List<String> getAudience(String audience){
		List<String> audiences = new ArrayList<String>();
		for(String audi : audience.split(",")){
			audiences.add(audi.trim());
		}
		return audiences;
	}

	public String getIssuer() {
		return issuer;
	}

	public int getSkew() {
		return skew;
	}

	public List<String> getAudiences() {
		return audiences;
	}

	public int getExpirationTime() {
		return expirationTime;
	}

	public int getIssuedInternal() {
		return issuedInternal;
	}

	public int getValidityPeriodFromCurrentTime() {
		return validityPeriodInterval;
	}

	public String getJti() {
		return jti;
	}

	public String getKeyStorePath() {
		return keyStorePath;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public String getPrivateKeyAlias() {
		return privateKeyAlias;
	}

	public String getPrivateKeyPassword() {
		return privateKeyPassword;
	}

	public String getTokenEndpoint() {
		return Utils.replaceSystemProperty(tokenEndpoint);
	}
}
