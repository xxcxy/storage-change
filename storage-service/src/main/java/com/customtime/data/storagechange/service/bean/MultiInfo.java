package com.customtime.data.storagechange.service.bean;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MultiInfo {
	private String bucketName;
	private String fileName;
	private String key;
	private List<PartInfo> nPartInfos;
	private Map<String,PartInfo> alPartInfos;
	public List<PartInfo> getnPartInfos() {
		return nPartInfos;
	}
	public void setnPartInfos(List<PartInfo> nPartInfos) {
		this.nPartInfos = nPartInfos;
	}
	public String getBucketName() {
		return bucketName;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Map<String, PartInfo> getAlPartInfos() {
		return alPartInfos;
	}
	public void setAlPartInfos(Map<String, PartInfo> alPartInfos) {
		this.alPartInfos = alPartInfos;
	}
	public MultiInfo(String bucketName,String fileName){
		this.bucketName = bucketName;
		this.fileName = fileName;
		nPartInfos = new Vector<PartInfo>();
		alPartInfos = new Hashtable<String,PartInfo>();
	}
	public void addAL(String tag,PartInfo partInfo){
		alPartInfos.put(tag, partInfo);
	}
	public void addNP(PartInfo partInfo){
		nPartInfos.add(partInfo);
	}
	public PartInfo getPartInfo(long start){
		for(PartInfo partInfo:nPartInfos){
			if(partInfo.getStart() == start)
				return partInfo;
		}
		return null;
	}
	public void remove(PartInfo partInfo){
		nPartInfos.remove(partInfo);
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
}
