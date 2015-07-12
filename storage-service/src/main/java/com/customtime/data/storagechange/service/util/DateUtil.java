package com.customtime.data.storagechange.service.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
	public static final TimeZone zone = TimeZone.getTimeZone("GMT+8");
	public static Date getTodayLastTime(){
		Calendar cal = Calendar.getInstance(zone);
		cal.set(Calendar.HOUR_OF_DAY,23);
		cal.set(Calendar.MINUTE,59);
		cal.set(Calendar.SECOND,59);
		return cal.getTime();
	}
	
	public static Date getTodayFirstTime(){
		Calendar cal = Calendar.getInstance(zone);
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		return cal.getTime();
	}
	
	public static Date getNow(){
		Calendar cal = Calendar.getInstance(zone);
		return cal.getTime();
	}
	public static Date getYesterdayTime(){
		Calendar cal = Calendar.getInstance(zone);
		cal.add(Calendar.DATE, -1);
		return cal.getTime();
	}
}
