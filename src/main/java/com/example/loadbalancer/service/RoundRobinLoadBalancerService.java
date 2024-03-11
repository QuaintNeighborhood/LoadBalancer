package com.example.loadbalancer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class RoundRobinLoadBalancerService extends LoadBalancerServiceBase implements LoadBalancerService {
    private static final Logger LOG = Logger.getLogger(RoundRobinLoadBalancerService.class.getName());

    @Autowired
    private BackendServerManager backendServerManager;

    public RoundRobinLoadBalancerService(@Autowired final RestClient restClient) {
        super(restClient);
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
}
