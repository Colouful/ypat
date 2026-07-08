package com.ypat.controller;

import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DependencyHealthControllerTest {

    @Test
    public void dependenciesReturnsServiceUnavailableWhenSystemApiCannotBeChosen() {
        DependencyHealthController controller = new DependencyHealthController();
        FakeLoadBalancerClient loadBalancerClient = new FakeLoadBalancerClient(null);
        ReflectionTestUtils.setField(controller, "loadBalancerClient", loadBalancerClient);

        ResponseEntity<Map<String, Object>> response = controller.dependencies();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("DOWN", response.getBody().get("status"));
        assertEquals("SYSTEM-API", loadBalancerClient.serviceId);
    }

    @Test
    public void dependenciesReturnsOkWhenSystemApiCanBeChosen() {
        DependencyHealthController controller = new DependencyHealthController();
        FakeLoadBalancerClient loadBalancerClient = new FakeLoadBalancerClient(new FakeServiceInstance());
        ReflectionTestUtils.setField(controller, "loadBalancerClient", loadBalancerClient);

        ResponseEntity<Map<String, Object>> response = controller.dependencies();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("SYSTEM-API", response.getBody().get("systemApi"));
    }

    private static class FakeLoadBalancerClient implements LoadBalancerClient {
        private final ServiceInstance serviceInstance;
        private String serviceId;

        FakeLoadBalancerClient(ServiceInstance serviceInstance) {
            this.serviceInstance = serviceInstance;
        }

        @Override
        public ServiceInstance choose(String serviceId) {
            this.serviceId = serviceId;
            return serviceInstance;
        }

        @Override
        public <T> T execute(String serviceId, LoadBalancerRequest<T> request) {
            try {
                return request.apply(serviceInstance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public <T> T execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest<T> request) {
            try {
                return request.apply(serviceInstance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public URI reconstructURI(ServiceInstance instance, URI original) {
            return original;
        }
    }

    private static class FakeServiceInstance implements ServiceInstance {
        @Override
        public String getServiceId() {
            return "SYSTEM-API";
        }

        @Override
        public String getHost() {
            return "127.0.0.1";
        }

        @Override
        public int getPort() {
            return 9081;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public URI getUri() {
            return URI.create("http://127.0.0.1:9081");
        }

        @Override
        public Map<String, String> getMetadata() {
            return Collections.emptyMap();
        }
    }
}
