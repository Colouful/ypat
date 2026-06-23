package com.ypat;

import com.ypat.config.SystemConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
@EnableFeignClients(basePackages = "com.ypat")
@EnableConfigurationProperties({SystemConfig.class})
@EnableCaching
@EnableRedisHttpSession
public class SystemWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemWebApplication.class, args);
    }

}
