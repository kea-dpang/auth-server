package kea.dpang.auth.redis.entity

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("RefreshToken", timeToLive = 60 * 60 * 24 * 5) // 5일
data class RefreshToken(
    // 사용자 식별자
    @Id @Indexed
    var id: Long,

    // 사용자 리프레쉬 토큰
    var refreshToken: String
)

/*
<Redis>

RefreshToken{
    "userId1" : "refreshToken1",
    "userId2" : "refreshToken2",
    ...
}
 */