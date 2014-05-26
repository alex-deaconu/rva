package com.risevision.ui.client.common.info;

import java.io.Serializable;

@SuppressWarnings("serial")
public class OAuthInfo implements Serializable {
	private String consumerKey;
	private String consumerSecret;

	public String getConsumerKey() {
		return consumerKey;
	}
	
	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

}
