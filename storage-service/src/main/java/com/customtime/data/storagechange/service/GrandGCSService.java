package com.customtime.data.storagechange.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import com.customtime.data.storagechange.service.bean.FileObject;
import com.customtime.data.storagechange.service.bean.IntermedObject;
import com.customtime.data.storagechange.service.bean.MultiInfo;
import com.customtime.data.storagechange.service.bean.PartInfo;
import com.customtime.data.storagechange.service.bean.SKey;
import com.customtime.data.storagechange.service.exception.OperatorException;
import com.customtime.data.storagechange.service.util.Constants;
import com.customtime.data.storagechange.service.util.DateUtil;
import com.customtime.data.storagechange.service.util.IntermedObjectUtil;
import com.customtime.data.storagechange.service.util.ListUtil;
import com.customtime.data.storagechange.service.util.StringUtil;
import com.snda.storage.SNDAObject;
import com.snda.storage.SNDAObjectMetadata;
import com.snda.storage.SNDAStorage;
import com.snda.storage.SNDAStorageBuilder;
import com.snda.storage.core.UploadObjectResult;
import com.snda.storage.core.UploadPartResult;
import com.snda.storage.fluent.PutObject;
import com.snda.storage.xml.BucketSummary;
import com.snda.storage.xml.CommonPrefix;
import com.snda.storage.xml.CompleteMultipartUploadResult;
import com.snda.storage.xml.InitiateMultipartUploadResult;
import com.snda.storage.xml.ListBucketResult;
import com.snda.storage.xml.ListMultipartUploadsResult;
import com.snda.storage.xml.ListPartsResult;
import com.snda.storage.xml.ObjectSummary;
import com.snda.storage.xml.Part;
import com.snda.storage.xml.PartSummary;
import com.snda.storage.xml.UploadSummary;

@Service
public class GrandGCSService implements CSService{
	
	public static void main(String[] args){
		SNDAStorage storage = null;
		try{
			storage = new SNDAStorageBuilder().credential("2CX9QMP0K0HS1AO42PYMHXSEG","Y2Q2NTc2NDAtZTgzMy00ODFhLTkxODUtNmFhNThmN2U5NGE2").connectionTimeout(10 * 1000).soTimeout(30 * 1000).build();
			SNDAObject object = storage.bucket("gcs2oss").object("中文try/fdsashuai.jpg").download();
			 SNDAObjectMetadata sm = object.getObjectMetadata();
			 System.out.println(sm.toString());
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(storage!=null)
				storage.destory();
		}
	}
	
