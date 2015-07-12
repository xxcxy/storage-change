package com.customtime.data.storagechange.service.util;

import java.util.Collections;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.auth.oauth2.MemoryCredentialStore;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.storage.StorageScopes;

@Component
public class GoogAuthorizetionUtil implements ApplicationContextAware{
	
	public void setApplicationContext(ApplicationContext appcontext)
			throws BeansException {
		applicationcontext = appcontext;
	}
	public static final JsonFactory jsonFactory = new GsonFactory();
	private static CredentialStore credentialStore = new MemoryCredentialStore();
	private static ApplicationContext applicationcontext;
	public static AuthorizationCodeFlow getFlow(String keyId,String keySecret){
		if(applicationcontext!=null){
			credentialStore = (CredentialStore)applicationcontext.getBean("googJPACredentialStore");
		}
		return new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(),
				jsonFactory ,keyId,keySecret,Collections.singleton(StorageScopes.DEVSTORAGE_FULL_CONTROL))
				.setCredentialStore(credentialStore).setAccessType("offline").build();
	}
}
