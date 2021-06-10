package com.example.webflux.config;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.web.util.UriComponentsBuilder;

import com.example.webflux.common.error.GatewayResponseErrorCode;
import com.example.webflux.common.error.SummerException;
import com.google.common.base.Charsets;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UriBuilderFactory implements org.springframework.web.util.UriBuilderFactory {
	@Override
	public UriComponentsBuilder uriString(String uriTemplate) {
		return UriComponentsBuilder.fromUriString(uriTemplate);
	}

	@Override
	public UriComponentsBuilder builder() {
		return UriComponentsBuilder.newInstance();
	}

	@Override
	public URI expand(String uriTemplate, Map<String, ?> uriVariables) {
		return expand(uriTemplate, () -> uriVariables);
	}

	@Override
	public URI expand(String uriTemplate, Object... uriVariables) {
		return expand(uriTemplate, () -> uriVariables);
	}

	public <T> URI expand(String uriTemplate, Supplier<T> uriVariablesSupplier) {
		T uriVariables = uriVariablesSupplier.get();
		try {
			return new URI(UriComponentsBuilder.fromHttpUrl(uriTemplate).build().expand(uriVariables).toUriString());
		} catch (URISyntaxException e) {
			try {
				String decode = URLDecoder.decode(uriTemplate, Charsets.UTF_8.name());
				String encodeUrl = UriComponentsBuilder.fromHttpUrl(decode).build().expand(uriVariables).encode().toUriString();
				log.warn("Url encode:[{},{}]", uriTemplate, encodeUrl);
				return new URI(encodeUrl);
			} catch (URISyntaxException | IllegalArgumentException | UnsupportedEncodingException exception) {
				log.error(exception.getMessage() + ":" + uriTemplate, exception); // log error for monitor
				throw SummerException.getSummerException(GatewayResponseErrorCode.BAD_REQUEST_EXCEPTION, exception);
			}
		}
	}
}
