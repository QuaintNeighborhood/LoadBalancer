package com.example.loadbalancer.controller;

import com.example.loadbalancer.service.LoadBalancerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping(path = "api/v1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class LoadBalancerController {
    private static final Logger LOG = Logger.getLogger(LoadBalancerController.class.getName());

    @Autowired
    @Qualifier("roundRobin")
    private LoadBalancerService svc;

    @PostMapping(
            value = "/loadbalancer",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Map<String, Object> loadBalancerEndpoint(@RequestBody Map<String, Object> requestBody) {
        LOG.info("Request with body: " + requestBody);
        return svc.processRequest(requestBody);
    }
}
