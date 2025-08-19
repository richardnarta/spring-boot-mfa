package com.example.mfa.core.redis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class MultiRedisConfig {
    @Bean("redisTemplate")
    fun redisTemplate(
        @Qualifier("authRedisConnectionFactory") connectionFactory: RedisConnectionFactory
    ): StringRedisTemplate {
        return StringRedisTemplate(connectionFactory)
    }

    @Bean("authRedisProperties")
    @Primary
    @ConfigurationProperties(prefix = "spring.data.redis.auth")
    fun authRedisProperties(): RedisProperties {
        return RedisProperties()
    }

    @Bean("authRedisConnectionFactory")
    @Primary
    fun authRedisConnectionFactory(
        @Qualifier("authRedisProperties") properties: RedisProperties
    ): RedisConnectionFactory {
        return LettuceConnectionFactory(properties.host, properties.port)
    }

    @Bean("authRedisTemplate")
    @Primary
    fun authRedisTemplate(
        @Qualifier("authRedisConnectionFactory") connectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory

        // Use Jackson for serializing values
        val jackson2JsonRedisSerializer = Jackson2JsonRedisSerializer(jacksonObjectMapper(), Any::class.java)

        // Configure serializers
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = jackson2JsonRedisSerializer
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = jackson2JsonRedisSerializer

        template.afterPropertiesSet()
        return template
    }
}