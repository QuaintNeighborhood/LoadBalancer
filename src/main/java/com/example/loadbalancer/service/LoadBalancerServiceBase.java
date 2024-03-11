package com.example.loadbalancer.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Base class for LoadBalancerService implementations.
 */
abstract class LoadBalancerServiceBase implements LoadBalancerService {
    private static final Logger LOG = Logger.getLogger(LoadBalancerServiceBase.class.getName());

    private final RestClient restClient;

    final List<String> uris;

    public LoadBalancerServiceBase(final RestClient restClient, final List<String> uris) {
        this.restClient = restClient;
        this.uris = uris;
    }

    /**
     * Sends the request to the given server with request body
     *
     * @param uri         URI of underlying server to send request to
     * @param requestBody request body
     * @return response body
     */
    Map<String, Object> processRequest(final String uri, final Map<String, Object> requestBody) {
        LOG.info("Server:%s processing request with body %s".formatted(uri, requestBody));
        return restClient.post()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    int getNumURIs() {
        return uris.size();
    }
}
