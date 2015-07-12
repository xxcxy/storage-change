package com.customtime.data.storagechange.web.controller;

import java.io.IOException;

import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.customtime.data.storagechange.service.util.StringUtil;
import com.customtime.data.storagechange.web.bean.UserBean;
import com.customtime.data.storagechange.web.bean.UserKey;
import com.customtime.data.storagechange.web.dataservice.UserService;
import com.customtime.data.storagechange.web.util.Constants;

@Controller
public class MainController extends BaseController{
	public static final Log logger = LogFactory.getLog(MainController.class);

	@Autowired
	private UserService userService;
	
	@RequestMapping("/login")
	public void login(String userName,String password,String remember,String verify,HttpServletResponse res,HttpSession session){
		if(!verifyValid(verify,session)){
			String result = "{\"code\":1,\"msg\":\"验证码错误!\"}";
			responseJson(res,result);
		}else{
			UserBean user = userService.getUserBean(userName,StringUtil.MD5Encode(password));
			if(user!=null){
				if("remember".equals(remember)){
					logger.info(StringUtil.encryptStr(userName+"=="+StringUtil.MD5Encode(password)));
					Cookie cookie = new Cookie("cstid", StringUtil.encryptStr(userName+"=="+StringUtil.MD5Encode(password)));  
		        	cookie.setMaxAge(30*24*60*60);  
//		        	cookie.setHttpOnly(true);
		        	logger.info(cookie.getValue());
		        	res.addCookie(cookie); 
				}
				session.setAttribute(Constants.SESSION_USER,user);
				responseJson(res,"{\"code\":0}");
			}else{
				responseJson(res,"{\"code\":2,\"msg\":\"用户名或者密码错误\"}");
			}
		}
	}
	@RequestMapping("/regist")
	public String toRegist(String nextPath,ModelMap model){
		if(StringUtil.isNotBlank(nextPath))
			model.put("nextPath",nextPath);
		return "userInfo";
	}
	@RequestMapping("/userInfo")
	public String toUserInfo(ModelMap model,HttpSession session){
		Object obj = session.getAttribute(Constants.SESSION_USER);
		if(obj!=null&&obj instanceof UserBean){
			UserBean ub = userService.getUserBean(((UserBean)obj).getUserId());
			session.setAttribute(Constants.SESSION_USER,ub);
			model.put("user",ub);
			return "userInfo";
		}else
			return "login";
	}
	@RequestMapping("/saveUserInfo")
	public void saveUserInfo(String userId,String userName,String email,String phone,String company,String address,String webUrl,String verify,String password,HttpSession session,ServletResponse res){
		if(StringUtil.isNotBlank(userId)){
			UserBean user = userService.getUserBean(Long.parseLong(userId));
			if(user!=null){
				if(StringUtil.isNotBlank(email))
					user.setEmail(email);
				if(StringUtil.isNotBlank(phone))
					user.setPhone(phone);
				if(StringUtil.isNotBlank(company))
					user.setCompany(company);
				if(StringUtil.isNotBlank(address))
					user.setAddress(address);
				if(StringUtil.isNotBlank(webUrl))
					user.setWebUrl(webUrl);
				userService.updateUser(user);
				session.setAttribute(Constants.SESSION_USER,user);
				responseJson(res,"{\"code\":2}");
			}else{
				responseJson(res,"{\"code\":1,\"msg\":\"用户不存在\"}");
			}
		}else{
			if(StringUtil.isBlank(userName)||StringUtil.isBlank(password)||StringUtil.isBlank(email)){
				responseJson(res,"{\"code\":1,\"msg\":\"用户名，密码，邮箱都不能为空\"}");
			}else if(!verifyValid(verify,session)){
				responseJson(res,"{\"code\":1,\"msg\":\"验证码错误\"}");
			}else if(userService.isExistUser(userName)){
				responseJson(res,"{\"code\":1,\"msg\":\"用户名已存在\"}");
			}else{
				UserBean userBean = new UserBean();
				userBean.setAddress(address);
				userBean.setCompany(company);
				userBean.setEmail(email);
				userBean.setPassword(StringUtil.MD5Encode(password));
				userBean.setPhone(phone);
				userBean.setUserName(userName);
				userBean.setWebUrl(webUrl);
				userService.addUser(userBean);
				session.setAttribute(Constants.SESSION_USER, userBean);
				responseJson(res,"{\"code\":0,\"msg\":\"注册成功\"}");
			}
				
		}
	}
	@RequestMapping("/changePassword")
	public void changePassword(String oldPassword,String newPassword,String verify,ServletResponse res,HttpSession session){
		Long userId = ((UserBean)session.getAttribute(Constants.SESSION_USER)).getUserId();
		if(StringUtil.isBlank(oldPassword)||StringUtil.isBlank(newPassword)){
			responseJson(res,"{\"code\":1,\"msg\":\"修改失败，参数不完整\"}");
		}else if(!verifyValid(verify,session)){
			responseJson(res,"{\"code\":1,\"msg\":\"验证码错误\"}");
		}else{
			UserBean user = userService.getUserBean(userId);
			if(user!=null&&user.getPassword().equals(StringUtil.MD5Encode(oldPassword))){
				user.setPassword(StringUtil.MD5Encode(newPassword));
				userService.updateUser(user);
				responseJson(res,"{\"code\":0,\"msg\":\"密码修改成功\"}");
			}else{
				responseJson(res,"{\"code\":1,\"msg\":\"密码错误\"}");
			}
		}
	}
	@RequestMapping("/editKey")
	public void editKey(String op,String sKeyId,String keyName,String keySer,String keyType,String keyId,String keyDescription,String project,ServletResponse res,HttpSession session){
		Object obj = session.getAttribute(Constants.SESSION_USER);
		if(obj==null||!(obj instanceof UserBean)){
			responseJson(res,"{\"code\":5,\"msg\":\"未登录\"}");
		}else{
			UserBean user = userService.getUserBean(((UserBean)obj).getUserId());
			if("add".equals(op)){
				for(UserKey uk:user.getSkey()){
					if(uk.getKeyId().equals(keyId)&&uk.getKeyType().equals(keyType)){
						responseJson(res,"{\"code\":1,\"msg\":\"键已存在\"}");
						return;
					}
				}
				UserKey skey = new UserKey();
				skey.setKeyId(keyId);
				skey.setKeyName(keyName);
				skey.setKeySecret(keySer);
				skey.setKeyType(keyType);
				skey.setKeyDescription(keyDescription);
				skey.setProject(project);
				userService.addSKey(user.getUserId(), skey);
				responseJson(res,"{\"code\":0,\"msg\":\"新增成功\"}");
			}else if("update".equals(op)){
				if(StringUtil.isBlank(sKeyId)){
					responseJson(res,"{\"code\":1,\"msg\":\"键不存在\"}");
				}else{
					UserKey uk = userService.getUserKey(Long.parseLong(sKeyId));
					if(uk==null){
						responseJson(res,"{\"code\":1,\"msg\":\"键不存在\"}");
					}else{
						uk.setKeyName(keyName);
						if(StringUtil.isNotBlank(keyDescription))
							uk.setKeyDescription(keyDescription);
						userService.updateSKey(uk);
						responseJson(res,"{\"code\":0,\"msg\":\"修改成功\"}");
					}
				}
			}else if("del".equals(op)){
				if(StringUtil.isBlank(sKeyId)){
					responseJson(res,"{\"code\":1,\"msg\":\"键不存在\"}");
				}else{
					userService.deleteSKey(Long.parseLong(sKeyId));
					responseJson(res,"{\"code\":0,\"msg\":\"删除成功\"}");
				}
			}
			session.setAttribute(Constants.SESSION_USER, userService.getUserBean(user.getUserId()));
		}
	}
	@RequestMapping("/toLogin")
	public String toLogin(){
		return "login";
	}
	@RequestMapping("/logout")
	public String logout(HttpServletResponse res,HttpSession session){
		Cookie cookie = new Cookie("cstid","x==p");  
    	cookie.setMaxAge(0);  
//    	cookie.setHttpOnly(true);
    	res.addCookie(cookie); 
    	session.removeAttribute(Constants.SESSION_USER);
		return "login";
	}
	@RequestMapping("/ukeyInfo")
	public String toEditKey(ModelMap model,HttpSession session){
		UserBean user = (UserBean)session.getAttribute(Constants.SESSION_USER);
		model.put("userKeys",userService.getUserBean(user.getUserId()).getSkey());
		model.put("user", user);
		return "userKeyInfo";
		
	}
	@RequestMapping("/filemanager")
	public String toFileManager(ModelMap model,HttpSession session){
		UserBean user = (UserBean)session.getAttribute(Constants.SESSION_USER);
		model.put("userKeys",userService.getUserBean(user.getUserId()).getSkey());
		model.put("user", user);
		return "fileManager";
	}
	
	private void responseJson(ServletResponse res,String context){
		res.setCharacterEncoding("UTF-8");
		res.setContentType("application/x-json");
		try {
			res.getWriter().print(context);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private boolean verifyValid(String verify,HttpSession session){
		if(StringUtil.isBlank(verify))
			return false;
		Object obj = session.getAttribute(Constants.VERIFYCODE);
		if(obj==null)
			return false;
		else
			return verify.equalsIgnoreCase(obj.toString());
	}
}
