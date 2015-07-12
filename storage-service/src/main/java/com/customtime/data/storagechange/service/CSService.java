package com.customtime.data.storagechange.service;

import java.util.List;

import com.customtime.data.storagechange.service.bean.FileObject;
import com.customtime.data.storagechange.service.bean.IntermedObject;
import com.customtime.data.storagechange.service.bean.MultiInfo;
import com.customtime.data.storagechange.service.bean.PartInfo;
import com.customtime.data.storagechange.service.bean.SKey;
import com.customtime.data.storagechange.service.exception.OperatorException;

public interface CSService {
	public boolean isBucketExist(String bucketName,SKey key);
	public void createBucket(String bucketName,SKey key)throws OperatorException;
	public IntermedObject getObject(String bucketName,String fileName,SKey key)throws OperatorException;
	public IntermedObject getObject(String bucketName,String fileName,PartInfo partInfo,SKey key)throws OperatorException;
	public MultiInfo getNeccessPart(String bucketName,String fileName,SKey key,long size)throws OperatorException;
	public boolean putPartObject(PartInfo partInfo,IntermedObject intermedObject,SKey key,MultiInfo mi)throws OperatorException;
	public boolean putObject(String bucketName,IntermedObject intermedObject,SKey key)throws OperatorException;
	public boolean completeMultipartUpload(SKey key,MultiInfo mi)throws OperatorException;
	public List<String> listObject(String bucketName,SKey key)throws OperatorException;
	public long getFileSize(String bucketName,String fileName,SKey key);
	public List<FileObject> listBucket(SKey key)throws OperatorException;
	public List<FileObject> listDirs(SKey skey,String bucketName,String prefix)throws OperatorException;
	public List<FileObject> listObject(SKey skey,String bucketName,String prefix)throws OperatorException;
	public List<FileObject> listAllObject(SKey skey,String bucketName,String prefix)throws OperatorException;
}
