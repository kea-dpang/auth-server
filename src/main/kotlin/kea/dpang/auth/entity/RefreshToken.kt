package kea.dpang.auth.entity

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("RefreshToken")
class RefreshToken protected constructor(
    id: String,
    refreshToken: String,
    expiration: Long
) {

    @Id
    var refreshToken: String = refreshToken
        protected set

    @Indexed
    var id: String = id
        protected set

    @TimeToLive
    var expiration: Long = expiration
        protected set

}

