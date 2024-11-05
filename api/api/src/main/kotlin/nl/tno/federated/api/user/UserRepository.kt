package nl.tno.federated.api.user

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


@Repository
interface UserRepository : CrudRepository<UserEntity, Long> {

    fun findByUserName(username: String): UserEntity?
}