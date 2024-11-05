package nl.tno.federated.api.user

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties


@ConditionalOnProperty(prefix = "federated.node.api.security.user", name = ["enabled"], havingValue = "true")
@ConfigurationProperties(prefix = "federated.node.api.security.user")
class UserMappingConfig(val users: List<FedUser>) {
    class FedUser {
        lateinit var userName: String
        lateinit var encryptedPassword: String
        var roles: String? = null

        fun toUser() = User(userName,encryptedPassword, roles)
    }
}