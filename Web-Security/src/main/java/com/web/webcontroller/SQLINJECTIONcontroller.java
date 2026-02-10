package com.web.webcontroller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.web.webDTO.SQLinjectionDTO;
import com.web.webservices.SQLINJECTIONservices;

import java.util.List;
@RestController
@RequestMapping("/sqlattack")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
public class SQLINJECTIONcontroller {

    @Autowired
    private SQLINJECTIONservices sqlInjectionService;

    @PostMapping("/scan")
    public ResponseEntity<List<String>> runSqlInjectionTest(@RequestBody SQLinjectionDTO request) {
        try {
            List<String> results = sqlInjectionService.runTest(request);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of(
                "[✖] Server error during SQL injection test: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/test")
    String getTest() {
    	
    	return "yes it is working no cors error";
    }
    
}