package com.example.loadbalancer.service;

import com.example.loadbalancer.exception.NoServersAvailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class LoadBalancerService {
    private static final Logger LOG = Logger.getLogger(LoadBalancerService.class.getName());

    @Autowired
    private EndpointService endpointSvc;

    public Map<String, Object> processRequest(final Map<String, Object> requestBody) {
        for (int numFailedServers = 0; numFailedServers < endpointSvc.getNumEndpoints(); numFailedServers++) {
            final String endpoint = endpointSvc.getNextEndpoint();
            try {
                return processRequest(endpoint, requestBody);
            } catch (final WebClientResponseException | WebClientRequestException webEx) {
                // retry this request with another server
                LOG.log(
                        Level.WARNING,
                        "Server:%s failed to process, retrying this request...".formatted(endpoint),
                        webEx
                );
            } catch (final Exception e) {
                LOG.severe("Server:%s unrecoverable exception while processing requestBody:%s"
                        .formatted(endpoint, requestBody));
                throw e;
            }
        }
        throw new NoServersAvailableException("All servers failed to respond");
    }

    private Map<String, Object> processRequest(final String endpoint, final Map<String, Object> requestBody) {
        LOG.info("Server:%s processing request with body %s".formatted(endpoint, requestBody));
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
