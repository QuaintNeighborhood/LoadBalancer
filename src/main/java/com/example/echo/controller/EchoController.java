package com.example.echo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping(
        path = "api/v1",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class EchoController {
    private static final Logger LOG = Logger.getLogger(EchoController.class.getName());

    @PostMapping(
            value = "/echo",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Map<String, Object> echoEndpoint(@RequestBody Map<String, Object> requestBody) {
        LOG.info("Request with body: " + requestBody);
        return requestBody;
    }
}
