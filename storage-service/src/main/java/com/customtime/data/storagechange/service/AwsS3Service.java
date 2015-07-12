package com.customtime.data.storagechange.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jclouds.ContextBuilder;
import org.jclouds.aws.s3.AWSS3Client;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder.PayloadBlobBuilder;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.MutableBlobMetadata;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.options.GetOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.io.MutableContentMetadata;
import org.jclouds.s3.S3ApiMetadata;
import org.springframework.stereotype.Service;

import com.customtime.data.storagechange.service.bean.FileObject;
import com.customtime.data.storagechange.service.bean.IntermedObject;
import com.customtime.data.storagechange.service.bean.MultiInfo;
import com.customtime.data.storagechange.service.bean.PartInfo;
import com.customtime.data.storagechange.service.bean.SKey;
import com.customtime.data.storagechange.service.exception.OperatorException;
import com.customtime.data.storagechange.service.util.DateUtil;
import com.customtime.data.storagechange.service.util.IntermedObjectUtil;
import com.customtime.data.storagechange.service.util.StringUtil;
import com.google.common.io.Closeables;

@Service
public class AwsS3Service implements CSService{

	@SuppressWarnings("deprecation")
	public static void main(String[] args){
		BlobStoreContext context = ContextBuilder.newBuilder("aws-s3").credentials("AKIAIJLKEH52JHYGGO7A","Wx6Kjzdu7Dm+oR5vc5wyF93HFibS8YxAtdiqLFEa").buildView(BlobStoreContext.class);
		AWSS3Client ac = (AWSS3Client)context.unwrap(S3ApiMetadata.CONTEXT_TOKEN).getApi();
		boolean be = ac.bucketExists("oss2s3");
		ac.abortMultipartUpload("ss","ss","ss");
		System.out.println(be);
//		BlobStore bs = context.getBlobStore();
//		ListContainerOptions listcontaineroptions = ListContainerOptions.Builder.maxResults(Integer.MAX_VALUE);
//		listcontaineroptions.inDirectory("中文try");
//		PageSet<? extends StorageMetadata> page = bs.list("oss2s3", listcontaineroptions);
//		for(StorageMetadata each : page) {
//			System.out.println(each.toString());
//			if(each instanceof BlobMetadata)
//				System.out.println(((BlobMetadata)each).getContentMetadata().getContentLength());
//		}
		context.close();
	}
	
	public boolean isBucketExist(String bucketName, SKey key) {
		BlobStoreContext context = null;
		try{
			context = ContextBuilder.newBuilder("s3").credentials(key.getKeyId(),key.getKeySecret()).buildView(BlobStoreContext.class);
			BlobStore bs = context.getBlobStore();
			return bs.containerExists(bucketName);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(context!=null)
				context.close();
		}
		return false;
	}

	public void createBucket(String bucketName, SKey key)
			throws OperatorException {
		BlobStoreContext context = null;
		try{
			context = ContextBuilder.newBuilder("s3").credentials(key.getKeyId(),key.getKeySecret()).buildView(BlobStoreContext.class);
			BlobStore bs = context.getBlobStore();
			bs.createContainerInLocation(null,bucketName);
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(context!=null)
				context.close();
		}
	}

	public IntermedObject getObject(String bucketName, String fileName, SKey key)
			throws OperatorException {
		BlobStoreContext context = null;
		try{
			context = ContextBuilder.newBuilder("s3").credentials(key.getKeyId(),key.getKeySecret()).buildView(BlobStoreContext.class);
			BlobStore bs = context.getBlobStore();
			Blob bb = bs.getBlob(bucketName, fileName);
			if(bb!=null){
				return trancefer(bb);
			}
			return null;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(context!=null)
				context.close();
		}
	}

