package kea.dpang.auth.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import kea.dpang.auth.base.BaseEntity
import kea.dpang.auth.base.Role
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
data class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var userIdx: Int? = null,

    var email: String? = null,

    var password: String? = null,

    var role: Role = Role.USER

) : BaseEntity()
