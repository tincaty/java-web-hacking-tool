package com.web.webDTO;

//ScanResponseDTO.java
import java.util.List;

public class ScanResponseDTO {
 private boolean vulnerable;
 private String vulnerabilityType;
 private String description;
 private String payloadUsed;
 private List<String> bypassTechniquesUsed;
 private String responseData;

 // Getters and Setters
 public boolean isVulnerable() { return vulnerable; }
 public void setVulnerable(boolean vulnerable) { this.vulnerable = vulnerable; }
 public String getVulnerabilityType() { return vulnerabilityType; }
 public void setVulnerabilityType(String vulnerabilityType) { this.vulnerabilityType = vulnerabilityType; }
 public String getDescription() { return description; }
 public void setDescription(String description) { this.description = description; }
 public String getPayloadUsed() { return payloadUsed; }
 public void setPayloadUsed(String payloadUsed) { this.payloadUsed = payloadUsed; }
 public List<String> getBypassTechniquesUsed() { return bypassTechniquesUsed; }
 public void setBypassTechniquesUsed(List<String> bypassTechniquesUsed) { this.bypassTechniquesUsed = bypassTechniquesUsed; }
 public String getResponseData() { return responseData; }
 public void setResponseData(String responseData) { this.responseData = responseData; }
}