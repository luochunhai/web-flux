package com.example.webflux;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest
class WebfluxApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	public void testHttpWebClient() {

		final WebClient client = WebClient.create("http://localhost:8080/calculator");
		final Mono<String> mono = client.get()
			.uri("?operator=add&v1=1&v2=3")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.flatMap(response -> response.bodyToMono(String.class));
		System.out.println(mono.block());
	}

	@Test
	public void testServerSentEvent() {
		final WebClient client = WebClient.create();
		client.get()
			.uri("http://localhost:8080/sse/randomNumbers")
			.accept(MediaType.TEXT_EVENT_STREAM)
			.exchange()
			.flatMapMany(response -> response.body(BodyExtractors.toFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
			})))
			.filter(sse -> Objects.nonNull(sse.data()))
			.map(ServerSentEvent::data)
			.buffer(10)
			.doOnNext(System.out::println)
			.blockFirst();
	}

	@Test
	public void testWebSocketClient() {
		Flux.range(1, 10).subscribe(System.out::println);
		final WebSocketClient client = new ReactorNettyWebSocketClient();
		client.execute(URI.create("ws://localhost:8080/echo"), session ->
			session.send(Flux.just(session.textMessage("Hello")))
				.thenMany(session.receive().take(1).map(WebSocketMessage::getPayloadAsText))
				.doOnNext(System.out::println)
				.then())
			.block(Duration.ofMillis(5000));
	}

	@Test
	public void testFluxAndMono() {

		Flux.just("a", "b")
			.zipWith(Flux.just("c", "d"))
			.subscribe(System.out::println);
		Flux.just("a", "b")
			.zipWith(Flux.just("c", "d"), (s1, s2) -> String.format("%s-%s", s1, s2))
			.subscribe(System.out::println);}



}
