package kea.dpang.auth.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = arrayOf("kea.dpang.auth.repository"))
class JpaConfig {
    // JPA 설정이 필요한 경우 여기서 진행
}