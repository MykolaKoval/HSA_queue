package com.course.hsa.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "queue.beanstalk")
public class BeanstalkConfigurationProperties {

    private String host;
    private Integer port;
}
