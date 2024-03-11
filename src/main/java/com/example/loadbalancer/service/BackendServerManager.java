package com.example.loadbalancer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Service
public class BackendServerManager {
    private static final Logger LOG = Logger.getLogger(BackendServerManager.class.getName());

    private final AtomicInteger index = new AtomicInteger(0);
    private final List<String> uris;

    public BackendServerManager(@Value("#{'${loadbalancer.uris}'.split(',')}") final List<String> uris) {
        this.uris = uris;
    }

    public int getNumURIs() {
        return uris.size();
    }

    public String getNextURI() {
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
