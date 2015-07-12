package com.customtime.data.storagechange.web.dataservice;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.customtime.data.storagechange.service.util.ListUtil;
import com.customtime.data.storagechange.web.bean.UserBean;
import com.customtime.data.storagechange.web.bean.UserKey;


@Service
public class UserService{
	@PersistenceContext
	private  EntityManager em;
	
	@SuppressWarnings("rawtypes")
	public UserBean getUserBean(String userName,String password){
		Query query = em.createQuery("select ub from UserBean ub where ub.userName = :userName and ub.password= :password");
		query.setParameter("userName",userName);
		query.setParameter("password", password);
		List lo = query.getResultList();
		if(!ListUtil.isEmpty(lo))
			return (UserBean)lo.get(0);
		return null;
	}
	public UserBean getUserBean(Long userId){
		return em.find(UserBean.class,userId);
	}
	public boolean isExistUser(String userName){
		Query query = em.createQuery("select ub from UserBean ub where ub.userName = :userName");
		query.setParameter("userName",userName);
		return !ListUtil.isEmpty(query.getResultList());
	}
	@Transactional
	public boolean addUser(UserBean userBean){
		em.persist(userBean);
		return true;
	}
	public UserKey getUserKey(Long keyId){
		return em.find(UserKey.class,keyId);
	}
	@Transactional
	public void addSKey(Long userId,UserKey skey){
		UserBean ub = em.find(UserBean.class,userId);
		if(ub!=null){
			skey.setUserBean(ub);
			em.persist(skey);
		}
	}
	@Transactional
	public void deleteSKey(Long skeyId){
		UserKey skey = em.find(UserKey.class,skeyId);
		if(skey!=null){
			em.remove(skey);
		}
	}
	@Transactional
	public void updateSKey(UserKey skey){
		em.merge(skey);
	}
	@Transactional
	public void updateUser(UserBean ub){
		em.merge(ub);
	}
	
	@SuppressWarnings("rawtypes")
	public List getAllUser(){
		Query query = em.createQuery("select ub from UserBean ub");
		return query.getResultList();
	}
}
