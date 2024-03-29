package kea.dpang.auth.entity

import jakarta.persistence.*
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
    @Column(name = "user_id")
    var id: Long? = null,

    var email: String? = null,

    var password: String? = null,

    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER

) : BaseEntity()
