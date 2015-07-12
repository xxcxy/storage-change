package com.customtime.data.storagechange.web.util;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.storagechange.service.CSService;
import com.customtime.data.storagechange.service.bean.IntermedObject;
import com.customtime.data.storagechange.service.bean.PartInfo;
import com.customtime.data.storagechange.service.bean.SKey;
import com.customtime.data.storagechange.service.exception.OperatorException;
import com.customtime.data.storagechange.web.bean.MultiQueueBean;

public class MultiDownloadPartThread implements Runnable {
	private static final Log logger = LogFactory.getLog(MultiDownloadPartThread.class);
	private CSService csService;
	private LinkedBlockingQueue<MultiQueueBean> queue;
	private String fileName;
	private String toFileName;
	private String bucketName;
	private SKey sKey;
	private List<PartInfo> downParts;
	
	public MultiDownloadPartThread(CSService csService,LinkedBlockingQueue<MultiQueueBean> queue,List<PartInfo> downParts,String fileName,String toFileName,String bucketName,SKey sKey){
		this.csService = csService;
		this.queue = queue;
		this.downParts = downParts;
		this.fileName = fileName;
		this.toFileName = toFileName;
		this.bucketName = bucketName;
		this.sKey = sKey;
	}
	public void run() {
		while (!downParts.isEmpty()) {
			try {
				PartInfo pi = downParts.remove(0);
				IntermedObject io = csService.getObject(bucketName, fileName,pi,sKey);
				io.setObjectName(toFileName);
				queue.put(new MultiQueueBean(pi, io));
			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.error("the fileName: " + bucketName + "/" + fileName
						+ " is not upload!");
			} catch (OperatorException e) {
				e.printStackTrace();
				logger.error("the fileName: " + bucketName + "/" + fileName
						+ " download error!");
			} catch (ArrayIndexOutOfBoundsException e) {
				logger.info("no partinfo need to download!");
			}
		}
	}
}
