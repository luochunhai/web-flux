package com.example.webflux.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "endpoint.web-client")
public class WebClientConfig {
	private int connectionTimeoutSec;
	private int streamReadTimeoutSec;
	private int defaultReadTimeoutSec;
	private int idleTimeoutSec;
	private int maxPoolSize;

	@Bean("httpClient")
	public HttpClient httpClient() {
		return createHttpClient("webClient-pool", defaultReadTimeoutSec);
	}

	@Bean("streamHttpClient")
	public HttpClient streamHttpClient() {
		return createHttpClient("streamWebClient-pool", streamReadTimeoutSec);
	}

	@Primary
	@Bean("webClient")
	public WebClient webClient(HttpClient httpClient) {
		return createWebClient(httpClient);
	}

	@Bean("webClient")
	public WebClient streamWebClient() {
		return createWebClient(streamHttpClient());
	}

	@Bean("filterWebClient")
	public WebClient filterWebClient() {
		return createWebClient(createHttpClient("filterWebClient-pool", defaultReadTimeoutSec));
	}

	private WebClient createWebClient(HttpClient httpClient) {
		return WebClient.builder()
			.filter(new LogFilter())
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.exchangeStrategies(ExchangeStrategies.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1)).build())
			.uriBuilderFactory(new UriBuilderFactory())
			.build();
	}

	private HttpClient createHttpClient(String poolName, int timeoutSec) {
		return HttpClient
			.create(ConnectionProvider.builder(poolName)
				.maxConnections(maxPoolSize)
				.maxIdleTime(Duration.of(idleTimeoutSec, ChronoUnit.SECONDS))
				.build()
			)
			.tcpConfiguration(tcpClient -> tcpClient
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int)TimeUnit.SECONDS.toMillis(connectionTimeoutSec))
				.option(ChannelOption.SO_KEEPALIVE, false)
				.doOnConnected(connection -> connection
					.addHandlerLast(new ReadTimeoutHandler(timeoutSec))
					.markPersistent(false)
				)
			)
			.followRedirect(false)
			.keepAlive(false)
			.compress(true);
	}
}
