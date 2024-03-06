package com.example.loadbalancer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Service
public class EndpointService {

    private static final Logger LOG = Logger.getLogger(EndpointService.class.getName());
    private final AtomicInteger index = new AtomicInteger(0);
    private final List<String> endpoints;

    public EndpointService(@Value("#{'${endpoints}'.split(',')}") final List<String> endpoints) {
        this.endpoints = endpoints;
    }

    public String getNextEndpoint() {
        final int size = endpoints.size();
        int index = getNextIndex();
        final String endpoint = endpoints.get(index);
        LOG.fine("Endpoint index: %d/%d, endpoint: %s".formatted(index + 1, size, endpoint));
        return endpoint;
    }

    private int getNextIndex() {
        final int curIndex = index.getAndIncrement();
        index.compareAndSet(endpoints.size(), 0);
        return curIndex;
    }
}
