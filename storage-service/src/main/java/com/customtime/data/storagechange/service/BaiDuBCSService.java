package com.customtime.data.storagechange.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.baidu.inf.iis.bcs.BaiduBCS;
import com.baidu.inf.iis.bcs.auth.BCSCredentials;
import com.baidu.inf.iis.bcs.model.BCSClientException;
import com.baidu.inf.iis.bcs.model.BCSServiceException;
import com.baidu.inf.iis.bcs.model.BucketSummary;
import com.baidu.inf.iis.bcs.model.DownloadObject;
import com.baidu.inf.iis.bcs.model.ObjectListing;
import com.baidu.inf.iis.bcs.model.ObjectMetadata;
import com.baidu.inf.iis.bcs.model.ObjectSummary;
import com.baidu.inf.iis.bcs.model.Pair;
import com.baidu.inf.iis.bcs.model.SuperfileSubObject;
import com.baidu.inf.iis.bcs.request.GetObjectRequest;
import com.baidu.inf.iis.bcs.request.ListBucketRequest;
import com.baidu.inf.iis.bcs.request.ListObjectRequest;
import com.baidu.inf.iis.bcs.request.PutSuperfileRequest;
import com.baidu.inf.iis.bcs.response.BaiduBCSResponse;
import com.customtime.data.storagechange.service.bean.FileObject;
import com.customtime.data.storagechange.service.bean.IntermedObject;
import com.customtime.data.storagechange.service.bean.MultiInfo;
import com.customtime.data.storagechange.service.bean.PartInfo;
import com.customtime.data.storagechange.service.bean.SKey;
import com.customtime.data.storagechange.service.exception.OperatorException;
import com.customtime.data.storagechange.service.util.Constants;
import com.customtime.data.storagechange.service.util.IntermedObjectUtil;
import com.customtime.data.storagechange.service.util.StringUtil;

@Service
public class BaiDuBCSService implements CSService{
	private static final Log logger = LogFactory.getLog(BaiDuBCSService.class);
	public static void main(String[] args) {
		BCSCredentials credentials = new BCSCredentials("4E41891c7a25c4e9439d48a9d4e9d874","5b02f5642c3aa02df3310d3e9172dad2");
		BaiduBCS baiduBCS = new BaiduBCS(credentials, Constants.BAIDU_BCS_URL);
		baiduBCS.setDefaultEncoding("UTF-8");
		ListObjectRequest listObjectRequest = new ListObjectRequest("bcs2oss");
//		listObjectRequest.setStart(0);
//		listObjectRequest.setLimit(20);
		// ------------------by dir
		{
			// prefix must start with '/' and end with '/'
			// listObjectRequest.setPrefix("/1/");
			// listObjectRequest.setListModel(2);
		}
		// ------------------only object
		{
			// prefix must start with '/'
			// listObjectRequest.setPrefix("/1/");
		}
		listObjectRequest.setListModel(2);
		listObjectRequest.setPrefix("/txt/");
		BaiduBCSResponse<ObjectListing> response = baiduBCS.listObject(listObjectRequest);
		logger.debug("we get [" + response.getResult().getObjectSummaries().size() + "] object record.");
		for (ObjectSummary os : response.getResult().getObjectSummaries()) {
			logger.info(os.toString());
			logger.info(os.getName());
			System.out.println(os.getSize());
		}
//		for(IntermedObject io:new BaiDuBCSService().getAllObjFromBucket("bcs2oss","4E41891c7a25c4e9439d48a9d4e9d874","5b02f5642c3aa02df3310d3e9172dad2")){
//			logger.debug(IntermedObjectUtil.intermedObjectToString(io));
//		}
	}
	
