package com.web.webDTO;
//CaptureRequestDTO.java


public class CaptureRequestDTO {
	 private String targetUrl;
	 private boolean captureRequestContent;
	 private boolean captureResponseContent;

	 // Getters and Setters
	 public String getTargetUrl() {
	     return targetUrl;
	 }

	 public void setTargetUrl(String targetUrl) {
	     this.targetUrl = targetUrl;
	 }

	 public boolean isCaptureRequestContent() {
	     return captureRequestContent;
	 }

	 public void setCaptureRequestContent(boolean captureRequestContent) {
	     this.captureRequestContent = captureRequestContent;
	 }

	 public boolean isCaptureResponseContent() {
	     return captureResponseContent;
	 }

	 public void setCaptureResponseContent(boolean captureResponseContent) {
	     this.captureResponseContent = captureResponseContent;
	 }

	
	}