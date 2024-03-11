package com.example.loadbalancer.service;

import java.util.Map;

/**
 * Load balancer service which processes incoming requests
 */
public interface LoadBalancerService {

    /**
     * Processes incoming request for the load balancer
     *
     * @param requestBody request body of incoming request
     * @return response body
     */
    Map<String, Object> processRequest(final Map<String, Object> requestBody);
}