	public List<IntermedObject> getAllObjFromBucket(String bucketName,String accessKey,String secretKey){
		List<IntermedObject> intermedObjects = new ArrayList<IntermedObject>();
		BCSCredentials credentials = new BCSCredentials(accessKey,secretKey);
		BaiduBCS baiduBCS = new BaiduBCS(credentials, Constants.BAIDU_BCS_URL);
		baiduBCS.setDefaultEncoding("UTF-8");
		ListObjectRequest listObjectRequest = new ListObjectRequest(bucketName);
		BaiduBCSResponse<ObjectListing> response = baiduBCS.listObject(listObjectRequest);
		for (ObjectSummary os : response.getResult().getObjectSummaries()) {
			DownloadObject downloadObject = baiduBCS.getObject(bucketName, os.getName()).getResult();
			intermedObjects.add(IntermedObjectUtil.convertIntermedObject(downloadObject));
		}
		return intermedObjects;
	}

	
	public boolean isBucketExist(String bucketName,SKey key) {
		BCSCredentials credentials = new BCSCredentials(key.getKeyId(),key.getKeySecret());
		BaiduBCS baiduBCS = new BaiduBCS(credentials, Constants.BAIDU_BCS_URL);
		baiduBCS.setDefaultEncoding("UTF-8");
		BaiduBCSResponse<List<BucketSummary>> bcsRes = baiduBCS.listBucket(new ListBucketRequest());
		if(bcsRes == null)
			return false;
		for(BucketSummary bs:bcsRes.getResult()){
			if(bs.getBucket().equals(bucketName))
				return true;
		}
		return false;
	}

	
	public void createBucket(String bucketName,SKey key)throws OperatorException {
		BCSCredentials credentials = new BCSCredentials(key.getKeyId(),key.getKeySecret());
		BaiduBCS baiduBCS = new BaiduBCS(credentials, Constants.BAIDU_BCS_URL);
		baiduBCS.setDefaultEncoding("UTF-8");
		try{
			baiduBCS.createBucket(bucketName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
	}

	
	public IntermedObject getObject(String bucketName, String fileName,SKey key)throws OperatorException {
		BCSCredentials credentials = new BCSCredentials(key.getKeyId(),key.getKeySecret());
		BaiduBCS baiduBCS = new BaiduBCS(credentials, Constants.BAIDU_BCS_URL);
		baiduBCS.setDefaultEncoding("UTF-8");
		BaiduBCSResponse<DownloadObject> bcsRes = null;
		try{
			bcsRes = baiduBCS.getObject(bucketName,"/"+fileName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		if(bcsRes!=null){
			DownloadObject downloadObject = bcsRes.getResult();
			if(downloadObject!=null)
				return IntermedObjectUtil.convertIntermedObject(downloadObject);
		}
		return null;
	}

	
	public boolean putObject(String bucketName,IntermedObject intermedObject,SKey key)throws OperatorException {
		BCSCredentials credentials = new BCSCredentials(key.getKeyId(),key.getKeySecret());
		BaiduBCS baiduBCS = new BaiduBCS(credentials, Constants.BAIDU_BCS_URL);
		baiduBCS.setDefaultEncoding("UTF-8");
		BaiduBCSResponse<ObjectMetadata> bcsRes = null;
		try{
			bcsRes = baiduBCS.putObject(bucketName,"/"+intermedObject.getObjectName(),intermedObject.getContext(),IntermedObjectUtil.convertBaiduBCSObjectMetadata(intermedObject));
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		if(bcsRes!=null){
			ObjectMetadata om = bcsRes.getResult();
			if(om!=null&&StringUtil.isNotBlank(om.getContentMD5())&&om.getContentMD5().equalsIgnoreCase(intermedObject.getMD5String()))
				return true;
		}
		return false;
	}

	
	public List<String> listObject(String bucketName, SKey key)throws OperatorException {
		List<String> fileNames = new ArrayList<String>();
		BCSCredentials credentials = new BCSCredentials(key.getKeyId(),key.getKeySecret());
		BaiduBCS baiduBCS = new BaiduBCS(credentials, Constants.BAIDU_BCS_URL);
		baiduBCS.setDefaultEncoding("UTF-8");
		BaiduBCSResponse<ObjectListing> bcsRes = null;
		try{
			bcsRes = baiduBCS.listObject(new ListObjectRequest(bucketName));
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		if(bcsRes!=null){
			ObjectListing ol = bcsRes.getResult();
			if(ol!=null){
				for(ObjectSummary os:ol.getObjectSummaries()){
					fileNames.add(os.getName());
				}
			}
		}
		return fileNames;
	}

	
	public IntermedObject getObject(String bucketName, String fileName,
			PartInfo partInfo, SKey key) throws OperatorException {
		BCSCredentials credentials = new BCSCredentials(key.getKeyId(),key.getKeySecret());
		BaiduBCS baiduBCS = new BaiduBCS(credentials, Constants.BAIDU_BCS_URL);
		baiduBCS.setDefaultEncoding("UTF-8");
		BaiduBCSResponse<DownloadObject> bcsRes = null;
		try{
			GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName,"/"+fileName);
			getObjectRequest.setRange(new Pair<Long>(partInfo.getStart(),partInfo.getEnd()-1));
			bcsRes = baiduBCS.getObject(getObjectRequest);
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		if(bcsRes!=null){
			DownloadObject downloadObject = bcsRes.getResult();
			if(downloadObject!=null)
				return IntermedObjectUtil.convertIntermedObject(downloadObject);
		}
		return null;
	}

	
	public MultiInfo getNeccessPart(String bucketName, String fileName, SKey key,long size)
			throws OperatorException {
		if(size<=Constants.FILE_PART_SIZE)
			return null;
		MultiInfo mi = new MultiInfo(bucketName,fileName);
		BCSCredentials credentials = new BCSCredentials(key.getKeyId(),key.getKeySecret());
		BaiduBCS baiduBCS = new BaiduBCS(credentials, Constants.BAIDU_BCS_URL);
		baiduBCS.setDefaultEncoding("UTF-8");
		ListObjectRequest listObjectRequest = new ListObjectRequest(bucketName);
		listObjectRequest.setPrefix("/"+fileName+Constants.FILE_PART_MARK+"/");
		BaiduBCSResponse<ObjectListing> bcsRes = null;
		try{
			bcsRes = baiduBCS.listObject(listObjectRequest);
			long lastSize = size%Constants.FILE_PART_SIZE;
			long partCount = size/Constants.FILE_PART_SIZE +1;
			List<Integer> has = new ArrayList<Integer>();
			if(bcsRes!=null){
				ObjectListing ol = bcsRes.getResult();
				if(ol!=null){
					for(ObjectSummary os:ol.getObjectSummaries()){
						ObjectMetadata om = baiduBCS.getObjectMetadata(bucketName,os.getName()).getResult();
						if(om!=null){
							String tag = om.getETag();
							String fName = os.getName();
							Integer p = Integer.parseInt(fName.substring(fName.lastIndexOf("_")+1));
							has.add(p);
							mi.addAL(tag, new PartInfo(p*Constants.FILE_PART_SIZE,om.getContentLength()));
						}
					}
				}
			}
			for(Integer i=0;i<partCount;i++){
				if(has.contains(i))
					continue;
				if(i==partCount-1)
					mi.addNP(new PartInfo(i*Constants.FILE_PART_SIZE,lastSize));
				else
					mi.addNP(new PartInfo(i*Constants.FILE_PART_SIZE,Constants.FILE_PART_SIZE));
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		return mi;
	}

	
	public boolean putPartObject(PartInfo partInfo,IntermedObject intermedObject,SKey key,MultiInfo mi)
			throws OperatorException {
		BCSCredentials credentials = new BCSCredentials(key.getKeyId(),key.getKeySecret());
		BaiduBCS baiduBCS = new BaiduBCS(credentials, Constants.BAIDU_BCS_URL);
		baiduBCS.setDefaultEncoding("UTF-8");
		BaiduBCSResponse<ObjectMetadata> bcsRes = null;
		try{
			long i = partInfo.getStart()/Constants.FILE_PART_SIZE;
			bcsRes = baiduBCS.putObject(mi.getBucketName(),"/"+mi.getFileName()+Constants.FILE_PART_MARK+i,intermedObject.getContext(),IntermedObjectUtil.convertBaiduBCSObjectMetadata(intermedObject));
			PartInfo pi = mi.getPartInfo(partInfo.getStart());
			if(pi!=null&&bcsRes!=null&&bcsRes.getResult()!=null){
				mi.remove(pi);
				mi.addAL(bcsRes.getResult().getETag(),pi);
				String md5 = bcsRes.getResult().getContentMD5();
				if(StringUtil.isNotBlank(md5)&&md5.equalsIgnoreCase(intermedObject.getMD5String()))
					return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		return false;
	}

	
	public boolean completeMultipartUpload(SKey key, MultiInfo mi)
			throws OperatorException {
		if(!mi.getnPartInfos().isEmpty())
			return false;
		BCSCredentials credentials = new BCSCredentials(key.getKeyId(),key.getKeySecret());
		BaiduBCS baiduBCS = new BaiduBCS(credentials, Constants.BAIDU_BCS_URL);
		baiduBCS.setDefaultEncoding("UTF-8");
		List<SuperfileSubObject> subObjectList = new ArrayList<SuperfileSubObject>();
		for(Entry<String,PartInfo> entry:mi.getAlPartInfos().entrySet()){
			long i = entry.getValue().getStart()/Constants.FILE_PART_SIZE;
			subObjectList.add(new SuperfileSubObject(mi.getBucketName(),"/"+mi.getFileName()+Constants.FILE_PART_MARK+i,entry.getKey()));
		}
		PutSuperfileRequest request = new PutSuperfileRequest(mi.getBucketName(),mi.getFileName(), subObjectList);
		try{
			baiduBCS.putSuperfile(request);
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		return true;
	}

	
	public long getFileSize(String bucketName, String fileName, SKey key) {
		BCSCredentials credentials = new BCSCredentials(key.getKeyId(),key.getKeySecret());
		BaiduBCS baiduBCS = new BaiduBCS(credentials, Constants.BAIDU_BCS_URL);
		baiduBCS.setDefaultEncoding("UTF-8");
		try {
			BaiduBCSResponse<ObjectMetadata> bcsRes = baiduBCS.getObjectMetadata(bucketName,"/"+fileName);
			if(bcsRes !=null){
				ObjectMetadata om = bcsRes.getResult();
				if(om!=null)
					return om.getContentLength();
			}
		} catch (BCSServiceException e) {
			e.printStackTrace();
		} catch (BCSClientException e) {
			e.printStackTrace();
		}
		return -1L;
	}

	
	public List<FileObject> listBucket(SKey key)throws OperatorException {
		BCSCredentials credentials = new BCSCredentials(key.getKeyId(),key.getKeySecret());
		BaiduBCS baiduBCS = new BaiduBCS(credentials, Constants.BAIDU_BCS_URL);
		baiduBCS.setDefaultEncoding("UTF-8");
		List<FileObject> fileObjects = new ArrayList<FileObject>();
		try {
			BaiduBCSResponse<List<BucketSummary>> bcsRes = baiduBCS.listBucket(new ListBucketRequest());
			if(bcsRes !=null){
				for(BucketSummary bs:bcsRes.getResult()){
					FileObject fo = new FileObject();
					fo.setBucketName(bs.getBucket());
					fo.setHasDirs(true);
					fo.setName(bs.getBucket());
					fo.setMime("directory");
					fo.setPath(bs.getBucket());
					fo.setTs(bs.getCdatatime());
					fo.setSize(bs.getUsedCapacity());
					fo.setWrite(true);
					fo.setRead(true);
					fileObjects.add(fo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		return fileObjects;
	}

	
	public List<FileObject> listDirs(SKey skey, String bucketName, String prefix)
			throws OperatorException {
		BCSCredentials credentials = new BCSCredentials(skey.getKeyId(),skey.getKeySecret());
		BaiduBCS baiduBCS = new BaiduBCS(credentials, Constants.BAIDU_BCS_URL);
		baiduBCS.setDefaultEncoding("UTF-8");
		List<FileObject> fileObjects = new ArrayList<FileObject>();
		ListObjectRequest lor = new ListObjectRequest(bucketName);
		if(StringUtil.isNotBlank(prefix))
			lor.setPrefix("/"+prefix+"/");
		lor.setListModel(2);
		try {
			 BaiduBCSResponse<ObjectListing> bcsRes = baiduBCS.listObject(lor);
			 if(bcsRes !=null){
				for(ObjectSummary bs:bcsRes.getResult().getObjectSummaries()){
					if(bs.isDir()){
						FileObject fo = new FileObject();
						fo.setBucketName(bucketName);
						fo.setHasDirs(true);
						fo.setMime("directory");
						String name = bs.getName();
						String ppath = bs.getParentDir();
						if("/".equals(ppath)){
							//fo.setParentPath("");
						}else{
							fo.setParentPath(ppath.substring(1,ppath.length()-1));
						}
						String[] names = name.split("/");
						fo.setName(names[names.length-1]);
						fo.setPath(name.substring(1,name.length()-1));
						fo.setTs(bs.getLastModifiedTime());
						fo.setSize(bs.getSize());
						fo.setWrite(true);
						fo.setRead(true);
						fileObjects.add(fo);
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		return fileObjects;
	}

	
	public List<FileObject> listObject(SKey skey, String bucketName,
			String prefix) throws OperatorException {
		BCSCredentials credentials = new BCSCredentials(skey.getKeyId(),skey.getKeySecret());
		BaiduBCS baiduBCS = new BaiduBCS(credentials, Constants.BAIDU_BCS_URL);
		baiduBCS.setDefaultEncoding("UTF-8");
		List<FileObject> fileObjects = new ArrayList<FileObject>();
		ListObjectRequest lor = new ListObjectRequest(bucketName);
		if(StringUtil.isNotBlank(prefix))
			lor.setPrefix("/"+prefix+"/");
		lor.setListModel(2);
		try {
			 BaiduBCSResponse<ObjectListing> bcsRes = baiduBCS.listObject(lor);
			 if(bcsRes !=null){
				for (ObjectSummary bs : bcsRes.getResult().getObjectSummaries()) {
					FileObject fo = new FileObject();
					fo.setBucketName(bucketName);
					String name = bs.getName();
					if (bs.isDir()) {
						fo.setHasDirs(true);
						fo.setMime("directory");
						fo.setRead(true);
						fo.setPath(name.substring(1, name.length() - 1));
					}else{
						//TODO
						fo.setMime("file");
						fo.setRead(false);
						fo.setPath(name.substring(1, name.length()));
					}
					String ppath = bs.getParentDir();
					if ("/".equals(ppath)) {
						// fo.setParentPath("");
					} else {
						fo.setParentPath(ppath.substring(1, ppath.length() - 1));
					}
					String[] names = name.split("/");
					fo.setName(names[names.length - 1]);
					fo.setTs(bs.getLastModifiedTime());
					fo.setSize(bs.getSize());
					fo.setWrite(true);
					fileObjects.add(fo);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		return fileObjects;
	}

	
	public List<FileObject> listAllObject(SKey skey, String bucketName,
			String prefix) throws OperatorException {
		BCSCredentials credentials = new BCSCredentials(skey.getKeyId(),skey.getKeySecret());
		BaiduBCS baiduBCS = new BaiduBCS(credentials, Constants.BAIDU_BCS_URL);
		baiduBCS.setDefaultEncoding("UTF-8");
		List<FileObject> fileObjects = new ArrayList<FileObject>();
		ListObjectRequest lor = new ListObjectRequest(bucketName);
		if(StringUtil.isNotBlank(prefix))
			lor.setPrefix("/"+prefix+"/");
		try {
			 BaiduBCSResponse<ObjectListing> bcsRes = baiduBCS.listObject(lor);
			 if(bcsRes !=null){
				for (ObjectSummary bs : bcsRes.getResult().getObjectSummaries()) {
					FileObject fo = new FileObject();
					fo.setBucketName(bucketName);
					String name = bs.getName();
					if (bs.isDir()) {
						fo.setHasDirs(true);
						fo.setMime("directory");
						fo.setRead(true);
						fo.setPath(name.substring(1, name.length() - 1));
					}else{
						//TODO
						fo.setMime("file");
						fo.setRead(false);
						fo.setPath(name.substring(1, name.length()));
					}
					String ppath = bs.getParentDir();
					if ("/".equals(ppath)) {
						// fo.setParentPath("");
					} else {
						fo.setParentPath(ppath.substring(1, ppath.length() - 1));
					}
					String[] names = name.split("/");
					fo.setName(names[names.length - 1]);
					
					fo.setTs(bs.getLastModifiedTime());
					fo.setSize(bs.getSize());
					fo.setWrite(true);
					fileObjects.add(fo);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		return fileObjects;
	}
	
	
}
