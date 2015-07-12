package com.customtime.data.storagechange.service.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map.Entry;

import com.aliyun.openservices.oss.model.OSSObject;
import com.baidu.inf.iis.bcs.model.DownloadObject;
import com.baidu.inf.iis.bcs.model.ObjectMetadata;
import com.customtime.data.storagechange.service.bean.IntermedObject;
import com.google.common.io.Closeables;
import com.snda.storage.SNDAObject;
import com.snda.storage.SNDAObjectMetadata;

public class IntermedObjectUtil {
	public static final SimpleDateFormat dsf = new SimpleDateFormat("yyyyMMddhhmmss");
	private static final SimpleDateFormat dateStringFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",Locale.US);
	public static IntermedObject convertIntermedObject(DownloadObject downloadObject){
		IntermedObject intermedObject = new IntermedObject();
		intermedObject.setContext(downloadObject.getContent());
		intermedObject.setObjectName(downloadObject.getObject().substring(1));
		ObjectMetadata objectMetadata = downloadObject.getObjectMetadata();
		intermedObject.setMD5String(objectMetadata.getContentMD5());
		for(Entry<String,String> entry:objectMetadata.getUserMetadata().entrySet()){
			if(entry.getKey().startsWith("x-bs-meta-")){
				intermedObject.setMetadata(entry.getKey().replaceFirst("x-bs-meta-","inter-med-"),entry.getValue());
			}else
				intermedObject.setMetadata(entry.getKey(),entry.getValue());
		}
		for(Entry<String,Object> entry:objectMetadata.getRawMetadata().entrySet()){
			if(!"Expires".equals(entry.getKey()))
				intermedObject.setMetadata(entry.getKey(),Object2String(entry.getValue()));
			else
				intermedObject.setMetadata(entry.getKey(),dateStringToFormatString(entry.getValue().toString(),0));
		}
		return intermedObject;
	}
	
	public static IntermedObject convertIntermedObject(OSSObject ossobj){
		IntermedObject intermedObject = new IntermedObject();
		intermedObject.setContext(ossobj.getObjectContent());
		intermedObject.setObjectName(ossobj.getKey());
		com.aliyun.openservices.oss.model.ObjectMetadata objectMetadata = ossobj.getObjectMetadata();
		intermedObject.setMD5String(objectMetadata.getETag());
		for(Entry<String,String> entry:objectMetadata.getUserMetadata().entrySet()){
			if(entry.getKey().startsWith("x-oss-")){
				intermedObject.setMetadata(entry.getKey().replaceFirst("x-oss-","inter-med-"),entry.getValue());
			}else
				intermedObject.setMetadata(entry.getKey(),entry.getValue());
		}
		for(Entry<String,Object> entry:objectMetadata.getRawMetadata().entrySet()){
			intermedObject.setMetadata(entry.getKey(),Object2String(entry.getValue()));
		}
		return intermedObject;
	}
	
