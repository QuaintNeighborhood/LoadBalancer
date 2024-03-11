package com.example.loadbalancer.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.logging.Logger;

abstract class LoadBalancerServiceBase {
    private static final Logger LOG = Logger.getLogger(LoadBalancerServiceBase.class.getName());

    private final RestClient restClient;

    public LoadBalancerServiceBase(final RestClient restClient) {
        this.restClient = restClient;
    }

    protected Map<String, Object> processRequest(final String uri, final Map<String, Object> requestBody) {
        LOG.info("Server:%s processing request with body %s".formatted(uri, requestBody));
        return restClient.post()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
