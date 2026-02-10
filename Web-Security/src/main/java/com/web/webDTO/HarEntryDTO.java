package com.web.webDTO;

import java.util.Date;

public class HarEntryDTO {
    private String requestUrl;
    private int responseStatus;
    private String method;
    private Date startedDateTime;
    private long time;
    private String requestContent;
    private String responseContent;
    private String requestHeaders;
    private String responseHeaders;
    private String contentEncoding;
    private long contentSize;

    public HarEntryDTO() {
    }

    public HarEntryDTO(String requestUrl, int responseStatus, String method, 
                      Date startedDateTime, long time, String requestContent, 
                      String responseContent, String requestHeaders, String responseHeaders) {
        this.requestUrl = requestUrl;
        this.responseStatus = responseStatus;
        this.method = method;
        this.startedDateTime = startedDateTime;
        this.time = time;
        this.requestContent = requestContent;
        this.responseContent = responseContent;
        this.requestHeaders = requestHeaders;
        this.responseHeaders = responseHeaders;
    }



	// Add new getters and setters
    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public long getContentSize() {
        return contentSize;
    }

    public void setContentSize(long contentSize) {
        this.contentSize = contentSize;
    }

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public int getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(int responseStatus) {
		this.responseStatus = responseStatus;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Date getStartedDateTime() {
		return startedDateTime;
	}

	public void setStartedDateTime(Date startedDateTime) {
		this.startedDateTime = startedDateTime;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getRequestContent() {
		return requestContent;
	}

	public void setRequestContent(String requestContent) {
		this.requestContent = requestContent;
	}

	public String getResponseContent() {
		return responseContent;
	}

	public void setResponseContent(String responseContent) {
		this.responseContent = responseContent;
	}

	public String getRequestHeaders() {
		return requestHeaders;
	}

	public void setRequestHeaders(String requestHeaders) {
		this.requestHeaders = requestHeaders;
	}

	public String getResponseHeaders() {
		return responseHeaders;
	}

	public void setResponseHeaders(String responseHeaders) {
		this.responseHeaders = responseHeaders;
	}

    // ... (keep all existing getters and setters) ...
}