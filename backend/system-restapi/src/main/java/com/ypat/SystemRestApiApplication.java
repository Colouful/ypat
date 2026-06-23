package com.ypat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * @author dingyinxin
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
@EnableHystrix
@EnableFeignClients(basePackages = "com.ypat")
public class SystemRestApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemRestApiApplication.class, args);
    }
}
