package com.example.loadbalancer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Round Robin Load Balancer Service implementation.
 */
@Service
public class RoundRobinLoadBalancerService extends LoadBalancerServiceBase {
    private static final Logger LOG = Logger.getLogger(RoundRobinLoadBalancerService.class.getName());

    private final AtomicInteger index = new AtomicInteger(0);

    public RoundRobinLoadBalancerService(
            @Autowired final RestClient restClient,
            @Value("#{'${loadbalancer.uris}'.split(',')}") final List<String> uris
    ) {
        super(restClient, uris);
    }

    /**
     * Sends a request to the next server in a round-robin manner. If a recoverable exception
     * is thrown, the request is retried with the next server. If all servers fail, a ResponseStatusException
     * with 503 HTTP status code is thrown.
     *
     * @param requestBody request body of incoming request
     * @return response body
     * @throws ResponseStatusException if all servers fail to respond
     */
    public Map<String, Object> processRequest(final Map<String, Object> requestBody) {
        final int numURIs = getNumURIs();
        for (int numFailedServers = 0; numFailedServers < numURIs; numFailedServers++) {
            final String uri = getNextURI();
            try {
                return processRequest(uri, requestBody);
            } catch (final RestClientException restEx) {
                // retry this request with another server
                LOG.log(
                        Level.WARNING,
                        "Server:%s failed to process, retrying this request...".formatted(uri),
                        restEx
                );
            }
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "All servers failed to respond");
    }

    private String getNextURI() {
        final int size = uris.size();
        int index = getNextIndex();
        final String uri = uris.get(index);
        LOG.fine("Backend server index: %d/%d, uri: %s".formatted(index + 1, size, uri));
        return uri;
    }

    private int getNextIndex() {
        final int curIndex = index.getAndIncrement();
        index.compareAndSet(uris.size(), 0);
        return curIndex;
    }
}
