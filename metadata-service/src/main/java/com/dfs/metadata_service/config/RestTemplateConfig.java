// RestTemplateConfig.java
package com.dfs.metadata_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
// update: 2026-05-14 23:30:28.299459

// update: 2026-05-14 23:30:31.575615

// update: 2026-05-14 23:30:35.752210

// update: 2026-05-14 23:30:36.497723

// update: 2026-05-14 23:30:39.504937

// update: 2026-05-14 23:30:50.192266
