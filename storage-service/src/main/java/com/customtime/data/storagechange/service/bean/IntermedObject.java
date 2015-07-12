package com.customtime.data.storagechange.service.bean;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class IntermedObject {
	private InputStream context;
	private Map<String,String> metadata;
	private String objectName;
	private String MD5String;
	public InputStream getContext() {
		return context;
	}
	public void setContext(InputStream context) {
		this.context = context;
	}
	public Map<String, String> getMetadata() {
		return metadata;
	}
	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	public void setMetadata(String key,String value){
		if(metadata==null)
			metadata = new HashMap<String,String>();
		metadata.put(key, value);
	}
	public String getMD5String() {
		return MD5String;
	}
	public void setMD5String(String mD5String) {
		MD5String = mD5String;
	}
	
}
