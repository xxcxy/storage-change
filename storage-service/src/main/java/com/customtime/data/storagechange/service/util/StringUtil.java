package com.customtime.data.storagechange.service.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import org.apache.commons.codec.binary.Base64;


public class StringUtil {
	private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
	private static Key key;
	private final static Character CONVERT_SPLIT = '-';
	static{
		try {
			KeyGenerator generator = KeyGenerator.getInstance("DES");
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");  
		    secureRandom.setSeed("customtime".getBytes());  
			generator.init(secureRandom);
			key = generator.generateKey();
			generator = null;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	public static String byteArrayToHexString(byte[] b) {
		StringBuffer resultSb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			resultSb.append(byteToHexString(b[i]));
		}
		return resultSb.toString();
	}
	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0)
			n = 256 + n;
		int d1 = n / 16;
		int d2 = n % 16;
		return hexDigits[d1] + hexDigits[d2];
	}
	
	public static String MD5Encode(String origin) {
		String resultString = null;
		try {
			resultString = new String(origin);
			MessageDigest md = MessageDigest.getInstance("MD5");
			resultString = byteArrayToHexString(md.digest(resultString
					.getBytes()));
		} catch (Exception ex) {
		}
		return resultString;
	}
	
	public static String encryptStr(String strMing) {
		byte[] byteMi = null;
		byte[] byteMing = null;
		String strMi = "";
		try {
			byteMing = strMing.getBytes("UTF8");
			byteMi = encryptByte(byteMing);
			strMi = new String(Base64.encodeBase64(byteMi),"UTF8");
		} catch (Exception e) {
			throw new RuntimeException(
					"Error initializing base64en class. Cause: " + e);
		} finally {
			byteMing = null;
			byteMi = null;
		}
		return strMi;
	}
	
	public static String decryptStr(String strMi) {
		byte[] byteMing = null;
		byte[] byteMi = null;
		String strMing = "";
		try {
			byteMi = Base64.decodeBase64(strMi.getBytes("UTF8"));
			byteMing = decryptByte(byteMi);
			strMing = new String(byteMing, "UTF8");
		} catch (Exception e) {
			throw new RuntimeException(
					"Error initializing base64en class. Cause: " + e);
		} finally {
			byteMing = null;
			byteMi = null;
		}
		return strMing;
	}
	
	private static byte[] encryptByte(byte[] byteS) {
		byte[] byteFina = null;
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byteFina = cipher.doFinal(byteS);
		} catch (Exception e) {
			throw new RuntimeException(
					"Error initializing base64en class. Cause: " + e);
		} finally {
			cipher = null;
		}
		return byteFina;
	}
	
	private static byte[] decryptByte(byte[] byteD) {
		Cipher cipher;
		byte[] byteFina = null;
		try {
			cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			byteFina = cipher.doFinal(byteD);
		} catch (Exception e) {
			throw new RuntimeException(
					"Error initializing base64en class. Cause: " + e);
		} finally {
			cipher = null;
		}
		return byteFina;
	}
	public static String convert(String source){
		if(isBlank(source))
			return source;
		StringBuilder sb = new StringBuilder();
		char c;
		int j;
		String tmp;
		for(int i=0;i<source.length();i++){
			c = source.charAt(i);
//			if(Character.isLetterOrDigit(c))
			if( ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9'))
				sb.append(c);
			else{
				sb.append(CONVERT_SPLIT);
				j = (c >>> 8);
				tmp = Integer.toHexString(j);
				if (tmp.length() == 1)
					sb.append("0");
				sb.append(tmp);
				j = (c & 0xFF); 
				tmp = Integer.toHexString(j);
				if (tmp.length() == 1)
					sb.append("0");
				sb.append(tmp);
				sb.append(CONVERT_SPLIT);
			}	
		}
		return sb.toString();
	}
	public static String revert(String target){
		if(isBlank(target))
			return target;
		StringBuilder sb = new StringBuilder();
		int i=0;
		int k=target.indexOf(CONVERT_SPLIT, i);
		if(k==-1)
			sb.append(target.substring(i));
		while(k!=-1){
			sb.append(target.substring(i,k));
			i=target.indexOf(CONVERT_SPLIT,k+1)+1;
			if(i==0)
				return "";
			else{
				String value = target.substring(k+1,i-1);
				int c=0;
				for (int j = 0; j < value.length(); j++) {
					char tempChar = value.charAt(j);
					int t = 0;
					switch (tempChar) {
						case 'a':
							t = 10;
							break;
						case 'b':
							t = 11;
							break;
						case 'c':
							t = 12;
							break;
						case 'd':
							t = 13;
							break;
						case 'e':
							t = 14;
							break;
						case 'f':
							t = 15;
							break;
						default:
							t = tempChar - 48;
							break;
					}
					c += t * ((int) Math.pow(16, (value.length() - j - 1)));
				}
				sb.append((char) c); 
			}
			k=target.indexOf(CONVERT_SPLIT, i);
			if(k==-1)
				sb.append(target.substring(i));
		}
		return sb.toString();
	}
	public static boolean isBlank(String str) {
		if (str == null || "".equals(str))
			return true;
		return false;
	}
	
	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}
	
	public static String base64ThexString(String source){
		if (isBlank(source))
			return "";
		byte[] byteMi = null;
		try {
			byteMi = Base64.decodeBase64(source.getBytes("UTF8"));
			return byteArrayToHexString(byteMi);
		} catch (Exception e) {
			throw new RuntimeException(
					"Error initializing base64en class. Cause: " + e);
		} finally {
			byteMi = null;
		}
	}
	public static void main(String[] args) throws UnsupportedEncodingException, IOException{
		System.out.println(decryptStr("PHewgJEJpLLWiFtdVTAzmc3466+oXgdUrUusIxiQwmAXitzQgaJik/n4F9oGIWvbJe9kcxKisaY="));
		System.out.println(decryptStr("PHewgJEJpLLWiFtdVTAzmc3466+oXgdUrUusIxiQwmAXitzQgaJik/n4F9oGIWvbJe9kcxKisaY="));
		
	}
}
