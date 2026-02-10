package com.web.webDTO;

//ScanRequestDTO.java
import java.util.Map;

public class ScanRequestDTO {
 private String url;
 private String httpMethod;
 private String jsonPayload;
 private String attackType;
 private Map<String, String> headers;
 private boolean useBypassTechniques;

 // Getters and Setters
 public String getUrl() { return url; }
 public void setUrl(String url) { this.url = url; }
 public String getHttpMethod() { return httpMethod; }
 public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
 public String getJsonPayload() { return jsonPayload; }
 public void setJsonPayload(String jsonPayload) { this.jsonPayload = jsonPayload; }
 public String getAttackType() { return attackType; }
 public void setAttackType(String attackType) { this.attackType = attackType; }
 public Map<String, String> getHeaders() { return headers; }
 public void setHeaders(Map<String, String> headers) { this.headers = headers; }
 public boolean isUseBypassTechniques() { return useBypassTechniques; }
 public void setUseBypassTechniques(boolean useBypassTechniques) { this.useBypassTechniques = useBypassTechniques; }
}