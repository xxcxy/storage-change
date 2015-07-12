package com.customtime.data.storagechange.web.servlet;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.customtime.data.storagechange.service.util.GoogAuthorizetionUtil;
import com.customtime.data.storagechange.service.util.StringUtil;
import com.customtime.data.storagechange.web.bean.UserBean;
import com.customtime.data.storagechange.web.bean.UserKey;
import com.customtime.data.storagechange.web.util.Constants;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponseException;

public class GoogAuthorizationCodeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final Lock lock = new ReentrantLock();
	
	private String getRedirectUri(HttpServletRequest request)
			throws ServletException, IOException {
		String path = request.getRequestURL().toString();
		String uri = request.getRequestURI();
		String basePath = path.substring(0,path.lastIndexOf(uri));
		String context = request.getContextPath();
		if(StringUtil.isNotBlank(context)&&!"/".equals(context))
			basePath = basePath + context;
		String skeyId = request.getParameter("skeyId");
		GenericUrl url = new GenericUrl(basePath+"/goog/oauth2callback?skeyId="+skeyId);
		
	    return url.build();
	}

	private UserKey getUserKey(HttpServletRequest req){
		UserBean ub = (UserBean)req.getSession().getAttribute(Constants.SESSION_USER);
		String skeyId = req.getParameter("skeyId");
		if(ub==null||StringUtil.isBlank(skeyId))
			return null;
		for(UserKey uk:ub.getSkey()){
			if(Long.parseLong(skeyId)==uk.getSkeyId()){
				return uk;
			}
		}
		return null;
	}
	
	protected void service(HttpServletRequest req, HttpServletResponse resp)throws IOException, ServletException {
		lock.lock();
		try {
			UserKey ukey = getUserKey(req);
			if(ukey==null)
				throw new ServletException();
			AuthorizationCodeFlow flow = GoogAuthorizetionUtil.getFlow(ukey.getKeyId(),ukey.getKeySecret());
			Credential credential = flow.loadCredential(ukey.getKeyId());
			if (credential != null && credential.getAccessToken() != null) {
				try {
					resp.setStatus(HttpServletResponse.SC_OK);
					resp.getWriter().print("认证成功!");
					return;
				} catch (HttpResponseException e) {
					if (credential.getAccessToken() != null) {
						throw e;
					}
				}
			}
			AuthorizationCodeRequestUrl authorizationUrl = flow
					.newAuthorizationUrl();
			authorizationUrl.setRedirectUri(getRedirectUri(req));
			onAuthorization(req, resp, authorizationUrl);
			credential = null;
		} finally {
			lock.unlock();
		}
	}
	
	private void onAuthorization(HttpServletRequest req,HttpServletResponse resp,AuthorizationCodeRequestUrl authorizationUrl)throws ServletException, IOException {
		resp.sendRedirect(authorizationUrl.build());
	}
}
