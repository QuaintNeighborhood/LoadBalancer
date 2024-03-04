package com.example.loadbalancer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.logging.Logger;

@Service
public class LoadBalancerService {

    private static final Logger LOG = Logger.getLogger(LoadBalancerService.class.getName());

    @Autowired
    private EndpointService endpointSvc;

    public Map<String, Object> processRequest(final Map<String, Object> requestBody) {
        final String endpoint = endpointSvc.getNextEndpoint();
        LOG.info(String.format("Server with URI:%s processed request with body %s", endpoint, requestBody));
        return WebClient.builder()
                .build()
                .post()
                .uri(endpoint)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }
}
