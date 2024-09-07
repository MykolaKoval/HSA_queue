package com.course.hsa.config;

import com.dinstone.beanstalkc.BeanstalkClientFactory;
import com.dinstone.beanstalkc.JobConsumer;
import com.dinstone.beanstalkc.JobProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BeanstalkConfig {

    public static final String EVENTS_TUBE = "events";

    private final BeanstalkConfigurationProperties beanstalkConfigurationProperties;

    @Bean
    public BeanstalkClientFactory beanstalkClientFactory() {
        var config = new com.dinstone.beanstalkc.Configuration();
        config.setServiceHost(beanstalkConfigurationProperties.getHost());
        config.setServicePort(beanstalkConfigurationProperties.getPort());
        return new BeanstalkClientFactory(config);
    }

    @Bean
    public JobProducer jobProducer(BeanstalkClientFactory beanstalkClientFactory) {
        return beanstalkClientFactory.createJobProducer(EVENTS_TUBE);
    }

    @Bean
    public JobConsumer jobConsumer(BeanstalkClientFactory beanstalkClientFactory) {
        return beanstalkClientFactory.createJobConsumer(EVENTS_TUBE);
    }
}
