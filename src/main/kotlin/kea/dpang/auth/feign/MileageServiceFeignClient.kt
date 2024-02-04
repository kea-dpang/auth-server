package kea.dpang.auth.feign

import feign.Headers
import feign.Param
import kea.dpang.auth.base.BaseResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable

@Headers("X-DPANG-CLIENT-ID: {clientId}")
@FeignClient(name = "mileage-server")
fun interface MileageServiceFeignClient {

    @DeleteMapping("/api/mileage/{userId}")
    fun deleteMileageInfo(
        @Param("clientId") clientId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<BaseResponse>
}