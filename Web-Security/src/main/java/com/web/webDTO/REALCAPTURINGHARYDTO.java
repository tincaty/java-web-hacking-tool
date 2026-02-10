package com.web.webDTO;

import java.util.Date;
import java.util.Objects;

public class REALCAPTURINGHARYDTO {
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
    private boolean edited;

    public REALCAPTURINGHARYDTO() {
    }

    public REALCAPTURINGHARYDTO(String requestUrl, int responseStatus, String method, 
                     Date startedDateTime, long time, String requestContent, 
                     String responseContent, String requestHeaders, 
                     String responseHeaders) {
        this(requestUrl, responseStatus, method, startedDateTime, time, 
             requestContent, responseContent, requestHeaders, responseHeaders, 
             null, 0, false);
    }

    public REALCAPTURINGHARYDTO(String requestUrl, int responseStatus, String method,
                      Date startedDateTime, long time, String requestContent,
                      String responseContent, String requestHeaders,
                      String responseHeaders, String contentEncoding,
                      long contentSize, boolean edited) {
        this.requestUrl = requestUrl;
        this.responseStatus = responseStatus;
        this.method = method;
        this.startedDateTime = startedDateTime;
        this.time = time;
        this.requestContent = requestContent;
        this.responseContent = responseContent;
        this.requestHeaders = requestHeaders;
        this.responseHeaders = responseHeaders;
        this.contentEncoding = contentEncoding;
        this.contentSize = contentSize;
        this.edited = edited;
    }

    // Getters and Setters
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

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        REALCAPTURINGHARYDTO that = (REALCAPTURINGHARYDTO) o;
        return responseStatus == that.responseStatus &&
                time == that.time &&
                contentSize == that.contentSize &&
                edited == that.edited &&
                Objects.equals(requestUrl, that.requestUrl) &&
                Objects.equals(method, that.method) &&
                Objects.equals(startedDateTime, that.startedDateTime) &&
                Objects.equals(requestContent, that.requestContent) &&
                Objects.equals(responseContent, that.responseContent) &&
                Objects.equals(requestHeaders, that.requestHeaders) &&
                Objects.equals(responseHeaders, that.responseHeaders) &&
                Objects.equals(contentEncoding, that.contentEncoding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestUrl, responseStatus, method, startedDateTime, time, 
                          requestContent, responseContent, requestHeaders, 
                          responseHeaders, contentEncoding, contentSize, edited);
    }

    @Override
    public String toString() {
        return "HarEntryDTO{" +
                "requestUrl='" + requestUrl + '\'' +
                ", responseStatus=" + responseStatus +
                ", method='" + method + '\'' +
                ", startedDateTime=" + startedDateTime +
                ", time=" + time +
                ", requestContent='" + requestContent + '\'' +
                ", responseContent='" + responseContent + '\'' +
                ", requestHeaders='" + requestHeaders + '\'' +
                ", responseHeaders='" + responseHeaders + '\'' +
                ", contentEncoding='" + contentEncoding + '\'' +
                ", contentSize=" + contentSize +
                ", edited=" + edited +
                '}';
    }
}