package com.web.webservices;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.*;
import net.lightbody.bmp.proxy.CaptureType;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.stereotype.Service;
import com.web.webDTO.CaptureRequestDTO;
import com.web.webDTO.REALCAPTURINGEDITYDTO;
import com.web.webDTO.REALCAPTURINGHARYDTO;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.JavascriptExecutor;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import io.netty.handler.codec.http.HttpMethod;

@Service
public class REALCAPTURINGService {

	private final AtomicReference<BrowserMobProxy> proxyRef = new AtomicReference<>();
	private final AtomicReference<WebDriver> driverRef = new AtomicReference<>();
	private final Map<String, REALCAPTURINGEDITYDTO> requestEdits = new HashMap<>();

	private static final Pattern STATIC_RESOURCE_PATTERN = Pattern.compile(
			".*\\.(js|css|ico|png|jpg|jpeg|gif|svg|woff|woff2|ttf|eot|map|html|mozlz4|json)(\\?.*)?$",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern EXCLUDED_DOMAINS = Pattern.compile(
			"(firefox-settings-attachments\\.cdn\\.mozilla\\.net|detectportal\\.firefox\\.com|googleapis\\.com|gstatic\\.com|facebook\\.net)",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern API_PATH_PATTERN = Pattern
			.compile("/api/|/graphql|/rest/|/v[0-9]+/|/services/|/endpoint/|/ajax/", Pattern.CASE_INSENSITIVE);

	// Small stealth JavaScript injected into pages
	private static final String STEALTH_JS = """
			    Object.defineProperty(navigator, 'webdriver', {get: () => undefined});
			    window.navigator.chrome = { runtime: {} };
			    Object.defineProperty(navigator, 'languages', {get: () => ['en-US', 'en']});
			    Object.defineProperty(navigator, 'plugins', {get: () => [1,2,3,4,5]});
			    const originalQuery = window.navigator.permissions.query;
			    window.navigator.permissions.query = (parameters) => (
			      parameters.name === 'notifications' ?
			        Promise.resolve({ state: Notification.permission }) :
			        originalQuery(parameters)
			    );
			""";

	public void startCapture(CaptureRequestDTO captureRequest) {
		BrowserMobProxy proxy = new BrowserMobProxyServer();
		proxy.setTrustAllServers(true);
		proxy.start();
		proxy.newHar();

		proxy.enableHarCaptureTypes(CaptureType.REQUEST_HEADERS, CaptureType.RESPONSE_HEADERS,
				CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT, CaptureType.REQUEST_COOKIES,
				CaptureType.RESPONSE_COOKIES);

		proxy.addRequestFilter((request, contents, messageInfo) -> {
			String requestId = generateRequestId(messageInfo.getOriginalUrl(), request.getMethod().name());

			if (requestEdits.containsKey(requestId)) {
				REALCAPTURINGEDITYDTO edit = requestEdits.get(requestId);

				if (edit.isEdited()) {
					if (edit.getMethod() != null) {
						request.setMethod(HttpMethod.valueOf(edit.getMethod()));
					}
					if (edit.getUrl() != null) {
						messageInfo.getOriginalRequest().setUri(edit.getUrl());
					}
					if (edit.getHeaders() != null) {
						request.headers().clear();
						edit.getHeaders().forEach((name, value) -> request.headers().add(name, value));
					}
					if (edit.getPostData() != null && "POST".equalsIgnoreCase(request.getMethod().name())) {
						contents.setTextContents(edit.getPostData());
					}
				}
			}

			return null;
		});

		Proxy seleniumProxy = new Proxy();
		seleniumProxy.setHttpProxy("localhost:" + proxy.getPort());
		seleniumProxy.setSslProxy("localhost:" + proxy.getPort());
		// Load custom Firefox profile folder path
		File profileDir = new File(System.getProperty("user.home") + "/.mozilla/firefox/andrew");
		FirefoxProfile profile = new FirefoxProfile(profileDir);
		FirefoxOptions options = new FirefoxOptions();
		options.setProxy(seleniumProxy);
		options.setAcceptInsecureCerts(true);
		// --- Stealth Mode Setup ---
		profile.setPreference("dom.webdriver.enabled", false);
		profile.setPreference("useAutomationExtension", false);
		profile.setPreference("general.useragent.override",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
		profile.setPreference("media.navigator.enabled", false);
		profile.setPreference("permissions.default.microphone", 2);
		profile.setPreference("permissions.default.camera", 2);
		profile.setPreference("permissions.default.geo", 2);

		options.setProfile(profile);
		options.addPreference("dom.webnotifications.enabled", false);
		options.addArguments("--disable-blink-features=AutomationControlled");

		WebDriver driver = new FirefoxDriver(options);

		proxyRef.set(proxy);
		driverRef.set(driver);

		if (captureRequest.getTargetUrl() != null && !captureRequest.getTargetUrl().isEmpty()) {
			driver.get(captureRequest.getTargetUrl());
			injectStealthJavaScript(driver); // 🛡 Inject stealth JS after opening target
		}
	}

	public List<REALCAPTURINGHARYDTO> getCapturedData() {
		BrowserMobProxy proxy = proxyRef.get();
		if (proxy == null) {
			throw new IllegalStateException("Proxy not initialized. Start capture first.");
		}

		Har har = proxy.getHar();
		List<HarEntry> logEntries = har.getLog().getEntries();

		return logEntries.stream().filter(this::isDesiredRequest).map(this::mapToHarEntryDTO)
				.collect(Collectors.toList());
	}

	private boolean isDesiredRequest(HarEntry entry) {
		String method = entry.getRequest().getMethod();
		String url = entry.getRequest().getUrl();

		if ("OPTIONS".equalsIgnoreCase(method) || "CONNECT".equalsIgnoreCase(method)) {
			return false;
		}

		if (!"GET".equalsIgnoreCase(method) && !"POST".equalsIgnoreCase(method)) {
			return false;
		}

		if (STATIC_RESOURCE_PATTERN.matcher(url).matches()) {
			return false;
		}

		if (EXCLUDED_DOMAINS.matcher(url).find()) {
			return false;
		}

		if (API_PATH_PATTERN.matcher(url).find()) {
			return true;
		}

		List<HarNameValuePair> headers = entry.getRequest().getHeaders();
		boolean hasJsonAccept = headers.stream().anyMatch(
				h -> "Accept".equalsIgnoreCase(h.getName()) && h.getValue().toLowerCase().contains("application/json"));

		boolean hasJsonContent = entry.getResponse().getContent() != null
				&& "application/json".equalsIgnoreCase(entry.getResponse().getContent().getMimeType());

		return hasJsonAccept || hasJsonContent;
	}

	public void saveRequestEdit(REALCAPTURINGEDITYDTO edit) {
		String requestId = generateRequestId(edit.getOriginalUrl(), edit.getOriginalMethod());
		requestEdits.put(requestId, edit);
	}

	private String generateRequestId(String url, String method) {
		return Base64.getEncoder().encodeToString((url + "|" + method).getBytes());
	}

	private REALCAPTURINGHARYDTO mapToHarEntryDTO(HarEntry entry) {
		HarRequest request = entry.getRequest();
		HarResponse response = entry.getResponse();
		HarPostData postData = request.getPostData();

		return new REALCAPTURINGHARYDTO(request.getUrl(), response.getStatus(), request.getMethod(),
				entry.getStartedDateTime(), entry.getTime(), postData != null ? postData.getText() : null,
				response.getContent() != null ? response.getContent().getText() : null,
				convertHeadersToString(request.getHeaders()), convertHeadersToString(response.getHeaders()),
				response.getContent() != null ? response.getContent().getEncoding() : null,
				response.getContent() != null ? response.getContent().getSize() : 0, isRequestEdited(entry));
	}

	private boolean isRequestEdited(HarEntry entry) {
		String requestId = generateRequestId(entry.getRequest().getUrl(), entry.getRequest().getMethod());
		return requestEdits.containsKey(requestId) && requestEdits.get(requestId).isEdited();
	}

	private String convertHeadersToString(List<HarNameValuePair> headers) {
		if (headers == null || headers.isEmpty()) {
			return "";
		}
		return headers.stream().map(header -> header.getName() + ": " + header.getValue())
				.collect(Collectors.joining("\n"));
	}

	public String stopCaptureAndGetHar() {
		BrowserMobProxy proxy = proxyRef.get();
		WebDriver driver = driverRef.get();

		if (proxy != null) {
			Har har = proxy.getHar();
			proxy.stop();
			proxyRef.set(null);

			if (driver != null) {
				driver.quit();
				driverRef.set(null);
			}

			return har.toString();
		}

		throw new IllegalStateException("No active capture session");
	}

	// 🚀 New Method: Injects stealth JavaScript
	private void injectStealthJavaScript(WebDriver driver) {
		if (driver instanceof JavascriptExecutor jsExecutor) {
			jsExecutor.executeScript(STEALTH_JS);
		}
	}

}