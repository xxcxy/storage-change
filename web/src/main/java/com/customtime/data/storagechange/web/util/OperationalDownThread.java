package com.customtime.data.storagechange.web.util;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.storagechange.service.CSService;
import com.customtime.data.storagechange.service.bean.IntermedObject;
import com.customtime.data.storagechange.service.bean.SKey;
import com.customtime.data.storagechange.service.exception.OperatorException;

public class OperationalDownThread implements Runnable {
	private static final Log logger = LogFactory.getLog(OperationalDownThread.class);
	private CSService csService;
	private LinkedBlockingQueue<IntermedObject> queue;
	private List<String> fileNames;
	private String bucketName;
	private SKey sKey;
	private String sourceDir;
	private String targetDir;
	public OperationalDownThread(CSService csService,LinkedBlockingQueue<IntermedObject> queue,List<String> fileNames,String sourceDir,String targetDir,String bucketName,SKey sKey){
		this.csService = csService;
		this.queue = queue;
		this.fileNames = fileNames;
		this.bucketName = bucketName;
		this.sKey = sKey;
		this.sourceDir = sourceDir;
		this.targetDir = targetDir;
	}
	public void run() {
		String fileName = "";
		while(!fileNames.isEmpty()){
			try {
				fileName = fileNames.remove(0);
				String targetName = fileName.replace(sourceDir, targetDir);
				IntermedObject io = csService.getObject(bucketName, fileName,sKey);
				io.setObjectName(targetName);
				queue.put(io);
			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.error("the fileName: "+bucketName+"/"+fileName+" is not upload!");
			} catch (OperatorException e) {
				e.printStackTrace();
				logger.error("the fileName: "+bucketName+"/"+fileName+" download error!");
			} catch (ArrayIndexOutOfBoundsException e) {
				logger.info("no partinfo need to download!");
			}
		}
	}
}
