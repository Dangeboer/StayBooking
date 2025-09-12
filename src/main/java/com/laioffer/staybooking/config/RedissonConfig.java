package com.laioffer.staybooking.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String redisUrl = "redis://" + redisHost + ":" + redisPort;
        // 最后拼出来的URL：redis://localhost:6379
        // RedissonClient 就知道去哪个地址、哪个端口、用什么密码去连接 Redis 了
        
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.useSingleServer()
                    .setAddress(redisUrl)
                    .setPassword(redisPassword)
                    .setDatabase(0);
        } else {
            config.useSingleServer()
                    .setAddress(redisUrl)
                    .setDatabase(0);
        }
        
        return Redisson.create(config);
    }
}
