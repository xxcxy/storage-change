package com.customtime.data.storagechange.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.customtime.data.storagechange.service.bean.FileObject;
import com.customtime.data.storagechange.service.bean.SKey;
import com.customtime.data.storagechange.service.exception.OperatorException;
import com.customtime.data.storagechange.service.util.DateUtil;
import com.customtime.data.storagechange.service.util.GoogAuthorizetionUtil;
import com.customtime.data.storagechange.service.util.StringUtil;
import com.customtime.data.storagechange.web.bean.MonitorBean;
import com.customtime.data.storagechange.web.bean.UserBean;
import com.customtime.data.storagechange.web.bean.UserKey;
import com.customtime.data.storagechange.web.service.OperationalService;
import com.customtime.data.storagechange.web.util.Constants;
import com.google.api.client.auth.oauth2.Credential;

@Controller
public class FileManagerController extends BaseController{
	
	@Autowired
	private OperationalService operationalService;
	
	@RequestMapping("/fileOp/controller")
	public void fileOp(long skeyId,String cmd,String target,String init,String source,Long targetKeyId,String type,ServletResponse res,HttpSession session){
		UserBean user = (UserBean)session.getAttribute(Constants.SESSION_USER);
		SKey skey = new SKey();
		boolean hasKey = false;
		for(UserKey uk:user.getSkey()){
			if(skeyId==uk.getSkeyId()){
				skey.setKeyId(uk.getKeyId());
				skey.setKeyName(uk.getKeyName());
				skey.setKeySecret(uk.getKeySecret());
				skey.setKeyType(uk.getKeyType());
				skey.setKeyDescription(uk.getKeyDescription());
				skey.setProject(uk.getProject());
				hasKey = true;
				break;
			}
		}
		if(hasKey&&"open".equals(cmd)){
			if(StringUtil.isNotBlank(target)){
				open(skey,target,res);
			}else if(StringUtil.isNotBlank(init)&&"1".equals(init)){
				init(skey,res);
			}
		}else if(hasKey&&"tree".equals(cmd)){
			tree(skey,target,res);
		}else if(hasKey&&"transfer".equals(cmd)){
			SKey tkey = new SKey();
			boolean hasTKey = false;
			for(UserKey uk:user.getSkey()){
				if(targetKeyId.longValue()==uk.getSkeyId()){
					tkey.setKeyId(uk.getKeyId());
					tkey.setKeyName(uk.getKeyName());
					tkey.setKeySecret(uk.getKeySecret());
					tkey.setKeyType(uk.getKeyType());
					hasTKey = true;
					break;
				}
			}
			if(hasTKey)
				transfer(skey,tkey,source,target,type,res);
		}
		
	}
	
