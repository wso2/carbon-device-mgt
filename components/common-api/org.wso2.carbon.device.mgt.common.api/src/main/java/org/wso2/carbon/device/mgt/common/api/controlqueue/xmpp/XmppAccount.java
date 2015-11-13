package org.wso2.carbon.device.mgt.common.api.controlqueue.xmpp;

import java.util.Map;

/**
 * Created by smean-MAC on 7/24/15.
 */
public class XmppAccount {
	private String username;
	private String password;
	private String accountName;
	private String email;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
