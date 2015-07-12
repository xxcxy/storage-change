package com.customtime.data.storagechange.web.bean;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.google.api.client.auth.oauth2.Credential;

@Entity
public class GoogCredential {
	@Id
	private String userId;
	@Basic
	private String accessToken;
	@Basic
	private String refreshToken;
	@Basic
	private Long expirationTimeMillis;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public Long getExpirationTimeMillis() {
		return expirationTimeMillis;
	}
	public void setExpirationTimeMillis(Long expirationTimeMillis) {
		this.expirationTimeMillis = expirationTimeMillis;
	}
	
	public GoogCredential(){
		super();
	}
	
	public GoogCredential(String userId, Credential credential){
		this.userId = userId;
		update(credential);
	}
	
	public void update(Credential credential){
		accessToken = credential.getAccessToken();
	    refreshToken = credential.getRefreshToken();
	    expirationTimeMillis = credential.getExpirationTimeMilliseconds();
	}
	
	public void load(Credential credential) {
		credential.setAccessToken(accessToken);
		credential.setRefreshToken(refreshToken);
		credential.setExpirationTimeMilliseconds(expirationTimeMillis);
	}
	
//	public StoredCredential toStoredCredential() {
//	    return new StoredCredential().setAccessToken(accessToken).setRefreshToken(refreshToken).setExpirationTimeMilliseconds(expirationTimeMillis);
//	}
}
