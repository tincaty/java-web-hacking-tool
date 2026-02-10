package com.web.webcontroller;

//NetworkCaptureController.java
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.web.webDTO.CaptureRequestDTO;
import com.web.webDTO.HarEntryDTO;
import com.web.webservices.NetworkCaptureService;

import java.util.List;

@RestController
@CrossOrigin(origins="http://localhost:3000")
@RequestMapping("/api/capture")
public class NetworkCaptureController {

 private final NetworkCaptureService captureService;

 public NetworkCaptureController(NetworkCaptureService captureService) {
     this.captureService = captureService;
 }

 @PostMapping("/start")
 public ResponseEntity<String> startCapture(@RequestBody CaptureRequestDTO request) {
     captureService.startCapture(request);
     return ResponseEntity.ok("Capture started successfully");
 }

 @GetMapping("/entries")
 public ResponseEntity<List<HarEntryDTO>> getCapturedEntries() {
     List<HarEntryDTO> entries = captureService.getCapturedData();
     return ResponseEntity.ok(entries);
 }

 @GetMapping("/stop")
 public ResponseEntity<byte[]> stopCapture() {
     String harContent = captureService.stopCaptureAndGetHar();
     
     HttpHeaders headers = new HttpHeaders();
     headers.setContentType(MediaType.APPLICATION_JSON);
     headers.setContentDispositionFormData("attachment", "network_capture.har");
     
     return new ResponseEntity<>(harContent.getBytes(), headers, HttpStatus.OK);
 }
}