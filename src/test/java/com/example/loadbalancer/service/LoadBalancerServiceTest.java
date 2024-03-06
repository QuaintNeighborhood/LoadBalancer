package com.example.loadbalancer.service;

import com.example.loadbalancer.exception.NoServersAvailableException;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadBalancerServiceTest {

    private static MockWebServer mockBackEnd;
    @Mock
    private EndpointService endpointService;
    @InjectMocks
    private LoadBalancerService lbSvc;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    void processRequest_AllServersHealthy() {
        when(endpointService.getNumEndpoints()).thenReturn(1);
        final String mockEndpoint = String.format("http://localhost:%s", mockBackEnd.getPort());
        when(endpointService.getNextEndpoint()).thenReturn(mockEndpoint);

        final Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("key", "value");

        mockBackEnd.enqueue(new MockResponse().newBuilder()
                .body("{\"key\": \"value\"}")
                .addHeader("Content-Type", "application/json")
                .build()
        );

        final Map<String, Object> response = lbSvc.processRequest(requestBody);
        assertEquals(requestBody, response);
    }

    @Test
    void testProcessRequest_AllServersFail() {
        when(endpointService.getNumEndpoints()).thenReturn(2);
        final String mockEndpoint = String.format("http://localhost:%s", mockBackEnd.getPort());
        when(endpointService.getNextEndpoint()).thenReturn(mockEndpoint);
        when(endpointService.getNextEndpoint()).thenReturn(mockEndpoint);

        mockBackEnd.enqueue(new MockResponse().newBuilder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build()
        );
        mockBackEnd.enqueue(new MockResponse().newBuilder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build()
        );

        assertThrows(NoServersAvailableException.class, () -> lbSvc.processRequest(Collections.emptyMap()));
    }

    @Test
    void testProcessRequest_OneServerFail() {
        when(endpointService.getNumEndpoints()).thenReturn(2);
        final String mockEndpoint = String.format("http://localhost:%s", mockBackEnd.getPort());
        when(endpointService.getNextEndpoint()).thenReturn(mockEndpoint);
        when(endpointService.getNextEndpoint()).thenReturn(mockEndpoint);

        mockBackEnd.enqueue(new MockResponse().newBuilder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build()
        );

        final Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("key", "value");
        mockBackEnd.enqueue(new MockResponse().newBuilder()
                .body("{\"key\": \"value\"}")
                .addHeader("Content-Type", "application/json")
                .build()
        );

        final Map<String, Object> response = lbSvc.processRequest(requestBody);
        assertEquals(requestBody, response);
    }
}