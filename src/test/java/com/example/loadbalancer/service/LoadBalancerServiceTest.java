package com.example.loadbalancer.service;

import com.example.loadbalancer.exception.NoServersAvailableException;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class LoadBalancerServiceTest {
    private static final Map<String, Object> REQ_BODY = new HashMap<>();
    private static final MockResponse SUCCESS_RES = new MockResponse().newBuilder()
            .body("{\"key\": \"value\"}")
            .addHeader("Content-Type", "application/json")
            .build();
    private static final MockResponse ERR_RES = new MockResponse().newBuilder()
            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .build();
    private static final WebClient webClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                    .responseTimeout(Duration.ofSeconds(1))))
            .build();

    private static MockWebServer mockBackEnd1;
    private static MockWebServer mockBackEnd2;

    @Mock
    private EndpointService endpointService;

    @InjectMocks
    private LoadBalancerService lbSvc = new LoadBalancerService(webClient);

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

        when(endpointService.getNumEndpoints()).thenReturn(2);
        final String mockEndpoint1 = "http://localhost:%s".formatted(mockBackEnd1.getPort());
        final String mockEndpoint2 = "http://localhost:%s".formatted(mockBackEnd2.getPort());
        when(endpointService.getNextEndpoint()).thenReturn(mockEndpoint1, mockEndpoint2);
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

        assertThrows(NoServersAvailableException.class, () -> lbSvc.processRequest(REQ_BODY));
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