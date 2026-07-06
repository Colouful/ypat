package com.ypat.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfiguration {

    @Bean
    public StorageService storageService(CosStorageProperties properties) {
        properties.validate();
        if (properties.isCosEnabled()) {
            return new CosStorageService(properties);
        }
        return new FastDfsStorageService();
    }
}
