package kea.dpang.auth.dto

import kea.dpang.auth.base.Role
import java.time.LocalDate

data class RegisterRequestDto(
    val email: String,
    val password: String,
    val role: Role?,
    val name: String,
    val employeeNumber: String,
    val joinDate: LocalDate
)
