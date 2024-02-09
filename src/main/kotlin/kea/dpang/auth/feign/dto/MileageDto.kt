package kea.dpang.auth.feign.dto

data class MileageDto(
    var userId: Long,
    var mileage: Int,
    var personalChargedMileage: Int
)