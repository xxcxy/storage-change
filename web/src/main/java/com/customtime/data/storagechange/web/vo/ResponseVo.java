package com.customtime.data.storagechange.web.vo;

public class ResponseVo {
	private String code;
	
	private String msg;
	
	public ResponseVo(){
		super();
	}
	
	public ResponseVo(String code,String msg){
		this();
		this.code = code;
		this.msg = msg;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	
}
