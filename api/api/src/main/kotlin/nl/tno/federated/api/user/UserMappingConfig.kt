package nl.tno.federated.api.user

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.DefaultResourceLoader
import java.nio.charset.StandardCharsets

@ConfigurationProperties(prefix = "federated.node.user.admin")
class UserMappingConfig(val adminUser: AdminUser) {
    class AdminUser {
        lateinit var username: String
        lateinit var encryptedPassword: String
        var roles: String? = null

        fun toUser() = User(username,encryptedPassword, roles)
    }
}