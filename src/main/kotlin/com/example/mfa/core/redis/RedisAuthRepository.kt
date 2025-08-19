package com.example.mfa.core.redis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class RedisAuthRepository(
    @param:Qualifier("authRedisTemplate") private val redisTemplate: RedisTemplate<String, Any>
) {
    fun <T : Any> set(key: String, value: T, expiryInMs: Long = 15L * 60L * 1000L) {
        redisTemplate.opsForValue().set(key, value, expiryInMs, TimeUnit.MILLISECONDS)
    }

    fun <T : Any> get(key: String, clazz: Class<T>): T? {
        val value = redisTemplate.opsForValue().get(key)
        // Safely cast the retrieved object to the desired type
        return if (clazz.isInstance(value)) {
            clazz.cast(value)
        } else {
            try {
                jacksonObjectMapper().convertValue(value, clazz)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun sAdd(key: String, vararg values: Any): Long? {
        return redisTemplate.opsForSet().add(key, *values)
    }

    fun sRem(key: String, vararg values: Any): Long? {
        return redisTemplate.opsForSet().remove(key, *values)
    }

    fun <T : Any> sMembers(key: String, clazz: Class<T>): Set<T>? {
        val members = redisTemplate.opsForSet().members(key) ?: return null

        val resultSet = mutableSetOf<T>()
        val objectMapper = jacksonObjectMapper()

        for (member in members) {
            val convertedMember = if (clazz.isInstance(member)) {
                clazz.cast(member)
            } else {
                try {
                    objectMapper.convertValue(member, clazz)
                } catch (_: Exception) {
                    continue
                }
            }
            resultSet.add(convertedMember)
        }
        return resultSet
    }

    fun sCard(key: String): Long {
        return redisTemplate.opsForSet().size(key) ?: 0L
    }

    fun del(key: String) {
        redisTemplate.delete(key)
    }
}