package com.customtime.data.storagechange.web.view;

import java.util.Map;

import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.velocity.VelocityViewResolver;

public class CTViewResolver extends VelocityViewResolver{
	private String dateToolAttribute;
    private String numberToolAttribute;
	private Map<String,?> staticAttributes;

	@Override
	protected AbstractUrlBasedView buildView(String viewName) throws Exception {
		CTView view = (CTView)super.buildView(viewName);
        view.setDateToolAttribute(dateToolAttribute);
        view.setNumberToolAttribute(numberToolAttribute);
        view.setOptions(staticAttributes);
        return view;
	}

	@Override
	protected Class<?> requiredViewClass() {
		return CTView.class;
	}
	
	public void setDateToolAttribute(String dateToolAttribute) {
		this.dateToolAttribute = dateToolAttribute;
	}
	
	public void setNumberToolAttribute(String numberToolAttribute) {
		this.numberToolAttribute = numberToolAttribute;
	}
	public Map<String,?> getStaticAttributes() {
		return staticAttributes;
	}

	public void setStaticAttributes(Map<String, ?> staticAttributes) {
		this.staticAttributes = staticAttributes;
	}
	
}
