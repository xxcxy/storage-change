package com.customtime.data.storagechange.web.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.customtime.data.storagechange.service.util.StringUtil;
import com.customtime.data.storagechange.web.bean.UserBean;
import com.customtime.data.storagechange.web.dataservice.UserService;
import com.customtime.data.storagechange.web.util.Constants;

public class LoginInterceptor implements HandlerInterceptor {
	@Autowired
	private UserService userService;
	
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handle, Exception exception)
			throws Exception {
		// TODO Auto-generated method stub
	}
	
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handle, ModelAndView modelView)
			throws Exception {
		// TODO Auto-generated method stub
	}
	
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handle) throws Exception {
		HttpSession session = request.getSession(true);
		Object obj = session.getAttribute(Constants.SESSION_USER);
		if (obj == null || !(obj instanceof UserBean)) {
			Cookie[] cookies = request.getCookies(); 
	        String[] cooks = null;  
	        String userName = null;  
	        String password = null;  
	        if (cookies != null) { 
	            for (Cookie coo : cookies) { 
	            	if("cstid".equals(coo.getName())){
						try {
							String aa = StringUtil.decryptStr(coo.getValue());
							cooks = aa.split("==");
							if (cooks.length == 2) {
								userName = cooks[0];
								password = cooks[1];
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
	 	                break;
	            	}
	            }
	            if(StringUtil.isNotBlank(userName)&&StringUtil.isNotBlank(password)) {  
		            UserBean user =  userService.getUserBean(userName, password);
		            if(user!=null){
		            	session.setAttribute(Constants.SESSION_USER,user);
		            	return true;
		            }
		        }
	        }  
        	String destUrl = request.getRequestURI();
        	if(StringUtil.isNotBlank(request.getQueryString())){
        		destUrl = destUrl+"?"+request.getQueryString();
        	}
			request.setAttribute("nextPath", destUrl);
			request.getRequestDispatcher("/toLogin.giy").forward(request,
					response);
			return false; 
		}
		return true;
	}
}
