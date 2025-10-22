package ru.practicum.ewm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.stats.client.StatsClient;

@Configuration
public class StatsClientConfig {

    @Bean
    public StatsClient statsClient(RestTemplateBuilder builder,
                                   @Value("${stats-service.url:http://localhost:9090}") String baseUrl) {
        return new StatsClient(builder.build(), baseUrl);
    }
}

