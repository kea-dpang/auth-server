package kea.dpang.auth.redis.repository

import kea.dpang.auth.redis.entity.VerificationCode
import org.springframework.data.repository.CrudRepository

interface VerificationCodeRepository : CrudRepository<VerificationCode, String>
