package kea.dpang.auth.dto

data class UserInfo(
    var userIdx: Long,
    var userName: String,
    var mileage: Int,
    var personalChargedMileage: Int
)
