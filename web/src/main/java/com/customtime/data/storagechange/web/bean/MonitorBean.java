package com.customtime.data.storagechange.web.bean;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import com.customtime.data.storagechange.service.util.DateUtil;

public class MonitorBean {
	private final AtomicInteger fileCount = new AtomicInteger(0);
	private List<String> successFiles;
	private List<String> warnFiles;
	private List<String> errorFiles;
	private Date startTime;
	private Date endTime;
	public MonitorBean(){
		successFiles = new Vector<String>();
		warnFiles = new Vector<String>();
		errorFiles = new Vector<String>();
	}
	public int getFileCount() {
		return fileCount.get();
	}
	public void addFile(){
		fileCount.getAndIncrement();
	}
	public void addFile(int i){
		fileCount.getAndAdd(i);
	}
	public List<String> getSuccessFiles() {
		return successFiles;
	}
	public void addSuccessFiles(List<String> successFiles) {
		this.successFiles.addAll(successFiles);
	}
	public void addSuccessFile(String fileName){
		successFiles.add(fileName);
	}
	public List<String> getWarnFiles() {
		return warnFiles;
	}
	public void addWarnFiles(List<String> warnFiles) {
		this.warnFiles.addAll(warnFiles);
	}
	public void addWarnFile(String fileName){
		warnFiles.add(fileName);
	}
	public List<String> getErrorFiles() {
		return errorFiles;
	}
	public void addErrorFiles(List<String> errorFiles) {
		this.errorFiles.addAll(errorFiles);
	}
	public void addErrorFile(String fileName){
		errorFiles.add(fileName);
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public Long getOperatorTime() {
		if(startTime==null||endTime==null)
			return -1L;
		else
			return endTime.getTime()-startTime.getTime();
	}
	public void start(){
		startTime = DateUtil.getNow();
	}
	public void end(){
		endTime = DateUtil.getNow();
	}
}
