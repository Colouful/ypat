package com.ypat;

import com.ypat.config.SystemConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * @author dingyinxin
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
@EnableFeignClients(basePackages = "com.ypat")
@EnableConfigurationProperties({SystemConfig.class})
public class SystemWapApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemWapApplication.class, args);
    }

}
