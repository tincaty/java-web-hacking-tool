
package com.web.webcontroller;

import com.web.errors.SSRFSCANNINGException;
import com.web.webDTO.SSRFSCANNINGDTO;
import com.web.webservices.SSRFSCANNINGService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/ssrf")
public class SSRFSCANNINGController {

	private final SSRFSCANNINGService ssrfService;
	private final Map<String, Integer> scanAttempts = new ConcurrentHashMap<>();

	public SSRFSCANNINGController(SSRFSCANNINGService ssrfService) {
		this.ssrfService = ssrfService;
	}

	@GetMapping("/fuzz")
	public ResponseEntity<List<String>> runFuzzScan(@RequestHeader Map<String, String> headers) {
		try {
			// Rate limiting check
			String clientIp = headers.getOrDefault("x-forwarded-for", "local");
			if (scanAttempts.getOrDefault(clientIp, 0) > 10) {
				return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(List.of("Too many requests"));
			}

			scanAttempts.merge(clientIp, 1, Integer::sum);
			List<String> results = ssrfService.ssrfPayloadFuzzer();
			return ResponseEntity.ok(results);
		} catch (SSRFSCANNINGException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of("Error: " + e.getMessage()));
		}
	}

	@PostMapping("/scan")
	public ResponseEntity<String> smartScan(@RequestBody SSRFSCANNINGDTO request,
			@RequestHeader Map<String, String> headers) {

		// Rate limiting
		String clientIp = headers.getOrDefault("x-forwarded-for", "local");
		if (scanAttempts.getOrDefault(clientIp, 0) > 5) {
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many scan attempts");
		}

		scanAttempts.merge(clientIp, 1, Integer::sum);

		try {
			String result = ssrfService.fetchUrlSmart(request.getUrl(), request.getMethod(), request.getBody());
			return ResponseEntity.ok(result);
		} catch (SSRFSCANNINGException | IOException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
		}
	}

	@PostMapping("/bypass-scan")
	public ResponseEntity<String> bypassScan(@RequestBody SSRFSCANNINGDTO request,
			@RequestHeader Map<String, String> headers) {

		String clientIp = headers.getOrDefault("x-forwarded-for", "local");
		if (scanAttempts.getOrDefault(clientIp, 0) > 3) {
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many bypass attempts");
		}

		scanAttempts.merge(clientIp, 1, Integer::sum);

		try {
			String result = ssrfService.fetchUrlWithBypass(request.getUrl(), request.getMethod(), request.getBody(), 0);
			return ResponseEntity.ok(result);
		} catch (SSRFSCANNINGException | IOException | InterruptedException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bypass failed: " + e.getMessage());
		}
	}

	@ExceptionHandler(SSRFSCANNINGException.class)
	public ResponseEntity<String> handleSSRFError(SSRFSCANNINGException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("SSRF Error: " + ex.getMessage());
	}

	@GetMapping("/reset-attempts")
	public ResponseEntity<String> resetAttempts(@RequestParam String secret) {
		if ("admin123".equals(secret)) {
			scanAttempts.clear();
			return ResponseEntity.ok("Scan attempts reset");
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid secret");
	}
}
