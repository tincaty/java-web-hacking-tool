package com.web.webDTO;


//REALCAPTURINGEDITYDTO.java

import java.util.Map;

public class REALCAPTURINGEDITYDTO {
 private String originalUrl;
 private String originalMethod;
 private boolean edited;
 private String method;
 private String url;
 private Map<String, String> headers;
 private String postData;

 // Getters and Setters
 public String getOriginalUrl() {
     return originalUrl;
 }

 public void setOriginalUrl(String originalUrl) {
     this.originalUrl = originalUrl;
 }

 public String getOriginalMethod() {
     return originalMethod;
 }

 public void setOriginalMethod(String originalMethod) {
     this.originalMethod = originalMethod;
 }

 public boolean isEdited() {
     return edited;
 }

 public void setEdited(boolean edited) {
     this.edited = edited;
 }

 public String getMethod() {
     return method;
 }

 public void setMethod(String method) {
     this.method = method;
 }

 public String getUrl() {
     return url;
 }

 public void setUrl(String url) {
     this.url = url;
 }

 public Map<String, String> getHeaders() {
     return headers;
 }

 public void setHeaders(Map<String, String> headers) {
     this.headers = headers;
 }

 public String getPostData() {
     return postData;
 }

 public void setPostData(String postData) {
     this.postData = postData;
 }
}