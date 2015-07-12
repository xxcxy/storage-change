package com.customtime.data.storagechange.web.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.springframework.web.servlet.view.velocity.VelocityView;

import com.customtime.data.storagechange.service.util.StringUtil;


public class CTView extends VelocityView {

	@SuppressWarnings("rawtypes")
	private Map options;
	@Override
	protected Context createVelocityContext(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		VelocityContext vc = new VelocityContext(model);
		String path = request.getRequestURL().toString();
		String uri = request.getRequestURI();
		String basePath = path.substring(0,path.lastIndexOf(uri));
		String context = request.getContextPath();
		if(StringUtil.isNotBlank(context)&&!"/".equals(context))
			basePath = basePath + context;
		vc.put("baseUrl", basePath);
		vc.put("vmUtil",new VmUtil());
		vc.put("options", options);
		return vc;
	}
	@SuppressWarnings("rawtypes")
	public void setOptions(Map options){
		this.options=options;
	}
}
