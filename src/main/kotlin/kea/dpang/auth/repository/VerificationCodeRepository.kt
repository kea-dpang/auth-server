package kea.dpang.auth.repository

import kea.dpang.auth.entity.VerificationCode
import org.springframework.data.repository.CrudRepository

interface VerificationCodeRepository : CrudRepository<VerificationCode, String> {
}
