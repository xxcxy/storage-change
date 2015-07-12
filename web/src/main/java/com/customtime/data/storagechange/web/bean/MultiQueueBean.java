package com.customtime.data.storagechange.web.bean;

import com.customtime.data.storagechange.service.bean.IntermedObject;
import com.customtime.data.storagechange.service.bean.PartInfo;

public class MultiQueueBean {
	private PartInfo partInfo;
	private IntermedObject intermedObject;
	public MultiQueueBean(PartInfo partInfo,IntermedObject intermedObject){
		this.intermedObject = intermedObject;
		this.partInfo = partInfo;
	}
	public PartInfo getPartInfo() {
		return partInfo;
	}
	public void setPartInfo(PartInfo partInfo) {
		this.partInfo = partInfo;
	}
	public IntermedObject getIntermedObject() {
		return intermedObject;
	}
	public void setIntermedObject(IntermedObject intermedObject) {
		this.intermedObject = intermedObject;
	}
}
