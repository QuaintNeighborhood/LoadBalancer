package com.example.loadbalancer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class LoadBalancerService {
    private static final Logger LOG = Logger.getLogger(LoadBalancerService.class.getName());

    @Autowired
    private BackendServerManager backendServerManager;

    private final RestClient restClient;

    public LoadBalancerService(final RestClient restClient) {
        this.restClient = restClient;
    }

    public Map<String, Object> processRequest(final Map<String, Object> requestBody) {
        for (int numFailedServers = 0; numFailedServers < backendServerManager.getNumURIs(); numFailedServers++) {
            final String uri = backendServerManager.getNextURI();
            try {
                return processRequest(uri, requestBody);
            } catch (final RestClientException restEx) {
                // retry this request with another server
                LOG.log(
                        Level.WARNING,
                        "Server:%s failed to process, retrying this request...".formatted(uri),
                        restEx
                );
            } catch (final Exception e) {
                LOG.severe("Server:%s unrecoverable exception while processing requestBody:%s"
                        .formatted(uri, requestBody));
                throw e;
            }
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "All servers failed to respond");
    }

    private Map<String, Object> processRequest(final String uri, final Map<String, Object> requestBody) {
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
