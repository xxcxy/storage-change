package com.customtime.data.storagechange.service.bean;

public class PartInfo {
	private long start;
	private long size;
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public long getEnd(){
		return start+size;
	}
	public PartInfo(long start,long size){
		this.start=start;
		this.size=size;
	}
}
