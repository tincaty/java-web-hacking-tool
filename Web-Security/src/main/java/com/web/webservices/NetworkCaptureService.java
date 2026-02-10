package com.web.webservices;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.*;
import net.lightbody.bmp.proxy.CaptureType;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.springframework.stereotype.Service;
import com.web.webDTO.CaptureRequestDTO;
import com.web.webDTO.HarEntryDTO;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class NetworkCaptureService {

    private final AtomicReference<BrowserMobProxy> proxyRef = new AtomicReference<>();
    private final AtomicReference<WebDriver> driverRef = new AtomicReference<>();

    public void startCapture(CaptureRequestDTO captureRequest) {
        BrowserMobProxy proxy = new BrowserMobProxyServer();
        proxy.setTrustAllServers(true);
        proxy.start();
        proxy.newHar();

        proxy.enableHarCaptureTypes(
            CaptureType.REQUEST_HEADERS,
            CaptureType.RESPONSE_HEADERS,
            CaptureType.REQUEST_CONTENT,
            CaptureType.RESPONSE_CONTENT
        );

        proxy.addRequestFilter((request, contents, messageInfo) -> {
            if ("POST".equalsIgnoreCase(request.method().name())) {
                // Capture POST requests
                return null;
            }
            return null; // Allow all requests
        });

        Proxy seleniumProxy = new Proxy();
        seleniumProxy.setHttpProxy("localhost:" + proxy.getPort());
        seleniumProxy.setSslProxy("localhost:" + proxy.getPort());

        FirefoxOptions options = new FirefoxOptions();
        options.setCapability(CapabilityType.PROXY, seleniumProxy);
        options.setAcceptInsecureCerts(true);

        WebDriver driver = WebDriverManager.firefoxdriver().capabilities(options).create();

        proxyRef.set(proxy);
        driverRef.set(driver);

        if (captureRequest.getTargetUrl() != null && !captureRequest.getTargetUrl().isEmpty()) {
            driver.get(captureRequest.getTargetUrl());
        }
    }

    public List<HarEntryDTO> getCapturedData() {
        BrowserMobProxy proxy = proxyRef.get();
        if (proxy == null) {
            throw new IllegalStateException("Proxy not initialized. Start capture first.");
        }

        Har har = proxy.getHar();
        List<HarEntry> logEntries = har.getLog().getEntries();

        return logEntries.stream()
            .filter(entry -> "POST".equalsIgnoreCase(entry.getRequest().getMethod()))
            .map(this::mapToHarEntryDTO)
            .collect(Collectors.toList());
    }

    private HarEntryDTO mapToHarEntryDTO(HarEntry entry) {
        HarRequest request = entry.getRequest();
        HarResponse response = entry.getResponse();
        HarPostData postData = request.getPostData();

        return new HarEntryDTO(
            request.getUrl(),
            response.getStatus(),
            request.getMethod(),
            entry.getStartedDateTime(),
            entry.getTime(),
            postData != null ? postData.getText() : null,
            response.getContent() != null ? response.getContent().getText() : null,
            convertHeadersToString(request.getHeaders()),
            convertHeadersToString(response.getHeaders())
        );
    }

    private String convertHeadersToString(List<HarNameValuePair> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }
        return headers.stream()
            .map(header -> header.getName() + ": " + header.getValue())
            .collect(Collectors.joining("\n"));
    }

    public String stopCaptureAndGetHar() {
        BrowserMobProxy proxy = proxyRef.get();
        WebDriver driver = driverRef.get();

        if (proxy != null) {
            Har originalHar = proxy.getHar();

            List<HarEntry> filteredEntries = originalHar.getLog().getEntries().stream()
                .filter(entry -> "POST".equalsIgnoreCase(entry.getRequest().getMethod()))
                .filter(entry -> isBundleRequest(entry.getRequest().getUrl())) // Filter by URL pattern for bundle files
                .collect(Collectors.toList());

            // Log the filtered POST requests (for example)
            filteredEntries.forEach(entry -> System.out.println("Captured POST URL: " + entry.getRequest().getUrl()));

            proxy.stop();
            proxyRef.set(null);

            if (driver != null) {
                driver.quit();
                driverRef.set(null);
            }

            return "Filtered POST entries processed. Total: " + filteredEntries.size();
        }

        throw new IllegalStateException("No active capture session");
    }

    private boolean isBundleRequest(String url) {
        // Filter URLs that likely correspond to the JS bundles (based on known patterns in your filenames)
        return url.contains("index-") || url.contains("bundle-") || url.contains("link-");
    }
}
