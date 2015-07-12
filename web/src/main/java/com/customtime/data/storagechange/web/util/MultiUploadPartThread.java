package com.customtime.data.storagechange.web.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.storagechange.service.CSService;
import com.customtime.data.storagechange.service.bean.IntermedObject;
import com.customtime.data.storagechange.service.bean.MultiInfo;
import com.customtime.data.storagechange.service.bean.PartInfo;
import com.customtime.data.storagechange.service.bean.SKey;
import com.customtime.data.storagechange.service.exception.OperatorException;
import com.customtime.data.storagechange.web.bean.MonitorBean;
import com.customtime.data.storagechange.web.bean.MultiQueueBean;

public class MultiUploadPartThread implements Runnable {
	private static final Log logger = LogFactory.getLog(MultiUploadPartThread.class);
	private CSService csService;
	private LinkedBlockingQueue<MultiQueueBean> queue;
	private SKey sKey;
	private boolean isFinish;
	private MonitorBean monitorBean;
	private MultiInfo multiInfo;
	
	public MultiUploadPartThread(CSService csService,LinkedBlockingQueue<MultiQueueBean> queue,SKey sKey,MultiInfo multiInfo,MonitorBean mb){
		this.csService = csService;
		this.queue = queue;
		this.sKey = sKey;
		monitorBean = mb;
		isFinish = false;
	}
	public void run() {
		String fileLog = "";
		while (!isFinish || !queue.isEmpty()) {
			try {
				MultiQueueBean mqb = queue.poll(1L, TimeUnit.SECONDS);
				if (mqb != null){
					IntermedObject io = mqb.getIntermedObject();
					PartInfo pi = mqb.getPartInfo();
					fileLog = io.getObjectName()+"_"+pi.getStart()/Constants.FILE_PART_SIZE;
					if(csService.putPartObject(pi, io, sKey,multiInfo)){
						monitorBean.addSuccessFile(fileLog);
					}else
						monitorBean.addWarnFile(fileLog);
					monitorBean.addFile();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (OperatorException e) {
				e.printStackTrace();
				logger.error("the fileName: "+fileLog+" upload error!");
				monitorBean.addErrorFile(fileLog);
			}
		}
	}
	
	public void threadFinish(){
		this.isFinish = true;
	}
}
