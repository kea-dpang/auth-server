package kea.dpang.auth.redis.repository

import kea.dpang.auth.redis.entity.RefreshToken
import org.springframework.data.repository.CrudRepository

interface RefreshTokenRepository : CrudRepository<RefreshToken, Int> {
}