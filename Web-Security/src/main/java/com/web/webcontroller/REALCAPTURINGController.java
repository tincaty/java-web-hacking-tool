package com.web.webcontroller;

// REALCAPTURINGController.java
import com.web.webservices.REALCAPTURINGService;
import com.web.webDTO.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/real/capture")
@CrossOrigin(origins="http://localhost:3000")
public class REALCAPTURINGController {

    private final REALCAPTURINGService capturingService;

    public REALCAPTURINGController(REALCAPTURINGService capturingService) {
        this.capturingService = capturingService;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startCapture(@RequestBody CaptureRequestDTO request) {
        try {
            capturingService.startCapture(request);
            return ResponseEntity.ok("Capture started successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error starting capture: " + e.getMessage());
        }
    }

    @GetMapping("/data")
    public ResponseEntity<List<REALCAPTURINGHARYDTO>> getCapturedData() {
        try {
            List<REALCAPTURINGHARYDTO> data = capturingService.getCapturedData();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/edit")
    public ResponseEntity<String> saveRequestEdit(@RequestBody REALCAPTURINGEDITYDTO edit) {
        try {
            capturingService.saveRequestEdit(edit);
            return ResponseEntity.ok("Request edit saved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error saving edit: " + e.getMessage());
        }
    }

   

    @PostMapping("/stop")
    public ResponseEntity<String> stopCapture() {
        try {
            String har = capturingService.stopCaptureAndGetHar();
            return ResponseEntity.ok(har);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error stopping capture: " + e.getMessage());
        }
    }
}