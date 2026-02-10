package com.web.webDTO;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class SQLInjectionTestResult {
    private Date startTime;
    private Date endTime;
    private boolean vulnerable;
    private String dbms;
    private Set<String> techniquesDetected = new LinkedHashSet<>();
    private Set<String> successfulPayloads = new LinkedHashSet<>();
    private Set<String> wafBypassTechniques = new LinkedHashSet<>();
    private Map<String, String> extractedData = new LinkedHashMap<>();
    private String errorMessage;

    // Getters and Setters
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public boolean isVulnerable() {
        return vulnerable;
    }

    public void setVulnerable(boolean vulnerable) {
        this.vulnerable = vulnerable;
    }

    public String getDbms() {
        return dbms;
    }

    public void setDbms(String dbms) {
        this.dbms = dbms;
    }

    public Set<String> getTechniquesDetected() {
        return techniquesDetected;
    }

    public void setTechniquesDetected(Set<String> techniquesDetected) {
        this.techniquesDetected = techniquesDetected;
    }

    public Set<String> getSuccessfulPayloads() {
        return successfulPayloads;
    }

    public void setSuccessfulPayloads(Set<String> successfulPayloads) {
        this.successfulPayloads = successfulPayloads;
    }

    public Set<String> getWafBypassTechniques() {
        return wafBypassTechniques;
    }

    public void setWafBypassTechniques(Set<String> wafBypassTechniques) {
        this.wafBypassTechniques = wafBypassTechniques;
    }

    public Map<String, String> getExtractedData() {
        return extractedData;
    }

    public void setExtractedData(Map<String, String> extractedData) {
        this.extractedData = extractedData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}