package com.course.hsa;

import com.course.hsa.config.BeanstalkConfigurationProperties;
import com.course.hsa.config.RedisConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties({RedisConfigurationProperties.class, BeanstalkConfigurationProperties.class})
public class Application {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(Application.class, args);
	}

}
