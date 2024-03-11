package com.example.loadbalancer.service;

import java.util.Map;

public interface LoadBalancerService {
    Map<String, Object> processRequest(final Map<String, Object> requestBody);
}