	@SuppressWarnings("deprecation")
	public static IntermedObject convertIntermedObject(SNDAObject andaObject){
		IntermedObject intermedObject = new IntermedObject();
		intermedObject.setContext(getInputStreanm(andaObject.getContent()));
		intermedObject.setObjectName(andaObject.getKey());
		SNDAObjectMetadata objectMetadata = andaObject.getObjectMetadata();
		intermedObject.setMD5String(objectMetadata.getETag());
		for(Entry<String,String> entry:objectMetadata.getMetadata().entrySet()){
			if(entry.getKey().startsWith("x-snda-meta-")){
				intermedObject.setMetadata(entry.getKey().replaceFirst("x-snda-meta-","inter-med-"),entry.getValue());
			}else
				intermedObject.setMetadata(entry.getKey(),entry.getValue());
		}
		if(StringUtil.isNotBlank(objectMetadata.getExpires()))
			intermedObject.setMetadata("Expires", objectMetadata.getExpires());
		intermedObject.setMetadata("Content-Length",Long.toString(objectMetadata.getContentLength()));
		if(StringUtil.isNotBlank(objectMetadata.getContentType()))
			intermedObject.setMetadata("Content-Type",objectMetadata.getContentType());
		if(StringUtil.isNotBlank(objectMetadata.getContentEncoding()))
			intermedObject.setMetadata("Content-Encoding",objectMetadata.getContentEncoding());
		if(StringUtil.isNotBlank(objectMetadata.getCacheControl()))
			intermedObject.setMetadata("Cache-Control",objectMetadata.getCacheControl());
		if(StringUtil.isNotBlank(objectMetadata.getContentDisposition()))
			intermedObject.setMetadata("Content-Disposition",objectMetadata.getContentDisposition());
		if(objectMetadata.getLastModified()!=null)
			intermedObject.setMetadata("Last-Modified",dsf.format(objectMetadata.getLastModified().toDate()));
		if(objectMetadata.getExpirationDays()!=null)
			intermedObject.setMetadata("ExpirationDays",Integer.toString(objectMetadata.getExpirationDays()));
		if(StringUtil.isNotBlank(objectMetadata.getETag())){
			String et = objectMetadata.getETag().startsWith("\"")?objectMetadata.getETag().replace("\"",""):objectMetadata.getETag();
			intermedObject.setMetadata("ETag",et);
			intermedObject.setMD5String(et);
		}
		 Closeables.closeQuietly(andaObject);
		return intermedObject;
	}
	public static com.aliyun.openservices.oss.model.ObjectMetadata convertALiOssObjectMetadata(IntermedObject intermedObject){
		com.aliyun.openservices.oss.model.ObjectMetadata objectMetadata = new com.aliyun.openservices.oss.model.ObjectMetadata();
		for(Entry<String,String> entry:intermedObject.getMetadata().entrySet()){
			if("Last-Modified".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue())){
				try {
					objectMetadata.setLastModified(dsf.parse(entry.getValue()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}else if("Expires".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue())){
				try {
					objectMetadata.setExpirationTime(dsf.parse(entry.getValue()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}else if("Content-Length".equals(entry.getKey())){
				objectMetadata.setContentLength(Long.valueOf(entry.getValue()));
			}else if("Content-Type".equals(entry.getKey())){
				objectMetadata.setContentType(entry.getValue());
			}else if("Content-Encoding".equals(entry.getKey())){
				objectMetadata.setContentEncoding(entry.getValue());
			}else if("Cache-Control".equals(entry.getKey())){
				objectMetadata.setCacheControl(entry.getValue());
			}else if("Content-Disposition".equals(entry.getKey())){
				objectMetadata.setContentDisposition(entry.getValue());
			}else if(entry.getKey().startsWith("inter-med-")){
				objectMetadata.addUserMetadata(entry.getKey().replaceFirst("inter-med-", ""),entry.getValue());
			}else{
				objectMetadata.addUserMetadata(entry.getKey(),entry.getValue());
			}
		}
		return objectMetadata;
	}
	
	public static com.baidu.inf.iis.bcs.model.ObjectMetadata convertBaiduBCSObjectMetadata(IntermedObject intermedObject){
		com.baidu.inf.iis.bcs.model.ObjectMetadata objectMetadata = new com.baidu.inf.iis.bcs.model.ObjectMetadata();
		for(Entry<String,String> entry:intermedObject.getMetadata().entrySet()){
			if("Last-Modified".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue())){
				try {
					objectMetadata.setLastModified(dsf.parse(entry.getValue()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}else if("Expires".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue())){
				objectMetadata.setHeader("Expires",dateStringToFormatString(entry.getValue(),1));
			}else if("Content-Length".equals(entry.getKey())){
				objectMetadata.setContentLength(Long.valueOf(entry.getValue()));
			}else if("Content-Type".equals(entry.getKey())){
				objectMetadata.setContentType(entry.getValue());
			}else if("Content-Encoding".equals(entry.getKey())){
				objectMetadata.setContentEncoding(entry.getValue());
			}else if("Cache-Control".equals(entry.getKey())){
				objectMetadata.setCacheControl(entry.getValue());
			}else if("Content-Disposition".equals(entry.getKey())){
				objectMetadata.setContentDisposition(entry.getValue());
			}else if(entry.getKey().startsWith("inter-med-")){
				objectMetadata.addUserMetadata(entry.getKey().replaceFirst("inter-med-", "x-bs-meta-"),entry.getValue());
			}else{
				objectMetadata.addUserMetadata(entry.getKey(),entry.getValue());
			}
		}
		return objectMetadata;
	}
	
	private static String Object2String(Object obj){
		if(obj instanceof Date){
			return dsf.format((Date)obj);
		}else
			return obj.toString();
	}
	
	public static String intermedObjectToString(IntermedObject intermedObject){
		StringBuilder sb = new StringBuilder(intermedObject.getObjectName());
		sb.append(":{");
		for(Entry<String,String> entry:intermedObject.getMetadata().entrySet()){
			sb.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
		}
		sb.deleteCharAt(sb.lastIndexOf(","));
		sb.append("}");
		return sb.toString();
	}
	
	private static String dateStringToFormatString(String dateString,int df){
		try {
			if(df==0)
				return dsf.format(dateStringFormat.parse(dateString));
			else
				return dateStringFormat.format(dsf.parse(dateString));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static InputStream getInputStreanm(InputStream source){
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        byte[] temp = new byte[1024];         
        try {
			int size = 0;         
			while ((size = source.read(temp)) != -1) {  
			    out.write(temp, 0, size);         
			}
			return new ByteArrayInputStream(out.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}        
        return null;
	}
}
