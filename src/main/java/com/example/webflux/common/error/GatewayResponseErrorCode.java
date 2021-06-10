
package com.example.webflux.common.error;

import java.beans.ConstructorProperties;

public enum GatewayResponseErrorCode {
	BAD_REQUEST_EXCEPTION(400, "100", "Bad Request Exception"),
	AUTHENTICATION_FAILED(401, "200", "Authentication Failed"),
	PERMISSION_DENIED(401, "210", "Permission Denied"),
	FORBIDDEN(403, "230", "Forbidden"),
	NOT_FOUND_EXCEPTION(404, "300", "Not Found Exception"),
	QUOTA_EXCEEDED(429, "400", "Quota Exceeded"),
	THROTTLE_LIMITED(429, "410", "Throttle Limited"),
	RATE_LIMITED(429, "420", "Rate Limited"),
	REQUEST_ENTITY_TOO_LARGE(413, "430", "Request Entity Too Large"),
	ENDPOINT_ERROR(503, "500", "Endpoint Error"),
	ENDPOINT_TIMEOUT(504, "510", "Endpoint Timeout"),
	UNKNOWN_ENDPOINT_DOMAIN(503, "520", "Unknown Endpoint Domain"),
	CONNECTION_CLOSED_BY_ENDPOINT(503, "530", "Connection Closed By Endpoint"),
	UNEXPECTED_ERROR(500, "900", "Unexpected Error");

	private int statusCode;
	private GatewayResponseErrorCode.Message message;

	private GatewayResponseErrorCode(int statusCode, String errorCode, String message) {
		this.statusCode = statusCode;
		this.message = new GatewayResponseErrorCode.Message(new GatewayResponseErrorCode.Message.Error(errorCode, message));
	}

	public int getStatusCode() {
		return this.statusCode;
	}

	public GatewayResponseErrorCode.Message getMessage() {
		return this.message;
	}

	public static class Message {
		private final GatewayResponseErrorCode.Message.Error error;

		@ConstructorProperties({"error"})
		public Message(GatewayResponseErrorCode.Message.Error error) {
			this.error = error;
		}

		public GatewayResponseErrorCode.Message.Error getError() {
			return this.error;
		}

		public static class Error {
			private final String errorCode;
			private final String message;

			@ConstructorProperties({"errorCode", "message"})
			public Error(String errorCode, String message) {
				this.errorCode = errorCode;
				this.message = message;
			}

			public String getErrorCode() {
				return this.errorCode;
			}

			public String getMessage() {
				return this.message;
			}
		}
	}
}
