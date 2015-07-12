package com.customtime.data.storagechange.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
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
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;

public class GoogAuthorizationCodeCallbackServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Lock lock = new ReentrantLock();
	private AuthorizationCodeFlow flow;
	
	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		StringBuffer buf = req.getRequestURL();
		if (req.getQueryString() != null) {
			buf.append('?').append(req.getQueryString());
		}
		GoogAuthorizationCodeResponseUrl responseUrl = new GoogAuthorizationCodeResponseUrl(buf.toString());
		String code = responseUrl.getCode();
		if (responseUrl.getError() != null) {
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().print("error authorization code");
		} else if (code == null) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().print("Missing authorization code");
		} else {
			lock.lock();
			try {
				UserKey ukey = getUserKey(req);
				if (flow == null) {
					flow = GoogAuthorizetionUtil.getFlow(ukey.getKeyId(),ukey.getKeySecret());
				}
				String redirectUri = getRedirectUri(req);
				TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
				flow.createAndStoreCredential(response,ukey.getKeyId());
				resp.setStatus(200);
				resp.setContentType("text/html");
	            PrintWriter doc = resp.getWriter();
	            doc.println("<html>");
	            doc.println("<head><title>OAuth 2.0 Authentication Token Recieved</title></head>");
	            doc.println("<body>");
	            doc.println("Received verification code. Closing...");
	            doc.println("<script type='text/javascript'>");
	            doc.println("window.setTimeout(function() {");
	            doc.println("    window.open('', '_self', ''); window.close(); }, 1000);");
	            doc.println("if (window.opener) { window.opener.checkToken(); }");
	            doc.println("</script>");
	            doc.println("</body>");
	            doc.println("</HTML>");
	            doc.flush();
			} finally {
				lock.unlock();
			}
		}
	}
	
	private String getRedirectUri(HttpServletRequest request)throws ServletException, IOException {
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
}
