package com.example.webflux;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.example.webflux.entity.Person;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@RunWith(SpringRunner.class)
class PerformanceWebfluxApplicationTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceWebfluxApplicationTests.class);
	private static ObjectMapper mapper = new ObjectMapper();
	private static Random r = new Random();
	private static int i = 0;

	@Rule
	public TestRule benchmarkRun = new BenchmarkRule();
	@Autowired
	TestRestTemplate template;

	public static MockWebServer mockBackEnd;

	@BeforeClass
	public static void setUp() throws IOException {
		final Dispatcher dispatcher = new Dispatcher() {

			@NotNull
			@Override
			public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) throws InterruptedException {
				String pathParam = recordedRequest.getPath().replaceAll("/slow/", "");

				List<Person> personsPart2 = new ArrayList<>();
				personsPart2.add(new Person(r.nextInt(100000), "Name" + pathParam, "Surname" + pathParam, r.nextInt(100)));
				personsPart2.add(new Person(r.nextInt(100000), "Name" + pathParam, "Surname" + pathParam, r.nextInt(100)));
				personsPart2.add(new Person(r.nextInt(100000), "Name" + pathParam, "Surname" + pathParam, r.nextInt(100)));

				try {
					return new MockResponse()
						.setResponseCode(200)
						.setBody(mapper.writeValueAsString(personsPart2))
						.setHeader("Content-Type", "application/json")
						.setBodyDelay(200, TimeUnit.MILLISECONDS);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		mockBackEnd = new MockWebServer();
		mockBackEnd.setDispatcher(dispatcher);
		mockBackEnd.start();
		System.setProperty("target.uri", "http://localhost:" + mockBackEnd.getPort());
	}

	@AfterClass
	public static void tearDown() throws IOException {
		mockBackEnd.shutdown();
	}

	@org.junit.Test
	@BenchmarkOptions(warmupRounds = 10, concurrency = 50, benchmarkRounds = 300)
	public void testPerformance() throws InterruptedException {
		ResponseEntity<Person[]> r = template.exchange("/persons/integration/{param}", HttpMethod.GET, null, Person[].class, ++i);
		Assert.assertEquals(200, r.getStatusCodeValue());
		Assert.assertNotNull(r.getBody());
		Assert.assertEquals(6, r.getBody().length);
	}

	@org.junit.Test
	@BenchmarkOptions(warmupRounds = 10, concurrency = 50, benchmarkRounds = 30000)
	public void testPerformanceInDifferentPool() throws InterruptedException {
		ResponseEntity<Person[]> r = template.exchange("/persons/integration-in-different-pool/{param}", HttpMethod.GET, null, Person[].class, ++i);
		Assert.assertEquals(200, r.getStatusCodeValue());
		Assert.assertNotNull(r.getBody());
		Assert.assertEquals(6, r.getBody().length);
	}

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
			.subscribe(System.out::println);
	}

}
