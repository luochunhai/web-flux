package com.example.webflux.service;

import com.nickolasfisher.webflux.model.WelcomeMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class FallbackService {

    private final WebClient serviceAWebClient;

    public FallbackService(@Qualifier("service-a-web-client")
                                   WebClient serviceAWebClient) {
        this.serviceAWebClient = serviceAWebClient;
    }

    public Mono<WelcomeMessage> getWelcomeMessageByLocale(String locale) {
        return this.serviceAWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/locale/{locale}/message").build(locale))
                .retrieve()
                .bodyToMono(WelcomeMessage.class)
                .onErrorReturn(
                        throwable -> throwable instanceof WebClientResponseException
                            && ((WebClientResponseException)throwable).getStatusCode().is5xxServerError(),
                        new WelcomeMessage("hello fallback!")
                );
    }
}
