// RestTemplateConfig.java
package com.dfs.storage.config;

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
// update: 2026-05-14 23:30:30.154207

// update: 2026-05-14 23:30:37.329255

// update: 2026-05-14 23:30:52.916399

// update: 2026-05-14 23:30:54.698406

// update: 2026-05-14 23:30:57.851396
