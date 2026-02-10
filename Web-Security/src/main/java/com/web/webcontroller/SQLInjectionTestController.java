package com.web.webcontroller;

import com.web.webDTO.SQLInjectionTestRequest;
import com.web.webDTO.SQLInjectionTestResult;
import com.web.webservices.SQLInjectionWithGetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sqli-test")
public class SQLInjectionTestController {

    private final SQLInjectionWithGetService sqlInjectionService;

    @Autowired
    public SQLInjectionTestController(SQLInjectionWithGetService sqlInjectionService) {
        this.sqlInjectionService = sqlInjectionService;
    }

    @PostMapping("/test")
    public SQLInjectionTestResult testForSQLInjection(@RequestBody SQLInjectionTestRequest request) {
        return sqlInjectionService.performAdvancedTest(request);
    }
}