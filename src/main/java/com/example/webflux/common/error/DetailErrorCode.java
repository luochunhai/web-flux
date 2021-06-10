package com.example.webflux.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DetailErrorCode {
	API_KEY_NOT_AVAILABLE("1", "API Key not available."),
	API_KEY_INVALID("2", "API Key is invalid."),
	SUBSCRIPTION_API_REQUIRED("3", "A subscription to the API is required."),
	IP_NOT_ALLOWED("4", "IP(%s) not allowed."),
	URL_NOT_FOUND("5", "URL not found."),
	ACCOUNT_NOT_ALLOWED("6", "This account is not allowed."),
	AUTHENTICATION_INFORMATION_MISSING("7", "Authentication information are missing."),
	AUTHENTICATION_INFORMATION_INVALID("8", "Invalid authentication information."),
	EXPIRED_TIMESTAMP("9", "Expired timestamp."),
	AUTHORIZER_PAYLOAD_INVALID("10", "Authorizer payload is invalid."),
	AUTHORIZER_CHECK_FAILED("11", "Authorizer check failed."),
	INVALID_HEADER("12", "Header is invalid."),
	INVALID_QUERY_STRING("13", "Query string is invalid."),
	SSL_HANDSHAKE_FAILED("14", "SSL handshake failed: %s.");

	private String code;
	private String detail;
}
