package com.example.loadbalancer.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EndpointServiceTest {

    private EndpointService svc;

    @Test
    void getNextEndpoint() {
        final List<String> endpoints = Arrays.asList("endpoint1", "endpoint2", "endpoint3");
        svc = new EndpointService(endpoints);

        final String firstEndpoint = svc.getNextEndpoint();
        final String secondEndpoint = svc.getNextEndpoint();
        final String thirdEndpoint = svc.getNextEndpoint();
        final String fourthEndpoint = svc.getNextEndpoint();

        assertEquals("endpoint1", firstEndpoint);
        assertEquals("endpoint2", secondEndpoint);
        assertEquals("endpoint3", thirdEndpoint);
        assertEquals("endpoint1", fourthEndpoint); // Should wrap around to the first endpoint
    }
}