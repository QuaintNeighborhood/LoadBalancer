package com.example.echoapi.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping(path = "api", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
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
