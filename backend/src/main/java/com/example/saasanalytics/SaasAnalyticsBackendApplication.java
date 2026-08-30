package com.example.saasanalytics;

import com.example.saasanalytics.config.ClickHouseProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ClickHouseProperties.class)
public class SaasAnalyticsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaasAnalyticsBackendApplication.class, args);
    }
}
