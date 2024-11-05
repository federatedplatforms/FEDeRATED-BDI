package nl.tno.federated.api.user

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("USERS")
data class UserEntity(
    @Id
    var id: Long? = null,
    val userName: String,
    val encryptedPassword: String,
    val roles: String?,
)