package nl.tno.federated.api.user

import nl.tno.federated.api.event.type.EventTypeServiceException
import org.springframework.stereotype.Service
import java.util.*

class UserServiceException(msg: String) : Exception(msg)

@Service
class UserService(private val userRepository: UserRepository) {

    fun addUser(user: User): UserEntity {
        val current = userRepository.findByUserName(username = user.username)
        if( current != null) throw EventTypeServiceException("User with username '${user.username}' already exists")
        return userRepository.save(UserEntity(username = user.username, encryptedPassword = user.encryptedPassword, roles = user.roles))
    }

    fun deleteUser(userName: String) {
        val current = userRepository.findByUserName(username = userName)
            ?: throw EventTypeServiceException("No user found: ${userName}")
        userRepository.delete(current)
    }

    fun updateUserPassword(userName: String, encryptedPassword: String) {
        val current = userRepository.findByUserName(username = userName)
            ?: throw EventTypeServiceException("No user found: ${userName}")
        val copy = current.copy(encryptedPassword = encryptedPassword)
        userRepository.save(copy)
    }

    fun updateRoles(userName: String, roles: String) {
        val current = userRepository.findByUserName(username = userName)
            ?: throw EventTypeServiceException("No user found: ${userName}")
        val copy = current.copy(roles = roles)
        userRepository.save(copy)
    }

}

