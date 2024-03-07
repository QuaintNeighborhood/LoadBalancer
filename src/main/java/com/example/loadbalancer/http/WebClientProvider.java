package com.example.loadbalancer.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientProvider {

    @Value("${http-client-response-timeout-seconds}")
    public int HTTP_CLIENT_RESPONSE_TIMEOUT_SECONDS;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient()))
                .build();
    }

    private HttpClient httpClient() {
        return HttpClient.create()
                .responseTimeout(Duration.ofSeconds(HTTP_CLIENT_RESPONSE_TIMEOUT_SECONDS));
    }
}
