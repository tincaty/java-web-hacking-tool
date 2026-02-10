package com.web.webDTO;

import java.util.Map;
import java.util.HashMap;

public class SSRFSCANNINGDTO {
	private String url;
	private String method = "GET";
	private String body;
	private Map<String, String> headers = new HashMap<>();
	private boolean useBypass = false;
	private int attemptId;

	
	public void addHeader(String key, String value) {
		this.headers.put(key, value);
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public String getMethod() {
		return method;
	}


	public void setMethod(String method) {
		this.method = method;
	}


	public String getBody() {
		return body;
	}


	public void setBody(String body) {
		this.body = body;
	}


	public Map<String, String> getHeaders() {
		return headers;
	}


	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}


	public boolean isUseBypass() {
		return useBypass;
	}


	public void setUseBypass(boolean useBypass) {
		this.useBypass = useBypass;
	}


	public int getAttemptId() {
		return attemptId;
	}


	public void setAttemptId(int attemptId) {
		this.attemptId = attemptId;
	}
	
	
	
}