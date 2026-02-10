package com.web.webDTO;

import java.util.Map;

public class SQLInjectionTestRequest {
    private String url;
    private Map<String, String> parameters;
    private String httpMethod = "GET";
    private Map<String, String> customHeaders;
    private boolean disableSSLVerification = false;
    private boolean testErrorBased = true;
    private boolean testBooleanBased = true;
    private boolean testTimeBased = true;
    private boolean testUnionBased = true;
    private boolean testStackedQueries = false;
    private boolean attemptWafBypass = false;
    private boolean attemptDataExtraction = false;

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getCustomHeaders() {
        return customHeaders;
    }

    public void setCustomHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
    }

    public boolean isDisableSSLVerification() {
        return disableSSLVerification;
    }

    public void setDisableSSLVerification(boolean disableSSLVerification) {
        this.disableSSLVerification = disableSSLVerification;
    }

    public boolean isTestErrorBased() {
        return testErrorBased;
    }

    public void setTestErrorBased(boolean testErrorBased) {
        this.testErrorBased = testErrorBased;
    }

    public boolean isTestBooleanBased() {
        return testBooleanBased;
    }

    public void setTestBooleanBased(boolean testBooleanBased) {
        this.testBooleanBased = testBooleanBased;
    }

    public boolean isTestTimeBased() {
        return testTimeBased;
    }

    public void setTestTimeBased(boolean testTimeBased) {
        this.testTimeBased = testTimeBased;
    }

    public boolean isTestUnionBased() {
        return testUnionBased;
    }

    public void setTestUnionBased(boolean testUnionBased) {
        this.testUnionBased = testUnionBased;
    }

    public boolean isTestStackedQueries() {
        return testStackedQueries;
    }

    public void setTestStackedQueries(boolean testStackedQueries) {
        this.testStackedQueries = testStackedQueries;
    }

    public boolean isAttemptWafBypass() {
        return attemptWafBypass;
    }

    public void setAttemptWafBypass(boolean attemptWafBypass) {
        this.attemptWafBypass = attemptWafBypass;
    }

    public boolean isAttemptDataExtraction() {
        return attemptDataExtraction;
    }

    public void setAttemptDataExtraction(boolean attemptDataExtraction) {
        this.attemptDataExtraction = attemptDataExtraction;
    }
}