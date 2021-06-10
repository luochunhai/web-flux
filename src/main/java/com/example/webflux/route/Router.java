package com.example.webflux.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.webflux.filter.BaseFilter;
import com.example.webflux.handler.CalculatorHandler;
import com.example.webflux.handler.CityHandler;
import reactor.core.publisher.Mono;

@Configuration
public class Router {

	@Bean
	public RouterFunction<ServerResponse> routeCity(CityHandler cityHandler, BaseFilter baseFilter) {
		return RouterFunctions
			.route(RequestPredicates.GET("/hello")
				.and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), cityHandler::helloCity)
			.filter(baseFilter);
	}

	@Bean
	@Autowired
	public RouterFunction<ServerResponse>routerFunction(final CalculatorHandler calculatorHandler) {
		return RouterFunctions.route(RequestPredicates.path("/calculator"), request ->
			request.queryParam("operator").map(operator ->
				Mono.justOrEmpty(ReflectionUtils.findMethod(CalculatorHandler.class, operator, ServerRequest.class))
					.flatMap(method -> (Mono<ServerResponse>) ReflectionUtils.invokeMethod(method, calculatorHandler, request))
					.switchIfEmpty(ServerResponse.badRequest().build())
					.onErrorResume(ex -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build()))
				.orElse(ServerResponse.badRequest().build()));
	}
}
