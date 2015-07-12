package com.customtime.data.storagechange.web.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.customtime.data.storagechange.service.ALiOSSService;
import com.customtime.data.storagechange.service.BaiDuBCSService;
import com.customtime.data.storagechange.service.CSService;
import com.customtime.data.storagechange.service.bean.FileObject;
import com.customtime.data.storagechange.service.bean.IntermedObject;
import com.customtime.data.storagechange.service.bean.MultiInfo;
import com.customtime.data.storagechange.service.bean.PartInfo;
import com.customtime.data.storagechange.service.bean.SKey;
import com.customtime.data.storagechange.service.exception.OperatorException;
import com.customtime.data.storagechange.service.util.StringUtil;
import com.customtime.data.storagechange.web.bean.MonitorBean;
import com.customtime.data.storagechange.web.bean.MultiQueueBean;
import com.customtime.data.storagechange.web.util.Constants;
import com.customtime.data.storagechange.web.util.MultiDownloadPartThread;
import com.customtime.data.storagechange.web.util.MultiUploadPartThread;
import com.customtime.data.storagechange.web.util.OperationalDownThread;
import com.customtime.data.storagechange.web.util.OperationalUpThread;

public class OperationalService {
	private Map<String,CSService> serviceMap;
	public Map<String, CSService> getServiceMap() {
		return serviceMap;
	}
	public void setServiceMap(Map<String, CSService> serviceMap) {
		this.serviceMap = serviceMap;
	}

//	public boolean bcsSyn2Oss(String bucketName){
//		List<IntermedObject> intermedObjects = serviceMap.get("").getAllObjFromBucket(bucketName,"4E41891c7a25c4e9439d48a9d4e9d874","5b02f5642c3aa02df3310d3e9172dad2");
//		return aliOssService.putFiles(intermedObjects,"Dvuhyf4nNFhChKaV","62RCHTPornY3pMDpur3p7kZa2qQgpL", bucketName);
//	}
//	
//	public boolean ossSyn2Bcs(String bucketName,MonitorBean mb){
//		SKey sourceKey = new SKey();
//		sourceKey.setKeyId("Dvuhyf4nNFhChKaV");
//		sourceKey.setKeySecret("62RCHTPornY3pMDpur3p7kZa2qQgpL");
//		SKey targetKey = new SKey();
//		targetKey.setKeyId("4E41891c7a25c4e9439d48a9d4e9d874");
//		targetKey.setKeySecret("5b02f5642c3aa02df3310d3e9172dad2");
//		try {
//			List<String> fileNames = aliOssService.ListObject(bucketName,sourceKey);
//			return synFiles(fileNames,bucketName,bucketName,aliOssService,baiDuBCSService,sourceKey,targetKey,mb);
//		} catch (OperatorException e) {
//			e.printStackTrace();
//			return false;
//		}
//	}
//	
	public static void main(String[] args){
		String bucketName = "bcs2oss";
		List<IntermedObject> intermedObjects = new BaiDuBCSService().getAllObjFromBucket(bucketName,"4E41891c7a25c4e9439d48a9d4e9d874","5b02f5642c3aa02df3310d3e9172dad2");
		new ALiOSSService().putFiles(intermedObjects,"Dvuhyf4nNFhChKaV","62RCHTPornY3pMDpur3p7kZa2qQgpL", bucketName);//"Dvuhyf4nNFhChKaV","62RCHTPornY3pMDpur3p7kZa2qQgpL"
	}
	public boolean transferFile(SKey sourceKey,SKey targetKey,String sourceFile,String targetDir,MonitorBean mb){
		String sourceBucket = getBucketName(sourceFile);
		String targetBucket = getBucketName(targetDir);
		String sourcePath = filePrefix(sourceFile);
		String targetPath = filePrefix(targetDir);
		return synFile(sourcePath,targetPath+"/"+sourcePath.substring(sourcePath.lastIndexOf("/")+1),0L,sourceBucket,targetBucket,sourceKey,targetKey,mb,false);
	}
	public boolean transferDir(SKey sourceKey,SKey targetKey,String sourceDir,String targetDir,MonitorBean mb){
		String sourceBucket = getBucketName(sourceDir);
		String targetBucket = getBucketName(targetDir);
		String sourcePath = filePrefix(sourceDir);
		String targetPath = filePrefix(targetDir);
		CSService service = serviceMap.get(sourceKey.getKeyType());
		if(service==null)
			return false;
		try {
			List<FileObject> fs = service.listAllObject(sourceKey, sourceBucket, sourcePath);
			List<String> fnames = new ArrayList<String>(fs.size());
			for(FileObject fo:fs)
				fnames.add(fo.getPath());
			return synFiles(fnames,sourcePath,targetPath,sourceBucket,targetBucket,sourceKey,targetKey,mb);
		} catch (OperatorException e) {
			e.printStackTrace();
			return false;
		}
	}
	public List<FileObject> listDirs(SKey skey,String parentPath) throws OperatorException{
		List<FileObject> dirs = new ArrayList<FileObject>();
		CSService service = serviceMap.get(skey.getKeyType());
		if(service!=null)
			dirs.addAll(tranceFileObjects(service.listDirs(skey,getBucketName(parentPath),filePrefix(parentPath))));
		return dirs;
	}
	public List<FileObject> listObject(SKey skey,String parentPath)throws OperatorException{
		List<FileObject> files = new ArrayList<FileObject>();
		CSService service = serviceMap.get(skey.getKeyType());
		if(service!=null)
			files.addAll(tranceFileObjects(service.listObject(skey,getBucketName(parentPath),filePrefix(parentPath))));
		return files;
	}
	public List<FileObject> listBuckets(SKey skey) throws OperatorException{
		CSService service = serviceMap.get(skey.getKeyType());
		List<FileObject> buckets = new ArrayList<FileObject>();
		if(service!=null){
			buckets.addAll(service.listBucket(skey));
		}
		return buckets;
	}
	public boolean synFiles(List<String> fileNames,String sourceDir,String targetDir,String sourceBucketName,String targetBucketName,SKey sourceKey,SKey targetKey,MonitorBean mb){
		CSService sourceService = serviceMap.get(sourceKey.getKeyType());
		CSService targetService = serviceMap.get(targetKey.getKeyType());
		if(sourceService==null||targetService==null)
			return false;
		if(!targetService.isBucketExist(targetBucketName,targetKey)){
			try {
				targetService.createBucket(targetBucketName,targetKey);
			} catch (OperatorException e1) {
				e1.printStackTrace();
				return false;
			}
		}
		ThreadPoolExecutor downTE = new ThreadPoolExecutor(Constants.DOWNLOAD_THREAD_COUNT,Constants.DOWNLOAD_THREAD_COUNT,1L,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
		ThreadPoolExecutor upTE = new ThreadPoolExecutor(Constants.UPLOAD_THREAD_COUNT,Constants.UPLOAD_THREAD_COUNT,1L,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
		LinkedBlockingQueue<IntermedObject> queue = new LinkedBlockingQueue<IntermedObject>(Constants.QUEUE_CAPACITY);
		List<String> dp = new Vector<String>(fileNames);
		mb.start();
		for(int i=0;i<Constants.DOWNLOAD_THREAD_COUNT;i++){
			downTE.execute(new OperationalDownThread(sourceService,queue,dp,sourceDir,targetDir,sourceBucketName,sourceKey));
		}
		List<OperationalUpThread> upThreads = new ArrayList<OperationalUpThread>(Constants.UPLOAD_THREAD_COUNT);
		for(int i=0;i<Constants.UPLOAD_THREAD_COUNT;i++){
			OperationalUpThread ot = new OperationalUpThread(targetService,queue,targetBucketName,targetKey,mb);
			upTE.execute(ot);
			upThreads.add(ot);
		}
		downTE.shutdown();
		upTE.shutdown();
		boolean isFinish = false;
		while(!isFinish){
			if(downTE.isTerminated()){
				for(OperationalUpThread ot:upThreads){
					ot.threadFinish();
				}
			}
			if(upTE.isTerminated()){
				isFinish = true;
				mb.end();
			}
			if(!isFinish){
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	
	public boolean synFile(String sourceFile,String targetFile,Long fileSize,String sourceBucket,String targetBucket,SKey sourceKey,SKey targetKey,MonitorBean mb,boolean superFile){
		CSService sourceService = serviceMap.get(sourceKey.getKeyType());
		CSService targetService = serviceMap.get(targetKey.getKeyType());
		if(sourceService==null||targetService==null)
			return false;
		if(superFile&&fileSize>Constants.FILE_PART_SIZE){
			return synMultiFile(sourceFile,targetFile,fileSize,sourceBucket,targetBucket,sourceService,targetService,sourceKey,targetKey,mb);
		}else{
			try {
				mb.start();
				IntermedObject io = sourceService.getObject(sourceBucket, sourceFile, sourceKey);
				io.setObjectName(targetFile);
				if(targetService.putObject(targetBucket, io, targetKey)){
					mb.addSuccessFile(targetFile);
				}else{
					mb.addWarnFile(targetFile);
				}
				mb.addFile();
			} catch (OperatorException e) {
				e.printStackTrace();
				mb.addErrorFile(targetFile);
//				return false;
			}finally{
				mb.end();
			}
			return true;
		}
	}
	
	private boolean synMultiFile(String sourceFile,String targetFile,Long fileSize,String sourceBucket,String targetBucket,CSService sourceService,CSService targetService,SKey sourceKey,SKey targetKey,MonitorBean mb){
		try{
			MultiInfo multiInfo = targetService.getNeccessPart(targetBucket, targetFile, targetKey, fileSize);
			ThreadPoolExecutor downTE = new ThreadPoolExecutor(Constants.DOWNLOAD_THREAD_COUNT,Constants.DOWNLOAD_THREAD_COUNT,1L,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
			ThreadPoolExecutor upTE = new ThreadPoolExecutor(Constants.UPLOAD_THREAD_COUNT,Constants.UPLOAD_THREAD_COUNT,1L,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
			LinkedBlockingQueue<MultiQueueBean> queue = new LinkedBlockingQueue<MultiQueueBean>(Constants.QUEUE_CAPACITY);
			List<PartInfo> downPart = new Vector<PartInfo>(multiInfo.getnPartInfos());
			mb.start();
			for(int i=0;i<Constants.DOWNLOAD_THREAD_COUNT;i++){
				downTE.execute(new MultiDownloadPartThread(sourceService,queue,downPart,sourceFile,targetFile,sourceBucket,sourceKey));
			}
			List<MultiUploadPartThread> upThreads = new ArrayList<MultiUploadPartThread>(Constants.UPLOAD_THREAD_COUNT);
			for(int i=0;i<Constants.UPLOAD_THREAD_COUNT;i++){
				MultiUploadPartThread ot = new MultiUploadPartThread(targetService,queue,targetKey,multiInfo,mb);
				upTE.execute(ot);
				upThreads.add(ot);
			}
			downTE.shutdown();
			upTE.shutdown();
			boolean isFinish = false;
			while(!isFinish){
				if(downTE.isTerminated()){
					for(MultiUploadPartThread ot:upThreads){
						ot.threadFinish();
					}
				}
				if(upTE.isTerminated()){
					isFinish = true;
				}
				if(!isFinish){
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			targetService.completeMultipartUpload(targetKey, multiInfo);
			return true;
		}catch (OperatorException e) {
			e.printStackTrace();
			return false;
		}finally{
			mb.end();
		}
	}
	
	private String getBucketName(String path){
		if(path.contains("/"))
			return path.substring(0,path.indexOf("/"));
		else
			return path;
	}
	private String filePrefix(String path){
		if(path.contains("/"))
			return path.substring(path.indexOf("/")+1);
		else
			return "";
	}
	private List<FileObject> tranceFileObjects(List<FileObject> fileObjects){
		for(FileObject fo:fileObjects){
			fo.setPath(fo.getBucketName()+"/"+fo.getPath());
			if(StringUtil.isBlank(fo.getParentPath()))
				fo.setParentPath(fo.getBucketName());
			else
				fo.setParentPath(fo.getBucketName()+"/"+fo.getParentPath());
		}
		return fileObjects;
	}
}
