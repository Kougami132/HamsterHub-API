package com.hamsterhub.common.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(Include.NON_NULL)
@Schema( description = "响应通用参数包装")
public class RestResponse<T> {

	@Schema(description = "响应错误编码,0为正常")
	private int code;
	
	@Schema(description = "响应错误信息")
	private String msg;
	
	@Schema(description = "响应内容")
	private T data;

	public static <T> RestResponse<T> success() {
		return new RestResponse<T>();
	}

	public static <T> RestResponse<T> success(T result) {
		RestResponse<T> response = new RestResponse<T>();
		response.setResult(result);
		return response;
	}

	public static <T> RestResponse<T> validfail(String msg) {
		RestResponse<T> response = new RestResponse<T>();
		response.setCode(-2);
		response.setMsg(msg);
		return response;
	}

	public RestResponse() {
		this(0, "");
	}

	public RestResponse(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setResult(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "RestResponse [code=" + code + ", msg=" + msg + ", result="
				+ data + "]";
	}

}
