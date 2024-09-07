package com.course.hsa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@EnableScheduling
@Configuration
public class SchedulerConfig {

    @Bean
    public ScheduledExecutorService scheduler() {
        return Executors.newScheduledThreadPool(5);
    }
}
