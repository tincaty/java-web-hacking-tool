package com.web.webDTO;

public class SSRFSCANNINGRequestDTO {

	private String endpoint;
	private long timestamp;
	private String evasionMode;
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getEvasionMode() {
		return evasionMode;
	}
	public void setEvasionMode(String evasionMode) {
		this.evasionMode = evasionMode;
	}
	
	

}
