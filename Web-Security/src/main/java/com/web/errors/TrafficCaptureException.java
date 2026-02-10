package com.web.errors;

public class TrafficCaptureException extends Exception {

    private static final long serialVersionUID = 1L;

	public TrafficCaptureException(String message) {
        super(message);
    }

    public TrafficCaptureException(String message, Throwable cause) {
        super(message, cause);
    }
}
