package com.course.hsa.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.JedisPooled;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisConfigurationProperties redisConfiguration;

    @Bean
    public JedisPooled jedisClient() {
        return new JedisPooled(connectionPoolConfig(), redisConfiguration.getHost(),
                redisConfiguration.getPort(), redisConfiguration.getUser(), redisConfiguration.getPassword());
    }

    private ConnectionPoolConfig connectionPoolConfig() {
        ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        // maximum active connections in the pool,
        // tune this according to your needs and application type
        // default is 8
        poolConfig.setMaxTotal(100);

        // maximum idle connections in the pool, default is 8
        poolConfig.setMaxIdle(8);

        // minimum idle connections in the pool, default 0
        poolConfig.setMinIdle(0);

        // Enables waiting for a connection to become available.
        poolConfig.setBlockWhenExhausted(true);

        // The maximum number of seconds to wait for a connection to become available
        poolConfig.setMaxWait(Duration.ofSeconds(1));

        // Enables sending a PING command periodically while the connection is idle.
        poolConfig.setTestWhileIdle(true);

        return poolConfig;
    }
}
