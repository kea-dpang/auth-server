package kea.dpang.auth.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.StringRedisSerializer

@EnableCaching
@Configuration
@EnableRedisRepositories(basePackages = ["kea.dpang.auth.redis.repository"])
class RedisConfig {

    @Value("\${spring.data.redis.cluster.nodes}")
    private val nodes: String = ""

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val redisClusterConfiguration = RedisClusterConfiguration(nodes.split(","))
        return LettuceConnectionFactory(redisClusterConfiguration)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String, Any>()
        redisTemplate.connectionFactory = redisConnectionFactory()
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = StringRedisSerializer()
        return redisTemplate
    }
}