	public IntermedObject getObject(String bucketName, String fileName,
			PartInfo partInfo, SKey key) throws OperatorException {
		BlobStoreContext context = null;
		try{
			context = ContextBuilder.newBuilder("s3").credentials(key.getKeyId(),key.getKeySecret()).buildView(BlobStoreContext.class);
			GetOptions go = GetOptions.Builder.range(partInfo.getStart(),partInfo.getEnd());
			BlobStore bs = context.getBlobStore();
			Blob bb = bs.getBlob(bucketName, fileName,go);
			if(bb!=null){
				return trancefer(bb);
			}
			return null;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(context!=null)
				context.close();
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
		BlobStoreContext context = null;
		try{
			context = ContextBuilder.newBuilder("s3").credentials(key.getKeyId(),key.getKeySecret()).buildView(BlobStoreContext.class);
			BlobStore bs = context.getBlobStore();
			PayloadBlobBuilder pbb =bs.blobBuilder(intermedObject.getObjectName()).payload(intermedObject.getContext());
			Map<String,String> userData = new HashMap<String,String>();
			for(Entry<String,String> entry:intermedObject.getMetadata().entrySet()){
				if("Content-Disposition".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue())){
					pbb.contentDisposition(entry.getValue());
				}else if("Content-Encoding".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue())){
					pbb.contentEncoding(entry.getValue());
				}else if("Content-Language".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue())){
					pbb.contentLanguage(entry.getValue());
				}else if("Content-Length".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue())){
					pbb.contentLength(Long.parseLong(entry.getValue()));
				}else if("Content-Type".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue())){
					pbb.contentType(entry.getValue());
				}else if("Expires".equals(entry.getKey())&&StringUtil.isNotBlank(entry.getValue())){
					pbb.expires(IntermedObjectUtil.dsf.parse(entry.getValue()));
				}else{
					userData.put(entry.getKey(), entry.getValue());
				}
			}
			pbb.userMetadata(userData);
			String md5 = bs.putBlob(bucketName, pbb.build());
			if(StringUtil.isNotBlank(md5)&&md5.replace("\"","").equalsIgnoreCase(intermedObject.getMD5String()))
				return true;
			else
				return false;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(context!=null)
				context.close();
		}
	}

	
	public boolean completeMultipartUpload(SKey key, MultiInfo mi)
			throws OperatorException {
		// TODO Auto-generated method stub
		throw new OperatorException("not Support multiUpload");
	}

	
	public List<String> listObject(String bucketName, SKey key)
			throws OperatorException {
		List<String> fileObjects = new ArrayList<String>();
		BlobStoreContext context = null;
		try{
			context = ContextBuilder.newBuilder("s3").credentials(key.getKeyId(),key.getKeySecret()).buildView(BlobStoreContext.class);
			for(StorageMetadata each : context.getBlobStore().list(bucketName)) {
				fileObjects.add(each.getName());
			}
			return fileObjects;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(context!=null)
				context.close();
		}
	}

	
	public long getFileSize(String bucketName, String fileName, SKey key) {
		BlobStoreContext context = null;
		try{
			context = ContextBuilder.newBuilder("s3").credentials(key.getKeyId(),key.getKeySecret()).buildView(BlobStoreContext.class);
			BlobMetadata bmd = context.getBlobStore().blobMetadata(bucketName, fileName);
			if(bmd!=null)
				return bmd.getContentMetadata().getContentLength();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(context!=null)
				context.close();
		}
		return 0;
	}

	
	public List<FileObject> listBucket(SKey key) throws OperatorException {
		List<FileObject> fileObjects = new ArrayList<FileObject>();
		BlobStoreContext context = null;
		try{
			context = ContextBuilder.newBuilder("s3").credentials(key.getKeyId(),key.getKeySecret()).buildView(BlobStoreContext.class);
			for(StorageMetadata each : context.getBlobStore().list()) {
				FileObject fo = new FileObject();
				fo.setBucketName(each.getName());
				fo.setHasDirs(true);
				fo.setName(each.getName());
				fo.setMime("directory");
				fo.setPath(each.getName());
				fo.setTs(getTs(each.getCreationDate()));
				fo.setSize(0);
				fo.setWrite(true);
				fo.setRead(true);
				fileObjects.add(fo);
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(context!=null)
				context.close();
		}
		return fileObjects;
	}

	
	public List<FileObject> listDirs(SKey skey, String bucketName, String prefix)
			throws OperatorException {
		List<FileObject> fileObjects = new ArrayList<FileObject>();
		BlobStoreContext context = null;
		try{
			context = ContextBuilder.newBuilder("s3").credentials(skey.getKeyId(),skey.getKeySecret()).buildView(BlobStoreContext.class);
			BlobStore bs = context.getBlobStore();
			ListContainerOptions listcontaineroptions = ListContainerOptions.Builder.maxResults(Integer.MAX_VALUE);
			if(StringUtil.isNotBlank(prefix))
				listcontaineroptions.inDirectory(prefix);
			for(StorageMetadata each : bs.list(bucketName, listcontaineroptions)) {
				String filePath = each.getName();
				if(each.getType()==StorageType.RELATIVE_PATH||each.getType()==StorageType.FOLDER){
					FileObject fo = new FileObject();
					fo.setHasDirs(true);
					fo.setMime("directory");
					fo.setRead(true);
					fo.setBucketName(bucketName);
					if(filePath.contains("/"))
						fo.setParentPath(filePath.substring(0,filePath.lastIndexOf("/")));
					fo.setName(filePath.substring(filePath.lastIndexOf("/")+1));
					fo.setPath(filePath);
					fo.setTs(getTs(each.getCreationDate()));
					fo.setSize(0);
					fo.setWrite(true);
					fileObjects.add(fo);
				}
			}
			return fileObjects;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(context!=null)
				context.close();
		}
	}

	
	public List<FileObject> listObject(SKey skey, String bucketName,
			String prefix) throws OperatorException {
		List<FileObject> fileObjects = new ArrayList<FileObject>();
		BlobStoreContext context = null;
		try{
			context = ContextBuilder.newBuilder("s3").credentials(skey.getKeyId(),skey.getKeySecret()).buildView(BlobStoreContext.class);
			BlobStore bs = context.getBlobStore();
			ListContainerOptions listcontaineroptions = ListContainerOptions.Builder.maxResults(Integer.MAX_VALUE);
			if(StringUtil.isNotBlank(prefix))
				listcontaineroptions.inDirectory(prefix);
			for(StorageMetadata each : bs.list(bucketName, listcontaineroptions)) {
				String filePath = each.getName();
				//long size = each.get
				if(each.getType()==StorageType.RELATIVE_PATH||each.getType()==StorageType.FOLDER){
					FileObject fo = new FileObject();
					fo.setMime("directory");
					fo.setBucketName(bucketName);
					if(filePath.contains("/"))
						fo.setParentPath(filePath.substring(0,filePath.lastIndexOf("/")));
					fo.setName(filePath.substring(filePath.lastIndexOf("/")+1));
					fo.setPath(filePath);
					fo.setTs(getTs(each.getCreationDate()));
					fo.setSize(0);
					fo.setWrite(true);
					fo.setRead(true);
					fileObjects.add(fo);
				}else if(each.getType()==StorageType.BLOB){
					FileObject fo = new FileObject();
					fo.setMime("file");
					fo.setBucketName(bucketName);
					if(filePath.contains("/"))
						fo.setParentPath(filePath.substring(0,filePath.lastIndexOf("/")));
					fo.setName(filePath.substring(filePath.lastIndexOf("/")+1));
					fo.setPath(filePath);
					fo.setTs(getTs(each.getCreationDate()));
					if(each instanceof BlobMetadata)
						fo.setSize(((BlobMetadata)each).getContentMetadata().getContentLength());
					else
						fo.setSize(0);
					fo.setWrite(true);
					fileObjects.add(fo);
				}
			}
			return fileObjects;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(context!=null)
				context.close();
		}
	}

	
	public List<FileObject> listAllObject(SKey skey, String bucketName,
			String prefix) throws OperatorException {
		List<FileObject> fileObjects = new ArrayList<FileObject>();
		BlobStoreContext context = null;
		try{
			context = ContextBuilder.newBuilder("s3").credentials(skey.getKeyId(),skey.getKeySecret()).buildView(BlobStoreContext.class);
			BlobStore bs = context.getBlobStore();
			ListContainerOptions listcontaineroptions = ListContainerOptions.Builder.withDetails().recursive();
			if(StringUtil.isNotBlank(prefix))
				listcontaineroptions.inDirectory(prefix);
			for(StorageMetadata each : bs.list(bucketName, listcontaineroptions)) {
				FileObject fo = new FileObject();
				String filePath = each.getName();
				if(each.getType()==StorageType.RELATIVE_PATH||each.getType()==StorageType.FOLDER){
					fo.setHasDirs(true);
					fo.setMime("directory");
					fo.setRead(true);
				}else{
					fo.setMime("file");
				}
				fo.setBucketName(bucketName);
				if(filePath.contains("/"))
					fo.setParentPath(filePath.substring(0,filePath.lastIndexOf("/")));
				fo.setName(filePath.substring(filePath.lastIndexOf("/")+1));
				fo.setPath(filePath);
				fo.setTs(getTs(each.getCreationDate()));
				if(each instanceof BlobMetadata)
					fo.setSize(((BlobMetadata)each).getContentMetadata().getContentLength());
				else
					fo.setSize(0);
				fo.setWrite(true);
				fileObjects.add(fo);
			}
			return fileObjects;
		}catch(Exception e){
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}finally{
			if(context!=null)
				context.close();
		}
	}
	
	@SuppressWarnings("deprecation")
	private IntermedObject trancefer(Blob bb){
		IntermedObject intermedObject = new IntermedObject();
		MutableBlobMetadata mbm = bb.getMetadata();
		intermedObject.setMD5String(mbm.getETag().replace("\"",""));
		intermedObject.setObjectName(mbm.getName());
		MutableContentMetadata mcm = mbm.getContentMetadata();
		if(StringUtil.isNotBlank(mcm.getContentDisposition()))
			intermedObject.setMetadata("Content-Disposition",mcm.getContentDisposition());
		if(StringUtil.isNotBlank(mcm.getContentEncoding()))
			intermedObject.setMetadata("Content-Encoding",mcm.getContentEncoding());
		if(StringUtil.isNotBlank(mcm.getContentLanguage()))
			intermedObject.setMetadata("Content-Language",mcm.getContentLanguage());
		if(StringUtil.isNotBlank(mcm.getContentType()))
			intermedObject.setMetadata("Content-Type",mcm.getContentType());
		intermedObject.setMetadata("Content-Length",Long.toString(mcm.getContentLength()));
		if(mcm.getExpires()!=null)
			intermedObject.setMetadata("Expires", IntermedObjectUtil.dsf.format(mcm.getExpires()));
		if(mbm.getLastModified()!=null)
			intermedObject.setMetadata("Last-Modified", IntermedObjectUtil.dsf.format(mbm.getLastModified()));
		for(Entry<String,String> entry:mbm.getUserMetadata().entrySet()){
			intermedObject.setMetadata(entry.getKey(),entry.getValue());
		}
		InputStream is = bb.getPayload().getInput();
		intermedObject.setContext(IntermedObjectUtil.getInputStreanm(is));
		if(is!=null)
			Closeables.closeQuietly(is);
		return intermedObject;
	}
	
	private long getTs(Date date){
		if(date!=null)
			return date.getTime();
		else
			return DateUtil.getNow().getTime();
	}
}
