package com.ypat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DependencyHealthController {

    private static final String SYSTEM_API = "SYSTEM-API";

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @GetMapping("/internal/health/dependencies")
    public ResponseEntity<Map<String, Object>> dependencies() {
        ServiceInstance systemApi = loadBalancerClient.choose(SYSTEM_API);
        Map<String, Object> body = new HashMap<String, Object>(4);
        body.put("systemApi", SYSTEM_API);
        if (systemApi == null) {
            body.put("status", "DOWN");
            return new ResponseEntity<Map<String, Object>>(body, HttpStatus.SERVICE_UNAVAILABLE);
        }
        body.put("status", "UP");
        body.put("uri", systemApi.getUri().toString());
        return new ResponseEntity<Map<String, Object>>(body, HttpStatus.OK);
    }
}
