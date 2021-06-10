package com.example.webflux.common.error;

import lombok.Getter;

@Getter
public class SummerException extends RuntimeException {
	private GatewayResponseErrorCode errorCode;
	private Throwable throwable;
	private String message;

	public SummerException(GatewayResponseErrorCode errorCode, Throwable throwable) {
		super(errorCode.toString(), throwable);
		this.errorCode = errorCode;
		this.throwable = throwable;
		this.message = errorCode.toString();
	}

	public SummerException(GatewayResponseErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
		this.message = message;
	}

	public static SummerException getSummerException(GatewayResponseErrorCode errorCode, Throwable throwable) {
		return new SummerException(errorCode, throwable);
	}

	public static SummerException getSummerException(GatewayResponseErrorCode errorCode, String message) {
		return new SummerException(errorCode, message);
	}
}
