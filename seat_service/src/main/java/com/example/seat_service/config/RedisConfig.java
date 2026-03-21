package com.example.seat_service.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration
                .defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(GenericJacksonJsonRedisSerializer.builder().build()));

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                "cities",        defaults.entryTtl(Duration.ofHours(24)),
                "venues",        defaults.entryTtl(Duration.ofHours(24)),
                "seats",         defaults.entryTtl(Duration.ofHours(24)),
                "events",        defaults.entryTtl(Duration.ofMinutes(5)),
                "eventDetail",   defaults.entryTtl(Duration.ofMinutes(5)),
                "eventsAdmin",   defaults.entryTtl(Duration.ofMinutes(1)),
                "userBookings",  defaults.entryTtl(Duration.ofMinutes(1)),
                "bookingDetail", defaults.entryTtl(Duration.ofMinutes(2)),
                "seatMap",       defaults.entryTtl(Duration.ofSeconds(30))
        );

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}