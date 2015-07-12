package com.customtime.data.storagechange.web.util;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.customtime.data.storagechange.web.bean.GoogCredential;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;

@Component
public class GoogJPACredentialStore implements CredentialStore {
	@PersistenceContext
	private EntityManager em;
	private final Lock lock = new ReentrantLock();
	
	@Transactional
	public void delete(String userId, Credential credential) throws IOException {
		lock.lock();
	    try {
	    	GoogCredential googCredential = em.find(GoogCredential.class, userId);
	    	em.remove(googCredential);
	    } finally {
	      lock.unlock();
	    }
	}

	public boolean load(String userId, Credential credential) throws IOException {
		lock.lock();
	    try {
	    	GoogCredential googCredential = em.find(GoogCredential.class, userId);
	    	if(googCredential!=null){
	    		googCredential.load(credential);
		    	return true;
	    	}else
	    		return false;
	    } finally {
	      lock.unlock();
	    }
	}

	@Transactional
	public void store(String userId, Credential credential) throws IOException {
		lock.lock();
		try {
			GoogCredential googCredential = em.find(GoogCredential.class, userId);
			if(googCredential!=null)
				googCredential.update(credential);
			else
				googCredential = new GoogCredential(userId, credential);
			em.persist(googCredential);
		} finally {
			lock.unlock();
	    }
	}
	
}
