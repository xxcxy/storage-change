package com.customtime.data.storagechange.web.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.storagechange.service.CSService;
import com.customtime.data.storagechange.service.bean.IntermedObject;
import com.customtime.data.storagechange.service.bean.SKey;
import com.customtime.data.storagechange.service.exception.OperatorException;
import com.customtime.data.storagechange.web.bean.MonitorBean;

public class OperationalUpThread implements Runnable {
	private static final Log logger = LogFactory.getLog(OperationalUpThread.class);
	private CSService csService;
	private LinkedBlockingQueue<IntermedObject> queue;
	private String bucketName;
	private SKey sKey;
	private boolean isFinish;
	private MonitorBean monitorBean;
	public OperationalUpThread(CSService csService,LinkedBlockingQueue<IntermedObject> queue,String bucketName,SKey sKey,MonitorBean mb){
		this.csService = csService;
		this.queue = queue;
		this.bucketName = bucketName;
		this.sKey = sKey;
		monitorBean = mb;
		isFinish = false;
	}
	public void run() {
		String fileLog = "";
		while (!isFinish || !queue.isEmpty()) {
			try {
				IntermedObject io = queue.poll(1L, TimeUnit.SECONDS);
				if (io != null){
					fileLog = io.getObjectName();
					if(csService.putObject(bucketName, io, sKey)){
						monitorBean.addSuccessFile(fileLog);
					}else
						monitorBean.addWarnFile(fileLog);
					monitorBean.addFile();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (OperatorException e) {
				e.printStackTrace();
				logger.error("the fileName: "+bucketName+"/"+fileLog+" upload error!");
				monitorBean.addErrorFile(fileLog);
			}
		}
	}
	public void threadFinish(){
		this.isFinish = true;
	}
}
