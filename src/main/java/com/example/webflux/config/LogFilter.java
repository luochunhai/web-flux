package com.example.webflux.config;

import java.net.URI;
import java.util.Optional;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class LogFilter implements ExchangeFilterFunction {
	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		URI url = request.url();
		Optional<String> traceIdOption = Optional.ofNullable(request.headers().getFirst("trace-id"));
		log.info("{} Request: {} {}", traceIdOption.orElse(request.logPrefix()), request.method(), url);
		return next.exchange(request).flatMap(clientResponse -> {
			log.info("{} Response:{} {} {}", traceIdOption.orElse(clientResponse.logPrefix()), request.method(), url, clientResponse.statusCode());
			return Mono.just(clientResponse);
		});
	}
}
