package com.example.loadbalancer.controller;

import com.example.loadbalancer.service.LoadBalancerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping(path = "api", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class LoadBalancerController {
    private static final Logger LOG = Logger.getLogger(LoadBalancerController.class.getName());

    @Autowired
    private LoadBalancerService svc;

    @PostMapping(
            value = "/roundrobin",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Map<String, Object> roundRobinEndpoint(@RequestBody Map<String, Object> requestBody) {
        LOG.info("Request with body: " + requestBody);
        return svc.processRequest(requestBody);
    }
}