package kea.dpang.auth.feign

import feign.Headers
import feign.Param
import kea.dpang.auth.base.BaseResponse
import kea.dpang.auth.base.SuccessResponse
import kea.dpang.auth.feign.dto.MileageDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader

@Headers("X-DPANG-CLIENT-ID: {clientId}")
@FeignClient(name = "mileage-server")
interface MileageServiceFeignClient {

    /**
     * 사용자의 마일리지를 조회하는 메서드
     *
     * @param clientId 클라이언트 ID
     * @param userId 마일리지를 조회할 사용자의 ID
     * @return 마일리지 정보가 담긴 응답 객체
     */
    @GetMapping("/api/mileage/{userId}")
    fun getUserMileageInfo(
        @RequestHeader("X-DPANG-CLIENT-ID") clientId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<SuccessResponse<MileageDto>>

    @DeleteMapping("/api/mileage/{userId}")
    fun deleteMileageInfo(
        @Param("clientId") clientId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<BaseResponse>
}