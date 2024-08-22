package nl.tno.federated.api.user

import jakarta.validation.constraints.NotNull


data class User(@NotNull val username: String, @NotNull val encryptedPassword: String,val roles: String? = null)
