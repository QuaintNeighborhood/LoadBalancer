package com.example.loadbalancer.service;

import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoundRobinLoadBalancerServiceTest {
    private static final Map<String, Object> REQ_BODY = new HashMap<>();
    private static final MockResponse SUCCESS_RES = new MockResponse().newBuilder()
            .body("{\"key\": \"value\"}")
            .addHeader("Content-Type", "application/json")
            .build();
    private static final MockResponse ERR_RES = new MockResponse().newBuilder()
            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .build();

    private static MockWebServer mockBackEnd1;
    private static MockWebServer mockBackEnd2;

    private LoadBalancerService lbSvc;

    private static ClientHttpRequestFactory clientHttpRequestFactory() {
        final JdkClientHttpRequestFactory clientHttpRequestFactory = new JdkClientHttpRequestFactory();
        clientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(1));
        return clientHttpRequestFactory;
    }

    @BeforeAll
    static void beforeAll() {
        REQ_BODY.put("key", "value");
    }

    @BeforeEach
    void beforeEach() throws IOException {
        mockBackEnd1 = new MockWebServer();
        mockBackEnd1.start();
        mockBackEnd2 = new MockWebServer();
        mockBackEnd2.start();

        final String mockURI1 = "http://localhost:%s".formatted(mockBackEnd1.getPort());
        final String mockURI2 = "http://localhost:%s".formatted(mockBackEnd2.getPort());

        lbSvc = new RoundRobinLoadBalancerService(
                RestClient.builder()
                        .requestFactory(clientHttpRequestFactory())
                        .build(),
                List.of(mockURI1, mockURI2)
        );
    }

    @AfterEach
    void afterEach() throws IOException {
        mockBackEnd1.shutdown();
        mockBackEnd2.shutdown();
    }

    @Test
    void processRequest_AllServersHealthy() {
        mockBackEnd1.enqueue(SUCCESS_RES);
        mockBackEnd2.enqueue(SUCCESS_RES);

        final Map<String, Object> response = lbSvc.processRequest(REQ_BODY);
        assertEquals(REQ_BODY, response);
        assertEquals(1, mockBackEnd1.getRequestCount());
        assertEquals(0, mockBackEnd2.getRequestCount());
    }

    @Test
    void testProcessRequest_AllServersFail() {
        mockBackEnd1.enqueue(ERR_RES);
        mockBackEnd2.enqueue(ERR_RES);

        assertThrows(ResponseStatusException.class, () -> lbSvc.processRequest(REQ_BODY));
        assertEquals(1, mockBackEnd1.getRequestCount());
        assertEquals(1, mockBackEnd2.getRequestCount());
    }

    @Test
    void testProcessRequest_OneServerFail() {
        mockBackEnd1.enqueue(ERR_RES);
        mockBackEnd2.enqueue(SUCCESS_RES);

        final Map<String, Object> response = lbSvc.processRequest(REQ_BODY);
        assertEquals(REQ_BODY, response);
        assertEquals(1, mockBackEnd1.getRequestCount());
        assertEquals(1, mockBackEnd2.getRequestCount());
    }

    @Test
    void testProcessRequest_OneServerIsSlow() {
        final MockResponse delayedRes = new MockResponse().newBuilder()
                .addHeader("Content-Type", "application/json")
                .headersDelay(2, TimeUnit.SECONDS)
                .body("{\"key\": \"value\"}")
                .build();
        mockBackEnd1.enqueue(delayedRes);
        mockBackEnd2.enqueue(SUCCESS_RES);

        final Map<String, Object> response = lbSvc.processRequest(REQ_BODY);
        assertEquals(REQ_BODY, response);
        assertEquals(1, mockBackEnd1.getRequestCount());
        assertEquals(1, mockBackEnd2.getRequestCount());
    }
}