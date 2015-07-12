package com.customtime.data.storagechange.web.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.customtime.data.storagechange.service.util.DateUtil;

public class VmUtil {
	private SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public VmUtil(){
		sf.setTimeZone(DateUtil.zone);
	}
	public String format(Date date){
		if(date==null)
			return "";
		else
			return sf.format(date);
	}
	public int getStringLeng(String str){
		return str.length();
	}
	public String getSubString(String str,int len){
		return str.substring(0,len);
	}
	
}
