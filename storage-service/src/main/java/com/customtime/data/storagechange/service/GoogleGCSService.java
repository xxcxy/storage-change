package com.customtime.data.storagechange.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import com.customtime.data.storagechange.service.bean.FileObject;
import com.customtime.data.storagechange.service.bean.IntermedObject;
import com.customtime.data.storagechange.service.bean.MultiInfo;
import com.customtime.data.storagechange.service.bean.PartInfo;
import com.customtime.data.storagechange.service.bean.SKey;
import com.customtime.data.storagechange.service.exception.OperatorException;
import com.customtime.data.storagechange.service.util.DateUtil;
import com.customtime.data.storagechange.service.util.GoogAuthorizetionUtil;
import com.customtime.data.storagechange.service.util.IntermedObjectUtil;
import com.customtime.data.storagechange.service.util.StringUtil;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.Buckets;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;

@Service
public class GoogleGCSService implements CSService{
	private static final JsonFactory JSON_FACTORY = new GsonFactory();
	private static final long maxValue = 10000L;
	public static void main(String[] args){
		try {
			SKey key = new SKey();
			key.setKeyId("220505339768.apps.googleusercontent.com");
			key.setKeySecret("ZuFN71v1li1I9vGApUjeSJji");
			Storage storage = getStorage(key);
			storage.getRequestFactory().getInitializer();
//			Storage.Buckets.List listBuckets = storage.buckets().list("august-apogee-270").setMaxResults(maxValue);
//			listBuckets.setDisableGZipContent(true);
//			HttpHeaders hh = listBuckets.getRequestHeaders();
//			System.out.println(hh);
////			hh.setContentLength(9L);
////			listBuckets.setRequestHeaders(hh);
//			Buckets os = listBuckets.execute();
//			if(os!=null){
//				for(Bucket bucket:os.getItems()){
//					System.out.println(bucket.getName());
//				}
//			}
			Storage.Objects.List listObjects = storage.objects().list("gogcs2oss").setMaxResults(maxValue);
			listObjects.setPrefix("中文try/");
			listObjects.setDelimiter("/");
			Objects obj = listObjects.execute();
			if(obj!=null&&obj.getItems()!=null){
				for(StorageObject so:obj.getItems()){
					System.out.println(so.getMd5Hash());
					System.out.println(so.getEtag());
					System.out.println(so.getSize().longValue());
				}
			}
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Storage getStorage(SKey key) throws Exception {
		final Credential hil =  GoogAuthorizetionUtil.getFlow(key.getKeyId(),key.getKeySecret()).loadCredential(key.getKeyId());
		HttpRequestInitializer hri = new HttpRequestInitializer(){
			public void initialize(HttpRequest httpRequest) {
				try {
					hil.initialize(httpRequest);
				} catch (IOException e) {
					e.printStackTrace();
				}
	            httpRequest.setConnectTimeout(3*60*000);
	            httpRequest.setReadTimeout(3*60*000);
	          }
		};
		return new Storage.Builder(GoogleNetHttpTransport.newTrustedTransport(),JSON_FACTORY,hri).setApplicationName("storageChange").build();
	}

	
	public boolean isBucketExist(String bucketName, SKey key) {
		try{
			Storage storage = getStorage(key);
			Storage.Buckets.Get getBucket = storage.buckets().get(bucketName);
			getBucket.setProjection("full");
			Bucket bucket = getBucket.execute();
			if(bucket!=null && bucketName.equals(bucket.getName()))
				return true;
			return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	
	public void createBucket(String bucketName, SKey key)
			throws OperatorException {
		try{
			Storage storage = getStorage(key);
			Storage.Buckets.Insert insertBucket = storage.buckets().insert(key.getProject(), new Bucket().setName(bucketName).setLocation("US"));
			insertBucket.execute();
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
	}

	
	public IntermedObject getObject(String bucketName, String fileName, SKey key)
			throws OperatorException {
		try{
			Storage storage = getStorage(key);
		    Storage.Objects.Get getObject = storage.objects().get(bucketName,fileName);
		    return getIntermedObject(getObject);
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
	}

	
	public IntermedObject getObject(String bucketName, String fileName,
			PartInfo partInfo, SKey key) throws OperatorException {
		try{
			Storage storage = getStorage(key);
		    Storage.Objects.Get getObject = storage.objects().get(bucketName,fileName);
		    getObject.setRequestHeaders(new HttpHeaders().setRange(String.format("bytes=%d-%d",partInfo.getStart(),partInfo.getEnd())));
		    return getIntermedObject(getObject);
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
	}

	
	public MultiInfo getNeccessPart(String bucketName, String fileName,
			SKey key, long size) throws OperatorException {
		// TODO Auto-generated method stub
		throw new OperatorException("not Support multiUpload");
	}

	
	public boolean putPartObject(PartInfo partInfo,
			IntermedObject intermedObject, SKey key, MultiInfo mi)
			throws OperatorException {
		// TODO Auto-generated method stub
		throw new OperatorException("not Support multiUpload");
	}

	
	public boolean putObject(String bucketName, IntermedObject intermedObject,
			SKey key) throws OperatorException {
		try{
			Storage storage = getStorage(key);
			StorageObject objectMetadata = new StorageObject();
			objectMetadata.setBucket(bucketName);
			objectMetadata.setName(intermedObject.getObjectName());
			for(Entry<String,String> entry:intermedObject.getMetadata().entrySet()){
				if("Cache-Control".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue()))
					objectMetadata.setCacheControl(entry.getValue());
				if("Content-Disposition".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue()))
					objectMetadata.setContentDisposition(entry.getValue());
				if("Content-Encoding".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue()))
					objectMetadata.setContentEncoding(entry.getValue());
				if("Content-Language".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue()))
					objectMetadata.setContentLanguage(entry.getValue());
				if("Content-Type".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue()))
					objectMetadata.setContentType(entry.getValue());
				if("Content-Length".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue()))
					objectMetadata.setSize(BigInteger.valueOf(Long.parseLong(entry.getValue())));
				if("crc32".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue()))
					objectMetadata.setCrc32c(entry.getValue());
				if("Last-Modified".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue()))
					objectMetadata.setUpdated(new DateTime(IntermedObjectUtil.dsf.parse(entry.getValue())));
//				if("Expires".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue()))
//					objectMetadata.setTimeDeleted(new DateTime(IntermedObjectUtil.dsf.parse(entry.getValue())));
			}
			Storage.Objects.Insert insertObject = storage.objects().insert(bucketName,objectMetadata,new InputStreamContent("application/octet-stream",intermedObject.getContext()));
			insertObject.getMediaHttpUploader().setDirectUploadEnabled(true).setDisableGZipContent(true);
			StorageObject so = insertObject.execute();
			if(StringUtil.isNotBlank(so.getMd5Hash())&&StringUtil.base64ThexString(so.getMd5Hash()).equalsIgnoreCase(intermedObject.getMD5String()))
				return true;
			return false;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
	}

	
	public boolean completeMultipartUpload(SKey key, MultiInfo mi)
			throws OperatorException {
		// TODO Auto-generated method stub
		throw new OperatorException("not Support multiUpload");
	}

	
	public List<String> listObject(String bucketName, SKey key)
			throws OperatorException {
		try{
			List<String> files = new ArrayList<String>();
			Storage storage = getStorage(key);
			Storage.Objects.List listObjects = storage.objects().list(bucketName).setMaxResults(maxValue);
			Objects os = listObjects.execute();
			if(os!=null&&os.getItems()!=null){
				for(StorageObject obj:os.getItems()){
					files.add(obj.getName());
				}
			}
			return files;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
	}

	
	public long getFileSize(String bucketName, String fileName, SKey key) {
		try{
			Storage storage = getStorage(key);
		    Storage.Objects.Get getObject = storage.objects().get(bucketName,fileName);
		    StorageObject so = getObject.execute();
		    if(so!=null&&so.getSize()!=null)
		    	return so.getSize().longValue();
		}catch(Exception e){
			e.printStackTrace();
		}
	    return 0;
	}

	
	public List<FileObject> listBucket(SKey key) throws OperatorException {
		try{
			List<FileObject> files = new ArrayList<FileObject>();
			Storage storage = getStorage(key);
			Storage.Buckets.List listBuckets = storage.buckets().list(key.getProject()).setMaxResults(maxValue);
			Buckets os = listBuckets.execute();
			if(os!=null&&os.getItems()!=null){
				for(Bucket bucket:os.getItems()){
					FileObject fo = new FileObject();
					fo.setBucketName(bucket.getName());
					fo.setHasDirs(true);
					fo.setName(bucket.getName());
					fo.setMime("directory");
					fo.setPath(bucket.getName());
					fo.setTs(bucket.getTimeCreated().getValue());
					fo.setSize(0);
					fo.setWrite(true);
					fo.setRead(true);
					files.add(fo);
				}
			}
			return files;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
	}

	
	public List<FileObject> listDirs(SKey skey, String bucketName, String prefix)
			throws OperatorException {
		try{
			List<FileObject> files = new ArrayList<FileObject>();
			Storage storage = getStorage(skey);
			Storage.Objects.List listObjects = storage.objects().list(bucketName).setMaxResults(maxValue);
			if(StringUtil.isNotBlank(prefix))
				listObjects.setPrefix(prefix+"/");
			listObjects.setDelimiter("/");
			Objects os = listObjects.execute();
			if(os!=null&&os.getPrefixes()!=null){
				for(String pathName:os.getPrefixes()){
					FileObject fo = new FileObject();
					String filePath = pathName.substring(0,pathName.length()-1);
					fo.setBucketName(bucketName);
					fo.setHasDirs(true);
					fo.setName(filePath.substring(filePath.lastIndexOf("/")+1));
					if(filePath.contains("/"))
						fo.setParentPath(filePath.substring(0,filePath.lastIndexOf("/")));
					fo.setMime("directory");
					fo.setPath(filePath);
					fo.setTs(DateUtil.getNow().getTime());
					fo.setSize(0);
					fo.setWrite(true);
					fo.setRead(true);
					files.add(fo);
				}
			}
			return files;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
	}

	
	public List<FileObject> listObject(SKey skey, String bucketName,
			String prefix) throws OperatorException {
		try{
			List<FileObject> files = new ArrayList<FileObject>();
			Storage storage = getStorage(skey);
			Storage.Objects.List listObjects = storage.objects().list(bucketName).setMaxResults(maxValue);
			if(StringUtil.isNotBlank(prefix))
				listObjects.setPrefix(prefix+"/");
			listObjects.setDelimiter("/");
			Objects os = listObjects.execute();
			if(os!=null&&os.getItems()!=null){
				for(StorageObject obj:os.getItems()){
					FileObject fo = new FileObject();
					String filePath = obj.getName();
					fo.setMime("file");
					fo.setBucketName(bucketName);
					if(filePath.contains("/"))
						fo.setParentPath(filePath.substring(0,filePath.lastIndexOf("/")));
					fo.setName(filePath.substring(filePath.lastIndexOf("/")+1));
					fo.setPath(filePath);
					fo.setTs(obj.getUpdated().getValue());
					fo.setSize(obj.getSize().longValue());
					fo.setWrite(true);
					files.add(fo);
				}
			}
			if(os!=null&&os.getPrefixes()!=null){
				for(String pathName:os.getPrefixes()){
					FileObject fo = new FileObject();
					String filePath = pathName.substring(0,pathName.length()-1);
					fo.setBucketName(bucketName);
					fo.setHasDirs(true);
					fo.setName(filePath.substring(filePath.lastIndexOf("/")+1));
					if(filePath.contains("/"))
						fo.setParentPath(filePath.substring(0,filePath.lastIndexOf("/")));
					fo.setMime("directory");
					fo.setPath(filePath);
					fo.setTs(DateUtil.getNow().getTime());
					fo.setSize(0);
					fo.setWrite(true);
					fo.setRead(true);
					files.add(fo);
				}
			}
			return files;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
	}

	
	public List<FileObject> listAllObject(SKey skey, String bucketName,
			String prefix) throws OperatorException {
		try{
			List<FileObject> files = new ArrayList<FileObject>();
			Storage storage = getStorage(skey);
			Storage.Objects.List listObjects = storage.objects().list(bucketName).setMaxResults(maxValue);
			if(StringUtil.isNotBlank(prefix))
				listObjects.setPrefix(prefix+"/");
			Objects os = listObjects.execute();
			if(os!=null&&os.getItems()!=null){
				for(StorageObject obj:os.getItems()){
					FileObject fo = new FileObject();
					String filePath = obj.getName();
					if(filePath.endsWith("/")){
						fo.setHasDirs(true);
						fo.setMime("directory");
						fo.setRead(true);
						filePath = filePath.substring(0,filePath.length()-1);
					}else{
						fo.setMime("file");
					}
					fo.setBucketName(bucketName);
					if(filePath.contains("/"))
						fo.setParentPath(filePath.substring(0,filePath.lastIndexOf("/")));
					fo.setName(filePath.substring(filePath.lastIndexOf("/")+1));
					fo.setPath(filePath);
					fo.setTs(obj.getUpdated().getValue());
					fo.setSize(obj.getSize().longValue());
					fo.setWrite(true);
					files.add(fo);
				}
			}
			return files;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
	}
	
	private IntermedObject getIntermedObject(Storage.Objects.Get getObject) throws IOException{
		StorageObject so = getObject.execute();
		InputStream content = getObject.executeMediaAsInputStream();
		try{
		if (so != null && content != null) {
			IntermedObject intermedObject = new IntermedObject();
			intermedObject.setContext(IntermedObjectUtil.getInputStreanm(content));
			intermedObject.setMD5String(StringUtil.base64ThexString(so.getMd5Hash()));
			intermedObject.setObjectName(so.getName());
			if (StringUtil.isNotBlank(so.getCacheControl()))
				intermedObject.setMetadata("Cache-Control", so.getCacheControl());
			if (StringUtil.isNotBlank(so.getContentDisposition()))
				intermedObject.setMetadata("Content-Disposition", so.getContentDisposition());
			if (StringUtil.isNotBlank(so.getContentEncoding()))
				intermedObject.setMetadata("Content-Encoding", so.getContentEncoding());
			if (StringUtil.isNotBlank(so.getContentLanguage()))
				intermedObject.setMetadata("Content-Language", so.getContentLanguage());
			if (StringUtil.isNotBlank(so.getContentType()))
				intermedObject.setMetadata("Content-Type", so.getContentType());
			if (StringUtil.isNotBlank(so.getCrc32c()))
				intermedObject.setMetadata("crc32", so.getCrc32c());
			if (StringUtil.isNotBlank(so.getEtag()))
				intermedObject.setMetadata("Etag", so.getEtag());
			if (so.getSize() != null)
				intermedObject.setMetadata("Content-Length", so.getSize().toString());
			if (so.getUpdated() != null)
				intermedObject.setMetadata("Last-Modified", IntermedObjectUtil.dsf
						.format(new Date(so.getUpdated().getValue())));
			if(so.getTimeDeleted()!=null)
				intermedObject.setMetadata("Expires", IntermedObjectUtil.dsf
						.format(new Date(so.getUpdated().getValue())));
			if (so.getMetadata() != null) {
				for (Entry<String, String> entry : so.getMetadata().entrySet())
					intermedObject
							.setMetadata(entry.getKey(), entry.getValue());
			}
			return intermedObject;
		}
		}finally{
			content.close();
		}
		return null;
	}
}
