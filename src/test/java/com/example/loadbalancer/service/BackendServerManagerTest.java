package com.example.loadbalancer.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BackendServerManagerTest {

    private BackendServerManager mgr;

    @Test
    void getNumEndpoints() {
        final List<String> oneURIs = List.of("uri1");
        mgr = new BackendServerManager(oneURIs);
        assertEquals(1, mgr.getNumURIs());

        final List<String> twoURIs = Arrays.asList("uri1", "uri2");
        mgr = new BackendServerManager(twoURIs);
        assertEquals(2, mgr.getNumURIs());
    }

    @Test
    void getNextEndpoint() {
        final List<String> uris = Arrays.asList("uri1", "uri2", "uri3");
        mgr = new BackendServerManager(uris);

        final String firstURI = mgr.getNextURI();
        final String secondURI = mgr.getNextURI();
        final String thirdURI = mgr.getNextURI();
        final String fourthURI = mgr.getNextURI();

        assertEquals("uri1", firstURI);
        assertEquals("uri2", secondURI);
        assertEquals("uri3", thirdURI);
        assertEquals("uri1", fourthURI); // Should wrap around to the first uri
    }
}