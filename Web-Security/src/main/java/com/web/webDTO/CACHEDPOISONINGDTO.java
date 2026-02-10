package com.web.webDTO;

import java.util.Map;
import java.util.HashMap;

public class CACHEDPOISONINGDTO {
    private String targetUrl;
    private String payload;
    private String httpMethod;
    private String cdnType; 
    private Map<String, String> headers;
    private Map<String, String> bodyParams;
    private boolean akamaiDeception;
    private boolean serviceWorkerPoisoning;
    private boolean cloudflareBypass;
    private boolean awsEdgeCase;
    private boolean advancedSeleniumDetection;
    private boolean methodOverride;
    private boolean cacheKeyInjection;

    public CACHEDPOISONINGDTO() {
        this.headers = new HashMap<>();
        this.bodyParams = new HashMap<>();
    }

    // Getters and setters
    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getCdnType() {
        return cdnType;
    }

    public void setCdnType(String cdnType) {
        this.cdnType = cdnType;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getBodyParams() {
        return bodyParams;
    }

    public void setBodyParams(Map<String, String> bodyParams) {
        this.bodyParams = bodyParams;
    }

    public boolean isAkamaiDeception() {
        return akamaiDeception;
    }

    public void setAkamaiDeception(boolean akamaiDeception) {
        this.akamaiDeception = akamaiDeception;
    }

    public boolean isServiceWorkerPoisoning() {
        return serviceWorkerPoisoning;
    }

    public void setServiceWorkerPoisoning(boolean serviceWorkerPoisoning) {
        this.serviceWorkerPoisoning = serviceWorkerPoisoning;
    }

    public boolean isCloudflareBypass() {
        return cloudflareBypass;
    }

    public void setCloudflareBypass(boolean cloudflareBypass) {
        this.cloudflareBypass = cloudflareBypass;
    }

    public boolean isAwsEdgeCase() {
        return awsEdgeCase;
    }

    public void setAwsEdgeCase(boolean awsEdgeCase) {
        this.awsEdgeCase = awsEdgeCase;
    }

    public boolean isAdvancedSeleniumDetection() {
        return advancedSeleniumDetection;
    }

    public void setAdvancedSeleniumDetection(boolean advancedSeleniumDetection) {
        this.advancedSeleniumDetection = advancedSeleniumDetection;
    }

    public boolean isMethodOverride() {
        return methodOverride;
    }

    public void setMethodOverride(boolean methodOverride) {
        this.methodOverride = methodOverride;
    }

    public boolean isCacheKeyInjection() {
        return cacheKeyInjection;
    }

    public void setCacheKeyInjection(boolean cacheKeyInjection) {
        this.cacheKeyInjection = cacheKeyInjection;
    }
}