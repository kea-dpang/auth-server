package kea.dpang.auth.config.feign

import feign.RequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NotificationFeignConfig {

    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            template.header("X-DPANG-CLIENT-ROLE", "SYSTEM")
        }
    }
}