	public boolean isBucketExist(String bucketName, SKey key) {
		SNDAStorage storage = null;
		try{
			storage = new SNDAStorageBuilder().credential(key.getKeyId(),key.getKeySecret()).connectionTimeout(10 * 1000).soTimeout(30 * 1000).build();
			return storage.bucket(bucketName).exist();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(storage!=null)
				storage.destory();
		}
		return false;
	}

	
	public void createBucket(String bucketName, SKey key)
			throws OperatorException {
		SNDAStorage storage = null;
		try{
			storage = new SNDAStorageBuilder().credential(key.getKeyId(),key.getKeySecret()).connectionTimeout(10 * 1000).soTimeout(30 * 1000).build();
			storage.bucket(bucketName).create();
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(storage!=null)
				storage.destory();
		}
	}

	
	public IntermedObject getObject(String bucketName, String fileName, SKey key)
			throws OperatorException {
		SNDAStorage storage = null;
		try{
			storage = new SNDAStorageBuilder().credential(key.getKeyId(),key.getKeySecret()).connectionTimeout(10 * 1000).soTimeout(30 * 1000).build();
			SNDAObject object = storage.bucket(bucketName).object(fileName).download();
			if(object!=null)
				return IntermedObjectUtil.convertIntermedObject(object);
			else 
				return null;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(storage!=null)
				storage.destory();
		}
	}

	
	public IntermedObject getObject(String bucketName, String fileName,
			PartInfo partInfo, SKey key) throws OperatorException {
		SNDAStorage storage = null;
		try{
			storage = new SNDAStorageBuilder().credential(key.getKeyId(),key.getKeySecret()).connectionTimeout(10 * 1000).soTimeout(30 * 1000).build();
			SNDAObject object = storage.bucket(bucketName).object(fileName).range(partInfo.getStart(),partInfo.getEnd()).download();
			if(object!=null)
				return IntermedObjectUtil.convertIntermedObject(object);
			else 
				return null;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(storage!=null)
				storage.destory();
		}
	}

	
	public MultiInfo getNeccessPart(String bucketName, String fileName,
			SKey key, long size) throws OperatorException {
		SNDAStorage storage = null;
		try{
			long lastSize = size%Constants.FILE_PART_SIZE;
			long partCount = size/Constants.FILE_PART_SIZE +1;
			storage = new SNDAStorageBuilder().credential(key.getKeyId(),key.getKeySecret()).connectionTimeout(10 * 1000).soTimeout(30 * 1000).build();
			ListMultipartUploadsResult lmur = storage.bucket(bucketName).listMultipartUploads();
			if(lmur!=null){
				for(UploadSummary us:lmur.getUploadSummaries()){
					if(us.getKey().equals(fileName)){
						MultiInfo mi = new MultiInfo(bucketName,fileName);
				        mi.setKey(us.getUploadId());
				        ListPartsResult lpr = storage.bucket(bucketName).object(fileName).multipartUpload(us.getUploadId()).listParts();
				        List<Integer> has = new ArrayList<Integer>(); 
				        if(lpr!=null&&!ListUtil.isEmpty(lpr.getPartSummaries())){
				        	for(PartSummary ps:lpr.getPartSummaries()){
				        		has.add(ps.getPartNumber());
				        		mi.addAL(ps.getEntityTag(),new PartInfo(ps.getPartNumber()*Constants.FILE_PART_SIZE,ps.getSize()));
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
				        return mi;
					}
				}
			}
			InitiateMultipartUploadResult result = storage.bucket(bucketName).object(fileName).initiateMultipartUpload();
			MultiInfo mi = new MultiInfo(bucketName,fileName);
	        mi.setKey(result.getUploadId());
	        for(Integer i=0;i<partCount;i++){
	        	if(i==partCount-1)
					mi.addNP(new PartInfo(i*Constants.FILE_PART_SIZE,lastSize));
				else
					mi.addNP(new PartInfo(i*Constants.FILE_PART_SIZE,Constants.FILE_PART_SIZE));
	        }
			return mi;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(storage!=null)
				storage.destory();
		}
	}

	
	public boolean putPartObject(PartInfo partInfo,
			IntermedObject intermedObject, SKey key, MultiInfo mi)
			throws OperatorException {
		SNDAStorage storage = null;
		try{
			storage = new SNDAStorageBuilder().credential(key.getKeyId(),key.getKeySecret()).connectionTimeout(10 * 1000).soTimeout(30 * 1000).build();
			Long pn = partInfo.getStart()/Constants.FILE_PART_SIZE;
			UploadPartResult ur = storage.bucket(mi.getBucketName()).object(mi.getFileName()).multipartUpload(mi.getKey()).partNumber(pn.intValue()).entity(partInfo.getSize(), intermedObject.getContext()).upload();
			PartInfo pi = mi.getPartInfo(partInfo.getStart());
			if(pi!=null&&ur!=null&&ur.getETag()!=null){
				mi.remove(pi);
				mi.addAL(ur.getETag(),pi);
				if(ur.getETag().equalsIgnoreCase(intermedObject.getMD5String())){
					return true;
				}
			}
			return false;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(storage!=null)
				storage.destory();
		}
	}

	
	public boolean putObject(String bucketName, IntermedObject intermedObject,
			SKey key) throws OperatorException {
		SNDAStorage storage = null;
		try{
			storage = new SNDAStorageBuilder().credential(key.getKeyId(),key.getKeySecret()).connectionTimeout(10 * 1000).soTimeout(30 * 1000).build();
			Map<String,String> metadata = intermedObject.getMetadata();
			PutObject uo = storage.bucket(bucketName).object(intermedObject.getObjectName()).contentType(metadata.get("Content-Type"));
			uo = uo.contentDisposition(metadata.get("Content-Disposition")).contentEncoding(metadata.get("Content-Encoding")).expires(metadata.get("Expires"));
			for(Entry<String,String> e:metadata.entrySet()){
				String vk = e.getKey();
				if(vk.startsWith("inter-med-")){
					uo = uo.metadata(vk.replace("inter-med-", "x-snda-meta-"), e.getValue());
				}
			}
			long siez = Long.parseLong(metadata.get("Content-Length"));
			UploadObjectResult ur = uo.entity(siez, intermedObject.getContext()).upload();
			if(ur!=null && intermedObject.getMD5String().equalsIgnoreCase(ur.getETag().replace("\"","")))
				return true;
			else
				return false;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(storage!=null)
				storage.destory();
		}
	}

	
	public boolean completeMultipartUpload(SKey key, MultiInfo mi)
			throws OperatorException {
		if(!mi.getnPartInfos().isEmpty())
			return false;
		SNDAStorage storage = null;
		try{
			storage = new SNDAStorageBuilder().credential(key.getKeyId(),key.getKeySecret()).connectionTimeout(10 * 1000).soTimeout(30 * 1000).build();
			List<Part> lp = new ArrayList<Part>();
			for(Entry<String,PartInfo> entry:mi.getAlPartInfos().entrySet()){
				Long i = entry.getValue().getStart()/Constants.FILE_PART_SIZE;
				lp.add(new Part(i.intValue(),entry.getKey()));
			}
			CompleteMultipartUploadResult cmur = storage.bucket(mi.getBucketName()).object(mi.getFileName()).multipartUpload(mi.getKey()).parts(lp).complete();
			if(cmur!=null)
				return true;
			else
				return false;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(storage!=null)
				storage.destory();
		}
	}

	
	public List<String> listObject(String bucketName, SKey key)
			throws OperatorException {
		SNDAStorage storage = null;
		List<String> fileNames = new ArrayList<String>();
		try{
			storage = new SNDAStorageBuilder().credential(key.getKeyId(),key.getKeySecret()).connectionTimeout(10 * 1000).soTimeout(30 * 1000).build();
			ListBucketResult lbr = storage.bucket(bucketName).listObjects();
			if(lbr!=null){
				for(ObjectSummary os:lbr.getObjectSummaries()){
					fileNames.add(os.getKey());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(storage!=null)
				storage.destory();
		}
		return fileNames;
	}

	
	public long getFileSize(String bucketName, String fileName, SKey key) {
		SNDAStorage storage = null;
		try{
			storage = new SNDAStorageBuilder().credential(key.getKeyId(),key.getKeySecret()).connectionTimeout(10 * 1000).soTimeout(30 * 1000).build();
			SNDAObjectMetadata metadata = storage.bucket(bucketName).object(fileName).head();
			if(metadata!=null)
				return metadata.getContentLength();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(storage!=null)
				storage.destory();
		}
		return 0;
	}

	
	public List<FileObject> listBucket(SKey key) throws OperatorException {
		SNDAStorage storage = null;
		List<FileObject> fileObjects = new ArrayList<FileObject>();
		try{
			storage = new SNDAStorageBuilder().credential(key.getKeyId(),key.getKeySecret()).connectionTimeout(10 * 1000).soTimeout(30 * 1000).build();
			for (BucketSummary each : storage.listBuckets()) {
				FileObject fo = new FileObject();
				fo.setBucketName(each.getName());
				fo.setHasDirs(true);
				fo.setName(each.getName());
				fo.setMime("directory");
				fo.setPath(each.getName());
				fo.setTs(each.getCreation().getMillis());
				fo.setSize(0);
				fo.setWrite(true);
				fo.setRead(true);
				fileObjects.add(fo);
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(storage!=null)
				storage.destory();
		}
		return fileObjects;
	}

	
	public List<FileObject> listDirs(SKey skey, String bucketName, String prefix)
			throws OperatorException {
		List<FileObject> fileObjects = new ArrayList<FileObject>();
		SNDAStorage storage = null;
		try{
			storage = new SNDAStorageBuilder().credential(skey.getKeyId(),skey.getKeySecret()).connectionTimeout(10 * 1000).soTimeout(30 * 1000).build();
			ListBucketResult lbr = null;
			if(StringUtil.isNotBlank(prefix))
				lbr = storage.bucket(bucketName).prefix(prefix+"/").delimiter("/").listObjects();
			else
				lbr = storage.bucket(bucketName).prefix("").delimiter("/").listObjects();
			if(lbr!=null){
				for(CommonPrefix dir:lbr.getCommonPrefixes()){
					FileObject fo = new FileObject();
					fo.setBucketName(bucketName);
					String filePath = dir.getPrefix().substring(0,dir.getPrefix().length()-1);
					fo.setName(filePath.substring(filePath.lastIndexOf("/")+1));
					if(filePath.contains("/"))
						fo.setParentPath(filePath.substring(0,filePath.lastIndexOf("/")));
					fo.setMime("directory");
					fo.setPath(filePath);
					fo.setTs(DateUtil.getNow().getTime());
					fo.setSize(0);
					fo.setWrite(true);
					fo.setRead(true);
					fileObjects.add(fo);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			storage.destory();
		}
		return fileObjects;
	}

	
	public List<FileObject> listObject(SKey skey, String bucketName,
			String prefix) throws OperatorException {
		List<FileObject> fileObjects = new ArrayList<FileObject>();
		SNDAStorage storage = null;
		try{
			storage = new SNDAStorageBuilder().credential(skey.getKeyId(),skey.getKeySecret()).connectionTimeout(10 * 1000).soTimeout(30 * 1000).build();
			ListBucketResult lbr = null;
			if(StringUtil.isNotBlank(prefix))
				lbr = storage.bucket(bucketName).prefix(prefix+"/").delimiter("/").listObjects();
			else
				lbr = storage.bucket(bucketName).prefix("").delimiter("/").listObjects();
			if(lbr!=null){
				for(ObjectSummary os:lbr.getObjectSummaries()){
					String filePath = os.getKey();
					if(!filePath.endsWith("/")){
						FileObject fo = new FileObject();
						fo.setBucketName(bucketName);
						fo.setName(filePath.substring(filePath.lastIndexOf("/")+1));
						if(filePath.contains("/"))
							fo.setParentPath(filePath.substring(0,filePath.lastIndexOf("/")));
						fo.setMime("file");
						fo.setPath(filePath);
						fo.setTs(os.getLastModified().getMillis());
						fo.setSize(os.getSize());
						fo.setWrite(true);
						fo.setRead(false);
						fileObjects.add(fo);
					}else{
						FileObject fo = new FileObject();
						fo.setBucketName(bucketName);
						filePath = filePath.substring(0,filePath.length()-1);
						fo.setName(filePath.substring(filePath.lastIndexOf("/")+1));
						if(filePath.contains("/"))
							fo.setParentPath(filePath.substring(0,filePath.lastIndexOf("/")));
						fo.setMime("directory");
						fo.setPath(filePath);
						fo.setTs(os.getLastModified().getMillis());
						fo.setSize(os.getSize());
						fo.setWrite(true);
						fo.setRead(true);
						fileObjects.add(fo);
					}
				}
				for(CommonPrefix dir:lbr.getCommonPrefixes()){
					FileObject fo = new FileObject();
					fo.setBucketName(bucketName);
					String filePath = dir.getPrefix().substring(0,dir.getPrefix().length()-1);
					fo.setName(filePath.substring(filePath.lastIndexOf("/")+1));
					if(filePath.contains("/"))
						fo.setParentPath(filePath.substring(0,filePath.lastIndexOf("/")));
					fo.setMime("directory");
					fo.setPath(filePath);
					fo.setTs(DateUtil.getNow().getTime());
					fo.setSize(0);
					fo.setWrite(true);
					fo.setRead(true);
					fileObjects.add(fo);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(storage!=null)
				storage.destory();
		}
		return fileObjects;
	}

	
	public List<FileObject> listAllObject(SKey skey, String bucketName,
			String prefix) throws OperatorException {
		SNDAStorage storage = null;
		List<FileObject> fileObjects = new ArrayList<FileObject>();
		try{
			storage = new SNDAStorageBuilder().credential(skey.getKeyId(),skey.getKeySecret()).connectionTimeout(10 * 1000).soTimeout(30 * 1000).build();
			ListBucketResult lbr = null;
			if(StringUtil.isNotBlank(prefix))
				lbr = storage.bucket(bucketName).prefix(prefix+"/").listObjects();
			else
				lbr = storage.bucket(bucketName).listObjects();
			if(lbr!=null){
				for(ObjectSummary os:lbr.getObjectSummaries()){
					String filePath = os.getKey();
					if(filePath.endsWith("/")){
						FileObject fo = new FileObject();
						filePath = filePath.substring(0,filePath.length()-1);
						fo.setBucketName(bucketName);
						fo.setHasDirs(true);
						fo.setName(filePath.substring(filePath.lastIndexOf("/")+1));
						if(filePath.contains("/"))
							fo.setParentPath(filePath.substring(0,filePath.lastIndexOf("/")));
						fo.setMime("directory");
						fo.setPath(filePath);
						fo.setTs(os.getLastModified().getMillis());
						fo.setSize(os.getSize());
						fo.setWrite(true);
						fo.setRead(true);
						fileObjects.add(fo);
					}else{
						FileObject fo = new FileObject();
						fo.setBucketName(bucketName);
						fo.setHasDirs(false);
						fo.setName(filePath.substring(filePath.lastIndexOf("/")+1));
						if(filePath.contains("/"))
							fo.setParentPath(filePath.substring(0,filePath.lastIndexOf("/")));
						fo.setMime("file");
						fo.setPath(filePath);
						fo.setTs(os.getLastModified().getMillis());
						fo.setSize(os.getSize());
						fo.setWrite(true);
						fo.setRead(false);
						fileObjects.add(fo);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(storage!=null)
				storage.destory();
		}
		return fileObjects;
	}
}
