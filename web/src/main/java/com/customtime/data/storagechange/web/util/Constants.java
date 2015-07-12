package com.customtime.data.storagechange.web.util;

public interface Constants {
	int UPLOAD_THREAD_COUNT = 5;
	int DOWNLOAD_THREAD_COUNT = 5;
	int QUEUE_CAPACITY = 200;
	long FILE_PART_SIZE = 10*1024*1024L;
	String VERIFYCODE = "validateCode";
	String SESSION_USER = "sessionUserKey";
}
