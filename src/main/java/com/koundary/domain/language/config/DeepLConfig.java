package com.koundary.domain.language.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(DeepLProperties.class)
public class DeepLConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