	private void init(SKey skey,ServletResponse res){
		try {
			List<FileObject> files = operationalService.listBuckets(skey);
			Map<String,Object> json = new HashMap<String,Object>();
			Map<String, Object> cmd = new HashMap<String, Object>();
			if(files.size()>0){
				FileObject fileObject = files.get(0);
				cmd.put("name", fileObject.getName());
				cmd.put("hash",StringUtil.convert(fileObject.getPath()));
				cmd.put("mime", "directory");
				cmd.put("ts", fileObject.getTs());
				cmd.put("volumeid","l1_");
				cmd.put("locked",fileObject.isLocked()?1:0);
				cmd.put("size",fileObject.getSize());
				cmd.put("read",fileObject.isRead()?1:0);
				cmd.put("write",fileObject.isWrite()?1:0);
				json.put("cwd",cmd);
			}
			List<Map<String,Object>> tf = new ArrayList<Map<String,Object>>();
			for(FileObject fo:files){
				Map<String,Object> file = new HashMap<String,Object>();
				file.put("name", fo.getName());
				file.put("hash",StringUtil.convert(fo.getPath()));
				file.put("mime", "directory");
				file.put("ts", fo.getTs());
				file.put("volumeid",fo.getBucketName());
				file.put("locked",fo.isLocked()?1:0);
				file.put("read",fo.isRead()?1:0);
				file.put("size",fo.getSize());
				file.put("write",fo.isWrite()?1:0);
				file.put("dirs", fo.isHasDirs()?1:0);
				tf.add(file);
				List<FileObject> fileChildren = operationalService.listObject(skey,fo.getBucketName());
				for(FileObject fileObject:fileChildren){
					Map<String,Object> fileChild = new HashMap<String,Object>();
					fileChild.put("name", fileObject.getName());
					fileChild.put("hash",StringUtil.convert(fileObject.getPath()));
					fileChild.put("phash",StringUtil.convert(fileObject.getParentPath()));
					fileChild.put("mime", fileObject.getMime());
					fileChild.put("ts", fileObject.getTs());
					fileChild.put("locked",fileObject.isLocked()?1:0);
					fileChild.put("read",fileObject.isRead()?1:0);
					fileChild.put("size",fileObject.getSize());
					fileChild.put("write",fileObject.isWrite()?1:0);
					fileChild.put("dirs", fileObject.isHasDirs()?1:0);
					tf.add(fileChild);
				}
			}
			json.put("api",skey.getKeyType());
			json.put("files",tf);
			writeResponse(json,res);
		} catch (OperatorException e) {
			e.printStackTrace();
			Map<String,Object> errJson = new HashMap<String,Object>();
			errJson.put("error","获取bucket出错");
			writeResponse(errJson,res);
			
		}
	}
	private void tree(SKey skey,String parentPath,ServletResponse res){
		try{
			List<FileObject> files = operationalService.listDirs(skey,StringUtil.revert(parentPath));
			Map<String,Object> json = new HashMap<String,Object>();
			List<Map<String,Object>> trees = new ArrayList<Map<String,Object>>();
			for(FileObject fileObject:files){
				Map<String,Object> file = new HashMap<String,Object>();
				file.put("name", fileObject.getName());
				file.put("hash",StringUtil.convert(fileObject.getPath()));
				file.put("phash",StringUtil.convert(fileObject.getParentPath()));
				file.put("mime", fileObject.getMime());
				file.put("ts", fileObject.getTs());
				file.put("locked",fileObject.isLocked()?1:0);
				file.put("read",fileObject.isRead()?1:0);
				file.put("size",fileObject.getSize());
				file.put("write",fileObject.isWrite()?1:0);
				file.put("dirs", fileObject.isHasDirs()?1:0);
				trees.add(file);
			}
			json.put("tree",trees);
			writeResponse(json,res);
		}catch(Exception e){
			e.printStackTrace();
			Map<String,Object> errJson = new HashMap<String,Object>();
			errJson.put("error","获取目录出错");
			writeResponse(errJson,res);
		}
	}
	private void open(SKey skey,String parentPath,ServletResponse res){
		try{
			String path = StringUtil.revert(parentPath);
			List<FileObject> files = operationalService.listObject(skey,path);
			Map<String,Object> json = new HashMap<String,Object>();
			List<Map<String,Object>> fileList = new ArrayList<Map<String,Object>>();
			Map<String, Object> cmd = new HashMap<String, Object>();
			cmd.put("name", path.substring(path.lastIndexOf("/")+1));
			cmd.put("hash",StringUtil.convert(path));
			if(path.contains("/")){
				cmd.put("phash", StringUtil.convert(path.substring(0,path.lastIndexOf("/"))));
			}
			cmd.put("mime", "directory");
			cmd.put("ts", DateUtil.getNow().getTime());
			cmd.put("locked",0);
			cmd.put("size",0);
			cmd.put("read",1);
			cmd.put("write",1);
			json.put("cwd",cmd);
			fileList.add(cmd);
			for(FileObject fileObject:files){
				Map<String,Object> file = new HashMap<String,Object>();
				file.put("name", fileObject.getName());
				file.put("hash",StringUtil.convert(fileObject.getPath()));
				file.put("phash",StringUtil.convert(fileObject.getParentPath()));
				file.put("mime", fileObject.getMime());
				file.put("ts", fileObject.getTs());
				file.put("locked",fileObject.isLocked()?1:0);
				file.put("read",fileObject.isRead()?1:0);
				file.put("size",fileObject.getSize());
				file.put("write",fileObject.isWrite()?1:0);
				file.put("dirs", fileObject.isHasDirs()?1:0);
				fileList.add(file);
			}
			json.put("files",fileList);
			writeResponse(json,res);
		}catch(Exception e){
			e.printStackTrace();
			Map<String,Object> errJson = new HashMap<String,Object>();
			errJson.put("error","打开目录出错");
			writeResponse(errJson,res);
		}
		
	}
	private void transfer(SKey skey,SKey tkey,String source,String target,String type,ServletResponse res){
		MonitorBean mb = new MonitorBean();
		boolean se = true;
		if("d2d".equals(type)){
			se = operationalService.transferDir(skey, tkey,StringUtil.revert(source),StringUtil.revert(target), mb);
		}else if("f2d".equals(type)){
			se = operationalService.transferFile(skey, tkey,StringUtil.revert(source),StringUtil.revert(target), mb);
		}
		Map<String,Object> resultJson = new HashMap<String,Object>();
		if(se){
			List<String> sfs = mb.getSuccessFiles();
			List<String> wfs = mb.getWarnFiles();
			List<String> efs = mb.getErrorFiles();
			resultJson.put("success_count",sfs.size());
			resultJson.put("warn_count",wfs.size());
			resultJson.put("error_count",efs.size());
			resultJson.put("success_files", sfs);
			resultJson.put("warn_files", wfs);
			resultJson.put("error_files", efs);
		}else{
			resultJson.put("error","操作失败");
		}
		writeResponse(resultJson,res);
	}
	private void writeResponse(Map<String,Object> param,ServletResponse res){
		JSONObject json = new JSONObject();
		try {
			for(Entry<String,Object> p:param.entrySet())
				json.put(p.getKey(),p.getValue());
			res.setCharacterEncoding("UTF-8");
			res.setContentType("application/json; charset=UTF-8");
			json.write(res.getWriter());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/fileOp/beforeOpen")
	public void beforeOpenStorage(long skeyId,ServletResponse response,HttpSession session){
		UserBean user = (UserBean)session.getAttribute(Constants.SESSION_USER);
		Map<String,Object> resultJson = new HashMap<String,Object>();
		String code = "error";
		for(UserKey uk:user.getSkey()){
			if(skeyId==uk.getSkeyId()){
				if("googcsService".equals(uk.getKeyType())){
					try {
						Credential credential = GoogAuthorizetionUtil.getFlow(uk.getKeyId(),uk.getKeySecret()).loadCredential(uk.getKeyId());
						if (credential != null && credential.getAccessToken() != null) {
							code = "access";
						}else
							code = "toGoogAuth";
					} catch (IOException e) {
						e.printStackTrace();
						code = "error";
					}
					
				}else{
					code = "access";
				}
			}
		}
		resultJson.put("code",code);
		writeResponse(resultJson,response);
	}
}
