package com.customtime.data.storagechange.web.controller;

import javax.annotation.Resource;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.customtime.data.storagechange.service.util.StringUtil;
import com.customtime.data.storagechange.web.bean.UserBean;
import com.customtime.data.storagechange.web.bean.UserKey;
import com.customtime.data.storagechange.web.dataservice.UserService;
import com.customtime.data.storagechange.web.util.Constants;
import com.customtime.data.storagechange.web.vo.ResponseVo;

@Controller
public class MainController extends BaseController{
	public static final Log logger = LogFactory.getLog(MainController.class);

	@Resource
	private UserService userService;
	
	@RequestMapping("/login")
	public @ResponseBody ResponseVo login(String userName,String password,String remember,String verify,HttpServletResponse res,HttpSession session){
		if(!verifyValid(verify,session)){
			return new ResponseVo("1","验证码错误!");
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
				return new ResponseVo("0","");
			}else{
				return new ResponseVo("2","用户名或者密码错误");
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
	public @ResponseBody ResponseVo saveUserInfo(String userId,String userName,String email,String phone,String company,String address,String webUrl,String verify,String password,HttpSession session,ServletResponse res){
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
				return new ResponseVo("2","");
			}else{
				return new ResponseVo("1","用户不存在");
			}
		}else{
			if(StringUtil.isBlank(userName)||StringUtil.isBlank(password)||StringUtil.isBlank(email)){
				return new ResponseVo("1","用户名，密码，邮箱都不能为空");
			}else if(!verifyValid(verify,session)){
				return new ResponseVo("1","验证码错误");
			}else if(userService.isExistUser(userName)){
				return new ResponseVo("1","用户名已存在");
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
				return new ResponseVo("0","注册成功");
			}
				
		}
	}
	@RequestMapping("/changePassword")
	public @ResponseBody ResponseVo changePassword(String oldPassword,String newPassword,String verify,ServletResponse res,HttpSession session){
		Long userId = ((UserBean)session.getAttribute(Constants.SESSION_USER)).getUserId();
		if(StringUtil.isBlank(oldPassword)||StringUtil.isBlank(newPassword)){
			return new ResponseVo("1","修改失败，参数不完整");
		}else if(!verifyValid(verify,session)){
			return new ResponseVo("1","验证码错误");
		}else{
			UserBean user = userService.getUserBean(userId);
			if(user!=null&&user.getPassword().equals(StringUtil.MD5Encode(oldPassword))){
				user.setPassword(StringUtil.MD5Encode(newPassword));
				userService.updateUser(user);
				return new ResponseVo("0","密码修改成功");
			}else{
				return new ResponseVo("1","密码错误");
			}
		}
	}
	@RequestMapping("/editKey")
	public @ResponseBody ResponseVo editKey(String op,String sKeyId,String keyName,String keySer,String keyType,String keyId,String keyDescription,String project,ServletResponse res,HttpSession session){
		Object obj = session.getAttribute(Constants.SESSION_USER);
		ResponseVo rvo = null;
		if(obj==null||!(obj instanceof UserBean)){
			rvo = new ResponseVo("5","未登录");
		}else{
			UserBean user = userService.getUserBean(((UserBean)obj).getUserId());
			if("add".equals(op)){
				for(UserKey uk:user.getSkey()){
					if(uk.getKeyId().equals(keyId)&&uk.getKeyType().equals(keyType)){
						rvo = new ResponseVo("1","键已存在");
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
				rvo = new ResponseVo("0","新增成功");
			}else if("update".equals(op)){
				if(StringUtil.isBlank(sKeyId)){
					rvo = new ResponseVo("1","键不存在");
				}else{
					UserKey uk = userService.getUserKey(Long.parseLong(sKeyId));
					if(uk==null){
						rvo = new ResponseVo("1","键不存在");
					}else{
						uk.setKeyName(keyName);
						if(StringUtil.isNotBlank(keyDescription))
							uk.setKeyDescription(keyDescription);
						userService.updateSKey(uk);
						rvo = new ResponseVo("0","修改成功");
					}
				}
			}else if("del".equals(op)){
				if(StringUtil.isBlank(sKeyId)){
					rvo = new ResponseVo("1","键不存在");
				}else{
					userService.deleteSKey(Long.parseLong(sKeyId));
					rvo = new ResponseVo("0","删除成功");
				}
			}
			session.setAttribute(Constants.SESSION_USER, userService.getUserBean(user.getUserId()));
		}
		return rvo;
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
