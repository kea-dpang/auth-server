package kea.dpang.auth.feign.dto

import java.time.LocalDate

data class RegisterUserRequestDto(
    val email: String,
    val employeeNumber: Long,
    val name: String,
    val joinDate: LocalDate
)
